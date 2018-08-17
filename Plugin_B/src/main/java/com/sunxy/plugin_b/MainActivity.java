package com.sunxy.plugin_b;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends BaseActivity {

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(context, "  plugin  receiver", Toast.LENGTH_SHORT).show();
        }
    };
    private boolean isReg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.plugin_main);

        TextView tv = findViewById(R.id.textview);
        tv.setText("包名：" + getPackageName() + "---" + getResources().getString(R.string.app_name));

        findViewById(R.id.registerBroad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isReg){
                    isReg = true;

                    IntentFilter filter = new IntentFilter();
                    filter.addAction("sunxiaoyu");
                    registerReceiver(receiver,filter);
                }
            }
        });

        findViewById(R.id.imageview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("sunxiaoyu");
                sendBroadcast(intent);
            }
        });

        findViewById(R.id.staticView).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setAction("static_sxy");
                sendBroadcast(intent);

            }
        });

        findViewById(R.id.setResult).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(99);
                finish();
            }
        });

        findViewById(R.id.startService).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, MyService.class);
                startService(intent);
            }
        });

        findViewById(R.id.showToast).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, " 这是插件中的toast", Toast.LENGTH_SHORT).show();
            }
        });

        findViewById(R.id.toOtherActivity).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SecondActivity.class));
            }
        });
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isReg){
            unregisterReceiver(receiver);
        }
    }
}
