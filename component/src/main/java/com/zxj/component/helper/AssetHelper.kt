package com.zxj.component.helper

import android.content.Context
import okio.buffer
import okio.sink
import okio.source
import java.io.File

fun Context.assetToCacheFile(assetName: String, fileName: String): File {
    val file = File(cacheDir, fileName)
    if (file.exists()) return file

    assets.open(assetName).source().use { source ->
        file.sink(false).buffer().use {
            it.writeAll(source)
        }
    }
    return file
}