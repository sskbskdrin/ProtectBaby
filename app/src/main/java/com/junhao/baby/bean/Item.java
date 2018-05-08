package com.junhao.baby.bean;

import com.junhao.baby.widget.ShowSequence;

/**
 * Created by sskbskdrin on 2018/三月/10.
 */

public class Item implements ShowSequence {

    public String name;
    public int type;

    public Item() {
        this(null);
    }

    public Item(String name) {
        this(name, -1);
    }

    public Item(String name, int type) {
        this.name = name;
        this.type = type;
    }

    @Override
    public CharSequence toCharSequence() {
        return name;
    }
}
