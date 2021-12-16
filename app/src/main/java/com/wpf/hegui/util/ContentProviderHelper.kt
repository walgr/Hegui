package com.wpf.hegui.util

import android.content.ContentValues
import android.content.Context
import android.net.Uri


/**
 * Created by 王朋飞 on 2021/6/16.
 *
 */
object ContentProviderHelper {

    private const val CONTENT = "content://"
    private const val AUTHORITY = "com.wpf.hegui.hook.HookContentProvider"
    private const val SEPARATOR = "/"
    private const val CONTENT_URI = CONTENT + AUTHORITY
    private const val NULL_STRING = "null"

    fun getString(context: Context?, name: String, defaultValue: String = ""): String {
        val hookContentProvider = context?.contentResolver
        val uri: Uri = Uri.parse(CONTENT_URI + SEPARATOR + name)
        val rtn: String? = hookContentProvider?.getType(uri)
        return if (rtn == null || rtn == NULL_STRING) {
            defaultValue
        } else rtn
    }

    fun postHookResult(context: Context?, hookResult: String) {
        val hookContentProvider = context?.contentResolver
        val uri: Uri = Uri.parse(CONTENT_URI + SEPARATOR + "postHookResult")
        hookContentProvider?.insert(uri, ContentValues().also {
            it.put("hookResult", hookResult)
        })
    }

    fun postHookState(context: Context?, hookState: Boolean) {
        val hookContentProvider = context?.contentResolver
        val uri: Uri = Uri.parse(CONTENT_URI + SEPARATOR + "postState")
        hookContentProvider?.insert(uri, ContentValues().also {
            it.put("postState", hookState)
        })
    }
}