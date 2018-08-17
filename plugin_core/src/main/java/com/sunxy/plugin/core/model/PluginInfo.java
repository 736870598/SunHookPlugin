package com.sunxy.plugin.core.model;

import android.content.pm.PackageInfo;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/15 0015.
 */
public class PluginInfo {

    private PackageInfo packageInfo;
    private AssetManager assetManager;
    private Resources resources;
    private ClassLoader classLoader;
    private String apkPath;
    private Object packageObj;


    public PackageInfo getPackageInfo() {
        return packageInfo;
    }

    public void setPackageInfo(PackageInfo packageInfo) {
        this.packageInfo = packageInfo;
    }

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public void setAssetManager(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    public Resources getResources() {
        return resources;
    }

    public void setResources(Resources resources) {
        this.resources = resources;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public String getApkPath() {
        return apkPath;
    }

    public void setApkPath(String apkPath) {
        this.apkPath = apkPath;
    }

    public Object getPackageObj() {
        return packageObj;
    }

    public void setPackageObj(Object packageObj) {
        this.packageObj = packageObj;
    }
}
