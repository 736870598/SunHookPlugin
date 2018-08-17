package com.sunxy.plugin.core.hook;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import com.sunxy.plugin.core.utils.ReflexUtils;

import java.lang.reflect.Field;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/16 0016.
 */
public class ActivityThreadHandlerCallback implements Handler.Callback{

    private Handler handler;

    ActivityThreadHandlerCallback(Handler handler) {
        this.handler = handler;
    }

    @Override
    public boolean handleMessage(Message msg) {

        //替换回来之前的intent
        if(msg.what == 100){
            handlerLaunchActivity(msg);
        }

        handler.handleMessage(msg);
        return true;
    }

    private void handlerLaunchActivity(Message msg) {
        Object obj = msg.obj;  //ActivityClientRecord

        try {
            Field intentField = ReflexUtils.findField(obj, "intent");
            Intent proxyIntent = (Intent) intentField.get(obj); //代理意图，需要替换
            Intent realIntent = proxyIntent.getParcelableExtra("oldIntent");
            if(realIntent != null){
                intentField.set(obj, realIntent);

                //将obj里的applicationInfo的包名改成插件的包名，
                //因为ActivityThread中通过包名去获取对应的LoadedApk，
                //再通过LoadedApk中的classLoader加载相应的类。
                Field activityInfoField = ReflexUtils.findField(obj, "activityInfo");
                ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);
                activityInfo.applicationInfo.packageName =  realIntent.getPackage() == null ?
                        realIntent.getComponent().getPackageName() : realIntent.getPackage();

            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
