package com.deepmedi.dm_updater

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*

class LaunchService :
    Service() {

    private val appUpdater = AppUpdater.getInstance()

    private var launchServiceModel: LaunchServiceModel? = null

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()

        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                launchServiceModel?.let {
                    try {
                        appUpdater.checkVersion(context = this@LaunchService, bucketName = it.bucketName, currentVersion = it.currentVersion)
                    } catch (e: Exception) {
                        Log.e("appUpdater", e.message.toString())
                    }
                    delay(VERSION_CHECK_DELAY)
                }
            }
        }
        createNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.getParcelableExtra<LaunchServiceModel>(LAUNCH_SERVICE_MODEL)?.let { launchServiceModel = it }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun createNotification() {
        val builder = NotificationCompat.Builder(this, "default").apply {
            setSmallIcon(R.drawable.logo_deep_medi)
            setContentTitle(NOTIFICATION_TITLE)
            setContentText(NOTIFICATION_TEXT)
        }


        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT))
        }
        notificationManager.notify(NOTIFICATION_ID, builder.build())
        val notification = builder.build()
        startForeground(NOTIFICATION_ID, notification)
    }

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val NOTIFICATION_TITLE = "Deep-medi"
        private const val NOTIFICATION_TEXT = "running"

        private const val VERSION_CHECK_DELAY = 600000L

        const val LAUNCH_SERVICE_MODEL = "launchServiceModel"
    }
}