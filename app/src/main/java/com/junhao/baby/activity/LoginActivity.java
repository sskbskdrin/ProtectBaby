package com.junhao.baby.activity;

import android.Manifest;
import android.app.DatePickerDialog;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.bean.User;
import com.junhao.baby.db.ThreadPool;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.utils.DateFormatUtil;
import com.junhao.baby.utils.ToastUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

import cn.feng.skin.manager.util.L;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends BaseActivity {
    private static final String TAG = "LoginActivity";

    private ImageView mHeadView;
    private TextView mCommitView;
    private EditText mNameView;
    private EditText mBabyNameView;
    private TextView mDateView;
    private long mConceiveDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mNameView = getView(R.id.login_name);
        mBabyNameView = getView(R.id.login_baby_name);
        mHeadView = getView(R.id.login_head);
        mHeadView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE,
                        STORAGE_PERMISSION_REQUEST_CODE)) {
                    getLocalPicture();
                }
            }
        });
        mDateView = getView(R.id.login_conceive_time);
        mDateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar c = Calendar.getInstance();
                DatePickerDialog dialog = new DatePickerDialog(mContext, 0, new DatePickerDialog
                        .OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        Calendar date = Calendar.getInstance();
                        date.set(year, month, dayOfMonth, 0, 0, 0);
                        mConceiveDate = date.getTimeInMillis();
                        mDateView.setText(DateFormatUtil.format(mConceiveDate, DateFormatUtil
                                .YYMD));
                    }
                }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH));
                DatePicker picker = dialog.getDatePicker();
                picker.setMaxDate(System.currentTimeMillis());
                c.add(Calendar.HOUR, -280 * 24);
                picker.setMinDate(c.getTimeInMillis());
                dialog.show();
            }
        });
        mCommitView = getView(R.id.login_commit);
        mCommitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = mNameView.getText().toString().trim();
                String babyName = mBabyNameView.getText().toString().trim();
                if (TextUtils.isEmpty(name) || TextUtils.isEmpty(babyName)) {
                    ToastUtil.show(mContext, "姓名不能为空");
                    return;
                }
                if (mConceiveDate <= 0) {
                    ToastUtil.show(mContext, "请选择受孕日期");
                    return;
                }
                User.getInstance().setName(name);
                User.getInstance().setBabyName(babyName);
                User.getInstance().setConceiveDate(mConceiveDate);

                User.login();
                mCommitView.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    }
                }, 1000);
                ToastUtil.showTip(mContext, "提交成功", "欢迎使用RAMO-C");
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
                        L.e(TAG, "file is null path=" + path);
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

