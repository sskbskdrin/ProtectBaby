package com.junhao.baby.download;

import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

/**
 * 下载任务
 *
 * @author keayuan
 */
public class DownloadTask implements Runnable {
    private final static String TAG = "DownloadTask";
    private String mUrl;
    private IDownloadListener mDlListener;
    /**
     * 下载到的数据资源
     */
    private byte[] mDataRes;
    /**
     * 下载到的数据长度
     */
    private int mDataLen;
    /**
     * 是否停止下载
     */
    private boolean mIsStopDl = false;
    /**
     * 唯一标识一个下载任务
     */
    private String mTaskTag;

    private Map<String, String> mPostData;
    private String mPostString;

    private static final int CONNECTION_TIMEOUT = 30 * 1000;
    private static final int SOCKET_TIMEOUT = 30 * 1000;

    public DownloadTask(String tag, String url, Map<String, String> data, IDownloadListener
            listener) {
        mTaskTag = tag;
        mIsStopDl = false;
        mPostData = data;
        mUrl = url;
        mDlListener = listener;
    }

    public DownloadTask(String tag, String url, String data,
                        IDownloadListener listener) {
        mTaskTag = tag;
        mIsStopDl = false;
        mPostString = data;
        mUrl = url;
        mDlListener = listener;
    }

    public void stop() {
        Log.d(TAG, "task stop: " + mTaskTag);
        mIsStopDl = true;
    }

    public void setDownloadListener(IDownloadListener listener) {
        mDlListener = listener;
    }

    @Override
    public void run() {
        Log.d(TAG, mTaskTag + " run start");
        if (mDlListener != null) {
            mDlListener.onDlStart(mTaskTag);
        }
        if (mPostData != null) {
            postRemoteRes(mUrl, mPostData);
        } else if (mPostString != null) {
            postRemoteRes(mUrl, mPostString);
        } else {
            getRemoteRes(mUrl);
        }
        Log.d(TAG, mTaskTag + " run end");
    }

    /**
     * 在线下载
     *
     * @param url
     */
    private void getRemoteRes(String url) {
        // Log.d(TAG, "getRemoteRes url=" + url);
        try {
            // 新建一个URL对象
            URL requestUrl = new URL(url);
            // 打开一个HttpURLConnection连接
            HttpURLConnection connection = (HttpURLConnection) requestUrl.openConnection();
            // 设置连接主机超时时间
            connection.setConnectTimeout(10 * 1000);
            //设置从主机读取数据超时
            connection.setReadTimeout(60 * 1000);
            // 设置是否使用缓存  默认是true
            connection.setUseCaches(true);
            // 设置为Post请求
            connection.setRequestMethod("GET");
            //urlConn设置请求头信息
            //设置请求中的媒体类型信息。
//            connection.setRequestProperty("Content-Type", "application/json");
            //设置客户端与服务连接类型
            connection.addRequestProperty("Connection", "Keep-Alive");
            // 开始连接
            connection.connect();
            // 判断请求是否成功
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                byte[] buffer = new byte[512];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int readLength = 0;
                int offset = 0;
                mDataLen = 0;
                mDataRes = null;
                do {
                    readLength = is.read(buffer);
                    if (readLength > 0) {
                        baos.write(buffer, 0, readLength);
                        offset += readLength;
                        mDataLen = offset;
                    }
                } while (!mIsStopDl && readLength > 0);
                mDataRes = baos.toByteArray();
                baos.close();
                is.close();
            }
            // 下载完成
            Log.i(TAG, "downloadRemoteRes download completed. data length="
                    + (mDataRes == null ? "null" : mDataRes.length)
                    + " record length=" + mDataLen + " url=" + url);
            if (!mIsStopDl && (mDataRes == null || mDataRes.length == 0)) {
                Log.e(TAG, "data = null");
                if (mDlListener != null)
                    mDlListener.onDlError(mTaskTag, -1);
                return;
            }
            if (!mIsStopDl && mDlListener != null) {
                Log.d(TAG, "downloadRemoteRes ---- callback in.");
                mDlListener.onDlCompleted(mTaskTag, mDataRes, mDataLen);
                Log.d(TAG, "downloadRemoteRes ---- callback out.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG,
                    "downloadRemoteRes exception url=" + url + "msg="
                            + e.getMessage());
            if (!mIsStopDl && mDlListener != null) {
                mDlListener.onDlError(mTaskTag, -1);
            }
        }
    }

