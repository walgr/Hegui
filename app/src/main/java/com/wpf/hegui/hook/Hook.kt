package com.wpf.hegui.hook

import android.content.*
import android.location.LocationManager
import android.net.wifi.WifiInfo
import android.provider.Settings.Secure
import android.telephony.TelephonyManager
import com.wpf.hegui.util.ContentProviderHelper
import de.robv.android.xposed.*
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.net.NetworkInterface
import android.util.Log
import com.wpf.hegui.hotload.XposedInit
import com.wpf.hegui.util.AppHelper
import de.robv.android.xposed.XposedHelpers.*

class Hook : XposedInit() {
    private val TAG = "Xposed"
    var hookPackageName = ""
    var isHook = false

    override fun onHookContext(context: Context) {
        super.onHookContext(context)
        isHook = ContentProviderHelper.getString(context, "postState", "0") == "1"
        if (isHook) return
        if (hookPackageName.isEmpty()) {
            hookPackageName = ContentProviderHelper.getString(context, "hookPackageName", "")
        }
        if (hookPackageName != loadPackageParam.packageName) {
            return
        }
        hook(loadPackageParam)
    }

    private fun hook(lpparam: LoadPackageParam) {
        if (isHook) return
        val appName = AppHelper.getApplicationNameByPackageName(context, hookPackageName)
        postMsg("正在hook应用:${appName}")
        isHook = true
        ContentProviderHelper.postHookState(context, isHook)
        //固定格式
        //8.0以下获取imei
        findAndHookMethod(
            TelephonyManager::class.java.name,  // 需要hook的方法所在类的完整类名
            lpparam.classLoader,  // 类加载器，固定这么写就行了
            "getDeviceId",  // 需要hook的方法名
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用getDeviceId()获取了imei"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )
        findAndHookMethod(
            TelephonyManager::class.java.name,
            lpparam.classLoader,
            "getDeviceId",
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用getDeviceId(int)获取了imei"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )
        //8.0以上获取imei
        findAndHookMethod(
            TelephonyManager::class.java.name,
            lpparam.classLoader,
            "getImei",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用getImei获取了imei"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )

        findAndHookMethod(
            TelephonyManager::class.java.name,
            lpparam.classLoader,
            "getImei",
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用getImei(int)获取了imei"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )

        findAndHookMethod(
            TelephonyManager::class.java.name,
            lpparam.classLoader,
            "getSubscriberId",
            Int::class.javaPrimitiveType,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用getSubscriberId获取了imsi"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )
        findAndHookMethod(
            WifiInfo::class.java.name,
            lpparam.classLoader,
            "getMacAddress",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用getMacAddress()获取了mac地址"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )
        findAndHookMethod(
            NetworkInterface::class.java.name,
            lpparam.classLoader,
            "getHardwareAddress",
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用getHardwareAddress()获取了mac地址"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )

        //可能获取android_id
        findAndHookMethod(
            Secure::class.java.name,
            lpparam.classLoader,
            "getString",
            ContentResolver::class.java,
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val name = param.args[1]
                    if (name == "android_id") {
                        val msg = "应用:${appName}调用Settings.Secure.getString获取了${name}"
                        Thread.dumpStack()
                        Log.e(TAG, msg)
                        postMsg(msg)
                    }
                }
            }
        )

        findAndHookMethod(
            LocationManager::class.java.name,
            lpparam.classLoader,
            "getLastKnownLocation",
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用getLastKnownLocation获取了GPS地址"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )

        findAndHookMethod(
            "android.app.ApplicationPackageManager",
            lpparam.classLoader,
            "queryIntentActivitiesAsUser",
            String::class.java,
            object : XC_MethodHook() {
                override fun beforeHookedMethod(param: MethodHookParam) {
                    val msg = "应用:${appName}调用${param.method}获取了应用列表"
                    Thread.dumpStack()
                    Log.e(TAG, msg)
                    postMsg(msg)
                }
            }
        )

    }

    private fun postMsg(msg: String) {
        ContentProviderHelper.postHookResult(context, msg)
    }
}