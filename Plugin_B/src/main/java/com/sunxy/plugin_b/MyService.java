package com.sunxy.plugin_b;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/15 0015.
 */
public class MyService extends Service {



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v("sunxyy", "plugin Service onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }
}
