package com.eebbk.clockdemo;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {
	private Context mContext;
	private String time,tip,rate;//得到时间，提示信息，周期
	private View myView;
	private TextView mText;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;


		time = intent.getStringExtra("time");//得到闹铃的时间
		tip = intent.getStringExtra("tip");//得到闹铃的提示语
		rate  = intent.getStringExtra("rate");//得到闹铃的周期
		Intent i = new Intent(context, MusicService.class);
		i.putExtra("intent", 1);
		i.putExtra("rate", rate);
		i.putExtra("id", intent.getStringExtra("id"));
		i.putExtra("lazy", intent.getLongExtra("lazy", 5*60*1000l));

		context.startService(i);
		Log.i("aaa", "接受者收到意图了，启动了服务");

		myView = LayoutInflater.from(context).inflate(R.layout.dlg_alert, null);
		mText = (TextView) myView.findViewById(R.id.dlg_text_time);
		mText.setText(time);

		popAlertdialog(context);
	}

	//弹窗
	private void popAlertdialog(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(tip)
		.setView(myView)
		.setNegativeButton("停止闹钟", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent o = new Intent(mContext, MusicService.class);
				o.putExtra("intent", 2);
				mContext.startService(o);
				Toast.makeText(mContext, "停止闹钟", Toast.LENGTH_SHORT).show();

			}
		})
		.setPositiveButton("再睡一会", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent o = new Intent(mContext, MusicService.class);
				o.putExtra("intent", 3);
				mContext.startService(o);

			}
		});
		AlertDialog mAlert = builder.create();
		//指定全局，会在后台弹出
		mAlert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mAlert.setCanceledOnTouchOutside(false);
		mAlert.show();
		//设置全局弹窗需要一个权限
		//<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
	}

}
