package com.sunxy.plugin.core.hook;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.sunxy.plugin.core.component.ProxyActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * -- ActivityManagerNative交给AMS之前 在这里进行伪装处理
 * <p>
 * Created by sunxy on 2018/8/16 0016.
 */
public class AmsInvocationHandler implements InvocationHandler {

    private Object iActivityManagerObject;
    private Context context;

    public AmsInvocationHandler(Context context, Object iActivityManagerObject){
        this.context = context;
        this.iActivityManagerObject = iActivityManagerObject;
    }

    @Override
    public Object invoke(final Object proxy, Method method, final Object[] args) throws Throwable {
        if("startActivity".contains(method.getName())){
            for (int i = 0; i < args.length; i++){
                if(args[i] instanceof Intent){
                    //找到了startActivity的intent参数
                    Intent realIntent = (Intent) args[i]; //原意图,没有注册
                    if (!context.getApplicationInfo().packageName.equals( realIntent.getComponent().getPackageName())){
                        // 跳转的不是本apk的，需要设置
                        Intent proxyIntent = new Intent();
                        ComponentName componentName = new ComponentName(context, ProxyActivity.class);
                        proxyIntent.setComponent(componentName);
                        proxyIntent.putExtra("oldIntent", realIntent);
                        args[i] = proxyIntent;
                    }

                    break;
                }
            }
        }
        return method.invoke(iActivityManagerObject, args);
    }
}
