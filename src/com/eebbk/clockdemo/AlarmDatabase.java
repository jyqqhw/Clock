package com.eebbk.clockdemo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

public class AlarmDatabase extends SQLiteOpenHelper {

	// 数据库版本号
	private static final int DATABASE_VERSION = 1;
	// 数据库名
	private static final String DATABASE_NAME = "AlarmInfo.db";
	private Context mContext;

	public static final String TABLE_NAME = "alarm";
	public static final String ALARM_ID = "_id";
	public static final String ALARM_TIME = "time";
	public static final String ALARM_TIP = "tip";
	public static final String ALARM_LAZY = "lazy";
	public static final String ALARM_RATE = "rate";
	public static final String ALARM_RING = "ring";
	public static final String ALARM_BUTTON_FLAG = "btnflag";

	public static final String ALARM_ID1 = "_id";
	public static final String ALARM_SAVEID = "saveid";
	public static final String TABLE_NAME1 = "alarmlist";

	public AlarmDatabase(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		mContext = context;
	}



	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME1+"("
				+ALARM_ID1+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ALARM_SAVEID +" TEXT)");
		Toast.makeText(mContext, "创建已保存闹钟数据库表成功", Toast.LENGTH_SHORT).show();

		db.execSQL("CREATE TABLE IF NOT EXISTS "+TABLE_NAME+"("
				+ALARM_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ALARM_TIME+" TEXT,"
				+ALARM_TIP+" TEXT,"
				+ALARM_LAZY+" LONG,"
				+ALARM_RATE+" TEXT,"
				+ALARM_RING+" TEXT,"
				+ALARM_BUTTON_FLAG +" INTEGER)");
		Toast.makeText(mContext, "创建闹钟数据库表成功", Toast.LENGTH_SHORT).show();

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
