package com.wpf.hegui.hotload;

import android.content.Context;

import com.google.common.collect.Lists;

import org.apache.commons.io.IOUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * Created by virjar on 2017/12/21.<br/>插件热加载器
 */

public class HotLoadPackageEntry {
    private static final String TAG = "HotPluginLoader";

    //这里需要通过反射调用，HotLoadPackageEntry的entry的全路径不允许改变（包括方法签名），方法签名是xposed回调和热加载器的桥梁，需要满足调用接口规范
    //但是这个类的其他地方是可以修改的，因为这个代码已经是在最新插件apk的类加载器里面执行了
    @SuppressWarnings("unused")
    public static void entry(ClassLoader masterClassLoader, ClassLoader pluginClassLoader, Context context, XC_LoadPackage.LoadPackageParam loadPackageParam) {

        //将一批有用的对象放置到静态区域，方便使用
        SharedObject.context = context;
        SharedObject.loadPackageParam = loadPackageParam;
        SharedObject.masterClassLoader = masterClassLoader;
        SharedObject.pluginClassLoader = pluginClassLoader;
        // SharedObject.pluginApkLocation = pluginApkLocation;

        List<XposedHotLoadCallBack> allCallBack = findAllCallBack();
        if (allCallBack.isEmpty()) {
            SharedObject.clear();
            return;
        }

        //开启日志
//        LogUtil.start(loadPackageParam.packageName, loadPackageParam.processName);

        //开启网络请求堆栈输出（便于爆破）
//        NetDataPrinter.hook(null, true);

        //开启webview调试 （便于分析h5实现的加解密）https://www.cnblogs.com/wmhuang/p/7396150.html
//        WebViewDebuggerController.enableDebug(masterClassLoader, loadPackageParam.packageName);

        //开启DroidSword
//        DroidSword.startDroidSword();

        //开启okhttp 异步拦截
        //OkHttpClientHook.hook();
        //目前使用同一个线程跳跃拦截模块追踪异步，okhttp的单独封装已经没有意义了

        //解决 certificate pinning
        //see https://github.com/moxie0/AndroidPinning
//        JustTrustMe.trustAllCertificate();

        //监控所有异步任务的堆栈，彻底解决通过堆栈定位代码，遇到异步无法定位的尴尬
//        ThreadPoolHookV2.monitorThreadPool();

        //如果程序自杀，通过这里来定位自杀代码
//        ExitMonitor.monitorAppExist();

        //开启js websocket服务
//        SocketServer.start();

        //加载业务代码回调
        for (XposedHotLoadCallBack callBack : allCallBack) {
            callBack.onXposedHotLoad();
        }
    }

    private static List<XposedHotLoadCallBack> findAllCallBack() {
        InputStream stream = SharedObject.pluginClassLoader.getResourceAsStream("assets/hotload_entry.txt");
        if (stream == null) {
            //cancel log print,because the code will execute for all app process
            //XposedBridge.log("can not find hotload_entry.txt,please check");
            return Collections.emptyList();
        }
        List<XposedHotLoadCallBack> result = Lists.newLinkedList();
        try {
            BufferedReader moduleClassesReader = new BufferedReader(new InputStreamReader(stream));
            String moduleClassName;
            while ((moduleClassName = moduleClassesReader.readLine()) != null) {
                moduleClassName = moduleClassName.trim();
                //ignore blank
                if (moduleClassName.isEmpty()) {
                    continue;
                }
                //ignore for comment
                if (moduleClassName.startsWith("#")) {
                    continue;
                }
                //ignore for action
                if (moduleClassName.startsWith("action:")) {
                    continue;
                }
                try {
                    Class<?> moduleClass = SharedObject.pluginClassLoader.loadClass(moduleClassName);

                    if (!XposedHotLoadCallBack.class.isAssignableFrom(moduleClass)) {
                        XposedBridge.log("    This class doesn't implement any sub-interface of XposedHotLoadCallBack, skipping it");
                        continue;
                    }
                    XposedHotLoadCallBack moduleInstance = (XposedHotLoadCallBack) moduleClass.newInstance();
                    if (moduleInstance.needHook(SharedObject.loadPackageParam)) {
                        result.add(moduleInstance);
                    }

                } catch (Throwable t) {
                    XposedBridge.log("    Failed to load class " + moduleClassName);
                    XposedBridge.log(t);
                }
            }
        } catch (IOException e) {
            XposedBridge.log("load hot plugin failed");
            XposedBridge.log(e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
        return result;
    }
}
