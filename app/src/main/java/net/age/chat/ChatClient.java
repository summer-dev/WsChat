package net.age.chat;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

public class ChatClient{
    static final String TAG = "ChatClient";
//    static final String serverAddr = "ws://fyh520.cn:8887";
    static final String serverAddr = "ws://192.168.3.151:8887";
    public Draft draft;//
    public WebSocketClient cc;// = new WebSocketClient();
    public StringBuffer msg = new StringBuffer();
    public Handler chatHandler;

    public ChatClient(Handler handler) {
        chatHandler = handler;
        draft = new Draft_6455();
        Log.v(TAG,"constructor");
    }
    public void send(String msg){
        if(cc.isOpen()){
            cc.send(msg);
            chatHandler.sendEmptyMessage(ChatConstant.MESSAGE_CLEAR_EDIT);
        }
    }
    public void chat(){

        try {
            // cc = new ChatClient(new URI(uriField.getText()), area, ( Draft ) draft.getSelectedItem() );
            cc = new WebSocketClient(new URI(serverAddr),draft) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.v(TAG,"You are connected to ChatServer: " + getURI() + "\n");
                }

                @Override
                public void onMessage(String s) {
                    Log.v(TAG,s);
                    if(s.startsWith("org.java_websocket.WebSocketImpl")){
                        chatHandler.sendEmptyMessage(ChatConstant.MESSAGE_OFFLINE);
                    }else if(s.startsWith("You are")){
                        chatHandler.sendEmptyMessage(ChatConstant.MESSAGE_ONLINE);
                    }else if(!s.startsWith("new connection:")){
                        Message message = new Message();
                        message.what = ChatConstant.MESSAGE_NEW_MSG;
                        message.obj = s;
                        chatHandler.sendMessage(message);
//                        chatHandler.sendEmptyMessage(ChatConstant.MESSAGE_NEW_MSG);
//                        msg.append(s + "\n");
                    }
                }
                @Override
                public void onMessage(ByteBuffer s) {
                    Message message = new Message();
                    message.what = ChatConstant.MESSAGE_NEW_MEDIA;
                    message.obj = s;
                    chatHandler.sendMessage(message);
//                    chatHandler.sendEmptyMessage(ChatConstant.MESSAGE_NEW_MSG);
                }
                @Override
                public void onClose(int i, String s, boolean b) {

                }

                @Override
                public void onError(Exception e) {
                    Log.v(TAG,e.getMessage());

                }
            };
            cc.connect();
        }catch (URISyntaxException e){
            Log.v(TAG,e.getMessage());
            e.printStackTrace();
        }
    }
}
