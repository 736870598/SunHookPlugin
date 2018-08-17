package com.sunxy.plugin_b;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/15 0015.
 */
public class StateBroadReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, " this is plugin static reveiver " + context.getPackageName(), Toast.LENGTH_SHORT).show();
    }
}
