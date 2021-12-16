package com.wpf.hegui.ui

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.*
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ImageView
import android.widget.SimpleAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.wpf.hegui.AppApplication
import com.wpf.hegui.R
import com.wpf.hegui.data.AppInfo
import com.wpf.hegui.ui.adapter.AppInfoAdapter
import com.wpf.hegui.ui.adapter.HookResultAdapter
import com.wpf.hegui.util.ACache
import com.wpf.hegui.util.AppPackageUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.async
import java.util.*
import android.graphics.Bitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    var hookResultAdapterData: MutableList<String> = mutableListOf()
    var hookResultAdapter: HookResultAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var viewMap: MutableList<Map<String, Any?>> = arrayListOf()
        launch {
            val appInfoList = withContext(Dispatchers.IO) { AppPackageUtil.getAllPackage(this@MainActivity) }
            viewMap = appInfoList.map {
                mapOf(
                        Pair("icon", it.appIcon),
                        Pair("name", it.appName),
                        Pair("version", it.appVersion),
                        Pair("package", it.appPackageName)
                )
            }.toMutableList()
            viewMap.add(0, mapOf(
                    Pair("icon", null),
                    Pair("name", ""),
                    Pair("version", ""),
                    Pair("package", "")
            ))
            selectPackage.adapter = (AppInfoAdapter(this@MainActivity, viewMap).also {
                it.viewBinder = SimpleAdapter.ViewBinder { view, data, _ ->
                    if (view is ImageView) {
                        view.setImageDrawable(data as? Drawable)
                    } else if (view is TextView) {
                        view.text = data as? String
                    }
                    return@ViewBinder true
                }
            })
            val find = viewMap.find { it["package"] == AppApplication.hookPackageName }
            AppApplication.hookAppName = find?.get("name") as? String ?: ""
            selectPackage.setSelection(viewMap.indexOf(find))
        }

        selectPackage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                newSelect(viewMap[position]["package"] as String,
                        viewMap[position]["name"] as? String ?: "")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                newSelect("", "")
            }
        }

        hookResultList.layoutManager = LinearLayoutManager(this)
        hookResultAdapter = HookResultAdapter(hookResultAdapterData)
        hookResultList.adapter = hookResultAdapter

        AppApplication.hookResultData.observeForever {
            if (AppApplication.hookResultData.value?.find { it.contains(AppApplication.hookAppName) } != null) {
                state.text = "正在监听"
            }
            hookResultAdapter?.setNewData(AppApplication.hookResultData.value)
        }
    }

    private fun newSelect(packageName: String, appName: String) {
        if (packageName != AppApplication.hookPackageName) {
            AppApplication.hookPackageName = packageName
            AppApplication.hookAppName = appName
            state.text = "正在等待"
            ACache.get(this@MainActivity).put("hookPackageName", AppApplication.hookPackageName)
            if (AppApplication.hookPackageName.isNotEmpty()) {
                Log.e(
                    "Xposed",
                    "hook应用:${AppApplication.hookAppName}-包名${AppApplication.hookPackageName}"
                )
            } else {
                Log.e("Xposed","已关闭hook")
                state.text = "已关闭hook"
            }
            AppApplication.hookResultData.value?.clear()
            hookResultAdapter?.setNewData(AppApplication.hookResultData.value)
        }
    }
}