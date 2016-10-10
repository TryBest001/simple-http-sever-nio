package myHttpServer_nio;
/**
 * @Description: represents HttpRequest
 * @author: jitianyu
 * @time: Aug 24, 2016
 *        6:28:25 AM
 */
public class Request {

	private String startLine;
	private String header;
	
	//start-line
	private String method;
	private String requestURL;
	private String version;
	
	public Request( String meth, String requ, String vers, String[] headerFields ){

		method = meth;
		requestURL = requ;
		version = vers;

		startLine = method + ' ' + requestURL + ' ' + version + "\r\n";

		for( String tmp : headerFields ){
			header = ( header == null ) ? tmp : header + tmp + "\r\n";
		}
		header += "\r\n\r\n";
	}

	public String getStartLine() {
		return startLine;
	}

	public void setStartLine(String startLine) {
		this.startLine = startLine;
	}

	public String getHeader() {
		return header;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public String getRequestURL() {
		return requestURL;
	}

	public void setRequestURL(String requestURL) {
		this.requestURL = requestURL;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	
}
