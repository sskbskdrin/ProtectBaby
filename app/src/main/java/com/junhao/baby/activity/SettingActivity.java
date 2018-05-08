package com.junhao.baby.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.bean.User;
import com.junhao.baby.db.ThreadPool;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.utils.DateFormatUtil;
import com.junhao.baby.utils.L;
import com.junhao.baby.utils.ToastUtil;
import com.junhao.baby.widget.InputDialog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import cn.feng.skin.manager.listener.ILoaderListener;
import cn.feng.skin.manager.loader.SkinManager;

/**
 * Created by ex-keayuan001 on 2018/3/2.
 *
 * @author ex-keayuan001
 */
public class SettingActivity extends BaseActivity implements View.OnClickListener {
    private static final String TAG = "SettingActivity";

    private static final long SECOND = 1000;
    private static final long MINUTE = 60 * SECOND;
    private static final long HOUR = 60 * MINUTE;
    private static final long DAY = 24 * HOUR;
    public static final long WEEK = 7 * DAY;

    private ImageView mHeadView;
    private TextView mNameView;
    private TextView mBabyNameView;
    private TextView mConceiveWeekView;
    private TextView mPreDateView;
    private TextView mConceiveDateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        mHeadView = getView(R.id.setting_head);
        mNameView = getView(R.id.setting_name);
        mBabyNameView = getView(R.id.setting_baby_name);
        mConceiveWeekView = getView(R.id.setting_week);
        mPreDateView = getView(R.id.setting_pre_date);
        mConceiveDateView = getView(R.id.setting_conceive_date);

