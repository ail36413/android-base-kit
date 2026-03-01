package com.ail.lib_network.http.util

import com.ail.lib_network.http.model.ProgressInfo
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import okio.ForwardingSource
import okio.buffer

/**
 * 封装 ResponseBody 并在读取时发射进度。用于下载场景。
 * onProgress 的 [ProgressInfo.done] 在读取到 EOF (bytesRead == -1) 时为 true。
 */
class ProgressResponseBody(
    private val responseBody: ResponseBody,
    private val onProgress: (ProgressInfo) -> Unit
) : ResponseBody() {

    private var bufferedSource: BufferedSource? = null

    override fun contentType(): MediaType? = responseBody.contentType()

    override fun contentLength(): Long = responseBody.contentLength()

    override fun source(): BufferedSource {
        if (bufferedSource == null) {
            bufferedSource = source(responseBody.source()).buffer()
        }
        return bufferedSource!!
    }

    private fun source(source: okio.Source): okio.Source {
        return object : ForwardingSource(source) {
            var totalBytesRead = 0L
            var seqCounter = 0L

            override fun read(sink: okio.Buffer, byteCount: Long): Long {
                val bytesRead = super.read(sink, byteCount)
                totalBytesRead += if (bytesRead != -1L) bytesRead else 0
                seqCounter++
                val totalSize = responseBody.contentLength()
                val progress = if (totalSize > 0) (100 * totalBytesRead / totalSize).toInt() else -1
                onProgress(ProgressInfo(progress, totalBytesRead, totalSize, bytesRead == -1L, seqCounter))
                return bytesRead
            }
        }
    }
}
