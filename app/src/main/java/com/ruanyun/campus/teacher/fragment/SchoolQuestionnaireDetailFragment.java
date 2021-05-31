package com.ruanyun.campus.teacher.fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
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
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.ruanyun.campus.teacher.BuildConfig;
import com.ruanyun.campus.teacher.CampusApplication;
import com.ruanyun.campus.teacher.R;
import com.ruanyun.campus.teacher.activity.ImagesActivity;
import com.ruanyun.campus.teacher.activity.SchoolDetailActivity;
import com.ruanyun.campus.teacher.activity.StudentSelectActivity;
import com.ruanyun.campus.teacher.adapter.ListOfBillAdapter;
import com.ruanyun.campus.teacher.adapter.ListViewImageAdapter;
import com.ruanyun.campus.teacher.adapter.MyPictureAdapter;
import com.ruanyun.campus.teacher.api.CampusAPI;
import com.ruanyun.campus.teacher.api.CampusException;
import com.ruanyun.campus.teacher.api.CampusParameters;
import com.ruanyun.campus.teacher.api.RequestListener;
import com.ruanyun.campus.teacher.base.Constants;
import com.ruanyun.campus.teacher.entity.DownloadSubject;
import com.ruanyun.campus.teacher.entity.ImageItem;
import com.ruanyun.campus.teacher.entity.QuestionnaireList;
import com.ruanyun.campus.teacher.entity.QuestionnaireList.Question;
import com.ruanyun.campus.teacher.entity.User;
import com.ruanyun.campus.teacher.lib.DateTimePickDialogUtil;
import com.ruanyun.campus.teacher.service.Alarmreceiver;
import com.ruanyun.campus.teacher.util.AppUtility;
import com.ruanyun.campus.teacher.util.AppUtility.CallBackInterface;
import com.ruanyun.campus.teacher.util.Base64;
import com.ruanyun.campus.teacher.util.DateHelper;
import com.ruanyun.campus.teacher.util.DialogUtility;
import com.ruanyun.campus.teacher.util.FileUtility;
import com.ruanyun.campus.teacher.util.ImageUtility;
import com.ruanyun.campus.teacher.util.MaxLengthWatcher;
import com.ruanyun.campus.teacher.util.PrefUtility;
import com.ruanyun.campus.teacher.widget.NonScrollableGridView;
import com.ruanyun.campus.teacher.widget.NonScrollableListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SuppressLint({"ValidFragment","LongLogTag"})
public class SchoolQuestionnaireDetailFragment extends Fragment implements View.OnTouchListener{
	private final String TAG = "SchoolQuestionnaireDetailFragment";
	private ListView myListview;
	private Button btnLeft;
	private TextView tvTitle, tvRight,emptytext;
	private LinearLayout lyLeft, lyRight,loadingLayout,contentLayout,failedLayout,emptyLayout;
	private QuestionnaireList questionnaireList;
	private String title,status, interfaceName,picturePath,delImagePath,autoClose;
	private LayoutInflater inflater;
	private QuestionAdapter adapter;
	private boolean isEnable = true;
	private Dialog dialog, getPictureDiaLog;
	private MyPictureAdapter myPictureAdapter;
	//List<String> picturePaths = new ArrayList<String>();
	private ArrayList<Question> questions = new ArrayList<Question>();
	//private List<ImageItem> images = new ArrayList<ImageItem>();
	private static final int REQUEST_CODE_TAKE_PICTURE = 2;// //设置图片操作的标志
	private static final int REQUEST_CODE_TAKE_CAMERA = 1;// //设置拍照操作的标志
	private static final int REQUEST_CODE_TAKE_DOCUMENT = 3;// //设置图片操作的标志
	private static final int REQUEST_CODE_SelectMuti = 4;// //设置图片操作的标志
	//private int size = 5;//已提交图片数量;size:图片最大数量
	private int curIndex;
	private ProgressDialog progressDlg;
	private EditText lastFocusEt;

