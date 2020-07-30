package net.age.chat;

import androidx.appcompat.app.AppCompatActivity;
import cn.qqtheme.framework.picker.FilePicker;
import cn.qqtheme.framework.util.StorageUtils;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;

import com.vise.basebluetooth.common.ChatConstant;
import com.vise.basebluetooth.mode.BaseMessage;
import com.vise.common_utils.log.LogUtils;
import com.vise.common_utils.utils.character.DateTime;

import net.age.chat.adapter.ChatAdapter;
import net.age.chat.model.ChatInfo;
import net.age.chat.model.ChatUtils;
import net.age.chat.model.FriendInfo;

import org.java_websocket.server.WebSocketServer;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener{
    static final String TAG = "ChatActivity";
    private ListView mChatMsgLv;
    private ChatAdapter mChatAdapter;
    private List<ChatInfo> mChatInfoList = new ArrayList<>();
    private ImageButton mMsgFaceIb;
    private ImageButton mMsgAddIb;
    private EditText mMsgEditEt;
    private ImageButton mMsgSendIb;
    private FrameLayout mEmojiconFl;
    private boolean mIsSendFile = false;
    private boolean mIsRecevFile = false;
    private File mSendFile;
    private String mFilePath;
    private String mLastMessage;
    ChatHandler handler = new ChatHandler();
    ChatClient chatClient = new ChatClient(handler);
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        initData();
        chatClient.chat();
    }

    private void initView(){
        mChatMsgLv = (ListView) findViewById(R.id.chat_msg_show_list);
        mMsgFaceIb = (ImageButton) findViewById(R.id.chat_msg_face);
        mMsgAddIb = (ImageButton) findViewById(R.id.chat_msg_add);
        mMsgAddIb.setOnClickListener(this);
        mMsgEditEt = (EditText) findViewById(R.id.chat_msg_edit);
        mMsgSendIb = (ImageButton) findViewById(R.id.chat_msg_send);
        mMsgSendIb.setOnClickListener(this);
        mEmojiconFl = (FrameLayout) findViewById(R.id.chat_emojicons);
    }

    private void initData(){
        mChatAdapter = new ChatAdapter(this);
        mChatMsgLv.setAdapter(mChatAdapter);
    }
    private void receive(String data,boolean sent){
        if(data == null || data.length() == 0){
            LogUtils.e("readData is Null or Empty!");
            return;
        }
        BaseMessage message = new BaseMessage();
        message.setMsgType(ChatConstant.VISE_COMMAND_TYPE_TEXT);
        message.setMsgContent(data);
        message.setMsgLength(data.length());

        ChatInfo chatInfo = new ChatInfo();
        chatInfo.setMessage(message);
        chatInfo.setReceiveTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));
        chatInfo.setSend(false);

        FriendInfo friendInfo = new FriendInfo();
        friendInfo.setOnline(true);
        friendInfo.setFriendNickName("清明");
        friendInfo.setIdentificationName("1989");

        chatInfo.setFriendInfo(friendInfo);

        if(!sent){
            mChatInfoList.add(chatInfo);
            mChatAdapter.setListAll(mChatInfoList);
            mLastMessage = data;
        }
    }
    private void sendOld(){
        ChatInfo chatInfo = new ChatInfo();
        Editable editable = mMsgEditEt.getText();
        FriendInfo friendInfo = new FriendInfo();
        friendInfo.setOnline(true);
        friendInfo.setFriendNickName("谷雨");
        friendInfo.setIdentificationName("1989");
        chatInfo.setFriendInfo(friendInfo);
        chatInfo.setSend(true);
        chatInfo.setSendTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));

        BaseMessage message = null;

        message = new BaseMessage();
        chatClient.send(editable.toString().trim());
        message.setMsgType(ChatConstant.VISE_COMMAND_TYPE_TEXT);
        message.setMsgContent(editable.toString().trim());
        if(editable.toString().equals("mm")){
//            chatClient.send(ChatUtils.image2byte("/sdcard/mm.jpeg"));
            new Thread(new Runnable() {
                @Override
                public void run() {
                    chatClient.send(ChatUtils.image2byte("/sdcard/mryy.apk"));
//                    chatClient.send(ChatUtils.image2byte("/sdcard/mm.jpeg"));
                }
            }).start();
        }
        message.setMsgLength(editable.toString().length());

        chatInfo.setMessage(message);

        mChatInfoList.add(chatInfo);
        mChatAdapter.setListAll(mChatInfoList);
    }
    private void send(){
        ChatInfo chatInfo = new ChatInfo();
        Editable editable = mMsgEditEt.getText();
        FriendInfo friendInfo = new FriendInfo();
        friendInfo.setOnline(true);
        friendInfo.setFriendNickName("谷雨");
        friendInfo.setIdentificationName("1989");
        chatInfo.setFriendInfo(friendInfo);
        chatInfo.setSend(true);
        chatInfo.setSendTime(DateTime.getStringByFormat(new Date(), DateTime.DEFYMDHMS));

        BaseMessage message = null;

        message = new BaseMessage();
        String msg = editable.toString().trim();
        if(mIsSendFile){
            msg += "*#*#";
        }
        chatClient.send(msg);
        message.setMsgType(ChatConstant.VISE_COMMAND_TYPE_TEXT);
        message.setMsgContent(msg);

        if(mFilePath != null){
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.v(TAG,"sendFile" + mFilePath);
                    chatClient.send(ChatUtils.image2byte(mFilePath));
                    mFilePath = null;
                }
            }).start();
        }
