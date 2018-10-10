package cn.ipanel.android.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.media.AudioManager;

public class SysUtils {
	public static String PROP_STB_ID = "ro.di.stb_id";
	public static String PROP_STB_SN = "ro.di.stb_sn";
	public static String PROP_STB_ID_2 = "ro.deviceinfo.stbid";
	
	public static String PROP_FACTORY = "ro.di.factory";
	public static String PROP_MANUFACTOR  = "ro.di.stb_manufacturer_id";
	public static String PROP_SERVICE_PHONE  = "ro.di.serviceline";
	public static String PROP_CHIP_COMPANY  = "ro.di.chipcompany";
	public static String PROP_SOFTWARE_DATE  ="ro.di.swdate";
	public static String PROP_HARDWARE_VER = "ro.di.hw_version";
	public static String PROP_SOFTWARE_VER = "ro.di.sw_version";
	public static String PROP_MAC = "ro.di.mac";
	public static String PROP_REGION_ID = "ro.di.region_id";
	public static String PROP_PRODUCTION_BATCH = "ro.di.production_batch";
	public static String PROP_MODEL = "ro.di.model";

	public static String getSystemProperty(String key) {
		try {
			Class<?> clazz = Class.forName("android.os.SystemProperties");
			Method get = clazz.getMethod("get", String.class);
			return (String) get.invoke(null, key);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static boolean isStreamMute(AudioManager am, int streamType){
		try {
			Method ismute = AudioManager.class.getMethod("isStreamMute", int.class);
			ismute.setAccessible(true);
			return (Boolean) ismute.invoke(am, streamType);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//fallback to check stream volume
		return am.getStreamVolume(streamType) == 0;
	}
}
