package com.sunxy.sunhookplugin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.sunxy.plugin.core.SunPlugin;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void toPlugin(View v){
        SunPlugin.get().startActivity(this, "com.sunxy.plugin_b.MainActivity");
//        ComponentName componentName = new ComponentName("com.sunxy.plugin_b", "com.sunxy.plugin_b.MainActivity");
//        Intent intent = new Intent();
//        intent.setComponent(componentName);
//        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.v("sunxyy", "requestCode : " + requestCode + " , resultCode: " + resultCode);
    }
}
