package net.age.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.vise.basebluetooth.common.ChatConstant;
import com.vise.basebluetooth.mode.BaseMessage;
import com.vise.common_utils.log.LogUtils;
import com.vise.common_utils.utils.character.DateTime;

import net.age.chat.adapter.ChatAdapter;
import net.age.chat.model.ChatInfo;
import net.age.chat.model.ChatUtils;
import net.age.chat.model.FriendInfo;
import net.age.chat.model.MyClipBoardManager;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static net.age.chat.ChatConstant.WELCOME;
import static net.age.chat.ChatConstant.fileIndicator;

public class ChatActivity extends AppCompatActivity implements View.OnClickListener,AdapterView.OnItemLongClickListener {
    // Request code for selecting a PDF document.
    private static final int GET_CONTENT_INTENT = 1000;
    private static final int REQUEST_FOR_READ_EXTERNAL_PERMISSION = 1001;
    private static final int REQUEST_FOR_WRITE_EXTERNAL_PERMISSION = 1002;
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
    private String mFilePath;
    private String mLastMessage;
    ChatHandler handler = new ChatHandler();
    ChatClient chatClient = new ChatClient(handler);
    Object mMessage;
    Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        initView();
        initData();
        chatClient.chat();
        mContext = this;
    }

    private void initView(){
        mChatMsgLv = (ListView) findViewById(R.id.chat_msg_show_list);
        mChatMsgLv.setOnItemLongClickListener(this);
        mMsgFaceIb = (ImageButton) findViewById(R.id.chat_msg_face);
        mMsgAddIb = (ImageButton) findViewById(R.id.chat_msg_add);
        mMsgAddIb.setOnClickListener(this);
        mMsgEditEt = (EditText) findViewById(R.id.chat_msg_edit);
        mMsgSendIb = (ImageButton) findViewById(R.id.chat_msg_send);
        mMsgSendIb.setOnClickListener(this);
        mEmojiconFl = (FrameLayout) findViewById(R.id.chat_emojicons);
        Toast.makeText(this,WELCOME,Toast.LENGTH_SHORT).show();
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
            mLastMessage = data;
            mChatInfoList.add(chatInfo);
            mChatAdapter.setListAll(mChatInfoList);
        }
        mMsgEditEt.getText().clear();

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

        BaseMessage message = new BaseMessage();
        String msg = editable.toString().trim();
        if(mIsSendFile){
            mLastMessage = msg;
            msg += fileIndicator;
            mIsSendFile = false;
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
                }
            }).start();
        }
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
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    int permission = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
                    if (permission == PackageManager.PERMISSION_GRANTED) {
                        openFile(Uri.parse(Environment.getExternalStorageDirectory() + "/"));
                    }else {
                        requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                                REQUEST_FOR_READ_EXTERNAL_PERMISSION);
                    }
                }else {
                    openFile(Uri.parse(Environment.getExternalStorageDirectory() + "/"));
                }
                break;
            default:
                break;
        }
    }



    private void openFile(Uri pickerInitialUri) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
//        intent.setType("image/jpeg");

        // Optionally, specify a URI for the file that should appear in the
        // system file picker when it loads.
        intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri);

        startActivityForResult(intent, GET_CONTENT_INTENT);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {
        if (requestCode == GET_CONTENT_INTENT
                && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            Uri uri = null;
            // Perform operations on the document using its URI.
            if (resultData != null) {
                uri = resultData.getData();
//                grantUriPermission();
                mFilePath = ChatUtils.getPath(this,uri);
                mIsSendFile = true;
                mMsgEditEt.setText(new File(mFilePath).getName());
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_FOR_READ_EXTERNAL_PERMISSION){
//            if (permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            if (permissions[0].equals(Manifest.permission.READ_EXTERNAL_STORAGE)
                    &&grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //用户同意使用write
                openFile(Uri.parse(Environment.getExternalStorageDirectory() + "/"));
//                startGetImageThread();
            } else{
                //用户不同意，自行处理即可
                return;
            }
        }else if(requestCode == REQUEST_FOR_WRITE_EXTERNAL_PERMISSION){
            if(permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                saveFile(mMessage);
            }else {
                return;
            }
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
        ChatUtils.androidLog(null);
        switch (adapterView.getId()){
            case R.id.chat_msg_show_list:
                ChatInfo chatInfo = (ChatInfo)adapterView.getItemAtPosition(i);
                String clipString = chatInfo.getMessage().getMsgContent();
                MyClipBoardManager.copyToClipboard(this,clipString);
                Toast.makeText(this,R.string.textCopyed,Toast.LENGTH_SHORT).show();
                break;
            default:
                break;
        }
        return false;
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
                            Log.v(TAG,"Here true" + mLastMessage);
                            receive(msg.obj.toString(),true);
                    }
                        else {
                            Log.v(TAG,"Here false" + mLastMessage);
                            receive(msg.obj.toString(),false);
                        }
                    }
                    break;
                case net.age.chat.ChatConstant.MESSAGE_NEW_MEDIA:
                    mMsgEditEt.getText().clear();
                    Log.v(TAG,"Media Arrived" + mLastMessage + mFilePath);
                    if(mFilePath != null && mFilePath.contains(mLastMessage)){
                        mFilePath = null;
                        return;
                    }
                    if(msg.obj != null){
                        mMessage = msg.obj;
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            int permission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
                            if (permission == PackageManager.PERMISSION_GRANTED) {
                                saveFile(mMessage);
                            }else {
                                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        REQUEST_FOR_WRITE_EXTERNAL_PERMISSION);
                            }
                        }else {
                            saveFile(mMessage);
                        }
                    }
                    break;
                case net.age.chat.ChatConstant.MESSAGE_OFFLINE:
                    Log.v(TAG,"Someone Left");
                    break;
                case net.age.chat.ChatConstant.MESSAGE_CLEAR_EDIT:
//                    mMsgEditEt.getText().clear();
                    Log.v(TAG,"Someone Left");
                    break;
                case net.age.chat.ChatConstant.MESSAGE_DISCONNECT:
                    Toast.makeText(mContext,R.string.disConnect,Toast.LENGTH_SHORT).show();
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    protected void onDestroy(){
//        unregisterReceiver(networkReceiver);
        super.onDestroy();
    }
    @Override
    protected  void onResume() {
        super.onResume();
    }
    private void saveFile(Object msg){
        ByteBuffer bb = (ByteBuffer)msg;
        ChatUtils.byte2image(bb.array(),"/sdcard/" + mLastMessage);
    }
    BroadcastReceiver networkReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equalsIgnoreCase("android.net.conn.CONNECTIVITY_CHANGE")) {
                NetworkInfo networkInfo =
                        (NetworkInfo)intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
                if (networkInfo.isConnected()) {
                    Log.v(TAG,"networkReceiver connected");
                    chatClient.reconnect();
                }
                else {
                    Log.v(TAG,"networkReceiver disconnected");
                }
            }
//            Log.v(TAG,"networkReceiver" + intent.getDataString());
        }
    };
}
