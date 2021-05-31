package com.ruanyun.campus.teacher.fragment;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.androidquery.AQuery;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.ruanyun.campus.teacher.CampusApplication;
import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.activity.SchoolActivity;
import com.ruanyun.campus.teacher.activity.SchoolDetailActivity;
import com.ruanyun.campus.teacher.activity.ShowPersonInfo;
import com.ruanyun.campus.teacher.activity.StudentInfoActivity;
import com.ruanyun.campus.teacher.activity.WebSiteActivity;
import com.ruanyun.campus.teacher.api.CampusAPI;
import com.ruanyun.campus.teacher.api.CampusException;
import com.ruanyun.campus.teacher.api.CampusParameters;
import com.ruanyun.campus.teacher.api.RequestListener;
import com.ruanyun.campus.teacher.base.Constants;
import com.ruanyun.campus.teacher.entity.AchievementItem;
import com.ruanyun.campus.teacher.entity.AchievementItem.Achievement;
import com.ruanyun.campus.teacher.util.AppUtility;
import com.ruanyun.campus.teacher.util.Base64;
import com.ruanyun.campus.teacher.util.DialogUtility;
import com.ruanyun.campus.teacher.util.PrefUtility;
import com.ruanyun.campus.teacher.widget.SegmentedGroup;
import com.ruanyun.campus.teacher.widget.XListView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static android.view.View.VISIBLE;


/**
 * 成绩
 */
