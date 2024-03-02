package com.engineer.ai.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.alibaba.fastjson.JSONObject
import com.engineer.ai.model.Labels
import com.google.mlkit.common.model.LocalModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.custom.CustomImageLabelerOptions
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.Executors
import kotlin.concurrent.Volatile


/**
 *
 *
 * @see [image-labeling](https://developers.google.com/ml-kit/vision/image-labeling/custom-models/android?hl=zh-cn)
 */
object ImageLabelHelper {
    private const val TAG = "ImageLabelHelper"

    private var labeler: ImageLabeler? = null

    private var categories = HashMap<Int, ArrayList<Uri>>()

    @Volatile
    private var isRunning = false

    private var labelList = ArrayList<Labels>()


    fun getLabelList() = labelList

    fun init(context: Context) {
        initDefault()
//        initCustomLabeler()
        val localLabelList = LabelManager.getLabelCategories(context)
        if (localLabelList.isEmpty().not()) {
            labelList.clear()
            labelList.addAll(localLabelList)
        }
    }

    // 1.tflite https://www.kaggle.com/models/google/mobilenet-v3/frameworks/tfLite/variations/large-075-224-classification-metadata
    // 2.tflite https://www.kaggle.com/models/tensorflow/efficientnet/frameworks/tfLite/variations/lite4-fp32
    private fun initCustomLabeler() {
        val localModel = LocalModel.Builder().setAssetFilePath("1.tflite").build()
        val customImageLabelerOptions =
            CustomImageLabelerOptions.Builder(localModel).setConfidenceThreshold(0.1f)
                .setMaxResultCount(15).build()
        labeler = ImageLabeling.getClient(customImageLabelerOptions)
    }

    private fun initDefault() {
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    }

    fun getLabel(context: Context, uris: List<Uri>, callback: (() -> Unit)? = null) {

        if (labelList.size > 0) {
            callback?.invoke()
            return
        }

        if (isRunning) {
            Log.i(TAG, "last task is running ,ignore")
            return
        }
        isRunning = true

        categories.clear()
        Log.i(TAG, uris.size.toString())


        val cachedThreadPool = Executors.newFixedThreadPool(4)
        var count = 0
        val start = System.currentTimeMillis()
        uris.forEach {
            cachedThreadPool.submit {
                getLabel(context, it) {
                    count++
//                    Log.i(TAG, count.toString())
                    if (count >= uris.size) {
                        convertToLabels(context)
                        callback?.invoke()
                        isRunning = false
                        Log.d(
                            TAG,
                            "total cost ${(System.currentTimeMillis() - start) / 1000f} seconds on ${
                                uris.size
                            } picture"
                        )
                    }
                }
            }
        }
    }

    private fun convertToLabels(context: Context) {
        val result = ArrayList<Labels>(categories.keys.size)
        val tags = JsonUtil.readJsonStr(context, "tags.json")
        val tagsMap = JSONObject.parseObject(tags)
        categories.forEach {
            val key = it.key
            val list = it.value
            val realKey: String = tagsMap.getString(key.toString())
            val labels = Labels(realKey, list)
            result.add(labels)
        }
        labelList.clear()
        labelList.addAll(result)
        Log.e(TAG, result.toString())
        LabelManager.updateLabelCategory(context, labelList)
    }

    fun getLabel(context: Context, uri: Uri, callback: () -> Unit) {
        val image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)
            labeler?.process(image)?.addOnSuccessListener { labels ->
                if (labels == null || labels.size == 0) {
                    Log.e(TAG, "null label")
                    callback()
                }
                for (label in labels) {
                    val text = label.text
                    val confidence = label.confidence
                    val index = label.index
                    val string = String.format(
                        "text=%-10s,confidence=%-10f,index=%-3d ,uri=%s",
                        text,
                        confidence,
                        index,
                        uri
                    )
                    Log.i(TAG, string)

                    var list = categories[index]
                    if (list == null) {
                        list = ArrayList()
                        list.add(uri)
                    }
                    if (categories[index] == null) {
                        categories[index] = list
                    } else {
                        categories[index]?.add(uri)
                    }
                    callback()
                    break
                }

            }?.addOnFailureListener { e ->
                callback()
                Log.e(TAG, e.stackTraceToString())
            }

        } catch (e: Exception) {
            callback()
            e.printStackTrace()
        }
    }
}