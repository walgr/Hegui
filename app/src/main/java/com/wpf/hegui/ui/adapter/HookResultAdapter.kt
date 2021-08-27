package com.wpf.hegui.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wpf.hegui.R

/**
 * Created by 王朋飞 on 2021/6/16.
 *
 */
class HookResultAdapter(var data: MutableList<String>?) : RecyclerView.Adapter<HookResultAdapter.HookResultViewHolder>() {

    inner class HookResultViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var msg: TextView? = null
        init {
            itemView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            msg = itemView.findViewById(R.id.msg)
        }
    }

    fun setNewData(data: MutableList<String>?) {
        this.data = data
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HookResultViewHolder {
        return HookResultViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_hook_result_msg_layout, null))
    }

    override fun onBindViewHolder(holder: HookResultViewHolder, position: Int) {
        holder.msg?.text = data?.get(position)
    }

    override fun getItemCount(): Int {
        return data?.size ?: 0
    }
}