package com.deepmedi.launcher

import android.content.Context
import android.content.SharedPreferences

object PrefUtil {
    private const val DEF_PREF_NAME = "Launch"
    private const val VERSION = "version"
    private fun getPref(context: Context, name: String): SharedPreferences {
        return context.getSharedPreferences(name, Context.MODE_PRIVATE or 0x4)
    }

    private fun setStringValue(context: Context, name: String, value: String) {
        val pref = getPref(
            context,
            DEF_PREF_NAME
        )
        val editor = pref.edit()
        editor.putString(name, value)
        editor.apply()
    }
    private fun getStringValue(context: Context, name: String, defValue: String): String {
        val pref = getPref(
            context,
            DEF_PREF_NAME
        )
        return pref.getString(name, defValue).toString()
    }
    fun setVersion(version: String) {
        setStringValue(App.context(), VERSION, version)
    }

    fun getVersion() = getStringValue(App.context(), VERSION, "1.0.0")
}
