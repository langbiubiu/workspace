package ipaneltv.toolkit;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import android.text.format.Time;
import android.util.Log;

/**
 * 时间格式工具类
 */
public class TimerFormater {
	static final String TAG = "TimerFormater";
	public static SimpleDateFormat formatter_a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	public static SimpleDateFormat formatter_b = new SimpleDateFormat("yyyyMMddHHmmss");
	public static SimpleDateFormat formatter_c = new SimpleDateFormat("yyyy-MM-dd");
	public static SimpleDateFormat formatter_d = new SimpleDateFormat("MM-dd");
	public static SimpleDateFormat formatter_e = new SimpleDateFormat("yyyy年MM月dd日 HH:mm");
	public static SimpleDateFormat formatter_f = new SimpleDateFormat("MM月dd日");
	public static SimpleDateFormat formatter_g = new SimpleDateFormat("HH:mm:ss");
	public static SimpleDateFormat formatter_h = new SimpleDateFormat("HH:mm");
	public static SimpleDateFormat formatter_i = new SimpleDateFormat("HHmm");

	// UTC时间示例: 1985-04-12T23:20:50.52Z
	// GMT-8时间示例:1996-12-19T16:39:57-08:00
	public static long rfc3339tolong(String rfc3399) {
		Time t = new Time();
		String timezone = TimeZone.getDefault().getID();
		Log.d(TAG, "rfc3339tolong timezone = "+ timezone);
		boolean b = t.parse3339(rfc3399);
		return t.toMillis(b);
	}

	public static long rfc3339tolong2(String rfc3399) {
		Time t = new Time();
		boolean b = t.parse3339(rfc3399);
		t.switchTimezone("GMT+8");
		return t.toMillis(b);
	}

	@SuppressWarnings("deprecation")
	public static String longtorfc3999(long t) {
		Calendar r = Calendar.getInstance();
		r.setTimeInMillis(t);
		return r.getTime().toGMTString();
	}
	
	
	public static long formatDuration(long duration) {
		int flag = 1;
		long j = 0;
		String s = Long.toHexString(duration);
		int i = s.length();
		while (i > 0) {
			if (flag > 3600) {
				break;
			}
			String s1 = s.substring(i - 2 > 0 ? i - 2 : 0, i);
			j = j + Integer.parseInt(s1) * flag;
			i = i - 2;
			flag = flag * 60;
		}
		j = j * 1000;
		return j;
	}	

	public static String getTodayTime() {
		return formatter_a.format(new Date());
	}
	
	public static String getTimeShiftTimeString(Date date) {
		Date currentTime;
		if (date == null) {
			currentTime = new Date();
		} else {
			currentTime = date;
		}
		return formatter_b.format(currentTime);
	}

	public static String getTimeShiftTimeString(String start, String duration) {
		try {
			Date d = formatter_b.parse(start);
			Date end = new Date(d.getTime() + Integer.parseInt(duration) * 1000);
			return getTimeShiftTimeString(end);
		} catch (java.text.ParseException e) {
			e.printStackTrace();
		}
		return getTimeShiftTimeString(null);
	}
	
	public static String getTodayString() {
		return formatter_c.format(new Date());
	}
	
	public static String getDayTime(long time){
		return formatter_c.format(new Date(time));
	}
	
	public static String getTomorrowString() {
		Date d = new Date();
		Calendar c = Calendar.getInstance();
		c.setTime(d);
		c.add(Calendar.DAY_OF_MONTH, 1);
		return formatter_c.format(c.getTime());
	}

	public static String getDetailTime(String time) {
		return formatter_e.format(new Date(Long.parseLong(time)));
	}
	
	public static String getMonth(long time) {
		return formatter_f.format(new Date(time));
	}
	public static String getNowTime(long time) {
		return formatter_h.format(new Date(time));
	}

	public static String getMonth() {
		return formatter_f.format(new Date());
	}

	public static String getNowTime() {
		return formatter_h.format(new Date());
	}

	public static long[] getTodaySection() {
		long[] times = new long[3];
		times[0] = System.currentTimeMillis();
		times[1] = times[0] + 24 * 3600 * 1000;
		return times;
	}

	public static String getAMPM() {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		return c.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";
	}

	public static String getHourTime(String start, String duration) {
		long time = Long.parseLong(start) + Long.parseLong(duration);
		return formatter_h.format(new Date(time));
	}

	public static String getEventTime(String start, String duration) {
		long s = Long.parseLong(start);
		long e = Long.parseLong(start) + Long.parseLong(duration);
		return formatter_h.format(new Date(s)) + " - " + formatter_h.format(new Date(e));
	}

	public static String getEventTime(long s, long e) {
		return formatter_h.format(new Date(s)) + " - " + formatter_h.format(new Date(e));
	}

	public static String getHourTime(long start, long duration) {
		long time = start + duration;
		return formatter_h.format(new Date(time));
	}

	public static String getHourTime(String start) {
		long time = Long.parseLong(start);
		return formatter_h.format(new Date(time));
	}

	public static String getHourTime(long start) {
		return formatter_h.format(new Date(start));
	}
	
	public static String getDate1() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		String date = format.format(new Date(System.currentTimeMillis()));
		return date;
	}
	
	public static String getDate2() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
		long time = System.currentTimeMillis() - 7*24*60*60*1000;
		String date = format.format(new Date(time));
		return date;
	}
	
	public static String [] getTimeshiftTime(){
		String [] times=new String[2];
		times[0]=formatter_b.format(new Date(System.currentTimeMillis() + 24*60*60*1000));
		times[1]=formatter_b.format(new Date(System.currentTimeMillis() - 7*24*60*60*1000));
		return times;
	}

	public static String changeTime(String time) {
		return time.substring(0, 2) + " : " + time.substring(2, 4);
	}

	/** 判断有效时间 */
	public static boolean isTimeValidate(int year, int month, int day) {
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

	public static boolean isHourValidate(int hour, int minute) {
		return hour >= 0 && hour < 24 && minute >= 0 && minute < 60;
	}

	/** 是否是闰年 */
	public static boolean isLeapYear(int year) {
		return ((year % 4 == 0 && year % 100 != 0) || year % 400 == 0);
	}

	public static int getDayValue(int year, int month) {
		int[] monthLengths = new int[] { 0, 31, -1, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31 };
		if (isLeapYear(year)) {
			monthLengths[2] = 29;
		} else {
			monthLengths[2] = 28;
		}
		return monthLengths[month];
	}

	public static String getSystemTime() {
		long times = System.currentTimeMillis();
		String date = formatter_i.format(times);
		return date;
	}
}
