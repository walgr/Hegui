package com.wpf.hegui.ui

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
import com.wpf.hegui.ui.adapter.AppInfoAdapter
import com.wpf.hegui.ui.adapter.HookResultAdapter
import com.wpf.hegui.util.ACache
import com.wpf.hegui.util.AppPackageUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {

    var hookResultAdapterData: MutableList<String> = mutableListOf()
    var hookResultAdapter: HookResultAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var viewMap: List<Map<String, Any?>> = arrayListOf()
        launch {
            val appInfoList = AppPackageUtil.getAllPackage(this@MainActivity)
            viewMap = appInfoList.map {
                mapOf(
                        Pair("icon", it.appIcon),
                        Pair("name", it.appName),
                        Pair("version", it.appVersion),
                        Pair("package", it.appPackageName)
                )
            }
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

        object : CountDownTimer(Long.MAX_VALUE, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                if (AppApplication.hookResultData?.find { it.contains(AppApplication.hookAppName) } != null) {
                    state.text = "正在监听"
                }
                hookResultAdapter?.setNewData(AppApplication.hookResultData)
            }

            override fun onFinish() {

            }
        }.start()
    }

    private fun newSelect(packageName: String, appName: String) {
        if (packageName != AppApplication.hookPackageName) {
            AppApplication.hookPackageName = packageName
            AppApplication.hookAppName = appName
            state.text = "正在等待应用启动"
            ACache.get(this@MainActivity).put("hookPackageName", AppApplication.hookPackageName)
            Log.e("Xposed", "hook包名变成${AppApplication.hookPackageName}-应用名:${AppApplication.hookAppName}")

            AppApplication.hookResultData?.clear()
            hookResultAdapter?.setNewData(AppApplication.hookResultData)
        }
    }
}