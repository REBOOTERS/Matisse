package com.engineer.ai.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.util.concurrent.Executors


/**
 * https://developers.google.com/ml-kit/vision/image-labeling/custom-models/android?hl=zh-cn
 */
object ImageLabelHelper {
    private const val TAG = "ImageLabelHelper"

    private var labeler: ImageLabeler? = null

    @Volatile
    private var categories = HashMap<String, ArrayList<Uri>>()


    fun init() {
//        val localModel = LocalModel.Builder().setAssetFilePath("model.tflite").build()
//        val customImageLabelerOptions =
//            CustomImageLabelerOptions.Builder(localModel).setConfidenceThreshold(0.5f).setMaxResultCount(5).build()
//        labeler = ImageLabeling.getClient(customImageLabelerOptions)

        initDefault()
    }

    fun initDefault() {
        labeler = ImageLabeling.getClient(ImageLabelerOptions.DEFAULT_OPTIONS)
    }

    fun doLabel(context: Context, uris: List<Uri>) {
        categories.clear()
        Log.i(TAG, uris.size.toString())

        val cachedThreadPool = Executors.newFixedThreadPool(4)
        var count = 0
        uris.forEach {
            val start = System.currentTimeMillis()
            cachedThreadPool.submit {
                doLabel(context, it) {
                    count++
//                    Log.i(TAG, count.toString())
                    if (count >= uris.size) {
                        Log.i(TAG, categories.keys.toString())
                        var sum = 0
                        categories.keys.forEach {
                            sum += (categories[it]?.size ?: 0)
                        }
                        Log.i(TAG, sum.toString())
                        Log.i(TAG, categories.values.size.toString())
                        Log.i(TAG, "cost time ${System.currentTimeMillis() - start}")
                    }
                }
            }
        }
    }

    fun doLabel(context: Context, uri: Uri, callback: () -> Unit) {
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
//                    Log.i(TAG, "uri = $uri")
//                    Log.i(TAG, "text=$text,confidence=$confidence,index=$index")

                    var list = categories[text]
                    if (list == null) {
                        list = ArrayList()
                        list.add(uri)
                    }
                    if (categories[text] == null) {
                        categories[text] = list
                    } else {
                        categories[text]?.add(uri)
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