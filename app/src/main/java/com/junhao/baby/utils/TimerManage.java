package com.junhao.baby.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

public class TimerManage {
    private static final String TAG = "TimerManage";

    private static TimerManage mInstance;

    public static TimerManage getInstance() {
        if (mInstance == null) {
            mInstance = new TimerManage();
        }
        return mInstance;
    }

    private Handler mHandler = null;

    private TimerManage() {
        mHandler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                TimerInfo info = (TimerInfo) msg.obj;
                if (info != null && info.mCount > 0) {
                    TimerTaskListener listener = info.getListener();
                    info.mCount--;
                    if (info.mCount > 0) {
                        Message message = Message.obtain();
                        message.what = msg.what;
                        message.obj = info;
                        mHandler.sendMessageDelayed(message, info.mPeriodTime);
                    }
                    if (listener != null) {
                        listener.onTimer(info.getTag(), info.getCount());
                    }
                }
                return true;
            }
        });
    }

    public void startTimerTask(String tag, long period, TimerTaskListener listener) {
        startTimerTask(tag, period, 1, listener);
    }

    public void startTimerTask(String tag, long period, int count, TimerTaskListener listener) {
        startTimerTask(tag, period, count, 0, listener);
    }

    /**
     * 添加计时器，时间以10ms为倍数
     *
     * @param tag      计时器标识
     * @param count    计时次数，count != 0,大于0时为计时次数，小于0时，为无限循环
     * @param delay    延迟时间
     * @param period   计时周期，period > 10
     * @param listener 计时回调 listener != null;
     */
    public void startTimerTask(String tag, long period, int count, long delay,
                               TimerTaskListener listener) {
        if (TextUtils.isEmpty(tag) || delay < 0 || period < 10 || count == 0 || listener == null) {
            throw new IllegalArgumentException("params is illegal");
        }
        mHandler.removeMessages(tag.hashCode());
        Message message = Message.obtain();
        message.what = tag.hashCode();
        message.obj = new TimerInfo(tag, count, period, listener);
        mHandler.sendMessageDelayed(message, delay + period);
    }

    /**
     * 停止计时器
     *
     * @param tag 计时器标识
     */
    public void stopTimerTask(String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("tag is null");
        }
        mHandler.removeMessages(tag.hashCode());
        L.d(TAG, "Timer task stop " + tag);
    }

    /**
     * 停止所有计时器
     */
    public void stopTimerAll() {
        L.d(TAG, "Timer task remove all");
        mHandler.removeCallbacksAndMessages(null);
    }

    public interface TimerTaskListener {
        void onTimer(String tag, int count);
    }

    private class TimerInfo {

        private boolean isRemove = false;
        private String mTag;
        private long mPeriodTime;
        private TimerTaskListener mListener;

        protected int mCount;

        public TimerInfo(String tag, int count, long period, TimerTaskListener listener) {
            mTag = tag;
            mCount = count;
            mPeriodTime = period;
            mListener = listener;
        }

        public String getTag() {
            return mTag;
        }

        public int getCount() {
            return mCount;
        }

        public long getPeriodTime() {
            return mPeriodTime;
        }

        public TimerTaskListener getListener() {
            return mListener;
        }

        public boolean isRemove() {
            return isRemove;
        }

        public void setRemove(boolean move) {
            isRemove = move;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null)
                return false;
            if (o instanceof TimerInfo) {
                TimerInfo info = (TimerInfo) o;
                return mTag == null ? info.mTag == null : mTag.equals(info.getTag());
            }
            return false;
        }
    }

}
