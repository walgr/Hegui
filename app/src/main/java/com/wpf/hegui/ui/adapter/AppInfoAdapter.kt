package com.wpf.hegui.ui.adapter

import android.content.Context
import android.widget.*
import com.wpf.hegui.R
import java.util.ArrayList

/**
 * Created by 王朋飞 on 2021/8/26.
 *
 */
class AppInfoAdapter(context: Context, private var mData: List<Map<String, *>>?)
    : SimpleAdapter(context, mData, R.layout.item_appinfo_layout,
        arrayOf("icon", "name"), arrayOf(R.id.icon, R.id.name).toIntArray()) {

    private var mFilter: MyFilter? = null

    override fun getFilter(): Filter {
        if (mFilter == null) {
            mFilter = MyFilter()
        }
        return mFilter!!
    }

    inner class MyFilter : Filter() {
        private var mUnfilteredData: List<Map<String, *>>? = null

        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val results = FilterResults()
            if (mUnfilteredData == null) {
                mUnfilteredData = ArrayList(mData)
            }
            if (constraint == null || constraint.isEmpty()) {
                val list: List<Map<String, *>> = mUnfilteredData!!
                results.values = list
                results.count = list.size
            } else {
                val filterList = mUnfilteredData?.filter {
                    (it["name"] as? String)?.contains(constraint) ?: false
                            || (it["package"] as? String)?.contains(constraint) ?: false
                }
                results.values = filterList
                results.count = filterList?.size ?: 0
            }
            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
            mData = results?.values as? List<Map<String, *>>
            if (mData?.isNotEmpty() == true) {
                notifyDataSetChanged()
            } else {
                notifyDataSetInvalidated()
            }
        }

    }
}