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
			return "������";
		case Calendar.MONDAY:
			return "����һ";
		case Calendar.TUESDAY:
			return "���ڶ�";
		case Calendar.WEDNESDAY:
			return "������";
		case Calendar.THURSDAY:
			return "������";
		case Calendar.FRIDAY:
			return "������";
		case Calendar.SATURDAY:
			return "������";
		default:
			return "";
		}
	}
	
	public static String getDate(Calendar cal){
		return String.format("%02d��%02d��", cal.get(Calendar.MONTH) + 1,
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
