package com.ndtlg.driver

import android.app.Application
import android.os.StrictMode
import android.os.StrictMode.VmPolicy


class Mapplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // android 7.0系统解决拍照的问题
        // android 7.0系统解决拍照的问题
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        builder.detectFileUriExposure()
    }

}
