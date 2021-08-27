package com.wpf.hegui.data

import android.graphics.drawable.Drawable

/**
 * Created by 王朋飞 on 2021/8/26.
 *
 */
data class AppInfo(
        var appIcon: Drawable?,
        var appName: String,
        var appPackageName: String?,
        var appVersion: String?
)
