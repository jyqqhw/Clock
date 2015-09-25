package com.eebbk.clockdemo;

import java.util.Calendar;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eebbk.timepickview.TimePickView;
import com.eebbk.utils.DatabaseManager;

public class AlarmSet extends Activity implements OnClickListener {
	//可变字符序列
	private StringBuffer sb;
	//时间选取
	private TimePickView mTpv;
	private Button mLazyArrow = null,mPeriodArrow = null,mRingArrow = null;
	private LinearLayout mLazy = null,mPeriod = null,mRing = null;
	private TextView mLazyText = null,mPeriodText = null,mRingText = null;
	private EditText mTipText = null;
	//取消与保存按钮控件
	private Button mCancel = null,mSave = null;
	//贪睡时间选择索引
	private int mLazySelect = 1;
	//铃声选择索引
	private int mRingSelect = 3;
	//贪睡时间数组
	private String[] mLazyStringArray = null;
	private long[] mLazyLongArray;
	//铃声数组
	private String[] mRingArray = null;
	//周期数组
	private String[] mPeriodArrayChoice = null;
	private boolean[] mPeriodArrayChecked;
	private String what;//区分添加闹钟和编辑闹钟
	private int id = -2;
	private PendingIntent pi;
	private AlarmManager mAlarmManager;
	private Intent intent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_alarm_set);

		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		sb = new StringBuffer();
		//时间选择控件
		//mTimePicker  = (TimePicker) findViewById(R.id.alarm_timePicker);
		mTpv = (TimePickView) findViewById(R.id.tpv_timePicker);

		// 高画质、循环显示、反转方向、关闭快速滑动、对齐显示
		mTpv.setHighQuality(true);
		mTpv.setLoopShow(true);
		mTpv.setAntiDirect(true);
		mTpv.setFastFlip(false);
		mTpv.setAlignShow(true);

		mTpv.setRange(0, 23, TimePickView.DATA_TYPE_LEFT);
		mTpv.setRange(1, 59, TimePickView.DATA_TYPE_RIGHT);



		//取消与保存控件
		mCancel = (Button) findViewById(R.id.alarm_cancel);
		mSave = (Button) findViewById(R.id.alarm_save);
		//线性布局控件
		mLazy = (LinearLayout) findViewById(R.id.alarm_lazy);
		mPeriod = (LinearLayout) findViewById(R.id.alarm_period);
		mRing = (LinearLayout) findViewById(R.id.alarm_ring);
		//箭头按钮控件
		mLazyArrow = (Button) findViewById(R.id.alarm_lazy_arrow);
		mPeriodArrow = (Button) findViewById(R.id.alarm_period_arrow);
		mRingArrow = (Button) findViewById(R.id.alarm_ring_arrow);
		//文本显示控件
		mTipText = (EditText) findViewById(R.id.edit_alarm_tip);
		mLazyText = (TextView) findViewById(R.id.alarm_lazy_set);
		mPeriodText = (TextView) findViewById(R.id.alarm_period_set);
		mRingText = (TextView) findViewById(R.id.alarm_ring_set);
		//贪睡字符串数组初始化
		mLazyStringArray = new String[]{"3分钟","5分钟","10分钟","15分钟"};
		mLazyLongArray = new long[]{3 , 5 , 10 , 15};
		//贪睡字符串数组初始化
		mRingArray = new String[]{"自定义铃声","静音","default_alarm","Backroad","Jump_up"};
		//周期数组初始化
		mPeriodArrayChoice = new String[]{"周一","周二","周三","周四","周五","周六","周日"};
		mPeriodArrayChecked = new boolean[7];
		//设置监听事件
		mCancel.setOnClickListener(this);
		mSave.setOnClickListener(this);

		mLazy.setOnClickListener(this);
		mPeriod.setOnClickListener(this);
		mRing.setOnClickListener(this);
		mLazyArrow.setOnClickListener(this);
		mPeriodArrow.setOnClickListener(this);
		mRingArrow.setOnClickListener(this);

		doActionByIntentExtra();
	}

	private void doActionByIntentExtra() {
		//得到意图的附加信息
		Intent getI  = getIntent();
		what = getI.getStringExtra("what");
		if("save".equals(what)){
			Log.i("aaa", "这是要修改已有的闹钟");
			String time = getI.getStringExtra("time");
			String tip = getI.getStringExtra("tip");
			long lazy = getI.getLongExtra("lazy", 0);
			String rate = getI.getStringExtra("rate");
			id  = getI.getIntExtra("id", -2);

			//设置timepicker的时间
			String[] times = time.split(":");
			mTpv.setTime(Integer.parseInt(times[0]), true, TimePickView.DATA_TYPE_LEFT);
			mTpv.setTime(Integer.parseInt(times[1]), true, TimePickView.DATA_TYPE_RIGHT);
			//mTimePicker.setCurrentHour(Integer.parseInt(times[0]));
			//mTimePicker.setCurrentMinute(Integer.parseInt(times[1]));
			//闹钟名
			mTipText.setText(tip);
			//闹钟贪睡时间
			if(lazy == 3){
				mLazyText.setText("3分钟");
			}else if(lazy == 5){
				mLazyText.setText("5分钟");
			}else if(lazy == 10){
				mLazyText.setText("10分钟");
			}else if(lazy == 15){
				mLazyText.setText("15分钟");
			}
			//闹钟周期
			mPeriodText.setText(rate);


		}else if("new".equals(what)){
			Calendar mcalendar = Calendar.getInstance();
			mcalendar.setTimeInMillis(System.currentTimeMillis());
			mTpv.setTime(mcalendar.get(Calendar.HOUR_OF_DAY), true, true, TimePickView.DATA_TYPE_LEFT);
			mTpv.setTime(mcalendar.get(Calendar.MINUTE), true, true, TimePickView.DATA_TYPE_RIGHT);
			Log.i("aaa", "这是要添加一条新闹钟");
		}
	}



	//处理监听事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.alarm_lazy:
		case R.id.alarm_lazy_arrow:
			AlertDialog.Builder builder1 = new Builder(this);
			builder1.setTitle("延缓（贪睡）时间")
			.setSingleChoiceItems(mLazyStringArray, mLazySelect, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mLazySelect = which;
					mLazyText.setText(mLazyStringArray[mLazySelect]);
				}
			}).create().show();
			break;
		case R.id.alarm_period:
		case R.id.alarm_period_arrow:
			AlertDialog.Builder builder2 = new Builder(this);
			builder2.setTitle("周期")
			.setMultiChoiceItems(mPeriodArrayChoice, mPeriodArrayChecked, 
					new DialogInterface.OnMultiChoiceClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which, boolean isChecked) {
					mPeriodArrayChecked[which] = isChecked;		

					sb = new StringBuffer();
					for(int i = 0;i<mPeriodArrayChecked.length;i++){
						if(mPeriodArrayChecked[i] == true){
							sb.append(mPeriodArrayChoice[i]+",");
						}
					}
					Log.i("aaa", sb.toString());
					if(sb.toString().isEmpty()){
						mPeriodText.setText("仅一次");
						sb = new StringBuffer();
					}else if(sb.length() == 21){
						mPeriodText.setText("每一天");
					}else{
						mPeriodText.setText(sb.substring(0, sb.length()-1));
					}


				}
			}).create().show();
			break;
		case R.id.alarm_ring:
		case R.id.alarm_ring_arrow:
			AlertDialog.Builder builder3 = new Builder(this);
			builder3.setTitle("选择闹钟铃声")
			.setSingleChoiceItems(mRingArray, mRingSelect, 
					new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					mRingSelect = which;
					mRingText.setText(mRingArray[mRingSelect]);

				}
			}).create().show();
			break;
		case R.id.alarm_cancel:
			finish();
			break;
		case R.id.alarm_save:
			saveAlarmSet();
			finish();
			break;
		default:
			break;
		}

	}
	//保存闹钟设置到数据库
	private void saveAlarmSet() {
		//int currentHour = mTimePicker.getCurrentHour().intValue();
		//int currentMinute = mTimePicker.getCurrentMinute().intValue();
		int currentHour = mTpv.getTime(TimePickView.DATA_TYPE_LEFT);
		int currentMinute = mTpv.getTime(TimePickView.DATA_TYPE_RIGHT);
		ContentValues cv = new ContentValues();
		//保存闹铃时间
		if(currentMinute<10){
			cv.put("time", String.valueOf(currentHour+":0"+currentMinute));
		}else{
			cv.put("time", String.valueOf(currentHour+":"+currentMinute));
		}

		//保存闹铃提示信息
		if(mTipText.getText().toString().isEmpty()){
			cv.put("tip", "闹钟");
		}else{
			cv.put("tip", mTipText.getText().toString());
		}
		//保存贪睡时间
		cv.put("lazy", mLazyLongArray[mLazySelect]);
		//保存闹铃周期
		if(sb.toString().isEmpty()){
			cv.put("rate", "仅一次");
		}else if(sb.length() == 21){
			cv.put("rate", "每一天");
		}else{
			cv.put("rate", sb.substring(0, sb.length()-1));
		}
		//保存铃声信息
		cv.put("ring", "");
		//设置闹钟开关状态
		cv.put("btnflag", 1);
		Intent data = new Intent();
		data.putExtra("time",cv.getAsString("time"));
		data.putExtra("tip",cv.getAsString("tip"));
		data.putExtra("rate",cv.getAsString("rate"));
		data.putExtra("lazy",cv.getAsLong("lazy"));
		data.putExtra("btnflag", cv.getAsInteger("btnflag"));
		data.putExtra("ring", cv.getAsString("ring"));
		if("save".equals(what)){
			data.putExtra("id", id);
			data.putExtra("what", "save");
			DatabaseManager.updateDB(AlarmDatabase.TABLE_NAME, cv, AlarmDatabase.ALARM_ID+"=?", new String[]{String.valueOf(id)});

			intent = new Intent("Alarm_Boot");
			Cursor aacursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME1, null, null, null, null, null, null);
			Log.i("aaa", "马上while循环");
			while(aacursor.moveToNext()){
				String temp = aacursor.getString(aacursor.getColumnIndex(AlarmDatabase.ALARM_SAVEID));
				String dataa = temp.substring(0, temp.lastIndexOf("7"));
				Log.i("aaa", "if执行了没有");
				if(dataa.equals(String.valueOf(id))){

					pi  = PendingIntent.getBroadcast(this,
							Integer.parseInt(temp), intent, 0);
					mAlarmManager.cancel(pi);
					Log.i("aaa", "设定的闹铃已经取消");
					DatabaseManager.deleteDB(AlarmDatabase.TABLE_NAME1, AlarmDatabase.ALARM_SAVEID+"=?",
							new String[]{temp});

				}

			}


		}else if("new".equals(what)){
			data.putExtra("what", "new");
			DatabaseManager.insertDB(AlarmDatabase.TABLE_NAME, null, cv);
		}

		setResult(200, data);
		Log.i("aaa", "设定的闹铃jieguo");

	}

	//物理返回键监听事件
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			AlertDialog.Builder buildera = new Builder(this);
			buildera.setTitle("温馨提示")
			.setMessage("闹钟已被修改，您是否要保存")
			.setNegativeButton("否", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			})
			.setPositiveButton("是", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					saveAlarmSet();
					finish();
				}
			}).create().show();
		}
		return super.onKeyDown(keyCode, event);
	}




}
