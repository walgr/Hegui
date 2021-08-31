package com.wpf.hegui.hook

import com.wpf.hegui.AppApplication
import kotlinx.coroutines.*

/**
 * Created by 王朋飞 on 2021/6/16.
 *
 */
object HookResultData {

    fun postResult(result: String) {
        CoroutineScope(Dispatchers.Main).launch {
            if (AppApplication.hookResultData.value == null) {
                AppApplication.hookResultData.value = arrayListOf(result)
            }
            AppApplication.hookResultData.value?.add(result)
            AppApplication.hookResultData.postValue(AppApplication.hookResultData.value)
        }
    }
}