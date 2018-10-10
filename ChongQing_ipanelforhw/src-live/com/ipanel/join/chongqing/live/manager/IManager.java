package com.ipanel.join.chongqing.live.manager;

import android.content.Context;

public interface IManager {
	public UIManager getUIManager();

	public SettingManager getSettingManager();

	public StationManager getStationManager();

	public DataManager getDataManager();

	public BookManager getBookManager();

	public CAAuthManager getCAAuthManager();

	public ADManager getADManager();
	
	public Context getContext();
}
