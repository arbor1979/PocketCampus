package com.ruanyun.campus.teacher.activity;


import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;

import com.androidquery.AQuery;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.ruanyun.campus.teacher.CampusApplication;
import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.activity.TabHostActivity.MenuListener;
import com.ruanyun.campus.teacher.adapter.SchoolWorkAdapter;
import com.ruanyun.campus.teacher.adapter.SchoolWorkGroupAdapter;
import com.ruanyun.campus.teacher.adapter.SectionedSpanSizeLookup;
import com.ruanyun.campus.teacher.api.CampusAPI;
import com.ruanyun.campus.teacher.api.CampusException;
import com.ruanyun.campus.teacher.api.CampusParameters;
import com.ruanyun.campus.teacher.api.RequestListener;
import com.ruanyun.campus.teacher.base.Constants;
import com.ruanyun.campus.teacher.db.DatabaseHelper;
import com.ruanyun.campus.teacher.entity.Notice;
import com.ruanyun.campus.teacher.entity.NoticesItem;
import com.ruanyun.campus.teacher.entity.SchoolWorkItem;
import com.ruanyun.campus.teacher.entity.User;
import com.ruanyun.campus.teacher.service.Alarmreceiver;
import com.ruanyun.campus.teacher.util.AppUtility;
import com.ruanyun.campus.teacher.util.Base64;
import com.ruanyun.campus.teacher.util.PrefUtility;

public class TabSchoolActivtiy extends FragmentActivity {
	private String TAG = "TabSchoolActivtiy";
	private LinearLayout loadingLayout;
	private LinearLayout contentLayout;
	private LinearLayout failedLayout;
	private LinearLayout emptyLayout;
	private Button btnLeft;
	private AQuery aq;
	private RecyclerView myGridView;
	private SchoolWorkGroupAdapter adapter;
	private List<SchoolWorkItem> schoolWorkItems = new ArrayList<SchoolWorkItem>();
	private List<Notice> notices = new ArrayList<Notice>();
	private Dao<Notice, Integer> noticeInfoDao;
	private DatabaseHelper database;
	private User user;
	private boolean isruning;
	private Timer timer; 
	static LinearLayout layout_menu;
	private Dao<User, Integer> userDao;
	public final static int SCANNIN_GREQUEST_CODE = 2;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
				case -1:
					showFetchFailedView();
					AppUtility.showErrorToast(TabSchoolActivtiy.this, msg.obj.toString());
					break;
				case 0:
					showProgress(false);
					String result = msg.obj.toString();
					String resultStr = "";
					if (AppUtility.isNotEmpty(result)) {
						try {
							resultStr = new String(Base64.decode(result.getBytes("GBK")));
							Log.d(TAG, "----resultStr:"+resultStr);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}

					if (AppUtility.isNotEmpty(resultStr)) {
						schoolWorkItems.clear();
						try {
							JSONArray jo = new JSONArray(resultStr);
							for (int i = 0; i < jo.length(); i++) {
								SchoolWorkItem swItem=new SchoolWorkItem(jo.getJSONObject(i));
								schoolWorkItems.add(swItem);
							}
							adapter.setSchoolWorkItems(schoolWorkItems);


						/*
						if(timer==null)
						{
							timer=new Timer();
							timer.schedule(new myTask(),0,10000);
						}
						*/
							getUnreadCount();
							for(SchoolWorkItem item:schoolWorkItems)
							{
								if(item.getTemplateName().equals("通知"))
								{
									getNoticesItem(item.getInterfaceName(),item.getWorkText());
								}
							}

						} catch (JSONException e) {
							showFetchFailedView();
							e.printStackTrace();
						}
					}
					break;
				case 1:

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
					}

