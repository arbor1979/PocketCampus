package com.ruanyun.campus.teacher.adapter;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import com.androidquery.AQuery;
import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.activity.SchoolActivity;
import com.ruanyun.campus.teacher.activity.TabSchoolActivtiy;
import com.ruanyun.campus.teacher.activity.WebSiteActivity;
import com.ruanyun.campus.teacher.api.CampusAPI;
import com.ruanyun.campus.teacher.api.CampusException;
import com.ruanyun.campus.teacher.api.CampusParameters;
import com.ruanyun.campus.teacher.api.RequestListener;
import com.ruanyun.campus.teacher.base.Constants;
import com.ruanyun.campus.teacher.entity.SchoolWorkItem;
import com.ruanyun.campus.teacher.util.AppUtility;
import com.ruanyun.campus.teacher.util.BadgeView;
import com.ruanyun.campus.teacher.util.Base64;
import com.ruanyun.campus.teacher.util.PrefUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Created by lyd10892 on 2016/8/23.
 */

public class SchoolWorkGroupAdapter extends SectionedRecyclerViewAdapter<HeaderHolder, DescHolder, RecyclerView.ViewHolder> {


    private List<SchoolWorkItem> schoolWorkItems;
    private List<String> groupList=new ArrayList<String>();
    private Context mContext;
    private LayoutInflater mInflater;
    private Map<String,List<SchoolWorkItem>> allItemMap = new HashMap<String,List<SchoolWorkItem>>();
    private SparseBooleanArray mBooleanMap;

    public SchoolWorkGroupAdapter(Context context, List<SchoolWorkItem> schoolWorkItems) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mBooleanMap = new SparseBooleanArray();
        setSchoolWorkItems(schoolWorkItems);
    }
    public void setSchoolWorkItems(List<SchoolWorkItem> schoolWorkItems){
        this.schoolWorkItems = schoolWorkItems;
        for (SchoolWorkItem item :schoolWorkItems)
        {
            String groupname=item.getGroupName();
            if(!groupList.contains(groupname)) {
                groupList.add(groupname);
            }
        }
        for (int i=0;i<groupList.size();i++)
        {
            String groupname=groupList.get(i);
            List<SchoolWorkItem> itemList=new ArrayList<SchoolWorkItem>();
            for (SchoolWorkItem item :schoolWorkItems)
            {
                if(item.getGroupName().equals(groupname))
                    itemList.add(item);
            }
            allItemMap.put(groupname,itemList);
        }
        notifyDataSetChanged();
    }
    @Override
    protected int getSectionCount() {
        return groupList.size();
    }

    @Override
    protected int getItemCountForSection(int section) {
        String groupname = groupList.get(section);
        List<SchoolWorkItem> itemList=allItemMap.get(groupname);
        return itemList.size();
    }

    //是否有footer布局
    @Override
    protected boolean hasFooterInSection(int section) {
        return false;
    }

    @Override
    protected HeaderHolder onCreateSectionHeaderViewHolder(ViewGroup parent, int viewType) {
        return new HeaderHolder(mInflater.inflate(R.layout.hotel_title_item, parent, false));
    }


    @Override
    protected RecyclerView.ViewHolder onCreateSectionFooterViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    protected DescHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        return new DescHolder(mInflater.inflate(R.layout.school_work_item, parent, false));
    }


    @Override
    protected void onBindSectionHeaderViewHolder(final HeaderHolder holder, final int section) {
        holder.openView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isOpen = mBooleanMap.get(section);
                String text = isOpen ? "展开" : "关闭";
                mBooleanMap.put(section, !isOpen);
                //holder.openView.setText(text);
                //notifyDataSetChanged();
            }
        });
        String groupname = groupList.get(section);
        holder.titleView.setText(groupname);
        //holder.openView.setText(mBooleanMap.get(section) ? "关闭" : "展开");

    }


    @Override
    protected void onBindSectionFooterViewHolder(RecyclerView.ViewHolder holder, int section) {

    }

    @Override
    protected void onBindItemViewHolder(DescHolder holder, int section, int position) {
        String groupname = groupList.get(section);
        List<SchoolWorkItem> itemList=allItemMap.get(groupname);
        final SchoolWorkItem item=itemList.get(position);
        holder.descView.setText(item.getWorkText());
        AQuery aq = new AQuery(mContext);
        Bitmap bitmap = aq.getCachedImage(item.getTransIcon());
        if (bitmap != null) {
            aq.id(holder.itemIcon).image(bitmap);
        }
        else
            aq.id(holder.itemIcon).image(item.getTransIcon(),false,true);
        if(holder.badge==null) {
            holder.badge = new BadgeView(mContext, holder.itemIcon);
            holder.badge.setBadgeMargin(0, 0);
        }
        if(item.getUnread()>0)
        {
            holder.badge.setText(String.valueOf(item.getUnread()));
            holder.badge.show();
        }
        else
            holder.badge.hide();
        holder.itemIcon.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(item.getTemplateName().equals("浏览器"))
                {
                    Intent contractIntent = new Intent(mContext,WebSiteActivity.class);
                    String url=item.getInterfaceName();
                    if(url.indexOf("pda2014")>0) {
                        int index=url.indexOf("?");
                        if(index==-1)
                            url=url+"?a=1";

                        String jiaoyanma = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
                        JSONObject obj=new JSONObject();
                        try {
                            obj.put("用户较验码",jiaoyanma);
                            jiaoyanma = Base64.encode(obj.toString().getBytes());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        url = url + "&jiaoyanma=" + jiaoyanma;
                    }
                    contractIntent.putExtra("url",url);
                    contractIntent.putExtra("title", item.getWorkText());
                    mContext.startActivity(contractIntent);
                }
                else if(item.getTemplateName().equals("二维码"))
                {
                    if (Build.VERSION.SDK_INT >= 23) {
                        Activity act=(Activity)mContext;
                        if (AppUtility.checkPermission(act.getParent(), 12, Manifest.permission.CAMERA))
                            AppUtility.openScanCode((Activity)mContext, TabSchoolActivtiy.SCANNIN_GREQUEST_CODE,item.getInterfaceName());
                    } else
                        AppUtility.openScanCode((Activity)mContext,TabSchoolActivtiy.SCANNIN_GREQUEST_CODE,item.getInterfaceName());
                }
                else
                {
                    Intent intent = new Intent(mContext, SchoolActivity.class);
                    intent.putExtra("title", item.getWorkText());

                    intent.putExtra("interfaceName",item.getInterfaceName());
                    intent.putExtra("templateName",item.getTemplateName());
                    mContext.startActivity(intent);
                }
                long datatime =System.currentTimeMillis();
                String checkCode=PrefUtility.get(Constants.PREF_CHECK_CODE, "");
                JSONObject jo = new JSONObject();
                try {
                    jo.put("用户较验码", checkCode);
                    jo.put("模块名称", item.getWorkText());
                    jo.put("模块地址", item.getInterfaceName());
                    jo.put("DATETIME", datatime);
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                String base64Str = Base64.encode(jo.toString().getBytes());
                CampusParameters params = new CampusParameters();
                params.add(Constants.PARAMS_DATA, base64Str);
                CampusAPI.trickAccessLog(params, new RequestListener() {

                    @Override
                    public void onComplete(String response) {

                    }

                    @Override
                    public void onIOException(IOException e) {

                    }

                    @Override
                    public void onError(CampusException e) {

                    }
                });
            }
        });
    }
}