//        if(msg.equals("mm")){
//            chatClient.send(ChatUtils.image2byte("/sdcard/mm.jpeg"));
//        }
        message.setMsgLength(msg.length());
        chatInfo.setMessage(message);

        mChatInfoList.add(chatInfo);
        mChatAdapter.setListAll(mChatInfoList);
    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.chat_msg_send:
                if(mMsgEditEt.getText() != null && mMsgEditEt.getText().toString().trim().length() > 0){
                    send();
                }
                break;
            case R.id.chat_msg_add:
                FilePicker picker = new FilePicker(ChatActivity.this, FilePicker.FILE);
                picker.setShowHideDir(false);
                picker.setRootPath(StorageUtils.getInternalRootPath(ChatActivity.this));
                picker.setOnFilePickListener(new FilePicker.OnFilePickListener() {
                    @Override
                    public void onFilePicked(String currentPath) {
                        mIsSendFile = true;
                        mFilePath = currentPath;
                        mSendFile = new File(mFilePath);
                        mMsgEditEt.setText(mSendFile.getName());
                    }
                });
                picker.show();
                break;
            default:
                break;
        }
    }
    class ChatHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case net.age.chat.ChatConstant.MESSAGE_ONLINE:
                    Log.v(TAG,"New Member");
                    break;
                case net.age.chat.ChatConstant.MESSAGE_NEW_MSG:
                    if(msg.obj != null){
                        Log.v(TAG,"New Message " + msg.obj.toString());
                        if(msg.obj.toString().equals(mMsgEditEt.getText().toString())){
                            Log.v(TAG,"Here");
                            receive(msg.obj.toString(),true);
                            mMsgEditEt.getText().clear();
                        }
                        else {
                            receive(msg.obj.toString(),false);}
                        }
                    break;
                case net.age.chat.ChatConstant.MESSAGE_NEW_MEDIA:
                    mMsgEditEt.getText().clear();
                    Log.v(TAG,"Media Arrived");
                    if(msg.obj != null){
                        Log.v(TAG,"New Message" + msg.obj.toString());
                        ByteBuffer bb = (ByteBuffer)msg.obj;
//                        ChatUtils.byte2image(bb.array(),"/sdcard/oo.jpeg");
                        ChatUtils.byte2image(bb.array(),"/sdcard/" + mLastMessage);
                    }
                    break;
                case net.age.chat.ChatConstant.MESSAGE_OFFLINE:
                    Log.v(TAG,"Someone Left");
                    break;
                case net.age.chat.ChatConstant.MESSAGE_CLEAR_EDIT:
//                    mMsgEditEt.getText().clear();
                    Log.v(TAG,"Someone Left");
                    break;
                default:
                    break;
            }
        }
    }
}
