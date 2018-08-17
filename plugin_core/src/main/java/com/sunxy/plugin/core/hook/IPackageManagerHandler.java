package com.sunxy.plugin.core.hook;

import android.content.pm.PackageInfo;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/16 0016.
 */
public class IPackageManagerHandler implements InvocationHandler {

    private PackageInfo packageInfo = new PackageInfo();
    private Object sPackageManager;

    public IPackageManagerHandler(Object sPackageManager) {
        this.sPackageManager = sPackageManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getPackageInfo")) {
            Log.v("sunxyy", "pms getPackageInfo");
            return packageInfo;
        }
        return  method.invoke(sPackageManager,args);
    }
}
