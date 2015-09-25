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
	private String time,tip,rate;//�õ�ʱ�䣬��ʾ��Ϣ������
	private View myView;
	private TextView mText;

	@Override
	public void onReceive(Context context, Intent intent) {
		mContext = context;


		time = intent.getStringExtra("time");//�õ������ʱ��
		tip = intent.getStringExtra("tip");//�õ��������ʾ��
		rate  = intent.getStringExtra("rate");//�õ����������
		Intent i = new Intent(context, MusicService.class);
		i.putExtra("intent", 1);
		i.putExtra("rate", rate);
		i.putExtra("id", intent.getStringExtra("id"));
		i.putExtra("lazy", intent.getLongExtra("lazy", 5*60*1000l));

		context.startService(i);
		Log.i("aaa", "�������յ���ͼ�ˣ������˷���");

		myView = LayoutInflater.from(context).inflate(R.layout.dlg_alert, null);
		mText = (TextView) myView.findViewById(R.id.dlg_text_time);
		mText.setText(time);

		popAlertdialog(context);
	}

	//����
	private void popAlertdialog(Context context) {
		AlertDialog.Builder builder = new Builder(context);
		builder.setTitle(tip)
		.setView(myView)
		.setNegativeButton("ֹͣ����", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent o = new Intent(mContext, MusicService.class);
				o.putExtra("intent", 2);
				mContext.startService(o);
				Toast.makeText(mContext, "ֹͣ����", Toast.LENGTH_SHORT).show();

			}
		})
		.setPositiveButton("��˯һ��", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent o = new Intent(mContext, MusicService.class);
				o.putExtra("intent", 3);
				mContext.startService(o);

			}
		});
		AlertDialog mAlert = builder.create();
		//ָ��ȫ�֣����ں�̨����
		mAlert.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
		mAlert.setCanceledOnTouchOutside(false);
		mAlert.show();
		//����ȫ�ֵ�����Ҫһ��Ȩ��
		//<uses-permission android:name="android.permission.SYSTEM_ALERT_WINDOW" />
	}

}
