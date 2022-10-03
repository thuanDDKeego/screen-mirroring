package com.abc.mirroring.ui.browsermirror

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import kotlin.system.exitProcess

class CloseAppActivity: Activity() {
    companion object {
        fun start(context: Context) {
            val intent = Intent(context, CloseAppActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finishAffinity()
        exitProcess(0)
    }
}