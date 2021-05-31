package com.ruanyun.campus.teacher.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.androidquery.AQuery;
import com.ruanyun.campus.teacher.CampusApplication;
import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.activity.SchoolActivity;
import com.ruanyun.campus.teacher.activity.SchoolDetailActivity;
import com.ruanyun.campus.teacher.activity.WebSiteActivity;
import com.ruanyun.campus.teacher.api.CampusAPI;
import com.ruanyun.campus.teacher.api.CampusException;
import com.ruanyun.campus.teacher.api.CampusParameters;
import com.ruanyun.campus.teacher.api.RequestListener;
import com.ruanyun.campus.teacher.base.Constants;
import com.ruanyun.campus.teacher.entity.GroupListItem;
import com.ruanyun.campus.teacher.entity.User;
import com.ruanyun.campus.teacher.stickyheader.HeaderBannerView;
import com.ruanyun.campus.teacher.stickyheader.HeaderChannelView;
import com.ruanyun.campus.teacher.stickyheader.HeaderDividerView;
import com.ruanyun.campus.teacher.stickyheader.HeaderOperationView;
import com.ruanyun.campus.teacher.stickyheader.SmoothListView;
import com.ruanyun.campus.teacher.stickyheader.adapter.TravelingAdapter;
import com.ruanyun.campus.teacher.stickyheader.util.ColorUtil;
import com.ruanyun.campus.teacher.stickyheader.util.DensityUtil;
import com.ruanyun.campus.teacher.util.AppUtility;
import com.ruanyun.campus.teacher.util.Base64;
import com.ruanyun.campus.teacher.util.PrefUtility;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * 作者：孙福生
 * <p>
 * 个人博客：sunfusheng.com
 */
public class SchoolGroupListFragment extends Fragment implements SmoothListView.ISmoothListViewListener {


    private HeaderBannerView headerBannerView; // 广告视图
    private HeaderChannelView headerChannelView; // 频道视图
    private HeaderOperationView headerOperationView; // 运营视图
    private HeaderDividerView headerDividerView; // 分割线占位图
    private TravelingAdapter mAdapter;

    private int titleViewHeight = 65; // 标题栏的高度

    private View itemHeaderBannerView; // 从ListView获取的广告子View
    private int bannerViewHeight = 180; // 广告视图的高度
    private int bannerViewTopMargin; // 广告视图距离顶部的距离

    private boolean isScrollIdle = true; // ListView是否在滑动
    private boolean isSmooth = false; // 没有吸附的前提下，是否在滑动
    private FrameLayout flActionMore;
    private String interfaceName,title;
    private LayoutInflater inflater;
    private User user;
    AQuery aq;
    private SmoothListView smoothListView;
    private RelativeLayout rlBar;
    private View viewActionMoreBg;
    private TextView tvTitle;
    private LinearLayout loadingLayout,contentLayout,failedLayout,emptyLayout,ll_multisel;
    private GroupListItem grouoList;
    private FloatingActionButton mFab;
    private boolean bShowMutiSel=false;
    private CheckBox cb_selAll;
    public static final Fragment newInstance(String title, String interfaceName){
        Fragment fragment = new SchoolGroupListFragment();
        Bundle bundle = new Bundle();
        bundle.putString("title", title);
        bundle.putString("interfaceName", interfaceName);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        title=getArguments().getString("title");
        interfaceName=getArguments().getString("interfaceName");
        this.inflater = inflater;
        View view = inflater.inflate(R.layout.fragment_grouplist,
                container, false);

        user=((CampusApplication)getActivity().getApplicationContext()).getLoginUserObj();
        initView(view);
        initListener();
        getNoticesList(true);
        return view;
    }


