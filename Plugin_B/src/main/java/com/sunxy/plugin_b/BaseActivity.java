package com.sunxy.plugin_b;

import android.app.Activity;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/15 0015.
 */
public class BaseActivity extends Activity {


    @Override
    public AssetManager getAssets() {
        if (getApplication() != null && getApplication().getAssets() != null) {
            return getApplication().getAssets();
        }
        return super.getAssets();
    }


    @Override
    public Resources getResources() {
        if (getApplication() != null && getApplication().getResources() != null) {
            return getApplication().getResources();
        }
        return super.getResources();
    }

}
