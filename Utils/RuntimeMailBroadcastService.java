import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.mail.Address;
import javax.mail.MessagingException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import ru.aplix.posttrackingapi.services.api.RuntimeMailBroadcast;
import ru.aplix.posttrackingapi.services.database.BroadcastDBService;
import ru.aplix.posttrackingapi.services.entity.ApiKey;
import ru.aplix.posttrackingapi.services.entity.MailBroadcast;
import ru.aplix.posttrackingapi.services.model.base.WebOperation;
import ru.aplix.posttrackingapi.services.util.Logger;
import ru.aplix.posttrackingapi.services.util.ObjectPackCreator;
import ru.aplix.posttrackingapi.services.validation.TrackType;

public class RuntimeMailBroadcastService implements Runnable {
	
	@Autowired
	private Logger logger;
	@Autowired
	private BroadcastDBService broadcastService;
	
	protected Session session;	
	protected InternetAddress sender;	
	protected String subject;
	protected String messagePattern;	
	
	private Map<TrackType, RuntimeMailBroadcast> mailBroadcasts;
	
	public void setLogger(Logger logger) {
		this.logger = logger;
	}

	public void setBroadcastService(BroadcastDBService broadcastService) {
		this.broadcastService = broadcastService;
	}
	
	public void setMailBroadcasts(Map<TrackType, RuntimeMailBroadcast> mailBroadcasts) {
		this.mailBroadcasts = mailBroadcasts;
	}
	
	public void setSession(String session) {
		try {
			InitialContext ctx = new InitialContext();
			this.session = (Session) ctx.lookup(session);
		} catch (NamingException e) {						
			e.printStackTrace();
		}
	}

	public void setSender(String sender) {
		try {
			this.sender = new InternetAddress(sender);
		} catch (AddressException e) {
			e.printStackTrace();
		}
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setMessagePattern(Resource messagePattern) {
		StringBuilder msgTemplateStr = new StringBuilder();
		BufferedReader reader = null;
		try {
			reader = new BufferedReader(new InputStreamReader(new FileInputStream(messagePattern.getFile()), "UTF-8"));
			String line;
			while((line=reader.readLine())!=null)
				msgTemplateStr.append(line + "\r\n");
		} catch (IOException e) {
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		this.messagePattern = msgTemplateStr.toString();
	}
	
	@Override
	public void run() {
		try {
			this.sendEmailBroadcast();
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(RuntimeMailBroadcastService.class, "Internal server error email broadcast: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Send email broadcast
	 */	
	private void sendEmailBroadcast() {
		logger.info(RuntimeMailBroadcastService.class, "Email broadcast start");
		final List<MailBroadcast> mailAddresses = broadcastService.getActiveMailBroadcastSubscribers();
		for (final MailBroadcast broadcast : mailAddresses) {			
			InternetAddress recipient = null;
			try {
				recipient = new InternetAddress(broadcast.getEmail().trim());
			} catch (MessagingException e) {
				logger.info(RuntimeMailBroadcastService.class, "Error getting client e-mail: " + broadcast.getEmail().trim() +", info - " + e.getLocalizedMessage());
				boolean isEnabled = true;
				for (TrackType type : mailBroadcasts.keySet()) {
					if (!mailBroadcasts.get(type).isEnabled(broadcast));
						isEnabled = false;						
				}
				if (!isEnabled) {
					for (TrackType type : mailBroadcasts.keySet()) {
						mailBroadcasts.get(type).clearAllCache(broadcast.getId());
					}
					ApiKey key = broadcastService.getMailBroadcastApiKey(broadcast);
					broadcastService.setEmailBroadcastStatus(key.getApiKey(), broadcast.getEmail(), false);
					logger.error(RuntimeMailBroadcastService.class, "Disable e-mail broadcast with invalid address: " + broadcast.getEmail());
				}
			}
			for (TrackType type : mailBroadcasts.keySet()) {
				if (!mailBroadcasts.get(type).isEnabled(broadcast))
					continue;
				List<String> totalOperIds = new ArrayList<String>();
				List<String> barcodes = mailBroadcasts.get(type).getActiveBroadcastTracks(broadcast);
				for (String barcode : barcodes) {
					List<WebOperation> opers = mailBroadcasts.get(type).getUnbroadcastedEmailTrackOperations(broadcast, barcode);					
					if (opers.size() > 0) {
						List<String> operIds = new ArrayList<String>();
						String content = "Track: ".concat(barcode).concat("\r\n\r\n");
						for (WebOperation oper : opers) {
							operIds.add(oper.getId());							
							Map<String,String> parameters = new HashMap<String, String>();
							String[] paramsArray = oper.getParameters().split(";");
							for (String mapValue : paramsArray) {
								String[] keyValue = mapValue.split("=");
								parameters.put(keyValue[0], keyValue[1]);
							}
							content += this.messagePattern
									.replace("$oper_type$", oper.getType() == null ? "none" : oper.getType())
									.replace("$oper_category$", parameters.containsKey("category") ? parameters.get("category") : "none")
									.replace("$oper_attr$", parameters.containsKey("attribute") ? parameters.get("attribute") : "none")
									.replace("$oper_date$", oper.getDate())
									.replace("$oper_index$", oper.getIndex()==null ? "none" : oper.getIndex())
									.concat("\r\n");
						}
						totalOperIds.addAll(operIds);
						try {
							MimeMessage message = new MimeMessage(this.session);
							message.setFrom(this.sender);
							recipient = new InternetAddress(broadcast.getEmail());
							message.addRecipient(RecipientType.TO, recipient);
							message.setSubject(this.subject);
							message.setText(content, "UTF-8");
							Transport.send(message);
							mailBroadcasts.get(type).clearCache(broadcast.getId(), barcode);
						} catch (AddressException e) {
							logger.error(RuntimeMailBroadcast.class,
								"Can not send updates message - invalid subscriber mail address. Track barcode - " + barcode + ", email - " + broadcast.getEmail());
							mailBroadcasts.get(type).addCache(broadcast.getId(), barcode, operIds);
						} catch (MessagingException e) {
							mailBroadcasts.get(type).addCache(broadcast.getId(), barcode, operIds);
							if (e instanceof SendFailedException) {
								SendFailedException ex = (SendFailedException) e;
								Address[] invalidAddresses = ex.getInvalidAddresses();
								if (invalidAddresses.length > 0) {
									logger.error(RuntimeMailBroadcast.class, "Email address " + broadcast.getEmail() + " is invalid");
									broadcastService.setEmailBroadcastStatus(broadcast.getApiKey().getApiKey(), broadcast.getEmail(), false);
								} else {
									logger.error(RuntimeMailBroadcast.class, "Can not send broadcast message. Barcode - " + barcode + ", subscriber - " + broadcast.getEmail(), e);					
								}
							} else {
								logger.error(RuntimeMailBroadcast.class, "Internal error sending broadcast message. Barcode - " + barcode + ", subscriber - " + broadcast.getEmail(), e);				
							}		
						} catch (Exception e) {
							mailBroadcasts.get(type).addCache(broadcast.getId(), barcode, operIds);
							logger.error(RuntimeMailBroadcast.class, "Unexpected exception", e);
						}
					}
				}				
				if (totalOperIds.size() > 0) {
					List<List<String>> idsPacks = ObjectPackCreator.getObjectPacks(totalOperIds, 2000);
					for (List<String> ids : idsPacks) {
						mailBroadcasts.get(type).disableMailBroadcastOperations(ids);						
					}
				}
			}
		}
		logger.info(RuntimeMailBroadcastService.class, "Email broadcast end");
	}

	
}
