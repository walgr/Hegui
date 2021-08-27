package com.wpf.hegui.util

import android.app.Activity
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import com.wpf.hegui.data.AppInfo

/**
 * Created by 王朋飞 on 2021/8/26.
 *
 */
object AppPackageUtil {

    fun getAllPackage(context: Activity): List<AppInfo> {
        val packageManager = context.packageManager
        val packageInfoList = packageManager.getInstalledPackages(0)
        return packageInfoList.filter {
            (it.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0
                    && "com.wpf.hegui" != it.packageName
        }.map {
            AppInfo(appIcon = getAppIcon(packageManager, it.applicationInfo),
                    appName = getAppName(packageManager, it.applicationInfo),
                    appPackageName = it.packageName, appVersion = it.versionName)
        }
    }

    fun getAppIcon(packageManager: PackageManager ,info: ApplicationInfo): Drawable {
        return packageManager.getApplicationIcon(info)
    }

    fun getAppName(packageManager: PackageManager ,info: ApplicationInfo): String {
        return packageManager.getApplicationLabel(info).toString()
    }
}