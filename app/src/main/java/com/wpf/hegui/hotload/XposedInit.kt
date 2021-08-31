package com.wpf.hegui.hotload

import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.XC_MethodHook
import kotlin.Throws
import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageInfo
import de.robv.android.xposed.XposedBridge
import dalvik.system.PathClassLoader
import android.content.pm.PackageManager
import android.util.Log
import brut.androlib.res.decoder.AXmlResourceParser
import com.google.common.collect.Maps
import de.robv.android.xposed.XposedHelpers.findAndHookMethod
import de.robv.android.xposed.XposedHelpers.findClass
import org.apache.commons.io.IOUtils
import org.apache.commons.lang3.StringUtils
import org.xmlpull.v1.XmlPullParser
import java.io.InputStream
import java.lang.Exception
import java.util.zip.ZipFile
import de.robv.android.xposed.callbacks.XC_LoadPackage


/**
 * XposedInit
 * <br></br>
 * 请注意，该类是热加载入口，不允许直接访问工程其他代码，只要访问过的类，都不能实现热加载
 *
 * @author virjar@virjar.com
 */
open class XposedInit : IXposedHookLoadPackage {

    var context: Context? = null
    lateinit var loadPackageParam: LoadPackageParam

    override fun handleLoadPackage(lpparam: LoadPackageParam) {
        loadPackageParam = lpparam
        //获取本检测AppContext
        findAndHookMethod(
            Application::class.java,
            "attach",
            Context::class.java,
            object : XC_MethodHook(PRIORITY_HIGHEST * 2) {
                //由于集成了脱壳功能，所以必须选择before了
                @Throws(Throwable::class)
                override fun beforeHookedMethod(param: MethodHookParam) {
                    hotLoadPlugin(lpparam.classLoader, param.args[0] as Context, lpparam)
                }
            })
        //获取被检测AppContext
        if (lpparam.isFirstApplication && context == null) {
            try {
                val contextClass: Class<*> =
                    findClass(ContextWrapper::class.java.name, lpparam.classLoader)
                findAndHookMethod(contextClass, "getApplicationContext", object : XC_MethodHook() {
                    @Throws(Throwable::class)
                    override fun afterHookedMethod(param: MethodHookParam) {
                        super.afterHookedMethod(param)
                        if (context != null) return
                        context = param.result as? Context
                        onHookContext(context!!)
                    }
                })
            } catch (t: Throwable) {
                XposedBridge.log("获取上下文出错$t")
            }
        }
    }

    open fun onHookContext(context: Context) {

    }

    @SuppressLint("PrivateApi")
    private fun hotLoadPlugin(
        ownerClassLoader: ClassLoader,
        context: Context,
        lpparam: LoadPackageParam
    ) {
        var hasInstantRun = true
        try {
            XposedInit::class.java.classLoader.loadClass(INSTANT_RUN_CLASS)
        } catch (e: ClassNotFoundException) {
            //正常情况应该报错才对
            hasInstantRun = false
        }
        if (hasInstantRun) {
            Log.e(
                "weijia",
                "  Cannot load module, please disable \"Instant Run\" in Android Studio."
            )
            return
        }

        val hotClassLoader = replaceClassloader(context, lpparam)
        hasInstantRun = true
        try {
            hotClassLoader!!.loadClass(INSTANT_RUN_CLASS)
        } catch (e: ClassNotFoundException) {
            //正常情况应该报错才对
            hasInstantRun = false
        }
        if (hasInstantRun) {
            Log.e(
                "weijia",
                "  Cannot load module, please disable \"Instant Run\" in Android Studio."
            )
            return
        }

        try {
            val aClass = hotClassLoader!!.loadClass("com.wpf.hegui.hotload.HotLoadPackageEntry")
            Log.i("weijia", "invoke hot load entry")
            aClass
                .getMethod(
                    "entry",
                    ClassLoader::class.java,
                    ClassLoader::class.java,
                    Context::class.java,
                    LoadPackageParam::class.java
                )
                .invoke(null, ownerClassLoader, hotClassLoader, context, lpparam)
        } catch (e: Exception) {
            if (e is ClassNotFoundException) {
                val inputStream = hotClassLoader!!.getResourceAsStream("assets/hotload_entry.txt")
                if (inputStream == null) {
                    XposedBridge.log("do you not disable Instant Runt for Android studio?")
                } else {
                    IOUtils.closeQuietly(inputStream)
                }
            }
            XposedBridge.log(e)
        }
    }

