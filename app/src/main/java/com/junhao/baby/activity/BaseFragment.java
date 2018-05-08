package com.junhao.baby.activity;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.junhao.baby.R;
import com.junhao.baby.utils.CommonUtils;
import com.junhao.baby.widget.LoadingDialog;

/**
 * Created by ex-keayuan001 on 2018/3/2.
 *
 * @author ex-keayuan001
 */
public abstract class BaseFragment extends Fragment {

    protected View mRootView;
    private LoadingDialog mLoadingDialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(getLayoutId(), null);
        }
        ViewGroup parent = (ViewGroup) mRootView.getParent();
        if (parent != null) {
            parent.removeView(mRootView);
        }
        View actionbar = mRootView.findViewById(R.id.status_bar_view);
        if (actionbar != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                actionbar.getLayoutParams().height = CommonUtils.getStatusHeight(getContext());
            } else {
                actionbar.getLayoutParams().height = 0;
            }
        }
        return mRootView;
    }

    @Override
    public LayoutInflater getLayoutInflater(Bundle savedInstanceState) {
        return getActivity().getLayoutInflater();
    }

    protected abstract int getLayoutId();

    @SuppressWarnings("unchecked")
    protected <T extends View> T getView(int id) {
        return (T) mRootView.findViewById(id);
    }

    protected void showLoadingDialog(String content) {
        Activity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            if (mLoadingDialog == null) {
                mLoadingDialog = new LoadingDialog(getActivity());
            }
            if (!isDetached()) {
                mLoadingDialog.setMessage(content);
                if (!mLoadingDialog.isShowing()) {
                    mLoadingDialog.show();
                }
            }
        }
    }

    protected void hideLoadingDialog() {
        if (mLoadingDialog != null && mLoadingDialog.isShowing()) {
            mLoadingDialog.dismiss();
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

}
