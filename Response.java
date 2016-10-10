package myHttpServer_nio;

import java.util.Date;

import myHttpServer_nio.Request;

/**
 * @Description: represents HTTP Response
 * @author: jitianyu
 * @time: Aug 24, 2016
 *        6:38:48 AM
 */
public class Response {
	private String startLine;
	private String header;
	private byte[] content;
	
	//start-line
	private String version; 
	private String status;
	private String reasonPhase;
	
	//header-fields
	private String date;
	private String server;
	private String contentType;
	private int contentLength;
	
	//related HTTP request
	private Request request;

	public Response( Request requ, String stat, byte[] cont, int contLength ){
		request = requ;

		version = requ.getVersion();
		status =  stat;

		//reasonPhase need to check
		switch(status){
			case "200" : reasonPhase ="OK";
						 content = cont;
						 contentLength = contLength ;
					     break;
			case "404" : reasonPhase = "File Not Found";
						 content = "File Not Found".getBytes();
						 contentLength = content.length;
					     break;
			case "501" : reasonPhase = "Not Implemented";
						 content = "Not Implemented".getBytes();
						 contentLength = content.length;
					     break;
		}

		startLine = version + ' ' + status + ' ' + reasonPhase + "\r\n";

		date = new Date().toString();
		server = "Jitianyu's Server";

		contentType = guessContentType( request.getRequestURL() );

		//contentLength = ( cont == null ) ? null : content.length;
		
		//notice: there is \r\n\r\n indicating the end of  Http response header
		header = "Date: " + date + "\r\n" + 
					    "Server: " + server + "\r\n" +
					    "Content-Length: " + contentLength + "\r\n" +
					    "Content-Type: " + contentType + "\r\n\r\n";
	}

	public String toString(){
		return startLine + header + content;
	}
	
	public String guessContentType( String requestURL ){
		int pos = requestURL.lastIndexOf(".");
		if( pos == -1 )return "text/plain";

		String extendName = requestURL.substring(pos, requestURL.length());
		switch(extendName){
			case ".html"  :return "text/html";
			case ".htm"   :return "text/html";
			case ".txt"   :return "text/plain";
			case ".java"  :return "text/plain";
			case ".gif"   :return "image/gif";
			case ".class" :return "application/octet-stream";
			case ".jpeg"  :return "image/jpeg";
			case ".jpg"   :return "image/jpeg";
			default       :return "text/plain";
		}
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

	public byte[] getContent() {
		return content;
	}

	public void setContent(byte[] content) {
		this.content = content;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getReasonPhase() {
		return reasonPhase;
	}

	public void setReasonPhase(String reasonPhase) {
		this.reasonPhase = reasonPhase;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getServer() {
		return server;
	}

	public void setServer(String server) {
		this.server = server;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public int getContentLength() {
		return contentLength;
	}

	public void setContentLength(int contentLength) {
		this.contentLength = contentLength;
	}

	public Request getRequest() {
		return request;
	}

	public void setRequest(Request request) {
		this.request = request;
	}
}