	public static JSONObject multiListData=new JSONObject();
	@SuppressLint("HandlerLeak")
	private Handler mHandler = new Handler() {
		@SuppressLint("NewApi")
		public void handleMessage(Message msg) {
			String result = "";
			String resultStr = "";
			switch (msg.what) {
			case -1:
				if(dialog != null){
					dialog.dismiss();
				}
				if(progressDlg!=null)
					progressDlg.dismiss();
				AppUtility.showErrorToast(getActivity(), msg.obj.toString());
				
				break;
			case 0://获取数据
				showProgress(false);
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
						if (AppUtility.isNotEmpty(res)) {
							//AppUtility.showToastMsg(getActivity(), res,1);
							emptytext.setText(res);
						} else {
							questionnaireList = new QuestionnaireList(jo);
							
							tvTitle.setText(questionnaireList.getTitle());
							questions = questionnaireList.getQuestions();
							status=questionnaireList.getStatus();
							autoClose=questionnaireList.getAutoClose();
							if(questionnaireList.getSavebtn().length()>0)
								tvRight.setText(questionnaireList.getSavebtn());
							if (status.equals("进行中")) {
								tvRight.setVisibility(View.VISIBLE);
								isEnable = true;

							} else {
								lyRight.setVisibility(View.INVISIBLE);
								isEnable = false;
							}
							for(int i=0;i<questions.size();i++)
							{
								Question item=questions.get(i);
								if(item.getLinkUpdate()>0)
								{
									Question guanlianItem=questions.get(item.getLinkUpdate());
									JSONObject obj=guanlianItem.getFilterObj();
									if(item.getUsersAnswer().length()==0 && item.getOptions().length>0)
										item.setUsersAnswer(item.getOptions()[0]);
									JSONArray ja = null;
									if(obj!=null)
										ja=obj.optJSONArray(item.getUsersAnswer());
									if(ja!=null)
									{
										String [] options = new String[ja.length()];
										for (int j = 0; j < ja.length(); j++) {
											options[j] = ja.optString(j);
										}
										guanlianItem.setOptions(options);
										questions.set(item.getLinkUpdate(), guanlianItem);
									}
									
								}
							}
							
							adapter.notifyDataSetChanged();
							if(questionnaireList.getNeedLocation().equals("是"))
							{
								if (Build.VERSION.SDK_INT >= 23)
								{
									if(AppUtility.checkPermission(getActivity(), 5,Manifest.permission.ACCESS_FINE_LOCATION))
										getLocation();
								}
								else
									getLocation();
							}
						}
					} catch (JSONException e) {
						showFetchFailedView();
						e.printStackTrace();
					}
				}else{
					showFetchFailedView();
				}
				break;
			case 1://保存成功
				
				result = msg.obj.toString();
				resultStr = "";
				if (AppUtility.isNotEmpty(result)) {
					try {
						resultStr = new String(Base64.decode(result
								.getBytes("GBK")));
						
						JSONObject jo = new JSONObject(resultStr);
						String res = jo.optString("结果");
						if(!AppUtility.isNotEmpty(res))
							res=jo.optString("状态");
						if(res.equals("成功"))
						{
							AppUtility.showToastMsg(getActivity(), "保存成功！");
							if(jo.optString("自动关闭").length()>0)
								autoClose=jo.optString("自动关闭");
							if(autoClose!=null && autoClose.equals("是"))
							{
								
								Intent aintent = new Intent();
								getActivity().setResult(1,aintent); 
								getActivity().finish();
							}
						}
						else
							AppUtility.showErrorToast(getActivity(), "失败:"+res);
							
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
						AppUtility.showErrorToast(getActivity(), "失败:"+e.getMessage());
						
					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						AppUtility.showErrorToast(getActivity(), "失败:"+e.getMessage());
						
					}
					
				}

				break;
			case 2://删除图片
				result = msg.obj.toString();
				int type=msg.getData().getInt("type");
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
						JSONObject	jo = new JSONObject(resultStr);
						if("成功".equals(jo.optString("STATUS"))){
							if(type==1)
							{
								List<ImageItem> images=questions.get(curIndex).getImages();
								for (int i = 0; i < images.size(); i++) {
									if(images.get(i).getDownAddress().equals(delImagePath)){
										images.remove(i);
									}
								}
								questions.get(curIndex).setImages(images);
								File cacheFile=FileUtility.getCacheFile(delImagePath);
								if(cacheFile.exists())
									cacheFile.delete();
								myPictureAdapter.setPicPathsByImages(images);
							}
							else if(type==2)
							{
								JSONArray fujianArray=questions.get(curIndex).getFujianArray();
								for (int i = 0; i < fujianArray.length(); i++) {
									JSONObject item=(JSONObject) fujianArray.get(i);
									if(item.optString("newname").equals(FileUtility.getFileRealName(jo.optString("path")))){
										fujianArray.remove(i);
									}
								}
								questions.get(curIndex).setFujianArray(fujianArray);
								View view= myListview.getChildAt(curIndex-myListview.getFirstVisiblePosition());
								NonScrollableListView listview=(NonScrollableListView) view.findViewById(R.id.lv_choose);
								SimpleAdapter fujianAdapter=setupFujianAdpter(questions.get(curIndex));
								listview.setAdapter(fujianAdapter);
							}
							//myPictureAdapter.notifyDataSetChanged();
						}else{
							AppUtility.showToastMsg(getActivity(), jo.optString("STATUS"));
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				break;
			case 3://图片上传
				result = msg.obj.toString();
				resultStr = "";
				Bundle data=msg.getData();
				String oldFileName=data.getString("oldFileName");
				type=data.getInt("type");
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
					if("OK".equals(jo.optString("STATUS"))){
						
						String newFileName=jo.getString("文件名");
						if(type==1)
						{
							List<ImageItem> images=questions.get(curIndex).getImages();
							FileUtility.fileRename(oldFileName, newFileName);
							ImageItem ds = new ImageItem(jo);
							images.add(ds);
							questions.get(curIndex).setImages(images);
							myPictureAdapter.setPicPathsByImages(images);
						}
						else if(type==2)
						{
							if(progressDlg!=null) progressDlg.dismiss();
							Question question=questions.get(curIndex);
							if(question!=null)
							{
								JSONArray fujianArray=question.getFujianArray();
								JSONObject newItem=new JSONObject();
								newItem.put("name", oldFileName);
								newItem.put("newname", newFileName);
								newItem.put("url", jo.optString("文件地址"));
								fujianArray.put(newItem);
								question.setFujianArray(fujianArray);
								questions.set(curIndex, question);
								View view= myListview.getChildAt(curIndex-myListview.getFirstVisiblePosition());
								NonScrollableListView listview=(NonScrollableListView) view.findViewById(R.id.lv_choose);
								SimpleAdapter fujianAdapter=setupFujianAdpter(questions.get(curIndex));
								listview.setAdapter(fujianAdapter);
							}
							
							
						}
						//myPictureAdapter.notifyDataSetChanged();
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
				break;
			case 4://回调函数
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
						String res = jo.optString("结果");
						if (res.equals("失败")) {
							AppUtility.showToastMsg(getActivity(), jo.optString("errorMsg"));
						} else {
							JSONArray ja=jo.optJSONArray("rs");
							if(ja!=null && ja.length()>0) {
								for (int i = 0; i < ja.length(); i++) {
									JSONObject subjo=ja.optJSONObject(i);
									if(subjo!=null)
										setQuestionByJson(subjo);
								}
								//if(lastFocusEt!=null)
								//	closeInputMethod(lastFocusEt);
								adapter.notifyDataSetChanged();
							}

						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				break;
			}
		}
	};
	public SchoolQuestionnaireDetailFragment() {

	}
	public SchoolQuestionnaireDetailFragment(String title,String status,
			String iunterfaceName,String autoClose) {
		if (status==null || status.equals("已结束") || status.equals("未开始")) {
			isEnable = false;
		}
		if(status==null)
			status="已结束";
		this.title = title;
		this.status = status;
		this.interfaceName = iunterfaceName;
		if(autoClose==null)
			autoClose="是";
		this.autoClose=autoClose;
	}

	 
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		registerBroastcastReceiver();
		
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		this.inflater = inflater;
		View view = inflater.inflate(R.layout.school_listview_fragment,
				container, false);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
			AppUtility.setRootViewPadding(view);
		myListview = (ListView) view.findViewById(R.id.my_listview);
		btnLeft = (Button) view.findViewById(R.id.btn_left);
		tvTitle = (TextView) view.findViewById(R.id.tv_title);
		tvRight = (TextView) view.findViewById(R.id.tv_right);
		lyLeft = (LinearLayout) view.findViewById(R.id.layout_btn_left);
		lyRight = (LinearLayout) view.findViewById(R.id.layout_btn_right);
		loadingLayout = (LinearLayout) view.findViewById(R.id.data_load);
		contentLayout = (LinearLayout) view.findViewById(R.id.content_layout);
		failedLayout = (LinearLayout) view.findViewById(R.id.empty_error);
		emptyLayout = (LinearLayout) view.findViewById(R.id.empty);
		emptytext=(TextView) view.findViewById(R.id.emptytext);
		myListview.setEmptyView(emptyLayout);
		btnLeft.setVisibility(View.VISIBLE);
		btnLeft.setCompoundDrawablesWithIntrinsicBounds(
				R.drawable.bg_btn_left_nor, 0, 0, 0);
		tvRight.setText("保存");
		tvTitle.setText(title);

		adapter = new QuestionAdapter();
		myListview.setAdapter(adapter);
		getPictureDiaLog = new Dialog(getActivity(), R.style.dialog);
		//退出
		lyLeft.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getActivity().finish();
			}
		});
		//保存数据
		lyRight.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.d(TAG, "-----保存");
				if (status.equals("已结束") || status.equals("未开始")) {
					return;
				} else {
					Log.d(TAG, "-----保存");
					View view=myListview.findFocus();
					if(view!=null && view instanceof EditText)
						view.clearFocus();
					saveQuestionAnswer();
				}
			}
		});
		// 重新加载
		failedLayout.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getQuestionsItem();
			}
		});
		((SchoolDetailActivity)getActivity()).callBack=callBack;

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//AppUtility.showToastMsg(getActivity(), "正在获取数据");
		getQuestionsItem();
	}

	private void registerBroastcastReceiver() {
		IntentFilter mFilter = new IntentFilter(Constants.GET_PICTURE);
		mFilter.addAction(Constants.DEL_OR_LOOK_PICTURE);
		getActivity().registerReceiver(mBroadcastReceiver, mFilter);
	}

	private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			String fromTag=intent.getStringExtra("TAG");
			String imageSource=intent.getStringExtra("imageSource");
			curIndex=intent.getIntExtra("position",0);
			Log.d(TAG, "--------action:" + action);
			Log.d(TAG, "--------fromTag:" + fromTag);
			if (action.equals(Constants.GET_PICTURE)&&fromTag.equals(TAG)) {
				if(imageSource!=null && imageSource.length()>0) {
					if(imageSource.equals("camera")) {
						if (AppUtility.checkPermission(getActivity(), 6, Manifest.permission.CAMERA))
							getPictureByCamera();
					}
					else if(imageSource.equals("gallery"))
					{
						if(AppUtility.checkPermission(getActivity(),7,Manifest.permission.READ_EXTERNAL_STORAGE))
							getPictureFromLocation();
					}
				}
				else
					showGetPictureDiaLog();
			}else if(action.equals(Constants.DEL_OR_LOOK_PICTURE)&&fromTag.equals(TAG)){
				//查看详图或删除图片
				delImagePath = intent.getStringExtra("imagePath");
				showDelOrShowPictureDiaLog(delImagePath);
			}
		}
	};
	
	@Override
	public void onDestroy() {
		getActivity().unregisterReceiver(mBroadcastReceiver);
		super.onDestroy();
	}

    /**
	 * 功能描述:获取图片
	 * 
	 * @author shengguo 2014-5-5 下午3:45:04
	 * 
	 */
	private void showGetPictureDiaLog() {
		View view = getActivity().getLayoutInflater()
				.inflate(R.layout.view_get_picture, null);
		Button cancel = (Button) view.findViewById(R.id.cancel);
		TextView byCamera = (TextView) view.findViewById(R.id.tv_by_camera);
		TextView byLocation = (TextView) view.findViewById(R.id.tv_by_location);
		getPictureDiaLog.setContentView(view);
		getPictureDiaLog.show();
		Window window = getPictureDiaLog.getWindow();
		window.setGravity(Gravity.BOTTOM);// 在底部弹出
		window.setWindowAnimations(R.style.CustomDialog);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getPictureDiaLog.dismiss();
			}
		});
		//调用系统相机拍照
		byCamera.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(AppUtility.checkPermission(getActivity(), 6,Manifest.permission.CAMERA))
					getPictureByCamera();
				getPictureDiaLog.dismiss();
			}
		});
		//选择本地图片
		byLocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if(AppUtility.checkPermission(getActivity(),7,Manifest.permission.READ_EXTERNAL_STORAGE))
					getPictureFromLocation();
				getPictureDiaLog.dismiss();
			}
		});
	}
	/**
	 * 功能描述:删除或查看图片
	 *
	 * @author shengguo  2014-5-8 下午6:32:49
	 *
	 */
	private void showDelOrShowPictureDiaLog(final String imageName) {
		
		View view = getActivity().getLayoutInflater()
				.inflate(R.layout.view_show_or_del_picture, null);
		Button cancel = (Button) view.findViewById(R.id.cancel);
		TextView delPicture = (TextView) view.findViewById(R.id.tv_delete);
		TextView showPicture = (TextView) view.findViewById(R.id.tv_show);
		View v = view.findViewById(R.id.view_dividing_line);
		final AlertDialog ad=new AlertDialog.Builder(getActivity()).setView(view).create();
		if(isEnable){
			delPicture.setVisibility(View.VISIBLE);
			v.setVisibility(View.VISIBLE);
		}else{
			delPicture.setVisibility(View.GONE);
			v.setVisibility(View.GONE);
		}
		
		
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
		//删除图片
		delPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String fileName=FileUtility.getFileRealName(imageName);
				SubmitDeleteinfo(fileName,1);
				ad.dismiss();
			}
		});
		//显示大图
		showPicture.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				List<String> picturePaths = new ArrayList<String>();// 选中的图片路径
				List<ImageItem> images=questions.get(curIndex).getImages();
				if(images != null)
				{
					for (int i = 0; i < images.size(); i++) {
						picturePaths.add(images.get(i).getDownAddress());
					}
				}
				Intent intent = new Intent(getActivity(), ImagesActivity.class);
				intent.putStringArrayListExtra("pics", (ArrayList<String>) picturePaths);
				
				for (int i = 0; i < picturePaths.size(); i++) {
					if(picturePaths.get(i).equals(imageName)){
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
	private void getPictureByCamera() {
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);// 调用android自带的照相机
		String sdStatus = Environment.getExternalStorageState();
		if (!sdStatus.equals(Environment.MEDIA_MOUNTED)) { // 检测sd是否可用
			AppUtility.showToastMsg(getActivity(), getString(R.string.Commons_SDCardErrorTitle));
			return;
		}
		picturePath =FileUtility.getRandomSDFileName("jpg");
		
		File mCurrentPhotoFile = new File(picturePath);

		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".fileProvider", mCurrentPhotoFile)); //Uri.fromFile(tempFile)
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
	 */
	private void getPictureFromLocation() {
		/*
		picturePaths.remove("");
		Intent intent = new Intent(getActivity(),AlbumActivity.class);
		intent.putStringArrayListExtra("picturePaths",
				(ArrayList<String>) picturePaths);
		intent.putExtra("size", 5);
		startActivityForResult(intent,
				SchoolDetailActivity.REQUEST_CODE_TAKE_PICTURE);
		*/
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
			AppUtility.showToastMsg(getActivity(), "SD卡不可用");
		}
		
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

	/**
	 * 功能描述:获取问卷内容
	 * 
	 * @author shengguo 2014-4-16 上午11:12:43
	 * 
	 */

	public void getQuestionsItem() {
		showProgress(true);
		Log.d(TAG, "--------" + String.valueOf(new Date().getTime()));
		long datatime = System.currentTimeMillis();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "----------datatime:" + datatime);
		Log.d(TAG, "----------checkCode:" + checkCode + "++");
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", datatime);
			jo.put("language", language);
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

	/**
	 * 功能描述:保存问卷答案
	 * 
	 * @author shengguo 2014-5-5 下午5:29:30
	 * 
	 */
	private void saveQuestionAnswer() {
		Log.d(TAG, "--------" + String.valueOf(new Date().getTime()));
		
		long datatime = System.currentTimeMillis();
		Locale locale = getResources().getConfiguration().locale;
		String language = locale.getCountry();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		// String userId = PrefUtility.get(Constants.PREF_USER_ID, "");
		// String fromId = PrefUtility.get(Constants.PREF_USER_NUNMBER, "");
		Log.d(TAG, "----------datatime:" + datatime);
		Log.d(TAG, "----------checkCode:" + checkCode + "++");
		JSONArray joarr = getAnswers();
		if(joarr==null){
			return ;
		}
		JSONObject jo = new JSONObject();
		try {
			jo.put("用户较验码", checkCode);
			jo.put("选项记录集", joarr);
			if(questionnaireList.getNeedLocation().equals("是")) {
				User user = ((CampusApplication) getActivity().getApplicationContext()).getLoginUserObj();
				jo.put("GPS定位", user.getLatestGps()+"\n"+user.getLatestAddress());
			}
			jo.put("DATETIME", datatime);
			jo.put("language", language);
			jo.put("client", "Android");
			// jo.put("USER_ID", userId);//可不传
			// jo.put("SCHOOLID", 0);//可不传
			// / jo.put("FROMID", fromId);//可不传
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		dialog = DialogUtility.createLoadingDialog(getActivity(),
				"保存中...");
		dialog.show();
		Log.d(TAG, "------->jo:" + jo.toString());
		String base64Str = Base64.encode(jo.toString().getBytes());
		Log.d(TAG, "------->base64Str:" + base64Str);
		CampusParameters params = new CampusParameters();
		params.add(Constants.PARAMS_DATA, base64Str);
		int pos=interfaceName.indexOf("?");
		String preUrl=interfaceName;
		if(pos>-1)
			preUrl=interfaceName.substring(0, pos);
		CampusAPI.getSchoolItem(params,
				preUrl + questionnaireList.getSubmitTo(),
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
						msg.what = 1;
						msg.obj = response;
						mHandler.sendMessage(msg);
					}
				});
	}
	
	/**
	 * 功能描述:处理文件上传
	 * 
	 * @author shengguo 2013-12-26 下午4:36:51
	 * 
	 * @param
	 *
	 */
	public void uploadFile(File file,int type)  {
		if(!file.exists()) return;
		if(AppUtility.formetFileSize(file.length()) > 5242880*2){
			AppUtility.showToastMsg(getActivity(), "对不起，您上传的文件太大了，请选择小于10M的文件！");
		}else{
			 
	        try
	        {
	        	if(type==1)
	        		ImageUtility.rotatingImageIfNeed(file.getAbsolutePath());
	        	DownloadSubject	downloadSubject = new DownloadSubject();
				String filebase64Str = FileUtility.fileupload(file);
				downloadSubject.setFilecontent(filebase64Str);
				String filename = file.getName();
				downloadSubject.setFileName(filename);
				downloadSubject.setLocalfile(file.getAbsolutePath());
				downloadSubject.setFilesize(file.length());
				SubmitUploadFile(downloadSubject,type);
				
	        }
	        catch(Exception e)
	        {
	        	e.printStackTrace();
	        }
		}
	}
	/**
	 * 功能描述:上传文件
	 *
	 * @author shengguo  2013-12-18 上午11:48:59
	 * 

	 */
	public void SubmitUploadFile(final DownloadSubject downloadSubject,final int type){
		
		final CampusParameters params = new CampusParameters();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");// 获取用户校验码
		params.add("用户较验码", checkCode);
		params.add("课程名称", questionnaireList.getTitle());
		params.add("老师上课记录编号", questionnaireList.getTitle());
		params.add("图片类别", "问卷调查");
		params.add("文件名称", downloadSubject.getFileName());
		params.add("WenJianMing", downloadSubject.getFileName());
		params.add("文件内容", downloadSubject.getFilecontent());
		if(type==1)
		{
			List<String> picturePaths=myPictureAdapter.getPicPaths();
			picturePaths.remove("");
			picturePaths.add("loading");
			myPictureAdapter.setPicPaths(picturePaths);
		}
		else if(type==2)
		{
			progressDlg=ProgressDialog.show(getActivity(), "", "上传中...",true,false);
		}
		//myPictureAdapter.notifyDataSetChanged();
		
		CampusAPI.uploadFiles(params, new RequestListener(){

			@Override
			public void onComplete(String response) {
				Log.d(TAG, "------------------response"+response);
				
				Message msg = new Message();
				msg.what = 3;
				msg.obj = response;
				Bundle data=new Bundle();
				data.putString("oldFileName", params.getValue("文件名称"));
				data.putInt("type",type);
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
				
				Message msg = new Message();
				msg.what = -1;
				msg.obj = e.getMessage();
				mHandler.sendMessage(msg);	
			}
		});
	}
	/**
	 * 功能描述:获取答案
	 * 
	 * @author shengguo 2014-5-5 下午5:37:56
	 * 
	 * @return
	 */
	private JSONArray getAnswers() {
		JSONArray joarr = new JSONArray();
		for (int i = 0; i < questions.size(); i++) {
			String mStatus = questions.get(i).getStatus();
			if(mStatus.equals("图片"))
			{
				JSONArray joimages = new JSONArray();
				List<ImageItem> images=questions.get(i).getImages();
				for (ImageItem imageItem :images) {
					JSONObject joimgs = new JSONObject();
					try {
						joimgs.put("文件名", imageItem.getFileName());
						joimgs.put("文件地址", imageItem.getDownAddress());
						joimgs.put("课程名称", imageItem.getCurriculumName());
						joimgs.put("下载次数", imageItem.getLoadCount());
						joimgs.put("上课记录编号", imageItem.getSubjectId());
						joimgs.put("最后一次下载", imageItem.getLastDown());
						joimgs.put("名称", imageItem.getName());
						joimgs.put("STATUS", "OK");
						joimages.put(joimgs);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				String isRequired = questions.get(i).getIsRequired();//是否必填
				if(isRequired.equals("是") && joimages.length()==0){
					AppUtility.showToastMsg(getActivity(),"请填写所有必填项");
					myListview.setSelection(i);
					return null;
				}
				joarr.put(joimages);
			}
			else if(mStatus.equals("附件"))
			{
				
				JSONArray fujianArray=questions.get(i).getFujianArray();
				for (int j=0;i<fujianArray.length();j++) {
					JSONObject joimgs = null;
					try {
						joimgs = fujianArray.getJSONObject(j);
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					try {
						joimgs.put("文件名", joimgs.optString("newname"));
						joimgs.put("文件地址",joimgs.optString("url"));
						joimgs.put("课程名称", questionnaireList.getTitle());
						joimgs.put("下载次数","0");
						joimgs.put("上课记录编号", questionnaireList.getTitle());
						joimgs.put("最后一次下载", "0");
						joimgs.put("名称", joimgs.optString("name"));
						joimgs.put("STATUS", "OK");
						fujianArray.put(joimgs);
					} catch (JSONException e) {
						e.printStackTrace();
					}
				}
				String isRequired = questions.get(i).getIsRequired();//是否必填
				if(isRequired.equals("是") && fujianArray.length()==0){
					AppUtility.showToastMsg(getActivity(),"请填写所有必填项");
					myListview.setSelection(i);
					return null;
				}
				joarr.put(fujianArray);
			}
			else if(mStatus.equals("弹出列表") || mStatus.equals("弹出多选"))
			{
				JSONArray fujianArray=questions.get(i).getFujianArray();
				String isRequired = questions.get(i).getIsRequired();//是否必填
				if(isRequired.equals("是") && fujianArray.length()==0){
					AppUtility.showToastMsg(getActivity(),"请填写所有必填项");
					myListview.setSelection(i);
					return null;
				}
				joarr.put(fujianArray);
			}
			else
			{
				String usersAnswer = questions.get(i).getUsersAnswer();
				String isRequired = questions.get(i).getIsRequired();//是否必填
				String validate=questions.get(i).getValidate();
				if(AppUtility.isNotEmpty(isRequired) && isRequired.equals("是")){
						if(AppUtility.isNotEmpty(usersAnswer)){
							joarr.put(usersAnswer);
						}else{
							AppUtility.showToastMsg(getActivity(),"请填写所有必填项");
							myListview.setSelection(i);
							return null;
						}
				}else{
					joarr.put(usersAnswer);
				}
				if(AppUtility.isNotEmpty(validate) && AppUtility.isNotEmpty(usersAnswer)){
					if(validate.equals("手机号") && !AppUtility.checkPhone(usersAnswer))
					{
						AppUtility.showToastMsg(getActivity(),title+",格式不正确");
						myListview.setSelection(i);
						return null;
					}
					else if(validate.equals("浮点型") && !AppUtility.isDecimal(usersAnswer))
					{
						AppUtility.showToastMsg(getActivity(),title+",必须是浮点型数字,如:99.9");
						myListview.setSelection(i);
						return null;
					}
					else if(validate.equals("整型") && !AppUtility.isInteger(usersAnswer))
					{
						AppUtility.showToastMsg(getActivity(),title+",必须整形数字,如:99");
						myListview.setSelection(i);
						return null;
					}
					else if(validate.equals("邮箱") && !AppUtility.checkEmail(usersAnswer))
					{
						AppUtility.showToastMsg(getActivity(),title+",邮箱格式不正确");
						myListview.setSelection(i);
						return null;
					}
				}
			}
		}
		Log.d(TAG, joarr.toString());
		return joarr;
	}
	/**
	 * 功能描述:删除图片
	 *
	 * @author shengguo  2014-5-9 下午12:05:03
	 * 
	 * @param fileName
	 */
	public void SubmitDeleteinfo(String fileName,final int type) {
		JSONObject jo = new JSONObject();
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		Log.d(TAG, "--------------filename----------" + fileName);
		try {
			jo.put("用户较验码", checkCode);
			jo.put("DATETIME", String.valueOf(new Date().getTime()));
			jo.put("课件名称", fileName);
			jo.put("图片类别", "问卷调查");
		} catch (JSONException e) {
			e.printStackTrace();
		}
		String base64Str = Base64.encode(jo.toString()
				.getBytes());
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
				Bundle data=new Bundle();
				data.putInt("type",type);
				msg.setData(data);
				mHandler.sendMessage(msg);
			}
		});
	}
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		switch (requestCode) {
			case REQUEST_CODE_TAKE_CAMERA: // 拍照返回
				//Bundle bundle = data.getExtras();
				//Bitmap bitmap = (Bitmap) bundle.get("data");// 获取相机返回的数据，并转换为Bitmap图片格式
				//ImageUtility.writeTofiles(bitmap, picturePath);
				uploadFile(new File(picturePath),1);
				
				break;
			case REQUEST_CODE_TAKE_PICTURE:
				
				if (data != null) {
					Uri uri = data.getData();
					String[] pojo  = { MediaStore.Images.Media.DATA };
					CursorLoader cursorLoader = new CursorLoader(getActivity(), uri, pojo, null,null, null); 
					Cursor cursor = cursorLoader.loadInBackground();
					cursor.moveToFirst(); 
				    picturePath = cursor.getString(cursor.getColumnIndex(pojo[0])); 
					
					String tempPath =FileUtility.getRandomSDFileName("jpg");
					if(FileUtility.copyFile(picturePath,tempPath))
						uploadFile(new File(tempPath),1);
					else
						AppUtility.showErrorToast(getActivity(), "向SD卡复制文件出错");
				}
				break;
			case REQUEST_CODE_TAKE_DOCUMENT:
				if (resultCode == Activity.RESULT_OK) 
				{
					Uri uri = data.getData();
					String filepath=FileUtility.getFilePathInSD(getActivity(),uri);
					if(filepath!=null)
						uploadFile(new File(filepath),2);
					
				}
				break;
			case REQUEST_CODE_SelectMuti:
				if(resultCode==1)
				{
					String returnJsonStr=data.getStringExtra("returnJson");
					JSONArray returnJson=null;
					try {
						returnJson=new JSONArray(returnJsonStr);
					} catch (JSONException e) {
						e.printStackTrace();
					}
					int index=data.getIntExtra("curIndex",-1);
					if(index>-1 && returnJson!=null)
					{
						Question question=questions.get(index);
						question.setFujianArray(returnJson);
						adapter.notifyDataSetChanged();
					}
				}
				break;

		}
	}


    class QuestionAdapter extends BaseAdapter {
	
		int mFocusPosition = -1;
		private HashMap<Integer, QuestionAdapter.OnFocusChangeListenerImpl> listenerhm=new HashMap();
		private HashMap<Integer, MaxLengthWatcher> listenertchm=new HashMap();
		@Override
		public int getCount() {
			return questions.size();
		}

		@Override
		public Object getItem(int position) {
			return questions.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

	
		@SuppressWarnings("deprecation")
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
						
			final Question question = (Question) getItem(position);
			convertView = inflater.inflate(R.layout.school_questionnaire_item, parent, false);
			final ViewHolder holder = new ViewHolder();
			holder.lv_layout=(LinearLayout) convertView.findViewById(R.id.lv_layout);
			holder.lv_parentlayout=(LinearLayout)convertView.findViewById(R.id.lv_parentlayout);
			holder.title = (TextView) convertView.findViewById(R.id.tv_questionnaire_name);
			holder.radioGroup = (RadioGroup) convertView.findViewById(R.id.rg_choose);
			holder.multipleChoice = (NonScrollableListView) convertView.findViewById(R.id.lv_choose);
			holder.etAnswer = (EditText) convertView.findViewById(R.id.et_answer);
			holder.tvAnswer = (TextView) convertView.findViewById(R.id.tv_answer);
			holder.imageGridView = (NonScrollableGridView) convertView.findViewById(R.id.grid_picture);
			holder.tvRemark = (TextView) convertView.findViewById(R.id.tv_remark);
			holder.bt_date=(Button)convertView.findViewById(R.id.bt_date);
			holder.bt_datetime=(Button)convertView.findViewById(R.id.bt_datetime);
			holder.sp_select=(Spinner)convertView.findViewById(R.id.sp_select);
			holder.sp_select1=(Spinner)convertView.findViewById(R.id.sp_select1);
			holder.sp_select2=(Spinner)convertView.findViewById(R.id.sp_select2);
			holder.et_autotext = (AutoCompleteTextView ) convertView.findViewById(R.id.et_autotext);
			OnFocusChangeListenerImpl listener=listenerhm.get(position);
			if(listener==null) {
				listener=new OnFocusChangeListenerImpl(position);
				listenerhm.put(position,listener);
			}
			holder.etAnswer.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.setOnFocusChangeListener(listenerhm.get(position));
					if (canVerticalScroll(holder.etAnswer)) {
						v.getParent().requestDisallowInterceptTouchEvent(true);
						if (event.getAction() == MotionEvent.ACTION_UP) {
							v.getParent().requestDisallowInterceptTouchEvent(false);
						}
					}
					return false;
				}
			});
			holder.et_autotext.setOnTouchListener(new View.OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					v.setOnFocusChangeListener(listenerhm.get(position));
					return false;
				}
			});
			holder.etAnswer.setTag(position);
			holder.et_autotext.setTag(position);
			if (lastFocusEt!=null && lastFocusEt.getTag().equals(position))
			{
				EditText et=(EditText) convertView.findViewById(lastFocusEt.getId());
				if(et!=null) {
					et.setOnFocusChangeListener(listenerhm.get(position));
					popInputDelay(et);
				}
			}
			convertView.setOnTouchListener(touchListener);
			if(question.isIfHide())
				holder.lv_parentlayout.setVisibility(View.GONE);
			else
				holder.lv_parentlayout.setVisibility(View.VISIBLE);
			String mStatus = question.getStatus();
			String addstr="";
			if(question.getIsRequired().equals("是") && !question.getTitle().endsWith("*"))
				addstr="*";
			holder.title.setText(position+1+"."+question.getTitle()+addstr);
			String remark = question.getRemark();
			if(AppUtility.isNotEmpty(remark) && remark.trim().length()>0){
				holder.tvRemark.setText(remark);
				holder.tvRemark.setVisibility(View.VISIBLE);
				if(question.getRemardColor().length()>0)
					holder.tvRemark.setTextColor(Color.parseColor(question.getRemardColor()));
				else
					holder.tvRemark.setTextColor(Color.parseColor("black"));
				if(status.equals("已结束") && !mStatus.equals("单行文本输入框") && !mStatus.equals("图片") && !mStatus.equals("日期")){

					if(remark.length()>=7 && (remark.substring(0, 7).equals("答题状态:错误") || remark.indexOf("error")>0)){
						holder.tvRemark.setTextColor(getActivity().getResources().getColor(R.color.red_color));
					}else if(remark.length()>=7 && (remark.substring(0, 7).equals("答题状态:正确") || remark.indexOf("right")>0)){
						holder.tvRemark.setTextColor(getActivity().getResources().getColor(R.color.subject_current));
					}
					else
						holder.tvRemark.setTextColor(Color.BLUE);
				}
			}
			else
				holder.tvRemark.setVisibility(View.GONE);
			if (mStatus.equals("单选")) {
				setAllGone(holder);
				holder.radioGroup.setVisibility(View.VISIBLE);
				final String[] answers = question.getOptions();
				final List<JSONObject> jsonanswers = question.getOptionsJson();
				holder.radioGroup.removeAllViews();
				int checkIndex = -1;
				holder.radioGroup.setOnCheckedChangeListener(null);
				for (int i = 0; i < answers.length; i++) {
					View v= inflater.inflate(R.layout.my_radiobutton, parent, false);
					RadioButton radioButton = (RadioButton) v.findViewById(R.id.rb_chenck);
					radioButton.setText(answers[i].toString());
					radioButton.setTextSize(12.0f);
					radioButton.setId(i);
					boolean bflag=false;
					if(isEnable && !question.isIfRead())
						bflag=true;
					radioButton.setEnabled(bflag);
					if (answers[i].equals(question.getUsersAnswer())) {
						checkIndex = i;
					}
					holder.radioGroup.addView(radioButton);
				}
				for (int i = 0; i < jsonanswers.size(); i++) {
					JSONObject objItem=jsonanswers.get(i);
					String key=objItem.optString("key");
					String value=objItem.optString("value");
					View v= inflater.inflate(R.layout.my_radiobutton, parent, false);
					RadioButton radioButton = (RadioButton) v.findViewById(R.id.rb_chenck);
					radioButton.setText(value);
					radioButton.setTextSize(12.0f);
					radioButton.setId(i);
					boolean bflag=false;
					if(isEnable && !question.isIfRead())
						bflag=true;
					radioButton.setEnabled(bflag);
					if (key.equals(question.getUsersAnswer())) {
						checkIndex = i;
					}
					radioButton.setTag(key);
					holder.radioGroup.addView(radioButton);
				}
				if (checkIndex != -1) {
					holder.radioGroup.clearCheck();
					holder.radioGroup.check(checkIndex);
				} else {
					holder.radioGroup.clearCheck();
				}
				holder.radioGroup
						.setOnCheckedChangeListener(new OnCheckedChangeListener() {

							@Override
							public void onCheckedChanged(RadioGroup group,
									int checkedId) {
								if(answers.length>0)
									question.setUsersAnswer(answers[checkedId]);
								else if(jsonanswers.size()>0)
								{
									JSONObject objItem=jsonanswers.get(checkedId);
									String key=objItem.optString("key");
									question.setUsersAnswer(key);
								}
								questions.set(position, question);
								//questionnaireList.setQuestions(questions);
								int linkIndex=question.getLinkUpdate();
								if(linkIndex>0)
								{
									Question linkItem=questions.get(linkIndex);
									JSONObject obj=linkItem.getFilterObj();
									if(obj!=null && obj.length()>0)
									{
										JSONArray ja=obj.optJSONArray(question.getUsersAnswer());
										if(ja!=null){
											String[] options = new String[ja.length()];
											for (int i = 0; i < ja.length(); i++) {
												options[i] = ja.optString(i);
											}
											linkItem.setOptions(options);
											
										}
										else
										{
											linkItem.setOptions(new String[0]);
										}
										questions.set(linkIndex, linkItem);
										adapter.notifyDataSetChanged();
									}
								}
								if(question.getCallback().length()>0) {
									//startTimer(question,300,null);
									String callback=question.getCallback()+"&"+question.getTitle()+"="+question.getUsersAnswer();
									sendCallBack(callback,4);
								}
							}
						});
			}
			else if (mStatus.equals("多选")) {
				setAllGone(holder);
				holder.multipleChoice.setVisibility(View.VISIBLE);
				CheckBoxAdapter checkBoxAdapter = new CheckBoxAdapter(
						getActivity(), position, question);
				holder.multipleChoice.setAdapter(checkBoxAdapter);
				
			}
			else if (mStatus.equals("单行文本输入框")) {
				setAllGone(holder);
				if (status.equals("已结束") || status.equals("未开始")) {
					holder.etAnswer.setVisibility(View.GONE);
					holder.tvAnswer.setVisibility(View.VISIBLE);
					holder.tvAnswer.setText(question.getUsersAnswer());
				} else {
					holder.etAnswer.setVisibility(View.VISIBLE);
					holder.etAnswer.setEnabled(!question.isIfRead());
					holder.tvAnswer.setVisibility(View.GONE);
					holder.etAnswer.setText(question.getUsersAnswer());
					if (question.getLines() <= 2) {
						holder.etAnswer.setSingleLine();
						holder.etAnswer.setLines(1);
						if(question.getValidate().equals("浮点型"))
							holder.etAnswer.setInputType(EditorInfo.TYPE_CLASS_NUMBER| EditorInfo.TYPE_NUMBER_FLAG_DECIMAL|EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
						else if(question.getValidate().equals("整型"))
							holder.etAnswer.setInputType(EditorInfo.TYPE_CLASS_NUMBER|EditorInfo.TYPE_NUMBER_FLAG_SIGNED);
						else if(question.getValidate().equals("邮箱"))
							holder.etAnswer.setInputType(EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
						else if(question.getValidate().equals("手机号"))
							holder.etAnswer.setInputType(EditorInfo.TYPE_CLASS_PHONE);
						else
							holder.etAnswer.setInputType(EditorInfo.TYPE_CLASS_TEXT);
					} else {
						holder.etAnswer.setInputType(EditorInfo.TYPE_CLASS_TEXT);
						holder.etAnswer.setSingleLine(false);
						holder.etAnswer.setLines(question.getLines());
					}
					MaxLengthWatcher txw=listenertchm.get(position);
					if(txw!=null)
						holder.etAnswer.removeTextChangedListener(txw);
					if(question.getMaxLetter()>0) {
						txw=new MaxLengthWatcher(question.getMaxLetter(), holder.etAnswer,getActivity());
						holder.etAnswer.addTextChangedListener(txw);
						listenertchm.put(position,txw);
					}
					if(question.getBackgroundcolor().length()>0)
						holder.lv_layout.setBackgroundColor(Color.parseColor(question.getBackgroundcolor()));
					else
						holder.lv_layout.setBackgroundColor(Color.TRANSPARENT);
					
				}
			}
			else if (mStatus.equals("图片")) {
				setAllGone(holder);
				holder.imageGridView.setVisibility(View.VISIBLE);
				int size=5;
				if(question.getLines()>0)
					size=question.getLines();
				boolean bflag=false;
				if(!question.isIfRead() && isEnable)
					bflag=true;
				myPictureAdapter = new MyPictureAdapter(getActivity(),bflag,new ArrayList<String>(),size,"调查问卷");
				myPictureAdapter.setFrom(TAG);
				myPictureAdapter.setCurIndex(position);
				myPictureAdapter.setPicPathsByImages(question.getImages());
				myPictureAdapter.setImageSource(question.getImageSource());
				holder.imageGridView.setAdapter(myPictureAdapter);
			}
			else if (mStatus.equals("日期")) {
				setAllGone(holder);
				holder.bt_date.setVisibility(View.VISIBLE);
				boolean bflag=false;
				if(!question.isIfRead() && isEnable)
					bflag=true;
				holder.bt_date.setEnabled(bflag);
				if(!AppUtility.isNotEmpty(question.getUsersAnswer()))
				{
					question.setUsersAnswer(DateHelper.getToday());
				}
				holder.bt_date.setText(question.getUsersAnswer());

				holder.bt_date.setOnClickListener(new OnClickListener(){
					
					private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener(){  //
						@Override
						public void onDateSet(DatePicker arg0, int arg1, int arg2, int arg3) {
						
							question.setUsersAnswer(DateHelper.getDateString(new Date(arg1-1900,arg2,arg3), "yyyy-MM-dd"));
							Button bt=(Button)arg0.getTag();
							bt.setText(question.getUsersAnswer());
						}
					};
						
					@Override
					public void onClick(View v) {
						Date dt=DateHelper.getStringDate(question.getUsersAnswer(), "yyyy-MM-dd");
						Calendar cal=Calendar.getInstance();
						cal.setTime(dt);
						DatePickerDialog dialog = new DatePickerDialog(getActivity(),listener,cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH));
						dialog.getDatePicker().setTag(v);
						if(question.getOptions().length==2)
						{
							Date minDt=DateHelper.getStringDate(question.getOptions()[0], "yyyy-MM-dd");
							Date maxDt=DateHelper.getStringDate(question.getOptions()[1], "yyyy-MM-dd");
							dialog.getDatePicker().setMinDate(minDt.getTime());
							dialog.getDatePicker().setMaxDate(maxDt.getTime());
						}
						dialog.setButton2("取消", new DialogInterface.OnClickListener() 
						{
							@Override
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								dialog.dismiss();
							}
						});
						dialog.show();
					}
					
				});
	
			}
			else if (mStatus.equals("日期时间")) {
				setAllGone(holder);
				holder.bt_datetime.setVisibility(View.VISIBLE);
				boolean bflag=false;
				if(!question.isIfRead() && isEnable)
					bflag=true;
				holder.bt_datetime.setEnabled(bflag);
				if(!AppUtility.isNotEmpty(question.getUsersAnswer()))
				{
					question.setUsersAnswer(DateHelper.getToday());
				}
				holder.bt_datetime.setText(question.getUsersAnswer());

				holder.bt_datetime.setOnClickListener(new OnClickListener(){
				
					@Override
					public void onClick(View v) {
						
						DateTimePickDialogUtil dialog = new DateTimePickDialogUtil(getActivity(),question.getUsersAnswer(),"yyyy-MM-dd HH:mm");
						Button bt=(Button)v;
						bt.setTag(question);
						dialog.dateTimePicKDialog(bt);
						
					}
					
				});
	
			}
			else if (mStatus.equals("下拉")) {
				setAllGone(holder);
				holder.sp_select.setVisibility(View.VISIBLE);
				boolean bflag=false;
				if(!question.isIfRead() && isEnable)
					bflag=true;
				holder.sp_select.setEnabled(bflag);

				int pos=0;
				for(int i=0;i<question.getOptions().length;i++)
				{
					if(question.getOptions()[i].equalsIgnoreCase(question.getUsersAnswer()))
						pos=i;
				}
				String [] listStr=new String[question.getOptionsJson().size()];
				for(int i=0;i<question.getOptionsJson().size();i++)
				{
					JSONObject obj=question.getOptionsJson().get(i);
					if(i==0 && !AppUtility.isNotEmpty(question.getUsersAnswer()))
						question.setUsersAnswer(obj.optString("key"));
					listStr[i]=obj.optString("value");
					if(obj.optString("key").equals(question.getUsersAnswer()))
						pos=i;
				}
				ArrayAdapter<String> aa;
				if(question.getOptions().length>0)
					aa = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,question.getOptions());
				else
					aa = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,listStr);
				holder.sp_select.setAdapter(aa);
				holder.sp_select.setSelection(pos);
				holder.sp_select.setOnItemSelectedListener(new OnItemSelectedListener() {
					
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						boolean ischanged=false;
						if(question.getOptions().length>0) {
							if(!question.getUsersAnswer().equals(question.getOptions()[position])) {
								ischanged = true;
								question.setUsersAnswer(question.getOptions()[position]);
							}
						}
						else
						{
							JSONObject obj=question.getOptionsJson().get(position);
							if(!question.getUsersAnswer().equals(obj.optString("key"))) {
								ischanged = true;
								question.setUsersAnswer(obj.optString("key"));
							}
						}
						if(question.getCallback().length()>0 && ischanged) {
							//startTimer(question,300,null);
							String callback=question.getCallback()+"&"+question.getTitle()+"="+question.getUsersAnswer();
							sendCallBack(callback,4);
						}

					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
						
					}
				});
				
			}
			else if (mStatus.equals("附件")) {
				setAllGone(holder);
				holder.multipleChoice.setVisibility(View.VISIBLE);

				SimpleAdapter fujianAdapter=setupFujianAdpter(question);
				holder.multipleChoice.setAdapter(fujianAdapter);
				holder.multipleChoice.setTag(position);
				boolean bflag=false;
				if(!question.isIfRead() && isEnable)
					bflag=true;
				final boolean finalBflag = bflag;
				holder.multipleChoice.setOnItemClickListener(new OnItemClickListener(){
					
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						curIndex=(Integer) parent.getTag();
						final HashMap<String, Object> item=(HashMap<String, Object>) parent.getAdapter().getItem(position);
						if(finalBflag)
						{
							if(item.get("url").toString().length()>0)
							{
								new AlertDialog.Builder(view.getContext())
										.setMessage("是否删除此附件?")
										.setPositiveButton("是", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which) {

												SubmitDeleteinfo(item.get("newname").toString(),2);
											}})
										.setNegativeButton("否", null)
										.show();
							}
							else
							{
								if (Build.VERSION.SDK_INT >= 23)
								{
									if(AppUtility.checkPermission(getActivity(), 7,Manifest.permission.READ_EXTERNAL_STORAGE))
										getFujian();
								}
								else
									getFujian();

							}
						}
						else if(item.get("url").toString().length()>0)
							AppUtility.downloadAndOpenFile(item.get("url").toString(),view);

					}     
				});
				
			}
			else if (mStatus.equals("弹出列表")) {
				setAllGone(holder);
				holder.multipleChoice.setVisibility(View.VISIBLE);

				ListOfBillAdapter billAdapter=setupPeiJianAdpter(question,position);
				holder.multipleChoice.setAdapter(billAdapter);
				holder.multipleChoice.setTag(position);
				
			}
			else if (mStatus.equals("二级下拉")) {
				setAllGone(holder);
				holder.sp_select.setVisibility(View.VISIBLE);
				holder.sp_select1.setVisibility(View.VISIBLE);
				boolean bflag=false;
				if(!question.isIfRead() && isEnable)
					bflag=true;
				holder.sp_select.setEnabled(bflag);
				holder.sp_select1.setEnabled(bflag);
				int pos=0;
				for(int i=0;i<question.getOptions().length;i++)
				{
					if(question.getOptions()[i].equalsIgnoreCase(question.getUsersAnswerOne()))
						pos=i;
				}
				String [] listStr=new String[question.getOptionsJson().size()];
				for(int i=0;i<question.getOptionsJson().size();i++)
				{
					JSONObject obj=question.getOptionsJson().get(i);
					if(i==0 && !AppUtility.isNotEmpty(question.getUsersAnswer()))
						question.setUsersAnswer(obj.optString("key"));
					listStr[i]=obj.optString("value");
					if(obj.optString("key").equals(question.getUsersAnswer()))
						pos=i;
				}
				ArrayAdapter<String> aa;
				if(question.getOptions().length>0)
					aa = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,question.getOptions());
				else
					aa = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,listStr);
				holder.sp_select.setAdapter(aa);
				holder.sp_select.setSelection(pos);
				holder.sp_select.setOnItemSelectedListener(new OnItemSelectedListener() {
					
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						if(question.getOptions().length>0)
							question.setUsersAnswerOne(question.getOptions()[position]);
						else
						{
							JSONObject obj=question.getOptionsJson().get(position);
							question.setUsersAnswerOne(obj.optString("key"));
						}
						reloadSpinner2(holder.sp_select1,question);
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
						
					}
				});
				reloadSpinner2(holder.sp_select1,question);
				holder.sp_select1.setOnItemSelectedListener(new OnItemSelectedListener() {
					
					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						// TODO Auto-generated method stub
						JSONArray subOptionsJson=question.getSubOptions().optJSONArray(question.getUsersAnswerOne());
						JSONObject item=subOptionsJson.optJSONObject(holder.sp_select1.getSelectedItemPosition());
						if(item!=null)
							question.setUsersAnswer(item.optString("id"));
						else
						{
							String itemvalue=subOptionsJson.optString(holder.sp_select1.getSelectedItemPosition());
							if(itemvalue!=null)
								question.setUsersAnswer(itemvalue);
						}
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub
						
					}
				});
				
			}
			else if (mStatus.equals("三级下拉")) {
				setAllGone(holder);
				holder.sp_select.setVisibility(View.VISIBLE);
				holder.sp_select1.setVisibility(View.VISIBLE);
				holder.sp_select2.setVisibility(View.VISIBLE);
				boolean bflag=false;
				if(!question.isIfRead() && isEnable)
					bflag=true;
				holder.sp_select.setEnabled(bflag);
				holder.sp_select1.setEnabled(bflag);
				holder.sp_select2.setEnabled(bflag);

				int pos1=0;
				String answer2="";
				try {
					for(int i=0;i<question.getOptions().length;i++) {
						String key1 = question.getOptions()[i];//key=省
						JSONObject json2 = question.getSubOptions().optJSONObject(key1);
						Iterator<String> it1 = json2.keys();
						while (it1.hasNext()) {
							String key2 = it1.next();//key=市
							JSONArray json3 = json2.getJSONArray(key2);
							for (int j = 0; j < json3.length(); j++) {
								JSONObject townItem =  json3.getJSONObject(j);
								if (townItem.optString("id").equals(question.getUsersAnswer())) {
									pos1 = i;
									answer2 = key2;
								}
							}
						}
					}
				}catch (JSONException e) {
					e.printStackTrace();
				}
				question.setUsersAnswerOne(question.getOptions()[pos1]);
				question.setUsersAnswerTwo(answer2);
				ArrayAdapter<String> aa = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,question.getOptions());
				holder.sp_select.setAdapter(aa);
				holder.sp_select.setSelection(pos1);

				holder.sp_select.setOnItemSelectedListener(new OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent,
											   View view, int position, long id) {
						question.setUsersAnswerOne(question.getOptions()[position]);
						reloadThreeSpinner1(holder.sp_select1,question,holder.sp_select2);

					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});

				holder.sp_select1.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
											   View view, int position, long id) {
						// TODO Auto-generated method stub
						String selitem=(String)parent.getAdapter().getItem(position);
						question.setUsersAnswerTwo(selitem);
						JSONObject subOptionsJson=question.getSubOptions().optJSONObject(question.getUsersAnswerOne());
						JSONArray grade3= null;
						try {
							grade3 = subOptionsJson.getJSONArray(selitem);
						} catch (JSONException e) {
							e.printStackTrace();
						}
						reloadThreeSpinner2(holder.sp_select2,question,grade3);

					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});

				holder.sp_select2.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
											   View view, int position, long id) {
						JSONObject subOptionsJson=question.getSubOptions().optJSONObject(question.getUsersAnswerOne());
						JSONArray grade3=subOptionsJson.optJSONArray(question.getUsersAnswerTwo());
						JSONObject item=grade3.optJSONObject(position);
						if(item!=null)
							question.setUsersAnswer(item.optString("id"));
					}
					@Override
					public void onNothingSelected(AdapterView<?> parent) {
						// TODO Auto-generated method stub

					}
				});
				reloadThreeSpinner1(holder.sp_select1,question,holder.sp_select2);
			}
			else if (mStatus.equals("弹出多选")) {
				setAllGone(holder);
				holder.multipleChoice.setVisibility(View.VISIBLE);

				ListViewImageAdapter fujianAdapter=setupPopMutiAdpter(question);
				holder.multipleChoice.setAdapter(fujianAdapter);
				holder.multipleChoice.setTag(position);
				boolean bflag=false;
				if(!question.isIfRead() && isEnable)
					bflag=true;
				final boolean finalBflag = bflag;
				holder.multipleChoice.setOnItemClickListener(new OnItemClickListener(){

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
											int position, long id) {
						curIndex=(Integer) parent.getTag();
						final HashMap<String, Object> item=(HashMap<String, Object>) parent.getAdapter().getItem(position);
						if(finalBflag)
						{
							if(item.get("id").toString().length()>0)
							{
								new AlertDialog.Builder(view.getContext())
										.setMessage("是否删除此行?")
										.setPositiveButton("是", new DialogInterface.OnClickListener()
										{
											@Override
											public void onClick(DialogInterface dialog, int which) {
												for(int i=0;i<question.getFujianArray().length();i++)
												{
													JSONObject jo= null;
													try {
														jo = question.getFujianArray().getJSONObject(i);
														if(jo.optString("id").equals(item.get("id").toString()))
														{
															question.getFujianArray().remove(i);
															adapter.notifyDataSetChanged();
															break;
														}
													} catch (JSONException e) {
														e.printStackTrace();
													}

												}
											}})
										.setNegativeButton("否", null)
										.show();
							}
							else
							{
								Intent intent = new Intent(getActivity(), StudentSelectActivity.class);
								intent.putExtra("选项",question.getOptions());
								//intent.putExtra("子选项",question.getSubOptions().toString());
								multiListData=question.getSubOptions();
								intent.putExtra("用户答案",question.getFujianArray().toString());
								intent.putExtra("curIndex",curIndex);
								startActivityForResult(intent,REQUEST_CODE_SelectMuti);
							}
						}

					}
				});

			}
			else if (mStatus.equals("下拉提示框")) {
				setAllGone(holder);
				if (status.equals("已结束") || status.equals("未开始")) {
					holder.et_autotext.setVisibility(View.GONE);
					holder.tvAnswer.setVisibility(View.VISIBLE);
					holder.tvAnswer.setText(question.getUsersAnswer());
				} else {
					holder.et_autotext.setVisibility(View.VISIBLE);
					boolean bflag=false;
					if(!question.isIfRead() && isEnable)
						bflag=true;
					holder.et_autotext.setEnabled(bflag);
					holder.tvAnswer.setVisibility(View.GONE);
					holder.et_autotext.setText(question.getUsersAnswer());

					ArrayAdapter<String> aa = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,question.getOptions());
					holder.et_autotext.setAdapter(aa);

					holder.et_autotext.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
							question.setUsersAnswer(holder.et_autotext.getText().toString());
							questions.set(position, question);
							if(question.getCallback().length()>0)
							{
								//startTimer(question, 300, null);
								String callback=question.getCallback()+"&"+question.getTitle()+"="+question.getUsersAnswer();
								sendCallBack(callback,4);
							}
						}
					});

				}
			}
			return convertView;
		}

		class ViewHolder {
			TextView title;
			EditText etAnswer;
			TextView remark;
			TextView tvAnswer;
			TextView tvRemark;
			RadioGroup radioGroup;
			NonScrollableListView multipleChoice;
			NonScrollableGridView imageGridView;
			Spinner sp_select;
			Spinner sp_select1;
			Spinner sp_select2;
			Button bt_date;
			Button bt_datetime;
			LinearLayout lv_layout;
			LinearLayout lv_parentlayout;
			AutoCompleteTextView et_autotext;
		}
		private class OnFocusChangeListenerImpl implements OnFocusChangeListener {
			private int position;
			public OnFocusChangeListenerImpl(int position) {
				this.position = position;
			}
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				EditText et = (EditText) arg0;
				Question question = (Question) getItem(position);
				if(arg1) {
					Log.d("", "获得焦点"+position);
					lastFocusEt=et;
				} else {
					Log.d("", "失去焦点"+position);
					String newtxt = et.getText().toString();
					if(et.getId()==R.id.et_answer || et.getId()==R.id.et_autotext ) {

						if(!question.getUsersAnswer().equals(newtxt))
						{

							question.setUsersAnswer(newtxt);
							if (question.getCallback().length()>0 ) {
								String callback=question.getCallback()+"&"+question.getTitle()+"="+question.getUsersAnswer();
								sendCallBack(callback,4);
								//et.setOnFocusChangeListener(null);

							}

						}
					}


				}
			}

		}
		private void setAllGone(ViewHolder holder)
		{
			holder.imageGridView.setVisibility(View.GONE);
			holder.radioGroup.setVisibility(View.GONE);
			holder.multipleChoice.setVisibility(View.GONE);
			holder.bt_date.setVisibility(View.GONE);
			holder.bt_datetime.setVisibility(View.GONE);
			holder.sp_select.setVisibility(View.GONE);
			holder.sp_select1.setVisibility(View.GONE);
			holder.sp_select2.setVisibility(View.GONE);
			holder.etAnswer.setVisibility(View.GONE);
			holder.tvAnswer.setVisibility(View.GONE);
			holder.et_autotext.setVisibility(View.GONE);
		}
	}

	private void reloadSpinner2(Spinner sp,Question question)
	{
		JSONArray subOptionsJson=question.getSubOptions().optJSONArray(question.getUsersAnswerOne());
		if(subOptionsJson!=null && subOptionsJson.length()>0)
		{
			String [] subOptions=new String[subOptionsJson.length()];
			int pos=0;
			for(int i=0;i<subOptionsJson.length();i++)
			{
				JSONObject item=null;
				item = subOptionsJson.optJSONObject(i);
				if(item!=null)
					subOptions[i]=item.optString("name");
				if(question.getUsersAnswer().equals(item.optString("id")))
					pos=i;
					
			}
			ArrayAdapter<String> bb = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,subOptions);
			sp.setAdapter(bb);
			sp.setSelection(pos);
		}
	}
	private void reloadThreeSpinner1(Spinner sp,Question question,Spinner sp2) {
		try
		{
			JSONObject subOptionsJson=question.getSubOptions().optJSONObject(question.getUsersAnswerOne());
			if(subOptionsJson!=null && subOptionsJson.length()>0)
			{
				String [] subOptions=new String[subOptionsJson.length()];
				Iterator<String> it = subOptionsJson.keys();
				int i=0;
				int pos2=0;
				JSONArray json3=null;
				while(it.hasNext()) {
					String key = it.next();
					json3=subOptionsJson.getJSONArray(key);
					subOptions[i]=key;
					if(question.getUsersAnswerTwo().equals(key)) {
						pos2 = i;
					}
					i++;
				}
				question.setUsersAnswerTwo(subOptions[pos2]);
				ArrayAdapter<String> bb = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,subOptions);
				sp.setAdapter(bb);
				sp.setSelection(pos2);
				reloadThreeSpinner2(sp2,question,subOptionsJson.getJSONArray(question.getUsersAnswerTwo()));
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	private void reloadThreeSpinner2(Spinner sp,Question question,JSONArray json3)  {
		if(json3!=null && json3.length()>0)
		{
			String [] subOptions=new String[json3.length()];
			int pos=0;
			for(int i=0;i<json3.length();i++)
			{
				JSONObject item=json3.optJSONObject(i);
				if(item!=null)
					subOptions[i]=item.optString("name");
				if(question.getUsersAnswer().equals(item.optString("id")))
					pos=i;

			}
			try {
				question.setUsersAnswer(json3.getJSONObject(pos).optString("id"));
			} catch (JSONException e) {
				e.printStackTrace();
			}
			ArrayAdapter<String> bb = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_spinner_dropdown_item,subOptions);
			sp.setAdapter(bb);
			sp.setSelection(pos);
		}
	}
	private void getFujian()
	{
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);  
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "请选择一个要上传的文件"), REQUEST_CODE_TAKE_DOCUMENT); 
	}
	private SimpleAdapter setupFujianAdpter(Question question)
	{
		final ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String,Object>>();  
		for(int i=0;i<question.getFujianArray().length();i++){  
        	JSONObject item = null;
			try {
				item = (JSONObject) question.getFujianArray().get(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(item!=null)
			{
	            HashMap<String, Object> tempHashMap = new HashMap<String, Object>();  
	            tempHashMap.put("name", item.optString("name"));
	            tempHashMap.put("url", item.optString("url"));
	            tempHashMap.put("newname", item.optString("newname"));
	            arrayList.add(tempHashMap);  
			}
              
        } 
		if(arrayList.size()<question.getLines())
		{
			HashMap<String, Object> tempHashMap = new HashMap<String, Object>();  
            tempHashMap.put("name", "添加附件");
            tempHashMap.put("url", "");
            arrayList.add(tempHashMap);  
		}
		SimpleAdapter fujianAdapter = new SimpleAdapter(getActivity(), arrayList, R.layout.list_item_simple,  
                new String[]{"name"}, new int[]{R.id.item_textView});
		return fujianAdapter;
	}
	private ListViewImageAdapter setupPopMutiAdpter(Question question)
	{
		final ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String,Object>>();
		for(int i=0;i<question.getFujianArray().length();i++){
			JSONObject item = null;
			try {
				item = (JSONObject) question.getFujianArray().get(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(item!=null)
			{
				HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
				tempHashMap.put("name", item.optString("name"));
				tempHashMap.put("icon",item.optString("icon") );
				tempHashMap.put("id", item.optString("id"));
				arrayList.add(tempHashMap);
			}

		}
		HashMap<String, Object> tempHashMap = new HashMap<String, Object>();
		tempHashMap.put("icon", "add");
		tempHashMap.put("name", "弹出多选");
		tempHashMap.put("id", "");
		arrayList.add(tempHashMap);

		ListViewImageAdapter fujianAdapter = new ListViewImageAdapter(getActivity(), arrayList);
		return fujianAdapter;
	}
	private ListOfBillAdapter setupPeiJianAdpter(Question question,int position)
	{
		final ArrayList<HashMap<String, Object>> arrayList = new ArrayList<HashMap<String,Object>>(); 
		double jine=0;
		for(int i=0;i<question.getFujianArray().length();i++){  
        	JSONObject item = null;
			try {
				item = (JSONObject) question.getFujianArray().get(i);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(item!=null)
			{
	            HashMap<String, Object> tempHashMap = new HashMap<String, Object>();  
	            tempHashMap.put("id", item.optInt("id"));
	            tempHashMap.put("name", item.optString("name"));
	            tempHashMap.put("price", item.optDouble("price"));
	            tempHashMap.put("num", item.optInt("num"));
	            tempHashMap.put("jine", item.optDouble("jine"));
	            arrayList.add(tempHashMap);  
	            jine+=item.optDouble("jine");
			}
              
        } 
		if(arrayList.size()<question.getLines())
		{
			HashMap<String, Object> tempHashMap = new HashMap<String, Object>();  
            tempHashMap.put("name", "添加一行");
            tempHashMap.put("id", 0);
            tempHashMap.put("jine", jine);
            arrayList.add(tempHashMap);  
		}
		ListOfBillAdapter billAdapter = new ListOfBillAdapter(getActivity(), arrayList,question,position);
		return billAdapter;
	}
	
	@SuppressWarnings("unused")
	private class CheckBoxAdapter extends BaseAdapter {

		private Context context;
		private String[] anwsers;
		private List<JSONObject> jsonanwsers;
		private String anwser;
		private int questionIndex;// question 在list中的下标
		private Question question;
		public Map<String, Boolean> isChecked = new HashMap<String, Boolean>();

		public CheckBoxAdapter(Context context, int questionIndex,
				Question question) {
			super();
			this.context = context;
			this.questionIndex = questionIndex;
			this.question = question;
			anwsers = question.getOptions();
			jsonanwsers=question.getOptionsJson();
			anwser = question.getUsersAnswer();
			initDate();
		}

		private void initDate() {

			String[] arr = anwser.split("@");
			List<String> list = Arrays.asList(arr);
			for (int i = 0; i < anwsers.length; i++) {
				if (list.contains(anwsers[i])) {
					isChecked.put(anwsers[i], true);
				} else {
					isChecked.put(anwsers[i], false);
				}
			}
			for(JSONObject item :jsonanwsers)
			{
				String key=item.optString("key");
				if (list.contains(key)) {
					isChecked.put(key, true);
				} else {
					isChecked.put(key, false);
				}
			}
		}

		public String getAnwser() {
			StringBuffer str = new StringBuffer();
			for (int i = 0; i < anwsers.length; i++) {
				if (isChecked.get(anwsers[i])) {
					str.append(anwsers[i]).append("@");
				}
			}
			for(JSONObject item :jsonanwsers)
			{
				String key=item.optString("key");
				if (isChecked.get(key)) {
					str.append(key).append("@");
				}
			}
			if (str.indexOf(",") > -1) {
				str.deleteCharAt(str.lastIndexOf("@"));
			}
			return str.toString();
		}

		public void setAnwser(String anwser) {
			this.anwser = anwser;
		}

		@Override
		public int getCount() {
			return (anwsers.length==0?jsonanwsers.size():anwsers.length);
		}

		@Override
		public Object getItem(int position) {
			return anwsers[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View view, ViewGroup parent) {
			view = inflater.inflate(R.layout.checkbox_item, parent, false);
			final CheckBox cb = (CheckBox) view.findViewById(R.id.cb_chenck);
			if(anwsers.length>0) {
				cb.setText(anwsers[position]);
				if (isChecked.get(anwsers[position])) {
					cb.setChecked(true);
				} else {
					cb.setChecked(false);
				}
			}
			else if(jsonanwsers.size()>0)
			{
				JSONObject item=jsonanwsers.get(position);
				cb.setText(item.optString("value"));
				String key=item.optString("key");
				if (isChecked.get(key)) {
					cb.setChecked(true);
				} else {
					cb.setChecked(false);
				}
			}
			boolean bflag=false;
			if(isEnable && !question.isIfRead())
				bflag=true;
			cb.setEnabled(bflag);
			cb.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {

					Boolean flag = cb.isChecked();
					if(anwsers.length>0)
						isChecked.put(anwsers[position], flag);
					else if(jsonanwsers.size()>0)
					{
						JSONObject item=jsonanwsers.get(position);
						String key=item.optString("key");
						isChecked.put(key, flag);
					}
					String answer = getAnwser();
					Log.d(TAG, "---------" + answer +question.getStatus()+"ss" +question.getTitle());
					question.setUsersAnswer(answer);
					questions.set(questionIndex, question);
					questionnaireList.setQuestions(questions);
				}
			});
			return view;
		}
	}
	private void getLocation()
	{
		Intent intent = new Intent(getActivity(), Alarmreceiver.class);
		intent.setAction("reportLocation");
		getActivity().sendBroadcast(intent);
	}
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
	{
		AppUtility.permissionResult(requestCode,grantResults,getActivity(),callBack);
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
	public CallBackInterface callBack=new CallBackInterface()
	{

		@Override
		public void getLocation1(int rqcode) {
			getLocation();
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
		public void sendCall1() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void sendMsg1() {
			// TODO Auto-generated method stub
			
		}
		@Override
		public void getFujian1() {
			getFujian();

		}

		
	};
	
	public void updateQuestions(Question question, int index) {
		questions.set(index, question);
		adapter.notifyDataSetChanged();
	}

	@Override
	public boolean onTouch(View view, MotionEvent motionEvent) {
		//触摸的是EditText并且当前EditText可以滚动则将事件交给EditText处理；否则将事件交由其父类处理
		if ((view.getId() == R.id.et_answer && canVerticalScroll((EditText)view))) {
			view.getParent().requestDisallowInterceptTouchEvent(true);
			if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
				view.getParent().requestDisallowInterceptTouchEvent(false);
			}
		}
		return false;
	}
	private boolean canVerticalScroll(EditText editText) {

			//滚动的距离
			int scrollY = editText.getScrollY();
			//控件内容的总高度
			int scrollRange = editText.getLayout().getHeight();
			//控件实际显示的高度
			int scrollExtent = editText.getHeight() - editText.getCompoundPaddingTop() - editText.getCompoundPaddingBottom();
			//控件内容总高度与实际显示高度的差值
			int scrollDifference = scrollRange - scrollExtent;

			if (scrollDifference == 0) {
				return false;
			}
			return (scrollY > 0) || (scrollY < scrollDifference - 1);

	}
	private void sendCallBack(String url,int what)
	{
		String checkCode = PrefUtility.get(Constants.PREF_CHECK_CODE, "");
		JSONObject queryJson=AppUtility.parseQueryStrToJson(url);
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put("用户较验码", checkCode);
			Iterator it = queryJson.keys();
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = queryJson.getString(key);
				jsonObj.put(key, value);
			}
			String linkindex=queryJson.optString("linkindex");
			if(linkindex!=null && linkindex.length()>0)
			{
				String[] indexarr=linkindex.split(",");
				for(String index : indexarr) {
					int i = Integer.parseInt(index)-1;
					if(i>=0)
						jsonObj.put(questions.get(i).getTitle(), questions.get(i).getUsersAnswer());
				}
			}

		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String preurl=AppUtility.removeURLQuery(url);
		if(preurl.length()==0)
			preurl=AppUtility.removeURLQuery(interfaceName);
		Log.d("timer3000",jsonObj.toString());
		CampusAPI.httpPost(preurl,jsonObj, mHandler, what);
	}
	private void setQuestionByJson(JSONObject subjo)
	{
		for(int i=0;i<questions.size();i++)
		{
			Question question=questions.get(i);
			if(question.getTitle().equals(subjo.optString("题目")))
			{
				Iterator<?> it = subjo.keys();
				while(it.hasNext()){//遍历JSONObject
					String key =  it.next().toString();
					String value=subjo.optString(key);
					if(key.equals("备注"))
						question.setRemark(value);
					else if(key.equals("备注颜色"))
						question.setRemardColor(value);
					else if(key.equals("只读"))
						question.setIfRead(subjo.optBoolean(key));
					else if(key.equals("用户答案"))
						question.setUsersAnswer(value);
					else if(key.equals("隐藏"))
						question.setIfHide(subjo.optBoolean(key));
					else if(key.equals("背景色"))
						question.setBackgroundcolor(subjo.optString(key));
					else if(key.equals("选项")) {
						String[] options = new String[0];
						ArrayList optionsJson = new ArrayList<JSONObject>();
						try {
							JSONArray josArr = subjo.optJSONArray(key);
							if (josArr != null) {
								for (int j = 0; j < josArr.length(); j++) {
									if (josArr.get(j) instanceof String) {
										if(j==0)
											options = new String[josArr.length()];
										options[j] = josArr.optString(j);
									}
									else if(josArr.get(j) instanceof JSONObject)
										optionsJson.add((JSONObject)josArr.get(j));
								}
							}
						} catch (JSONException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						question.setOptions(options);
						question.setOptionsJson(optionsJson);
					}
					else if(key.equals("类型"))
						question.setStatus(subjo.optString(key));
					else if(key.equals("回调"))
						question.setCallback(subjo.optString(key));
					else if(key.equals("是否必填"))
						question.setIsRequired(subjo.optString(key));

				}
				//questions.set(i,question);
				break;
			}
		}
	}
	private View.OnTouchListener touchListener= new View.OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.setFocusable(true);
			v.setFocusableInTouchMode(true);
			v.requestFocus();
			lastFocusEt=null;
			closeInputMethod(v);
			return false;
		}

	};
	private void closeInputMethod(View v) {
		InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
		boolean isOpen = imm.isActive();
		if (isOpen) {
			// imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);//没有显示则显示
			imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}
	private void popInputDelay(final EditText et)
	{
		new Handler().postDelayed(new Runnable(){
			@Override
			public void run(){
				et.requestFocus();
				et.setSelection(et.getText().length());
				//InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
				//imm.showSoftInput(et, InputMethodManager.SHOW_IMPLICIT);
			}
		},300);
	}
}
