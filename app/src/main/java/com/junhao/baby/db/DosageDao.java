package com.junhao.baby.db;

import android.content.Context;
import android.text.TextUtils;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.junhao.baby.BabyApp;
import com.junhao.baby.bean.DosageBean;
import com.junhao.baby.service.ServiceManager;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ex-keayuan001 on 2018/3/14.
 *
 * @author ex-keayuan001
 */
public class DosageDao extends BaseDao<DosageBean> {


    private static DosageDao mInstance;

    private DosageDao(Context context, Class<DosageBean> clazz) {
        super(context, clazz);
    }

    public static void init() {
        mInstance = null;
        getInstance();
    }

    public static DosageDao getInstance() {
        if (mInstance == null) {
            synchronized (DosageDao.class) {
                if (mInstance == null) {
                    mInstance = new DosageDao(BabyApp.getContext(), DosageBean.class);
                }
            }
        }
        return mInstance;
    }

    @Override
    public synchronized int addOrUpdate(DosageBean bean) {
        int result = -1;
        try {
            DosageBean temp = mDao.queryBuilder().where().ge("time", bean.time).queryForFirst();
            if (temp == null) {
                result = mDao.create(bean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public List<DosageBean> queryRange(long start, long end) {
        QueryBuilder<DosageBean, Integer> builder = mDao.queryBuilder();
        List<DosageBean> list;
        try {
            list = builder.orderBy("time", true).where().ge("time", start).and().le("time", end).query();
        } catch (SQLException e) {
            e.printStackTrace();
            list = new ArrayList<>();
        }
        return list;
    }

    public DosageBean queryLastForTime(long maxTime, String address) {
        QueryBuilder<DosageBean, Integer> builder = mDao.queryBuilder().orderBy("time", false);
        DosageBean bean = null;
        try {
            if (TextUtils.isEmpty(address)) {
                bean = builder.where().le("time", maxTime).queryForFirst();
            } else {
                bean = builder.where().le("time", maxTime).and().eq("address", ServiceManager
                        .getInstance().getDeviceAddress())
                        .queryForFirst();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return bean;
    }

    @Override
    public synchronized ArrayList<DosageBean> queryAll() {
        List<DosageBean> list = new ArrayList<>();
        try {
            list = mDao.queryBuilder().orderBy("time", true).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return (ArrayList<DosageBean>) list;
    }

    public void delete(String name, String value) {
        DeleteBuilder<DosageBean, Integer> builder = mDao.deleteBuilder();
        try {
            builder.where().eq(name, value);
            builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
