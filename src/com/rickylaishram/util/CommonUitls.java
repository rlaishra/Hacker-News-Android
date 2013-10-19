package com.rickylaishram.util;

import java.util.Vector;

import android.content.Context;
import android.content.SharedPreferences;

public class CommonUitls {
	
	public static final String SERVER_URL = "https://ricky-hnandroid.appspot.com";
    public static final String SENDER_ID = "";
    public static final String TAG = "Hacker News Android";
    public static final String AUTH_KEY = "";
	
	public static String getMonthNameShort(Integer pos) {
		Vector<String> monthsShort 	= new Vector<String>();
		monthsShort.add("Jan");
		monthsShort.add("Feb");
		monthsShort.add("Mar");
		monthsShort.add("Apr");
		monthsShort.add("May");
		monthsShort.add("Jun");
		monthsShort.add("Jul");
		monthsShort.add("Aug");
		monthsShort.add("Sep");
		monthsShort.add("Oct");
		monthsShort.add("Nov");
		monthsShort.add("Dec");
		
		return monthsShort.get(pos);
	}
	
	public static void setAnalytics(Context context, Boolean value) {
		context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
				.edit()
				.putBoolean("analytics", value)
				.commit();
	}
	
	public static Boolean agreesToAnalytics(Context context) {
		Boolean result	= true;
		
		try {
			SharedPreferences settings 	= context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        	result 		= settings.getBoolean("analytics", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	//adds 1 to the number of times app has been launched
	public static void appUsageNumber(Context context) {
		SharedPreferences settings 	= context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
    	Integer number 				= settings.getInt("usage_num", 0);
    	
    	settings.edit().putInt("usage_num", number+1).commit();
	}
	
	public static Integer getUsageNumber(Context context) {
		Integer value	= 0;
		
		try {
			SharedPreferences settings 	= context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        	value 		= settings.getInt("usage_num", 0);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return value;
	}
	
	public static void setRatingNotiFalse(Context context) {
		context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
				.edit()
				.putBoolean("rating_noti", false)
				.commit();
	}
	
	public static void setDonateNotiFalse(Context context) {
		context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
				.edit()
				.putBoolean("donate_noti", false)
				.commit();
	}
	
	public static Boolean getRatingNoti(Context context) {
		Boolean result	= true;
		
		try {
			SharedPreferences settings 	= context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        	result 		= settings.getBoolean("rating_noti", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static Boolean getDonateNoti(Context context) {
		Boolean result	= true;
		
		try {
			SharedPreferences settings 	= context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
        	result 		= settings.getBoolean("donate_noti", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return result;
	}
}
