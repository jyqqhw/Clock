package com.eebbk.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;



public class FormatDate {
	public static final Date getDate(String time){
		Date date = null;
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		try {
		date = format.parse(time);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return date;
	}
	
	public static final String getCurrentDate(long time){
		SimpleDateFormat format = new SimpleDateFormat("HH:mm");
		Date date = new Date(time);
		return format.format(date);
	}
	
	
}
