package com.junhao.baby.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/18.
 */

public class StatisticsBean implements Serializable {
    public int type;
    public int index;
    public long startTime;
    public long endTime;

    public String unitX;
    public String unitY;

    public List<String> periodIndex;//画圆点,为空就不画点
    public List<Float> periodPosition;//画坐标位置  0开始，1结束
    public List<Double> values;//数据坐标点，画坐标位置

    public List<Data> list;

    public StatisticsBean() {
        this(30);
    }

    public StatisticsBean(int type) {
        this.type = type;
        periodIndex = new ArrayList<>(7);
        periodPosition = new ArrayList<>(7);
        values = new ArrayList<>(7);
        list = new ArrayList<>(7);
    }

    public void addPoint(double value, float x, String peroid) {
        periodIndex.add(peroid);
        periodPosition.add(x);
        values.add(value);
        list.add(new Data(value, x, peroid));
    }

    public static class Data implements Serializable {
        public float percent;
        public double value;
        public String nameX;

        public Data(double value, float percent, String name) {
            this.value = value;
            this.percent = percent;
            this.nameX = name;
        }
    }
}
