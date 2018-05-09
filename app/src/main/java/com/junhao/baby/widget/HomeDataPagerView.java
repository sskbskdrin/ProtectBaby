package com.junhao.baby.widget;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.bean.DosageBean;
import com.junhao.baby.bean.StatisticsBean;
import com.junhao.baby.bean.User;
import com.junhao.baby.db.DosageDao;
import com.junhao.baby.view.ChartView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/28.
 */

public class HomeDataPagerView extends LinearLayout {

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;

    private ChartView mChartView;
    private TextView mDataView;

    public HomeDataPagerView(Context context, StatisticsBean bean) {
        super(context);
        View.inflate(context, R.layout.home_chart_layout, this);
        mDataView = (TextView) findViewById(R.id.home_chart_data);
        mChartView = (ChartView) findViewById(R.id.home_chart_view);
        updateData(bean);
    }

    public void updateData(StatisticsBean bean) {
        if (bean != null) {
            mChartView.setData(bean.list);
            if (bean.type == 1) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日");
                mDataView.setText(format.format(new Date(bean.startTime)));
            } else if (bean.type == 7) {
                SimpleDateFormat format = new SimpleDateFormat("MM月dd日");
                mDataView.setText("第" + bean.index + "周(" + format.format(new Date(bean.startTime)) + "-" + format
                    .format(new Date(bean.endTime)) + ")");
            } else if (bean.type == 30) {
                SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月");
                mDataView.setText(format.format(new Date(bean.endTime)));
            }
        }
    }

    public static List<StatisticsBean> loadMonthData() {
        List<StatisticsBean> data = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        long endDate = c.getTimeInMillis();

        c.setTimeInMillis(User.getInstance().getConceiveDate());
        int month = c.get(Calendar.MONTH);
        int week = 0;
        long startTime, endTime;
        StatisticsBean bean = new StatisticsBean(30);
        do {
            startTime = c.getTimeInMillis();
            c.add(Calendar.DAY_OF_YEAR, 7);
            endTime = c.getTimeInMillis();

            double sum = getDosage(startTime, endTime);
            float percent = c.get(Calendar.DAY_OF_MONTH) * 1f / c.getActualMaximum(Calendar.DAY_OF_MONTH);
            if (month != c.get(Calendar.MONTH)) {
                data.add(bean);

                bean = new StatisticsBean(30);
                month = c.get(Calendar.MONTH);
            }
            bean.startTime = startTime;
            bean.endTime = endTime;
            bean.addPoint(sum, percent, ++week + "周");

        } while (c.getTimeInMillis() < endDate);
        bean.startTime = startTime;
        bean.endTime = endTime;
        data.add(bean);
        return data;
    }

    public static List<StatisticsBean> loadWeekData() {
        List<StatisticsBean> data = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(User.getInstance().getConceiveDate());
        int week = c.get(Calendar.DAY_OF_WEEK) - 1;
        c.add(Calendar.DAY_OF_WEEK, -week);

        long step = DAY;
        while (c.getTimeInMillis() < System.currentTimeMillis()) {
            StatisticsBean bean = new StatisticsBean(7);
            bean.startTime = c.getTimeInMillis();
            c.add(Calendar.DAY_OF_YEAR, 7);
            bean.endTime = c.getTimeInMillis() - 5000;
            for (int i = 0; i < 7; i++) {
                double sum = getDosage(bean.startTime + step * i, bean.startTime + step * (i + 1));
                bean.addPoint(sum, i / 6.0f, getWeek(i));
            }
            data.add(bean);
            bean.index = data.size();
        }
        return data;
    }

    public static List<StatisticsBean> loadDayData() {
        List<StatisticsBean> data = new ArrayList<>();
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(User.getInstance().getConceiveDate());
        long step = 4 * HOUR;
        while (c.getTimeInMillis() < System.currentTimeMillis()) {
            StatisticsBean bean = new StatisticsBean(1);
            bean.startTime = c.getTimeInMillis();
            c.add(Calendar.DAY_OF_YEAR, 1);
            bean.endTime = c.getTimeInMillis();
            for (int i = 0; i < 6; i++) {
                double sum = getDosage(bean.startTime + step * i, bean.startTime + step * (i + 1));
                bean.addPoint(sum, i / 5.0f, ((i + 1) * 4) + "时");
            }
            data.add(bean);
        }
        return data;
    }

    private static float getDosage(long startTime, long endTime) {
        startTime /= 1000;
        if (endTime > System.currentTimeMillis()) {
            endTime = System.currentTimeMillis();
        }
        endTime /= 1000;
        DosageBean indexBean = DosageDao.getInstance().queryLastForTime(startTime, null);
        List<DosageBean> list = DosageDao.getInstance().queryRange(startTime, endTime);
        double sum = 0;
        if (indexBean != null) {
            if (indexBean.dosageEachH == 0) {
                indexBean = null;
            } else {
                indexBean.time = startTime;
            }
        }
        if (list != null) {
            for (DosageBean bean : list) {
                if (indexBean == null || indexBean.time == 0) {
                    indexBean = bean;
                    continue;
                }
                sum += (bean.time - indexBean.time) * indexBean.dosageEachH;
                indexBean = bean;
            }
        }
        if (indexBean != null) {
            DosageBean temp = DosageDao.getInstance().queryFirstForTime(indexBean.time, null);
            if (temp != null) {
                sum += (endTime - indexBean.time) * indexBean.dosageEachH;
            }
        }
        if (sum <= 0) {
            sum = getDosage(startTime * 1000, endTime * 1000, false);
        }
        return ((int) (sum / 3.6f)) / 1000f;
    }

    private static double getDosage(long startTime, long endTime, boolean to) {
        DosageBean startBean = DosageDao.getInstance().queryLastForTime(startTime / 1000, null);
        DosageBean endBean = DosageDao.getInstance().queryLastForTime(endTime / 1000, null);
        double sum;
        if (startBean == null) {
            if (endBean == null) {
                sum = 0;
            } else {
                sum = endBean.dosage;
            }
        } else {
            sum = endBean.dosage - startBean.dosage;
        }
        if (sum < 0) {
            sum = 0;
        }
        return sum;
    }

    private static String getWeek(int i) {
        switch (i) {
            case 0:
                return "周日";
            case 1:
                return "周一";
            case 2:
                return "周二";
            case 3:
                return "周三";
            case 4:
                return "周四";
            case 5:
                return "周五";
            case 6:
                return "周六";
            default:
        }
        return "周";
    }

}
