package com.ruanyun.campus.teacher.stickyheader.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.stickyheader.model.OperationEntity;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.xiaomi.mipush.sdk.help.HelpService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by sunfusheng on 16/4/20.
 */
public class HeaderOperationAdapter extends BaseAdapter {

    private Context context;
    private JSONArray list;
    protected LayoutInflater mInflater;
    public HeaderOperationAdapter(Context context, JSONArray list) {
        this.context=context;
        this.list=list;
        mInflater = LayoutInflater.from(context);
    }
    public void setList(JSONArray list)
    {
        this.list=list;
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
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.item_operation, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        JSONObject entity=list.optJSONObject(position);

        holder.tvTitle.setText(entity.optString("left"));
        ImageLoader.getInstance().displayImage(entity.optString("image"),holder.givImage);

        if (TextUtils.isEmpty(entity.optString("right"))) {
            holder.tvSubtitle.setVisibility(View.INVISIBLE);
        } else {
            holder.tvSubtitle.setVisibility(View.VISIBLE);
            holder.tvSubtitle.setText(entity.optString("right"));
        }

        return convertView;
    }

    static class ViewHolder {
        @BindView(R.id.giv_image)
        ImageView givImage;
        @BindView(R.id.tv_title)
        TextView tvTitle;
        @BindView(R.id.tv_subtitle)
        TextView tvSubtitle;
        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
