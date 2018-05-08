package com.junhao.baby.activity;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.LayoutRes;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.utils.ToastUtil;
import com.junhao.baby.widget.LoadingDialog;

import java.lang.reflect.Field;
import java.util.List;

import cn.feng.skin.manager.entity.DynamicAttr;
import cn.feng.skin.manager.listener.IDynamicNewView;
import cn.feng.skin.manager.listener.ISkinUpdate;
import cn.feng.skin.manager.loader.SkinInflaterFactory;
import cn.feng.skin.manager.loader.SkinManager;

/**
 * @author ex-keayuan001
 * @date 17/10/19
 */
public class BaseActivity extends FragmentActivity implements ISkinUpdate, IDynamicNewView {

    protected static final int REQUEST_PICTURE_CODE = 1010;
    protected static final int STORAGE_PERMISSION_REQUEST_CODE = 3001;

    protected Context mContext;
    protected int screenWidth;
    protected int screenHeight;
    private boolean isStop;
    protected LoadingDialog mLoadingDialog;

    private SkinInflaterFactory mSkinInflaterFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme);
        mContext = this;
        try {
            Field field = LayoutInflater.class.getDeclaredField("mFactorySet");
            field.setAccessible(true);
            field.setBoolean(getLayoutInflater(), false);

            mSkinInflaterFactory = new SkinInflaterFactory();
            getLayoutInflater().setFactory(mSkinInflaterFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        fullScreen();
    }

    /**
     * 5.0以上开启沉浸式
     */
    private void fullScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
// View
//                .SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);// calculateStatusColor(Color.WHITE,
            // (int) alphaValue)
        }
    }

    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        if (this instanceof MainActivity) {
            View actionbar = findViewById(R.id.status_bar_view);
            if (actionbar != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    actionbar.getLayoutParams().height = CommonUtils.getStatusHeight(this);
                } else {
                    actionbar.getLayoutParams().height = 0;
                }
            }
        }
    }

    protected void setMenuText(String text) {
        TextView view = getView(R.id.action_menu);
        if (TextUtils.isEmpty(text)) {
            showView(false, view);
        } else {
            showView(true, view);
            view.setText(text);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onClickMenu();
                }
            });
        }
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            onClickLeft();
        }
    };

    @Override
    protected void onStart() {
        isStop = false;
        super.onStart();
        View view = findViewById(R.id.back);
        if (view != null) {
            view.setOnClickListener(onBackClickListener);
        }
    }

    @Override
    public void setTitle(CharSequence title) {
        TextView view = (TextView) findViewById(R.id.action_title);
        if (view != null) {
            view.setText(title);
        }
    }

    protected void onClickLeft() {
        onBackPressed();
    }

    protected void onClickMenu() {
    }

    @Override
    protected void onResume() {
        isStop = false;
        super.onResume();
        SkinManager.getInstance().attach(this);
    }

    @Override
    protected void onStop() {
        isStop = true;
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SkinManager.getInstance().detach(this);
    }

    public boolean isStop() {
        return isStop;
    }

    protected void showLoadingDialog(int resId) {
        showLoadingDialog(getString(resId));
    }

    protected void showLoadingDialog(String content) {
        if (mLoadingDialog == null) {
            mLoadingDialog = new LoadingDialog(this);
        }
        if (!isFinishing()) {
            mLoadingDialog.setMessage(content);
            mLoadingDialog.show();
        }
    }

    protected void hideLoadingDialog() {
        if (mLoadingDialog != null) {
            mLoadingDialog.dismiss();
        }
        mLoadingDialog = null;
    }

    protected void showToast(String text) {
        showToast(text, false);
    }

    protected void showToast(String text, boolean isLong) {
        if (!isFinishing() && !TextUtils.isEmpty(text)) {
            ToastUtil.show(this, text, isLong);
        }
    }

    protected void toActivity(Class<?> clazz, Bundle bundle) {
        Intent intent = new Intent(this, clazz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    protected void setText(int viewId, int resId) {
        TextView view = getView(viewId);
        if (view != null && resId > 0) {
            view.setText(resId);
        }
    }

    protected static void setText(TextView view, CharSequence text) {
        if (view != null) {
            view.setText(text != null ? text : "");
        }
    }

    protected static void showView(boolean show, View... views) {
        if (views != null) {
            int visible = show ? View.VISIBLE : View.GONE;
            for (View view : views) {
                view.setVisibility(visible);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends View> T getView(int id) {
        return (T) findViewById(id);
    }

    @SuppressWarnings("unchecked")
    protected static <T extends View> T getView(View parent, int id) {
        if (parent != null) {
            return (T) parent.findViewById(id);
        }
        return null;
    }

    protected boolean checkPermission(String[] permission, int requestCode) {
        boolean flag = true;
        for (String str : permission) {
            if (!checkPermission(str, requestCode)) {
                flag = false;
            }
        }
        if (!flag && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permission, requestCode);
        }
        return flag;
    }

    protected boolean checkPermission(String permission, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    if (Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission) || Manifest
                            .permission
                            .WRITE_EXTERNAL_STORAGE.equals(permission)) {
                        showToast("请开启存储权限，否则无法使用该功能！", true);
                    } else if (Manifest.permission.RECORD_AUDIO.equals(permission)) {
                        showToast("请开启录音权限，否则无法使用该功能！", true);
                    } else if (Manifest.permission.CAMERA.equals(permission)) {
                        showToast("请开启相机权限，否则无法使用拍照功能！", true);
                    }
                } else {
                    requestPermissions(new String[]{permission}, requestCode);
                }
                return false;
            }
        }
        return true;
    }

    protected void dynamicAddSkinEnableView(View view, String attrName, int attrValueResId) {
        mSkinInflaterFactory.dynamicAddSkinEnableView(this, view, attrName, attrValueResId);
    }

    protected void dynamicAddSkinEnableView(View view, List<DynamicAttr> pDAttrs) {
        mSkinInflaterFactory.dynamicAddSkinEnableView(this, view, pDAttrs);
    }

    @Override
    public void dynamicAddView(View view, List<DynamicAttr> pDAttrs) {

    }

    @Override
    public void onThemeUpdate() {
        mSkinInflaterFactory.applySkin();
    }

    protected void getLocalPicture() {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
        startActivityForResult(intent, REQUEST_PICTURE_CODE);
    }
}
