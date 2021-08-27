package com.wpf.hegui.util

import android.content.Context
import android.content.pm.PackageManager

/**
 * Created by 王朋飞 on 2021/6/16.
 *
 */
object AppHelper {

    fun getApplicationNameByPackageName(context: Context?, packageName: String): String {
        if (context == null) return ""
        val pm = context.packageManager
        var name = ""
        name = try {
            pm.getApplicationLabel(pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA))
                .toString();
        } catch (e: PackageManager.NameNotFoundException) {
            ""
        }
        return name
    }
}