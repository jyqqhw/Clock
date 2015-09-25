package com.eebbk.utils;

import android.content.ContentValues;
import android.media.MediaPlayer;

public class ClockUtils {
	//������������
	private static final MediaPlayer mediaplayer = new MediaPlayer();
	public static final MediaPlayer getMediaPlayer(){
		return mediaplayer;
	}
	
	//����ContentValuesʵ��
	private static ContentValues mConv;
	//int����
	public static ContentValues getConvInt(String key,int value){
		if(mConv != null){
			mConv = null;
		}
		mConv = new ContentValues();
		mConv.put(key, value);
		return mConv;
	}
	//Long����
	public static ContentValues getConvLong(String key,Long value){
		if(mConv != null){
			mConv = null;
		}
		mConv = new ContentValues();
		mConv.put(key, value);
		return mConv;
	}
	//String����
		public static ContentValues getConvString(String key,String value){
			if(mConv != null){
				mConv = null;
			}
			mConv = new ContentValues();
			mConv.put(key, value);
			return mConv;
		}
	
	
	
}
