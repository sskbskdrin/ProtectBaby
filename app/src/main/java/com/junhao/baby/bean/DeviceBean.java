package com.junhao.baby.bean;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;
import com.junhao.baby.db.IDao;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */
@DatabaseTable(tableName = "tb_device")
public class DeviceBean implements IDao {

    @DatabaseField(generatedId = true)
    public int deviceId;
    @DatabaseField
    public String name;
    @DatabaseField(canBeNull = false)
    public String address;
    @DatabaseField
    public int backLightTime;
    @DatabaseField
    public boolean alertStatus;
    @DatabaseField
    public boolean voice;
    @DatabaseField
    public boolean vibrator;
    @DatabaseField
    public String alertThreshold;
    @DatabaseField
    public String alertTotalThreshold;
    @DatabaseField
    public String deviceNumber;
    @DatabaseField
    public String lastDosage;
    @DatabaseField
    public String lastTotalDosage;
    @DatabaseField
    public int lastTemperature;
    @DatabaseField
    public int lastBattery;
    @DatabaseField
    public long lastUpdateTime;

    public DeviceBean() {
    }

    public DeviceBean(String name, String address) {
        this.name = name;
        this.address = address;
    }

    @Override
    public Object getId() {
        return address;
    }

    @Override
    public void setId(Object id) {
        address = (String) id;
    }

    @Override
    public String getIdFieldName() {
        return "address";
    }
}
