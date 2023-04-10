package com.deepmedi.launcher

import android.app.*
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.os.Environment.getExternalStoragePublicDirectory
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.deepmedi.launcher.network.NetworkService
import com.deepmedi.launcher.network.RetrofitBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File

@AndroidEntryPoint
class LaunchService(private val newService: NetworkService = RetrofitBuilder.newServiceApi) : Service() {

    private var downloadId: Long = -1L
    private lateinit var downloadManager: DownloadManager

    private var downloadVersion = ""

    private val appOpenBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action.equals(Intent.ACTION_PACKAGE_ADDED)) {
                if (intent.dataString?.contains(getString(R.string.target_package_name)) == true) {
                    val process = Runtime.getRuntime().exec(getString(R.string.app_open_command))
                    process.waitFor()
                    PrefUtil.setVersion(downloadVersion)
                }
            }

        }
    }
    private val appUpdateBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE == intent.action) {
                if (downloadId == id) {
                    val query: DownloadManager.Query = DownloadManager.Query()
                    query.setFilterById(id)
                    val cursor = downloadManager.query(query)
                    if (!cursor.moveToFirst()) {
                        return
                    }
                    val columnIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS)
                    val status = cursor.getInt(columnIndex)
                    if (status == DownloadManager.STATUS_SUCCESSFUL) {
                        Toast.makeText(context, "Download succeeded", Toast.LENGTH_SHORT).show()
                        val installProcess = Runtime.getRuntime().exec(App.context().getString(R.string.install_command))
                        installProcess.waitFor()
                    } else if (status == DownloadManager.STATUS_FAILED) {
                        PrefUtil.setVersion("1.0.0")
                        Toast.makeText(context, "Download failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onCreate() {
        super.onCreate()
        downloadManager = getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

        val appDownloadIntentFilter = IntentFilter()
        appDownloadIntentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
        registerReceiver(appUpdateBroadcastReceiver, appDownloadIntentFilter)

        val appOpenIntentFilter = IntentFilter()
        appOpenIntentFilter.addAction(Intent.ACTION_PACKAGE_ADDED)
        appOpenIntentFilter.addDataScheme("package")
        registerReceiver(appOpenBroadcastReceiver, appOpenIntentFilter)

        createNotification()
        CoroutineScope(Dispatchers.IO).launch {
            while (true) {
                callApi()
                delay(60000)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    private fun callApi() {
        CoroutineScope(Dispatchers.IO).launch {
            val version = newService.getVersion().headers()["x-amz-meta-version"].toString()
            if (PrefUtil.getVersion() != version) {
                downloadVersion = version
                downloadImage()
            }
        }
    }

    private fun downloadImage() {
        val file = File(getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS), "app-debug.apk")
        if (file.isFile) {
            file.delete()
        }
        val request = DownloadManager.Request(Uri.parse(getString(R.string.download_url, downloadVersion)))
            .setTitle("Downloading a apk")
            .setDescription("Downloading a apk")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationUri(Uri.fromFile(file))
            .setRequiresCharging(false)
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        downloadId = downloadManager.enqueue(request)
    }

    private fun createNotification() {
        val builder = NotificationCompat.Builder(this, "default")
        builder.setSmallIcon(R.mipmap.ic_launcher)
        builder.setContentTitle("Update Launch")
        builder.setContentText("Update Launch")
        val notificationIntent = Intent(this, MainActivity::class.java)

        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, FLAG_IMMUTABLE)
        builder.setContentIntent(pendingIntent)

        val notificationManager = this.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(
                NotificationChannel(
                    "default",
                    "기본 채널",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
            )
        }
        notificationManager.notify(NOTI_ID, builder.build())
        val notification = builder.build()
        startForeground(NOTI_ID, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(appUpdateBroadcastReceiver)
        unregisterReceiver(appOpenBroadcastReceiver)
    }

    companion object {
        private const val NOTI_ID = 1
    }
}