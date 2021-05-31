package com.ruanyun.campus.teacher.stickyheader.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.ruanyun.campus.teacher.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sunfusheng on 16/4/20.
 */
public class TravelingAdapter extends BaseAdapter {

    private Context context;
    private JSONArray list;
    protected LayoutInflater mInflater;
    private boolean bShowMutiSel=false;
    public TravelingAdapter(Context context, JSONArray list,boolean bShowMutiSel) {
        super();
        if(list==null)
            list=new JSONArray();
        this.context=context;
        this.list=list;
        this.bShowMutiSel=bShowMutiSel;
        mInflater = LayoutInflater.from(context);
    }

    public void setList(JSONArray list,boolean bShowMutiSel) {
        if(list!=null)
            this.list = list;
        this.bShowMutiSel=bShowMutiSel;
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
    public View getView(final int position, View convertView, ViewGroup parent) {
        // 暂无数据
        if (list.length()==0) {
            convertView = mInflater.inflate(R.layout.item_no_data_layout, null);
            AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 200);
            RelativeLayout rootView = convertView.findViewById( R.id.rl_root_view);
            rootView.setLayoutParams(params);
            return convertView;
        }

        // 正常数据
        final ViewHolder holder;
        if (convertView != null && convertView instanceof LinearLayout) {
            holder = (ViewHolder) convertView.getTag();
        } else {
            convertView = mInflater.inflate(R.layout.item_travel, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        }
        final JSONObject entity = list.optJSONObject(position);

        holder.llRootView.setVisibility(View.VISIBLE);
        holder.tvTitle.setText(entity.optString("title"));
        holder.tvLeft.setText(entity.optString("left"));
        holder.tvRight.setText(entity.optString("right"));
        holder.tvBottom.setText(entity.optString("bottom"));

        if(entity.optString("leftcolor").length()>0) {
            int color=Color.parseColor(entity.optString("leftcolor"));
            if(color!=0)
                holder.tvLeft.setTextColor(color);
        }
        else
            holder.tvLeft.setTextColor(context.getResources().getColor(R.color.font_black_5));
        if(entity.optString("rightcolor").length()>0) {
            int color=Color.parseColor(entity.optString("rightcolor"));
            if(color!=0)
                holder.tvRight.setTextColor(color);
        }
        else
            holder.tvRight.setTextColor(context. getResources().getColor(R.color.font_black_5));
        ImageLoader.getInstance().displayImage(entity.optString("image"),holder.givImage);
        if(bShowMutiSel) {
            holder.cbCheckitem.setVisibility(View.VISIBLE);
            holder.cbCheckitem.setChecked(entity.optBoolean("checked"));
            holder.cbCheckitem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if(!compoundButton.isPressed())
                        return ;
                    try {
                        entity.put("checked",b);
                        list.put(position,entity);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        else
            holder.cbCheckitem.setVisibility(View.GONE);
        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.ll_root_view)
        LinearLayout llRootView;
        @BindView(R.id.giv_image)
        ImageView givImage;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_left)
        TextView tvLeft;
        @BindView(R.id.tv_right)
        TextView tvRight;
        @BindView(R.id.tv_bottom)
        TextView tvBottom;
        @BindView(R.id.cb_checkitem)
        CheckBox cbCheckitem;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