        mHeadView.setOnClickListener(this);
        mNameView.setOnClickListener(this);
        mBabyNameView.setOnClickListener(this);
        mConceiveDateView.setOnClickListener(this);
        CommonUtils.copyDBToSdcard(this, "baby_db.db");
        initView();
        updateHead();
    }

    private void initView() {
        mNameView.setText(User.getInstance().getName());
        mBabyNameView.setText(User.getInstance().getBabyName());

        long week = (System.currentTimeMillis() - User.getInstance().getConceiveDate()) / WEEK;
        mConceiveWeekView.setText(week + "周");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(User.getInstance().getConceiveDate());
        mConceiveDateView.setText(DateFormatUtil.format(calendar.getTime(), DateFormatUtil.YYMD));
        calendar.add(Calendar.HOUR, 280 * 24);
        mPreDateView.setText(DateFormatUtil.format(calendar.getTime(), DateFormatUtil.YYMD));

        RadioGroup group = getView(R.id.setting_theme_group);

        if ("Blue".equals(User.getInstance().getThemeName())) {
            group.check(R.id.setting_theme_blue);
        } else if ("Black".equals(User.getInstance().getThemeName())) {
            group.check(R.id.setting_theme_black);
        } else {
            group.check(R.id.setting_theme_red);
        }
        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.setting_theme_black) {
                    loadSkin("Black");
                } else if (checkedId == R.id.setting_theme_blue) {
                    loadSkin("Blue");
                } else {
                    loadSkin("Red");
                }
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.setting_head:
                if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                        STORAGE_PERMISSION_REQUEST_CODE)) {
                    getLocalPicture();
                }
                break;
            case R.id.setting_name:
                InputDialog dialog = new InputDialog(this);
                dialog.setTitle("请填写您的姓名");
                dialog.setMessage(User.getInstance().getName());
                dialog.setOnClickOkListener(new InputDialog.OnClickOkListener() {
                    @Override
                    public void onClickOk(DialogInterface dialog, String content) {
                        if (TextUtils.isEmpty(content)) {
                            ToastUtil.show(mContext, "姓名不能为空");
                            return;
                        }
                        User.getInstance().setName(content);
                        mNameView.setText(content);
                        dialog.dismiss();
                    }
                });
                dialog.show();
                break;
            case R.id.setting_baby_name:
                InputDialog babyDialog = new InputDialog(this);
                babyDialog.setMessage(User.getInstance().getBabyName());
                babyDialog.setTitle("请填写宝宝昵称");
                babyDialog.setOnClickOkListener(new InputDialog.OnClickOkListener() {
                    @Override
                    public void onClickOk(DialogInterface dialog, String content) {
                        if (TextUtils.isEmpty(content)) {
                            ToastUtil.show(mContext, "姓名不能为空");
                            return;
                        }
                        User.getInstance().setBabyName(content);
                        mBabyNameView.setText(content);
                        dialog.dismiss();
                    }
                });
                babyDialog.show();
                break;
            case R.id.setting_conceive_date:
                showConceiveDate();
                break;
        }
    }

    private void showConceiveDate() {
        Calendar c = Calendar.getInstance();
        DatePickerDialog dialog = new DatePickerDialog(mContext, 0, new DatePickerDialog
                .OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar date = Calendar.getInstance();
                date.set(year, month, dayOfMonth, 0, 0, 0);
                User.getInstance().setConceiveDate(date.getTimeInMillis());
                initView();
            }
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
        DatePicker picker = dialog.getDatePicker();
        picker.setMaxDate(System.currentTimeMillis());
        c.add(Calendar.HOUR, -280 * 24);
        picker.setMinDate(c.getTimeInMillis());
        dialog.show();
    }

    private void loadSkin(String name) {
        User.getInstance().setThemeName(name);
        if ("Red".equals(name)) {
            SkinManager.getInstance().restoreDefaultTheme();
            return;
        }
        File skin = new File(getFilesDir().getPath() + "/" + name + ".skin");
        SkinManager.getInstance().load(skin.getAbsolutePath(),
                new ILoaderListener() {
                    @Override
                    public void onStart() {
                        L.d(TAG, "startloadSkin");
                    }

                    @Override
                    public void onSuccess() {
                        L.d(TAG, "loadSkinSuccess");
                    }

                    @Override
                    public void onFailed() {
                        L.e(TAG, "loadSkinFail");
                    }
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_PICTURE_CODE:
                    Uri uri = data.getData();
                    final String path = CommonUtils.getImageAbsolutePath(this, uri);
                    final File filePhoto = new File(path);
                    if (!filePhoto.exists()) {
                        cn.feng.skin.manager.util.L.e(TAG, "file is null path=" + path);
                        ToastUtil.show(this, "文件不存在");
                        return;
                    }
                    ThreadPool.run(new Runnable() {
                        @Override
                        public void run() {
                            Bitmap bitmap = BitmapFactory.decodeFile(path);
                            int width = bitmap.getWidth();
                            int height = bitmap.getHeight();
                            int size = Math.min(width, height);
                            float scale = ((float) CommonUtils.dp2px(mContext, 100)) / size;
                            Matrix matrix = new Matrix();
                            matrix.postScale(scale, scale);
                            Bitmap temp = Bitmap.createBitmap(bitmap, (width - size) / 2,
                                    (height - size) / 2, size, size, matrix, true);

                            size = temp.getWidth();
                            Bitmap result = Bitmap.createBitmap(size, size, Bitmap.Config
                                    .ARGB_8888);
                            Canvas canvas = new Canvas(result);
                            Paint paint = new Paint();
                            paint.setShader(new BitmapShader(temp, BitmapShader.TileMode
                                    .CLAMP, BitmapShader.TileMode.CLAMP));
                            paint.setAntiAlias(true);
                            float r = size / 2f;
                            canvas.drawCircle(r, r, r, paint);
                            try {
                                File filePic = new File(User.getInstance().getHeadPath(mContext));
                                if (!filePic.exists()) {
                                    filePic.createNewFile();
                                }
                                FileOutputStream fos = new FileOutputStream(filePic);
                                result.compress(Bitmap.CompressFormat.PNG, 100, fos);
                                fos.flush();
                                fos.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } finally {
                                temp.recycle();
                                bitmap.recycle();
                                result.recycle();
                            }
                            mHeadView.post(new Runnable() {
                                @Override
                                public void run() {
                                    updateHead();
                                }
                            });
                        }
                    });
                default:
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        for (int i = 0; i < permissions.length; i++) {
            int result = grantResults[i];
            String permission = permissions[i];
            switch (requestCode) {
                case STORAGE_PERMISSION_REQUEST_CODE:
                    if (result == PackageManager.PERMISSION_GRANTED) {
                        getLocalPicture();
                    } else {
                        showToast("请开启相关权限，否则无法获取本地图片！", true);
                    }
                    break;
                default:
            }
        }
    }

    private void updateHead() {
        File file = new File(User.getInstance().getHeadPath(this));
        if (file.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(User.getInstance().getHeadPath(this));
            mHeadView.setImageBitmap(bitmap);
        } else {
            mHeadView.setImageResource(R.mipmap.default_head);
        }
    }
}
