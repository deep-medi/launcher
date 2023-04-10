package com.deepmedi.dm_updater

import android.content.Context
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
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

    fun checkVersion(context: Context, bucketName: String, currentVersion: String) {
        val getObjectRequest = GetObjectRequest(bucketName, "latest_version.txt")
        val objectResponse = amazonS3Client.getObject(getObjectRequest)

        val version = objectResponse.objectMetadata.userMetadata["version"].toString()
        if (currentVersion != version) {
            downloadApk(context, bucketName, version)
        }
    }

    private fun downloadApk(context: Context, bucketName: String, downloadVersion: String) {

        val apkName = "app-debug.apk"

        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), apkName)
        if (file.isFile) {
            file.delete()
        }
        val transferUtility = TransferUtility.builder()
            .context(context)
            .s3Client(amazonS3Client)
            .build()

        val downloadObserver = transferUtility.download(bucketName, "${downloadVersion}/${apkName}", file)

        // 객체 다운로드 상태를 감시하기 위한 리스너 등록
        downloadObserver.setTransferListener(object : TransferListener {
            override fun onStateChanged(id: Int, state: TransferState) {
                // 객체 다운로드 상태 변경 시 호출되는 콜백
                if (state == TransferState.COMPLETED) {
                    val installProcess = Runtime.getRuntime().exec("su root pm install -r /sdcard/Download/app-debug.apk")
                    installProcess.waitFor()
                } else if (state == TransferState.FAILED || state == TransferState.CANCELED) {
                }
            }

            override fun onProgressChanged(id: Int, bytesCurrent: Long, bytesTotal: Long) {}
            override fun onError(id: Int, ex: Exception) {
                CoroutineScope(Dispatchers.IO).launch {
                    delay(6000L)
                    downloadApk(context, bucketName, downloadVersion)
                }
            }
        })
    }

    companion object {
        const val ACCESS_KEY = "PbDvaXxkTaHf19QGViU1"
        const val SECRET_KEY = "HOAg4vr7bjzHr4OvMeAvw70Ae8nNKa6ctudDJuJy"
        const val ENDPOINT = "https://kr.object.ncloudstorage.com"

        const val BUCKET_NAME_FACE_HEALTH_FITNESS_RELEASES = "face-health-fitness-releases"
        const val BUCKET_NAME_FACE_HEALTH_RELEASES = "face-health-releases"

        fun getInstance() = AppUpdater()
    }
}