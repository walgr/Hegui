package com.wpf.hegui.hotload;

import android.annotation.SuppressLint;
import android.content.Context;

import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by virjar on 2017/12/23.<br/>
 * 讲一些常用对象放到静态区域
 */

public class SharedObject {
    public static ClassLoader masterClassLoader;
    public static ClassLoader pluginClassLoader;
    @SuppressLint("StaticFieldLeak")
    public static Context context;
    //public static String pluginApkLocation;
    public static XC_LoadPackage.LoadPackageParam loadPackageParam;

    static void clear() {
        masterClassLoader = null;
        pluginClassLoader = null;
        context = null;
      //  pluginApkLocation = null;
        loadPackageParam = null;
    }
}
