package com.ruanyun.campus.teacher.activity;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import androidx.core.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.androidquery.AQuery;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.ruanyun.campus.teacher.BuildConfig;
import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.adapter.MyPictureAdapter;
import com.ruanyun.campus.teacher.api.CampusAPI;
import com.ruanyun.campus.teacher.api.CampusException;
import com.ruanyun.campus.teacher.api.CampusParameters;
import com.ruanyun.campus.teacher.api.RequestListener;
import com.ruanyun.campus.teacher.base.Constants;
import com.ruanyun.campus.teacher.db.DatabaseHelper;
import com.ruanyun.campus.teacher.entity.AlbumImageInfo;
import com.ruanyun.campus.teacher.entity.DownloadSubject;
import com.ruanyun.campus.teacher.entity.ImageItem;
import com.ruanyun.campus.teacher.entity.StudentSummary;
import com.ruanyun.campus.teacher.entity.TeacherInfo;
import com.ruanyun.campus.teacher.util.AppUtility;
import com.ruanyun.campus.teacher.util.Base64;
import com.ruanyun.campus.teacher.util.DateHelper;
import com.ruanyun.campus.teacher.util.DialogUtility;
import com.ruanyun.campus.teacher.util.FileUtility;
import com.ruanyun.campus.teacher.util.ImageUtility;
import com.ruanyun.campus.teacher.util.PrefUtility;
import com.ruanyun.campus.teacher.util.AppUtility.CallBackInterface;
import com.ruanyun.campus.teacher.widget.NonScrollableGridView;
import com.ruanyun.campus.teacher.widget.wraplayout.WrapLayout;

