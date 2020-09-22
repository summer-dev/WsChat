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

import java.awt.Container;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.Dimension;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.TimerTask;
import java.util.Timer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.text.SimpleDateFormat;

import java.io.File;
import javax.swing.JLabel;
import javax.swing.JFileChooser;


import org.java_websocket.WebSocketImpl;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

public class ChatClient extends JFrame implements ActionListener {
	private static  long serialVersionUID = -6056260699202978657L;
	public static boolean deploy = ChatUtils.DEPLOY;
	static JTextField uriField;
	static JButton attatch;
	static JTextArea ta;
	static JTextField chatField;
	static JComboBox draft;
	static WebSocketClient cc;
	static String mRecevFileName = "file";
	static boolean mIsSendFile = false;
	static String mSavePath = "assets/client_";
	static String serverAddr = ChatUtils.SERVER_ADDR;
	static int serverPort = ChatUtils.SERVER_DEPLOY_PORT;
	// private static final String mSavePath = "D:\\Documents\\Downloads\\";
	static ConnectionTask task;
	static Timer t;

	public ChatClient( String defaultlocation ) {
		super( "WebSocket Chat Client" );
		// setSize(1024,768);
		Container c = getContentPane();
		GridLayout layout = new GridLayout();
		layout.setColumns( 1 );
		layout.setRows( 5 );
		c.setLayout( layout );

		Draft[] drafts = { new Draft_6455() };
		draft = new JComboBox( drafts );
		c.add( draft );

		uriField = new JTextField();
		uriField.setText( defaultlocation );
		c.add( uriField );

		JScrollPane scroll = new JScrollPane();
		ta = new JTextArea();
		scroll.setViewportView( ta );
		c.add( scroll );

		chatField = new JTextField();
		chatField.setText( "" );
		chatField.addActionListener( this );
		c.add( chatField );
		
		attatch = new JButton("Attatch");
		attatch.addActionListener( this );
		attatch.setEnabled( false );
		c.add( attatch);

		java.awt.Dimension d = new java.awt.Dimension( 300, 400 );
		setPreferredSize( d );
		setSize( d );

		addWindowListener( new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing( WindowEvent e ) {
				if( cc != null ) {
					cc.close();
				}
//				task.cancel();
				t.cancel();
				dispose();
//				System.exit(0);
			}
		} );

		setLocationRelativeTo( null );
		setVisible( true );
		chat();
	}
	public static void chat(){
		try {
			// cc = new ChatClient(new URI(uriField.getText()), area, ( Draft ) draft.getSelectedItem() );
			Map<String,String> header = new HashMap<>();
			header.put("client",System.getenv("COMPUTERNAME"));
			cc = new WebSocketClient( new URI( uriField.getText() ), (Draft) draft.getSelectedItem(),header) {

				@Override
				public void onMessage( String message ) {
					ta.append( ChatUtils.getStringByFormat(new Date(),ChatUtils.DEFYMDHMS) + " @ guyue(+_+): " +   message + "\n" );
					System.out.println("onMessage(String) @ : " + System.currentTimeMillis());
					ta.setCaretPosition( ta.getDocument().getLength() );
					if(message.contains(".")){
						mRecevFileName = message;
					}
				}
				@Override
				public void onMessage( ByteBuffer s) {
					System.out.println("onMessage(ByteBuffer) @ : " + System.currentTimeMillis());
					System.out.println("media received " + mSavePath + mRecevFileName);
					//ta.append( ChatUtils.getStringByFormat(new Date(),ChatUtils.DEFYMDHMS) + " @ guyue(+_+): " +   mRecevFileName + "\n" );
					if(mRecevFileName.equals("file")){
						mRecevFileName += "_" + UUID.randomUUID().toString();
					}
					ChatUtils.byte2image(s.array(),mSavePath + mRecevFileName);

				}
				@Override
				public void onOpen( ServerHandshake handshake ) {
					ta.append( "You are connected to ChatServer: " + getURI() + "\n" );
					ta.setCaretPosition( ta.getDocument().getLength() );
					attatch.setEnabled(true);
				}

				@Override
				public void onClose( int code, String reason, boolean remote ) {
					ta.append( "You have been disconnected from: " + getURI() + "; Code: " + code + " " + reason + "\n" );
					ta.setCaretPosition( ta.getDocument().getLength() );
					uriField.setEditable( true );
					draft.setEditable( true );
					attatch.setEnabled(false);
				}

				@Override
				public void onError( Exception ex ) {
					ta.append( "Exception occured ...\n" + ex + "\n" );
					ta.setCaretPosition( ta.getDocument().getLength() );
					ex.printStackTrace();
					uriField.setEditable( true );
					draft.setEditable( true );
				}
			};

			uriField.setEditable( false );
			draft.setEditable( false );
			cc.setConnectionLostTimeout( 0 );
			cc.connect();
		} catch ( URISyntaxException ex ) {
			ta.append( uriField.getText() + " is not a valid WebSocket URI\n" );
		}
	}

	public void actionPerformed( ActionEvent e ){
		if( e.getSource() == chatField ) {
			if(cc != null){
				cc.send(chatField.getText());
				chatField.setText( "" );
				chatField.requestFocus();
			}
		}
		else if( e.getSource() == attatch ) {
			// TODO Auto-generated method stub
			JFileChooser jfc=new JFileChooser();
			jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES );
			jfc.showDialog(new JLabel(), "File Chooser");
			File file=jfc.getSelectedFile();
			if(file.isDirectory()){
				System.out.println("Path:"+file.getAbsolutePath());
				return;
			}else if(file.isFile()){
				System.out.println("File:"+file.getAbsolutePath());
				cc.send(jfc.getSelectedFile().getName().trim()+ChatUtils.fileIndicator);
				new Thread(new Runnable() {
					@Override
					public void run() {
						cc.send(ChatUtils.image2byte(file.getAbsolutePath()));
					}
				}).start();
			}
		}
	}
	public static class ConnectionTask extends TimerTask
	{
		@Override
		public void run()
		{
			if(cc != null){
				if(!cc.isOpen()){
					System.out.println("cc reconnect");
					cc.reconnect();
				}
			}else {
				System.out.println("cc is null");
				chat();
			}
		}

	}
	public static void main( String[] args ) {
		if(deploy == false) {
			serverAddr = ChatUtils.SERVER_TEST_ADDR;
			serverPort = ChatUtils.SERVER_TEST_PORT;
		}
		System.out.println( "Default server url not specified: defaulting to \'" + ChatUtils.SERVER_PROTOCOL + serverAddr + ":" + serverPort + "\'" );
		new ChatClient( ChatUtils.SERVER_PROTOCOL + serverAddr + ":" + serverPort);

		t = new Timer();
		task = new ConnectionTask();
		t.schedule(task, 1000,5000);
	}
}
