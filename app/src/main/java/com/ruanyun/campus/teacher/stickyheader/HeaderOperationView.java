package com.ruanyun.campus.teacher.stickyheader;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.activity.WebSiteActivity;
import com.ruanyun.campus.teacher.stickyheader.adapter.HeaderOperationAdapter;
import com.ruanyun.campus.teacher.stickyheader.model.OperationEntity;
import com.ruanyun.campus.teacher.stickyheader.util.ToastUtil;
import com.ruanyun.campus.teacher.util.AppUtility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sunfusheng on 16/4/20.
 */
public class HeaderOperationView extends AbsHeaderView<List<OperationEntity>> {

    @BindView(R.id.gv_operation)
    ListView gvOperation;
    private Activity context;
    private HeaderOperationAdapter adapter;
    private View rootview;
    public HeaderOperationView(Activity context) {
        super(context);
        this.context=context;
    }

    @Override
    protected void getView(JSONArray list, ListView listView) {
        if(list==null || list.length()==0)
            return;
        View view = mInflate.inflate(R.layout.header_operation_layout, listView, false);
        ButterKnife.bind(this, view);
        if(list==null || list.length()==0)
            return;
        dealWithTheView(list);
        ListView.LayoutParams linearParams =(ListView.LayoutParams) view.getLayoutParams(); //取控件textView当前的布局参数
        linearParams.height =list.length()* AppUtility.dip2px(context,48);
        view.setLayoutParams(linearParams);
        listView.addHeaderView(view);
        rootview=view;
    }
    public View getRootView()
    {
        return rootview;
    }
    private void dealWithTheView(final JSONArray list) {

        adapter = new HeaderOperationAdapter(mActivity, list);
        gvOperation.setAdapter(adapter);

        gvOperation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                JSONObject banneritem=list.optJSONObject(position);
                Intent contractIntent = new Intent(context, WebSiteActivity.class);
                contractIntent.putExtra("url",banneritem.optString("url"));
                contractIntent.putExtra("title",banneritem.optString("urltitle"));
                context.startActivity(contractIntent);
            }
        });
    }
    public void reloadData(JSONArray list)
    {
        adapter.setList(list);
        adapter.notifyDataSetChanged();
    }
}
