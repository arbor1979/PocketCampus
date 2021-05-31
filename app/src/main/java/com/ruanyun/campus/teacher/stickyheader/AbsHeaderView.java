package com.ruanyun.campus.teacher.stickyheader;

import android.app.Activity;
import android.view.LayoutInflater;
import android.widget.ListView;

import org.json.JSONArray;

import java.util.List;

public abstract class AbsHeaderView<T> {

    protected Activity mActivity;
    protected LayoutInflater mInflate;
    protected JSONArray mEntity;

    public AbsHeaderView(Activity activity) {
        this.mActivity = activity;
        mInflate = LayoutInflater.from(activity);
    }

    public boolean fillView(JSONArray t, ListView listView) {

        this.mEntity = t;
        getView(t, listView);
        return true;
    }

    protected abstract void getView(JSONArray t, ListView listView);

}
