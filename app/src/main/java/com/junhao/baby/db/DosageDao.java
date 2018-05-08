package com.junhao.baby.db;

import android.content.Context;

import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.QueryBuilder;
import com.junhao.baby.BabyApp;
import com.junhao.baby.bean.DosageBean;

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

    public DosageBean queryLastForTime(long maxTime) {
        QueryBuilder<DosageBean, Integer> builder = mDao.queryBuilder();
        DosageBean bean = null;
        try {
            bean = builder.orderBy("time", false).where().le("time", maxTime).queryForFirst();
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
