package com.sunxy.plugin_b;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.TextView;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/16 0016.
 */
public class SecondActivity extends Activity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView textView = new TextView(this);
        textView.setText(getPackageName() + "--" + getResources().getString(R.string.app_name));
        setContentView(textView);
    }
}
