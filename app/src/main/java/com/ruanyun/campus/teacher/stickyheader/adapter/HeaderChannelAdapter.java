package com.ruanyun.campus.teacher.stickyheader.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.stickyheader.model.ChannelEntity;
import com.nostra13.universalimageloader.core.ImageLoader;


import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sunfusheng on 16/4/20.
 */
public class HeaderChannelAdapter extends BaseAdapter {

    private Context context;
    private JSONArray list;
    protected LayoutInflater mInflater;
    int menuId;
    public HeaderChannelAdapter(Context context, JSONArray list,int menuId) {
        super();
        this.context=context;
        this.list=list;
        this.menuId=menuId;
        mInflater = LayoutInflater.from(context);
    }

    public void setList(JSONArray list,int menuId) {
        this.list = list;
        this.menuId=menuId;
    }

    @Override
    public int getCount() {
        return list.length();
    }

    @Override
    public Object getItem(int position) {
        return list.optJSONObject(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            mInflater = LayoutInflater.from(context);
            convertView = mInflater.inflate(R.layout.item_channel, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        JSONObject entity=list.optJSONObject(position);
        holder.tvTitle.setText(entity.optString("name"));

        ImageLoader.getInstance().displayImage(entity.optString("image"),holder.givImage);

        if (TextUtils.isEmpty(entity.optString("badge"))) {
            holder.tvTips.setVisibility(View.INVISIBLE);
        } else {
            holder.tvTips.setVisibility(View.VISIBLE);
            holder.tvTips.setText(entity.optString("badge"));
        }
        if(menuId>0 && menuId-1==position)
            holder.tvline.setVisibility(View.VISIBLE);
        else
            holder.tvline.setVisibility(View.INVISIBLE);
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.giv_image)
        ImageView givImage;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_tips)
        TextView tvTips;
        @BindView(R.id.tv_line)
        TextView tvline;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
