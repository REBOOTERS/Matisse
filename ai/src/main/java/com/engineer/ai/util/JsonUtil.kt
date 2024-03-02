package com.engineer.ai.util

import android.content.Context
import android.util.JsonReader
import android.util.Log
import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.JSONArray
import com.alibaba.fastjson.JSONObject
import java.io.BufferedReader
import java.lang.StringBuilder


object JsonUtil {
    const val TAG = "JsonUtil"

    fun toJson(any: Any): String {
        return JSON.toJSONString(any)
    }

    fun <T : Any> toObj(str: String, t: T): T {
        return JSONObject.parseObject(str, t::class.java)
    }

    fun <T> toObjList(str: String, t: Class<T>): List<T> {
        return JSONObject.parseArray(str, t)
    }

    fun readJsonStr(context: Context, filename: String): String {
        context.assets.open(filename).use { inputStream ->
            val sb = StringBuilder()
            var line: String?
            BufferedReader(inputStream.reader()).use { bf ->
                while (bf.readLine().also { line = it } != null) {
                    sb.append(line)
                }
            }
            return sb.toString()
        }
    }

    inline fun <reified T> mapToJsonObj(map: String): List<T>? {
        val mapObj = JSONObject.parseObject(map, Map::class.java)
        var list: List<T>? = null
        for (key in mapObj.keys) {
            // 这里的强转，就是人类意志的胜利
            val item = mapObj[key] as JSONArray
            list = convertJSONArrayToTypeList(item, T::class.java)
            Log.d(TAG, "parseSpecialJson() called key = $key, list = $list")
        }
        return list
    }

    fun <T> convertJSONArrayToTypeList(jsonArray: JSONArray, clazz: Class<T>): List<T> {
        if (jsonArray.isEmpty()) return emptyList()

        val result = ArrayList<T>(jsonArray.size)

        jsonArray.forEach {
            if (it is String || it is Boolean || it is Number) {
                val element = it as T
                result.add(element)
            } else {
                val t = JSONObject.toJavaObject(it as JSONObject, clazz)
                result.add(t)
            }
        }
        return result
    }

}