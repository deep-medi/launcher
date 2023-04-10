package com.deepmedi.launcher

import android.content.Intent
import android.os.Build
import androidx.lifecycle.lifecycleScope
import com.deepmedi.launcher.base.BaseActivity
import com.deepmedi.launcher.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : BaseActivity<ActivityMainBinding>({ ActivityMainBinding.inflate(it) }) {
    override fun initViews() {
        lifecycleScope.launch {
            val serviceIntent = Intent(this@MainActivity, LaunchService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(serviceIntent)
            } else {
                startService(serviceIntent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopService(Intent(this@MainActivity, LaunchService::class.java))
    }
}