public class SummaryClassActivity extends Activity {
	public static final int REQUEST_CODE_TAKE_PICTURE = 2;// //设置图片操作的标志
	public static final int REQUEST_CODE_TAKE_CAMERA = 1;// //设置拍照操作的标志
	private DatabaseHelper database;
	private Dao<TeacherInfo, Integer> teacherInfoDao;
	private TeacherInfo teacherInfo;
	private Dialog mLoadingDialog;
	private AQuery aq;
	private Button btnLeft;
	private static final String TAG = "SummaryClassActivity";
	private String subjectid, userType, picturePath, delImagePath;
	private RatingBar ratingBar1, ratingBar2;
	private StudentSummary studentSummary;
	private EditText et1, et2,et3;
	private NonScrollableGridView myGridView,myGridView1,myGridView2;
	private MyPictureAdapter myPictureAdapter,myPictureAdapter1,myPictureAdapter2;
	private ArrayList<String> picturePaths = new ArrayList<String>();// 选中的图片路径
	private ArrayList<String> picturePaths1 = new ArrayList<String>();// 选中的图片路径
	private ArrayList<String> picturePaths2 = new ArrayList<String>();// 选中的图片路径
	private int submitImageCount = 0, size = 10;
	private LinearLayout loadingLayout;
	private ScrollView contentLayout;
	private LinearLayout failedLayout;
	private WrapLayout layout_rizhi_shouduan,layout_rizhi_pingtai;
	private String imagetype;
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {

		public void handleMessage(Message msg) {
			String result = "";
			String resultStr = "";
			switch (msg.what) {
			case -1:
				showFetchFailedView();
				AppUtility.showErrorToast(SummaryClassActivity.this,
						msg.obj.toString());
				break;
			case 0:
				closeDialog();
				result = msg.obj.toString();
				resultStr = "";
				try {
					resultStr = new String(
							Base64.decode(result.getBytes("GBK")));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				Log.d(TAG, "resultStr:" + resultStr);

				try {
					JSONObject jo = new JSONObject(resultStr);
					resultStr = jo.optString("成功");
					if (resultStr.equals("1")) {
						AppUtility.showToastMsg(SummaryClassActivity.this,
								"保存成功！");
						teacherInfoDao.update(teacherInfo);
						Log.d(TAG, "----------------->结束保存数据：" + new Date());
					} else {
						AppUtility.showToastMsg(SummaryClassActivity.this,
								jo.optString("结果"));
					}
				} catch (Exception e) {
					e.printStackTrace();
					AppUtility.showToastMsg(SummaryClassActivity.this, "保存失败！");
				}

				break;
			case 1:// 获取学生信息成功
				showProgress(false);

				result = msg.obj.toString();
				try {
					resultStr = new String(
							Base64.decode(result.getBytes("GBK")));
					Log.d(TAG, "resultStr" + resultStr);
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}
				try {
					JSONObject jo = new JSONObject(resultStr);
					studentSummary = new StudentSummary(jo);
					Boolean bCanAdd=true;
					if (userType.equals("家长"))
						bCanAdd=false;
					if (userType.equals("老师")) {
						initTeacherDate();

					} else {
						initStudentDate();

					}
					myPictureAdapter = new MyPictureAdapter(SummaryClassActivity.this,
							bCanAdd, picturePaths, size,"课堂笔记");

					myPictureAdapter1 = new MyPictureAdapter(SummaryClassActivity.this,
							bCanAdd, picturePaths1, size,"课堂作业");

					myPictureAdapter2 = new MyPictureAdapter(SummaryClassActivity.this,
							bCanAdd, picturePaths2, size,"课堂情况");

					myPictureAdapter.setFrom(TAG);
					myPictureAdapter1.setFrom(TAG);
					myPictureAdapter2.setFrom(TAG);
					myGridView.setAdapter(myPictureAdapter);
					myGridView1.setAdapter(myPictureAdapter1);
					myGridView2.setAdapter(myPictureAdapter2);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 2://删除图片
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
					try {
						JSONObject jo = new JSONObject(resultStr);
						if ("成功".equals(jo.optString("STATUS"))) {

							updateImagesAdpter(delImagePath,null);

							File cacheFile=FileUtility.getCacheFile(delImagePath);
							if(cacheFile.exists())
								cacheFile.delete();


						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				break;
			case 3:// 图片上传
				submitImageCount++;
				result = msg.obj.toString();
				resultStr = "";
				Bundle data=msg.getData();
				String oldFileName=data.getString("oldFileName");
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
						Log.d(TAG, resultStr);
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				try {
					JSONObject jo = new JSONObject(resultStr);
					if ("OK".equals(jo.optString("STATUS"))) {
						String newFileName=jo.getString("文件名");
						FileUtility.fileRename(oldFileName, newFileName);
						ImageItem ds = new ImageItem(jo);
						updateImagesAdpter("loading",ds.getDownAddress());
					}


				} catch (JSONException e) {
					e.printStackTrace();
				}
				Log.d(TAG, "submitCount:" + submitImageCount);
				Log.d(TAG, " picturePaths.size():" + picturePaths.size());
				break;
			}
		}
    };
	private void updateImagesAdpter(String removeUrl,String addUrl)
	{
		if(imagetype.equals("课堂笔记"))
		{
			if(removeUrl!=null)
				picturePaths.remove(removeUrl);
			if(addUrl!=null)
				picturePaths.add(addUrl);
			myPictureAdapter.setPicPaths(picturePaths);
		}
		else if(imagetype.equals("课堂作业"))
		{
			if(removeUrl!=null)
				picturePaths1.remove(removeUrl);
			if(addUrl!=null)
				picturePaths1.add(addUrl);
			myPictureAdapter1.setPicPaths(picturePaths1);
		}
		else if(imagetype.equals("课堂情况"))
		{
			if(removeUrl!=null)
				picturePaths2.remove(removeUrl);
			if(addUrl!=null)
				picturePaths2.add(addUrl);
			myPictureAdapter2.setPicPaths(picturePaths2);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_classroom_summary);

		loadingLayout = (LinearLayout) findViewById(R.id.data_load);
		contentLayout = (ScrollView) findViewById(R.id.content_layout);
		failedLayout = (LinearLayout) findViewById(R.id.empty_error);
		layout_rizhi_shouduan = (WrapLayout) findViewById(R.id.rizhi_shouduan);
		layout_rizhi_pingtai= (WrapLayout) findViewById(R.id.rizhi_pingtai);
		subjectid = ClassDetailActivity.subjectid;
		userType = ClassDetailActivity.userType;
		Log.d(TAG, "subjectid:" + subjectid);
		Log.d(TAG, "---------------onCreate is running----------");
		aq = new AQuery(this);
		try {
			teacherInfoDao = getHelper().getTeacherInfoDao();
			teacherInfo = teacherInfoDao
					.queryForId(Integer.parseInt(subjectid));
		} catch (SQLException e) {
			e.printStackTrace();
		}
		if(teacherInfo==null)
		{
			teacherInfo=ClassDetailActivity.teacherInfo;
		}
		if(!AppUtility.isNotEmpty(teacherInfo.getClassroomSituation()))
			teacherInfo.setClassroomSituation("");
		//myPictureAdapter = new MyPictureAdapter(SummaryClassActivity.this,false, picturePaths, size);


		initView();
		getPingjia();
		initListener();
		registerBroastcastReceiver();

	}

	private void showProgress(boolean progress) {
		if (progress) {

			contentLayout.setVisibility(View.GONE);
			failedLayout.setVisibility(View.GONE);
			loadingLayout.setVisibility(View.VISIBLE);
		} else {
			loadingLayout.setVisibility(View.GONE);
			failedLayout.setVisibility(View.GONE);
			contentLayout.setVisibility(View.VISIBLE);
		}
	}
	private void showFetchFailedView() {
		loadingLayout.setVisibility(View.GONE);
		contentLayout.setVisibility(View.GONE);
		failedLayout.setVisibility(View.VISIBLE);
	}
	private void initView() {
		myGridView = (NonScrollableGridView) findViewById(R.id.grid_picture);
		myGridView1 = (NonScrollableGridView) findViewById(R.id.grid_picture1);
		myGridView2 = (NonScrollableGridView) findViewById(R.id.grid_picture2);
		btnLeft = (Button) findViewById(R.id.btn_left);
		btnLeft.setVisibility(View.VISIBLE);
		btnLeft.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.bg_btn_left_nor, 0, 0, 0);
		aq.id(R.id.tv_right).text("保存");
		aq.id(R.id.tv_right).visibility(View.VISIBLE);
		et1 = (EditText) findViewById(R.id.et_1);
		et2 = (EditText) findViewById(R.id.et_2);
		et3 = (EditText) findViewById(R.id.et_3);
		if (userType.equals("老师")) {
			aq.id(R.id.tv_title).text(ClassDetailActivity.classname);
		} else {
			aq.id(R.id.bottom_tab_roll_call).visibility(View.GONE);
			aq.id(R.id.tv_title).text(teacherInfo.getCourseName());
		}
		//aq.id(R.id.tv_title).textSize(getResources().getDimensionPixelSize(R.dimen.text_size_micro));
		if (userType.equals("老师")) {
			aq.id(R.id.ly_rb_1).visibility(View.GONE);
			aq.id(R.id.ly_rb_2).visibility(View.GONE);

			String rizhi_shouduan_str=PrefUtility.get(Constants.PREF_ALLOW_SCHOOL_RECORD_SUMMARYKEYS_STR, "");
			String rizhi_pingtai_str=PrefUtility.get(Constants.PREF_ALLOW_SCHOOL_RECORDWORK_ATTENDANCEKEYS_STR, "");
			String wanzixistr=PrefUtility.get(Constants.PREF_WANZIXI_JIECI, "");
			boolean bwanzixi=false;
			if(wanzixistr!=null && wanzixistr.length()>0)
			{
				String wanzixi[]=wanzixistr.split(",");
				String jieci[]=teacherInfo.getSection().split("-");
				for(String s: wanzixi){
					if(s.equals(jieci[0])){
						bwanzixi=true;
						break;
					}
				}
			}
			try {

				if (rizhi_shouduan_str != null && rizhi_shouduan_str.length() > 2 && !bwanzixi) {
					findViewById(R.id.tv_6).setVisibility(View.VISIBLE);
					layout_rizhi_shouduan.setVisibility(View.VISIBLE);
					layout_rizhi_shouduan.removeAllViews();
					JSONArray ja = new JSONArray(rizhi_shouduan_str);


					for (int i = 0; i < ja.length(); i++) {
						String item = ja.getString(i);

						if (item.equals("其它") || item.equals("其他")) {
							EditText et = new EditText(this);
							et.setHint(item);
							et.setBackground(getResources().getDrawable(R.drawable.summary_bg));
							et.setTextSize(16);
							et.setEms(10);
							et.setSingleLine(true);
							layout_rizhi_shouduan.addView(et);
							et.setPadding(5, 5, 5, 5);

						} else {
							CheckBox cb = new CheckBox(this);
							cb.setText(item);
							layout_rizhi_shouduan.addView(cb);

							cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
								@Override
								public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
									if (buttonView.isChecked() && buttonView.getText().equals("无")) {
										for (int m = 0; m < layout_rizhi_shouduan.getChildCount(); m++) {
											View v = layout_rizhi_shouduan.getChildAt(m);
											if (v instanceof CheckBox) {
												CheckBox cb = (CheckBox) v;
												if (!cb.getText().equals("无"))
													cb.setChecked(false);
											}
										}
									}
									if (buttonView.isChecked() && !buttonView.getText().equals("无")) {
										for (int m = 0; m < layout_rizhi_shouduan.getChildCount(); m++) {
											View v = layout_rizhi_shouduan.getChildAt(m);
											if (v instanceof CheckBox) {
												CheckBox cb = (CheckBox) v;
												if (cb.getText().equals("无"))
													cb.setChecked(false);
											}
										}
									}
								}
							});
						}
					}
				} else {
					layout_rizhi_shouduan.setVisibility(View.GONE);
					findViewById(R.id.tv_6).setVisibility(View.GONE);
				}
				if(rizhi_pingtai_str!=null && rizhi_pingtai_str.length()>2 && !bwanzixi)
				{
					findViewById(R.id.tv_7).setVisibility(View.VISIBLE);
					layout_rizhi_pingtai.setVisibility(View.VISIBLE);
					layout_rizhi_pingtai.removeAllViews();
					JSONArray ja=new JSONArray(rizhi_pingtai_str);

					for(int i=0;i<ja.length();i++)
					{
						String item=ja.getString(i);
						CheckBox cb=new CheckBox(this);
						cb.setText(item);
						layout_rizhi_pingtai.addView(cb);

						cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
							@Override
							public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
								if(buttonView.isChecked() && buttonView.getText().equals("无"))
								{
									for(int m=0;m<layout_rizhi_pingtai.getChildCount();m++) {
										View v = layout_rizhi_pingtai.getChildAt(m);
										if (v instanceof CheckBox) {
											CheckBox cb = (CheckBox) v;
											if (!cb.getText().equals("无"))
												cb.setChecked(false);
										}
									}
								}
								if(buttonView.isChecked() && !buttonView.getText().equals("无"))
								{
									for(int m=0;m<layout_rizhi_pingtai.getChildCount();m++) {
										View v = layout_rizhi_pingtai.getChildAt(m);
										if (v instanceof CheckBox) {
											CheckBox cb = (CheckBox) v;
											if (cb.getText().equals("无"))
												cb.setChecked(false);
										}
									}
								}
							}
						});
					}
					EditText et=new EditText(this);
					et.setBackground(getResources().getDrawable(R.drawable.summary_bg));
					et.setTextSize(16);
					et.setEms(10);
					et.setHint("资源库课程库名称");
					et.setSingleLine(true);
					et.clearFocus();
					layout_rizhi_pingtai.addView(et);
					et.setPadding(5,5,5,5);

				}
				else
				{
					layout_rizhi_pingtai.setVisibility(View.GONE);
					findViewById(R.id.tv_7).setVisibility(View.GONE);
				}
			}
			catch (Exception e) {
					e.printStackTrace();
			}
		} else {
			myGridView1.setVisibility(View.GONE);
			myGridView2.setVisibility(View.GONE);
			et3.setVisibility(View.GONE);
			aq.id(R.id.tv_5).visibility(View.GONE);

			aq.id(R.id.group_discipline).visibility(View.GONE);
			aq.id(R.id.group_health).visibility(View.GONE);
			ratingBar1 = (RatingBar) findViewById(R.id.rb_1);
			ratingBar2 = (RatingBar) findViewById(R.id.rb_2);
			et1.setHint("请输入课堂笔记内容");
			et2.setHint("请输入对老师的建议");
			layout_rizhi_shouduan.setVisibility(View.GONE);
			findViewById(R.id.tv_6).setVisibility(View.GONE);
			layout_rizhi_pingtai.setVisibility(View.GONE);
			findViewById(R.id.tv_7).setVisibility(View.GONE);
		}

		if (userType.equals("家长"))
		{
			aq.id(R.id.layout_btn_right).visibility(View.INVISIBLE);
			ratingBar1.setIsIndicator(true);
			ratingBar2.setIsIndicator(true);
			et1.setEnabled(false);
			et2.setEnabled(false);
		}


	}
	private void fillData()
	{
		if (teacherInfo != null) {
			RadioGroup group_discipline = (RadioGroup) findViewById(R.id.group_discipline);
			RadioGroup group_health = (RadioGroup) findViewById(R.id.group_health);
			et1.setText(teacherInfo.getCourseContent());
			et2.setText(teacherInfo.getHomework());
			et3.setText(teacherInfo.getClassroomSituation());
			if (teacherInfo.getClassroomDiscipline().equals("优")) {
				group_discipline.check(R.id.group1_bn1);
			} else if (teacherInfo.getClassroomDiscipline().equals("良")) {
				group_discipline.check(R.id.group1_bn2);
			} else if (teacherInfo.getClassroomDiscipline().equals("中")) {
				group_discipline.check(R.id.group1_bn3);
			} else if (teacherInfo.getClassroomDiscipline().equals("差")) {
				group_discipline.check(R.id.group1_bn4);
			} else {
				group_discipline.check(R.id.group1_bn2);
			}
			if (teacherInfo.getClassroomHealth().equals("优")) {
				group_health.check(R.id.group2_bn1);
			} else if (teacherInfo.getClassroomHealth().equals("良")) {
				group_health.check(R.id.group2_bn2);
			} else if (teacherInfo.getClassroomHealth().equals("中")) {
				group_health.check(R.id.group2_bn3);
			} else if (teacherInfo.getClassroomHealth().equals("差")) {
				group_health.check(R.id.group2_bn4);
			} else {
				group_health.check(R.id.group2_bn2);
			}

			ArrayList<String> shouduancheckList = new ArrayList<String>();
			if (teacherInfo != null && teacherInfo.getCompositeScoreText() != null) {
				String shouduancheck[] = teacherInfo.getCompositeScoreText().split(",");
				for (String item : shouduancheck) {
					if (item.length() > 0)
						shouduancheckList.add(item);
				}
			}
			if(shouduancheckList.size()>0) {
				for (int m = 0; m < layout_rizhi_shouduan.getChildCount(); m++) {
					View v = layout_rizhi_shouduan.getChildAt(m);
					if (v instanceof CheckBox) {
						CheckBox cb = (CheckBox) v;
						if (shouduancheckList.contains(cb.getText())) {
							cb.setChecked(true);
							shouduancheckList.remove(cb.getText());
						}
						else
							cb.setChecked(false);
					}
				}
				for (int m = 0; m < layout_rizhi_shouduan.getChildCount(); m++) {
					View v = layout_rizhi_shouduan.getChildAt(m);
					if (v instanceof EditText) {
						EditText cb = (EditText) v;
						cb.setText("");
						if(shouduancheckList.size()>0)
							cb.setText(shouduancheckList.get(0));
					}
				}
			}
			ArrayList<String> pingtaicheckList=new ArrayList<String>();
			if (teacherInfo != null && teacherInfo.getCompositeScoreValue()!=null) {
				String[] pingtaicheck=teacherInfo.getCompositeScoreValue().split(",");
				for(String item : pingtaicheck)
				{
					if(item.length()>0)
						pingtaicheckList.add(item);
				}
			}
			for(int m=0;m<layout_rizhi_pingtai.getChildCount();m++) {
				View v = layout_rizhi_pingtai.getChildAt(m);
				if (v instanceof CheckBox) {
					CheckBox cb = (CheckBox) v;
					if (pingtaicheckList.contains(cb.getText())) {
						cb.setChecked(true);
					}
					else
						cb.setChecked(false);
				}
				else if(v instanceof EditText)
				{
					EditText cb = (EditText) v;
					cb.setText(teacherInfo.getBeginTime());
				}
			}

			if(AppUtility.isNotEmpty(teacherInfo.getShouldTime()) && AppUtility.isNotEmpty(teacherInfo.getLatestTime()))
			{
				String begintimeStr=teacherInfo.getShouldTime();
				String endtimeStr=teacherInfo.getLatestTime();
				if (begintimeStr.length()==10)
					begintimeStr=begintimeStr+" 00:00:00";
				if (endtimeStr.length()==10)
					endtimeStr=endtimeStr+" 23:59:59";

				Date dt_begin=DateHelper.getStringDate(begintimeStr,"");
				Date dt_end=DateHelper.getStringDate(endtimeStr,"");
				Date dt_now=new Date();
				if(dt_now.getTime()>=dt_begin.getTime() && dt_now.getTime()<=dt_end.getTime())
				{

				}
				else
				{
					AppUtility.showToastMsg(this, "请在"+teacherInfo.getShouldTime()+"至"+teacherInfo.getLatestTime()+"内填写",1);
					aq.id(R.id.layout_btn_right).visibility(View.GONE);
					et1.setEnabled(false);
					et2.setEnabled(false);
					et3.setEnabled(false);
				}

			}

		}
	}
	private void initListener() {
		aq.id(R.id.layout_btn_left).clicked(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 通知ClassDetailActivity finish();
				Intent intent = new Intent("finish_classdetailactivity");
				sendBroadcast(intent);
				finish();
			}
		});
		// 保存数据
		aq.id(R.id.layout_btn_right).clicked(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (userType.equals("老师")) {
					try {
						showDialog();
						String kaoqinJsonStr = getChangekaoqininfo();
						Log.d(TAG, "------kaoqinJsonStr:" + kaoqinJsonStr);
						// base64加密处理
						if (!"".equals(kaoqinJsonStr) && kaoqinJsonStr != null) {
							String base64Str = Base64.encode(kaoqinJsonStr
									.getBytes());
							Log.d(TAG, "------base64Str:" + base64Str);
							SubmitChangeinfo(base64Str);
						} else {
							closeDialog();
						}
					} catch (JSONException e) {
						e.printStackTrace();
						closeDialog();
					}
				} else {
					showDialog();
					saveSudentZongJie();
				}
			}
		});
	}

	private void registerBroastcastReceiver() {
		IntentFilter mFilter = new IntentFilter(Constants.GET_PICTURE);
		mFilter.addAction(Constants.DEL_OR_LOOK_PICTURE);
		registerReceiver(mBroadcastReceiver, mFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String fromTag = intent.getStringExtra("TAG");
			Log.d(TAG, "--------action:" + action);
			Log.d(TAG, "--------fromTag:" + fromTag);
			if (action.equals(Constants.GET_PICTURE) && fromTag.equals(TAG)) {
				imagetype=intent.getStringExtra("imagetype");
				showGetPictureDiaLog();
			} else if (action.equals(Constants.DEL_OR_LOOK_PICTURE)
					&& fromTag.equals(TAG)) {
				// 查看详图或删除图片
				imagetype=intent.getStringExtra("imagetype");
				delImagePath = intent.getStringExtra("imagePath");
				showDelOrShowPictureDiaLog(delImagePath);
			}
		}
	};

	@Override
	protected void onDestroy() {
		unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

    /**
	 * 功能描述:获取总结信息
	 * 
	 * @author shengguo 2014-5-8 下午4:47:28
	 * 
	 */
	private void getPingjia() {
		showProgress(true);
		
		Log.d(TAG, "--------" + String.valueOf(new Date().getTime()));
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "----------datatime:" + datatime);
		Log.d(TAG, "----------checkCode:" + checkCode + "++");
		JSONObject jo = new JSONObject();
		try {
			jo.put("ACTION", "GetInfo");
			jo.put("老师用户名", teacherInfo.getUsername());
			jo.put("课程名称", teacherInfo.getCourseName());
			jo.put("老师上课记录编号", teacherInfo.getId());
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		Log.d(TAG, "------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.getPingjiaByStudent(params, new RequestListener() {

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
	
	private DatabaseHelper getHelper() {
		if (database == null) {
			database = OpenHelperManager.getHelper(this, DatabaseHelper.class);
		}
		return database;
	}


	public String getChangekaoqininfo() throws JSONException {
		RadioGroup group_discipline = (RadioGroup) findViewById(R.id.group_discipline);
		RadioGroup group_health = (RadioGroup) findViewById(R.id.group_health);

		RadioButton rdobtn_discipline = (RadioButton) findViewById(group_discipline
				.getCheckedRadioButtonId());
		RadioButton rdobtn_health = (RadioButton) findViewById(group_health
				.getCheckedRadioButtonId());
		teacherInfo.setCourseContent(et1.getText().toString());
		if(ClassDetailActivity.teacherInfo!=null)
			ClassDetailActivity.teacherInfo.setCourseContent(teacherInfo.getCourseContent());
		teacherInfo.setHomework(et2.getText().toString());
		teacherInfo.setClassroomSituation(et3.getText().toString());
		teacherInfo.setClassroomDiscipline(rdobtn_discipline.getText()
				.toString());
		teacherInfo.setClassroomHealth(rdobtn_health.getText().toString());
		String shouduanstr="";
		for(int i=0;i<layout_rizhi_shouduan.getChildCount();i++)
		{
			View v=layout_rizhi_shouduan.getChildAt(i);
			if(v instanceof CheckBox)
			{
				CheckBox cb=(CheckBox)v;
				if (cb.isChecked())
				{
					if(shouduanstr.length()>0)
						shouduanstr+=",";
					shouduanstr+=cb.getText().toString();
				}

			}
			else if(v instanceof EditText)
			{
				EditText et=(EditText)v;
				if(et.getText().length()>0) {
					if(shouduanstr.length()>0)
						shouduanstr+=",";
					shouduanstr += et.getText().toString().trim();
				}
			}
		}

		String pingtaistr="";
		String kechengname="";
		for(int i=0;i<layout_rizhi_pingtai.getChildCount();i++)
		{
			View v=layout_rizhi_pingtai.getChildAt(i);
			if(v instanceof CheckBox)
			{
				CheckBox cb=(CheckBox)v;
				if (cb.isChecked())
				{
					if(pingtaistr.length()>0)
						pingtaistr+=",";
					pingtaistr+=cb.getText().toString();
				}
			}
			else if(v instanceof EditText)
			{
				EditText et=(EditText)v;
				if(et.getText().length()>0)
					kechengname=et.getText().toString().trim();
			}
		}
		if (userType.equals("老师")) {
			if(et1.getText().toString().trim().length()==0)
			{
				AppUtility.showToastMsg(this,"授课内容不能为空");
				et1.requestFocus();
				return null;
			}
			if(et2.getText().toString().trim().length()==0)
			{
				AppUtility.showToastMsg(this,"作业布置不能为空");
				et2.requestFocus();
				return null;
			}
			if(et3.getText().toString().trim().length()==0)
			{
				AppUtility.showToastMsg(this,"课堂情况摘要不能为空");
				et3.requestFocus();
				return null;
			}
			if(layout_rizhi_shouduan.getVisibility()==View.VISIBLE &&layout_rizhi_shouduan.getChildCount()>0 && shouduanstr.length()==0)
			{
				AppUtility.showToastMsg(this,"信息化手段请至少选择或填写一项");
				contentLayout.fullScroll(ScrollView.FOCUS_DOWN);
				return null;
			}
			if (layout_rizhi_pingtai.getVisibility() == View.VISIBLE && layout_rizhi_pingtai.getChildCount() > 0 && pingtaistr.length() == 0) {
				AppUtility.showToastMsg(this, "教学平台请至少选择或填写一项");
				contentLayout.fullScroll(ScrollView.FOCUS_DOWN);
				return null;
			}
		}
		teacherInfo.setCompositeScoreText(shouduanstr);
		teacherInfo.setCompositeScoreValue(pingtaistr);
		teacherInfo.setBeginTime(kechengname);
		JSONArray joarr = new JSONArray();
		JSONObject jo = new JSONObject();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
		jo.put("用户较验码", checkCode);
		jo.put("编号", teacherInfo.getId());
		jo.put("课堂纪律", teacherInfo.getClassroomDiscipline());
		jo.put("教室卫生", teacherInfo.getClassroomHealth());
		jo.put("授课内容", et1.getText().toString());
		jo.put("作业布置", et2.getText().toString());
		jo.put("课堂情况简要", et3.getText().toString());
		jo.put("备注1", shouduanstr);
		jo.put("备注2", pingtaistr);
		jo.put("资源库课程库名称", kechengname);
		Log.d(TAG, "----------------------json:" + jo.toString());
		joarr.put(jo);
		return joarr.toString();
	}


	private void fileUploadWay(File file) {
		if(!file.exists()) return;
		if(AppUtility.formetFileSize(file.length()) > 5242880*2){
			AppUtility.showToastMsg(this, "对不起，您上传的文件太大了，请选择小于10M的文件！");
		}else{
			
			ImageUtility.rotatingImageIfNeed(file.getAbsolutePath());
			DownloadSubject downloadSubject = new DownloadSubject();
			String filebase64Str = FileUtility.fileupload(file);
			downloadSubject.setFilecontent(filebase64Str);
			String filename = file.getName();
			downloadSubject.setFileName(filename);
			downloadSubject.setLocalfile(file.getAbsolutePath());
			downloadSubject.setFilesize(file.length());
			uploadFile(downloadSubject);
			
		}
	}


	public void uploadFile(DownloadSubject downloadSubject) {
		final CampusParameters params = new CampusParameters();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
		params.add("用户较验码", checkCode);
		params.add("课程名称", teacherInfo.getCourseName());
		params.add("老师上课记录编号", teacherInfo.getId());
		params.add("图片类别", imagetype);
		params.add("文件名称", downloadSubject.getFileName());
		params.add("文件内容", downloadSubject.getFilecontent());
		updateImagesAdpter("","loading");
		CampusAPI.uploadFiles(params, new RequestListener() {

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "------------------response" + response);
				
				Message msg = new Message();
				msg.what = 3;
				msg.obj = response;
				Bundle data=new Bundle();
				data.putString("oldFileName", params.getValue("文件名称"));
				msg.setData(data);
				mHandler.sendMessage(msg);

			}

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Log.d(TAG, "图片上传失败");
				picturePaths.remove("loading");
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}
		});
	}

	/**
	 * 功能描述:提交服务器
	 * 
	 * @author yanzy 2013-12-4 上午10:10:42
	 * 
	 * @param base64Str
	 */
	public void SubmitChangeinfo(String base64Str) {
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.saveTeacherZongJie(params, new RequestListener() {
			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "---------------response:" + response + "++");
				Message msg = new Message();
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}


	public void saveSudentZongJie() {
		JSONObject jo = new JSONObject();
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
		try {
			jo.put("用户较验码", checkCode);
			jo.put("老师上课记录编号", teacherInfo.getId());
			jo.put("老师用户名", teacherInfo.getUsername());
			jo.put("课程名称", teacherInfo.getCourseName());
			jo.put("DATETIME", datatime);
			jo.put("ACTION", "SetStatus");
			jo.put("老师评价", (int)ratingBar1.getRating());
			jo.put("课程评价", (int)ratingBar2.getRating());
			jo.put("课堂笔记", et1.getText().toString());
			jo.put("我的建议", et2.getText().toString());
		} catch (JSONException e1) {
			e1.printStackTrace();
			closeDialog();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.saveStudentZongJie(params, new RequestListener() {
			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "---------------response:" + response + "++");
				Message msg = new Message();
				msg.what = 0;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}

	/**
	 * 功能描述:显示提示框
	 * 
	 * @author yanzy 2013-12-21 上午10:54:41
	 * 
	 */
	public void showDialog() {
		mLoadingDialog = DialogUtility.createLoadingDialog(getParent(),
				"数据保存中...");
		mLoadingDialog.show();
	}

	/**
	 * 功能描述:操作失败，提示
	 * 
	 * @author yanzy 2013-12-21 上午10:37:17
	 * 
	 */
	public void closeDialog() {
		if (mLoadingDialog != null) {
			mLoadingDialog.dismiss();
		}
	}

	/**
	 * 功能描述:学生总结信息
	 * 
	 * @author shengguo 2014-4-30 下午4:19:26
	 * 
	 */
	private void initStudentDate() {
		aq.id(R.id.tv_1).text("老师评价");
		aq.id(R.id.tv_2).text("课程评价");
		aq.id(R.id.tv_3).text("课堂笔记");
		aq.id(R.id.tv_4).text("我的建议");
		et1.setText(studentSummary.getClassNotes());
		et2.setText(studentSummary.getMySuggestion());
		ratingBar1.setRating(Float.parseFloat(studentSummary
				.getTeacherEvaluate()));
		ratingBar2.setRating(Float.parseFloat(studentSummary
				.getCurriculumEvaluate()));
		List<ImageItem> images = studentSummary.getClassNoteImages();
		if (images != null) {
			for (int i = 0; i < images.size(); i++) {
				String imagePath = images.get(i).getDownAddress();
				if (!picturePaths.contains(imagePath)) {
					picturePaths.add(images.get(i).getDownAddress());
					
				}
			}
		}
		
		
	}

	/**
	 * 功能描述:老师的课堂笔记
	 *
	 * @author shengguo  2014-5-28 上午10:30:27
	 *
	 */
	private void initTeacherDate() {
		List<ImageItem> images = studentSummary.getClassNoteImages();
		if (images != null) {
			for (int i = 0; i < images.size(); i++) {
				String imagePath = images.get(i).getDownAddress();
				if (!picturePaths.contains(imagePath)) {
					picturePaths.add(images.get(i).getDownAddress());
				}
			}
		}
		images = studentSummary.getClassAssignImages();
		if (images != null) {
			for (int i = 0; i < images.size(); i++) {
				String imagePath = images.get(i).getDownAddress();
				if (!picturePaths1.contains(imagePath)) {
					picturePaths1.add(images.get(i).getDownAddress());
					
				}
			}
		}
		images = studentSummary.getClassSiduationImages();
		if (images != null) {
			for (int i = 0; i < images.size(); i++) {
				String imagePath = images.get(i).getDownAddress();
				if (!picturePaths2.contains(imagePath)) {
					picturePaths2.add(images.get(i).getDownAddress());
					
				}
			}
		}

		teacherInfo.setClassroomDiscipline(studentSummary.getClassroomDiscipline());
		teacherInfo.setClassroomHealth(studentSummary.getClassroomHealth());
		teacherInfo.setCourseContent(studentSummary.getCourseContent());
		teacherInfo.setHomework(studentSummary.getHomework());
		teacherInfo.setClassroomSituation(studentSummary.getClassroomSituation());
		teacherInfo.setCompositeScoreText(studentSummary.getShouduanStr());
		teacherInfo.setCompositeScoreValue(studentSummary.getPingtaiStr());
		teacherInfo.setBeginTime(studentSummary.getZiyuankuStr());
		try {
			teacherInfoDao.update(teacherInfo);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		fillData();
		if(studentSummary.getTeacherkaoqin().length()>0 && !studentSummary.getTeacherkaoqin().equals("正常"))
		{
			AppUtility.showToastMsg(this, studentSummary.getTeacherkaoqin(),1);
			aq.id(R.id.layout_btn_right).visibility(View.GONE);
			et1.setHint(studentSummary.getTeacherkaoqin());
			et1.setEnabled(false);
			et2.setEnabled(false);
			et3.setEnabled(false);
		}
	}
	/**
	 * 功能描述:清除缓存
	 * 
	 * @author shengguo 2014-5-5 下午3:45:04
	 * 
	 */
	private void showGetPictureDiaLog() {
		View view = getLayoutInflater()
				.inflate(R.layout.view_get_picture, null);
		Button cancel = (Button) view.findViewById(R.id.cancel);
		TextView byCamera = (TextView) view.findViewById(R.id.tv_by_camera);
		TextView byLocation = (TextView) view.findViewById(R.id.tv_by_location);

		final AlertDialog ad = new AlertDialog.Builder(this).setView(view)
				.create();

		Window window = ad.getWindow();
		window.setGravity(Gravity.BOTTOM);// 在底部弹出
		window.setWindowAnimations(R.style.CustomDialog);
		ad.show();
		((ClassDetailActivity)getParent()).callBack=callBack;
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ad.dismiss();
			}
		});
		byCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT >= 23) 
				{
					if(AppUtility.checkPermission(getParent(), 6,Manifest.permission.CAMERA))
						getPictureByCamera();
				}
				else
					getPictureByCamera();
				
				ad.dismiss();
			}
		});
		byLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Build.VERSION.SDK_INT >= 23) 
				{
					if(AppUtility.checkPermission(getParent(),7,Manifest.permission.READ_EXTERNAL_STORAGE))
						getPictureFromLocation();
				}
				else
					getPictureFromLocation();
				ad.dismiss();
			}
		});
	}

	/**
	 * 功能描述:删除或查看图片
	 * 
	 * @author shengguo 2014-5-8 下午6:32:49
	 * 
	 */
	private void showDelOrShowPictureDiaLog(final String imageName) {
		View view = getLayoutInflater().inflate(
				R.layout.view_show_or_del_picture, null);
		Button cancel = (Button) view.findViewById(R.id.cancel);
		TextView delPicture = (TextView) view.findViewById(R.id.tv_delete);
		TextView showPicture = (TextView) view.findViewById(R.id.tv_show);
		final AlertDialog ad = new AlertDialog.Builder(this).setView(view)
				.create();
		Window window = ad.getWindow();
		window.setGravity(Gravity.BOTTOM);// 在底部弹出
		window.setWindowAnimations(R.style.CustomDialog);
		ad.show();
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				ad.dismiss();
			}
		});
		delPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String fileName=imageName;
				if(imageName.indexOf("?")>=0)
					fileName=imageName.substring(0, imageName.indexOf("?"));
				fileName=fileName.substring(fileName.lastIndexOf("/")+1, fileName.length());
				SubmitDeleteinfo(fileName);
				ad.dismiss();
			}
		});
		showPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(SummaryClassActivity.this,
						ImagesActivity.class);
				ArrayList<String> thePaths;
				if(imagetype.equals("课堂笔记"))
					thePaths=picturePaths;
				else if(imagetype.equals("课堂作业"))
					thePaths=picturePaths1;
				else
					thePaths=picturePaths2;
				
				thePaths.remove("");
				intent.putStringArrayListExtra("pics",
                        thePaths);
				
				for (int i = 0; i < thePaths.size(); i++) {
					if(thePaths.get(i).equals(imageName)){
						intent.putExtra("position", i);
					}
				}
				startActivity(intent);
				ad.dismiss();
			}
		});
	}

	/**
	 * 调用系统相机拍照获取图片
	 * 
	 * @param
	 */
	private synchronized void getPictureByCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用android自带的照相机
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			AppUtility.showToastMsg(this, "没有安装SD卡，无法使用相机功能");
			return;
		}
		picturePath = FileUtility.getRandomSDFileName("jpg");
		
		File mCurrentPhotoFile = new File(picturePath);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", mCurrentPhotoFile)); //Uri.fromFile(tempFile)
		else {
			Uri uri = Uri.fromFile(mCurrentPhotoFile);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
		}
		startActivityForResult(intent, REQUEST_CODE_TAKE_CAMERA);
	}

	/**
	 * 功能描述:从本地获取图片
	 * 
	 * @author shengguo 2014-5-8 上午10:58:45
	 * 
	 
	private void getPictureFromLocation() {
		picturePaths.remove("");
		Intent intent = new Intent(SummaryClassActivity.this,AlbumActivity.class);
		intent.putStringArrayListExtra("picturePaths",(ArrayList<String>) picturePaths);
		intent.putExtra("size", 5);
		startActivityForResult(intent,
				SchoolDetailActivity.REQUEST_CODE_TAKE_PICTURE);
	}
	 */
	public void getPictureFromLocation() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)) {// 判断是否有SD卡
			/*
			Intent intent = new Intent();
			intent.setType("image/*");
			intent.setAction(Intent.ACTION_GET_CONTENT);
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
			*/
			Intent intent; 
			intent = new Intent(Intent.ACTION_PICK, 
			                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI); 
			startActivityForResult(intent, REQUEST_CODE_TAKE_PICTURE);
			
		} else {
			AppUtility.showToastMsg(this, "SD卡不可用");
		}
	}
	
	/**
	 * 功能描述:删除图片
	 * 
	 * @author shengguo 2014-5-9 下午12:05:03
	 * 
	 * @param fileName
	 */
	public void SubmitDeleteinfo(String fileName) {
		JSONObject jo = new JSONObject();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "--------------filename----------" + fileName);
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", String.valueOf(new Date().getTime()));
			jo.put("课件名称", fileName);
			jo.put("图片类别",imagetype);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString().getBytes());
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		CampusAPI.sendDownloadDeleteData(params, new RequestListener() {

			@Override
			public void onIOException(IOException e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onError(CampusException e) {
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);
			}

			@Override
			public void onComplete(String response) {
				Message msg = new Message();
				msg.what = 2;
				msg.obj = response;
				mHandler.sendMessage(msg);
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUEST_CODE_TAKE_CAMERA: // 拍照返回
			if (resultCode == RESULT_OK) {
				fileUploadWay(new File(picturePath));
			}
			break;
		case REQUEST_CODE_TAKE_PICTURE:
			if (data != null) {
				
				//picturePath = data.getStringExtra("filepath");
				//String myImageUrl = data.getDataString();
				//Uri uri = Uri.parse(myImageUrl);
				Uri uri = data.getData();
				String[] pojo  = { MediaStore.Images.Media.DATA };
				CursorLoader cursorLoader = new CursorLoader(this, uri, pojo, null,null, null); 
				Cursor cursor = cursorLoader.loadInBackground();
				if(cursor!=null)
				{
					cursor.moveToFirst(); 
					picturePath = cursor.getString(cursor.getColumnIndex(pojo[0]));
				}
				else
				{
					if(uri.toString().startsWith("file://"))
					{
						picturePath=uri.toString().replace("file://", "");
					}
					else
					{
						AppUtility.showErrorToast(this, "获取相册图片失败");
						return;
					}
				}
				
				String tempPath =FileUtility.getRandomSDFileName("jpg");
				if(FileUtility.copyFile(picturePath,tempPath))
					fileUploadWay(new File(tempPath));
				else
					AppUtility.showErrorToast(this, "向SD卡复制文件出错");
			}
			break;
		default:
			break;
		}
	}
	@Override
    public void onSaveInstanceState(Bundle savedInstanceState){
		super.onSaveInstanceState(savedInstanceState);
		savedInstanceState.putSerializable("picturePaths",picturePaths);
		savedInstanceState.putSerializable("picturePaths1",picturePaths1);
		savedInstanceState.putSerializable("picturePaths2",picturePaths2);
		savedInstanceState.putString("picturePath", picturePath);
		savedInstanceState.putString("subjectid", subjectid);
		savedInstanceState.putString("userType",  userType);
		
	}
	@Override
    public void onRestoreInstanceState(Bundle savedInstanceState){
        super.onRestoreInstanceState(savedInstanceState);
        picturePaths = (ArrayList<String>) savedInstanceState.getSerializable("picturePaths");
        picturePaths1 = (ArrayList<String>) savedInstanceState.getSerializable("picturePaths1");
        picturePaths2 = (ArrayList<String>) savedInstanceState.getSerializable("picturePaths2");
        picturePath=savedInstanceState.getString("picturePath");
        
        subjectid=savedInstanceState.getString("subjectid");
        userType=savedInstanceState.getString("userType");
    }
	
	public CallBackInterface callBack=new CallBackInterface()
	{

		@Override
		public void getLocation1(int rqcode) {
			// TODO Auto-generated method stub
		
		}
		@Override
		public void getPictureByCamera1(int rqcode) {
			getPictureByCamera();
		}

		@Override
		public void getPictureFromLocation1() {
			// TODO Auto-generated method stub
			getPictureFromLocation();
		}
		@Override
		public void getFujian1() {
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
		
	};
}
