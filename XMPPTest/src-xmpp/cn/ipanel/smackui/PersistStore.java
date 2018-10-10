package cn.ipanel.smackui;

import android.content.Context;
import android.content.SharedPreferences;

public class PersistStore {
	public static final String STORE_NAME = "UserStore";
	public static final String DEFAULT_HOST = "192.168.1.202";
	public static final String KEY_USER = "kUser";
	public static final String KEY_PWD = "kPassword";
	public static final String KEY_HOST = "kHost";
	
	public static String getUser(Context ctx){
		return getValue(ctx, KEY_USER, null);
	}
	
	public static void setUser(Context ctx, String user){
		setValue(ctx, KEY_USER,user);
	}
	
	public static String getUserPwd(Context ctx){
		return getValue(ctx, KEY_PWD, null);
	}
	
	public static void setUserPwd(Context ctx, String pwd){
		setValue(ctx, KEY_PWD,pwd);
	}
	
	public static String getHost(Context ctx){
		return getValue(ctx, KEY_HOST, DEFAULT_HOST);
	}
	
	public static void setHost(Context ctx, String host){
		setValue(ctx, KEY_HOST,host);
	}
	
	private static String getValue(Context ctx, String key, String def){
		SharedPreferences pref = ctx.getSharedPreferences(STORE_NAME, 0);
		return pref.getString(key, def);
	}
	private static void setValue(Context ctx, String key, String value){
		SharedPreferences pref = ctx.getSharedPreferences(STORE_NAME, 0);
		pref.edit().putString(key, value).commit();
	}
	
	public static boolean isRegistered(Context ctx, String user) {
		SharedPreferences pref = ctx.getSharedPreferences(STORE_NAME, 0);
		return pref.getString(KEY_USER, "").equals(user);
	}
}