    companion object {
        fun packageName(classLoader: ClassLoader): String? {
            val element = bindApkLocation(classLoader) ?: return null
            //原文件可能已被删除，直接打开文件无法得到句柄，所以只能去获取持有删除文件句柄对象
            val zipFile = XposedHelpers.getObjectField(element, "zipFile") as ZipFile
            return findPackageName(zipFile)
        }

        private fun replaceClassloader(context: Context, lpparam: LoadPackageParam): ClassLoader? {
            val classLoader = XposedInit::class.java.classLoader
            if (classLoader !is PathClassLoader) {
                XposedBridge.log("classloader is not PathClassLoader: $classLoader")
                return classLoader
            }
            val packageName = "com.wpf.hegui"

            //find real apk location by package name
            val packageManager = context.packageManager
            if (packageManager == null) {
                XposedBridge.log("can not find packageManager")
                return classLoader
            }
            var packageInfo: PackageInfo? = null
            try {
                packageInfo =
                    packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA)
            } catch (e: PackageManager.NameNotFoundException) {
                //ignore
            }
            if (packageInfo == null) {
                XposedBridge.log("can not find plugin install location for plugin: $packageName")
                return classLoader
            }

            //check if apk file has relocated,apk location maybe change if xposed plugin is reinstalled(system did not reboot)
            //xposed 插件安装后不能立即生效（需要重启Android系统）的本质原因是这两个文件不equal

            //hotClassLoader can load apk class && classLoader.getParent() can load xposed framework and android framework
            //使用parent是为了绕过缓存，也就是不走系统启动的时候链接的插件apk，但是xposed框架在这个classloader里面持有，所以集成
            return createClassLoader(classLoader.getParent(), packageInfo)
        }

        private const val INSTANT_RUN_CLASS = "com.android.tools.fd.runtime.BootstrapApplication"
        private val classLoaderCache = Maps.newConcurrentMap<String, PathClassLoader>()

        /**
         * 这样做的目的是保证classloader单例，因为宿主存在多个dex的时候，或者有壳的宿主在解密代码之后，存在多次context的创建，当然xposed本身也存在多次IXposedHookLoadPackage的回调
         *
         * @param parent      父classloader
         * @param packageInfo 插件自己的包信息
         * @return 根据插件apk创建的classloader
         */
        private fun createClassLoader(
            parent: ClassLoader,
            packageInfo: PackageInfo
        ): PathClassLoader? {
            if (classLoaderCache.containsKey(packageInfo.applicationInfo.sourceDir)) {
                return classLoaderCache[packageInfo.applicationInfo.sourceDir]
            }
            synchronized(XposedInit::class.java) {
                if (classLoaderCache.containsKey(packageInfo.applicationInfo.sourceDir)) {
                    return classLoaderCache[packageInfo.applicationInfo.sourceDir]
                }
                XposedBridge.log("create a new classloader for plugin with new apk path: " + packageInfo.applicationInfo.sourceDir)
                val hotClassLoader = PathClassLoader(packageInfo.applicationInfo.sourceDir, parent)
                classLoaderCache.putIfAbsent(packageInfo.applicationInfo.sourceDir, hotClassLoader)
                return hotClassLoader
            }
        }

        /**
         * File name in an APK for the Android manifest.
         */
        private const val ANDROID_MANIFEST_FILENAME = "AndroidManifest.xml"
        private fun bindApkLocation(pathClassLoader: ClassLoader): Any? {
            // we can`t call package parser in android inner api,parse logic implemented with native code
            //this object is dalvik.system.DexPathList,android inner api
            val pathList = XposedHelpers.getObjectField(pathClassLoader, "pathList")
            if (pathList == null) {
                XposedBridge.log("can not find pathList in pathClassLoader")
                return null
            }

            //this object is  dalvik.system.DexPathList.Element[]
            val dexElements = XposedHelpers.getObjectField(pathList, "dexElements") as? Array<Any>
            if (dexElements == null || dexElements.isEmpty()) {
                XposedBridge.log("can not find dexElements in pathList")
                return null
            }
            return dexElements[0]
        }

        private fun findPackageName(zipFile: ZipFile?): String? {
            if (zipFile == null) {
                return null
            }
            var stream: InputStream? = null
            return try {
                stream = zipFile.getInputStream(zipFile.getEntry(ANDROID_MANIFEST_FILENAME))
                val xpp = AXmlResourceParser(stream)
                var eventType: Int
                //migrated form ApkTool
                while (xpp.next().also { eventType = it } > -1) {
                    if (XmlPullParser.END_DOCUMENT == eventType) {
                        return null
                    } else if (XmlPullParser.START_TAG == eventType && "manifest".equals(
                            xpp.name,
                            ignoreCase = true
                        )
                    ) {
                        // read <manifest> for package:
                        for (i in 0 until xpp.attributeCount) {
                            if (StringUtils.equalsIgnoreCase(xpp.getAttributeName(i), "package")) {
                                return xpp.getAttributeValue(i)
                            }
                        }
                    }
                }
                null
            } catch (e: Exception) {
                XposedBridge.log(e)
                null
            } finally {
                //不能关闭zipFile
                IOUtils.closeQuietly(stream)
            }
        }
    }
}