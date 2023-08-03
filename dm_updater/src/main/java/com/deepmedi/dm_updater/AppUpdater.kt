package com.deepmedi.dm_updater

import android.content.Context
import android.os.Environment
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.GetObjectRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

class AppUpdater {

    private val amazonS3Client: AmazonS3Client by lazy {
        AmazonS3Client(BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)).apply {
            endpoint = ENDPOINT
        }
    }
    private var downloadTryIndex = 0

    private var downloadObserver: TransferObserver? = null
    fun checkVersion(context: Context, bucketName: String, currentVersion: String) {
        val getObjectRequest = GetObjectRequest(bucketName, AWS_OBJECT_KEY)
        val objectResponse = amazonS3Client.getObject(getObjectRequest)
        val version = objectResponse.objectMetadata.userMetadata[AWS_OBJECT_METADATA_KEY].toString()

        if (currentVersion != version) {
            downloadTryIndex = 0
            downloadApk(context, bucketName, version)
        }
    }

    private fun downloadApk(context: Context, bucketName: String, downloadVersion: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), APK_NAME)
        if (file.isFile) file.delete()

        val transferUtility = TransferUtility.builder()
            .context(context)
            .s3Client(amazonS3Client)
            .build()

        downloadObserver = transferUtility.download(bucketName, "${downloadVersion}/${APK_NAME}", file)

        downloadObserver?.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                if (state == TransferState.COMPLETED) {
                    downloadTryIndex = 0
                    Runtime.getRuntime().exec(APK_UPDATE_TERMINAL_COMMAND).waitFor()
                } else if (state == TransferState.FAILED || state == TransferState.CANCELED) {
                    retryDownload(context, bucketName, downloadVersion)
                }
            }

            override fun onError(id: Int, ex: Exception) {
                retryDownload(context, bucketName, downloadVersion)
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}
        })
    }

    private fun retryDownload(context: Context, bucketName: String, downloadVersion: String) {
        CoroutineScope(Dispatchers.IO).launch {
            downloadObserver?.cleanTransferListener()
            if (downloadTryIndex < DOWNLOAD_RETRY_MAX_INDEX) {
                delay(DOWNLOAD_RETRY_DELAY)
                downloadApk(context, bucketName, downloadVersion)
                downloadTryIndex++
            }
        }
    }

    companion object {
        private const val DOWNLOAD_RETRY_MAX_INDEX = 6
        private const val DOWNLOAD_RETRY_DELAY = 36000000L

        private const val ACCESS_KEY = "PbDvaXxkTaHf19QGViU1"
        private const val SECRET_KEY = "HOAg4vr7bjzHr4OvMeAvw70Ae8nNKa6ctudDJuJy"
        private const val ENDPOINT = "https://kr.object.ncloudstorage.com"

        private const val AWS_OBJECT_KEY = "latest_version.txt"
        private const val AWS_OBJECT_METADATA_KEY = "version"

        private const val APK_NAME = "app-debug.apk"
        private const val APK_UPDATE_TERMINAL_COMMAND = "su root pm install -r /sdcard/Download/app-debug.apk"

        const val BUCKET_NAME_FACE_HEALTH_FITNESS_RELEASES = "face-health-fitness-releases"
        const val BUCKET_NAME_FACE_HEALTH_RELEASES = "face-health-releases"

        fun getInstance() = AppUpdater()
    }
}