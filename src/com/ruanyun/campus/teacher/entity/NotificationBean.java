package com.ruanyun.campus.teacher.entity;

import com.ruanyun.campus.teacher.activity.TabSchoolActivtiy;
import com.ruanyun.campus.teacher.util.IntentUtility;
import com.ruanyun.campus.teacher.R;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

public class NotificationBean extends Notification {
	private Context mContext;

	@SuppressWarnings("deprecation")
	public NotificationBean(Context context, int icon, CharSequence tickerText,String fileName,
			long when,String filePath) {
		super(icon, tickerText, when);
		this.mContext = context;
		this.flags = Notification.FLAG_AUTO_CANCEL; // |=
		// this.flags = Notification.FLAG_ONGOING_EVENT;

		RemoteViews mRemoteView = new RemoteViews(mContext.getPackageName(),
				R.layout.remot_view);
		this.contentView = mRemoteView;
		contentView.setTextViewText(R.id.file_name, fileName);
		
		Intent intent;
		if(filePath==null)
		{
			intent = new Intent(mContext, TabSchoolActivtiy.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		}
		else
		{
			intent=IntentUtility.openUrl(filePath);
        	//IntentUtility.openIntent(context, intent);
		}
		
		PendingIntent pIntent = PendingIntent.getActivity(mContext, 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		this.contentIntent = pIntent;
	}
}
