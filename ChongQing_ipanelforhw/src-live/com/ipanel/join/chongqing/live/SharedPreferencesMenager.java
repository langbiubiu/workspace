package com.ipanel.join.chongqing.live;

import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferencesMenager {

	public static final String CONFIG = "live_share";

	private SharedPreferences sp;
	private SharedPreferences.Editor editor;

	private static SharedPreferencesMenager mSharedPreferencesHelper;

	public static synchronized SharedPreferencesMenager getInstance(Context mContext) {
		if (mSharedPreferencesHelper == null) {
			mSharedPreferencesHelper = new SharedPreferencesMenager(mContext, CONFIG);
		}
		return mSharedPreferencesHelper;
	}

	private SharedPreferencesMenager(Context mContext, String name) {
		sp = mContext.getSharedPreferences(name, Context.MODE_WORLD_READABLE
				+ Context.MODE_WORLD_WRITEABLE+Context.MODE_MULTI_PROCESS);
		editor = sp.edit();
	}

	@SuppressWarnings("unchecked")
	public Map<String, String> getTableMap() {
		return (Map<String, String>) sp.getAll();
	}

	public void putValueString(String key, String value) {
		editor.putString(key, value);
	}

	public String getValueString(String key) {
		return sp.getString(key, null);
	}

	public void putValueInt(String key, int value) {
		editor.putInt(key, value);
	}

	public int getValueInt(String key) {
		return sp.getInt(key, -1);
	}

	public int getValueInt(String key, int def) {
		return sp.getInt(key, def);
	}

	public void remove(String key) {
		editor.remove(key);
	}

	public void saveData() {
		editor.commit();
	}

	public SharedPreferences getSharedPreferences() {
		return sp;
	}

	public void putValueFloat(String string, float value) {
		editor.putFloat(string, value);
	}

	public float getValueFloat(String key) {
		return sp.getFloat(key, 0);
	}

	public void putValueBoolean(String string, boolean value) {
		editor.putBoolean(string, value);
	}

	public boolean getValueBoolean(String key) {
		return sp.getBoolean(key, false);
	}

	public void putValueLong(String string, long value) {
		editor.putLong(string, value);
	}

	public long getValueLong(String key) {
		return sp.getLong(key, 0);
	}

	public void saveChannelData(String name,int channel, long freq, int prog,int tsid) {
		mSharedPreferencesHelper.putValueString("channel_name", name);
		mSharedPreferencesHelper.putValueInt("channel", channel);
		mSharedPreferencesHelper.putValueLong("Frequency", freq);
		mSharedPreferencesHelper.putValueInt("Program", prog);
		mSharedPreferencesHelper.putValueInt("tsid", tsid);
		mSharedPreferencesHelper.saveData();
	}

	public long getSaveFreq() {
		return mSharedPreferencesHelper.getValueLong("Frequency");
	}
	public String getSaveChannelName() {
		return mSharedPreferencesHelper.getValueString("channel_name");
	}
	public int getSaveProg() {
		return mSharedPreferencesHelper.getValueInt("Program");
	}
	public int getSaveTSID() {
		return mSharedPreferencesHelper.getValueInt("tsid");
	}
	public int getSaveChannel() {
		return mSharedPreferencesHelper.getValueInt("channel");
	}

	public void setSearchTime(String earchTime) {
		mSharedPreferencesHelper.putValueString("SearchTime", earchTime);
		mSharedPreferencesHelper.saveData();
	}

	public String getInSearchTime() {
		return mSharedPreferencesHelper.getValueString("SearchTime");
	}

	public void saveBouquetId(int bouquetId) {
		mSharedPreferencesHelper.putValueInt("BouquetId", bouquetId);
		mSharedPreferencesHelper.saveData();
	}

	public void saveSearchSetting(String setting) {
		mSharedPreferencesHelper.putValueString("SearchSetting", setting);
		mSharedPreferencesHelper.saveData();
	}

	public String getSearchSetting() {
		return mSharedPreferencesHelper.getValueString("SearchSetting");
	}

	public int getBouquetId() {
		return mSharedPreferencesHelper.getValueInt("BouquetId");
	}

	public void setTVMute(boolean mute) {
		mSharedPreferencesHelper.putValueBoolean("tv_mute", mute);
		mSharedPreferencesHelper.saveData();
	}
	
	public void setRunning(String key){
		mSharedPreferencesHelper.putValueString("running", key);
		mSharedPreferencesHelper.saveData();
	}

	public boolean getTVMute() {
		return mSharedPreferencesHelper.getValueBoolean("tv_mute");
	}

	public void saveVoluem(String channel, float value) {
		mSharedPreferencesHelper.putValueFloat("channel_volume", value);
		mSharedPreferencesHelper.saveData();
	}

	public float getVolume(String channel) {
		return mSharedPreferencesHelper.getValueFloat("channel_volume");
	}
}
