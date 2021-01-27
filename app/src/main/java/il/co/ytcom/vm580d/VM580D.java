package il.co.ytcom.vm580d;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class VM580D extends Application {
    static VM580D c;

    final static String TAG = "VM580D";

    public static synchronized VM580D getInstance() {
        return c;
    }

    private void registerListeners() {
        registerReceiver(new BroadcastReceiver() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "onReceive: cm");
                intent.getExtras().keySet().forEach(s -> {
                    Log.d(TAG, s + " : " + intent.getExtras().get(s));
                });

//                il.co.ytcom.vm580d D/VM580D: filePath : /storage/3E90-110A/DCIM/Audio/20180103_191745.aac
//                il.co.ytcom.vm580d D/VM580D: audioRecord : 1
//                il.co.ytcom.vm580d D/VM580D: onReceive: com.media.operation.notify
//                il.co.ytcom.vm580d D/VM580D: filePath : /storage/3E90-110A/DCIM/Photo/20180103_191753.jpg
//                il.co.ytcom.vm580d D/VM580D: takePhoto : 0
//                il.co.ytcom.vm580d D/VM580D: onReceive: com.media.operation.notify
//                il.co.ytcom.vm580d D/VM580D: filePath : /storage/3E90-110A/DCIM/Photo/20180103_191757.jpg
//                il.co.ytcom.vm580d D/VM580D: takePhoto : 0
//                il.co.ytcom.vm580d D/VM580D: onReceive: com.media.operation.notify
//                il.co.ytcom.vm580d D/VM580D: filePath : /storage/3E90-110A/DCIM/Video/20180103_201050.mp4
//                il.co.ytcom.vm580d D/VM580D: videoRecord : 1
//                il.co.ytcom.vm580d D/VM580D: onReceive: com.media.operation.notify
//                il.co.ytcom.vm580d D/VM580D: filePath : /storage/3E90-110A/DCIM/Video/20180103_201050.mp4
//                il.co.ytcom.vm580d D/VM580D: videoRecord : 0

                try {
                    if ((intent.getExtras().get("audioRecord") != null && intent.getExtras().getInt("audioRecord") == 0) ||
                            (intent.getExtras().get("videoRecord") != null && intent.getExtras().getInt("videoRecord") == 0) ||
                                    (intent.getExtras().get("takePhoto") != null && intent.getExtras().getInt("takePhoto") == 0)
                    ) {
                        CryptoService.startActionEncrypt(VM580D.getInstance().getApplicationContext(),intent.getExtras().getString("filePath"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        },new IntentFilter("com.media.operation.notify"));
    }


    @Override
    public void onCreate() {
        super.onCreate();
        c = this;
        registerListeners();
    }
}
