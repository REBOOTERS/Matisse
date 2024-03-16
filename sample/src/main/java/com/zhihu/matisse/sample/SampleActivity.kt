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
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.engineer.ai.util.ImageLabelHelper
import com.permissionx.guolindev.PermissionX
import com.zhihu.matisse.Matisse
import com.zhihu.matisse.MimeType
import com.zhihu.matisse.engine.impl.GlideEngine
import com.zhihu.matisse.engine.impl.PicassoEngine
import com.zhihu.matisse.filter.Filter
import com.zhihu.matisse.internal.entity.CaptureStrategy


private const val TAG = "SampleActivity_TAG"

class SampleActivity : AppCompatActivity(), View.OnClickListener {

    private var mAdapter: UriAdapter? = null
    private lateinit var switcher: SwitchMaterial
    private var currentOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<View>(R.id.zhihu).setOnClickListener(this)
        findViewById<View>(R.id.dracula).setOnClickListener(this)
        findViewById<View>(R.id.material_design_3).setOnClickListener(this)
        findViewById<View>(R.id.only_gif).setOnClickListener(this)
        findViewById<View>(R.id.use_official).setOnClickListener(this)
        findViewById<View>(R.id.use_official_multi).setOnClickListener(this)
        val recyclerView = findViewById<View>(R.id.recyclerview) as RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = UriAdapter().also { mAdapter = it }
        switcher = findViewById(R.id.switcher)
        switcher.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switcher.text = "切竖屏"
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            } else {
                switcher.text = "切横屏"
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            }
            currentOrientation = requestedOrientation
        }
    }

    // <editor-fold defaultstate="collapsed" desc="onClick">
    @SuppressLint("CheckResult")
    override fun onClick(v: View) {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(Manifest.permission.READ_MEDIA_IMAGES, Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            listOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        PermissionX.init(this).permissions(permission).request { allGranted, _, _ ->
            if (allGranted) {
                ImageLabelHelper.init(this)
                startAction(v)
            } else {
                Toast.makeText(
                    this@SampleActivity, R.string.permission_request_denied, Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    // </editor-fold>

    private fun startAction(v: View) {
        var type = ZHIHU_THEME
        when (v.id) {
            R.id.zhihu -> type = ZHIHU_THEME
            R.id.dracula -> type = DRACULA_THEME
            R.id.material_design_3 -> type = MATERIAL_DESIGN_3_THEME
            R.id.only_gif -> type = ONLY_GIF
            R.id.use_official -> type = USE_OFFICIAL
            R.id.use_official_multi -> type = USE_OFFICIAL_MULTI
            else -> {}
        }
        mAdapter?.setData(null, null)
        if (type == USE_OFFICIAL) {
            if (isAndroidS()) {
                val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
                pickOfficialLauncher.launch(intent)
            } else {
                val msg = "only supported on Android 13 platform"
                Toast.makeText(this@SampleActivity, msg, Toast.LENGTH_LONG).show()
            }
            return
        } else if (type == USE_OFFICIAL_MULTI) {
            if (isAndroidS()) {
                val intent = Intent(MediaStore.ACTION_PICK_IMAGES)
                intent.putExtra(MediaStore.EXTRA_PICK_IMAGES_MAX, 10)
                pickOfficialMultiLauncher.launch(intent)
            } else {
                val msg = "only supported on Android 13 platform"
                Toast.makeText(this@SampleActivity, msg, Toast.LENGTH_LONG).show()
            }
            return
        }
        pickImageLauncher.launch(type)

    }

    private val pickOfficialLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val resultCode = it.resultCode
        val data = it.data
        if (resultCode == Activity.RESULT_OK) {
            val uri = data?.data
            val path = AndroidFileUtils.getFilePathByUri(this, uri)
            val pathList = arrayOf(path).toList()
            Log.e(TAG, "uri:  $uri")
            Log.e(TAG, "path: $path")
            val uriList = arrayOf(uri).toList()
            mAdapter?.setData(uriList, pathList)
        }
    }

    private val pickOfficialMultiLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val resultCode = it.resultCode
        val data = it.data
        if (resultCode == Activity.RESULT_OK) {
            Log.e(TAG, ": ${data?.clipData}")

            data?.clipData?.let { clip ->
                Log.e(TAG, "clipData $clip")
                val uriList = ArrayList<Uri>()
                val pathList = ArrayList<String>()
                for (i in 0 until clip.itemCount) {
                    Log.e(TAG, ": ${clip.getItemAt(i)}")
                    val uri = clip.getItemAt(i).uri
                    val path = AndroidFileUtils.getFilePathByUri(this, uri) ?: ""
                    Log.e(TAG, ": $path")
                    uriList.add(uri)
                    pathList.add(path)
                }
                mAdapter?.setData(uriList, pathList)
            }
        }
    }

    private val pickImageLauncher = registerForActivityResult(PickImageUriContract()) {
        it.let {
            if (it.first.isEmpty() || it.second.isEmpty()) {
                return@let
            }
            Log.d(TAG, "paths = ${it.first},urls = ${it.second}")

            it.second.forEach { uri ->
                ImageLabelHelper.getLabel(this, uri) {}
            }

            mAdapter?.setData(it.second, it.first)
        }
    }


    private inner class PickImageUriContract : ActivityResultContract<Any, Pair<List<String>, List<Uri>>>() {
        override fun createIntent(context: Context, input: Any): Intent {
            val intent: Intent?
            when (input) {
                DRACULA_THEME -> {
                    intent = Matisse.from(this@SampleActivity).choose(MimeType.ofImage()).theme(R.style.Matisse_Dracula)
                        .countable(false).restrictOrientation(currentOrientation)
                        .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K)).maxSelectable(9)
                        .originalEnable(true).maxOriginalSize(10).imageEngine(PicassoEngine()).createIntent()

                }

                MATERIAL_DESIGN_3_THEME -> {
                    intent = Matisse.from(this@SampleActivity).choose(MimeType.ofImage()).theme(R.style.Matisse_M3)
                        .restrictOrientation(currentOrientation).countable(false)
                        .addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K)).maxSelectable(9)
                        .autoHideToolbarOnSingleTap(true).originalEnable(true).maxOriginalSize(10)
                        .imageEngine(PicassoEngine()).createIntent()

                }

                ONLY_GIF -> {
                    intent = Matisse.from(this@SampleActivity).choose(MimeType.of(MimeType.GIF), false)

                        .restrictOrientation(currentOrientation).countable(false).theme(R.style.Matisse_Dracula)
                        .maxSelectable(1) //
                        // .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(
                            resources.getDimensionPixelSize(R.dimen.grid_expected_size)
                        ).restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT).thumbnailScale(0.85f)
                        .imageEngine(GlideEngine()).showSingleMediaType(true) //
                        // .originalEnable(true)
                        .maxOriginalSize(10).autoHideToolbarOnSingleTap(true).createIntent()
                }

                else -> {
                    intent = Matisse.from(this@SampleActivity).choose(MimeType.ofImage(), false)
                        .restrictOrientation(currentOrientation).countable(true).capture(true).captureStrategy(
                            CaptureStrategy(true, "com.zhihu.matisse.sample.fileprovider", "test")
                        ).maxSelectable(9).addFilter(GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(resources.getDimensionPixelSize(R.dimen.grid_expected_size))
                        .thumbnailScale(0.85f).imageEngine(GlideEngine())
                        .setOnSelectedListener { _: List<Uri?>?, pathList: List<String?> ->
                            Log.e(
                                "onSelected", "onSelected: pathList=$pathList"
                            )
                        }.showSingleMediaType(true).originalEnable(true).maxOriginalSize(10)
                        .autoHideToolbarOnSingleTap(true).setOnCheckedListener { isChecked: Boolean ->
                            Log.e(
                                "isChecked", "onCheck: isChecked=$isChecked"
                            )
                        }.createIntent()
                }
            }
            return intent!!
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Pair<List<String>, List<Uri>> {
            if (resultCode == Activity.RESULT_OK) {
                val paths = Matisse.obtainPathResult(intent)
                val uris = Matisse.obtainResult(intent)
                return Pair(paths, uris)
            }
            return Pair(emptyList(), emptyList())
        }
    }

    private fun isAndroidS(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
    }


    companion object {
        private const val ZHIHU_THEME = 1
        private const val DRACULA_THEME = ZHIHU_THEME + 1
        private const val MATERIAL_DESIGN_3_THEME = DRACULA_THEME + 1
        private const val ONLY_GIF = MATERIAL_DESIGN_3_THEME + 1
        private const val USE_OFFICIAL = ONLY_GIF + 1
        private const val USE_OFFICIAL_MULTI = USE_OFFICIAL + 1
    }
}