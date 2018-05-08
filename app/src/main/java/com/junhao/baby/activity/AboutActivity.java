package com.junhao.baby.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.junhao.baby.R;
import com.junhao.baby.base.ViewHolder;
import com.junhao.baby.bean.DeviceBean;
import com.junhao.baby.download.DownloadManager;
import com.junhao.baby.download.IDownloadListener;
import com.junhao.baby.service.FirmwareUpdate;
import com.junhao.baby.service.ServiceManager;
import com.junhao.baby.service.SyncHistoryData;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.utils.L;
import com.junhao.baby.utils.ToastUtil;
import com.junhao.baby.widget.AlertDialog;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public class AboutActivity extends CommonActivity<DeviceBean> {
    private static final String TAG = "AboutActivity";
    private final List<DeviceBean> mList = new ArrayList<>();
    private int maxVersion = 0;
    private String binName;

    private ServiceManager.Callback mCallback = new ServiceManager.Callback() {
        @Override
        public void call(char key, byte[] data, String value) {
            if ('Q' == key) {
                if ("OK".equalsIgnoreCase(value)) {
                }
                if (mLoadingDialog != null) {
                    mLoadingDialog.setBack(true);
                }
            } else if (ServiceManager.UPDATE_MODE == key) {
                if (data[1] == 0x00) {
                    int version = 0xff & data[3];
                    version = version << 8 | (0xff & data[4]);
                    version = version << 8 | (0xff & data[5]);
                    version = version << 8 | (0xff & data[6]);
                    if (version < maxVersion) {
                        if (data[2] <= 30) {
                            hideLoadingDialog();
                            ToastUtil.show(mContext, "电量过低，无法更新");
                        } else {
                            showLoadingDialog("正在更新，请勿退出");
                            return;
                        }
                    } else {
                        hideLoadingDialog();
                        ToastUtil.show(mContext, "固件已是最新版本");
                    }
                } else if (data[1] == 0x06) {
                    hideLoadingDialog();
                    String reason = "数据异常";
                    switch (0xff & data[2]) {
                        case 0x00:
                            reason = "数据丢失";
                            break;
                        case 0x01:
                            return;
                        case 0x02:
                            reason = "数据溢出";
                            break;
                        case 0x03:
                            reason = "超时退出";
                            break;
                        case 0x04:
                            reason = "数据错误";
                            break;
                    }
                    ToastUtil.showTip(mContext, "更新失败，" + reason, "");
                } else if (data[1] == 0x09) {
                    hideLoadingDialog();
                    mList.clear();
                    notifyDataSetChanged();
                    ToastUtil.showTip(mContext, "更新成功", "");
                }
                ServiceManager.getInstance().startSyncRealTimeData();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ServiceManager.getInstance().addCallback(ServiceManager.UPDATE_MODE, mCallback);
        ServiceManager.getInstance().addCallback('Q', mCallback);
        ServiceManager.getInstance().pauseSyncRealTimeData();
    }

    @Override
    protected void initView() {
        setTitle("关于我们");
        setLogoImage(R.mipmap.about_logo);
        setTipText(getString(R.string.app_name) + " v" + CommonUtils.getVersionName(mContext));
    }

    @Override
    protected void initData() {
        mList.add(new DeviceBean());
    }

    @Override
    protected List<DeviceBean> getList() {
        return mList;
    }

    @Override
    public void onBackPressed() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            AlertDialog mTipDialog = new AlertDialog(this);
            mTipDialog.setTitle("提示");
            mTipDialog.setMessage("返回将停止更新，是否返回?");
            mTipDialog.setOnClickOkListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    hideLoadingDialog();
                    finish();
                }
            });
            mTipDialog.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void getItemView(ViewHolder holder, DeviceBean item) {
        holder.setImageResource(R.id.item_common_icon, R.mipmap.hard_update_icon);
        holder.setText(R.id.item_common_title, "固件升级");
        holder.setText(R.id.item_common_name, "检查更新固件");
        showView(true, holder.getView(R.id.item_common_icon), holder.getView(R.id
                .item_common_title), holder.getView
                (R.id.item_common_name), holder.getView(R.id.item_common_arrow));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SyncHistoryData.isSyncMode) {
                    ToastUtil.show(mContext, "正在同步数据，无法更新");
                } else {
                    ServiceManager.getInstance().pauseSyncRealTimeData();
                    query();
                }
            }
        });
    }

    private void query() {
        DownloadManager.getInstance().addDlTask("query",
                "http://106.37.108.30:5001/release/medical/msg.html", "", new IDownloadListener() {
                    public void onDlStart(String tag) {
                        showLoadingDialog("正在检查固件");
                    }

                    @Override
                    public void onDlCompleted(String tag, byte[] data, int length) {
                        String result = new String(data);
                        L.d(TAG, result);
                        try {
                            JSONObject object = new JSONObject(result);
                            maxVersion = Integer.parseInt(object.optString("version", "0"));
                            binName = object.optString("upgradefile");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (maxVersion > 0 && ServiceManager.getInstance().isConnected()) {
                            downloadFile();
                        } else {
                            onDlError(tag, 0);
                        }
                    }

                    @Override
                    public void onDlError(String tag, int errorCode) {
                        hideLoadingDialog();
                        ToastUtil.show(mContext, "固件已是最新版本");
                    }
                });
        DownloadManager.getInstance().startDlTask("query");
    }

    private void downloadFile() {
        DownloadManager.getInstance().addDlTask("download",
                "http://106.37.108.30:5001/release/medical/" + binName, "", new IDownloadListener
                        () {

                    @Override
                    public void onDlStart(String tag) {
                    }

                    @Override
                    public void onDlCompleted(String tag, byte[] data, int length) {
                        File file = new File(getFilesDir(), binName);
                        if (file.exists()) {
                            file.delete();
                        }
                        try {
                            file.createNewFile();
                            FileOutputStream fos = new FileOutputStream(file);
                            fos.write(data);
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "onDlCompleted: " + file.length());
                        if (maxVersion > 0 && ServiceManager.getInstance().isConnected()) {
                            FirmwareUpdate.getInstance().startUpdate(file, maxVersion);
                        } else {
                            onDlError(tag, 0);
                        }
                    }

                    @Override
                    public void onDlError(String tag, int errorCode) {
                        hideLoadingDialog();
                        ToastUtil.show(mContext, "固件已是最新版本");
                    }
                });
        DownloadManager.getInstance().startDlTask("download");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirmwareUpdate.getInstance().stopUpdate();
        if (ServiceManager.getInstance().isConnected()) {
            ServiceManager.getInstance().startSyncRealTimeData();
        }
    }
}