    private void initView(View view) {
        aq= new AQuery(view);
        loadingLayout = (LinearLayout) view.findViewById(R.id.data_load);
        contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);
        failedLayout = (LinearLayout) view.findViewById(R.id.empty_error);
        emptyLayout = (LinearLayout) view.findViewById(R.id.empty);
        smoothListView=(SmoothListView)view.findViewById(R.id.listView);
        rlBar=(RelativeLayout)view.findViewById(R.id.rl_bar);
        tvTitle=(TextView)view.findViewById(R.id.tv_title);
        viewActionMoreBg=(View)view.findViewById(R.id.view_action_more_bg);
        flActionMore=(FrameLayout)view.findViewById(R.id.fl_action_more);
        mFab = (FloatingActionButton) view.findViewById(R.id.fab);
        ll_multisel= (LinearLayout) view.findViewById(R.id.ll_multisel);
        cb_selAll=(CheckBox) view.findViewById(R.id.cb_selAll);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                popFilterDlg();
            }
        });
        flActionMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
         /*
        btnLeft.setVisibility(View.VISIBLE);
        btnLeft.setCompoundDrawablesWithIntrinsicBounds(R.drawable.bg_btn_left_nor, 0, 0, 0);
        lyLeft.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
         */
        //重新加载
        failedLayout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getNoticesList(true);
            }
        });

    }
    private void initDate()
    {
        // 设置广告数据
        if(headerBannerView==null) {
            headerBannerView = new HeaderBannerView(getActivity());
            headerBannerView.fillView(grouoList.getBannerArr(), smoothListView);
        }

        // 设置分组数据
        if(headerChannelView==null) {
            headerChannelView = new HeaderChannelView(getActivity(),grouoList.getMenuId());
            headerChannelView.fillView(grouoList.getGroupMenu(), smoothListView);
            if(headerChannelView.gvChannel!=null) {
                headerChannelView.gvChannel.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        JSONArray list = grouoList.getGroupMenu();
                        jumptomoban(position, list, true);
                    }

                });
            }
        }
        else
            headerChannelView.reloadData(grouoList.getGroupMenu(),grouoList.getMenuId());
        // 设置公告数据
        if(headerOperationView==null) {
            headerOperationView = new HeaderOperationView(getActivity());
            headerOperationView.fillView(grouoList.getNotifyArr(), smoothListView);
        }
        else {
            smoothListView.removeHeaderView(headerOperationView.getRootView());
            headerOperationView.fillView(grouoList.getNotifyArr(), smoothListView);
        }
        // 设置ListView数据
        if(mAdapter==null) {
            mAdapter = new TravelingAdapter(getContext(), grouoList.getListArr(),bShowMutiSel);
            smoothListView.setAdapter(mAdapter);
            smoothListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    JSONArray list=grouoList.getListArr();
                    jumptomoban(position-smoothListView.getHeaderViewsCount(),list,false);
                }
            });
        }
        else {
            mAdapter.setList(grouoList.getListArr(),bShowMutiSel);
            mAdapter.notifyDataSetChanged();
        }
        if(grouoList.getMutiSelArr()!=null && grouoList.getMutiSelArr().length()>0) {
            mFab.show();
        }
        else
            mFab.hide();
        if(grouoList.getListArr()!=null && grouoList.getPagecount()>grouoList.getPage())
            smoothListView.setLoadMoreEnable(true);
        else
            smoothListView.setLoadMoreEnable(false);
    }
    private void jumptomoban(int position,JSONArray list,boolean filterflag)
    {
        JSONObject listitem=list.optJSONObject(position);
        if(listitem==null) return;
        String jumpurl=listitem.optString("url");
        String template= AppUtility.findUrlQueryString(jumpurl,"template");
        String templategrade=AppUtility.findUrlQueryString(jumpurl,"templategrade");
        String targettitle=AppUtility.findUrlQueryString(jumpurl,"targettitle");
        if(template.length()==0 && filterflag)
        {
            grouoList.setMenuId(position+1);
            grouoList.setPage(1);
            getNoticesList(false);
        }
        else if(template.equals("浏览器") || (!filterflag && template.length()==0))
        {
            if(jumpurl.indexOf("?")>0)
                jumpurl+="&";
            else
                jumpurl+="?";
            Intent contractIntent = new Intent(getActivity(), WebSiteActivity.class);
            String jiaoyanma = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
            jumpurl+= "jiaoyanma=" + Base64.safeUrlbase64(jiaoyanma);
            contractIntent.putExtra("url",jumpurl);
            contractIntent.putExtra("title",targettitle);
            getActivity().startActivity(contractIntent);
        }
        else
        {
            Intent intent;
            if (templategrade.equals("main"))
                intent = new Intent(getActivity(), SchoolActivity.class);
            else
                intent = new Intent(getActivity(), SchoolDetailActivity.class);
            intent.putExtra("title", targettitle);
            intent.putExtra("interfaceName",jumpurl);
            intent.putExtra("templateName",template);
            getActivity().startActivity(intent);
        }
    }
    private void getNoticesList(boolean flag)
    {
        showProgress(flag);
        long datatime = System.currentTimeMillis();
        String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
        JSONObject jo = new JSONObject();
        Locale locale = getResources().getConfiguration().locale;
        String language = locale.getCountry();
        try {
            jo.put("用户较验码", checkCode);
            jo.put("DATETIME", datatime);
            jo.put("language", language);
            if(grouoList!=null && grouoList.getPage()>0)
                jo.put("page",grouoList.getPage());
            if(grouoList!=null && grouoList.getMenuId()>0)
                jo.put("menuId",grouoList.getMenuId());
        } catch (JSONException e1) {
            e1.printStackTrace();
        }
        String base64Str = Base64.encode(jo.toString().getBytes());
        CampusParameters params = new CampusParameters();
        params.add(Constants.PARAMS_DATA, base64Str);
        CampusAPI.getSchoolItem(params, interfaceName, new RequestListener() {

            @Override
            public void onIOException(IOException e) {
            }

            @Override
            public void onError(CampusException e) {
            }

            @Override
            public void onComplete(String response) {
                Message msg = new Message();
                msg.what = 0;
                msg.obj = response;
                mHandler.sendMessage(msg);
            }
        });
    }
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case -1:
                    showProgress(false);
                    showFetchFailedView();
                    AppUtility.showErrorToast(getActivity(), msg.obj.toString());
                    break;

                case 0:
                    showProgress(false);

                    String result = msg.obj.toString();
                    String resultStr=null;
                    //byte[] contact64byte = null;
                    if (AppUtility.isNotEmpty(result)) {
                        try {
                            resultStr = new String(Base64.decode(result
                                    .getBytes("GBK")));
                        } catch (UnsupportedEncodingException e) {
                            showFetchFailedView();
                            e.printStackTrace();
                        }
                    }else{
                        showFetchFailedView();
                    }
                    //resultStr=ZLibUtils.decompress(contact64byte);
                    if (AppUtility.isNotEmpty(resultStr)) {
                        try {
                            JSONObject jo = new JSONObject(resultStr);
                            String res = jo.optString("结果");
                            if(AppUtility.isNotEmpty(res)){
                                AppUtility.showToastMsg(getActivity(), res);
                            }else{
                                grouoList = new GroupListItem(jo,grouoList);
                                if(grouoList.getTitle()!=null && grouoList.getTitle().length()>0)
                                    tvTitle.setText(grouoList.getTitle());

                                initDate();
                            }
                        } catch (JSONException e) {
                            showFetchFailedView();
                            e.printStackTrace();
                        }
                    }else{
                        showFetchFailedView();
                    }
                    break;
                case 2:
                    result = msg.obj.toString();
                    resultStr=null;
                    if (AppUtility.isNotEmpty(result)) {
                        try {
                            resultStr = new String(Base64.decode(result
                                    .getBytes("GBK")));
                        } catch (UnsupportedEncodingException e) {
                            showFetchFailedView();
                            e.printStackTrace();
                        }
                    }else{
                        showFetchFailedView();
                    }
                    if (AppUtility.isNotEmpty(resultStr)) {
                        try {
                            JSONObject jo = new JSONObject(resultStr);
                            String res = jo.optString("结果");
                            if(res.equals("成功"))
                            {
                                if(jo.optString("msg").length()>0)
                                    AppUtility.showToastMsg(getActivity(), jo.optString("msg"));
                                grouoList=null;
                                getNoticesList(false);
                            }
                            else {
                                String errmsg = res;
                                if (jo.optString("msg").length() > 0)
                                    errmsg += ":" + jo.optString("msg");
                                AppUtility.showToastMsg(getActivity(), errmsg);
                            }
                        }
                        catch (JSONException e) {
                            e.printStackTrace();
                            AppUtility.showErrorToast(getActivity(), e.getLocalizedMessage());
                        }
                    }
                    else
                        showFetchFailedView();
                    break;

            }
        }
    };
    private void showFetchFailedView() {
        loadingLayout.setVisibility(View.GONE);
        contentLayout.setVisibility(View.GONE);
        failedLayout.setVisibility(View.VISIBLE);
    }

    private void showProgress(boolean progress) {
        if (progress) {
            loadingLayout.setVisibility(View.VISIBLE);
            contentLayout.setVisibility(View.GONE);
            failedLayout.setVisibility(View.GONE);
        } else {
            loadingLayout.setVisibility(View.GONE);
            contentLayout.setVisibility(View.VISIBLE);
            failedLayout.setVisibility(View.GONE);
        }
    }
    private void initListener() {
        smoothListView.setRefreshEnable(false);
        smoothListView.setLoadMoreEnable(false);
        smoothListView.setSmoothListViewListener(this);
        smoothListView.setOnScrollListener(new SmoothListView.OnSmoothScrollListener() {
            @Override
            public void onSmoothScrolling(View view) {
            }

            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                isScrollIdle = (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE);
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (isScrollIdle && bannerViewTopMargin < 0) return;

                if (itemHeaderBannerView == null) {
                    itemHeaderBannerView = smoothListView.getChildAt(1);
                }
                if (itemHeaderBannerView != null) {
                    bannerViewTopMargin = DensityUtil.px2dip(getContext(), itemHeaderBannerView.getTop());
                    bannerViewHeight = DensityUtil.px2dip(getContext(), itemHeaderBannerView.getHeight());
                }

                // 处理标题栏颜色渐变
                handleTitleBarColorEvaluate();
            }
        });
    }
    // 处理标题栏颜色渐变
    private void handleTitleBarColorEvaluate() {
        float fraction;
        if (bannerViewTopMargin > 0) {
            fraction = 1f - bannerViewTopMargin * 1f / 60;
            if (fraction < 0f) fraction = 0f;
            rlBar.setAlpha(fraction);
            return;
        }

        float space = Math.abs(bannerViewTopMargin) * 1f;
        if(bannerViewHeight>titleViewHeight)
            fraction = space / (bannerViewHeight - titleViewHeight);
        else
            fraction = space / bannerViewHeight;
        if (fraction < 0f) fraction = 0f;
        if (fraction > 1f) fraction = 1f;
        rlBar.setAlpha(1f);

        if (fraction >= 1f) {
            tvTitle.setAlpha(1f);
            viewActionMoreBg.setAlpha(0f);
            rlBar.setBackgroundColor(getContext().getResources().getColor(R.color.colorPrimary));
        } else {
            tvTitle.setAlpha(fraction);
            viewActionMoreBg.setAlpha(1f - fraction);
            rlBar.setBackgroundColor(ColorUtil.getNewColorByStartEndColor(getContext(), fraction, R.color.transparent, R.color.colorPrimary));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getNoticesList(false);
        if(headerBannerView!=null)
            headerBannerView.enqueueBannerLoopMessage();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(headerBannerView!=null)
            headerBannerView.removeBannerLoopMessage();
    }

    @Override
    public void onRefresh() {
        getNoticesList(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                smoothListView.stopRefresh();
                smoothListView.setRefreshTime("刚刚");
            }
        }, 2000);
    }

    @Override
    public void onLoadMore() {
        if(grouoList!=null && grouoList.getPage()<grouoList.getPagecount())
            grouoList.setPage(grouoList.getPage()+1);
        getNoticesList(false);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                smoothListView.stopLoadMore();
            }
        }, 2000);
    }
    private void popFilterDlg()
    {
        if(grouoList.getMutiSelArr()!=null && grouoList.getMutiSelArr().length()>0) {
            bShowMutiSel=!bShowMutiSel;
            if(bShowMutiSel) {
                LinearLayout ll_btns=null;
                for(int i=0;i<ll_multisel.getChildCount();i++) {
                    View subview = ll_multisel.getChildAt(i);
                    if (subview instanceof LinearLayout) {
                        ll_btns=(LinearLayout)subview;
                        ll_btns.removeAllViews();
                        break;
                    }
                }
                cb_selAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                        for(int i=0;i<grouoList.getListArr().length();i++)
                        {
                            JSONObject item=grouoList.getListArr().optJSONObject(i);
                            try {
                                item.put("checked",b);
                                grouoList.getListArr().put(i,item);
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        mAdapter.notifyDataSetChanged();
                    }

                });
                for(int i=0;i<grouoList.getMutiSelArr().length();i++)
                {
                    final JSONObject jo=grouoList.getMutiSelArr().optJSONObject(i);
                    if(jo!=null)
                    {
                        Button btn=new Button(getActivity());
                        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        layoutParams.setMargins(10,0,10,0);//4个参数按顺序分别是左上右下
                        layoutParams.height=95;
                        btn.setLayoutParams(layoutParams);
                        btn.setText(jo.optString("name"));
                        ll_btns.addView(btn);
                        btn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String selIdStr="";
                                for(int i=0;i<grouoList.getListArr().length();i++)
                                {
                                    JSONObject item=grouoList.getListArr().optJSONObject(i);
                                    if(item.optBoolean("checked"))
                                    {
                                        if(selIdStr.length()>0)
                                            selIdStr+=","+item.optString("id");
                                        else
                                            selIdStr=item.optString("id");
                                    }
                                }
                                if(selIdStr.length()==0) {
                                    AppUtility.showToastMsg(getActivity(),"请先勾选记录");
                                    return;
                                }
                                String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
                                JSONObject jo1 = new JSONObject();
                                try {
                                    jo1.put("用户较验码", checkCode);
                                    jo1.put("selIdStr",selIdStr);

                                } catch (JSONException e1) {
                                    e1.printStackTrace();
                                }
                                CampusAPI.httpPost(jo.optString("url"),jo1, mHandler, 2);
                            }
                        });
                        if(jo.optString("color").length()>0)
                        {
                            if(jo.optString("color").equals("orange"))
                                btn.setBackgroundResource(R.drawable.button_round_corner_orange);
                            else if(jo.optString("color").equals("blue"))
                                btn.setBackgroundResource(R.drawable.button_round_corner_blue);
                            else
                                btn.setBackgroundResource(R.drawable.button_round_corner_green);

                        }
                    }

                }
                ll_multisel.setVisibility(View.VISIBLE);
            }
            else
                ll_multisel.setVisibility(View.GONE);
            mAdapter.setList(grouoList.getListArr(),bShowMutiSel);
            mAdapter.notifyDataSetChanged();
        }

    }

}
