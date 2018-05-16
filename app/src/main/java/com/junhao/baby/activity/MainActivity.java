package com.junhao.baby.activity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.view.View;

import com.junhao.baby.R;
import com.junhao.baby.db.ThreadPool;
import com.junhao.baby.service.ServiceManager;
import com.junhao.baby.utils.TimerManage;
import com.junhao.baby.utils.ToastUtil;
import com.junhao.baby.view.slidingmenu.SlidingMenu;
import com.junhao.baby.widget.BottleDrawable;

/**
 * Created by ex-keayuan001 on 2018/3/2.
 *
 * @author ex-keayuan001
 */
public class MainActivity extends BaseActivity {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    private SlidingMenu mSlidingMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TimerManage.getInstance().stopTimerTask("exit");
        setContentView(R.layout.activity_main);
        initSlidingMenu();
    }

    private void initSlidingMenu() {
        FragmentManager manager = getSupportFragmentManager();
        mSlidingMenu = new SlidingMenu(mContext);
        mSlidingMenu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        mSlidingMenu.setShadowWidthRes(R.dimen.home_shadow_width);
        mSlidingMenu.setShadowDrawable(R.drawable.home_menu_shadow);
        mSlidingMenu.setBehindOffsetRes(R.dimen.home_menu_offset);
        mSlidingMenu.setFadeDegree(0.35f);
        mSlidingMenu.attachToActivity(this, SlidingMenu.SLIDING_WINDOW);
        mSlidingMenu.setMenu(R.layout.main_menu_layout);
        manager.beginTransaction().replace(R.id.main_content, new HomeFragment(), "main_content").commit();
        manager.beginTransaction().replace(R.id.main_menu_content, new MenuFragment(), "main_menu").commit();
    }

    @Override
    protected void onClickLeft() {
        mSlidingMenu.showMenu(true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        System.out.println("main");
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            ToastUtil.show(this, "蓝牙不支持");
            finish();
        }
        if (!checkBluetooth()) {
            finish();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager
                .PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    REQUEST_LOCATION_PERMISSION);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (mSlidingMenu.isMenuShowing()) {
            mSlidingMenu.toggle();
            return;
        }
        super.onBackPressed();
    }

    private boolean checkBluetooth() {
        final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();  //获取蓝牙适配器
        if (bluetoothAdapter != null) {  //有蓝牙功能
            if (!bluetoothAdapter.isEnabled()) {  //蓝牙未开启
                ThreadPool.run(new Runnable() {
                    @Override
                    public void run() {
                        bluetoothAdapter.enable();  //开启蓝牙（还有一种方法开启，我就不说了，自己查去）
                    }
                });
            }
            return true;
        } else {  //无蓝牙功能
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[]
        grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION_PERMISSION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                }
                break;
        }
    }

    public void addIgnoreView(View view) {
        mSlidingMenu.addIgnoredView(view);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ServiceManager.getInstance().unBindDevice();
        TimerManage.getInstance().startTimerTask("exit", 60000, new TimerManage.TimerTaskListener() {
            @Override
            public void onTimer(String tag, int count) {
                System.exit(0);
            }
        });
    }
}
