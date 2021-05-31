package com.ruanyun.campus.teacher.stickyheader;

import android.app.Activity;
import android.view.View;
import android.widget.ListView;

import com.ruanyun.campus.teacher.R;

import org.json.JSONArray;


/**
 * Created by sunfusheng on 16/4/20.
 */
public class HeaderDividerView extends AbsHeaderView<String> {

    public HeaderDividerView(Activity context) {
        super(context);
    }

    @Override
    protected void getView(JSONArray s, ListView listView) {
        View view = mInflate.inflate(R.layout.header_divider_layout, listView, false);
        listView.addHeaderView(view);
    }

}
