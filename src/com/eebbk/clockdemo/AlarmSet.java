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
	//�ɱ��ַ�����
	private StringBuffer sb;
	//ʱ��ѡȡ
	private TimePickView mTpv;
	private Button mLazyArrow = null,mPeriodArrow = null,mRingArrow = null;
	private LinearLayout mLazy = null,mPeriod = null,mRing = null;
	private TextView mLazyText = null,mPeriodText = null,mRingText = null;
	private EditText mTipText = null;
	//ȡ���뱣�水ť�ؼ�
	private Button mCancel = null,mSave = null;
	//̰˯ʱ��ѡ������
	private int mLazySelect = 1;
	//����ѡ������
	private int mRingSelect = 3;
	//̰˯ʱ������
	private String[] mLazyStringArray = null;
	private long[] mLazyLongArray;
	//��������
	private String[] mRingArray = null;
	//��������
	private String[] mPeriodArrayChoice = null;
	private boolean[] mPeriodArrayChecked;
	private String what;//����������Ӻͱ༭����
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
		//ʱ��ѡ��ؼ�
		//mTimePicker  = (TimePicker) findViewById(R.id.alarm_timePicker);
		mTpv = (TimePickView) findViewById(R.id.tpv_timePicker);

		// �߻��ʡ�ѭ����ʾ����ת���򡢹رտ��ٻ�����������ʾ
		mTpv.setHighQuality(true);
		mTpv.setLoopShow(true);
		mTpv.setAntiDirect(true);
		mTpv.setFastFlip(false);
		mTpv.setAlignShow(true);

		mTpv.setRange(0, 23, TimePickView.DATA_TYPE_LEFT);
		mTpv.setRange(1, 59, TimePickView.DATA_TYPE_RIGHT);



		//ȡ���뱣��ؼ�
		mCancel = (Button) findViewById(R.id.alarm_cancel);
		mSave = (Button) findViewById(R.id.alarm_save);
		//���Բ��ֿؼ�
		mLazy = (LinearLayout) findViewById(R.id.alarm_lazy);
		mPeriod = (LinearLayout) findViewById(R.id.alarm_period);
		mRing = (LinearLayout) findViewById(R.id.alarm_ring);
		//��ͷ��ť�ؼ�
		mLazyArrow = (Button) findViewById(R.id.alarm_lazy_arrow);
		mPeriodArrow = (Button) findViewById(R.id.alarm_period_arrow);
		mRingArrow = (Button) findViewById(R.id.alarm_ring_arrow);
		//�ı���ʾ�ؼ�
		mTipText = (EditText) findViewById(R.id.edit_alarm_tip);
		mLazyText = (TextView) findViewById(R.id.alarm_lazy_set);
		mPeriodText = (TextView) findViewById(R.id.alarm_period_set);
		mRingText = (TextView) findViewById(R.id.alarm_ring_set);
		//̰˯�ַ��������ʼ��
		mLazyStringArray = new String[]{"3����","5����","10����","15����"};
		mLazyLongArray = new long[]{3 , 5 , 10 , 15};
		//̰˯�ַ��������ʼ��
		mRingArray = new String[]{"�Զ�������","����","default_alarm","Backroad","Jump_up"};
		//���������ʼ��
		mPeriodArrayChoice = new String[]{"��һ","�ܶ�","����","����","����","����","����"};
		mPeriodArrayChecked = new boolean[7];
		//���ü����¼�
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
		//�õ���ͼ�ĸ�����Ϣ
		Intent getI  = getIntent();
		what = getI.getStringExtra("what");
		if("save".equals(what)){
			Log.i("aaa", "����Ҫ�޸����е�����");
			String time = getI.getStringExtra("time");
			String tip = getI.getStringExtra("tip");
			long lazy = getI.getLongExtra("lazy", 0);
			String rate = getI.getStringExtra("rate");
			id  = getI.getIntExtra("id", -2);

			//����timepicker��ʱ��
			String[] times = time.split(":");
			mTpv.setTime(Integer.parseInt(times[0]), true, TimePickView.DATA_TYPE_LEFT);
			mTpv.setTime(Integer.parseInt(times[1]), true, TimePickView.DATA_TYPE_RIGHT);
			//mTimePicker.setCurrentHour(Integer.parseInt(times[0]));
			//mTimePicker.setCurrentMinute(Integer.parseInt(times[1]));
			//������
			mTipText.setText(tip);
			//����̰˯ʱ��
			if(lazy == 3){
				mLazyText.setText("3����");
			}else if(lazy == 5){
				mLazyText.setText("5����");
			}else if(lazy == 10){
				mLazyText.setText("10����");
			}else if(lazy == 15){
				mLazyText.setText("15����");
			}
			//��������
			mPeriodText.setText(rate);


		}else if("new".equals(what)){
			Calendar mcalendar = Calendar.getInstance();
			mcalendar.setTimeInMillis(System.currentTimeMillis());
			mTpv.setTime(mcalendar.get(Calendar.HOUR_OF_DAY), true, true, TimePickView.DATA_TYPE_LEFT);
			mTpv.setTime(mcalendar.get(Calendar.MINUTE), true, true, TimePickView.DATA_TYPE_RIGHT);
			Log.i("aaa", "����Ҫ���һ��������");
		}
	}



	//��������¼�
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.alarm_lazy:
		case R.id.alarm_lazy_arrow:
			AlertDialog.Builder builder1 = new Builder(this);
			builder1.setTitle("�ӻ���̰˯��ʱ��")
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
			builder2.setTitle("����")
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
						mPeriodText.setText("��һ��");
						sb = new StringBuffer();
					}else if(sb.length() == 21){
						mPeriodText.setText("ÿһ��");
					}else{
						mPeriodText.setText(sb.substring(0, sb.length()-1));
					}


				}
			}).create().show();
			break;
		case R.id.alarm_ring:
		case R.id.alarm_ring_arrow:
			AlertDialog.Builder builder3 = new Builder(this);
			builder3.setTitle("ѡ����������")
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
	//�����������õ����ݿ�
	private void saveAlarmSet() {
		//int currentHour = mTimePicker.getCurrentHour().intValue();
		//int currentMinute = mTimePicker.getCurrentMinute().intValue();
		int currentHour = mTpv.getTime(TimePickView.DATA_TYPE_LEFT);
		int currentMinute = mTpv.getTime(TimePickView.DATA_TYPE_RIGHT);
		ContentValues cv = new ContentValues();
		//��������ʱ��
		if(currentMinute<10){
			cv.put("time", String.valueOf(currentHour+":0"+currentMinute));
		}else{
			cv.put("time", String.valueOf(currentHour+":"+currentMinute));
		}

		//����������ʾ��Ϣ
		if(mTipText.getText().toString().isEmpty()){
			cv.put("tip", "����");
		}else{
			cv.put("tip", mTipText.getText().toString());
		}
		//����̰˯ʱ��
		cv.put("lazy", mLazyLongArray[mLazySelect]);
		//������������
		if(sb.toString().isEmpty()){
			cv.put("rate", "��һ��");
		}else if(sb.length() == 21){
			cv.put("rate", "ÿһ��");
		}else{
			cv.put("rate", sb.substring(0, sb.length()-1));
		}
		//����������Ϣ
		cv.put("ring", "");
		//�������ӿ���״̬
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
			Log.i("aaa", "����whileѭ��");
			while(aacursor.moveToNext()){
				String temp = aacursor.getString(aacursor.getColumnIndex(AlarmDatabase.ALARM_SAVEID));
				String dataa = temp.substring(0, temp.lastIndexOf("7"));
				Log.i("aaa", "ifִ����û��");
				if(dataa.equals(String.valueOf(id))){

					pi  = PendingIntent.getBroadcast(this,
							Integer.parseInt(temp), intent, 0);
					mAlarmManager.cancel(pi);
					Log.i("aaa", "�趨�������Ѿ�ȡ��");
					DatabaseManager.deleteDB(AlarmDatabase.TABLE_NAME1, AlarmDatabase.ALARM_SAVEID+"=?",
							new String[]{temp});

				}

			}


		}else if("new".equals(what)){
			data.putExtra("what", "new");
			DatabaseManager.insertDB(AlarmDatabase.TABLE_NAME, null, cv);
		}

		setResult(200, data);
		Log.i("aaa", "�趨������jieguo");

	}

	//�����ؼ������¼�
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK){
			AlertDialog.Builder buildera = new Builder(this);
			buildera.setTitle("��ܰ��ʾ")
			.setMessage("�����ѱ��޸ģ����Ƿ�Ҫ����")
			.setNegativeButton("��", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			})
			.setPositiveButton("��", new DialogInterface.OnClickListener() {

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
