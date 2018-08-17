package com.sunxy.plugin.core;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.sunxy.plugin.core.hook.HookManager;
import com.sunxy.plugin.core.model.PluginInfo;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/15 0015.
 */
public class SunPlugin {

    private static final SunPlugin ourInstance = new SunPlugin();

    public static SunPlugin get() {
        return ourInstance;
    }

    private SunPlugin() { }

    public void init(Application application){
        HookManager.manager().init(application);
    }

    public PluginInfo loadPlugin(String pluginApkPath){
        try {
            HookManager.manager().hookAms();
            HookManager.manager().hookActivityThread();
            return HookManager.manager().loadApk(pluginApkPath);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public boolean startActivity(Context context, String activityName) {
        try{
//            Class<?> aClass = context.getClassLoader().loadClass(activityName);
//            ComponentName componentName = new ComponentName(aClass.getPackage().getName(), aClass.getName());
            ComponentName componentName = new ComponentName("com.sunxy.plugin_b", "com.sunxy.plugin_b.MainActivity");
            Intent intent = new Intent();
            intent.setComponent(componentName);
            context.startActivity(intent);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    public boolean startActivityForResult(Activity context, String activityName, int requestCode) {
        try{
            Class<?> aClass = context.getClassLoader().loadClass(activityName);
            ComponentName componentName = new ComponentName(aClass.getPackage().getName(), aClass.getName());
            Intent intent = new Intent();
            intent.setComponent(componentName);
            context.startActivityForResult(intent, requestCode);
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
