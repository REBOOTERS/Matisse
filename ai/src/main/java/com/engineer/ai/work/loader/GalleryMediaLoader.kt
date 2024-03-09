/*
 * Copyright (C) 2014 nohana, Inc.
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an &quot;AS IS&quot; BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.engineer.ai.work.loader

import android.content.Context
import android.provider.MediaStore
import androidx.loader.content.CursorLoader

/**
 * Load images into a single cursor.
 */
class GalleryMediaLoader private constructor(context: Context, selection: String, selectionArgs: Array<String>) :
    CursorLoader(context, QUERY_URI, PROJECTION, selection, selectionArgs, ORDER_BY) {

    companion object {
        private val QUERY_URI = MediaStore.Files.getContentUri("external")
        private val PROJECTION = arrayOf(
            MediaStore.Files.FileColumns._ID, MediaStore.MediaColumns.MIME_TYPE, MediaStore.MediaColumns.SIZE
        )
        private const val SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?" + " AND " + MediaStore.MediaColumns.SIZE + ">0"

        private const val ORDER_BY = MediaStore.Images.Media.DATE_ADDED + " DESC"


        fun newInstance(context: Context): CursorLoader {
            val selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE
            val selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
            return GalleryMediaLoader(context, selection, selectionArgs)
        }
    }
}
