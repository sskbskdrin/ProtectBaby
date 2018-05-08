package com.junhao.baby.activity;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import com.junhao.baby.BabyApp;
import com.junhao.baby.R;
import com.junhao.baby.bean.Device;
import com.junhao.baby.bean.StatisticsBean;
import com.junhao.baby.bean.User;
import com.junhao.baby.db.DosageDao;
import com.junhao.baby.db.ThreadPool;
import com.junhao.baby.service.BluetoothScanListener;
import com.junhao.baby.service.BluetoothService;
import com.junhao.baby.service.DeviceStateListener;
import com.junhao.baby.service.ServiceManager;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.utils.TimerManage;
import com.junhao.baby.utils.ToastUtil;
import com.junhao.baby.widget.AlertDialog;
import com.junhao.baby.widget.BottleDrawable;
import com.junhao.baby.widget.DeviceDialog;
import com.junhao.baby.widget.HomeDataPagerView;

import java.util.ArrayList;
import java.util.List;

import cn.feng.skin.manager.loader.SkinManager;

/**
 * Created by sskbskdrin on 2018/3/2.
 */
public class HomeFragment extends BaseFragment implements View.OnClickListener, BluetoothScanListener,
        DeviceStateListener {
    private static final String TAG = "HomeFragment";

    private static final int WHAT_CLOSE = 1001;

    List<BluetoothDevice> mList = new ArrayList<>();
    private DeviceDialog mDialog;

    private TextView mMenuView;
    private TextView mReconnectView;
    private TextView mTemperatureView;
    private TextView mBatteryView;
    private TextView mAlertTipView;

    private View mDosageLayout;
    private ImageView mDosageImageView;
    private TextView mDosageView;
    private View mDosageTipView;

    private View mTotalDosageLayout;
    private ImageView mTotalDosageImageView;
    private Drawable mTotalDosageDynamicDrawable;
    private Drawable mTotalDosageStaticDrawable;
    private TextView mTotalDosageView;
    private View mTotalDosageTipView;

    private AlertDialog mTipDialog;
    private ViewPager mChartPagerView;
    private View mChartLeftView;
    private View mChartRightView;

    private Adapter mPageAdapter;

    private ObjectAnimator mDosageAnimator;

    private String mHistoryType;

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (msg.what == WHAT_CLOSE) {
                ServiceManager.getInstance().stopScan();
                hideLoadingDialog();
                ToastUtil.showTip(getContext(), "未扫描到设备", "");
            }
            return true;
        }
    });

    private ServiceManager.Callback mCallback = new ServiceManager.Callback() {
        @SuppressLint("SetTextI18n")
        @Override
        public void call(char key, byte[] data, String value) {
            switch (key) {
                case 'A':
                    mMenuView.setText("设备已连接");
                    mReconnectView.setVisibility(View.GONE);
                    if (!TextUtils.isEmpty(value) && value.length() > 2) {
                        alertDosage(value, true);
                    }
                    break;
                case 'B':
                    if (!TextUtils.isEmpty(value) && value.length() > 2) {
                        alertTotalDosage(value, true);
                    }
                    break;
                case 'D':
                    mTemperatureView.setText(value + "°C ");
                    break;
                case 'E':
                    mBatteryView.setText(value + "%");
                    break;
                case 'T':
                    if ("RE".equals(value)) {
                        showLoadingDialog("请在设备上确认");
                    }
                    break;
                case 'V':
                    hideLoadingDialog();
                    if ("OK".equalsIgnoreCase(value)) {
                        if (mDialog != null) {
                            mDialog.dismiss();
                        }
                        ToastUtil.showTip(getContext(), "设备已绑定", "");
                        Device.addDevice(ServiceManager.getInstance().getDeviceAddress(), ServiceManager.getInstance
                                ().getDeviceName());
                    } else {
                        ToastUtil.showTip(getContext(), "设备拒绝绑定", "");
                    }
                    break;
                case 'U':
                    hideLoadingDialog();
                    break;
                case 'S':
                    mMenuView.setText("数据同步中...");
                    break;
            }
            if (data != null && data[0] == 0x55) {
                if (data[1] == 0x1a || data[1] == 0x18) {
                    reloadData();
                }
            }
        }
    };

    private void alertDosage(String value, boolean isConnect) {
        float dosage = Float.parseFloat(value.substring(0, value.length() - 2));
        if (value.charAt(value.length() - 1) - 0x30 > 0) {
            dosage *= 1000;
        }
        if (value.charAt(value.length() - 1) - 0x30 > 1) {
            dosage *= 1000;
        }
        SpannableString ss = new SpannableString("实时剂量率\n" + translate(value) + "Sv/时");
        Drawable left;
        if (dosage >= Device.getAlertThreshold()) {
            mDosageLayout.setBackgroundResource(R.drawable.home_alert_bg);
            mDosageImageView.setImageResource(R.mipmap.home_static_dosage_a_icon);
            mDosageView.setTextColor(SkinManager.getInstance().getColor(R.color.theme_color));
            mAlertTipView.setTextColor(CommonUtils.getColor(getContext(), R.color.secondary));
            mDosageTipView.setVisibility(View.VISIBLE);
            mAlertTipView.setText("注意剂量率超标，请远离此处！");
            left = CommonUtils.getDrawable(getContext(), R.mipmap.home_alert_icon);
            if (mDosageAnimator != null) {
                mDosageAnimator.end();
            }
        } else {
            mDosageLayout.setBackground(null);
            mDosageView.setTextColor(CommonUtils.getColor(getContext(), R.color.white));
            mDosageTipView.setVisibility(View.INVISIBLE);
            mAlertTipView.setText("数值在范围内，" + User.getInstance().getBabyName() + "很安全哦！～");
            mAlertTipView.setTextColor(CommonUtils.getColor(getContext(), R.color.white));
            left = CommonUtils.getDrawable(getContext(), R.mipmap.ok_icon);
            if (isConnect) {
                mDosageImageView.setImageResource(R.mipmap.home_dynamic_dosage_icon);
                if (mDosageAnimator != null && !mDosageAnimator.isRunning()) {
                    mDosageAnimator.start();
                }
            } else {
                mDosageImageView.setImageResource(R.mipmap.home_static_dosage_icon);
                if (mDosageAnimator != null) {
                    mDosageAnimator.end();
                }
            }
            ss.setSpan(new ForegroundColorSpan(CommonUtils.getColor(getContext(), R.color.secondary)), 5, ss.length()
                    , Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        left.setBounds(0, 0, left.getIntrinsicWidth(), left.getIntrinsicHeight());
        mAlertTipView.setCompoundDrawables(left, null, null, null);
        mDosageView.setText(ss);
    }

    private void alertTotalDosage(String value, boolean isConnect) {
        float totalDosage = Float.parseFloat(value.substring(0, value.length() - 2));
        if (value.charAt(value.length() - 1) - 0x30 > 0) {
            totalDosage *= 1000;
        }
        if (value.charAt(value.length() - 1) - 0x30 > 1) {
            totalDosage *= 1000;
        }
        float scale = totalDosage / Device.getAlertTotalThreshold();
        int level = (int) (10000 * (0.855 * scale + 0.07246f));
        SpannableString ss = new SpannableString("总剂量\n" + translate(value) + "Sv");
        if (totalDosage >= Device.getAlertTotalThreshold()) {
            mTotalDosageLayout.setBackgroundResource(R.drawable.home_alert_bg);
            mTotalDosageImageView.setImageResource(R.mipmap.home_total_static_a_icon);
            mTotalDosageView.setTextColor(SkinManager.getInstance().getColor(R.color.theme_color));
            mTotalDosageTipView.setVisibility(View.VISIBLE);
        } else {
            mTotalDosageLayout.setBackground(null);
            Drawable drawable = isConnect ? mTotalDosageDynamicDrawable : mTotalDosageStaticDrawable;
            mTotalDosageImageView.setImageDrawable(drawable);
            drawable.setLevel(level);
            mTotalDosageView.setTextColor(CommonUtils.getColor(getContext(), R.color.white));
            mTotalDosageTipView.setVisibility(View.INVISIBLE);
            ss.setSpan(new ForegroundColorSpan(CommonUtils.getColor(getContext(), R.color.secondary)), 3, ss.length()
                    , Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
        }
        mTotalDosageView.setText(ss);
    }

    private void getTotalDosage() {
        DosageDao.getInstance().queryAll();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mMenuView = getView(R.id.action_menu);
        mReconnectView = getView(R.id.home_reconnect);
        mReconnectView.setOnClickListener(this);
        mTemperatureView = getView(R.id.home_thermometer);
        mBatteryView = getView(R.id.home_power);
        mAlertTipView = getView(R.id.home_safe_tip);

        mDosageLayout = getView(R.id.home_dosage_layout);
        mDosageView = getView(R.id.home_dosage);
        mDosageImageView = getView(R.id.home_dosage_image);
        mDosageTipView = getView(R.id.home_real_time_tip);

        mTotalDosageLayout = getView(R.id.home_total_dosage_layout);
        mTotalDosageView = getView(R.id.home_total_dosage);
        mTotalDosageImageView = getView(R.id.home_total_dosage_image);
        mTotalDosageTipView = getView(R.id.home_total_tip);
        mTotalDosageStaticDrawable = CommonUtils.getDrawable(BabyApp.getContext(), R.drawable.home_s_total_dosage_bg);
        mTotalDosageDynamicDrawable = new BottleDrawable(BitmapFactory.decodeResource(getResources(), R.drawable
                .home_d_total_dosage_down));

        mChartLeftView = getView(R.id.home_page_left);
        mChartLeftView.setOnClickListener(this);
        mChartRightView = getView(R.id.home_page_right);
        mChartRightView.setOnClickListener(this);
        mChartPagerView = getView(R.id.home_chart_pager);
        mChartPagerView.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                showView(position != 0, mChartLeftView);
                showView(position != mPageAdapter.getCount() - 1, mChartRightView);
            }
        });
        mPageAdapter = new Adapter(getContext(), null);
        mChartPagerView.setAdapter(mPageAdapter);
        mChartPagerView.setOffscreenPageLimit(3);
        ((MainActivity) getActivity()).addIgnoreView(mChartPagerView);

        if (Device.getLastDosage() > 0) {
            alertDosage(Device.getLastDosage() + " 0", false);
            alertTotalDosage(Device.getLastTotalDosage() + " 0", false);
            mCallback.call('D', null, String.valueOf(Device.getLastTemperature()));
            mCallback.call('E', null, String.valueOf(Device.getLastBattery()));
        }

        mDosageAnimator = ObjectAnimator.ofFloat(mDosageImageView, "rotation", 0, 360);
        mDosageAnimator.setDuration(10000);
        mDosageAnimator.setInterpolator(new LinearInterpolator(getContext(), null));
        mDosageAnimator.setRepeatCount(-1);

        ServiceManager.getInstance().addDeviceStateListener(this);
        ServiceManager.getInstance().addCallback("ABDESTUV", mCallback);
        ServiceManager.getInstance().addCallback(ServiceManager.SYNC_MODE, mCallback);
        mDialog = new DeviceDialog(getContext());
        mDialog.setOnClickOption(new DeviceDialog.onClickOption() {
            @Override
            public void onOption(BluetoothDevice device) {
                showLoadingDialog("正在绑定设备");
                ServiceManager.getInstance().connect(device.getName(), device.getAddress());
            }
        });
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                init();
            }
        }, 500);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.home_reconnect:
                init();
                break;
            case R.id.home_page_left:
                if (mChartPagerView.getCurrentItem() == 0) {
                    mChartLeftView.setVisibility(View.GONE);
                    return;
                }
                mChartPagerView.setCurrentItem(mChartPagerView.getCurrentItem() - 1, true);
                break;
            case R.id.home_page_right:
                if (mChartPagerView.getCurrentItem() >= mPageAdapter.getCount() - 1) {
                    mChartRightView.setVisibility(View.GONE);
                    return;
                }
                mChartPagerView.setCurrentItem(mChartPagerView.getCurrentItem() + 1, true);
                break;
            case R.id.home_chart_reload:
                reloadData();
                break;
            default:
        }
    }

    private void init() {
        if (!ServiceManager.getInstance().isConnected()) {
            String address = User.getInstance().getDeviceAddress();
            if (TextUtils.isEmpty(address)) {
                ServiceManager.getInstance().addScanListener(HomeFragment.this);
                ServiceManager.getInstance().startScan();
                showLoadingDialog("设备扫描中...");
                mHandler.sendEmptyMessageDelayed(WHAT_CLOSE, 60 * 1000);
            } else {
                showConnectTip(User.getInstance().getDeviceName(), address);
            }
        }
    }

    private void showConnectTip(final String name, final String address) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            if (mTipDialog != null) {
                mTipDialog.dismiss();
            }
            mTipDialog = new AlertDialog(getActivity());
            mTipDialog.setTitle("提示");
            mTipDialog.setMessage("是否连接设备?");
            mTipDialog.setOnClickOkListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    showLoadingDialog("设备连接中...");
                    ServiceManager.getInstance().connect(name, address);
                    ServiceManager.getInstance().addDeviceStateListener(HomeFragment.this);
                    mHandler.sendEmptyMessageDelayed(WHAT_CLOSE, 60 * 1000);
                }
            });
            mTipDialog.show();
        }
    }

    @Override
    public void onScanResult(BluetoothDevice device) {
        mList.add(device);
        mDialog.setList(mList);
        hideLoadingDialog();
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ServiceManager.getInstance().startSyncRealTimeData();
        if (!User.getInstance().getHistoryType().equals(mHistoryType)) {
            reloadData();
        }
        TimerManage.getInstance().startTimerTask("1234", 1000, new TimerManage.TimerTaskListener() {
            @Override
            public void onTimer(String tag, int count) {
                Log.d(TAG, "onTimer: tag=" + tag + " count=" + count);
            }
        });
        TimerManage.getInstance().stopTimerTask("1234");
    }

    private void reloadData() {
        mHistoryType = User.getInstance().getHistoryType();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<StatisticsBean> list;
                if ("周".equals(mHistoryType)) {
                    list = HomeDataPagerView.loadWeekData();
                } else if ("日".equals(mHistoryType)) {
                    list = HomeDataPagerView.loadDayData();
                } else {
                    list = HomeDataPagerView.loadMonthData();
                    if (list.size() > 1 && list.get(0).values.size() == 0) {
                        list.remove(0);
                    }
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        mPageAdapter = new Adapter(getActivity(), list);
                        mChartPagerView.setAdapter(mPageAdapter);
                        if (list.size() > 0) {
                            mChartPagerView.setCurrentItem(list.size() - 1, false);
                        }
                        mChartRightView.setVisibility(View.GONE);
                        showView(list.size() > 1, mChartLeftView);
                    }
                });
            }
        };
        ThreadPool.run(runnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        ServiceManager.getInstance().removeScanListener(this);
        ServiceManager.getInstance().removeDeviceStateListener(this);
    }

    @Override
    public void onConnectStatusChange(int status) {
        if (status == BluetoothService.STATE_CONNECTED) {
            mMenuView.setText("设备已连接");
            mReconnectView.setVisibility(View.GONE);
            alertDosage(Device.getLastDosage() + " 0", true);
            alertTotalDosage(Device.getLastTotalDosage() + " 0", true);
        } else if (status == BluetoothService.STATE_DISCONNECTED) {
            mMenuView.setText("设备已断开");
            mReconnectView.setVisibility(View.VISIBLE);
            alertDosage(Device.getLastDosage() + " 0", false);
            alertTotalDosage(Device.getLastTotalDosage() + " 0", false);
            hideLoadingDialog();
            showConnectTip(User.getInstance().getDeviceName(), User.getInstance().getDeviceAddress());
        }
    }

    @Override
    public void onDiscovered() {
        if (mTipDialog != null) {
            mTipDialog.dismiss();
        }
        ToastUtil.showTip(getContext(), "连接成功", "");
        mHandler.removeMessages(WHAT_CLOSE);
    }

    private String translate(String value) {
        if (TextUtils.isEmpty(value) || value.length() < 2) {
            return "";
        }
        String unit;
        if (value.charAt(value.length() - 1) == '2') {
            unit = "";
        } else if (value.charAt(value.length() - 1) == '1') {
            unit = "m";
        } else {
            unit = "μ";
        }
        return value.substring(0, value.length() - 1) + unit;
    }

    private class Adapter extends PagerAdapter {

        private Context mContext;
        private List<StatisticsBean> mList;
        private List<HomeDataPagerView> mPageList;

        public Adapter(Context context, List<StatisticsBean> list) {
            mContext = context;
            mPageList = new ArrayList<>(5);
            updateList(list);
        }


        public void updateList(List<StatisticsBean> list) {
            if (mList != list) {
                mList = list;
            }
            if (mList == null) {
                mList = new ArrayList<>();
            }
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            HomeDataPagerView view;
            if (mPageList.size() == 0) {
                view = new HomeDataPagerView(mContext, mList.get(position));
            } else {
                view = mPageList.remove(0);
            }
            view.updateData(mList.get(position));
            view.findViewById(R.id.home_chart_reload).setOnClickListener(HomeFragment.this);
            container.addView(view);
            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            if (object instanceof HomeDataPagerView) {
                HomeDataPagerView view = (HomeDataPagerView) object;
                container.removeView(view);
                mPageList.add(view);
            }
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
    }
}
