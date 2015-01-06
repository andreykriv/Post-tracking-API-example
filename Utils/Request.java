import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import ru.aplix.posttrackingapi.services.util.RequestStreamHandler;

public abstract class Request<S,T> {
	
	protected List<Header> headers;
	protected int connectionTimeout;
	protected int readTimeout;
	protected String endpoint;
	protected String error;
	protected T response;
	protected boolean traceReq = false;
	protected boolean traceResp = false;
	protected StringBuilder traceReqContainer;
	protected StringBuilder traceRespContainer;
	protected Class<T> responseClass;
	
	public abstract void request();	
	public abstract void setBody(S body);
	
	static {
		TrustManager[] trustAllCerts = new TrustManager[] { 
				new X509TrustManager() {			
		
		            @Override
					public java.security.cert.X509Certificate[] getAcceptedIssuers() {
		            	return null;
		            }
		            
		            @Override
					public void checkClientTrusted(X509Certificate[] certs, String authType) {}
		            @Override
					public void checkServerTrusted(X509Certificate[] certs, String authType) {}
				}
			};
		try {
			final SSLContext sc = SSLContext.getInstance("SSL");
			sc.init(null, trustAllCerts, new java.security.SecureRandom());			
		    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		    HostnameVerifier allHostsValid = new HostnameVerifier() {
	
		    	@Override
		    	public boolean verify(String arg0, SSLSession arg1) {
		    		return true;
		    	}
		    };
		    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
		} catch (Exception e) {}
	}
	
	public Request() {
		connectionTimeout = 60000;
		readTimeout = 300000;
	}
	
	public void setConnectionTimeout(int timeout) {
		this.connectionTimeout = timeout;
	}
	
	public void setReadTimeout(int timeout) {
		this.readTimeout = timeout;
	}
	
	public void setHeaders(List<Header> headers) {
		this.headers = headers;
	}
	
	public void addHeader(Header header) {
		if (this.headers == null)
			this.headers = new ArrayList<Header>();		
		this.headers.add(header);
	}
	
	public String getHeader(String headerName) {
		if (this.headers != null)
			for (Header header : this.headers) {
				if (header.getName().equals(headerName))
					return header.getValue();
			}
		return null;
	}
	
	public String getError() {
		return this.error;
	}
	
	public String getEndpointString() {
		return this.endpoint;
	}
	
	public URL getEndpoint() throws MalformedURLException {	
		RequestStreamHandler handler = new RequestStreamHandler();
		handler.setHeaders(this.headers);			
		handler.setConnectionTimeout(this.connectionTimeout);
		handler.setReadTimeout(this.readTimeout);
		return new URL(null, this.endpoint, handler);
	}
	
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}
	
	public T getResponse() {
		return response;
	}
	
	public boolean isTraceReq() {
		return traceReq;
	}
	public void setTraceReq(boolean traceReq) {
		this.traceReq = traceReq;
	}
	public boolean isTraceResp() {
		return traceResp;
	}
	public void setTraceResp(boolean traceResp) {
		this.traceResp = traceResp;
	}
	
	public String getTraceRequestBody() {
		if (this.traceReq == true && this.traceReqContainer != null)
			return this.traceReqContainer.toString();
		return null;
	}
	
	public String getTraceResponseBody() {
		if (this.traceResp == true && this.traceRespContainer != null)
			return this.traceRespContainer.toString();
		return null;
	}
	
	protected void setTraceRequestBody(String request) {
		if (this.traceReqContainer == null && this.traceReq == true) {
			this.traceReqContainer = new StringBuilder();
			this.traceReqContainer.append(request);
		}
	}
	
	protected void setTraceResponseBody(String response) {
		if (this.traceRespContainer == null && this.traceResp == true) {
			this.traceRespContainer = new StringBuilder();
			this.traceRespContainer.append(response);
		}
	}
	
}
