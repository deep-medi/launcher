package com.deepmedi.dm_updater

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class LaunchServiceModel(
    val executionPath: String,
    val packageName: String,
    val bucketName: String,
    val currentVersion: String,
) : Parcelable