package com.sunxy.plugin.core.hook;

import dalvik.system.DexClassLoader;

/**
 * --
 * <p>
 * Created by sunxy on 2018/8/16 0016.
 */
public class CustomClassLoader extends DexClassLoader{

    public CustomClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
}
