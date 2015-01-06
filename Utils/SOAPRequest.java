import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;

import ru.aplix.posttrackingapi.services.util.SOAPUtil;

public abstract class SOAPRequest<S,T> extends Request<S,T> {
	
	private static SOAPConnectionFactory factory = null;
	
	protected Marshaller marsh;
	protected Unmarshaller unmarsh;
	protected SOAPMessage message;
	protected Class<T> responseClass;
	protected List<SOAPFaultHandler<?>> faultHandlers;
	protected SOAPFaultHandler<?> responseFaultHandler;
	protected List<JAXBElement<? extends Object>> soapHeaders;
	
	public abstract JAXBContext getContext() throws JAXBException;
	
	public SOAPRequest(Class<T> responseClass) {
		try {
			JAXBContext context = this.getContext();
			if (context != null) {			
				this.marsh = context.createMarshaller();
				this.unmarsh = context.createUnmarshaller();			
			}
		} catch (JAXBException e) {
			this.error = "Can not create context marshaller - " + this.endpoint;
		}
		this.responseClass = responseClass;
	}	
	
	protected void setJaxbBody(JAXBElement<S> body) {
		try {
			if (this.message == null) 
				this.message = SOAPUtil.getMessage();
			SOAPBody soapBody = this.message.getSOAPBody();
			this.marsh.marshal(body, soapBody);
		} catch (Exception e) {
			this.error = "Exception marshalling message";
		}
	}
	
	
	
	protected void addSOAPHeader(JAXBElement<? extends Object> header) {
		if (this.soapHeaders == null)
			this.soapHeaders = new ArrayList<JAXBElement<? extends Object>>();
		this.soapHeaders.add(header);
	}
	
	@Override
	public void request() {
		if (endpoint != null) {
			SOAPConnection con = null;
			try {
				con = getConnectionFactory().createConnection();
				if (this.message == null)
					this.message = SOAPUtil.getMessage();
				SOAPEnvelope env = this.message.getSOAPPart().getEnvelope();
				SOAPHeader header = null;
				if (this.soapHeaders != null) {
					header = env.getHeader();
					for (Object soapHeader : soapHeaders) {						
						if (header == null) {
							header = env.addHeader();
						}
						try {
							this.marsh.marshal(soapHeader, header);
						} catch (JAXBException e) {
							this.error = "Can not set SOAP request headers: " + e.getLocalizedMessage();
							return;
						}
					}
				}
				ByteArrayOutputStream reqStream = new ByteArrayOutputStream();
				try {
					this.message.writeTo(reqStream);
				} catch (IOException e1) {
					this.error = "Can not get request body string for " + this.endpoint;
				}				
				super.setTraceRequestBody(new String(reqStream.toByteArray(), Charset.forName("UTF-8")));
				SOAPMessage response = con.call(this.message, super.getEndpoint());
				try {
					ByteArrayOutputStream output = new ByteArrayOutputStream();
					response.writeTo(output);
					String msg = new String(output.toByteArray(), Charset.forName("UTF-8"));
					String formattedMsg = msg.trim().replaceAll("((\n)|(\r\n))", "");
					this.setTraceResponseBody(formattedMsg);
					response = SOAPUtil.getMessage(formattedMsg);				
					output.close();
				} catch (Exception e) {
					this.error = "Internal request error - " + e.getMessage();
				}
				this.setResponse(response);
			} catch (SOAPException e) {				
				this.error = "SOAP exception executing request: " + e.getMessage();
			} catch (MalformedURLException e) {
				this.error = "SOAP API URL "+ this.endpoint + " is malformed";
			} finally {
				if (con != null) {
					try {
						con.close();
					} catch (SOAPException e) {
						this.error = "Can not close connection. " + e.getMessage();
					}
				}
				this.message = null;
			}
		}
	}
	
	@Override
	public T getResponse() {
		return response;
	}
	public SOAPFaultHandler<?> getFault() {
		return this.responseFaultHandler;
	}
	
	protected void setResponse(SOAPMessage response) {
		JAXBElement<T> el = null;
		try {
			el = unmarsh.unmarshal(response.getSOAPBody().extractContentAsDocument(), this.responseClass);
		} catch (JAXBException | SOAPException e) {
			if (this.faultHandlers != null && this.faultHandlers.size() > 0) {
				for (SOAPFaultHandler<?> handler : this.faultHandlers) {
					try {
						handler.handleFault(response, this.unmarsh);
						if (handler.getFault() != null)
							this.responseFaultHandler = handler;
					} catch (JAXBException | SOAPException ex) {
						continue;
					} 
				}
			}
			if (this.responseFaultHandler == null) {
				this.error = "Can not get response";
			}
		}		
		if (el != null) {
			this.response = el.getValue();
		}
	}	
	
	
	private SOAPConnectionFactory getConnectionFactory() throws SOAPException {
		if (factory == null)
			factory = SOAPConnectionFactory.newInstance();
		return factory;
	}
}
