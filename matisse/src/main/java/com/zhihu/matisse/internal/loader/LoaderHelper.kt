package com.zhihu.matisse.internal.loader

import android.provider.MediaStore

/**
 * 支持加载非 gif 格式的图片
 */
object LoaderHelper {

    @JvmStatic
    fun getSelection(): String {
        val holder = loadNoAnimImagesHolder()
        return holder.first
    }

    @JvmStatic
    fun getSelectionArgs(): Array<String> {
        val holder = loadNoAnimImagesHolder()
        return holder.second
    }

    @JvmStatic
    private fun loadNoAnimImagesHolder(): Pair<String, Array<String>> {
        return buildSelectionAndArgs(listOf("image/png", "image/jpeg", "image/x-ms-bmp", "image/webp"))
    }

    private fun buildSelectionAndArgs(mimeTypes: List<String>): Pair<String, Array<String>> {
        val selection: String
        val selectionArgs: Array<String>

        if (mimeTypes.isEmpty()) {
            // 如果没有指定 MIME 类型，则返回一个始终为真的条件（例如大小大于0）
            selection = MediaStore.MediaColumns.SIZE + " > 0"
            selectionArgs = arrayOf()
        } else {
            // 构建 MIME 类型的条件
            val placeholders = mimeTypes.joinToString(", ") { "?" }
            selection =
                MediaStore.Images.Media.MIME_TYPE + " IN ($placeholders)" + " AND " + MediaStore.MediaColumns.SIZE + " > 0"
            selectionArgs = mimeTypes.toTypedArray()
        }

        return Pair(selection, selectionArgs)
    }
}