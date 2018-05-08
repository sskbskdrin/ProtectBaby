package com.junhao.baby.db;

import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;

import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ex-keayuan001 on 2017/11/13.
 *
 * @author ex-keayuan001
 */
public class BaseDao<T extends IDao> {
    private static String TAG;

    protected Dao<T, Integer> mDao;

    public BaseDao(Context context, Class<T> clazz) {
        TAG = getClass().getSimpleName();
        try {
            mDao = DatabaseHelper.getHelper(context).getDao(clazz);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public synchronized int add(T t) {
        int result = -1;
        try {
            result = mDao.create(t);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized int addOrUpdate(T t) {
        int result = -1;
        try {
            T temp = mDao.queryBuilder().where().eq(t.getIdFieldName(), t.getId()).queryForFirst();
            if (temp != null) {
                t.setId(temp.getId());
                result = mDao.update(t);
            } else {
                result = mDao.create(t);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized T queryForId(String fieldName, Object id) {
        T result = null;
        try {
            result = mDao.queryBuilder().where().eq(fieldName, id).queryForFirst();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized ArrayList<T> queryAll() {
        ArrayList<T> list = new ArrayList<>();
        try {
            list = (ArrayList<T>) mDao.queryBuilder().query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        return list;
    }

    public synchronized int delete(T t) {
        int result = -1;
        try {
            DeleteBuilder builder = mDao.deleteBuilder();
            builder.where().eq(t.getIdFieldName(), t.getId());
            result = builder.delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public synchronized int deleteAll() {
        int result = -1;
        try {
            result = mDao.deleteBuilder().delete();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }
}
