package com.junhao.baby;

import android.app.Application;
import android.content.Context;

import com.junhao.baby.db.DatabaseHelper;
import com.junhao.baby.db.ThreadPool;
import com.junhao.baby.utils.LogUtil;
import com.junhao.baby.utils.SpfUtil;
import com.junhao.baby.utils.TimerManage;
import com.orhanobut.logger.LogcatHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import cn.feng.skin.manager.loader.SkinManager;

/**
 * Created by ex-keayuan001 on 2018/3/2.
 *
 * @author ex-keayuan001
 */
public class BabyApp extends Application {
    private static BabyApp mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("BabyApp");
        mContext = this;
        CrashHandler.getInstance().init(this);
        LogcatHelper.getInstance().init(this);
        LogcatHelper.getInstance().start();
        SpfUtil.init(this);
        DatabaseHelper.init();
        initSkinLoader();
        TimerManage.getInstance();
    }

    /**
     * Must call init first
     */
    private void initSkinLoader() {
        SkinManager.getInstance().init(this);
        SkinManager.getInstance().load();
        copyApkFromAssets(this, getFilesDir().getPath() + "/");
    }

    public void copyApkFromAssets(final Context context, final String path) {
        ThreadPool.run(new Runnable() {
            @Override
            public void run() {
                try {
                    String[] fileNames = context.getAssets().list("skin");
                    for (String name : fileNames) {
                        InputStream is = context.getAssets().open("skin/" + name);
                        File file = new File(path + name);
                        if (file.exists()) {
                            file.delete();
                        }
                        file.createNewFile();
                        FileOutputStream fos = new FileOutputStream(file);
                        byte[] temp = new byte[1024];
                        int i;
                        while ((i = is.read(temp)) > 0) {
                            fos.write(temp, 0, i);
                        }
                        fos.close();
                        is.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static BabyApp getContext() {
        return mContext;
    }
}
