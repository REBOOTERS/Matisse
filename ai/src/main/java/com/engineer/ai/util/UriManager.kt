package com.engineer.ai.util

import android.content.Context
import android.net.Uri
import android.text.TextUtils

object UriManager {
    private const val TAG = "UriManager"
    fun updateUri(context: Context, labelList: List<Uri>) {
        saveLabels(context, labelList)
    }

    fun getLocalUri(context: Context): List<Uri> {
        return getLabels(context)
    }


    private const val LOCAL_URI = "local_uri"

    private const val LOCAL_URI_KEY = "local_uri_key"

    private fun saveLabels(context: Context, labelList: List<Uri>) {
        val sp = context.getSharedPreferences(LOCAL_URI, Context.MODE_PRIVATE)
        val value = JsonUtil.toJson(labelList)
        sp.edit().putString(LOCAL_URI_KEY, value).apply()
    }

    private fun getLabels(context: Context): List<Uri> {
        val sp = context.getSharedPreferences(LOCAL_URI, Context.MODE_PRIVATE)
        val value = sp.getString(LOCAL_URI_KEY, "")
        if (!TextUtils.isEmpty(value)) {
            return JsonUtil.toObjList(value!!, Uri::class.java)
        }
        return emptyList()
    }
}