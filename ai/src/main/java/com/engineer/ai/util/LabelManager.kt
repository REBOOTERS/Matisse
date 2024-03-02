package com.engineer.ai.util

import android.content.Context
import android.text.TextUtils
import android.util.Log
import com.engineer.ai.model.Labels
import java.io.File

object LabelManager {
    private const val TAG = "LabelManager"
    fun updateLabelCategory(context: Context, labelList: ArrayList<Labels>) {
        saveLabels(context, labelList)
    }

    fun getLabelCategories(context: Context): List<Labels> {
        return getLabels(context)
    }


    private const val LABEL_CATEGORIES_FILE = "label_categories"

    private const val LABEL_KEY = "label_key"

    private fun saveLabels(context: Context, labelList: ArrayList<Labels>) {
        val sp = context.getSharedPreferences(LABEL_CATEGORIES_FILE, Context.MODE_PRIVATE)
        val value = JsonUtil.toJson(labelList)
        sp.edit().putString(LABEL_KEY, value).apply()
    }

    private fun getLabels(context: Context): List<Labels> {
        val sp = context.getSharedPreferences(LABEL_CATEGORIES_FILE, Context.MODE_PRIVATE)
        val value = sp.getString(LABEL_KEY, "")
        if (!TextUtils.isEmpty(value)) {
            val tempList = JsonUtil.toObjList(value!!, Labels::class.java)
            tempList.forEach {
                val labelSubList = it.subs
                val iterator = labelSubList.iterator()
                while (iterator.hasNext()) {
                    val uri = iterator.next()
                    val filePath = FileUtils.getFilePathByUri(context, uri)
                    Log.i(TAG, "filePath = $filePath, $uri")
                    if (filePath != null) {
                        val file = File(filePath)
                        if (file.exists().not()) {
                            Log.i(TAG, "$uri,$filePath not exist,removed")
                            iterator.remove()
                        }
                    } else {

                    }

                }
                it.subs = labelSubList
            }
            return tempList
        }
        return emptyList()
    }
}