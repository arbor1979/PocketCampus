package com.ruanyun.campus.teacher.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.ruanyun.campus.teacher.R;


/**
 * Created by lyd10892 on 2016/8/23.
 */

public class HeaderHolder extends RecyclerView.ViewHolder {
    public TextView titleView;
    public TextView openView;
    public HeaderHolder(View itemView) {
        super(itemView);
        initView();
    }

    private void initView() {
        titleView = (TextView) itemView.findViewById(R.id.tv_title);
        openView = (TextView) itemView.findViewById(R.id.tv_open);
    }
}