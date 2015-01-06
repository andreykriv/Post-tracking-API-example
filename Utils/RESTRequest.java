import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import ru.aplix.posttrackingapi.services.util.ContextProvider;
import ru.aplix.posttrackingapi.services.util.RESTfulWebParser;

public class RESTRequest<S,T> extends Request<S,T> {

	protected String request;
	protected String method;
	protected int code = 0;
	protected String parserBeanName;
	
		
	private static Map<String, RESTfulWebParser> restParsers = new HashMap<String, RESTfulWebParser>();
	
	protected RESTRequest(Class<T> respClass, String endpoint, String parserBeanName) {
		super.setEndpoint(endpoint);
		this.parserBeanName = parserBeanName;
		super.responseClass = respClass;
	}
	
	protected synchronized RESTfulWebParser getRestParser() {
		if (!restParsers.containsKey(this.parserBeanName)) {
			restParsers.put(this.parserBeanName, (RESTfulWebParser) 
					ContextProvider.getContext().getBean(this.parserBeanName));
		}
		return restParsers.get(this.parserBeanName);
	}	
	
	public void setMethod(String method) {
		this.method = method;
	}
	
	@Override
	public void setBody(S request) {
		if (request != null)
			try {
				this.request = getRestParser().formatToString(request);
			} catch (IOException e) {
				this.error = "Can not set request body: " + e.getLocalizedMessage();
			}
	}
	
	@Override
	public void request() {		
		if (error == null && endpoint != null) {
			try {			
				URL endpoint = super.getEndpoint();
				HttpURLConnection con = (HttpURLConnection)endpoint.openConnection();
				con.setRequestMethod(this.method==null ? "POST" : this.method);
				con.setDoInput(true);
				con.setDoOutput(true);
				this.setRequestBody(con);
				if (this.request != null && this.traceReq == true) {
					this.setTraceRequestBody(this.request);
				}
				this.code = con.getResponseCode();
				if (code == 200) {
					InputStream stream = con.getInputStream();
					ByteArrayOutputStream resp = new ByteArrayOutputStream();
					IOUtils.copy(stream, resp);
					String respStr = new String(resp.toByteArray(), Charset.forName("UTF-8"));
					String formattedMsg = respStr.replaceAll("((\n)|(\r\n))", "");
					if (this.traceResp == true)
						this.setTraceResponseBody(formattedMsg);
					this.setResponseBody(formattedMsg);
					stream.close();
				}
				this.checkResponseErrors();
				con.disconnect();
			} catch (IOException e) {
				this.error = "I/O exception requesting " + endpoint;
			}
		}
	}
	
	public int getResponseCode() {
		return this.code;
	}
	
	@Override
	public String getError() {
		return error;
	}
	
	protected void checkResponseErrors() {
		switch (this.code) {
		case 400: {
			this.error = "Bad request - " + endpoint;
			break;
		}
		case 403: {
			this.error = "Forbidden - " + endpoint;
			break;
		}
		case 404 : {
			this.error = "Not Found - " + endpoint;
			break;
		}
		case 415 : {
			this.error = "Unsupported Media Type - " + endpoint;
			break;
		}
		case 500 : {
			this.error = "Client Internal Server Error - " + endpoint;
			break;
		} 
		case 502 : {
			this.error = "Bad Gateway - " + endpoint;
			break;
		}
		case 503 : {
			this.error = "Service Unavailable - " + endpoint;
			break;
		}
		case 504 : {
			this.error = "Gateway Timeout - " + endpoint;
			break;
		}
		}
	}
	
	protected void setRequestBody(HttpURLConnection con) throws IOException {
		if (this.request != null) {
			con.getOutputStream().write(this.request.getBytes(Charset.forName("UTF-8")));			
		}
	}
	
	protected void setResponseBody(String response) {
		try {
			InputStream stream = new ByteArrayInputStream(response.getBytes(Charset.forName("UTF-8")));
			this.response = getRestParser().parseInput(this.responseClass, stream);
			if (stream != null)
				stream.close();
		} catch (Exception e) {
			this.error = "Can not parse response string";			
		}
	}
}
