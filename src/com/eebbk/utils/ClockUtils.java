package com.eebbk.utils;

import android.content.ContentValues;
import android.media.MediaPlayer;

public class ClockUtils {
	//播放器单例化
	private static final MediaPlayer mediaplayer = new MediaPlayer();
	public static final MediaPlayer getMediaPlayer(){
		return mediaplayer;
	}
	
	//创建ContentValues实例
	private static ContentValues mConv;
	//int类型
	public static ContentValues getConvInt(String key,int value){
		if(mConv != null){
			mConv = null;
		}
		mConv = new ContentValues();
		mConv.put(key, value);
		return mConv;
	}
	//Long类型
	public static ContentValues getConvLong(String key,Long value){
		if(mConv != null){
			mConv = null;
		}
		mConv = new ContentValues();
		mConv.put(key, value);
		return mConv;
	}
	//String类型
		public static ContentValues getConvString(String key,String value){
			if(mConv != null){
				mConv = null;
			}
			mConv = new ContentValues();
			mConv.put(key, value);
			return mConv;
		}
	
	
	
}
