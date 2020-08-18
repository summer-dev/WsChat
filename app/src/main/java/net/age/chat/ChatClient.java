package net.age.chat;
import android.os.Build;
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
import java.util.HashMap;
import java.util.Map;

import static net.age.chat.ChatConstant.SERVER_TEST_ADDR;
import static net.age.chat.ChatConstant.SERVER_TEST_PORT;
import static net.age.chat.ChatConstant.SERVER_DEPLOY_ADDR;
import static net.age.chat.ChatConstant.SERVER_DEPLOY_PORT;
import static net.age.chat.ChatConstant.SERVER_PROTOCOL;

public class ChatClient{
    static final boolean deploy = false;
    static final String TAG = "ChatClient";
//    static final String serverAddr = "ws://fyh520.cn:8887";
    private String serverAddr = SERVER_PROTOCOL + SERVER_TEST_ADDR + ":" + SERVER_TEST_PORT;//192.168.3.151:8887";
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

    public void send(byte[] msg){
        if(cc.isOpen()){
            cc.send(msg);
            chatHandler.sendEmptyMessage(ChatConstant.MESSAGE_SEND_MEDIA);
        }
    }
    public void reconnect(){
        if (cc.isOpen() ){
            Log.v(TAG,"reconnect isOpen");
            cc.reconnect();
        }//else  if(cc.isClosed()){
//            Log.v(TAG,"reconnect isClosed");
//            chat();
        else{
            Log.v(TAG,"reconnect  chat");
            chat();
        }
    }
    public void chat(){

        try {
            if(deploy){
                serverAddr = SERVER_PROTOCOL + SERVER_DEPLOY_ADDR + ":" + SERVER_DEPLOY_PORT;
            }
            Map<String,String> header = new HashMap<>();
            header.put("client",Build.DEVICE);
            cc = new WebSocketClient(new URI(serverAddr),draft,header) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    Log.v(TAG,"You are connected to ChatServer: " + getURI() + "\n");
                }

                @Override
                public void onMessage(String s) {
                    Log.v(TAG,"onMessage String");
                    if(s.startsWith("org.java_websocket.WebSocketImpl")){
                        chatHandler.sendEmptyMessage(ChatConstant.MESSAGE_OFFLINE);
                    }else if(s.startsWith("You are")){
                        chatHandler.sendEmptyMessage(ChatConstant.MESSAGE_ONLINE);
                    }else if(!s.startsWith("new connection:")){
                        Message message = new Message();
                        message.what = ChatConstant.MESSAGE_NEW_MSG;
                        message.obj = s;
                        chatHandler.sendMessage(message);
                    }
                }
                @Override
                public void onMessage(ByteBuffer s) {
                    Log.v(TAG,"onMessage ByteBuffer");
                    Message message = new Message();
                    message.what = ChatConstant.MESSAGE_NEW_MEDIA;
                    message.obj = s;
                    chatHandler.sendMessage(message);
                }
                @Override
                public void onClose(int i, String s, boolean b) {
                    Log.v(TAG,"onClose " + String.valueOf(i) + " " + s + " " + String.valueOf(b));
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
