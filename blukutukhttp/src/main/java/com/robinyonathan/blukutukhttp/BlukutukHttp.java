package com.robinyonathan.blukutukhttp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.View;
import android.widget.ProgressBar;

import androidx.fragment.app.Fragment;

import com.google.gson.Gson;
import com.google.gson.internal.Primitives;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.CertificatePinner;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;

public class BlukutukHttp {
    private Activity activity;

    private BlukutukFail blukutukFail;
    private BlukutukJsonObject blukutukJsonObject;
    private BlukutukJsonArray blukutukJsonArray;
    private BlukutukModel blukutukModel;
    private BlukutukUploadProgress blukutukUploadProgress;
    private BlukutukDownload blukutukDownload;

    private Class<Object> model;

    private boolean isAcceptAllCertificate = false;
    private boolean isFragment = false;

    private File downloadPath;

    public final static int POST = 1;
    public final static int PUT = 2;

    private int responseCode = 200;
    private int connectionTimeOut = 10;
    private int writeTimeOut = 10;
    private int readTimeOut = 10;
    private int bodyType = POST;

    private ProgressBar progressBar;

    private ProgressDialog progressDialog;

    private RequestBody requestBody;

    private String downloadFileName = "";
    private String paternCertificate = "";
    private String pinCertificate = "";
    private String responseMessage = "";
    private String url = "";
    private Fragment fragment = null;

    private Uri.Builder builder;

    public BlukutukHttp(Activity activity, Uri.Builder builder, RequestBody requestBody) {
        this.activity = activity;
        this.builder = builder;
        this.requestBody = requestBody;
    }

    public BlukutukHttp(Activity activity, Uri.Builder builder, RequestBody requestBody, int bodyType) {
        this.activity = activity;
        this.builder = builder;
        this.requestBody = requestBody;
        this.bodyType = bodyType;
    }

    public BlukutukHttp(Activity activity, Uri.Builder builder) {
        this.activity = activity;
        this.builder = builder;
        this.requestBody = null;
    }

    public BlukutukHttp(Activity activity, String url, RequestBody requestBody) {
        this.activity = activity;
        this.url = url;
        this.requestBody = requestBody;
    }

    public BlukutukHttp(Activity activity, String url, RequestBody requestBody, int bodyType) {
        this.activity = activity;
        this.url = url;
        this.requestBody = requestBody;
        this.bodyType = bodyType;
    }

    public BlukutukHttp(Activity activity, String url) {
        this.activity = activity;
        this.url = url;
        this.requestBody = null;
    }

    public void useProgressDialog(String message) {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
    }

    public void setProgressDialog(ProgressDialog progressDialog) {
        this.progressDialog = progressDialog;
    }

    public void setProgressBar(ProgressBar progressBar) {
        this.progressBar = progressBar;
    }

    public void setModel(Class model) {
        this.model = model;
    }

    public void setAcceptAllCertificate(boolean isAcceptAllCertificate) {
        this.isAcceptAllCertificate = isAcceptAllCertificate;
    }

    public void setCertificate(String paternCertificate, String pinCertificate) {
        this.paternCertificate = paternCertificate;
        this.pinCertificate = pinCertificate;
    }

