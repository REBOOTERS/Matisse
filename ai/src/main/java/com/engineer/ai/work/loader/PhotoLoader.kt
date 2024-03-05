package com.engineer.ai.work.loader


import android.annotation.SuppressLint
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.loader.app.LoaderManager
import androidx.loader.content.Loader

class PhotoLoader(private val context: Context, private val callback: (ArrayList<Uri>) -> Unit) :
    LoaderManager.LoaderCallbacks<Cursor> {
    private val TAG = "PhotoLoader"

    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
        return AlbumMediaLoader.newInstance(context)
    }

    override fun onLoaderReset(loader: Loader<Cursor>) {

    }

    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
        val uriList = ArrayList<Uri>()
        if (data!!.moveToFirst()) {
            do {
                val uri = getUri(data)
                uriList.add(uri)
            } while (data.moveToNext())
        }
        callback(uriList)
        Log.d(TAG, "total = " + uriList.size)
    }


    @SuppressLint("Range")
    private fun getUri(cursor: Cursor): Uri {
        val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
        val contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        return ContentUris.withAppendedId(contentUri, id)
    }
}