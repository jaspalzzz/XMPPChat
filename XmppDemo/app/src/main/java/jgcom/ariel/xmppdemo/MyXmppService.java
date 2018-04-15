package jgcom.ariel.xmppdemo;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Ariel-17 on 11/3/2017.
 */

public class MyXmppService extends Service {
    MyXmpp xmpp;
    String userName="g1.gmail.com@ec2-34-250-186-43.eu-west-1.compute.amazonaws.com";
    String password="6619";

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalBinder<MyXmppService>(this);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        xmpp = MyXmpp.getInstance(MyXmppService.this,userName,password);
        xmpp.connect();
    }


    @Override
    public int onStartCommand(final Intent intent, final int flags,
                              final int startId) {
        return Service.START_NOT_STICKY;
    }

    @Override
    public boolean onUnbind(final Intent intent) {
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        xmpp.connection.disconnect();
    }

}
