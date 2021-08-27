package com.wpf.hegui.hook

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.net.Uri
import com.wpf.hegui.util.ACache

/**
 * Created by 王朋飞 on 2021/6/15.
 *
 */
class HookContentProvider : ContentProvider() {

    private val SEPARATOR = "/"

    override fun getType(uri: Uri): String {
        val path: List<String> = uri.path!!.split(SEPARATOR)
        val key = path[1]
        return "" + ACache.get(context).getAsString(key)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val path: List<String> = uri.path!!.split(SEPARATOR)
        val key = path[1]
        if (key == "postHookResult") {
            values?.getAsString("hookResult")?.let {
                HookResultData.postResult(it)
            }
        }
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        return 0
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        return 0
    }
}