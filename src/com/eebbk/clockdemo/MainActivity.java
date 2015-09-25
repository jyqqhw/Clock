package com.eebbk.clockdemo;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.CursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.eebbk.utils.DatabaseManager;

public class MainActivity extends Activity implements OnClickListener {
	//CursorAdapter
	private MyCursorAdapter mCursorAdapter = null;
	//闹钟管理器
	private AlarmManager mAlarmManager = null;
	//延时意图
	private PendingIntent pi = null;
	private Intent intent = null;

	private TextView mShowLatestAlarm;

	//刷新消息码
	private static final int REFRESH = 100;
	//子线程开关
	private Boolean mStopThread = true;
	//分针和秒针的时间基数
	private static final double BASE_ANGLE = 2*Math.PI/60;
	//三个图片控件
	private ImageView mHourPointer = null,mMinutePointer = null,mSecondPointer = null;
	//显示日期和时间的文本控件
	private TextView mTextDate = null,mTextTime = null;
	//秒针的初始、当前旋转度数和秒数
	private float second_initial = 0,second_current = 0,second_count = 0;
	//秒针的初始、当前旋转度数和秒数
	private float minute_initial = 0,minute_current = 0,minute_count = 0;
	//秒针的初始、当前旋转度数和秒数
	private float hour_initial = 0,hour_current = 0,hour_count = 0;
	//日历类变量，用来获取当前时间
	private Calendar mCalendar = null;
	//格式化时间
	private Date mDate = null;
	private SimpleDateFormat mFormatDate = null,mFormatTime = null,mFormatWeek = null,mFormatDetailTime = null;
	//当前时间的时分秒
	private int current_hour = 0,current_minute = 0,current_second = 0;
	//listview控件
	private ListView mListView = null;
	//添加闹钟按钮控件
	private Button mAddAlarm = null;
	private Handler handler = new Handler(){
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case REFRESH:
				doCurrentTime();

				showTextDateAndTime();

				doSecond();
				if(second_count == 59){
					second_count = 0;

					doMinute();
					if(minute_count/12 == 0&&second_count == 0){
						//minute_count = 0;
						doHour();
					}
				}
				break;
			case 200:
				mCursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME, null, null, null, null, null, AlarmDatabase.ALARM_TIME +" ASC");
				mCursorAdapter = new MyCursorAdapter(MainActivity.this, mCursor);
				mListView.setAdapter(mCursorAdapter);
				break;
			case 300:
				Cursor pCursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME, null, AlarmDatabase.ALARM_BUTTON_FLAG+"=?",
						new String[]{String.valueOf(1)}, null, null, null);
				if(pCursor.moveToLast()){
					mShowLatestAlarm.setText("已设置闹钟："+pCursor.getString(pCursor.getColumnIndex(AlarmDatabase.ALARM_TIME)));
				}
				break;
			default:
				break;
			}
		};
	};
	//数据库变量
	//private AlarmDatabase mDatabase;
	//private SQLiteDatabase mdbWriter;
	private Cursor mCursor;

	private Button mTestDatabase;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//模拟时钟显示控件
		mHourPointer = (ImageView) findViewById(R.id.clock_hour);
		mMinutePointer = (ImageView) findViewById(R.id.clock_minute);
		mSecondPointer = (ImageView) findViewById(R.id.clock_second);

		mTestDatabase = (Button) findViewById(R.id.test_check_database);
		mTestDatabase.setOnClickListener(this);

		//数字时钟显示控件
		mTextDate = (TextView) findViewById(R.id.text_date);
		mTextTime = (TextView) findViewById(R.id.text_time);

		mShowLatestAlarm = (TextView) findViewById(R.id.tv_show_alarm);

		//格式化时间
		mFormatDate = new SimpleDateFormat("yyyy-MM-dd  EE");
		mFormatTime = new SimpleDateFormat("HH:mm");
		mFormatDetailTime = new SimpleDateFormat("HH:mm:ss");
		mFormatWeek = new SimpleDateFormat("EE");
		//初始化闹钟功能
		initAlarmFunction();

		//获得日历类实例
		mCalendar = Calendar.getInstance();
		//UI更新线程
		new Thread(new updateUI()).start();

		//添加闹钟按钮控件
		mAddAlarm = (Button) findViewById(R.id.btn_new_alarm);
		mAddAlarm.setOnClickListener(this);


	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//更新时钟模块
		initClock();
		//更新已设置闹钟模块
		initAlarmList();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mStopThread = false;
	}

	/*-----------------------------------1.时钟显示模块------------------------------------------*/
	//初始化时钟显示模块
	private void initClock(){
		//获得当前时间
		doCurrentTime();

		//文本日期时间显示部分
		showTextDateAndTime();

		//秒针显示部分
		doSecond();

		//时针显示部分
		RotateAnimation ra_hour = new RotateAnimation((float) Math.toDegrees(0), 
				(float) Math.toDegrees(BASE_ANGLE*hour_count), 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		LinearInterpolator hour_lip = new LinearInterpolator();
		ra_hour.setInterpolator(hour_lip);
		ra_hour.setFillAfter(true);
		mHourPointer.startAnimation(ra_hour);

		//分针显示部分
		RotateAnimation ra_minute = new RotateAnimation((float) Math.toDegrees(0), 
				(float) Math.toDegrees(BASE_ANGLE*minute_count), 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		LinearInterpolator minute_lip = new LinearInterpolator();
		ra_minute.setInterpolator(minute_lip);
		ra_minute.setFillAfter(true);
		mMinutePointer.startAnimation(ra_minute);

	}

	//显示文本日期和时间
	private void showTextDateAndTime(){
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		mDate = mCalendar.getTime();
		mTextDate.setText(mFormatDate.format(mDate));
		mTextTime.setText(mFormatTime.format(mDate));
	}

	//秒针转动
	public void doSecond(){
		second_initial = (float) Math.toDegrees(BASE_ANGLE*second_count);
		second_current = (float) Math.toDegrees(BASE_ANGLE*(second_count+1));
		RotateAnimation ra_second = new RotateAnimation(second_initial, second_current, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		LinearInterpolator second_lip = new LinearInterpolator();
		ra_second.setInterpolator(second_lip);
		ra_second.setFillAfter(true);
		mSecondPointer.startAnimation(ra_second);
		//Log.i("aaa", String.valueOf(second_initial)+"<-second->"+String.valueOf(second_current));
	}

	//分针转动
	public void doMinute(){
		minute_initial = (float) Math.toDegrees(BASE_ANGLE*minute_count);
		minute_current = (float) Math.toDegrees(BASE_ANGLE*(minute_count+1));
		RotateAnimation ra_minute = new RotateAnimation(minute_initial, minute_current, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		LinearInterpolator minute_lip = new LinearInterpolator();
		ra_minute.setInterpolator(minute_lip);
		ra_minute.setFillAfter(true);
		mMinutePointer.startAnimation(ra_minute);
		//Log.i("aaa", String.valueOf(minute_initial)+"<-minute->"+String.valueOf(minute_current));
	}

	//时针转动
	public void doHour(){
		hour_initial = (float) Math.toDegrees(BASE_ANGLE*hour_count);
		hour_current = (float) Math.toDegrees(BASE_ANGLE*(hour_count+1));
		RotateAnimation ra_hour = new RotateAnimation(hour_initial, hour_current, 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		LinearInterpolator hour_lip = new LinearInterpolator();
		ra_hour.setInterpolator(hour_lip);
		ra_hour.setFillAfter(true);
		mHourPointer.startAnimation(ra_hour);
		Log.i("aaa", String.valueOf(hour_count));
		//Log.i("aaa", String.valueOf(hour_initial)+"<-hour->"+String.valueOf(hour_current));
	}

	//获取当前时间
	private void doCurrentTime(){
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		current_hour = mCalendar.get(mCalendar.HOUR);
		current_minute = mCalendar.get(mCalendar.MINUTE);
		current_second = mCalendar.get(mCalendar.SECOND);
		hour_count = (float) (current_hour*5+Math.floor(current_minute/12));
		minute_count = current_minute;
		second_count = current_second;
		//Log.i("aaa", "当前时间为->"+current_hour+":"+current_minute+":"+current_second);
	}


	//更新UI的Runable类
	class updateUI implements Runnable{

		@Override
		public void run() {
			while(mStopThread){
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				handler.sendEmptyMessage(REFRESH);
			}

		}

	}

	/*-------------------------------------时钟显示模块结束------------------------------------*/

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		//添加闹钟
		case R.id.btn_new_alarm:
			Intent toAlarmSet = new Intent(this, AlarmSet.class);
			toAlarmSet.putExtra("what", "new");
			startActivityForResult(toAlarmSet, 100);
			break;
		case R.id.test_check_database:
			Cursor testCursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME1, null, null, null, null, null, null);
			while(testCursor.moveToNext()){
				int temp = testCursor.getInt(testCursor.getColumnIndex(AlarmDatabase.ALARM_SAVEID));
				Log.i("aaa", String.valueOf(temp));
			}
			break;
		default:
			break;
		}

	}

	/*-------------------------------------2.已设置闹钟列表模块------------------------------------*/
	public void initAlarmList(){
		mListView = (ListView) findViewById(R.id.alarm_list_show);

		//创建数据库，得到cursor对象
		//mDatabase = new AlarmDatabase(this);
		//mdbWriter = mDatabase.getWritableDatabase();
		DatabaseManager.getInstance(this);
		mCursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME, null, null, null, null, null, AlarmDatabase.ALARM_TIME +" ASC");
		mCursorAdapter = new MyCursorAdapter(this, mCursor);
		mListView.setAdapter(mCursorAdapter);

	}

	//游标适配器
	private class MyCursorAdapter extends CursorAdapter{

		private LayoutInflater mLayoutInflater = null;
		private Context context;
		public MyCursorAdapter(Context context, Cursor c) {
			super(context, c);
			this.context = context;
			mLayoutInflater = LayoutInflater.from(context);
		}

		@Override
		public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
			ViewHolder mHolder = new ViewHolder();
			View view = mLayoutInflater.inflate(R.layout.item_alarm, null);

			mHolder.imageLogo = (Button) view.findViewById(R.id.list_alarm_logo);
			mHolder.textTime = (TextView) view.findViewById(R.id.list_alarm_time);
			mHolder.textTip = (TextView) view.findViewById(R.id.list_alarm_tip);
			mHolder.textRate = (TextView) view.findViewById(R.id.list_alarm_rate);
			mHolder.btnSwitch = (ToggleButton) view.findViewById(R.id.list_alarm_switch);
			mHolder.btnDelete = (Button) view.findViewById(R.id.list_alarm_delete);
			view.setTag(mHolder);
			return view;
		}

		@Override
		public void bindView(View arg0, Context arg1, final Cursor arg2) {

			final ViewHolder holder = (ViewHolder) arg0.getTag();

			//从数据库中得到闹钟的各种设置信息
			final int id = arg2.getInt(arg2.getColumnIndex(AlarmDatabase.ALARM_ID));
			final String time = arg2.getString(arg2.getColumnIndex(AlarmDatabase.ALARM_TIME));
			final String tip = arg2.getString(arg2.getColumnIndex(AlarmDatabase.ALARM_TIP));
			final String rate = arg2.getString(arg2.getColumnIndex(AlarmDatabase.ALARM_RATE));
			final long lazy = arg2.getLong(arg2.getColumnIndex(AlarmDatabase.ALARM_LAZY));
			final int btnflag = arg2.getInt(arg2.getColumnIndex(AlarmDatabase.ALARM_BUTTON_FLAG));
			holder.textTime.setText(time);
			holder.textTip.setText(tip);
			holder.textRate.setText(rate);
			holder.btnDelete.setTag(id);
			holder.btnSwitch.setTag(id);
			if(btnflag == 1){
				holder.imageLogo.setBackgroundResource(R.drawable.ic_alarm_enabled);
				holder.btnSwitch.setChecked(true);
			}else if(btnflag == 0 ){
				holder.imageLogo.setBackgroundResource(R.drawable.ic_alarm_disabled);
				holder.btnSwitch.setChecked(false);
			}



			//删除闹钟监听事件
			holder.btnDelete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					final String id =v.getTag().toString();

					AlertDialog.Builder builder = new Builder(context);
					builder.setTitle("温馨提示")
					.setMessage("您确定要删除此闹钟吗？")
					.setNegativeButton("取消", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					})
					.setPositiveButton("确定", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							DatabaseManager.deleteDB(AlarmDatabase.TABLE_NAME, AlarmDatabase.ALARM_ID+"=?",
									new String[]{id});
							Log.i("aaa", "删除了一条闹钟设置数据");
							intent = new Intent("Alarm_Boot");
							Cursor aacursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME1, null, null, null, null, null, null);
							while(aacursor.moveToNext()){
								String temp = aacursor.getString(aacursor.getColumnIndex(AlarmDatabase.ALARM_SAVEID));
								String data = temp.substring(0, temp.lastIndexOf("7"));
								if(data.equals(id)){

									pi  = PendingIntent.getBroadcast(MainActivity.this,
											Integer.parseInt(temp), intent, 0);
									mAlarmManager.cancel(pi);

									DatabaseManager.deleteDB(AlarmDatabase.TABLE_NAME1, AlarmDatabase.ALARM_SAVEID+"=?",
											new String[]{temp});

								}

							}


							handler.sendEmptyMessage(200);
							dialog.dismiss();
						}
					})
					.create().show();

				}
			});

			//闹钟开关的开关监听事件
			holder.btnSwitch.setOnCheckedChangeListener(new android.widget.CompoundButton.OnCheckedChangeListener() {

				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					// TODO Auto-generated method stub
					String id = buttonView.getTag().toString();
					if(isChecked){
						holder.imageLogo.setBackgroundResource(R.drawable.ic_alarm_enabled);
						setAlarm(time,tip,rate,lazy,id);

						ContentValues cv = new ContentValues();
						cv.put(AlarmDatabase.ALARM_BUTTON_FLAG, 1);
						DatabaseManager.updateDB(AlarmDatabase.TABLE_NAME, cv, 
								AlarmDatabase.ALARM_ID+"=?", new String[]{id});
						handler.sendEmptyMessage(300);


					}else{
						holder.imageLogo.setBackgroundResource(R.drawable.ic_alarm_disabled);
						Log.i("aaa", "出问题了1");
						intent = new Intent("Alarm_Boot");
						//intent = new Intent("Alarm"+id);
						Cursor aacursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME1, null, null, null, null, null, null);
						while(aacursor.moveToNext()){
							String temp = aacursor.getString(aacursor.getColumnIndex(AlarmDatabase.ALARM_SAVEID));
							String data = temp.substring(0, temp.lastIndexOf("7"));
							if(data.equals(id)){
								pi  = PendingIntent.getBroadcast(MainActivity.this,
										Integer.parseInt(temp), intent, 0);
								mAlarmManager.cancel(pi);
							}

						}

					}


					ContentValues cv = new ContentValues();
					cv.put(AlarmDatabase.ALARM_BUTTON_FLAG, 0);
					DatabaseManager.updateDB(AlarmDatabase.TABLE_NAME, cv, 
							AlarmDatabase.ALARM_ID+"=?", new String[]{id});
					handler.sendEmptyMessage(300);

					Toast.makeText(MainActivity.this, "闹铃已取消", Toast.LENGTH_SHORT).show();

				}
			});

			//整个view的点击事件
			arg0.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent resetAlarm = new Intent(MainActivity.this,AlarmSet.class);
					resetAlarm.putExtra("what", "save");
					resetAlarm.putExtra("time", time);
					resetAlarm.putExtra("tip", tip);
					resetAlarm.putExtra("rate", rate);
					resetAlarm.putExtra("lazy", lazy);
					resetAlarm.putExtra("id", id);
					startActivityForResult(resetAlarm, 100);

				}
			});


		}		

	}


	//返回事件
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == 200){
			String myTime = data.getStringExtra("time");
			String myTip = data.getStringExtra("tip");
			String myRate = data.getStringExtra("rate");
			long myLazy = data.getLongExtra("lazy", -1);
			String what = data.getStringExtra("what");
			int myBtnFlag = data.getIntExtra("btnflag", -1);
			String myRing = data.getStringExtra("ring");
			ContentValues cv = new ContentValues();
			cv.put("time", myTime);
			cv.put("tip", myTip);
			cv.put("rate", myRate);
			cv.put("lazy", myLazy);
			cv.put("btnflag", myBtnFlag);
			cv.put("ring", myRing);

			if("save".equals(what)){
				int myID = data.getIntExtra("id", -2);

				setAlarm(myTime, myTip, myRate, myLazy, String.valueOf(myID));
			}else if("new".equals(what)){
				Cursor aCursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME, new String[]{"_id"}, 
						AlarmDatabase.ALARM_TIME+"=?", new String[]{myTime}, null, null, null);
				aCursor.moveToFirst();
				int aID = aCursor.getInt(aCursor.getColumnIndex(AlarmDatabase.ALARM_ID));
				setAlarm(myTime, myTip, myRate, myLazy, String.valueOf(aID));
				aCursor.close();

			}



		}

	}



	//ViewHolder
	static class ViewHolder{
		Button imageLogo;
		TextView textTime;
		TextView textTip;
		TextView textRate;
		ToggleButton btnSwitch;
		Button btnDelete;
	}


	/*-------------------------------------已设置闹钟列表模块结束------------------------------------*/



	/*-------------------------------------3.闹钟功能模块------------------------------------*/
	//初始化闹钟
	public void initAlarmFunction(){
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


	}
	//设置闹钟的方法
	private void setAlarm(String time,String tip,String rate,long lazy,String id) {
		intent = new Intent("Alarm_Boot");
		//intent = new Intent("Alarm"+id);
		intent.putExtra("lazy", lazy);//传递长整型的贪睡时间
		intent.putExtra("time", time);//传递字符串型的时间
		intent.putExtra("tip", tip);//传递字符串型的提示信息
		intent.putExtra("rate", rate);//传递字符串型的周期
		intent.putExtra("id", id);//传递字符串型的id
		//pi = PendingIntent.getBroadcast(this, Integer.parseInt(id), intent, 0);
		//PendingIntentManager.addRequestCode(id);
		String cd = mFormatDetailTime.format(mDate);  //当前字符串形式的时分秒
		long ct = mDate.getTime();//当前长整形毫秒数
		int[] mt = compareTime(time, cd);  //mt数组保存为时分秒的差值
		long cha = (mt[0]*3600+mt[1]*60+mt[2])*1000l;//闹钟的时间
		if(rate.equals("仅一次")){
			pi = PendingIntent.getBroadcast(this, Integer.parseInt(id+78), intent, 0);
			ContentValues cv = new ContentValues();
			cv.put(AlarmDatabase.ALARM_SAVEID, Integer.parseInt(id+78));
			DatabaseManager.insertDB(AlarmDatabase.TABLE_NAME1, null, cv);

			mAlarmManager.set(AlarmManager.RTC_WAKEUP, ct+cha,pi);
			Toast.makeText(this, "还有"+String.valueOf(mt[0])+"时"+String.valueOf(mt[1])+"分"+String.valueOf(mt[2])+"秒开始闹铃", Toast.LENGTH_SHORT).show();
			Log.i("aaa", "一次闹铃已设置");
		}else if(rate.equals("每一天")){
			pi = PendingIntent.getBroadcast(this, Integer.parseInt(id+79), intent, 0);
			ContentValues cv = new ContentValues();
			cv.put(AlarmDatabase.ALARM_SAVEID, Integer.parseInt(id+79));
			DatabaseManager.insertDB(AlarmDatabase.TABLE_NAME1, null, cv);
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ct+cha,24*60*60*1000,pi);
			Toast.makeText(this, "还有"+String.valueOf(mt[0])+"时"+String.valueOf(mt[1])+"分"+String.valueOf(mt[2])+"秒开始闹铃", Toast.LENGTH_SHORT).show();
			Log.i("aaa", "每天闹铃已设置");
		}else{
			String[] rates = rate.split(",");
			String cw = mFormatWeek.format(mDate);//当前星期
			int cwDigital = isWeek(cw);//当前数字星期

			//得到设置星期的数字数组，与当前数字星期比较，，，得到闹铃的所有时间差
			int setLength = rates.length;//设置的每周闹铃的天数
			int[] setDigital = new int[setLength];
			long[] timeDifference = new long[setLength];//时间差以秒为单位  时间差以秒为单位
			for(int i = 0;i<setLength;i++){
				setDigital[i] = isWeek(rates[i]);//得到设置星期的数字数组
				Log.i("aaa", String.valueOf(setDigital[i]));
				//设置的星期与当前星期比较，得到一个时间差
				int dayDifference = setDigital[i]-cwDigital; //天数的差异
				Log.i("aaa", "dayDifference的值为："+String.valueOf(dayDifference));
				if( dayDifference > 0 ){  //设定时间星期在当前时间星期之后
					timeDifference[i] = stringToTimeA(time) + 24*3600*dayDifference-stringToTimeB(cd);
				}else if( dayDifference == 0 ){  //设定时间星期与当前时间星期相等

					//此处又分为当前时间中设置时间在后与在前
					if(mt[3] == 1){  //在前
						timeDifference[i] = mt[0]*3600+mt[1]*60+mt[2];
					}else{  //相等或在后
						timeDifference[i] = (mt[0]*3600+mt[1]*60+mt[2])+6*24*3600;
					}

				}else if( dayDifference < 0 ){  //设定时间星期在当前时间星期之前
					timeDifference[i] = stringToTimeA(time) + 24*3600*(dayDifference + 7) - stringToTimeB(cd);
					//当前时间在本周的剩余时间                                                         
				}
				pi = PendingIntent.getBroadcast(this, Integer.parseInt(id+7+i), intent, 0);
				ContentValues cv = new ContentValues();
				cv.put(AlarmDatabase.ALARM_SAVEID, Integer.parseInt(id+7+i));
				DatabaseManager.insertDB(AlarmDatabase.TABLE_NAME1, null, cv);
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ct+timeDifference[i]*1000, 7*24*3600*1000, pi);
				Log.i("aaa", "mt的值为："+String.valueOf(mt[0])+"_"+String.valueOf(mt[1])+"_"+String.valueOf(mt[2])+"_"+String.valueOf(mt[3]));

				Log.i("aaa", "第"+i+"个Long值为："+String.valueOf(timeDifference[i]));

			}



			//timeDifference数组里面保存了当前时间到设置的星期里下一次闹铃的毫秒数,找到其中的最小值作为吐司

			long timeMin = findMinByArray(timeDifference);
			Log.i("aaa", "long数组中的最小值："+String.valueOf(timeMin));
			int tempDay = (int) (timeMin/3600/24);
			int tempHour = (int) ((timeMin-tempDay*24*3600)/3600);
			int tempMinute = (int) ((timeMin - tempHour*3600 - tempDay*24*3600)/60);
			int tempSecond = (int) (timeMin - tempDay*24*3600 - tempHour*3600 - tempMinute*60);

			if(tempDay == 0){
				Toast.makeText(this, "还有"+tempHour+"时"+tempMinute+"分"+tempSecond+"秒开始闹铃", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, "还有"+tempDay+"天"+tempHour+"时"+tempMinute+"分"+tempSecond+"秒开始闹铃", Toast.LENGTH_SHORT).show();
			}
			Log.i("aaa", "其他闹铃已设置");			

		}

		Log.i("aaa", "闹钟设置完成");	

	}

	//找到长整形数组中的最小值
	private long findMinByArray(long[] arr) {
		int length = arr.length;
		int count = 1;
		long temp = arr[0];

		while(count<length){
			if(arr[count]<temp){
				temp = arr[count];
			}
			count++;
		}

		return temp;
	}

	//由字符创星期得到一个代表星期的数字
	private int isWeek(String cw) {
		int week = 0;
		if(cw.equals("周一")){
			week = 1;
		}else if(cw.equals("周二")){
			week = 2;
		}else if(cw.equals("周三")){
			week = 3;
		}else if(cw.equals("周四")){
			week = 4;
		}else if(cw.equals("周五")){
			week = 5;
		}else if(cw.equals("周六")){
			week = 6;
		}else if(cw.equals("周日")){
			week = 7;
		}
		return week;
	}

	//比较设定时间和当前时间的大小,返回下一次闹铃的小时数和分钟数
	private int[] compareTime(String setTime,String otherTime){
		int remain = stringToTimeA(setTime) - stringToTimeB(otherTime);
		int rHour = 0;
		int rMinute = 0;
		int rSecond = 0;
		int setTimeIsAfter = 0;
		if(remain > 0){   //设定的时间在当前时间之后
			setTimeIsAfter = 1;
			rHour = remain/3600;
			rMinute = (remain-rHour*3600)/60;
			rSecond = remain-rHour*3600-rMinute*60-1;
		}else if(remain == 0){  //设定的时间等于当前时间
			setTimeIsAfter = 0;
			rHour = 23;
			rMinute = 59;
			rSecond = 59;
		}else{  //设定的时间在当前时间之前
			setTimeIsAfter = -1;
			rHour = (remain+24*60*60)/3600;
			rMinute = ((remain+24*60*60)-rHour*3600)/60;
			rSecond = (remain+24*60*60)-rHour*3600-rMinute*60-1;
		}
		int[] array = new int[4];
		array[0] = rHour;
		array[1] = rMinute;
		array[2] = rSecond;
		array[3] = setTimeIsAfter;
		return array;
	}
	//把形如17:29的时间全部转化为秒
	private int stringToTimeA(String setTime) {
		String[] sets = setTime.split(":");
		int setHour = Integer.parseInt(sets[0]);
		int setMinute = Integer.parseInt(sets[1]);

		int remain = setHour*3600+setMinute*60;
		return remain;
	}

	//把形如17:29:47的时间全部转化为秒
	private int stringToTimeB(String currentTime) {
		String[] others = currentTime.split(":");
		int otherHour = Integer.parseInt(others[0]);
		int otherMinute = Integer.parseInt(others[1]);
		int otherSecond = Integer.parseInt(others[2]);
		int remain = otherHour*3600+otherMinute*60+otherSecond;//时间差
		return remain;
	}

	//设置闹钟

	/*-------------------------------------闹钟功能模块结束------------------------------------*/

	class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

		}

	}


}
