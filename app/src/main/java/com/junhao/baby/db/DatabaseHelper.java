package com.junhao.baby.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.junhao.baby.bean.DeviceBean;
import com.junhao.baby.bean.DosageBean;
import com.junhao.baby.bean.DosageTableBean;
import com.junhao.baby.utils.L;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String TAG = "DatabaseHelper";

    public static String DB_NAME = "baby_db.db";
    private Map<String, Dao> mDaoMap = new HashMap<>();
    private volatile static DatabaseHelper instance;
    public static final int VERSION = 2;

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, VERSION);
    }

    public static void init() {
        if (instance != null) {
            instance.close();
        }
        instance = null;

        DeviceDao.init();
        DosageDao.init();
        DosageTableDao.init();
    }

    @Override
    public void onCreate(SQLiteDatabase arg0, ConnectionSource arg1) {
        try {
            L.i(TAG, "DB_VERSION   " + VERSION);
            TableUtils.createTable(connectionSource, DeviceBean.class);
            TableUtils.createTable(connectionSource, DosageBean.class);
            TableUtils.createTable(connectionSource, DosageTableBean.class);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 单例获取该Helper
     *
     * @param context
     * @return
     */
    public static synchronized DatabaseHelper getHelper(Context context) {
        context = context.getApplicationContext();
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context);
                }
            }
        }
        return instance;
    }

    public synchronized Dao getDao(Class clazz) throws SQLException {
        Dao dao = null;
        String className = clazz.getSimpleName();
        if (mDaoMap.containsKey(className)) {
            dao = mDaoMap.get(className);
        }
        if (dao == null) {
            dao = super.getDao(clazz);
            mDaoMap.put(className, dao);
        }
        return dao;
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource arg1, int oldVer, int newVer) {
        try {
            if (newVer > 1) {
                getDao(DosageBean.class).executeRawNoArgs("ALTER TABLE tb_dosage ADD COLUMN dosageEachH FLOAT");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放资源
     */
    @Override
    public void close() {
        super.close();
        Iterator iterator = mDaoMap.keySet().iterator();
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }
    }
}
