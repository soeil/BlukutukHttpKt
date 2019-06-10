@file:Suppress("unused")

package com.robinyonathan.blukutukhttp

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.internal.Primitives
import com.robinyonathan.blukutukhttp.coroutine.await
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.CertificatePinner
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okio.Okio
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.io.IOException
import java.lang.reflect.Type
import java.security.KeyManagementException
import java.security.NoSuchAlgorithmException
import java.security.cert.CertificateException
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

class BlukutukHttp {
    private var activity: Activity? = null

    private var blukutukFail: BlukutukFail? = null
    private var blukutukJsonObject: BlukutukJsonObject? = null
    private var blukutukJsonArray: BlukutukJsonArray? = null
    private var blukutukModel: BlukutukModel? = null
    private var blukutukUploadProgress: BlukutukUploadProgress? = null
    private var blukutukDownload: BlukutukDownload? = null

    private var model: Class<Any>? = null

    private var isAcceptAllCertificate = false
    private var isFragment = false

    private var downloadPath: File? = null

    private var responseCode = 200
    private var connectionTimeOut: Long = 10
    private var writeTimeOut: Long = 10
    private var readTimeOut: Long = 10
    private var bodyType = POST

    private var progressBar: ProgressBar? = null

//    private var progressDialog: ProgressDialog? = null

    private var requestBody: RequestBody? = null

    private var downloadFileName = ""
    private var paternCertificate = ""
    private var pinCertificate = ""
    private var responseMessage = ""
    private var url = ""
    private var fragment: Fragment? = null

    private var builder: Uri.Builder? = null

    companion object {
        const val POST = 1
        const val PUT = 2
        const val DELETE = 3
    }

    constructor(activity: Activity, builder: Uri.Builder, requestBody: RequestBody) {
        this.activity = activity
        this.builder = builder
        this.requestBody = requestBody
    }

    constructor(activity: Activity, builder: Uri.Builder, requestBody: RequestBody, bodyType: Int) {
        this.activity = activity
        this.builder = builder
        this.requestBody = requestBody
        this.bodyType = bodyType
    }

    constructor(activity: Activity, builder: Uri.Builder) {
        this.activity = activity
        this.builder = builder
        this.requestBody = null
    }

    constructor(activity: Activity, url: String, requestBody: RequestBody) {
        this.activity = activity
        this.url = url
        this.requestBody = requestBody
    }

    constructor(activity: Activity, url: String, requestBody: RequestBody, bodyType: Int) {
        this.activity = activity
        this.url = url
        this.requestBody = requestBody
        this.bodyType = bodyType
    }

    constructor(activity: Activity, url: String) {
        this.activity = activity
        this.url = url
        this.requestBody = null
    }

//    fun useProgressDialog(message: String) {
//        progressDialog = ProgressDialog(activity)
//        progressDialog?.setMessage(message)
//        progressDialog?.setCancelable(false)
//    }

//    fun setProgressDialog(progressDialog: ProgressDialog) {
//        this.progressDialog = progressDialog
//    }

    fun setProgressBar(progressBar: ProgressBar) {
        this.progressBar = progressBar
    }

    fun setModel(model: Class<Any>) {
        this.model = model
    }

    fun setAcceptAllCertificate(isAcceptAllCertificate: Boolean) {
        this.isAcceptAllCertificate = isAcceptAllCertificate
    }

    fun setCertificate(paternCertificate: String, pinCertificate: String) {
        this.paternCertificate = paternCertificate
        this.pinCertificate = pinCertificate
    }

    fun setDownloadPath(downloadPath: File) {
        this.downloadPath = downloadPath
    }

    fun setDownloadFileName(downloadFileName: String) {
        this.downloadFileName = downloadFileName
    }

    fun setFailedListener(blukutukFail: BlukutukFail) {
        this.blukutukFail = blukutukFail
    }

    fun setJsonObjectResultListener(blukutukJsonObject: BlukutukJsonObject) {
        this.blukutukJsonObject = blukutukJsonObject
    }

    fun setJsonArrayResultListener(blukutukJsonArray: BlukutukJsonArray) {
        this.blukutukJsonArray = blukutukJsonArray
    }

    fun setModelResultListener(blukutukModel: BlukutukModel) {
        this.blukutukModel = blukutukModel
    }

    fun setUploadProgressListener(blukutukUploadProgress: BlukutukUploadProgress) {
        this.blukutukUploadProgress = blukutukUploadProgress
    }

    fun setDownloadListener(blukutukDownload: BlukutukDownload) {
        this.blukutukDownload = blukutukDownload
    }

    fun setConnectionTimeOut(connectionTimeOut: Long, writeTimeOut: Long, readTimeOut: Long) {
        this.connectionTimeOut = connectionTimeOut
        this.writeTimeOut = writeTimeOut
        this.readTimeOut = readTimeOut
    }

