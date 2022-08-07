/*
 * Copyright 2017 Zhihu Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zhihu.matisse.sample

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.permissionx.guolindev.PermissionX
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.engine.impl.PicassoEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy

class SampleActivity : AppCompatActivity(), View.OnClickListener {

    private var mAdapter: UriAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.zhihu).setOnClickListener(this)
        findViewById<View>(R.id.dracula).setOnClickListener(this)
        findViewById<View>(R.id.only_gif).setOnClickListener(this)
        val recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = UriAdapter().also { mAdapter = it }
    }

    // <editor-fold defaultstate="collapsed" desc="onClick">
    @SuppressLint("CheckResult")
    override fun onClick(v: View) {
        PermissionX.init(this)
            .permissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            .request { allGranted, _, _ ->
                if (allGranted) {
                    startAction(v)
                } else {
                    Toast.makeText(
                        this@SampleActivity,
                        R.string.permission_request_denied,
                        Toast.LENGTH_LONG
                    )
                        .show()
                }
            }
    }

    // </editor-fold>

    private fun startAction(v: View) {
        var type = ZHIHU_THEME
        when (v.id) {
            R.id.zhihu -> type = ZHIHU_THEME
            R.id.dracula -> type = DRACULA_THEME
            R.id.only_gif -> type = ONLY_GIF
            else -> {}
        }
        pickImageLauncher.launch(type)
        mAdapter?.setData(null, null)
    }

    private val pickImageLauncher = registerForActivityResult(PickImageUriContract()) {
        it?.let {
            mAdapter?.setData(it.second, it.first)
        }
    }


    private inner class PickImageUriContract :
        ActivityResultContract<Any, Pair<List<String>, List<Uri>>>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            val intent: Intent?
            when (input) {
                DRACULA_THEME -> {
                    intent = Matisse.from(this@SampleActivity)
                        .choose(MimeType.ofImage())
                        .theme(R.style.Matisse_Dracula)
                        .countable(false)
                        .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .maxSelectable(9)
                        .originalEnable(true)
                        .maxOriginalSize(10)
                        .imageEngine(PicassoEngine())
                        .createIntent()

                }
                ONLY_GIF -> {
                    intent = Matisse.from(this@SampleActivity)
                        .choose(MimeType.of(MimeType.GIF), false)
                        .countable(false)
                        .theme(R.style.Matisse_Dracula)
                        .maxSelectable(1) //
                        // .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(
                            resources.getDimensionPixelSize(R.dimen.grid_expected_size)
                        )
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.85f)
                        .imageEngine(GlideEngine())
                        .showSingleMediaType(true) //
                        // .originalEnable(true)
                        .maxOriginalSize(10)
                        .autoHideToolbarOnSingleTap(true)
                        .createIntent()
                }
                else -> {
                    intent = Matisse.from(this@SampleActivity)
                        .choose(MimeType.ofImage(), false)
                        .countable(true)
                        .capture(true)
                        .captureStrategy(
                            CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test")
                        )
                        .maxSelectable(9)
                        .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(
                            resources.getDimensionPixelSize(R.dimen.grid_expected_size)
                        )
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.85f)
                        .imageEngine(GlideEngine())
                        .setOnSelectedListener { uriList: List<Uri?>?, pathList: List<String?> ->
                            Log.e(
                                "onSelected",
                                "onSelected: pathList=$pathList"
                            )
                        }
                        .showSingleMediaType(true)
                        .originalEnable(true)
                        .maxOriginalSize(10)
                        .autoHideToolbarOnSingleTap(true)
                        .setOnCheckedListener { isChecked: Boolean ->
                            Log.e(
                                "isChecked",
                                "onCheck: isChecked=$isChecked"
                            )
                        }
                        .createIntent()
                }
            }
            return intent!!
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Pair<List<String>, List<Uri>>? {
            if (resultCode == Activity.RESULT_OK) {
                val paths = Matisse.obtainPathResult(intent)
                val uris = Matisse.obtainResult(intent)
                return Pair(paths, uris)
            }
            return null
        }
    }


    companion object {
        private const val ZHIHU_THEME = 1
        private const val DRACULA_THEME = ZHIHU_THEME + 1
        private const val ONLY_GIF = DRACULA_THEME + 1
    }
}