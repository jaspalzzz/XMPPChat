package jgcom.ariel.xmppdemo;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * Created by Ariel-17 on 11/3/2017.
 */

public class MyToast {

    public static void threadToast(final MyXmppService context, final String msg)
    {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {

                Toast.makeText(context,msg,Toast.LENGTH_SHORT).show();
            }
        });
    }
}
