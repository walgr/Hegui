package com.wpf.hegui

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wpf.hegui.util.ACache
import java.util.*

/**
 * Created by 王朋飞 on 2021/6/15.
 *
 */
class AppApplication : Application() {

    companion object {

        var hookPackageName = ""
        var hookAppName = ""

        var hookResultData: MutableList<String>? = mutableListOf()
    }

    override fun onCreate() {
        super.onCreate()

        hookPackageName = ACache.get(this).getAsString("hookPackageName")
    }
}