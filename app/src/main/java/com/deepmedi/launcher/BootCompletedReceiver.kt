package com.deepmedi.launcher

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            Intent.ACTION_BOOT_COMPLETED -> {
                CoroutineScope(Dispatchers.IO).launch {
                    delay(6000L)
                    val bootCompleteIntent = Intent(context, MainActivity::class.java)
                    bootCompleteIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(bootCompleteIntent)
                }
            }
        }
    }
}