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
	//���ӹ�����
	private AlarmManager mAlarmManager = null;
	//��ʱ��ͼ
	private PendingIntent pi = null;
	private Intent intent = null;

	private TextView mShowLatestAlarm;

	//ˢ����Ϣ��
	private static final int REFRESH = 100;
	//���߳̿���
	private Boolean mStopThread = true;
	//����������ʱ�����
	private static final double BASE_ANGLE = 2*Math.PI/60;
	//����ͼƬ�ؼ�
	private ImageView mHourPointer = null,mMinutePointer = null,mSecondPointer = null;
	//��ʾ���ں�ʱ����ı��ؼ�
	private TextView mTextDate = null,mTextTime = null;
	//����ĳ�ʼ����ǰ��ת����������
	private float second_initial = 0,second_current = 0,second_count = 0;
	//����ĳ�ʼ����ǰ��ת����������
	private float minute_initial = 0,minute_current = 0,minute_count = 0;
	//����ĳ�ʼ����ǰ��ת����������
	private float hour_initial = 0,hour_current = 0,hour_count = 0;
	//�����������������ȡ��ǰʱ��
	private Calendar mCalendar = null;
	//��ʽ��ʱ��
	private Date mDate = null;
	private SimpleDateFormat mFormatDate = null,mFormatTime = null,mFormatWeek = null,mFormatDetailTime = null;
	//��ǰʱ���ʱ����
	private int current_hour = 0,current_minute = 0,current_second = 0;
	//listview�ؼ�
	private ListView mListView = null;
	//������Ӱ�ť�ؼ�
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
					mShowLatestAlarm.setText("���������ӣ�"+pCursor.getString(pCursor.getColumnIndex(AlarmDatabase.ALARM_TIME)));
				}
				break;
			default:
				break;
			}
		};
	};
	//���ݿ����
	//private AlarmDatabase mDatabase;
	//private SQLiteDatabase mdbWriter;
	private Cursor mCursor;

	private Button mTestDatabase;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//ģ��ʱ����ʾ�ؼ�
		mHourPointer = (ImageView) findViewById(R.id.clock_hour);
		mMinutePointer = (ImageView) findViewById(R.id.clock_minute);
		mSecondPointer = (ImageView) findViewById(R.id.clock_second);

		mTestDatabase = (Button) findViewById(R.id.test_check_database);
		mTestDatabase.setOnClickListener(this);

		//����ʱ����ʾ�ؼ�
		mTextDate = (TextView) findViewById(R.id.text_date);
		mTextTime = (TextView) findViewById(R.id.text_time);

		mShowLatestAlarm = (TextView) findViewById(R.id.tv_show_alarm);

		//��ʽ��ʱ��
		mFormatDate = new SimpleDateFormat("yyyy-MM-dd  EE");
		mFormatTime = new SimpleDateFormat("HH:mm");
		mFormatDetailTime = new SimpleDateFormat("HH:mm:ss");
		mFormatWeek = new SimpleDateFormat("EE");
		//��ʼ�����ӹ���
		initAlarmFunction();

		//���������ʵ��
		mCalendar = Calendar.getInstance();
		//UI�����߳�
		new Thread(new updateUI()).start();

		//������Ӱ�ť�ؼ�
		mAddAlarm = (Button) findViewById(R.id.btn_new_alarm);
		mAddAlarm.setOnClickListener(this);


	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		//����ʱ��ģ��
		initClock();
		//��������������ģ��
		initAlarmList();

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mStopThread = false;
	}

	/*-----------------------------------1.ʱ����ʾģ��------------------------------------------*/
	//��ʼ��ʱ����ʾģ��
	private void initClock(){
		//��õ�ǰʱ��
		doCurrentTime();

		//�ı�����ʱ����ʾ����
		showTextDateAndTime();

		//������ʾ����
		doSecond();

		//ʱ����ʾ����
		RotateAnimation ra_hour = new RotateAnimation((float) Math.toDegrees(0), 
				(float) Math.toDegrees(BASE_ANGLE*hour_count), 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		LinearInterpolator hour_lip = new LinearInterpolator();
		ra_hour.setInterpolator(hour_lip);
		ra_hour.setFillAfter(true);
		mHourPointer.startAnimation(ra_hour);

		//������ʾ����
		RotateAnimation ra_minute = new RotateAnimation((float) Math.toDegrees(0), 
				(float) Math.toDegrees(BASE_ANGLE*minute_count), 
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 1.0f);
		LinearInterpolator minute_lip = new LinearInterpolator();
		ra_minute.setInterpolator(minute_lip);
		ra_minute.setFillAfter(true);
		mMinutePointer.startAnimation(ra_minute);

	}

	//��ʾ�ı����ں�ʱ��
	private void showTextDateAndTime(){
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		mDate = mCalendar.getTime();
		mTextDate.setText(mFormatDate.format(mDate));
		mTextTime.setText(mFormatTime.format(mDate));
	}

	//����ת��
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

	//����ת��
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

	//ʱ��ת��
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

	//��ȡ��ǰʱ��
	private void doCurrentTime(){
		mCalendar.setTimeInMillis(System.currentTimeMillis());
		current_hour = mCalendar.get(mCalendar.HOUR);
		current_minute = mCalendar.get(mCalendar.MINUTE);
		current_second = mCalendar.get(mCalendar.SECOND);
		hour_count = (float) (current_hour*5+Math.floor(current_minute/12));
		minute_count = current_minute;
		second_count = current_second;
		//Log.i("aaa", "��ǰʱ��Ϊ->"+current_hour+":"+current_minute+":"+current_second);
	}


	//����UI��Runable��
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

	/*-------------------------------------ʱ����ʾģ�����------------------------------------*/

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		//�������
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

	/*-------------------------------------2.�����������б�ģ��------------------------------------*/
	public void initAlarmList(){
		mListView = (ListView) findViewById(R.id.alarm_list_show);

		//�������ݿ⣬�õ�cursor����
		//mDatabase = new AlarmDatabase(this);
		//mdbWriter = mDatabase.getWritableDatabase();
		DatabaseManager.getInstance(this);
		mCursor = DatabaseManager.queryDB(AlarmDatabase.TABLE_NAME, null, null, null, null, null, AlarmDatabase.ALARM_TIME +" ASC");
		mCursorAdapter = new MyCursorAdapter(this, mCursor);
		mListView.setAdapter(mCursorAdapter);

	}

	//�α�������
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

			//�����ݿ��еõ����ӵĸ���������Ϣ
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



			//ɾ�����Ӽ����¼�
			holder.btnDelete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					final String id =v.getTag().toString();

					AlertDialog.Builder builder = new Builder(context);
					builder.setTitle("��ܰ��ʾ")
					.setMessage("��ȷ��Ҫɾ����������")
					.setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					})
					.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							DatabaseManager.deleteDB(AlarmDatabase.TABLE_NAME, AlarmDatabase.ALARM_ID+"=?",
									new String[]{id});
							Log.i("aaa", "ɾ����һ��������������");
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

			//���ӿ��صĿ��ؼ����¼�
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
						Log.i("aaa", "��������1");
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

					Toast.makeText(MainActivity.this, "������ȡ��", Toast.LENGTH_SHORT).show();

				}
			});

			//����view�ĵ���¼�
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


	//�����¼�
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


	/*-------------------------------------�����������б�ģ�����------------------------------------*/



	/*-------------------------------------3.���ӹ���ģ��------------------------------------*/
	//��ʼ������
	public void initAlarmFunction(){
		mAlarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);


	}
	//�������ӵķ���
	private void setAlarm(String time,String tip,String rate,long lazy,String id) {
		intent = new Intent("Alarm_Boot");
		//intent = new Intent("Alarm"+id);
		intent.putExtra("lazy", lazy);//���ݳ����͵�̰˯ʱ��
		intent.putExtra("time", time);//�����ַ����͵�ʱ��
		intent.putExtra("tip", tip);//�����ַ����͵���ʾ��Ϣ
		intent.putExtra("rate", rate);//�����ַ����͵�����
		intent.putExtra("id", id);//�����ַ����͵�id
		//pi = PendingIntent.getBroadcast(this, Integer.parseInt(id), intent, 0);
		//PendingIntentManager.addRequestCode(id);
		String cd = mFormatDetailTime.format(mDate);  //��ǰ�ַ�����ʽ��ʱ����
		long ct = mDate.getTime();//��ǰ�����κ�����
		int[] mt = compareTime(time, cd);  //mt���鱣��Ϊʱ����Ĳ�ֵ
		long cha = (mt[0]*3600+mt[1]*60+mt[2])*1000l;//���ӵ�ʱ��
		if(rate.equals("��һ��")){
			pi = PendingIntent.getBroadcast(this, Integer.parseInt(id+78), intent, 0);
			ContentValues cv = new ContentValues();
			cv.put(AlarmDatabase.ALARM_SAVEID, Integer.parseInt(id+78));
			DatabaseManager.insertDB(AlarmDatabase.TABLE_NAME1, null, cv);

			mAlarmManager.set(AlarmManager.RTC_WAKEUP, ct+cha,pi);
			Toast.makeText(this, "����"+String.valueOf(mt[0])+"ʱ"+String.valueOf(mt[1])+"��"+String.valueOf(mt[2])+"�뿪ʼ����", Toast.LENGTH_SHORT).show();
			Log.i("aaa", "һ������������");
		}else if(rate.equals("ÿһ��")){
			pi = PendingIntent.getBroadcast(this, Integer.parseInt(id+79), intent, 0);
			ContentValues cv = new ContentValues();
			cv.put(AlarmDatabase.ALARM_SAVEID, Integer.parseInt(id+79));
			DatabaseManager.insertDB(AlarmDatabase.TABLE_NAME1, null, cv);
			mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ct+cha,24*60*60*1000,pi);
			Toast.makeText(this, "����"+String.valueOf(mt[0])+"ʱ"+String.valueOf(mt[1])+"��"+String.valueOf(mt[2])+"�뿪ʼ����", Toast.LENGTH_SHORT).show();
			Log.i("aaa", "ÿ������������");
		}else{
			String[] rates = rate.split(",");
			String cw = mFormatWeek.format(mDate);//��ǰ����
			int cwDigital = isWeek(cw);//��ǰ��������

			//�õ��������ڵ��������飬�뵱ǰ�������ڱȽϣ������õ����������ʱ���
			int setLength = rates.length;//���õ�ÿ�����������
			int[] setDigital = new int[setLength];
			long[] timeDifference = new long[setLength];//ʱ�������Ϊ��λ  ʱ�������Ϊ��λ
			for(int i = 0;i<setLength;i++){
				setDigital[i] = isWeek(rates[i]);//�õ��������ڵ���������
				Log.i("aaa", String.valueOf(setDigital[i]));
				//���õ������뵱ǰ���ڱȽϣ��õ�һ��ʱ���
				int dayDifference = setDigital[i]-cwDigital; //�����Ĳ���
				Log.i("aaa", "dayDifference��ֵΪ��"+String.valueOf(dayDifference));
				if( dayDifference > 0 ){  //�趨ʱ�������ڵ�ǰʱ������֮��
					timeDifference[i] = stringToTimeA(time) + 24*3600*dayDifference-stringToTimeB(cd);
				}else if( dayDifference == 0 ){  //�趨ʱ�������뵱ǰʱ���������

					//�˴��ַ�Ϊ��ǰʱ��������ʱ���ں�����ǰ
					if(mt[3] == 1){  //��ǰ
						timeDifference[i] = mt[0]*3600+mt[1]*60+mt[2];
					}else{  //��Ȼ��ں�
						timeDifference[i] = (mt[0]*3600+mt[1]*60+mt[2])+6*24*3600;
					}

				}else if( dayDifference < 0 ){  //�趨ʱ�������ڵ�ǰʱ������֮ǰ
					timeDifference[i] = stringToTimeA(time) + 24*3600*(dayDifference + 7) - stringToTimeB(cd);
					//��ǰʱ���ڱ��ܵ�ʣ��ʱ��                                                         
				}
				pi = PendingIntent.getBroadcast(this, Integer.parseInt(id+7+i), intent, 0);
				ContentValues cv = new ContentValues();
				cv.put(AlarmDatabase.ALARM_SAVEID, Integer.parseInt(id+7+i));
				DatabaseManager.insertDB(AlarmDatabase.TABLE_NAME1, null, cv);
				mAlarmManager.setRepeating(AlarmManager.RTC_WAKEUP, ct+timeDifference[i]*1000, 7*24*3600*1000, pi);
				Log.i("aaa", "mt��ֵΪ��"+String.valueOf(mt[0])+"_"+String.valueOf(mt[1])+"_"+String.valueOf(mt[2])+"_"+String.valueOf(mt[3]));

				Log.i("aaa", "��"+i+"��LongֵΪ��"+String.valueOf(timeDifference[i]));

			}



			//timeDifference�������汣���˵�ǰʱ�䵽���õ���������һ������ĺ�����,�ҵ����е���Сֵ��Ϊ��˾

			long timeMin = findMinByArray(timeDifference);
			Log.i("aaa", "long�����е���Сֵ��"+String.valueOf(timeMin));
			int tempDay = (int) (timeMin/3600/24);
			int tempHour = (int) ((timeMin-tempDay*24*3600)/3600);
			int tempMinute = (int) ((timeMin - tempHour*3600 - tempDay*24*3600)/60);
			int tempSecond = (int) (timeMin - tempDay*24*3600 - tempHour*3600 - tempMinute*60);

			if(tempDay == 0){
				Toast.makeText(this, "����"+tempHour+"ʱ"+tempMinute+"��"+tempSecond+"�뿪ʼ����", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(this, "����"+tempDay+"��"+tempHour+"ʱ"+tempMinute+"��"+tempSecond+"�뿪ʼ����", Toast.LENGTH_SHORT).show();
			}
			Log.i("aaa", "��������������");			

		}

		Log.i("aaa", "�����������");	

	}

	//�ҵ������������е���Сֵ
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

	//���ַ������ڵõ�һ���������ڵ�����
	private int isWeek(String cw) {
		int week = 0;
		if(cw.equals("��һ")){
			week = 1;
		}else if(cw.equals("�ܶ�")){
			week = 2;
		}else if(cw.equals("����")){
			week = 3;
		}else if(cw.equals("����")){
			week = 4;
		}else if(cw.equals("����")){
			week = 5;
		}else if(cw.equals("����")){
			week = 6;
		}else if(cw.equals("����")){
			week = 7;
		}
		return week;
	}

	//�Ƚ��趨ʱ��͵�ǰʱ��Ĵ�С,������һ�������Сʱ���ͷ�����
	private int[] compareTime(String setTime,String otherTime){
		int remain = stringToTimeA(setTime) - stringToTimeB(otherTime);
		int rHour = 0;
		int rMinute = 0;
		int rSecond = 0;
		int setTimeIsAfter = 0;
		if(remain > 0){   //�趨��ʱ���ڵ�ǰʱ��֮��
			setTimeIsAfter = 1;
			rHour = remain/3600;
			rMinute = (remain-rHour*3600)/60;
			rSecond = remain-rHour*3600-rMinute*60-1;
		}else if(remain == 0){  //�趨��ʱ����ڵ�ǰʱ��
			setTimeIsAfter = 0;
			rHour = 23;
			rMinute = 59;
			rSecond = 59;
		}else{  //�趨��ʱ���ڵ�ǰʱ��֮ǰ
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
	//������17:29��ʱ��ȫ��ת��Ϊ��
	private int stringToTimeA(String setTime) {
		String[] sets = setTime.split(":");
		int setHour = Integer.parseInt(sets[0]);
		int setMinute = Integer.parseInt(sets[1]);

		int remain = setHour*3600+setMinute*60;
		return remain;
	}

	//������17:29:47��ʱ��ȫ��ת��Ϊ��
	private int stringToTimeB(String currentTime) {
		String[] others = currentTime.split(":");
		int otherHour = Integer.parseInt(others[0]);
		int otherMinute = Integer.parseInt(others[1]);
		int otherSecond = Integer.parseInt(others[2]);
		int remain = otherHour*3600+otherMinute*60+otherSecond;//ʱ���
		return remain;
	}

	//��������

	/*-------------------------------------���ӹ���ģ�����------------------------------------*/

	class MyReceiver extends BroadcastReceiver{

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

		}

	}


}