    public void setDownloadPath(File downloadPath) {
        this.downloadPath = downloadPath;
    }

    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }

    public void setFailedListener(BlukutukFail blukutukFail) {
        this.blukutukFail = blukutukFail;
    }

    public void setJsonObjectResultListener(BlukutukJsonObject blukutukJsonObject) {
        this.blukutukJsonObject = blukutukJsonObject;
    }

    public void setJsonArrayResultListener(BlukutukJsonArray blukutukJsonArray) {
        this.blukutukJsonArray = blukutukJsonArray;
    }

    public void setModelResultListener(BlukutukModel blukutukModel) {
        this.blukutukModel = blukutukModel;
    }

    public void setUploadProgressListener(BlukutukUploadProgress blukutukUploadProgress) {
        this.blukutukUploadProgress = blukutukUploadProgress;
    }

    public void setDownloadListener(BlukutukDownload blukutukDownload) {
        this.blukutukDownload = blukutukDownload;
    }

    public void setConnectionTimeOut(int connectionTimeOut, int writeTimeOut, int readTimeOut) {
        this.connectionTimeOut = connectionTimeOut;
        this.writeTimeOut = writeTimeOut;
        this.readTimeOut = readTimeOut;
    }

    public void setIsFragment(boolean isFragment, Fragment fragment) {
        this.isFragment = isFragment;
        this.fragment = fragment;
    }

    private void processResult(Object o) {
        String data = (String) o;

        if (data.length() == 0 && responseMessage.length() == 0) {
            responseCode = 999;
            responseMessage = code("" + responseCode);

            blukutukFail.result(responseCode, responseMessage);

            return;
        }

        if (responseMessage.length() == 0) {
            if (blukutukJsonObject != null) {
                Boolean failedJsonTest = false;
                String jsonException = "";

                JSONObject result = null;
                try {
                    result = new JSONObject(data);
                } catch (JSONException e) {
                    jsonException = e.getMessage();
                    failedJsonTest = true;
                }

                if (failedJsonTest) {
                    responseCode = 999;
                    responseMessage = code("" + responseCode) + ". " + jsonException;

                    blukutukFail.result(responseCode, responseMessage);
                } else {
                    blukutukJsonObject.result(result);
                }
            }

            if (blukutukJsonArray != null) {
                boolean failedJsonTest = false;
                String jsonException = "";

                JSONArray result = null;
                try {
                    result = new JSONArray(data);
                } catch (JSONException e) {
                    jsonException = e.getMessage();
                    failedJsonTest = true;
                }

                if (failedJsonTest) {
                    responseCode = 999;
                    responseMessage = code("" + responseCode) + ". " + jsonException;

                    blukutukFail.result(responseCode, responseMessage);
                } else {
                    blukutukJsonArray.result(result);
                }
            }

            if (blukutukModel != null && model != null) {
                Gson gson = new Gson();
                Object modelResult = gson.fromJson(data, (Type) model);

                blukutukModel.result(Primitives.wrap(model).cast(modelResult));
            }

        } else {
            if (blukutukFail != null) {
                if (responseMessage.equals("Exception")) {
                    blukutukFail.result(responseCode, code("" + responseCode));
                } else {
                    blukutukFail.result(responseCode, responseMessage);
                }
            }
        }
    }

    public Boolean jsonObjectTest(String o) {
        boolean failedJsonTest = false;

        JSONObject result = null;
        try {
            result = new JSONObject(o);
        } catch (JSONException e) {
            try {
                new JSONArray(o);
            } catch (JSONException e1) {
                failedJsonTest = true;
            }
        }

        return failedJsonTest;
    }

    public Boolean jsonArrayTest(String o) {
        boolean failedJsonTest = false;

        JSONArray result = null;
        try {
            result = new JSONArray(o);
        } catch (JSONException e) {
            try {
                new JSONArray(o);
            } catch (JSONException e1) {
                failedJsonTest = true;
            }
        }

        return failedJsonTest;
    }

    public void execute() {
        if (!Network.isNetworkAvailable(activity)) {
            blukutukFail.result(900, code("900"));

            return;
        }
        OkHttp okHttp = new OkHttp();
        okHttp.setOkHttpInterface(new OkHttpInterface() {
            @Override
            public Boolean isAcceptAllCertificate() {
                return isAcceptAllCertificate;
            }

            @Override
            public File downloadPath() {
                return downloadPath;
            }

            @Override
            public int getBodyType() {
                return bodyType;
            }

            @Override
            public RequestBody requestBody() {
                return requestBody;
            }

            @Override
            public String downloadFileName() {
                return downloadFileName;
            }

            @Override
            public String paternCertificate() {
                return paternCertificate;
            }

            @Override
            public String pinCertificate() {
                return pinCertificate;
            }

            @Override
            public String url() {
                return url;
            }

            @Override
            public HashMap<Integer, Integer> getConnectionTimeOut() {
                HashMap<Integer, Integer> hashMap = new HashMap<>();
                hashMap.put(0, connectionTimeOut);
                hashMap.put(1, writeTimeOut);
                hashMap.put(2, readTimeOut);
                return hashMap;
            }

            @Override
            public Uri.Builder builder() {
                return builder;
            }

            @Override
            public void before() {
                if (!activity.isDestroyed()) {
                    if (progressDialog != null) {
                        progressDialog.show();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void progress(int progress) {
                if (blukutukUploadProgress != null && !activity.isDestroyed()) {
                    activity.runOnUiThread(() -> blukutukUploadProgress.result(progress));
                }
            }

            @Override
            public void status(int code, String message) {
                responseCode = code;
                responseMessage = message;
            }

            @Override
            public void after(Object o) {
                boolean noFragmentProblem = true;
                if (isFragment && fragment != null) {
                    if (fragment.getActivity() == null) {
                        noFragmentProblem = false;
                    } else {
                        if (fragment.getActivity().isDestroyed()) {
                            noFragmentProblem = false;
                        }
                        if (fragment.isDetached()) {
                            noFragmentProblem = false;
                        }
                    }
                }
                if (!activity.isDestroyed() || noFragmentProblem) {
                    processResult(o);

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        okHttp.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void download() {
        if (!Network.isNetworkAvailable(activity)) {
            blukutukFail.result(900, code("900"));

            return;
        }

        OkHttpDownload okHttp = new OkHttpDownload();
        okHttp.setOkHttpInterface(new OkHttpInterface() {
            @Override
            public Boolean isAcceptAllCertificate() {
                return isAcceptAllCertificate;
            }

            @Override
            public File downloadPath() {
                return downloadPath;
            }

            @Override
            public int getBodyType() {
                return bodyType;
            }

            @Override
            public RequestBody requestBody() {
                return requestBody;
            }

            @Override
            public String downloadFileName() {
                return downloadFileName;
            }

            @Override
            public String paternCertificate() {
                return paternCertificate;
            }

            @Override
            public String pinCertificate() {
                return pinCertificate;
            }

            @Override
            public String url() {
                return url;
            }

            @Override
            public Uri.Builder builder() {
                return builder;
            }

            @Override
            public HashMap<Integer, Integer> getConnectionTimeOut() {
                HashMap<Integer, Integer> hashMap = new HashMap<>();
                hashMap.put(0, connectionTimeOut);
                hashMap.put(1, writeTimeOut);
                hashMap.put(2, readTimeOut);
                return hashMap;
            }

            @Override
            public void before() {
                if (!activity.isDestroyed()) {
                    if (progressDialog != null) {
                        progressDialog.show();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void progress(int progress) {
                if (blukutukUploadProgress != null && !activity.isDestroyed()) {
                    activity.runOnUiThread(() -> blukutukUploadProgress.result(progress));
                }
            }

            @Override
            public void status(int code, String message) {
                responseCode = code;
                responseMessage = message;
            }

            @Override
            public void after(Object o) {
                if (!activity.isDestroyed()) {
                    if (blukutukDownload != null) {
                        if (o.equals("1")) {
                            blukutukDownload.success();
                        } else {
                            blukutukDownload.failed(responseCode, responseMessage);
                        }
                    }

                    if (progressDialog != null) {
                        progressDialog.dismiss();
                    }

                    if (progressBar != null) {
                        progressBar.setVisibility(View.GONE);
                    }
                }
            }
        });

        okHttp.execute();
    }

    @SuppressWarnings("ConstantConditions")
    static class OkHttp extends AsyncTask {

        OkHttpInterface okHttpInterface;

        void setOkHttpInterface(OkHttpInterface okHttpInterface) {
            this.okHttpInterface = okHttpInterface;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            okHttpInterface.before();
        }

        @Override
        protected Object doInBackground(final Object[] objects) {
            HashMap<Integer, Integer> hashMap = okHttpInterface.getConnectionTimeOut();
            OkHttpClient.Builder builderOkhttp = new OkHttpClient.Builder()
                    .connectTimeout(hashMap.get(0), TimeUnit.SECONDS)
                    .writeTimeout(hashMap.get(1), TimeUnit.SECONDS)
                    .readTimeout(hashMap.get(2), TimeUnit.SECONDS);

            if (okHttpInterface.paternCertificate().length() > 0 && okHttpInterface.pinCertificate().length() > 0) {
                builderOkhttp.certificatePinner(new CertificatePinner.Builder().add(okHttpInterface.paternCertificate(), okHttpInterface.pinCertificate()).build());
            }

            if (okHttpInterface.isAcceptAllCertificate()) {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                try {
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                    builderOkhttp.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                } catch (NoSuchAlgorithmException e) {
                    okHttpInterface.status(904, e.getMessage());

                    return "";
                } catch (KeyManagementException e) {
                    okHttpInterface.status(905, e.getMessage());

                    return "";
                }
            }
            OkHttpClient client = builderOkhttp.build();

            String urlTemp = okHttpInterface.url();

            if (okHttpInterface.builder() != null) {
                urlTemp = okHttpInterface.builder().toString();
            }

            Request request = new Request.Builder()
                    .url(urlTemp)
                    .build();

            if (okHttpInterface.requestBody() != null) {
                if (okHttpInterface.getBodyType() == POST) {
                    request = new Request.Builder()
                            .url(urlTemp)
//                    .post((RequestBody) objects[1])
                            .post(new ProgressRequestBody(okHttpInterface.requestBody(), progress -> okHttpInterface.progress(progress)))
                            .build();
                }
                if (okHttpInterface.getBodyType() == PUT) {
                    request = new Request.Builder()
                            .url(urlTemp)
//                    .post((RequestBody) objects[1])
                            .put(new ProgressRequestBody(okHttpInterface.requestBody(), progress -> okHttpInterface.progress(progress)))
                            .build();
                }
            }
            try {
                Response response = client.newCall(request).execute();

                ResponseBody responseBody = response.body();

                if (!response.isSuccessful() || response.code() != 200) {
                    okHttpInterface.status(response.code(), "Exception");

                    return "";
                } else if (responseBody != null) {
                    okHttpInterface.status(response.code(), "");

                    return responseBody.string();
                } else {
                    okHttpInterface.status(response.code(), "Exception");

                    return "";
                }
            } catch (Exception e) {
                if (e.getMessage() != null) {
                    okHttpInterface.status(900, e.getMessage());
                } else {
                    okHttpInterface.status(900, "Exception");
                }

                return "";
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            okHttpInterface.after(o);
        }
    }

    @SuppressWarnings("ConstantConditions")
    static class OkHttpDownload extends AsyncTask {

        OkHttpInterface okHttpInterface;

        void setOkHttpInterface(OkHttpInterface okHttpInterface) {
            this.okHttpInterface = okHttpInterface;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            okHttpInterface.before();
        }

        @Override
        protected Object doInBackground(final Object[] objects) {
            HashMap<Integer, Integer> hashMap = okHttpInterface.getConnectionTimeOut();

            OkHttpClient.Builder builderOkhttp = new OkHttpClient.Builder()
                    .connectTimeout(hashMap.get(0), TimeUnit.SECONDS)
                    .writeTimeout(hashMap.get(1), TimeUnit.SECONDS)
                    .readTimeout(hashMap.get(2), TimeUnit.SECONDS);

            if (okHttpInterface.paternCertificate().length() > 0 && okHttpInterface.pinCertificate().length() > 0) {
                builderOkhttp.certificatePinner(new CertificatePinner.Builder().add(okHttpInterface.paternCertificate(), okHttpInterface.pinCertificate()).build());
            }

            if (okHttpInterface.isAcceptAllCertificate()) {
                final TrustManager[] trustAllCerts = new TrustManager[]{
                        new X509TrustManager() {
                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @SuppressLint("TrustAllX509TrustManager")
                            @Override
                            public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
                            }

                            @Override
                            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                                return new java.security.cert.X509Certificate[]{};
                            }
                        }
                };
                try {
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    sslContext.init(null, trustAllCerts, new java.security.SecureRandom());

                    SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

                    builderOkhttp.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0]);
                } catch (NoSuchAlgorithmException e) {
                    okHttpInterface.status(904, e.getMessage());

                    return "";
                } catch (KeyManagementException e) {
                    okHttpInterface.status(905, e.getMessage());

                    return "";
                }
            }

            OkHttpClient client = builderOkhttp.build();

            String urlTemp = okHttpInterface.url();

            if (okHttpInterface.builder() != null) {
                urlTemp = okHttpInterface.builder().toString();
            }

            Request request = new Request.Builder()
                    .url(urlTemp)
                    .build();
            try {
                Response response = client.newCall(request).execute();

                if (!response.isSuccessful()) {
                    okHttpInterface.status(901, "");

                    return "";
                } else {
                    okHttpInterface.status(response.code(), "");

                    try {
                        File file = new File(okHttpInterface.downloadPath(), okHttpInterface.downloadFileName());
                        BufferedSink sink = Okio.buffer(Okio.sink(file));
                        BufferedSource bufferedSource = response.body().source();
                        if (bufferedSource != null) {
                            sink.writeAll(bufferedSource);
                            sink.close();
                            return "1";
                        } else {
                            return "";
                        }
                    } catch (FileNotFoundException e) {
                        return "";
                    } catch (IOException e) {
                        return "";
                    }
                }
            } catch (Exception e) {
                okHttpInterface.status(900, e.getMessage());

                return "";
            }
        }

        @Override
        protected void onPostExecute(Object o) {
            okHttpInterface.after(o);
        }
    }

    public static String code(String code) {
        String result = "";
        if (code.startsWith("5")) {
            result += "Server error.\nPlease contact customer support and describe your issue\n";
        } else if (code.startsWith("4")) {
            result += "Connection interrupted.\nPlease contact customer support and describe your issue\n";
        }
        switch (code) {
            case "505":
                result += "HTTP Version Not Supported";
                break;
            case "504":
                result += "Gateway Timeout";
                break;
            case "503":
                result += "Service Unavailable";
                break;
            case "502":
                result += "Bad Gateway";
                break;
            case "501":
                result += "Not Implemented";
                break;
            case "500":
                result += "Internal Server Error";
                break;
            case "417":
                result += "Expectation Failed";
                break;
            case "416":
                result += "Requested Range Not Satisfiable";
                break;
            case "415":
                result += "Unsupported Media Type";
                break;
            case "414":
                result += "Request-URI Too Long";
                break;
            case "413":
                result += "Request Entity Too Large";
                break;
            case "412":
                result += "Precondition Failed";
                break;
            case "411":
                result += "Length Required";
                break;
            case "410":
                result += "Gone";
                break;
            case "409":
                result += "Conflict";
                break;
            case "408":
                result += "Request Timeout";
                break;
            case "407":
                result += "Proxy Authentication Required";
                break;
            case "406":
                result += "Not Acceptable";
                break;
            case "405":
                result += "Method Not Allowed";
                break;
            case "404":
                result += "Not Found";
                break;
            case "403":
                result += "Forbidden";
                break;
            case "402":
                result += "Payment Required";
                break;
            case "401":
                result += "Unauthorized";
                break;
            case "400":
                result += "Bad Request";
                break;
            case "900":
                result = "No Internet Connection Available";
                break;
            default:
                result = "Unknown Connection Problem";
                break;
        }
        result += " ( " + code + " ).";
        return result;
    }
}

