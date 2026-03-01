package com.ail.lib_network.http.util

import com.ail.lib_network.http.model.ProgressInfo
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.ForwardingSink
import okio.buffer

/**
 * 封装 RequestBody 并提供进度回调的实现。用于上传时监控已写入字节数。
 * @param delegate 实际的 RequestBody（例如 file.asRequestBody）
 * @param onProgress 每次写入后会回调最新的 [ProgressInfo]
 */
class ProgressRequestBody(
    private val delegate: RequestBody,
    private val onProgress: (ProgressInfo) -> Unit
) : RequestBody() {

    override fun contentType(): MediaType? = delegate.contentType()

    override fun contentLength(): Long = delegate.contentLength()

    override fun writeTo(sink: BufferedSink) {
        val countingSink = object : ForwardingSink(sink) {
            private var bytesWritten = 0L
            private val totalBytes = contentLength()
            private var seqCounter = 0L

            override fun write(source: okio.Buffer, byteCount: Long) {
                super.write(source, byteCount)
                bytesWritten += byteCount
                seqCounter++
                val progress = if (totalBytes > 0) (100 * bytesWritten / totalBytes).toInt() else -1
                onProgress(ProgressInfo(progress, bytesWritten, totalBytes, bytesWritten == totalBytes, seqCounter))
            }
        }
        val bufferedSink = countingSink.buffer()
        delegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }
}
