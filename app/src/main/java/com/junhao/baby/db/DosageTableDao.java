package com.junhao.baby.db;

import android.content.Context;

import com.junhao.baby.BabyApp;
import com.junhao.baby.bean.DosageTableBean;

import java.sql.SQLException;

/**
 * Created by ex-keayuan001 on 2018/3/14.
 *
 * @author ex-keayuan001
 */
public class DosageTableDao extends BaseDao<DosageTableBean> {


    private static DosageTableDao mInstance;

    private DosageTableDao(Context context, Class<DosageTableBean> clazz) {
        super(context, clazz);
    }

    public static void init() {
        mInstance = null;
        getInstance();
    }

    public static DosageTableDao getInstance() {
        if (mInstance == null) {
            synchronized (DosageTableDao.class) {
                if (mInstance == null) {
                    mInstance = new DosageTableDao(BabyApp.getContext(), DosageTableBean.class);
                }
            }
        }
        return mInstance;
    }

    public synchronized DosageTableBean queryForIdAndAddress(int index, String address) {
        DosageTableBean result = null;
        try {
            result = mDao.queryBuilder().where().eq("index", index).and().eq("address", address).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    @Override
    public synchronized int addOrUpdate(DosageTableBean bean) {
        int result = -1;
        try {
            DosageTableBean temp = mDao.queryBuilder().where().eq(bean.getIdFieldName(), bean
                    .getId()).and().eq("address", bean.address).queryForFirst();
            if (temp != null) {
                bean.id = temp.id;
                result = mDao.update(bean);
            } else {
                result = mDao.create(bean);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
