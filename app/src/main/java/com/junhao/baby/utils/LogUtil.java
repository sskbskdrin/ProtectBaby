package com.junhao.baby.utils;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 将Log日志写入文件中
 * <p>
 * Created by ex-keayuan001 on 2018/5/4.
 *
 * @author ex-keayuan001
 */
public class LogUtil {

    private static final String TAG = "LogUtil";

    private static String logPath = Environment.getExternalStorageDirectory().getPath() + "/baby/";

    private static SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd HH:mm:ss.SSS", Locale.US);

    private static Handler mHandler;


    /**
     * 初始化，须在使用之前设置，最好在Application创建时调用
     */
    public static void init() {
        new HandlerThread("log") {
            @Override
            protected void onLooperPrepared() {
                mHandler = new Handler(new Handler.Callback() {
                    BufferedWriter writer;
                    int count = 0;

                    @Override
                    public boolean handleMessage(Message msg) {
                        if (count > 10 * 1024 * 1024) {
                            if (writer != null) {
                                try {
                                    writer.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } finally {
                                    writer = null;
                                }
                            }
                        }
                        if (writer == null) {
                            File file = new File(logPath);
                            if (!file.exists()) {
                                file.mkdirs();//创建父路径
                            }
                            try {
                                String fileName = logPath + "log_" + new SimpleDateFormat("MM-dd HH:mm:ss", Locale
                                    .US).format(new Date()) + ".log";
                                //log日志名，使用时间命名，保证不重复
                                FileOutputStream fos = new FileOutputStream(fileName, true);
                                //这里的第二个参数代表追加还是覆盖，true为追加，flase为覆盖
                                writer = new BufferedWriter(new OutputStreamWriter(fos));
                            } catch (FileNotFoundException e) {
                                e.printStackTrace();
                                writer = null;
                            }
                        }
                        try {
                            if (writer != null) {
                                String log = (String) msg.obj;
                                writer.write(log);
                                count += log.length();
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return true;
                    }
                });
            }
        }.start();
    }

    public static void v(String tag, String msg) {
        String log = dateFormat.format(new Date()) + " V/" + tag + ": " + msg + "\n";
        //        Message.obtain(mHandler, log.hashCode(), log).sendToTarget();
    }

    public static void d(String tag, String msg) {
        String log = dateFormat.format(new Date()) + " D/" + tag + ": " + msg + "\n";
        //        Message.obtain(mHandler, log.hashCode(), log).sendToTarget();
    }

    public static void i(String tag, String msg) {
        String log = dateFormat.format(new Date()) + " I/" + tag + ": " + msg + "\n";
        //        Message.obtain(mHandler, log.hashCode(), log).sendToTarget();
    }

    public static void w(String tag, String msg) {
        String log = dateFormat.format(new Date()) + " W/" + tag + ": " + msg + "\n";
        //        Message.obtain(mHandler, log.hashCode(), log).sendToTarget();
    }

    public static void e(String tag, String msg) {
        String log = dateFormat.format(new Date()) + " E/" + tag + ": " + msg + "\n";
        //        Message.obtain(mHandler, log.hashCode(), log).sendToTarget();
    }
}