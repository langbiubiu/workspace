package com.ipanel.join.cq.homed.tvcloud;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeHelper {
	public static SimpleDateFormat formatter_h = new SimpleDateFormat("HH:mm");
	public static SimpleDateFormat formatter_f = new SimpleDateFormat("MM月dd日");
	public static SimpleDateFormat formatter_a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat formatter_c= new SimpleDateFormat("yyyyMMddHHmmss");
	public final static SimpleDateFormat format = new SimpleDateFormat("MM-dd");
	public static SimpleDateFormat formatter_s = new SimpleDateFormat("HH:mm:ss");

	public static int adds[] = { 0, 1, 2, 3, -3, -2, -1 };
	
	public static int nineadds[] = { -2, -1, 0, 1, 2, 3, 4, 5, 6 };

	public static String getMonth() {
		return formatter_f.format(new Date());
	}
	
	public static String getNowTime() {
		return formatter_h.format(new Date());
	}
	public static String getEventTime(long s, long e) {
		return formatter_h.format(new Date(s)) + " - " + formatter_h.format(new Date(e));
	}
	public static String getMonth(long time) {
		return formatter_f.format(new Date(time));
	}

	public static String getNowTime(long time) {
		return formatter_h.format(new Date(time));
	}
	
	public static String getStartModeShiftTime(long time){
		return formatter_c.format(new Date(time));
	}
//	public static int getCurretnEventProgress(ipaneltv.toolkit.db.DatabaseObjectification.Program p) {
//		float start=p.getStart();
//		float end=p.getEnd();
//		float now=System.currentTimeMillis();
//		return (int) ((now-start)/(end-start+1.0f)*1000f);
//	}

	public static String getHourTime(String start) {
		long time = Long.parseLong(start);
		return formatter_h.format(new Date(time));
	}

	public static String getHourTime(long start) {
		return formatter_h.format(new Date(start));
	}
	public static String getDetailTime(long time) {
		return formatter_a.format(new Date(time));
	}
	public static long[] getTodaySection() {
		long[] times = new long[3];
		times[0] = System.currentTimeMillis();
		times[1] = times[0] + 24 * 3600 * 1000;
		return times;
	}
	public static boolean isPlaying(long start,long end,long time){
		return start <= time && end >= time;
	}
	public static boolean isPlayed(long start,long end,long time){
		return start <= time;
	}
	/** 判断主方法 */
	public static boolean validate(int year, int month, int day) {
		if (month < 1 || month > 12) {
			return false;
		}
		int[] monthLengths = new int[] { 0, 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		if (isLeapYear(year)) {
			monthLengths[2] = 29;
		} else {
			monthLengths[2] = 28;
		}
		int monthLength = monthLengths[month];
		if (day < 1 || day > monthLength) {
			return false;
		}
		return true;
	}

	public static boolean validate(int hour, int minute) {
		return hour >= 0 && hour < 24 && minute >= 0 && minute < 60;
	}

	/** 是否是闰年 */
	private static boolean isLeapYear(int year) {
		return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
	}

	public static int getDayValue(int year, int month) {
		month = Math.min(12, month);
		int[] monthLengths = new int[] { 0, 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		if (isLeapYear(year)) {
			monthLengths[2] = 29;
		} else {
			monthLengths[2] = 28;
		}
		return monthLengths[month];
	}
	
	public static String changeOffsetToKey(int offset){
		Date date = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		c.add(Calendar.DAY_OF_MONTH, offset);
		return format.format(c.getTime());
	}
	
	public static String getTotalTime(long duration){
		duration=duration/1000;
		long hour1=duration/3600;
		long hour2=duration%3600;
		long minute1=hour2/60;
		long minute2=hour2%60;
		String result="";
		result+=hour1>9?hour1:"0"+hour1;
		result+=":";
		result+=minute1>9?minute1:"0"+minute1;
		result+=":";
		result+=minute2>9?minute2:"0"+minute2;
		return result;

	}
	
	public static String getShowEventTime(long time){
		return formatter_s.format(new Date(time));
	}

	public static SimpleDateFormat formatter_m = new SimpleDateFormat(
			"yyyyMMdd HH:mm");
	
	public static long getMillisecond(String time1, String time2) {
		try {
			Date d = formatter_m.parse(time1 + " " + time2);
			return d.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}
}