    fun setIsFragment(isFragment: Boolean, fragment: Fragment) {
        this.isFragment = isFragment
        this.fragment = fragment
    }

    private fun processResult(o: Any) {
        val data = o as String

        if (data.isEmpty() && responseMessage.isEmpty()) {
            responseCode = 999
            responseMessage = code("" + responseCode)

            blukutukFail?.result(responseCode, responseMessage)

            return
        }

        if (responseMessage.isEmpty()) {
            if (blukutukJsonObject != null) {
                var failedJsonTest = false
                var jsonException = ""

                var result: JSONObject? = null
                try {
                    result = JSONObject(data)
                } catch (e: JSONException) {
                    jsonException = e.message ?: ""
                    failedJsonTest = true
                }

                if (failedJsonTest) {
                    responseCode = 999
                    responseMessage = code("" + responseCode) + ". " + jsonException

                    blukutukFail?.result(responseCode, responseMessage)
                } else {
                    result?.let { jsonObject ->
                        blukutukJsonObject?.result(jsonObject)
                    }
                }
            }

            if (blukutukJsonArray != null) {
                var failedJsonTest = false
                var jsonException = ""

                var result: JSONArray? = null
                try {
                    result = JSONArray(data)
                } catch (e: JSONException) {
                    jsonException = e.message ?: ""
                    failedJsonTest = true
                }

                if (failedJsonTest) {
                    responseCode = 999
                    responseMessage = code("" + responseCode) + ". " + jsonException

                    blukutukFail?.result(responseCode, responseMessage)
                } else {
                    result?.let { jsonArray ->
                        blukutukJsonArray?.result(jsonArray)
                    }
                }
            }

            if (blukutukModel != null && model != null) {
                val gson = Gson()
                val modelResult = gson.fromJson<Any>(data, model as Type)

                blukutukModel?.result(Primitives.wrap(model).cast(modelResult))
            }

        } else {
            if (blukutukFail != null) {
                if (responseMessage == "Exception") {
                    blukutukFail?.result(responseCode, code("" + responseCode))
                } else {
                    blukutukFail?.result(responseCode, responseMessage)
                }
            }
        }
    }

    fun jsonObjectTest(o: String): Boolean {
        var failedJsonTest = false

        try {
            JSONObject(o)
        } catch (e: JSONException) {
            try {
                JSONArray(o)
            } catch (e1: JSONException) {
                failedJsonTest = true
            }

        }

        return failedJsonTest
    }

    fun jsonArrayTest(o: String): Boolean {
        var failedJsonTest = false

        try {
            JSONArray(o)
        } catch (e: JSONException) {
            try {
                JSONArray(o)
            } catch (e1: JSONException) {
                failedJsonTest = true
            }

        }

        return failedJsonTest
    }

    fun execute() {
        var isInternetAvailable = true
        activity?.let { activity ->
            if (!Network.isNetworkAvailable(activity)) {
                blukutukFail?.result(900, code("900"))

                isInternetAvailable = false
            }
        }
        if (isInternetAvailable) {
            GlobalScope.launch(Dispatchers.IO) {
                okHttp(object : OkHttpInterface {
                    override fun before() {
                        if (checkView()) {
//                            progressDialog?.show()
                            progressBar?.visibility = View.VISIBLE
                        }
                    }

                    override fun progress(progress: Int) {
                        if (checkView()) {
                            activity?.let { activity ->
                                activity.runOnUiThread { blukutukUploadProgress?.result(progress) }
                            }
                        }
                    }

                    override fun status(code: Int, message: String) {
                        responseCode = code
                        responseMessage = message
                    }

                    override fun after(o: Any) {
                        var noProblem = true
                        if (isFragment) {
                            fragment?.let { temp ->
                                if (temp.isDetached) {
                                    noProblem = false
                                }
                                if (temp.isRemoving) {
                                    noProblem = false
                                }
                            }
                        }

                        if (activity == null) {
                            noProblem = false
                        } else {
                            activity?.let { activity ->
                                if (activity.isDestroyed) {
                                    noProblem = false
                                }

                            }
                        }

                        activity?.let { activity ->
                            if (!activity.isDestroyed || noProblem) {
                                processResult(o)

//                            progressDialog?.dismiss()

                                progressBar?.visibility = View.GONE

                            }
                        }
                    }
                })
            }
        }
    }