public class SchoolAchievementFragment extends Fragment implements XListView.IXListViewListener{
	private String TAG = "SchoolAchievementFragment";
	private XListView myListview;
	private Button btnLeft;
	private TextView tvTitle,tvRight,tv_huizong1;
	private LinearLayout lyLeft,lyRight;
	private LinearLayout loadingLayout;
	private LinearLayout contentLayout;
	private LinearLayout failedLayout;
	private LinearLayout emptyLayout,ll_multisel;
	private AchievementItem achievementItem;
	private String interfaceName,title;
	private LayoutInflater inflater;
	private AchieveAdapter adapter;
	private List<Achievement> achievements = new ArrayList<Achievement>();
	private Dialog dialog;
	private Dialog userTypeDialog;
	private boolean isLoading=false;
	private int curpage=0;
	private FloatingActionButton mFab,mFab1;
	private int mPreviousVisibleItem;
	private boolean bShowMutiSel=false;
	private CheckBox cb_selAll;
	private SegmentedGroup segmentedGroup2;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case -1:
				showFetchFailedView();
				AppUtility.showErrorToast(getActivity(), msg.obj.toString());
				break;
			case 0:
				isLoading=false;
				myListview.stopRefresh();
				showProgress(false);
				String result = msg.obj.toString();
				String resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						if(AppUtility.isNotEmpty(res)){
							AppUtility.showToastMsg(getActivity(), res,1);
						}
						else{
							achievementItem = new AchievementItem(jo);
							Log.d(TAG, "--------noticesItem.getNotices().size():"
									+ achievementItem.getAchievements().size());
							if(achievementItem.getPage()>0 && achievementItem.getAchievements().size()<achievementItem.getAllnum()) {
								myListview.setPullLoadEnable(true);
								curpage=achievementItem.getPage();
							}
							else
								myListview.setPullLoadEnable(false);
							achievements = achievementItem.getAchievements();
							if(achievementItem.getGroupArr().length()>0)
							{
								segmentedGroup2.setVisibility(VISIBLE);
								segmentedGroup2.removeAllViews();
								segmentedGroup2.setOnCheckedChangeListener(null);
								for(int i=0;i<achievementItem.getGroupArr().length();i++)
								{
									String groupname=achievementItem.getGroupArr().getString(i);

									RadioButton rdbtn = (RadioButton) LayoutInflater.from(getActivity()).inflate(R.layout.tabmenu_radiobutton, null);
									rdbtn.setText(groupname);
									if(achievementItem.getCurGroup()==i)
									{
										rdbtn.setChecked(true);
									}
									rdbtn.setId(i);
									segmentedGroup2.addView(rdbtn);
								}
								segmentedGroup2.updateBackground();
								segmentedGroup2.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener(){

									@Override
									public void onCheckedChanged(RadioGroup group, int checkedId) {
										// TODO Auto-generated method stub
										getAchievesItem(true,1);
									}

								});
							}
							adapter.notifyDataSetChanged();
							tvTitle.setText(achievementItem.getTitle());
							if(achievementItem.getHuizong()!=null && achievementItem.getHuizong().length()>0)
							{
								tv_huizong1.setText(achievementItem.getHuizong());
								tv_huizong1.setVisibility(VISIBLE);
							}
							else
							{
								tv_huizong1.setVisibility(View.GONE);
							}
							if(achievementItem.getRightButton()!=null && achievementItem.getRightButton().length()>0)
							{
								tvRight.setText(achievementItem.getRightButton());
								tvRight.setVisibility(View.VISIBLE);
								lyRight.setOnClickListener(new OnClickListener() {

									@Override
									public void onClick(View v) {
										if(achievementItem.getSubmitTarget().equals("是"))
										{
											submitButtonClick(achievementItem.getRightButtonURL());
										}
										else
										{
											int pos=interfaceName.indexOf("?");
											String preUrl=interfaceName;
											if(pos>-1)
												preUrl=interfaceName.substring(0, pos);
											String template=AppUtility.findUrlQueryString(achievementItem.getRightButtonURL(),"template");
											Intent intent;
											if(template.equals("浏览器"))
											{
												intent = new Intent(getActivity(),WebSiteActivity.class);
												String jiaoyanma = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
												String jumpurl=achievementItem.getRightButtonURL();
												if(jumpurl.indexOf("?")>-1)
													jumpurl+="&";
												else
													jumpurl+="?";
												jumpurl+="jiaoyanma=" + Base64.safeUrlbase64(jiaoyanma);
												intent.putExtra("url",jumpurl);
											}
											else {
												intent = new Intent(getActivity(), SchoolDetailActivity.class);
												intent.putExtra("templateName", "调查问卷");
												intent.putExtra("interfaceName", preUrl+achievementItem.getRightButtonURL());
											}
											intent.putExtra("title", title);
											intent.putExtra("status", "进行中");
											intent.putExtra("autoClose", "是");
											startActivityForResult(intent,101);
										}
									}
								});
							}
							else
							{
								tvRight.setVisibility(View.GONE);
								lyRight.setOnClickListener(null);
							}
							if(achievementItem.getFilterArr()!=null && achievementItem.getFilterArr().length()>0) {
								mFab.show();
							}
							else
								mFab.hide();
							if(achievementItem.getMutiSelArr().length()>0)
								mFab1.show();
							else
								mFab1.hide();
								
						}
					} 
					catch (UnsupportedEncodingException e) {

						e.printStackTrace();
						AppUtility.showErrorToast(getActivity(),e.getLocalizedMessage());
					}
					catch (JSONException e) {
						
						e.printStackTrace();
						AppUtility.showErrorToast(getActivity(), e.getLocalizedMessage());
					}
				}else{
					showFetchFailedView();
					
				}
				break;
			case 2:
				result = msg.obj.toString();
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result.getBytes("GBK")));
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						if(res.equals("成功"))
						{
							AppUtility.showToastMsg(getActivity(), jo.optString("msg"));
							getAchievesItem(false, curpage);
						}
						else {
							String errmsg=res;
							if(jo.optString("msg").length()>0)
								errmsg+=":"+jo.optString("msg");
							AppUtility.showToastMsg(getActivity(), errmsg);
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						AppUtility.showErrorToast(getActivity(), e.getLocalizedMessage());
					}
				}
				else
					showFetchFailedView();
				break;
			case 3:
				result = msg.obj.toString();
				resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
						Log.d(TAG, resultStr);
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
							AppUtility.showToastMsg(getActivity(), "操作成功!");
							String autoClose=jo.optString("自动关闭");
							if(autoClose!=null && autoClose.equals("是"))
							{
								Intent aintent = new Intent();
								getActivity().setResult(1,aintent); 
								getActivity().finish();
							}
							else
								getAchievesItem(false, curpage);
						}
						else
							AppUtility.showToastMsg(getActivity(), "操作失败:"+res,1);
						
					} catch (JSONException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					showFetchFailedView();
				}
				break;
			}
		}
	};
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (resultCode) { //resultCode为回传的标记，我在B中回传的是RESULT_OK
		case 1:
			getAchievesItem(false,curpage);
		    break;
		default:
		    break;
		}
	}
	public static final SchoolAchievementFragment newInstance(String title,String interfaceName)
	{
		SchoolAchievementFragment fragment = new SchoolAchievementFragment();
		Bundle bundle = new Bundle();
		bundle.putString("title",title);
		bundle.putString("interfaceName", interfaceName);
		fragment.setArguments(bundle);

		return fragment ;
	}
	@Override
	public void onCreate( Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		interfaceName=getArguments().getString("interfaceName");
		title=getArguments().getString("title");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.school_listview_fragment_pro,
				container, false);
		myListview = (XListView) view.findViewById(R.id.my_listview);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			AppUtility.setRootViewPadding(view);
		btnLeft = (Button) view.findViewById(R.id.btn_left);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvRight = (TextView) view.findViewById(R.id.tv_right);
		lyRight = (LinearLayout) view.findViewById(R.id.layout_btn_right);
		lyLeft = (LinearLayout) view.findViewById(R.id.layout_btn_left);
		tv_huizong1= (TextView) view.findViewById(R.id.tv_huizong1);
		loadingLayout = (LinearLayout) view.findViewById(R.id.data_load);
		contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);
		failedLayout = (LinearLayout) view.findViewById(R.id.empty_error);
		emptyLayout = (LinearLayout) view.findViewById(R.id.empty);
		ll_multisel= (LinearLayout) view.findViewById(R.id.ll_multisel);
		cb_selAll=(CheckBox) view.findViewById(R.id.cb_selAll);
		myListview.setEmptyView(emptyLayout);
		myListview.setPullRefreshEnable(true);
		myListview.setPullLoadEnable(false);
		myListview.setXListViewListener(this);
		mFab = (FloatingActionButton) view.findViewById(R.id.fab);
		mFab1 = (FloatingActionButton) view.findViewById(R.id.fab1);
		segmentedGroup2=(SegmentedGroup)view.findViewById(R.id.segmentedGroup2);
		btnLeft.setVisibility(View.VISIBLE);
		btnLeft.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.bg_btn_left_nor, 0, 0, 0);
		tvTitle.setText(title);
		adapter = new AchieveAdapter();
		myListview.setAdapter(adapter);
		lyLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		// 重新加载
		failedLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getAchievesItem(true,0);
			}
		});

		mFab.hide();
		mFab1.hide();
		mFab.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				popFilterDlg();
			}
		});
		mFab1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				showbatchpass();
			}
		});
		myListview.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
				if (firstVisibleItem > mPreviousVisibleItem) {
					mFab.hide();
					mFab1.hide();
				} else if (firstVisibleItem < mPreviousVisibleItem && achievementItem!=null && achievementItem.getFilterArr()!=null && achievementItem.getFilterArr().length()>0)  {
					mFab.show();
					mFab1.show();
				}
				mPreviousVisibleItem = firstVisibleItem;
			}
		});

		getAchievesItem(true,0);
		return view;
	}


	/**
	 * 显示加载失败提示页
	 */
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
	@Override
	public void onRefresh() {
		getAchievesItem(false,0);
	}

	@Override
	public void onLoadMore() {
		if(!isLoading)
		{
			if(achievementItem!=null && achievementItem.getPage()>0)
				getAchievesItem(false,achievementItem.getPage()+1);
		}
	}
	/**
	 * 功能描述:获取通知内容
	 * 
	 * @author shengguo 2014-4-16 上午11:12:43
	 * 
	 */
	public void getAchievesItem(boolean flag,int page) {
		showProgress(flag);
		Log.d(TAG, "--------" + String.valueOf(new Date().getTime()));
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "----------datatime:" + datatime);
		Log.d(TAG, "----------checkCode:" + checkCode + "++");
		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
			jo.put("version", CampusApplication.getVersion());
			jo.put("page", page);
			if(segmentedGroup2.getVisibility()==VISIBLE)
			{
				for(int i = 0 ;i < segmentedGroup2.getChildCount();i++) {
					RadioButton rb = (RadioButton) segmentedGroup2.getChildAt(i);
					if (rb.isChecked()){
						jo.put("curGroupId",rb.getId());
						break;
					}
				}
			}
			if(achievementItem!=null && achievementItem.getFilterArr()!=null)
				jo.put("过滤条件",achievementItem.getFilterArr());
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		Log.d(TAG, "------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getSchoolItem(params, interfaceName, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "----response" + e.getMessage());
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "----response" + response);
				Message msg = new Message();
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	//submit按钮
		private void submitButtonClick(String url) {

			long datatime = System.currentTimeMillis();
			String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");

			JSONObject jo = new JSONObject();
			try {
				jo.put("用户较验码", checkCode);
				jo.put("DATETIME", datatime);
			
			} catch (JSONException e1) {
				e1.printStackTrace();
			}
			dialog = DialogUtility.createLoadingDialog(getActivity(),
					"数据处理中...");
			dialog.show();

			String base64Str = Base64.encode(jo.toString().getBytes());
		
			CampusParameters params = new CampusParameters();
			params.add(Constants.PARAMS_DATA, base64Str);
			int pos=interfaceName.indexOf("?");
			String preUrl=interfaceName;
			if(pos>-1)
				preUrl=interfaceName.substring(0, pos);
			CampusAPI.getSchoolItem(params,
					preUrl + url,
					new RequestListener() {

						@Override
						public void onIOException(IOException e) {
							// TODO Auto-generated method stub

						}

						@Override
						public void onError(CampusException e) {
							Log.d(TAG, "----response" + e.getMessage());
							if(dialog != null){
								dialog.dismiss();
							}
							Message msg = new Message();
							msg.what = -1;
							msg.obj = e.getMessage();
							mHandler.sendMessage(msg);
						}

						@Override
						public void onComplete(String response) {
							Log.d(TAG, "----response" + response);
							if(dialog != null){
								dialog.dismiss();
							}
							Message msg = new Message();
							msg.what = 3;
							msg.obj = response;
							mHandler.sendMessage(msg);
						}
					});
		}
	
	@SuppressLint({ "DefaultLocale", "NewApi" })
	class AchieveAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return achievements.size();
		}

		@Override
		public Object getItem(int position) {
			return achievements.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		
		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;

			if (null == convertView) {
				convertView = inflater.inflate(
						R.layout.school_achievement_or_question_item, parent,
						false);
				holder = new ViewHolder();

				holder.icon = (ImageView) convertView
						.findViewById(R.id.iv_icon);
				holder.title = (TextView) convertView
						.findViewById(R.id.tv_title);
				holder.total = (TextView) convertView
						.findViewById(R.id.thieDescription);
				holder.rank = (TextView) convertView
						.findViewById(R.id.tv_right);
				holder.huizong = (TextView) convertView
						.findViewById(R.id.theTotalMoney);
				holder.iv_arrow=(ImageView) convertView
						.findViewById(R.id.iv_right);
				holder.iv_menu=(ImageView) convertView
						.findViewById(R.id.iv_right1);
				holder.cb_checkitem=(CheckBox)convertView.findViewById(R.id.cb_checkitem);
				holder.pb_bottom=(ProgressBar)convertView.findViewById(R.id.pb_bottom);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final Achievement achievement = (Achievement) getItem(position);
			AQuery aq = new AQuery(convertView);
			String imagurl = achievement.getIcon();
			Log.d(TAG, "----imagurl:" + imagurl);
			if (imagurl != null && !imagurl.equals("")) {
				aq.id(holder.icon).image(imagurl);
			}
	
			holder.title.setText(achievement.getTitle());
			holder.total.setText(achievement.getTotal());
			if(achievement.getTotal().length()>0)
				holder.total.setVisibility(View.VISIBLE);
			else
				holder.total.setVisibility(View.GONE);
			holder.rank.setText(achievement.getRank());
			holder.huizong.setText(achievement.getThirdline());
			if(achievement.getThirdline().length()>0)
				holder.huizong.setVisibility(View.VISIBLE);
			else
				holder.huizong.setVisibility(View.GONE);
			if(achievement.getThecolor()!=null && achievement.getThecolor().length()>0)
			{
				if(achievement.getThecolor().toLowerCase().equals("red"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_red));
				else if(achievement.getThecolor().toLowerCase().equals("blue"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_blue));
				else if(achievement.getThecolor().toLowerCase().equals("brown"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_brown));
				else if(achievement.getThecolor().toLowerCase().equals("pink"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_pink));
				else if(achievement.getThecolor().toLowerCase().equals("goldenrod"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_goldenrod));
				else if(achievement.getThecolor().toLowerCase().equals("blueviolet"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_blueviolet));
				else if(achievement.getThecolor().toLowerCase().equals("gray"))
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_gray));
				else
					holder.total.setBackground(getResources().getDrawable(R.drawable.school_achievement_bg));
			}
			if(achievement.getExtraMenu()==null)
			{
				holder.iv_arrow.setVisibility(View.VISIBLE);
				holder.iv_menu.setVisibility(View.GONE);
				if(achievement.getDetailUrl().length()==0)
					holder.iv_arrow.setVisibility(View.GONE);
			}
			else {
				holder.iv_arrow.setVisibility(View.GONE);
				holder.iv_menu.setVisibility(View.VISIBLE);
			}
			if(bShowMutiSel) {
				holder.cb_checkitem.setVisibility(VISIBLE);
				holder.cb_checkitem.setChecked(achievement.isIfChecked());
				holder.cb_checkitem.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
						if(!compoundButton.isPressed())
							return ;
						achievement.setIfChecked(b);
						//achievements.set(position,achievement);
					}
				});
			}
			else
				holder.cb_checkitem.setVisibility(View.GONE);
			if(achievement.getProgress()>-1)
			{
				holder.pb_bottom.setVisibility(VISIBLE);
				holder.pb_bottom.setProgress(achievement.getProgress());
			}
			else
				holder.pb_bottom.setVisibility(View.GONE);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if(bShowMutiSel) {
						CheckBox cb_checkitem=(CheckBox)v.findViewById(R.id.cb_checkitem);
						cb_checkitem.setChecked(!cb_checkitem.isChecked());
						achievement.setIfChecked(cb_checkitem.isChecked());
						//achievements.set(position,achievement);
						return;
					}
					String DetailUrl = achievement.getDetailUrl();
					if (AppUtility.isNotEmpty(DetailUrl)) {
						Log.d(TAG,"----notice.getEndUrl():"+ achievement.getDetailUrl());
						
						if(DetailUrl.length()>0 && !DetailUrl.equals("null"))
						{
							Intent intent =null;
							int pos=interfaceName.indexOf("?");
							String preUrl=interfaceName;
							if(pos>-1)
								preUrl=interfaceName.substring(0, pos);
							if(achievement.getTemplateName()==null || achievement.getTemplateName().length()==0)
							{
								intent=new Intent(getActivity(),SchoolDetailActivity.class);
								intent.putExtra("templateName", "成绩");
							}
							else
							{
								if(achievement.getTemplateName().equals("浏览器")) {
									intent = new Intent(getActivity(), WebSiteActivity.class);
									intent.putExtra("url", DetailUrl);
									String jiaoyanma = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
									String jumpurl=DetailUrl;
									if(jumpurl.indexOf("?")>-1)
										jumpurl+="&";
									else
										jumpurl+="?";
									jumpurl+="jiaoyanma=" + Base64.safeUrlbase64(jiaoyanma);
									intent.putExtra("url",jumpurl);
								}
								else {
									if (achievement.getTemplateGrade().equals("main"))
										intent = new Intent(getActivity(), SchoolActivity.class);
									else
										intent = new Intent(getActivity(), SchoolDetailActivity.class);
									intent.putExtra("templateName", achievement.getTemplateName());
								}
							}
							intent.putExtra("interfaceName", preUrl+DetailUrl);
							intent.putExtra("title", title);
							startActivityForResult(intent,101);
						}
					}
				}
			});
			holder.iv_menu.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					JSONObject popMenu=achievement.getExtraMenu();
					if(popMenu!=null && popMenu.length()>0)
					{
						String[] popMenuStr=new String[popMenu.length()+1];
						Iterator<?> it = popMenu.keys();
						int i=0;
						while(it.hasNext()){
							popMenuStr[i]= (String) it.next().toString();
							i++;
						}
						popMenuStr[popMenuStr.length-1]="取消";
						showUserTypeDialog(popMenuStr,achievement);
					}
				}

			});
			final JSONObject linkobj=achievement.getIconLink();
			if(linkobj!=null) {
				holder.icon.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if(linkobj.optInt("type")==1)
						{
							Intent intent = new Intent(getActivity(),
									StudentInfoActivity.class);
							intent.putExtra("studentId", linkobj.optString("value"));
							intent.putExtra("userImage", linkobj.optString("icon"));
							startActivity(intent);
						}
						else {
							Intent intent = new Intent(getActivity(),
									ShowPersonInfo.class);
							intent.putExtra("studentId", linkobj.optString("value"));
							startActivity(intent);
						}
					}

				});
			}
			else
				holder.icon.setOnClickListener(null);
			return convertView;
		}

		class ViewHolder {
			ImageView icon;
			TextView title;
			TextView total;
			TextView rank;
			TextView huizong;
			ImageView iv_arrow;
			ImageView iv_menu;
			CheckBox cb_checkitem;
			ProgressBar pb_bottom;
		}
		
	}
	private void showUserTypeDialog(String[] data,Achievement achievement) {
		userTypeDialog = new Dialog(getActivity(), R.style.dialog);
		View view = inflater.inflate(
				R.layout.view_exam_login_dialog, null);
		ListView mList = (ListView) view.findViewById(R.id.list);
		TextView tvTitle=(TextView)view.findViewById(R.id.dialogtitle);
		tvTitle.setVisibility(View.GONE);
		DialogAdapter dialogAdapter = new DialogAdapter(data,achievement);
		mList.setAdapter(dialogAdapter);
		Window window = userTypeDialog.getWindow();
		window.setGravity(Gravity.BOTTOM);// 在底部弹出
		window.setWindowAnimations(R.style.CustomDialog);
		window.setGravity(Gravity.CENTER);
		userTypeDialog.setContentView(view);
		userTypeDialog.show();

	}
	public class DialogAdapter extends BaseAdapter {
		String[] arrayData;
		Achievement achievement;
		public DialogAdapter(String[] array,Achievement achievement) {
			this.arrayData = array;
			this.achievement=achievement;
		}

		@Override
		public int getCount() {
			return arrayData == null ? 0 : arrayData.length;
		}

		@Override
		public Object getItem(int position) {
			return arrayData[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup arg2) {
			ViewHolder holder = null;
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = inflater.inflate(
						R.layout.view_testing_pop, arg2,false);

				holder.title = (TextView) convertView.findViewById(R.id.time);

				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final String text = arrayData[position];
			holder.title.setText(text);
			holder.title.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					PrefUtility.put(Constants.PREF_CHECK_TEST, false);
					if ("删除".equals(text)) {
						new AlertDialog.Builder(getActivity())
								.setIcon(android.R.drawable.ic_dialog_alert)
								.setTitle("确认对话框")
								.setMessage("是否确认删除?")
								.setPositiveButton("是", new DialogInterface.OnClickListener()
								{
									@Override
									public void onClick(DialogInterface dialog, int which)
									{
										JSONObject queryObj=AppUtility.parseQueryStrToJson(interfaceName);
										JSONObject queryObj1=AppUtility.parseQueryStrToJson(achievement.getExtraMenu().optString(text));
										JSONObject jo = new JSONObject();
										try {
											Iterator it = queryObj1.keys();
											while (it.hasNext()) {
												String key = (String) it.next();
												String value = queryObj1.getString(key);
												queryObj.put(key, value);
											}

										} catch (JSONException e1) {
											e1.printStackTrace();
										}

										String url = AppUtility.removeURLQuery(interfaceName) + "?" + AppUtility.jsonToUrlQuery(queryObj);
										CampusAPI.httpPost(url, jo, mHandler, 2);

									}
								})
								.setNegativeButton("否", null)
								.show();
					}
					else if ("取消".equals(text))
					{

					}
					else
					{
						JSONObject queryObj=AppUtility.parseQueryStrToJson(achievement.getExtraMenu().optString(text));
						if(queryObj.optString("templateName").length()>0) {

							Intent intent =null;
							if(queryObj.optString("templateGrade").equals("main"))
								intent=new Intent(getActivity(),SchoolActivity.class);
							else
								intent=new Intent(getActivity(),SchoolDetailActivity.class);
							intent.putExtra("templateName", queryObj.optString("templateName"));
							int pos=interfaceName.indexOf("?");
							String preUrl=interfaceName;
							if(pos>-1)
								preUrl=interfaceName.substring(0, pos);
							intent.putExtra("interfaceName", preUrl+achievement.getExtraMenu().optString(text));
							//intent.putExtra("title", title);
							startActivityForResult(intent,101);
						}
						else {
							String url = AppUtility.removeURLQuery(interfaceName) + "?" + AppUtility.jsonToUrlQuery(queryObj);
							CampusAPI.httpPost(url, new JSONObject(), mHandler, 2);
						}
					}
					userTypeDialog.dismiss();
				}
			});
			return convertView;
		}
		class ViewHolder {
			TextView title;
		}
	}
	private void popFilterDlg()
	{
		final LinearLayout layout=new LinearLayout(getActivity());
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.setPadding(10,10,10,10);
		for(int i=0;i<achievementItem.getFilterArr().length();i++)
		{
			JSONObject filterObj=achievementItem.getFilterArr().optJSONObject(i);
			if(filterObj!=null)
			{
				if(filterObj.optString("类型").equals("文本框"))
				{
					final EditText et_billid=new EditText(getActivity());
					et_billid.setContentDescription(filterObj.optString("标题"));
					et_billid.setHint(filterObj.optString("标题"));
					if(filterObj.optString("输入法").equals("数字"))
						et_billid.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_SIGNED);
					et_billid.setSingleLine();
					et_billid.setText(filterObj.optString("值"));
					layout.addView(et_billid);
				}
				else if(filterObj.optString("类型").equals("下拉框"))
				{
					Spinner sp_filter1 = new Spinner(getActivity());
					sp_filter1.setContentDescription(filterObj.optString("标题"));
					String[] mItems1 = new String[filterObj.optJSONArray("选项").length()];
					int selection=0;
					for (int j = 0; j < filterObj.optJSONArray("选项").length();j++) {
						mItems1[j] = filterObj.optJSONArray("选项").optString(j);
						if(filterObj.optString("值").equals(filterObj.optJSONArray("选项").optString(j)))
							selection=j;
					}
					ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, mItems1);
					adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
					sp_filter1.setAdapter(adapter);
					sp_filter1.setSelection(selection);
					layout.addView(sp_filter1);
					sp_filter1.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,100));
				}
			}
		}

		new AlertDialog.Builder(getActivity()).setTitle("过滤条件").setView(layout)
				.setPositiveButton("确定", new DialogInterface.OnClickListener()
				{
					@Override
					public void onClick(DialogInterface dialog, int which) {

						for(int i=0;i<layout.getChildCount();i++)
						{
							View view=layout.getChildAt(i);
							String key="";
							String value="";
							if(view instanceof EditText)
							{
								EditText editText=(EditText)view;
								key= (String) editText.getContentDescription();
								value=editText.getText().toString();
							}
							else if(view instanceof Spinner)
							{
								Spinner spinner=(Spinner)view;
								key= (String) spinner.getContentDescription();
								value=spinner.getSelectedItem().toString();
							}
							for(int j=0;j<achievementItem.getFilterArr().length();j++)
							{
								JSONObject item=achievementItem.getFilterArr().optJSONObject(j);
								if(item.optString("标题").equals(key))
								{
									try {
										item.put("值",value);
									} catch (JSONException e) {
										e.printStackTrace();
									}
								}
							}
						}
						getAchievesItem(true,0);
					}
				}).setNegativeButton("取消", null).show();

	}
	private void showbatchpass()
	{
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

					for(int i=0;i<achievements.size();i++)
					{
						Achievement item=achievements.get(i);
						item.setIfChecked(b);
					}
					adapter.notifyDataSetChanged();
				}

			});
			for(int i=0;i<achievementItem.getMutiSelArr().length();i++)
			{
				final JSONObject jo=achievementItem.getMutiSelArr().optJSONObject(i);
				if(jo!=null)
				{
					Button btn=new Button(getActivity());
					LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
					layoutParams.setMargins(10,0,10,0);//4个参数按顺序分别是左上右下
					layoutParams.height=95;
					btn.setLayoutParams(layoutParams);
					btn.setText(jo.optString("name"));
					ll_btns.addView(btn);
					btn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View view) {
							String selIdStr="";
							for(int i=0;i<achievements.size();i++)
							{
								Achievement item=achievements.get(i);
								if(item.isIfChecked())
								{
									if(selIdStr.length()>0)
										selIdStr+=","+item.getId();
									else
										selIdStr=item.getId();
								}
							}
							if(selIdStr.length()==0) {
								AppUtility.showToastMsg(getActivity(),"请先勾选记录");
								return;
							}
							String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
							JSONObject queryObj=AppUtility.parseQueryStrToJson(jo.optString("url"));
							if(queryObj.optString("templateName").length()>0) {
								Intent intent = new Intent(getActivity(), SchoolDetailActivity.class);
								intent.putExtra("templateName", queryObj.optString("templateName"));
								intent.putExtra("interfaceName", jo.optString("url")+"&ID="+selIdStr);
								intent.putExtra("title", title);
								startActivityForResult(intent, 101);
							}
							else {
								JSONObject jo1 = new JSONObject();
								try {
									jo1.put("用户较验码", checkCode);
									jo1.put("selIdStr", selIdStr);
									Iterator it = queryObj.keys();
									while (it.hasNext()) {
										String key = (String) it.next();
										String value = queryObj.getString(key);
										jo1.put(key, value);
									}

								} catch (JSONException e1) {
									e1.printStackTrace();
								}
								CampusAPI.httpPost(jo.optString("url"),jo1, mHandler, 2);
							}
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
			ll_multisel.setVisibility(VISIBLE);
		}
		else
			ll_multisel.setVisibility(View.GONE);
		adapter.notifyDataSetChanged();
	}
}
