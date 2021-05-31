package com.ruanyun.campus.teacher.stickyheader;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;


import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.activity.SchoolActivity;
import com.ruanyun.campus.teacher.activity.SchoolDetailActivity;
import com.ruanyun.campus.teacher.activity.WebSiteActivity;
import com.ruanyun.campus.teacher.base.Constants;
import com.ruanyun.campus.teacher.stickyheader.adapter.HeaderChannelAdapter;
import com.ruanyun.campus.teacher.stickyheader.model.ChannelEntity;
import com.ruanyun.campus.teacher.stickyheader.util.ToastUtil;
import com.ruanyun.campus.teacher.util.AppUtility;
import com.ruanyun.campus.teacher.util.Base64;
import com.ruanyun.campus.teacher.util.PrefUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sunfusheng on 16/4/20.
 */
public class HeaderChannelView extends AbsHeaderView<JSONArray> {

    @BindView(R.id.gv_channel)
    public FixedGridView gvChannel;
    Activity context;
    HeaderChannelAdapter adapter;
    int menuId;
    public HeaderChannelView(Activity context,int menuId) {
        super(context);
        this.context=context;
        this.menuId=menuId;
    }

    @Override
    protected void getView(JSONArray list, ListView listView) {
        if(list==null || list.length()==0)
            return;
        View view = mInflate.inflate(R.layout.header_channel_layout, listView, false);
        ButterKnife.bind(this, view);

        dealWithTheView(list);
        listView.addHeaderView(view);
    }

    private void dealWithTheView(JSONArray list) {
        if (list == null || list.length() ==0) return;
        int size = list.length();
        if (size < 4) {
            gvChannel.setNumColumns(size);
        }else {
            gvChannel.setNumColumns(4);
        }

        adapter = new HeaderChannelAdapter(mActivity, list,menuId);
        gvChannel.setAdapter(adapter);

    }

    public void reloadData(JSONArray list,int menuId)
    {
        if(list!=null) {
            adapter.setList(list, menuId);
            adapter.notifyDataSetChanged();
        }
    }

}
