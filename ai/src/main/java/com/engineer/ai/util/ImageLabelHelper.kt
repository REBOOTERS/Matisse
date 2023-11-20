package com.engineer.ai.util

import android.content.Context
import android.net.Uri
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.label.ImageLabeler
import com.google.mlkit.vision.label.ImageLabeling
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions
import java.io.IOException
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

        val cachedThreadPool = Executors.newCachedThreadPool()
        uris.forEach {
            val t = Runnable {

            }


            cachedThreadPool.submit {
                doLabel(context, it)
            }
        }
        Log.i(TAG, categories.keys.toString())
        var sum = 0
        categories.keys.forEach {
            sum += (categories[it]?.size ?: 0)
        }
        Log.i(TAG, sum.toString())
        Log.i(TAG, categories.values.size.toString())
    }

    fun doLabel(context: Context, uri: Uri) {


        var image: InputImage
        try {
            image = InputImage.fromFilePath(context, uri)
            labeler?.process(image)?.addOnSuccessListener { labels ->
                for (label in labels) {
                    val text = label.text
                    val confidence = label.confidence
                    val index = label.index
//                    Log.i(TAG, "uri = $uri")

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
                    break
//                    Log.i(TAG, "text=$text,confidence=$confidence,index=$index")
                }

            }?.addOnFailureListener { e ->
                Log.e(TAG, e.stackTraceToString())
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: UnsupportedOperationException) {
            e.printStackTrace()

        }

    }
}