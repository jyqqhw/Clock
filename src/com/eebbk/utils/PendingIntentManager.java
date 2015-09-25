package com.eebbk.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class PendingIntentManager implements Serializable{
	
	private static List<PendingIntent> mPIManager = new ArrayList<PendingIntent>();
	private static List<String> mRequestCodeManager = new ArrayList<String>();
	private AlarmManager mAlarmManager;
	
	//添加一个意图ID到意图列表
	public static void addRequestCode(String requestCode){
		mRequestCodeManager.add(requestCode);
	}
	//寻找对应意图ID
	public static String findPendingIntent(String requestCode){
		String aim = mRequestCodeManager.get(mRequestCodeManager.indexOf(requestCode));
		return aim;
	}
	//从已添加意图列表中移除对应的意图
	public static void removeOnePendingIntent(String requestCode){
		mRequestCodeManager.remove(requestCode);
	}
	
	public static void cancelOnePendingIntent(Context context,String id){
		Intent i = new Intent("Alarm_Boot");
		PendingIntent pi = PendingIntent.getBroadcast(context, Integer.parseInt(id), i, 0);
		
	}
	
	
}
