package com.eebbk.utils;

import android.media.MediaPlayer;

public class MyMediaPlayer {
	private static final MediaPlayer mediaplayer = new MediaPlayer();;
	public static final MediaPlayer getMediaPlayer(){
		return mediaplayer;
	}
	
}
