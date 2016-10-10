package myHttpServer_nio;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;


/**
 * @Description: main program of server
 * @author: jitianyu
 * @time: Aug 24, 2016
 *        6:21:40 AM
 */
class Server{
	
	String doucumentRootDirectory;
	String indexFileName;
	int port;
	
	public Server( String rootDirectory ){
		this(rootDirectory,"index.html",80);
	}
	
	public Server( String rootDirectory, int por ){
		this(rootDirectory,"index.html",por);
	}
	public Server( String rootDirectory, String index, int por ){
		File file = new File(rootDirectory);
		if(!file.isDirectory()){
			System.out.println( "Not a Directory" );
			System.exit(0);
		}
		doucumentRootDirectory = rootDirectory;
		indexFileName = index;
		port = por;
	}
	
	public void start(){
		Selector selector = null;
		try {
			//create ServerSocketChannel
			ServerSocketChannel serverChannel = ServerSocketChannel.open();
			//create ServerSocket
			ServerSocket server = serverChannel.socket();
			//create InetSocketAddress to represent server ip and port
			InetSocketAddress addr = new InetSocketAddress(port);
			//bind ServerSock
			server.bind( addr );
			//change ServerSocketChannel to be non-block
			serverChannel.configureBlocking(false);
			//create a selector used to save ready channel
			selector = Selector.open();
			//register ServerSocketChannel with a Selector
			SelectionKey key = serverChannel.register( selector, SelectionKey.OP_ACCEPT );
		} catch (IOException e) {
			System.err.println( "error when creating ServerSocketChannel" );
			e.printStackTrace();
		}
		while(true){
			try {
				//block if no readiness 
				System.out.println("waiting for connection");
				selector.select();
			} catch (IOException e) {
				System.err.println("error with calling selector.select()");
				e.printStackTrace();
			}
				
			//get the interested Set<SelectionKey>
			Set<SelectionKey> readyKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = readyKeys.iterator();
			try{
				while(iterator.hasNext()){
					SelectionKey readyKey  = iterator.next();
					//remove related SelectionKey from interested Set<SelectionKey>
					iterator.remove();
					if( readyKey.isAcceptable() ){
						//get ServerSocketChannel from SelectionKey
						ServerSocketChannel serverChannel = (ServerSocketChannel) readyKey.channel();
						//wait for client connection
						SocketChannel clientChannel = serverChannel.accept();
						System.out.println("Connection Established");
						//set clientChannel to non-block
						clientChannel.configureBlocking(false);
						//register the clientChannel with selector
						clientChannel.register(selector, SelectionKey.OP_READ);
					}else if( readyKey.isReadable() ){
						processRequest(selector,readyKey);
					//}else if( readyKey.isWritable() ){
					//	writeResponse(readyKey);
					}
				}
			} catch (IOException e) {
				System.err.println("error with handling readyKeys");
				e.printStackTrace();
			}
		}
	}
	
	private void processRequest(Selector selector, SelectionKey readyKey){
		//get SocketChannel from SelectionKey
		SocketChannel clientChannel = (SocketChannel)readyKey.channel();
		//create buffer to read HTTP request
		ByteBuffer buffer = ByteBuffer.allocate( 1024 );
		//create Requset
		Request requestImpl = null; 
		//begin reading
		int res;
		String tmp = null;
		try{
			//if res == 0 then 
			while( (res = clientChannel.read(buffer)) > 0 ){
				//process buffer
				tmp = ( tmp == null ) ? new String(buffer.array()) : tmp + new String(buffer.array());
				//prepare for next read 
				buffer.clear();
			}
			//get the startLine
			String[] requestStr = tmp.split("\r\n|\\s");
			String method = requestStr[0];
			String requestURL = requestStr[1];
			String version = requestStr[2];
			//get request header
			requestStr = tmp.split("\r\n");
			String[] headerFields = new String[requestStr.length-1];
			for( int i = 1; i < requestStr.length; i++ ){
				headerFields[i-1] = requestStr[i]+"\r\n";
			}
			//create Request that represents HTTP request
			requestImpl = new Request( method, requestURL, version, headerFields );
			//after reading
			//get requestURL 
			String filename = requestImpl.getRequestURL();
			//check suffix 
			if( filename.endsWith("/") )
				filename += indexFileName;
			requestImpl.setRequestURL(filename);
			//begin writting
			try{
				//create Response
				Response responseImpl = null; 
				//create File requested by requestURL  
				File fileToWrite= new File(doucumentRootDirectory, filename.substring( 1,filename.length() ));
				//choose different return status
				if(!fileToWrite.canRead()){//404
					responseImpl = new Response(requestImpl, "404", null, (int)fileToWrite.length());
					ByteBuffer buf = ByteBuffer.allocate(1024);
				
					buf.clear();
					buf.put(responseImpl.getStartLine().getBytes("UTF-8"));
					buf.flip();
					clientChannel.write(buf);

					buf.clear();
					buf.put( responseImpl.getHeader().getBytes("UTF-8") );
					buf.flip();
					clientChannel.write(buf);

					buf.clear();
					buf.put( responseImpl.getContent());
					buf.flip();
					clientChannel.write(buf);
				}else{//200
					responseImpl = new Response(requestImpl,"200", null, (int)fileToWrite.length());

					ByteBuffer buf = ByteBuffer.allocate(1024);
				
					buf.clear();
					buf.put(responseImpl.getStartLine().getBytes("UTF-8"));
					buf.flip();
					clientChannel.write(buf);

					buf.clear();
					buf.put( responseImpl.getHeader().getBytes("UTF-8") );
					buf.flip();
					clientChannel.write(buf);

					//use file channel
					FileInputStream in = new FileInputStream( fileToWrite );
					FileChannel fileChannel =  in.getChannel();
					buf.clear();
					while( fileChannel.read(buf) != -1 ){
						buf.flip();
						clientChannel.write(buf);
						buf.clear();
					}
					in.close();
				}
			} catch ( IOException e ){
				System.err.println("an error occured while writting response");
				readyKey.cancel();
				try {
					clientChannel.close();
				} catch (IOException e1) {
				System.err.println("an error occured while closing clientChannel");
				}
			}
			}catch(IOException e){
			//if client closed while reading, then close the channel
			System.out.println("remote connection closed");
			readyKey.cancel();
			try {
				clientChannel.close();
			} catch (IOException e1) {
				System.out.println("error occurs while closing clientChannel");
			}
		}	

	}
	public static void main(String[] args) {
		String rootDir = "src/myHttpServer_nio/webContent/"; 
		String index = "index.html";
		int port = 3339;
		
		Server serverMain = new Server(rootDir, index, port);
		serverMain.start();
	}
}
