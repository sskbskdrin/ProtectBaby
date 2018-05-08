package com.junhao.baby.activity;

import android.content.Intent;
import android.os.Bundle;

import com.junhao.baby.bean.User;
import com.junhao.baby.utils.SpfUtil;

/**
 * Created by ex-keayuan001 on 2018/3/2.
 *
 * @author ex-keayuan001
 */
public class SplashActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        System.out.println("splash");
        Intent intent;
        if (User.isLogin()) {
            intent = new Intent(this, MainActivity.class);
        } else {
            intent = new Intent(this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
