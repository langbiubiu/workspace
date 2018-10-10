package com.ipanel.join.cq.back;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUitl {
	
	public static SimpleDateFormat formatter_d = new SimpleDateFormat(
			"yyyyMMddHHmmss");
	public static SimpleDateFormat formatter_t = new SimpleDateFormat(
			"HH:mm");
	public static String getWeekDay(Calendar cal) {
		switch (cal.get(Calendar.DAY_OF_WEEK)) {
		case Calendar.SUNDAY:
			return "周日";
		case Calendar.MONDAY:
			return "周一";
		case Calendar.TUESDAY:
			return "周二";
		case Calendar.WEDNESDAY:
			return "周三";
		case Calendar.THURSDAY:
			return "周四";
		case Calendar.FRIDAY:
			return "周五";
		case Calendar.SATURDAY:
			return "周六";
		default:
			return "";
		}
	}
	
	public static String getDate(Calendar cal){
		return String.format("%02d-%02d", cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH));
	}
	
	public static String getYear(Calendar cal){
		return String.format("%04d-%02d-%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH));
	}
	
	public static String getYear2(Calendar cal){
		return String.format("%04d%02d%02d", cal.get(Calendar.YEAR),cal.get(Calendar.MONTH) + 1,
				cal.get(Calendar.DAY_OF_MONTH));
	}
	
	public static String getDateTime_d(long second)
	{
		long t = second*1000;
		Date d=new Date(t);
		return formatter_d.format(d);
	}
	
	public static String getDateTime_t(long second)
	{
		long t = second*1000;
		Date d=new Date(t);
		return formatter_t.format(d);
	}
}