    fun download() {
        var isInternetAvailable = true
        activity?.let { activity ->
            if (!Network.isNetworkAvailable(activity)) {
                blukutukFail?.result(900, code("900"))

                isInternetAvailable = false
            }
        }
        if (isInternetAvailable) {
            GlobalScope.launch(Dispatchers.IO) {
                okHttpDownload(object : OkHttpInterface {
                    override fun before() {
                        if (checkView()) {
//                            progressDialog?.show()
                            progressBar?.visibility = View.VISIBLE
                        }
                    }

                    override fun progress(progress: Int) {
                        if (checkView()) {
                            activity?.let { activity ->
                                activity.runOnUiThread { blukutukUploadProgress?.result(progress) }
                            }
                        }
                    }

                    override fun status(code: Int, message: String) {
                        responseCode = code
                        responseMessage = message
                    }

                    override fun after(o: Any) {
                        if (checkView()) {
                            if (o == "1") {
                                blukutukDownload?.success()
                            } else {
                                blukutukDownload?.failed(responseCode, responseMessage)
                            }

                            progressBar?.visibility = View.GONE

                        }

                    }
                })
            }
        }
    }

    private fun checkView(): Boolean {
        var noProblem = true
        if (isFragment) {
            fragment?.let { temp ->
                if (temp.isDetached) {
                    noProblem = false
                }
                if (temp.isRemoving) {
                    noProblem = false
                }
            }
        }

        if (activity == null) {
            noProblem = false
        } else {
            activity?.let { activity ->
                if (activity.isDestroyed) {
                    noProblem = false
                }

            }
        }

        return noProblem
    }

    private suspend fun okHttp(okHttpInterface: OkHttpInterface) {
        okHttpInterface.before()

        var result = ""

        var error = false

        val builderOkhttp = OkHttpClient.Builder()
                .connectTimeout(connectionTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)

        if (paternCertificate.isNotEmpty() && pinCertificate.isNotEmpty()) {
            builderOkhttp.certificatePinner(CertificatePinner.Builder().add(paternCertificate, pinCertificate).build())
        }

        if (isAcceptAllCertificate) {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })
            try {
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())

                val sslSocketFactory = sslContext.socketFactory

