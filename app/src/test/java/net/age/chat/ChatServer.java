package net.age.chat;
/*
 * Copyright (c) 2010-2020 Nathan Rajlich
 *
 *  Permission is hereby granted, free of charge, to any person
 *  obtaining a copy of this software and associated documentation
 *  files (the "Software"), to deal in the Software without
 *  restriction, including without limitation the rights to use,
 *  copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the
 *  Software is furnished to do so, subject to the following
 *  conditions:
 *
 *  The above copyright notice and this permission notice shall be
 *  included in all copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 *  EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 *  OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 *  NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 *  HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 *  WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 *  FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 *  OTHER DEALINGS IN THE SOFTWARE.
 */

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.ByteBuffer;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.TreeMap;


import static java.nio.charset.StandardCharsets.UTF_8;

import static net.age.chat.ChatConstant.DEPLOY;
import static net.age.chat.ChatConstant.SERVER_DEPLOY_ADDR;
import static net.age.chat.ChatConstant.SERVER_DEPLOY_PORT;
import static net.age.chat.ChatConstant.SERVER_TEST_ADDR;
import static net.age.chat.ChatConstant.SERVER_TEST_PORT;
import static net.age.chat.ChatConstant.fileIndicator;

/**
 * A simple WebSocketServer implementation. Keeps track of a "chatroom".
 */
public class ChatServer extends WebSocketServer {
    public String mFileName;

    private Map<String,String> chatMembers = new TreeMap<>();
	public ChatServer(){
		super(new InetSocketAddress(SERVER_TEST_ADDR, SERVER_TEST_PORT));
		if (!DEPLOY) {
			new InetSocketAddress(SERVER_TEST_ADDR, SERVER_TEST_PORT);
		} else {
			new InetSocketAddress(SERVER_DEPLOY_ADDR, SERVER_DEPLOY_PORT);
		}
	}
	@Override
	public void onOpen( WebSocket conn, ClientHandshake handshake ) {
		conn.send("Welcome to the server!"); //This method sends a message to the new client
		String model = handshake.getFieldValue("client");
		if (!model.equals("")){
			broadcast( model + " entered the room!" );
			chatMembers.put(conn.toString(),model);
		}else {
			System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + "@" + conn +  " entered the room!" );
		}
	}

	@Override
	public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
		String s = chatMembers.getOrDefault(conn.toString(),"Some one");
		broadcast( s + " has left the room!" );
		System.out.println( s + " has left the room!" );
	}

	@Override
	public void onMessage( WebSocket conn, String message ) {
		System.out.println("onMessage");
        //if(message.toLowerCase().endsWith(".jpeg") || message.toLowerCase().endsWith(".jpg"))
        if(message.endsWith(fileIndicator))
        {
//        	if(!message.equals("mm")){
            	mFileName = message.substring(0,message.length() - fileIndicator.length());
//        	}else {
//				mFileName = message;
//			}
        }else {
			broadcast( message );
            System.out.println( chatMembers.getOrDefault(conn.toString(),"Some one") + ": " + message );
        }
	}
	@Override
	public void onMessage( WebSocket conn, ByteBuffer message ) {
        final ByteBuffer bb = message;
        final String name = mFileName;
//        mFileName = "file";
        new Thread(new Runnable() {
            @Override
            public void run() {
				System.out.println("saving to " + "assets/" + name + " -> " + bb.array().length/1024/1024 + "MB");
                broadcast(name);
                byte[] array = bb.array();
				InputStream is = new ByteArrayInputStream(array);
				try {
					String mimeType = URLConnection.guessContentTypeFromStream(is);
					System.out.println("mime:" + mimeType);
				} catch (IOException e) {
					e.printStackTrace();
				}
                byte2image(array,"assets/" + name);
                broadcast(array);
            }
        }).start();
	}

	
	public static byte[] image2byte(String path){
		byte[] data = null;
		FileInputStream input = null;
		try {
		  input = new FileInputStream(new File(path));
		  ByteArrayOutputStream output = new ByteArrayOutputStream();
		  byte[] buf = new byte[1024];
		  int numBytesRead = 0;
		  while ((numBytesRead = input.read(buf)) != -1) {
		  output.write(buf, 0, numBytesRead);
		  }
		  data = output.toByteArray();
		  output.close();
		  input.close();
		}
		catch (FileNotFoundException ex1) {
		  ex1.printStackTrace();
		}
		catch (IOException ex1) {
		  ex1.printStackTrace();
		}
		return data;
	  } 
	  

	  public static String byte2string(byte[] data){
		if(data==null||data.length<=1) return "0x";
		if(data.length>200000) return "0x";
		StringBuffer sb = new StringBuffer();
		int buf[] = new int[data.length];
		for(int k=0;k<data.length;k++){
		  buf[k] = data[k]<0?(data[k]+256):(data[k]);
		}
		for(int k=0;k<buf.length;k++){
		  if(buf[k]<16) sb.append("0"+Integer.toHexString(buf[k]));
		  else sb.append(Integer.toHexString(buf[k]));
		}
		return "0x"+sb.toString().toUpperCase();
	  } 
	  

	  public static void byte2image(byte[] data,String path){
		if(data.length<3||path.equals("")) {
			return;
		}
		try{
			FileOutputStream imageOutput = new FileOutputStream(new File(path));
			imageOutput.write(data, 0, data.length);
			imageOutput.close();
			System.out.println("file saved to " + path);
		} catch(Exception ex) {
			System.out.println("Exception: " + ex);
			ex.printStackTrace();
		}
	  }

	public static void main( String[] args ) throws InterruptedException , IOException {
		System.out.println();
		ChatServer s = new ChatServer();
		s.start();
		System.out.println( "ChatServer started on port: " + s.getPort() );

		BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
		while ( true ) {
			String in = sysin.readLine();
			s.broadcast( in );
			if( in.equals( "exit" ) ) {
				s.stop(1000);
				break;
            }else if(in.equals( "go" )){
                byte[] img = image2byte("gg.jpeg");
                ByteBuffer bb = ByteBuffer.wrap(img);
                s.broadcast(bb.array());
            }else if(in.endsWith(fileIndicator)){

			}
		}
	}
	@Override
	public void onError( WebSocket conn, Exception ex ) {
		ex.printStackTrace();
		if( conn != null ) {
			// some errors like port binding failed may not be assignable to a specific websocket
		}
	}

	@Override
	public void onStart() {
		System.out.println("Server started!");
		setConnectionLostTimeout(0);
		setConnectionLostTimeout(100);
	}
}
