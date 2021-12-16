package com.wpf.hegui

import android.app.Application
import androidx.lifecycle.MutableLiveData
import com.wpf.hegui.util.ACache

/**
 * Created by 王朋飞 on 2021/6/15.
 *
 */
class AppApplication : Application() {

    companion object {
        var hookPackageName = ""
        var hookAppName = ""
        var isHook = false;

        var hookResultData: MutableLiveData<MutableList<String>> = MutableLiveData()
    }

    override fun onCreate() {
        super.onCreate()
        hookPackageName = ACache.get(this).getAsString("hookPackageName")
    }
}