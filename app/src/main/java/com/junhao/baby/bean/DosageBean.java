package com.junhao.baby.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.junhao.baby.db.IDao;

/**
 * Created by ex-keayuan001 on 2018/3/14.
 *
 * @author ex-keayuan001
 */
@DatabaseTable(tableName = "tb_dosage")
public class DosageBean implements IDao {
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public long time;
    @DatabaseField
    public double dosage;
    @DatabaseField
    public float dosageEachH;
    @DatabaseField
    public int unit;
    @DatabaseField
    public String name;
    @DatabaseField
    public String address;

    private static final int POOL_MAX = 20;
    private static DosageBean sPool = new DosageBean();
    private static int sPoolSize = 0;
    private static final Object sPoolSync = new Object();
    private DosageBean next;
    private boolean isRecycle;

    private DosageBean() {
    }

    @Override
    public Object getId() {
        return time;
    }

    @Override
    public void setId(Object id) {
        this.time = (long) id;
    }

    @Override
    public String getIdFieldName() {
        return "time";
    }

    public static DosageBean obtain() {
        synchronized (sPoolSync) {
            if (sPool != null) {
                DosageBean bean = sPool;
                sPool = sPool.next;
                sPoolSize--;
                bean.isRecycle = false;
                return bean;
            }
        }
        return new DosageBean();
    }

    public void recycle() {
        if (isRecycle) {
            return;
        }
        synchronized (sPoolSync) {
            if (sPoolSize < POOL_MAX) {
                this.next = sPool;
                sPool = this;
                sPool.id = 0;
                sPool.time = 0;
                sPool.dosage = 0;
                sPool.dosageEachH = 0;
                sPool.unit = 0;
                sPool.name = null;
                sPool.address = null;
                sPool.isRecycle = true;
                sPoolSize++;
            }
        }
    }

}
