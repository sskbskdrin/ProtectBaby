package com.junhao.baby.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.junhao.baby.R;
import com.junhao.baby.base.IBaseAdapter;
import com.junhao.baby.base.ViewHolder;
import com.junhao.baby.utils.CommonUtils;

import java.util.List;

import cn.feng.skin.manager.loader.SkinManager;

/**
 * Created by sskbskdrin on 2017/9/22.
 */
public class ListPopupWindow<T extends ShowSequence> extends IPopupWindow implements AdapterView
        .OnItemClickListener {

    private OnSelectListener<T> mOnSelectListener;
    private List<T> mList;
    private int mCurrentSelect = -1;

    public ListPopupWindow(Context context, List<T> list) {
        super(new ListView(context));
        mList = list;
        ListView listView = (ListView) mRootView;
        ViewGroup.LayoutParams layoutParams = listView.getLayoutParams();
        if (layoutParams == null) {
            layoutParams = new ViewGroup.LayoutParams(-1, -2);
        }
        layoutParams.width = CommonUtils.dp2px(context, 200);
        layoutParams.height = -2;
        listView.setBackgroundResource(R.drawable.popup_bg);
        listView.setLayoutParams(layoutParams);
        listView.setDivider(new ColorDrawable(context.getResources().getColor(R.color.white)));
        listView.setDividerHeight(CommonUtils.dp2px(context, 1));

        setBackgroundColor(0x01000000);
        listView.setAdapter(new IBaseAdapter<T>(context, mList, R.layout.item_popup_list) {
            @Override
            public void bindViewHolder(ViewHolder holder, T item) {
                TextView name = holder.getView(R.id.popup_name);
                holder.itemView.getLayoutParams().width = -1;
                name.setText(item.toCharSequence());

                boolean isSelect = holder.position() == mCurrentSelect;
                showView(isSelect, holder.getView(R.id.popup_icon));
                name.setTextColor(SkinManager.getInstance().getColor(isSelect ? R.color
                        .theme_color : R.color
                        .font_major));
            }
        });
        listView.setOnItemClickListener(this);
    }

    public void setOnSelectListener(OnSelectListener<T> listener) {
        mOnSelectListener = listener;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mCurrentSelect != position) {
            mCurrentSelect = position;
            if (mOnSelectListener != null) {
                mOnSelectListener.onSelect(this, mList.get(position));
            }
        }
        dismiss();
    }

    public void setCurrentSelect(int select) {
        mCurrentSelect = select;
    }

    public interface OnSelectListener<T> {
        void onSelect(IPopupWindow window, T select);
    }
}
