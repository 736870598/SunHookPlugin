package com.sunxy.plugin.core.component;

import android.app.Application;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Environment;

import com.sunxy.plugin.core.SunPlugin;
import com.sunxy.plugin.core.hook.HookManager;
import com.sunxy.plugin.core.model.PluginInfo;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/15 0015.
 */
public class SunPluginApplication extends Application{

    private PluginInfo pluginInfo;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            SunPlugin.get().init(this);
            String apkPath = Environment.getExternalStorageDirectory().getAbsolutePath()
                    + "/Android/sunxy_file/plugin/Plugin_B-debug.apk";
            pluginInfo = SunPlugin.get().loadPlugin(apkPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Resources getResources() {
        return (pluginInfo != null && pluginInfo.getResources() != null) ? pluginInfo.getResources() : super.getResources();
    }

    @Override
    public AssetManager getAssets() {
        return (pluginInfo != null && pluginInfo.getAssetManager() != null) ? pluginInfo.getAssetManager() : super.getAssets();
    }
}
