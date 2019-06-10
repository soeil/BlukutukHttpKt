/*
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Robin Yonathan <robin@robinyonathan.com>, 2017.
 */

/*
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * Written by Robin Yonathan <robin@robinyonathan.com>, 2017.
 */

package com.robinyonathan.blukutukhttp


import okhttp3.MediaType
import okhttp3.RequestBody
import okio.*
import java.io.IOException

/**
 * Created by Robin Yonathan on 14/08/17.
 */

class ProgressRequestBody internal constructor(private val mDelegate: RequestBody, private val mListener: Listener) : RequestBody() {

    override fun contentType(): MediaType? {
        return mDelegate.contentType()
    }

    override fun contentLength(): Long {
        try {
            return mDelegate.contentLength()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return -1
    }

    @Throws(IOException::class)
    override fun writeTo(sink: BufferedSink) {
        val mCountingSink = CountingSink(sink)
        val bufferedSink = Okio.buffer(mCountingSink)
        mDelegate.writeTo(bufferedSink)
        bufferedSink.flush()
    }

    private inner class CountingSink internal constructor(delegate: Sink) : ForwardingSink(delegate) {
        private var bytesWritten: Long = 0

        @Throws(IOException::class)
        override fun write(source: Buffer, byteCount: Long) {
            super.write(source, byteCount)
            bytesWritten += byteCount
            mListener.onProgress((100f * bytesWritten / contentLength()).toInt())
        }
    }

    interface Listener {
        fun onProgress(progress: Int)
    }
}
