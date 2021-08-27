package com.wpf.hegui.ui.adapter

import android.content.Context
import android.widget.*
import com.wpf.hegui.R

/**
 * Created by 王朋飞 on 2021/8/26.
 *
 */
class AppInfoAdapter(context: Context, mData: List<Map<String, *>>?)
    : SimpleAdapter(context, mData, R.layout.item_appinfo_layout,
        arrayOf("icon", "name"), arrayOf(R.id.icon, R.id.name).toIntArray()) {
}