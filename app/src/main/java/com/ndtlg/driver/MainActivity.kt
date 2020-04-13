package com.ndtlg.driver

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.core.app.ActivityCompat
import kotlinx.android.synthetic.main.activity_main.*


//15151963763

class MainActivity : Activity() {
    lateinit var chromeClient: PaxWebChromeClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE), 0);
        setContentView(R.layout.activity_main)
        chromeClient = PaxWebChromeClient(this, mProgressBar);
        mWebView.loadUrl("http://101.133.214.139/driver")
        mWebView.getSettings().setJavaScriptEnabled(true)
        mWebView.getSettings().setDomStorageEnabled(true)
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setDefaultTextEncodingName("UTF-8");
        mWebView.getSettings().setAllowContentAccess(true); // 是否可访问Content Provider的资源，默认值 true
        mWebView.getSettings().setAllowFileAccess(true);    // 是否可访问本地文件，默认值 true
        // 是否允许通过file url加载的Javascript读取本地文件，默认值 false
        mWebView.getSettings().setAllowFileAccessFromFileURLs(false);
        // 是否允许通过file url加载的Javascript读取全部资源(包括文件,http,https)，默认值 false
        mWebView.getSettings().setAllowUniversalAccessFromFileURLs(false);
        mWebView.setWebViewClient(object : WebViewClient() {
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean { // 重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                if (url.startsWith("tel:")) {
                    val intent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse(url)
                    )
                    startActivity(intent)
                    return true
                } else if (url.startsWith("bdapp:")) {
                    if (isAvilible(this@MainActivity, "com.baidu.BaiduMap")) {
                        val i1 = Intent()
                        i1.data = Uri.parse(url)
                        startActivity(i1)
                    } else {
                        Toast.makeText(this@MainActivity, "请先安装百度地图", 0).show()
                    }
                    return true
                } else if (url.startsWith("androidamap:")) {
                    if (isAvilible(this@MainActivity, "com.autonavi.minimap")) {
                        val i1 = Intent()
                        i1.data = Uri.parse(url)
                        startActivity(i1)
                    } else {
                        Toast.makeText(this@MainActivity, "请先安装高德地图", 0).show()
                    }
                    return true
                }
                mWebView.loadUrl(url)
                return true
            }
        })
        mWebView.webChromeClient = chromeClient
        mWebView.setOnKeyListener(View.OnKeyListener { v, keyCode, event ->
            if (event.action == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_BACK
                    && mWebView.canGoBack()
                ) { // 表示按返回键
                    mWebView.goBack() // 后退
                    return@OnKeyListener true // 已处理
                }
            }
            false
        })

        setAndroidNativeLightStatusBar(this, true)
    }

    /**
     * 检查手机上是否安装了指定的软件
     * @param context
     * @param packageName：应用包名
     * @return
     */
    private fun isAvilible(context: Context, packageName: String): Boolean { //获取packagemanager
        val packageManager: PackageManager = context.getPackageManager()
        //获取所有已安装程序的包信息
        val packageInfos = packageManager.getInstalledPackages(0)
        //用于存储所有已安装程序的包名
        val packageNames: MutableList<String> = ArrayList()
        //从pinfo中将包名字逐一取出，压入pName list中
        if (packageInfos != null) {
            for (i in packageInfos.indices) {
                val packName = packageInfos[i].packageName
                packageNames.add(packName)
            }
        }
        //判断packageNames中是否有目标程序的包名，有TRUE，没有FALSE
        return packageNames.contains(packageName)
    }

    private fun setAndroidNativeLightStatusBar(activity: Activity, dark: Boolean) {
        val decor = activity.window.decorView
        if (dark) {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        } else {
            decor.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        chromeClient.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }
}

