//
//  ActLoading
//
//  Created by 86139 on 2019-11-19 14:09:05
//  Copyright (c) 86139 All rights reserved.


/**
 *
 */

package com.ndtlg.driver


import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler

class ActLoading : Activity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getWindow().setNavigationBarColor(Color.BLUE); //写法一
        setContentView(R.layout.act_loading)
        loaddata()
    }


    fun loaddata() {
        Handler().postDelayed({
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }, 3000)
    }

}
