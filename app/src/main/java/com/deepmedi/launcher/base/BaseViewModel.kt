package com.deepmedi.launcher.base

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineExceptionHandler

abstract class BaseViewModel : ViewModel() {

    protected val coroutineExceptionHandler = CoroutineExceptionHandler { _, t ->
        t.printStackTrace()
    }

}