					if (AppUtility.isNotEmpty(resultStr)) {
						try {
							JSONObject jo = new JSONObject(resultStr);
							String res = jo.optString("结果");
							if(AppUtility.isNotEmpty(res)){
								AppUtility.showToastMsg(TabSchoolActivtiy.this, res);
							}else{
								NoticesItem noticesItem = new NoticesItem(jo);
								Log.d(TAG, "--------noticesItem.getNotices().size():"
										+ noticesItem.getNotices().size());
								notices = noticesItem.getNotices();

								for(Notice item:notices)
								{

									item.setNewsType(noticesItem.getTitle());
									item.setUserNumber(user.getUserNumber());
									Notice nt=noticeInfoDao.queryBuilder().where().eq("id",item.getId()).and().eq("newsType", item.getNewsType()).and().eq("userNumber",user.getUserNumber()).queryForFirst();
									if(nt==null)
										noticeInfoDao.create(item);
									else {
										nt.updateself(item);
										noticeInfoDao.update(nt);
									}
								}
								getUnreadByTitle(noticesItem.getTitle());


							}
						} catch (JSONException e) {
							showFetchFailedView();
							e.printStackTrace();
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						showFetchFailedView();
					}
					break;

				case 2:
					result = msg.obj.toString();
					resultStr = "";
					if (AppUtility.isNotEmpty(result)) {
						try {
							resultStr = new String(Base64.decode(result
									.getBytes("GBK")));
							Log.d(TAG, resultStr);
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
					}

					if (AppUtility.isNotEmpty(resultStr)) {
						try {
							JSONObject jo = new JSONObject(resultStr);
							if(jo!=null)
							{
								for(SchoolWorkItem item:schoolWorkItems)
								{
									if(!item.getTemplateName().equals("通知"))
										item.setUnread(jo.optInt(item.getWorkText()));

								}
								adapter.setSchoolWorkItems(schoolWorkItems);

							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
					break;

			}
		}
	};



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "----------------onCreate-----------------------");

		setContentView(R.layout.tab_activity_school);
		aq = new AQuery(this);

		myGridView = (RecyclerView) findViewById(R.id.recyclerView);
		btnLeft = (Button) findViewById(R.id.btn_left);
		layout_menu = (LinearLayout) findViewById(R.id.layout_btn_left);
		loadingLayout = (LinearLayout) findViewById(R.id.data_load);
		contentLayout = (LinearLayout) findViewById(R.id.content_layout);
		failedLayout = (LinearLayout) findViewById(R.id.empty_error);
		emptyLayout = (LinearLayout) findViewById(R.id.empty);

		aq.id(R.id.tv_title).text("校内");
		btnLeft.setBackgroundResource(R.drawable.bg_title_homepage_back);
		btnLeft.setVisibility(View.VISIBLE);
		adapter=new SchoolWorkGroupAdapter(TabSchoolActivtiy.this, schoolWorkItems);
		int screenWidth=AppUtility.getAndroiodScreenProperty(this);
		float fontscale=AppUtility.getSysConfigFontSize();
		int spancount= (int) Math.floor(screenWidth / (70*fontscale));
		GridLayoutManager manager = new GridLayoutManager(this,spancount);
		//设置header
		manager.setSpanSizeLookup(new SectionedSpanSizeLookup(adapter,manager));
		myGridView.setLayoutManager(manager);
		myGridView.setAdapter(adapter);

		//重新加载
		failedLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getSchool();
			}
		});
		emptyLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getSchool();
			}
		});
		try {
			noticeInfoDao = getHelper().getNoticeInfoDao();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		user=((CampusApplication)getApplicationContext()).getLoginUserObj();
		getSchool();
		registerBoradcastReceiver();

		layout_menu.setOnClickListener(TabHostActivity.menuListener);
		if(user.getUserType().equals("学生")) {
			if (Build.VERSION.SDK_INT >= 23) {
				if (AppUtility.checkPermission(getParent(), 5, Manifest.permission.ACCESS_FINE_LOCATION))
					AppUtility.beginGPS(getApplication());
			} else
				AppUtility.beginGPS(getApplication());
		}
	}

	/*
	class myTask extends TimerTask {
		public void run ( ) {
			if(isruning && schoolWorkItems.size()>0)
				getUnreadCount();
		}
	};
	*/
	@Override
	protected void onDestroy() {
		if (timer != null) {
			timer.cancel( );
			timer = null;
		}
		super.onDestroy();
		unregisterReceiver(mBroadcastReceiver);
	}
	private boolean getUnreadByTitle(String title)
	{
		List<Notice> unreadList=null;
		boolean flag=false;
		try {
			unreadList = noticeInfoDao.queryBuilder().where().eq("newsType",title).and().eq("userNumber", user.getUserNumber()).and().eq("ifread","0").query();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(unreadList!=null)
		{
			for(SchoolWorkItem item:schoolWorkItems)
			{
				if(item.getWorkText().equals(title))
				{
					item.setUnread(unreadList.size());
					flag=true;
					break;
				}
			}
		}
		if(flag)
			adapter.setSchoolWorkItems(schoolWorkItems);
		return flag;
	}
	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);

		}
		return database;
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
	//获取校内item选项详情
	public void getSchool() {
		showProgress(true);
		Log.d(TAG, "--------"+String.valueOf(new Date().getTime()));
		long datatime =System.currentTimeMillis();
		String checkCode=PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "----------datatime:"+datatime);
		Log.d(TAG, "----------checkCode:"+checkCode+"++");
		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("学生状态", user.getsStatus());
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		Log.d(TAG, "---------------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getSchool(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "----response"+e.getMessage());
				Message msg=new Message();
				msg.what=-1;
				msg.obj= e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "----response"+response);

				Message msg=new Message();
				msg.what=0;
				msg.obj= response;
				mHandler.sendMessage(msg);
			}
		});
	}
	/**
	 * 功能描述:获取通知内容
	 *
	 * @author shengguo 2014-4-16 上午11:12:43
	 *
	 */
	public void getNoticesItem(String interfaceName,String showName) {

		Log.d(TAG, "--------" + String.valueOf(new Date().getTime()));
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "----------datatime:" + datatime);
		Log.d(TAG, "----------checkCode:" + checkCode + "++");
		int lastId=0;
		try {
			Notice nt=noticeInfoDao.queryBuilder().orderBy("id", false).where().eq("newsType", showName).and().eq("userNumber", user.getUserNumber()).queryForFirst();
			if(nt!=null)
				lastId=nt.getId();
		} catch (SQLException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
			jo.put("LASTID", lastId);
			jo.put("发布对象", user.getRootDomain());
		} catch (JSONException e1) {
			// TODO Auto-generated catch block
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
				msg.what = 1;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}

	public void getUnreadCount() throws JSONException{


		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject funcObj=new JSONObject();
		for(SchoolWorkItem item:schoolWorkItems) {
			if(item.getIfShowCount().equals("是"))
			{
				String key=item.getWorkText();
				funcObj.put(key,item.getInterfaceName());
			}

		}
		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
			jo.put("funcObj",funcObj);

		} catch (JSONException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		Log.d(TAG, "------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getSchoolItem(params, "count.php", new RequestListener() {

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
				msg.what = 2;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}
	@Override
	protected void onStart() {
		super.onStart();
		isruning = true;
		Log.d(TAG, "生命周期:Start");
		((TabHostActivity)getParent()).callBack=callBack;
		//if(needCount)
		//	getUnreadCount();
	}

	@Override
	protected void onStop() {
		super.onStop();
		isruning = false;
		Log.d(TAG, "生命周期:Stop");
	}
	public void registerBoradcastReceiver() {
		IntentFilter myIntentFilter = new IntentFilter();
		myIntentFilter.addAction("refreshUnread");
		//myIntentFilter.addAction("Campus_reloadNotice");
		// 注册广播
		registerReceiver(mBroadcastReceiver, myIntentFilter);
	}
	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (action.equals("refreshUnread")) {

				String refreshTitle = intent.getStringExtra("title");
				getUnreadByTitle(refreshTitle);

			}
			//else if(action.equals("Campus_reloadNotice"))
			//{
			//	getSchool();
			//}
		}
	};
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode ==  SCANNIN_GREQUEST_CODE){
			if(resultCode == RESULT_OK){
				Bundle bundle = data.getExtras();
				String result = bundle.getString("result");
				String jumpurl=bundle.getString("jumpurl");

				String template=AppUtility.findUrlQueryString(jumpurl,"template");
				String templategrade=AppUtility.findUrlQueryString(jumpurl,"templategrade");
				String targettitle=AppUtility.findUrlQueryString(jumpurl,"targettitle");
				if(template.length()==0)
					template="浏览器";
				if(jumpurl.indexOf("?")>0)
					jumpurl+="&";
				else
					jumpurl+="?";
				try {
					result = new String(Base64.decode(result.getBytes("GBK")));
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				if(result!=null && result.length()>0) {
					if(template.equals("浏览器"))
					{
						Intent contractIntent = new Intent(this,WebSiteActivity.class);
						String jiaoyanma = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
						jumpurl+= "scancode="+Base64.safeUrlbase64(bundle.getString("result"))+"&jiaoyanma=" + Base64.safeUrlbase64(jiaoyanma);
						contractIntent.putExtra("url",jumpurl);
						contractIntent.putExtra("title",targettitle);
						startActivity(contractIntent);
					}
					else
					{
						jumpurl+="scancode="+Base64.safeUrlbase64(bundle.getString("result"));
						Intent intent;
						if (templategrade.equals("main"))
							intent = new Intent(this, SchoolActivity.class);
						else
							intent = new Intent(this, SchoolDetailActivity.class);
						intent.putExtra("title", targettitle);
						intent.putExtra("interfaceName",jumpurl);
						intent.putExtra("templateName",template);
						startActivity(intent);
					}
				}
				else
					AppUtility.showToastMsg(this, "扫描结果为空");
				Log.d(TAG,result);
			}
		}
	}
	public AppUtility.CallBackInterface callBack=new AppUtility.CallBackInterface()
	{

		@Override
		public void getLocation1(int rqcode) {
			// TODO Auto-generated method stub
			AppUtility.beginGPS(getApplication());
		}
		@Override
		public void getPictureByCamera1(int rqcode) {
			// TODO Auto-generated method stub
		}

		@Override
		public void getPictureFromLocation1() {
			// TODO Auto-generated method stub
		}

		@Override
		public void sendCall1() {
			// TODO Auto-generated method stub

		}

		@Override
		public void sendMsg1() {
			// TODO Auto-generated method stub

		}
		@Override
		public void getFujian1() {
			// TODO Auto-generated method stub
		}

	};
}
