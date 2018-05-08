package com.junhao.baby.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.bean.User;
import com.junhao.baby.utils.DateFormatUtil;

import java.io.File;
import java.util.Calendar;

/**
 * Created by ex-keayuan001 on 2018/3/2.
 *
 * @author ex-keayuan001
 */
public class MenuFragment extends BaseFragment implements View.OnClickListener {

    private ImageView mHeadView;
    private TextView mNameView;
    private TextView mConceiveWeekView;
    private TextView mPreDateView;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_menu;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mHeadView = getView(R.id.menu_head);
        mNameView = getView(R.id.menu_name);
        mConceiveWeekView = getView(R.id.menu_week);
        mPreDateView = getView(R.id.menu_pre_product);

        getView(R.id.menu_alert_layout).setOnClickListener(this);
        getView(R.id.menu_bg_time_layout).setOnClickListener(this);
        getView(R.id.menu_history_layout).setOnClickListener(this);
        getView(R.id.menu_about_layout).setOnClickListener(this);
        getView(R.id.menu_device_layout).setOnClickListener(this);
        getView(R.id.menu_person_layout).setOnClickListener(this);
    }

    private void init() {
        mNameView.setText(User.getInstance().getName());

        long week = (System.currentTimeMillis() - User.getInstance().getConceiveDate()) /
                SettingActivity.WEEK;
        mConceiveWeekView.setText("怀孕周数: " + week + "周");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(User.getInstance().getConceiveDate());
        calendar.add(Calendar.HOUR, 280 * 24);

        mPreDateView.setText("预产期: " + DateFormatUtil.format(calendar.getTime(), "yyyy.MM.dd"));
    }

    @Override
    public void onResume() {
        init();
        updateHead();
        super.onResume();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.menu_alert_layout:
                startActivity(new Intent(getActivity(), AlertActivity.class));
                break;
            case R.id.menu_bg_time_layout:
                startActivity(new Intent(getActivity(), BgTimeActivity.class));
                break;
            case R.id.menu_history_layout:
                startActivity(new Intent(getActivity(), HistoryActivity.class));
                break;
            case R.id.menu_about_layout:
                startActivity(new Intent(getActivity(), AboutActivity.class));
                break;
            case R.id.menu_device_layout:
                startActivity(new Intent(getActivity(), DeviceActivity.class));
                break;
            case R.id.menu_person_layout:
                startActivity(new Intent(getActivity(), SettingActivity.class));
                break;
            default:
                break;
        }
    }

    private void updateHead() {
        File file = new File(User.getInstance().getHeadPath(getActivity()));
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(User.getInstance().getHeadPath(getActivity()));
            mHeadView.setImageBitmap(bitmap);
        } else {
            mHeadView.setImageResource(R.mipmap.default_head);
        }
    }
}
