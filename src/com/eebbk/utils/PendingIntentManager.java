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
	
	//���һ����ͼID����ͼ�б�
	public static void addRequestCode(String requestCode){
		mRequestCodeManager.add(requestCode);
	}
	//Ѱ�Ҷ�Ӧ��ͼID
	public static String findPendingIntent(String requestCode){
		String aim = mRequestCodeManager.get(mRequestCodeManager.indexOf(requestCode));
		return aim;
	}
	//���������ͼ�б����Ƴ���Ӧ����ͼ
	public static void removeOnePendingIntent(String requestCode){
		mRequestCodeManager.remove(requestCode);
	}
	
	public static void cancelOnePendingIntent(Context context,String id){
		Intent i = new Intent("Alarm_Boot");
		PendingIntent pi = PendingIntent.getBroadcast(context, Integer.parseInt(id), i, 0);
		
	}
	
	
}
