package ipaneltv.toolkit.http;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SaxHandlerUtil {
	public static SimpleDateFormat formatter_h = new SimpleDateFormat("HH:mm");
	
	public static int parseString2Int(String val){
		int value = 0;
		if(val == null || "".equals(val) ||"".equals(val.trim())){
			return -1;
		}
		if(val.startsWith("0x") || val.startsWith("0X") || val.startsWith("oX") || val.startsWith("OX")){
			value = Integer.parseInt(val.replaceAll("0[x|X]", ""), 16);
		}else{
			value = Integer.parseInt(val);
		}		
		return value;
	}
	
	public static long parseString2Long(String val){
		if(val == null || "".equals(val) || "".equals(val.trim())){
			return -1;
		}
		return Long.valueOf(val);
	}
	
	public long convertString2Date(String val){
		long time  =System.currentTimeMillis();
		try {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
			Date date = format.parse(val);			
			Calendar c = Calendar.getInstance();
			c.setTime(date);
			time = c.getTimeInMillis();
			return time;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return time;
	}
	public static String chang2UTCFormate(String time){
		if("".equals(time) || time == null){
			return "";
		}
		
		if(time.contains("T")){
			return time.replace(" ", "");
		}
		
		String[]splites = time.split(" +");
		if(splites != null && splites.length == 2){
			return  splites[0]+"T"+splites[1];
		}
		return  "";
	}
	
	public static String getHourTime(String start) {
		long time = Long.parseLong(start);
		return formatter_h.format(new Date(time));
	}

	public static String getHourTime(long start) {
		return formatter_h.format(new Date(start));
	}
}