    private void postRemoteRes(String url, Map<String, String> data) {
        // Log.d(TAG, "postRemoteRes url=" + url);
        try {
            StringBuilder tempParams = new StringBuilder();
            int pos = 0;
            for (String key : data.keySet()) {
                if (pos > 0) {
                    tempParams.append("&");
                }
                tempParams.append(String.format("%s=%s", key, URLEncoder.encode(data.get(key),
                        "utf-8")));
                pos++;
            }
            String params = tempParams.toString();
            // 请求的参数转换为byte数组
            byte[] postData = params.getBytes();
            // 新建一个URL对象
            // 打开一个HttpURLConnection连接
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            // 设置连接超时时间
            connection.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            connection.setReadTimeout(5 * 1000);
            // Post请求必须设置允许输出 默认false
            connection.setDoOutput(true);
            //设置请求允许输入 默认是true
            connection.setDoInput(true);
            // Post请求不能使用缓存
            connection.setUseCaches(false);
            // 设置为Post请求
            connection.setRequestMethod("POST");
            //设置本次连接是否自动处理重定向
            connection.setInstanceFollowRedirects(true);
            // 配置请求Content-Type
            connection.setRequestProperty("Content-Type", "application/json");
            // 开始连接
            connection.connect();
            // 发送请求参数
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.write(postData);
            dos.flush();
            dos.close();
            // 判断请求是否成功
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                byte[] buffer = new byte[512];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int readLength = 0;
                int offset = 0;
                mDataLen = 0;
                mDataRes = null;
                do {
                    readLength = is.read(buffer);
                    if (readLength > 0) {
                        baos.write(buffer, 0, readLength);
                        offset += readLength;
                        mDataLen = offset;
                    }
                } while (!mIsStopDl && readLength > 0);
                mDataRes = baos.toByteArray();
                baos.close();
                is.close();
            }

            // 下载完成
            Log.i(TAG, "downloadRemoteRes download completed. data length="
                    + (mDataRes == null ? "null" : mDataRes.length)
                    + " record length=" + mDataLen + " url=" + url);
            if (!mIsStopDl && (mDataRes == null || mDataRes.length == 0)) {
                Log.e(TAG, "data = null");
                if (mDlListener != null)
                    mDlListener.onDlError(mTaskTag, -1);
                return;
            }
            if (!mIsStopDl && mDlListener != null) {
                Log.d(TAG, "downloadRemoteRes ---- callback in.");
                mDlListener.onDlCompleted(mTaskTag, mDataRes, mDataLen);
                Log.d(TAG, "downloadRemoteRes ---- callback out.");
            }
        } catch (Exception e) {
            Log.e(TAG,
                    "downloadRemoteRes exception url=" + url + "msg="
                            + e.getMessage());
            if (!mIsStopDl && mDlListener != null) {
                mDlListener.onDlError(mTaskTag, -1);
            }
        }
    }

    private void postRemoteRes(String url, String data) {
        // Log.d(TAG, "postRemoteRes url=" + url);
        try {
            StringBuilder tempParams = new StringBuilder();
            // 请求的参数转换为byte数组
            byte[] postData = data.getBytes();
            // 新建一个URL对象
            // 打开一个HttpURLConnection连接
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            // 设置连接超时时间
            connection.setConnectTimeout(5 * 1000);
            //设置从主机读取数据超时
            connection.setReadTimeout(5 * 1000);
            // Post请求必须设置允许输出 默认false
            connection.setDoOutput(true);
            //设置请求允许输入 默认是true
            connection.setDoInput(true);
            // Post请求不能使用缓存
            connection.setUseCaches(false);
            // 设置为Post请求
            connection.setRequestMethod("POST");
            //设置本次连接是否自动处理重定向
            connection.setInstanceFollowRedirects(true);
            // 配置请求Content-Type
            connection.setRequestProperty("Content-Type", "application/json");
            // 开始连接
            connection.connect();
            // 发送请求参数
            DataOutputStream dos = new DataOutputStream(connection.getOutputStream());
            dos.write(postData);
            dos.flush();
            dos.close();
            // 判断请求是否成功
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                byte[] buffer = new byte[512];
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                int readLength = 0;
                int offset = 0;
                mDataLen = 0;
                mDataRes = null;
                do {
                    readLength = is.read(buffer);
                    if (readLength > 0) {
                        baos.write(buffer, 0, readLength);
                        offset += readLength;
                        mDataLen = offset;
                    }
                } while (!mIsStopDl && readLength > 0);
                mDataRes = baos.toByteArray();
                baos.close();
                is.close();
            }

            // 下载完成
            Log.i(TAG, "downloadRemoteRes download completed. data length="
                    + (mDataRes == null ? "null" : mDataRes.length)
                    + " record length=" + mDataLen + " url=" + url);
            if (!mIsStopDl && (mDataRes == null || mDataRes.length == 0)) {
                Log.e(TAG, "data = null");
                if (mDlListener != null)
                    mDlListener.onDlError(mTaskTag, -1);
                return;
            }
            if (!mIsStopDl && mDlListener != null) {
                Log.d(TAG, "downloadRemoteRes ---- callback in.");
                mDlListener.onDlCompleted(mTaskTag, mDataRes, mDataLen);
                Log.d(TAG, "downloadRemoteRes ---- callback out.");
            }
        } catch (Exception e) {
            Log.e(TAG,
                    "downloadRemoteRes exception url=" + url + "msg="
                            + e.getMessage());
            if (!mIsStopDl && mDlListener != null) {
                mDlListener.onDlError(mTaskTag, -1);
            }
        }
    }
}
