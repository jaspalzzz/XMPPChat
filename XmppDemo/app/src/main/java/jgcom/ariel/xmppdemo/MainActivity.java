package jgcom.ariel.xmppdemo;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

import jgcom.ariel.xmppdemo.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static List<ChatModel> chatList;
    public static ChatAdapter chatAdapter;
    private View.OnClickListener mClickListener = onClickListener();
    ActivityMainBinding binding;
    LocalBroadcastManager manager;
    BroadcastReceiver mainReciver=broadcastReceiver();
    MyXmppService myXmppService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        manager = LocalBroadcastManager.getInstance(MainActivity.this);
        startService(new Intent(this, MyXmppService.class));
        chatList = new ArrayList<>();
        binding.sendBt.setOnClickListener(mClickListener);
        inflateChatList();

    }

    public void inflateChatList() {
        chatAdapter = new ChatAdapter(this, chatList);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        binding.chatlist.setLayoutManager(manager);
        binding.chatlist.setAdapter(chatAdapter);
    }


    public View.OnClickListener onClickListener() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // here sending message
                if (MyXmpp.instance.connection.isAuthenticated()) {
                    ChatModel chatModel = new ChatModel();
                    chatModel.setMsgId(CommonMethods.getInstance().getMsgID());
                    chatModel.setDate(CommonMethods.getInstance().getCurrentDate());
                    chatModel.setTime(CommonMethods.getInstance().getCurrenttime());
                    chatModel.setMsgStatus(getResources().getString(R.string.send));
                    chatModel.setMsg(binding.messageEt.getText().toString());
                    chatModel.setIsMine("true");
                    MyXmpp.instance.sendMessge(chatModel);
                    chatList.add(chatModel);
                    chatAdapter.notifyAdapter(chatList);
                    binding.messageEt.setText("");
                } else {
                    Log.e(">>>", "connection at main + not authenticated+");
                    binding.messageEt.setText("");
                }
            }
        };
    }


    public BroadcastReceiver broadcastReceiver()
    {
      return   new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent != null && intent.getAction() != null) {
                    if (intent.getAction().equals("msg")) {
                        ChatModel chatModel = new Gson().fromJson(intent.getStringExtra("msg"), ChatModel.class);
                        chatModel.setIsMine("false");
                        chatList.add(chatModel);
                        chatAdapter.notifyAdapter(chatList);
                    }
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter();
        filter.addAction("msg");
        manager.registerReceiver(mainReciver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.unregisterReceiver(mainReciver);
    }


}
