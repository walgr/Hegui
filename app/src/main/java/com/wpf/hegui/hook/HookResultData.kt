package com.wpf.hegui.hook

import com.wpf.hegui.AppApplication

/**
 * Created by 王朋飞 on 2021/6/16.
 *
 */
object HookResultData {

    fun postResult(result: String) {
//        if (AppApplication.hookResultData?.size?:0 >= 100) {
//            AppApplication.hookResultData?.removeFirst()
//        }
        AppApplication.hookResultData?.add(result)
    }
}