package com.abc.mirroring.cast.setup.startup

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.startup.Initializer
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import net.sofigo.cast.tv.R

class FCMOS: Initializer<Unit> {

    override fun create(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w("init FCM", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            // Log and toast
            val msg = token
            Log.d("init FCM", msg)
//            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
        })    }

    override fun dependencies(): MutableList<Class<out Initializer<*>>> = mutableListOf()
}