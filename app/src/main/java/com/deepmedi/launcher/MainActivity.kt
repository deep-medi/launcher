package com.deepmedi.launcher

import android.content.Intent
import android.os.Build
import androidx.lifecycle.lifecycleScope
import com.deepmedi.dm_updater.AppUpdater.Companion.BUCKET_NAME_FACE_HEALTH_FITNESS_RELEASES
import com.deepmedi.dm_updater.LaunchService
import com.deepmedi.dm_updater.LaunchService.Companion.LAUNCH_SERVICE_MODEL
import com.deepmedi.dm_updater.LaunchServiceModel
import com.deepmedi.launcher.base.BaseActivity
import com.deepmedi.launcher.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) {

    override fun initViews() {
        startLaunchService()
    }

    private fun startLaunchService() {
        lifecycleScope.launch {
            val serviceIntent = Intent(this@MainActivity, LaunchService::class.java)
            serviceIntent.putExtra(
                LAUNCH_SERVICE_MODEL, LaunchServiceModel(
                    executionPath = "${packageName}/MainActivity",
                    packageName = packageName,
                    bucketName = BUCKET_NAME_FACE_HEALTH_FITNESS_RELEASES,
                    currentVersion = BuildConfig.VERSION_NAME
                )
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}