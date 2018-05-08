package com.junhao.baby.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.junhao.baby.db.IDao;

/**
 * Created by ex-keayuan001 on 2018/3/14.
 *
 * @author ex-keayuan001
 */
@DatabaseTable(tableName = "tb_time_dosage")
public class DosageTableBean implements IDao {
    @DatabaseField(generatedId = true)
    public int id;
    @DatabaseField
    public int index;
    @DatabaseField
    public int second;
    @DatabaseField
    public long date;
    @DatabaseField
    public String name;
    @DatabaseField
    public String address;

    @Override
    public Object getId() {
        return index;
    }

    @Override
    public void setId(Object id) {
        this.index = (Integer) id;
    }

    @Override
    public String getIdFieldName() {
        return "index";
    }
}