                builderOkhttp.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            } catch (e: NoSuchAlgorithmException) {
                okHttpInterface.status(904, e.message)
            } catch (e: KeyManagementException) {
                okHttpInterface.status(905, e.message)
            }

            val client = builderOkhttp.build()

            var urlTemp = url

            if (builder != null) {
                urlTemp = builder.toString()
            }

            var request = Request.Builder()
                    .url(urlTemp)
                    .build()

            requestBody?.let { requestBody ->
                when (bodyType) {
                    POST -> {
                        request = Request.Builder()
                                .url(urlTemp)
                                .post(ProgressRequestBody(requestBody, object : ProgressRequestBody.Listener {
                                    override fun onProgress(progress: Int) {
                                        okHttpInterface.progress(progress)
                                    }
                                }))
                                .build()

                        requestBody.contentType()?.let { mediaType ->
                            request = Request.Builder()
                                    .url(urlTemp)
                                    .header("Content-Type", mediaType.toString())
                                    .post(ProgressRequestBody(requestBody, object : ProgressRequestBody.Listener {
                                        override fun onProgress(progress: Int) {
                                            okHttpInterface.progress(progress)
                                        }
                                    }))
                                    .build()
                        }
                    }
                    PUT -> {
                        request = Request.Builder()
                                .url(urlTemp)
                                .put(ProgressRequestBody(requestBody, object : ProgressRequestBody.Listener {
                                    override fun onProgress(progress: Int) {
                                        okHttpInterface.progress(progress)
                                    }
                                }))
                                .build()
                        requestBody.contentType()?.let { mediaType ->
                            request = Request.Builder()
                                    .url(urlTemp)
                                    .header("Content-Type", mediaType.toString())
                                    .put(ProgressRequestBody(requestBody, object : ProgressRequestBody.Listener {
                                        override fun onProgress(progress: Int) {
                                            okHttpInterface.progress(progress)
                                        }
                                    }))
                                    .build()
                        }
                    }
                    DELETE -> {
                        request = Request.Builder()
                                .url(urlTemp)
                                .delete(ProgressRequestBody(requestBody, object : ProgressRequestBody.Listener {
                                    override fun onProgress(progress: Int) {
                                        okHttpInterface.progress(progress)
                                    }
                                }))
                                .build()
                        requestBody.contentType()?.let { mediaType ->
                            request = Request.Builder()
                                    .url(urlTemp)
                                    .header("Content-Type", mediaType.toString())
                                    .delete(ProgressRequestBody(requestBody, object : ProgressRequestBody.Listener {
                                        override fun onProgress(progress: Int) {
                                            okHttpInterface.progress(progress)
                                        }
                                    }))
                                    .build()
                        }
                    }
                    else -> {
                        request = Request.Builder()
                                .url(urlTemp)
                                .post(ProgressRequestBody(requestBody, object : ProgressRequestBody.Listener {
                                    override fun onProgress(progress: Int) {
                                        okHttpInterface.progress(progress)
                                    }
                                }))
                                .build()
                    }
                }
            }

            try {
                val response = client.newCall(request).await(recordStack = false)

                val responseBody = response.body()

                if (!response.isSuccessful || response.code() != 200) {
                    error = true

                    okHttpInterface.status(response.code(), code("909"))
                } else if (responseBody != null) {
                    okHttpInterface.status(response.code(), "")

                    result = responseBody.string()
                } else {
                    error = true

                    okHttpInterface.status(response.code(), code("909"))
                }
            } catch (e: Exception) {
                if (e.message != null) {
                    error = true

                    okHttpInterface.status(900, e.message)
                } else {
                    error = true

                    okHttpInterface.status(900, code("909"))
                }
            }
        }

        if (!error) {
            okHttpInterface.after(result)
        }
    }

    private suspend fun okHttpDownload(okHttpInterface: OkHttpInterface) {
        okHttpInterface.before()

        var result = ""

        var error = false

        val builderOkhttp = OkHttpClient.Builder()
                .connectTimeout(connectionTimeOut, TimeUnit.SECONDS)
                .writeTimeout(writeTimeOut, TimeUnit.SECONDS)
                .readTimeout(readTimeOut, TimeUnit.SECONDS)

        if (paternCertificate.isNotEmpty() && pinCertificate.isNotEmpty()) {
            builderOkhttp.certificatePinner(CertificatePinner.Builder().add(paternCertificate, pinCertificate).build())
        }

        if (isAcceptAllCertificate) {
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @SuppressLint("TrustAllX509TrustManager")
                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf()
                }
            })
            try {
                val sslContext = SSLContext.getInstance("SSL")
                sslContext.init(null, trustAllCerts, java.security.SecureRandom())

                val sslSocketFactory = sslContext.socketFactory

                builderOkhttp.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            } catch (e: NoSuchAlgorithmException) {
                okHttpInterface.status(904, e.message)
            } catch (e: KeyManagementException) {
                okHttpInterface.status(905, e.message)
            }

            val client = builderOkhttp.build()

            var urlTemp = url

            if (builder != null) {
                urlTemp = builder.toString()
            }

            val request = Request.Builder()
                    .url(urlTemp)
                    .build()

            try {
                val response = client.newCall(request).await(recordStack = false)

                val responseBody = response.body()

                if (!response.isSuccessful || response.code() != 200) {
                    error = true

                    okHttpInterface.status(response.code(), code("909"))
                } else {
                    okHttpInterface.status(response.code(), "")

                    try {
                        val file = File(downloadPath, downloadFileName)
                        val sink = Okio.buffer(Okio.sink(file))
                        val bufferedSource = responseBody?.source()
                        if (bufferedSource != null) {
                            sink.writeAll(bufferedSource)
                            sink.close()

                            result = "1"
                        } else {
                            okHttpInterface.status(909, code("909"))
                        }
                    } catch (e: FileNotFoundException) {
                        var message = code("909")
                        e.message?.let { s ->
                            message = s
                        }

                        okHttpInterface.status(909, message)
                    } catch (e: IOException) {
                        var message = code("909")
                        e.message?.let { s ->
                            message = s
                        }
                        okHttpInterface.status(909, message)
                    }

                }
            } catch (e: Exception) {
                if (e.message != null) {
                    error = true

                    okHttpInterface.status(900, e.message)
                } else {
                    error = true

                    okHttpInterface.status(900, code("909"))
                }
            }
        }

        if (!error) {
            okHttpInterface.after(result)
        }
    }

    private fun code(code: String): String {
        var result = ""

        result += when (code) {
            "505" -> "HTTP Version Not Supported"
            "504" -> "Gateway Timeout"
            "503" -> "Service Unavailable"
            "502" -> "Bad Gateway"
            "501" -> "Not Implemented"
            "500" -> "Internal Server Error"
            "417" -> "Expectation Failed"
            "416" -> "Requested Range Not Satisfiable"
            "415" -> "Unsupported Media Type"
            "414" -> "Request-URI Too Long"
            "413" -> "Request Entity Too Large"
            "412" -> "Precondition Failed"
            "411" -> "Length Required"
            "410" -> "Gone"
            "409" -> "Conflict"
            "408" -> "Request Timeout"
            "407" -> "Proxy Authentication Required"
            "406" -> "Not Acceptable"
            "405" -> "Method Not Allowed"
            "404" -> "Not Found"
            "403" -> "Forbidden"
            "402" -> "Payment Required"
            "401" -> "Unauthorized"
            "400" -> "Bad Request"
            "900" -> "No Internet Connection Available"
            else -> "Unknown Connection Problem"
        }

        result += " ( $code )."

        return result
    }
}