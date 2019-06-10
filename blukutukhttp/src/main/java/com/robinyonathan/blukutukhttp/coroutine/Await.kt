package com.robinyonathan.blukutukhttp.coroutine

import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * @return Result of request or throw exception
 */
suspend fun Call.await(recordStack: Boolean = isRecordStack): Response {
    val callStack = if (recordStack) {
        IOException().apply {
            stackTrace = stackTrace.copyOfRange(1, stackTrace.size)
        }
    } else {
        null
    }
    return suspendCancellableCoroutine { continuation ->
        enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                continuation.resume(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                if (continuation.isCancelled) {
                    return
                }
                callStack?.initCause(e)
                continuation.resumeWithException(callStack ?: e)
            }
        })

        continuation.invokeOnCancellation {
            try {
                cancel()
            } catch (ex: Throwable) {
            }
        }
    }
}