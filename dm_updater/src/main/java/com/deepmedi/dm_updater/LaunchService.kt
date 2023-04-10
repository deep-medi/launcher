package com.deepmedi.dm_updater

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
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
                if (launchServiceModel != null) {
                    appUpdater.checkVersion(
                        context = this@LaunchService,
                        bucketName = launchServiceModel!!.bucketName,
                        currentVersion = launchServiceModel!!.currentVersion
                    )
                    delay(60000)
                }
            }
        }
        createNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
            intent?.getParcelableExtra<LaunchServiceModel>(LAUNCH_SERVICE_MODEL)?.let {
                launchServiceModel = it
            }
        return super.onStartCommand(intent, flags, startId)
    }


    private fun createNotification() {
        val builder = NotificationCompat.Builder(this, "default")
        builder.setSmallIcon(R.drawable.ic_launcher_background)
        builder.setContentTitle("Update Launch")
        builder.setContentText("Update Launch")

        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(NotificationChannel("default", "기본 채널", NotificationManager.IMPORTANCE_DEFAULT))
        }
        notificationManager.notify(NOTI_ID, builder.build())
        val notification = builder.build()
        startForeground(NOTI_ID, notification)
    }

    companion object {
        private const val NOTI_ID = 1
        const val LAUNCH_SERVICE_MODEL = "launchServiceModel"
    }
}