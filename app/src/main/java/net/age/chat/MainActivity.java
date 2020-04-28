package net.age.chat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements Button.OnClickListener{
    static final String TAG = "MainActivity";
    String[] msgs;
    TextView chatMsg;
    EditText editText;
    Button send;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }
    private void init(){
        msgs = getResources().getStringArray(R.array.msgs);
        send = (Button)findViewById(R.id.send);
        send.setOnClickListener(this);

        chatMsg = findViewById(R.id.message_all);

        editText = findViewById(R.id.newMsg);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.send:
                Intent intent = new Intent(MainActivity.this,net.age.chat.ChatActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }


}
