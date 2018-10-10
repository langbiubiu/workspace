package com.ipanel.join.cq.homed.tvcloud;

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
			return "星期日";
		case Calendar.MONDAY:
			return "星期一";
		case Calendar.TUESDAY:
			return "星期二";
		case Calendar.WEDNESDAY:
			return "星期三";
		case Calendar.THURSDAY:
			return "星期四";
		case Calendar.FRIDAY:
			return "星期五";
		case Calendar.SATURDAY:
			return "星期六";
		default:
			return "";
		}
	}
	
	public static String getDate(Calendar cal){
		return String.format("%02d月%02d日", cal.get(Calendar.MONTH) + 1,
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
