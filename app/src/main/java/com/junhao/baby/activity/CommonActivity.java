package com.junhao.baby.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.base.IBaseAdapter;
import com.junhao.baby.base.ViewHolder;

import java.util.List;

/**
 * Created by sskbskdrin on 2018/三月/4.
 */

public abstract class CommonActivity<T> extends BaseActivity {

    protected CommonAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_common);
        initData();
        ListView listView = getView(R.id.common_list);
        mAdapter = new CommonAdapter(this, getList());
        listView.setAdapter(mAdapter);
        initView();
    }

    protected void initData() {
    }

    protected abstract void initView();

    protected void setLogoImage(@DrawableRes int resId) {
        ImageView view = getView(R.id.common_image);
        view.setImageResource(resId);
    }

    protected void setTipText(CharSequence text) {
        TextView view = getView(R.id.common_tip);
        view.setText(text);
    }

    protected abstract List<T> getList();

    protected abstract void getItemView(ViewHolder holder, T item);

    protected void notifyDataSetChanged() {
        mAdapter.updateList(getList());
    }

    protected class CommonAdapter extends IBaseAdapter<T> {

        public CommonAdapter(Context context, List<T> list) {
            super(context, list, R.layout.item_common);
        }

        @Override
        public void bindViewHolder(ViewHolder holder, T item) {
            getItemView(holder, item);
        }
    }
}
