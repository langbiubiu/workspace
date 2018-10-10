package com.ipanel.chongqing_ipanelforhw;

import ipaneltv.uuids.ChongqingUUIDs;
import cn.ipanel.android.LogHelper;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

public class Config {
	public static String UUID = ChongqingUUIDs.ID;
	public static boolean isTestData = false;
	public static String PLAY_SERVICE_NAME = "cn.ipanel.tvapps.network.NcPlayService";
	public static String SRC_SERVICE_NAME = "com.ipanel.apps.common.tsvodsrcservice";
	public static final String CAPTURE_IMAGE_NAME="capture";
	public static final String CAPTURE_TMP_URL=Environment.getExternalStorageDirectory().toString()+"";
	
	private static final String URL_KEY = "home-url";
	private static final String VOD_HD_URL_KEY = "vod-hd-url";
	private static final String VOD_HUAWEI_URL_KEY = "vod-huawei-url";

	private static final String PREF_NAME = "config";
	/**
	 * 
	 * 重网地址
	 */
	private static final String HD_URL = "http://192.168.9.124/Android/home/vod_hd/vod_hd.xml";
	
	/**
	 * 
	 * 重网地址
	 */
	private static final String HUAWEI_URL = "http://192.168.45.65/Android/home/vod-huawei-config.xml";
	
	public static void setHomeUrl(Context context, String url){
		context.getSharedPreferences(PREF_NAME, 0).edit().putString(VOD_HD_URL_KEY, url).commit();
	}
	
	public static String getHDUrl(Context context){
		return context.getSharedPreferences(PREF_NAME, 0).getString(VOD_HD_URL_KEY, HD_URL);
	}
	
	public static String getHuaWeiUrl(Context context){
		return context.getSharedPreferences(PREF_NAME, 0).getString(VOD_HUAWEI_URL_KEY, HUAWEI_URL);
	}
	
	public static String getNetYype(Context context) {
		String result = "";
		try {
			Uri uri = Uri.parse("content://ipaneltv.chongqing.settings/net_type");
			Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
			while (cursor.moveToNext()) {
				result = cursor.getString(0);
				break;
			}
			cursor.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogHelper.i("get net type : " + result);
		return result;
	}
}
