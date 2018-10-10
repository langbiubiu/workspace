package com.ipanel.join.chongqing.live.manager.impl;

import ipaneltv.toolkit.entitlement.EntitlementDatabaseObjects.Entitlement;
import ipaneltv.toolkit.entitlement.EntitlementObserver;
import ipaneltv.toolkit.entitlement.EntitlementObserver.EntDataReadyLisentener;
import ipaneltv.toolkit.media.LiveCaSessionFragment;
import ipaneltv.toolkit.media.SettingsCaSessionFragment.Session;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import cn.ipanel.android.LogHelper;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.live.Constant;
import com.ipanel.join.chongqing.live.VODCACard;
import com.ipanel.join.chongqing.live.ca.LiveLiveCaFragment;
import com.ipanel.join.chongqing.live.ca.LiveLiveCaFragment.WasuLiveCaListener;
import com.ipanel.join.chongqing.live.ca.LiveSettingsCaFragment;
import com.ipanel.join.chongqing.live.ca.LiveSettingsCaFragment.WasuSettingsListener;
import com.ipanel.join.chongqing.live.data.CAMailData;
import com.ipanel.join.chongqing.live.data.OperatorData;
import com.ipanel.join.chongqing.live.manager.CAAuthManager;
import com.ipanel.join.chongqing.live.manager.IManager;

public class CAAuthManagerImpl extends CAAuthManager implements WasuSettingsListener,WasuLiveCaListener,EntDataReadyLisentener{

	public int moduleID = -1;
	public HashMap<String, String> mCAMap = new HashMap<String, String>();
	public final Handler mHandler = new Handler(Looper.getMainLooper()) ;

	Session settingSession = null;
	private IManager activity;
	protected CallBack mCallBack;
	private LiveSettingsCaFragment settings;
	private LiveLiveCaFragment liveFragmaent;
	private VODCACard card;
	Runnable mMailRunnable;
	public CAAuthManagerImpl(IManager context,LiveSettingsCaFragment settings,LiveLiveCaFragment liveFragmaent, CallBack callback) {
		this.activity = context;
		this.mCallBack = callback;
		settings.setListener(this);
		liveFragmaent.setLiveCaListener(this);
		this.settings=settings;
		this.liveFragmaent=liveFragmaent;
		EntitlementObserver	entObserver =new EntitlementObserver(activity.getContext());
		entObserver.prepare();
		entObserver.setDataReadyLisentener(this);
		card = new VODCACard();
		card.initCA(activity.getContext());
	}

	
	@Override
	public void onSessionReady(LiveCaSessionFragment.Session s) {
		// TODO Auto-generated method stub
		LogHelper.i("onSessionReady");
		s.queryNextScrollMessage();
		s.queryUnreadMailSize();
	}

	@Override
	public void onReadableEntries(HashMap<String, String> entries) {
		// TODO Auto-generated method stub
		LogHelper.i("onReadableEntries");

		if (entries != null) {
			mCAMap = entries;
			Set<String> set = entries.keySet();
			for (String string : set) {
				LogHelper.d("string " + string);
				LogHelper.d("entries.get(string) " + entries.get(string));
			}
		}
		settingSession.loosen(true);
//		settings.chooseSession(moduleID);
		liveFragmaent.chooseSession(moduleID);
		
		mCallBack.onCAInfoChnaged();
	}

	@Override
	public void onResponseUpdateSettings(String token, String err) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onResponseQuerySettings(String token, Bundle b) {
		// TODO Auto-generated method stub
		LogHelper.i("onResponseQuerySettings"+token);
		liveFragmaent.chooseSession(moduleID);
		try{
			JSONObject jo = new JSONObject(token);
			String action = jo.getString("action");
			if("ca_getMails".equals(action)){
				String params = jo.getString("param");
				
				JSONArray jarray = new JSONArray(params);
				int length = jarray.length();
				boolean hasNewMail = false;
				for(int i=0;i<length;i++){
					JSONObject mailjo = jarray.getJSONObject(i);
					if(mailjo.getInt("status") == 1){
						hasNewMail = true;
					}
				}
				mCallBack.onMailChanged(hasNewMail);
				
				activity.getUIManager().dispatchDataChange(Constant.DATA_CHANGE_OF_MAIL, params);
				
			}
			if("ca_getMailContent".equals(action)){
				String params = jo.getString("param");
				
				JSONArray joa = new JSONArray(params);
				JSONObject mail = joa.getJSONObject(0);
				activity.getUIManager().dispatchDataChange(Constant.DATA_CHANGE_OF_MAIL_CONTENT, mail.getString("detail"));
				
			}
			if("ca_deleteMail".equals(action) || "ca_deleteAllMail".equals(action)){
				
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
	}
	
	private void initMailData(String str){
		
		try {
//			JSONObject jo = new JSONObject(str);
			JSONArray jarray = new JSONArray(str);
			int length = jarray.length();
			for(int i=0;i<length;i++){
				CAMailData md = new CAMailData();
				JSONObject mailjo = jarray.getJSONObject(i);
				md.setNumber(mailjo.getInt("number"));
				md.setSendDate(mailjo.getString("sendDate"));
				md.setStatus(mailjo.getInt("status"));
				md.setTitle(mailjo.getString("title"));
				
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	@Override
	public void onScrollMessage(String msg) {
		// TODO Auto-generated method stub
		LogHelper.i("onScrollMessage"+msg);

		mCallBack.onScrollMessage(msg);
	}

	@Override
	public void onUrgencyMails(String token, Bundle b) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnreadMailSize(int size) {
		// TODO Auto-generated method stub
		mCallBack.onMailChanged(size > 0);
	}

	@Override
	public String getCAInfoByKey(int key) {
		// TODO Auto-generated method stub
		String result = "";
		switch (key) {
		case CA_INFO_FOR_CARDID:
			result = mCAMap.get("cardid");
			break;
		case CA_INFO_FOR_CA_VERSION:
			result = mCAMap.get("caversion");
			break;
		case CA_INFO_FOR_MODULE_VERSION:
			result = mCAMap.get("cosversion");
			break;
		case CA_INFO_FOR_CAS:
			result = mCAMap.get("caname");
			break;
		case CA_INFO_FOR_WATCH_LEVEL:
			result = mCAMap.get("rate");
			break;
		case CA_INFO_FOR_SERVICE_TIME:
			result = mCAMap.get("starttime") + "-" + mCAMap.get("endtime");
			break;
		case CA_INFO_FOR_PIN_STATE:
			result = "1".equals(mCAMap.get("pinlock")) ? "ÒÑËø¶¨" : "Î´Ëø¶¨";
			break;
		case CA_INFO_FOR_MATCH_STATE:
			int index = 0;
			if (mCAMap.get("pairstate") != null && !mCAMap.get("pairstate").equals("")) {
				index = Integer.parseInt(mCAMap.get("pairstate")) + 1;
			}
			String [] states = activity.getContext().getResources().getStringArray(R.array.ca_inf);
			result = states[index];
			break;
		}
		if (result == null) {
			result = "";
		}
		return result;
	}

	@Override
	public List<OperatorData> getOperatorDatas() {
		// TODO Auto-generated method stub
		List<OperatorData> datas = new ArrayList<OperatorData>();
		for (int i = 0; i < 13; i++) {
			OperatorData d = new OperatorData();
			d.setId(i + "");
			d.setInfo("name " + i);
			datas.add(d);
		}

		try {
			String str = mCAMap.get("operinfo");
			if (str == null || str.equals("")) {
				return datas;
			}
			JSONArray ja = new JSONArray(str);

			int lenght = ja.length();

			for (int i = 0; i < lenght; i++) {
				JSONObject json = ja.getJSONObject(i);
				OperatorData d = new OperatorData();
				d.setId(json.optString("opid"));
				d.setInfo(json.optString("opname"));
				datas.add(d);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		LogHelper.i("get opt data size :" + datas.size());
		return datas;
	}

	@Override
	public List<Entitlement> getAuthorDatas(String id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onSessionReady(Session s) {
		// TODO Auto-generated method stub
		LogHelper.i("onSessionReady");
		settingSession = s;
//		s.queryReadableEntries();
//		queryCAMail();
	}


	@Override
	public void updateCaModule(int moduleId) {
		// TODO Auto-generated method stub
		LogHelper.i("updateCaModule :"+moduleId);
		moduleID = moduleId;
//		if (settings != null) {
//			settings.chooseSession(moduleID);
//		}
//		queryCAMail();
		
		if(liveFragmaent!=null)
			liveFragmaent.chooseSession(moduleID);
		activity.getDataManager().caculateAuth();
	}

	@Override
	public int getCAModuleId() {
		// TODO Auto-generated method stub
		return moduleID;
	}
	/**
	 * 
	 * ²éÑ¯CAÓÊ¼þ
	 * */
	public void queryCAMail() {
		LogHelper.i("queryCAMail");
		if (settings != null) {
			settings.chooseSession(moduleID);
		}
		mHandler.removeCallbacks(mMailRunnable);
		mHandler.postDelayed(mMailRunnable=new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (settingSession != null) {
					settingSession.querySettings("ca_getMails", null);
				} else {
					LogHelper.i("session is null");
				}
			}
		}, 200);
	}
	
	/**
	 * 
	 * ·¢ËÍCAÃüÁî
	 * */
	public void queryMailContent(final int id) {
		LogHelper.i("queryCAMailContent mail_id:"+id);
		if (settings != null) {
			settings.chooseSession(moduleID);
		}
		mHandler.removeCallbacks(mMailRunnable);
		mHandler.postDelayed(mMailRunnable=new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (settingSession != null) {
					boolean b = settingSession.reserve();
					LogHelper.d("sess b= " + b);
					Bundle bundle = new Bundle();
					bundle.putString("EmailId", id+"");
					
					settingSession.querySettings("ca_getMailContent", bundle);
				} else {
					LogHelper.i("session is null");
				}
			}
		}, 200);
	}
	
	/**
	 * 
	 * ·¢ËÍCAÃüÁî
	 * */
	public void deleteCAMail(final int id) {
		LogHelper.i("deleteCAMail mail_id:"+id);
		if (settings != null) {
			settings.chooseSession(moduleID);
		}
		mHandler.removeCallbacks(mMailRunnable);
		mHandler.postDelayed(mMailRunnable=new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (settingSession != null) {
					boolean b = settingSession.reserve();
					LogHelper.d("sess b= " + b);
					Bundle bundle = new Bundle();
					bundle.putString("EmailId", id+"");
					
					settingSession.querySettings("ca_deleteMail", bundle);
				} else {
					LogHelper.i("session is null");
				}
				
				
			}
		}, 200);
	}
	
	public void deleteAllMail(){
		LogHelper.i("deleteAllMail");
		if (settings != null) {
			settings.chooseSession(moduleID);
		}
		mHandler.removeCallbacks(mMailRunnable);
		mHandler.postDelayed(mMailRunnable=new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				if (settingSession != null) {
					
					settingSession.querySettings("ca_deleteAllMail", null);
				} else {
					LogHelper.i("session is null");
				}
			}
		}, 200);
	}


	@Override
	public void onDataReady() {
		// TODO Auto-generated method stub
		activity.getDataManager().caculateAuth();
	}


	@Override
	public boolean isCAValid() {
		// TODO Auto-generated method stub
		if(Constant.DEVELOPER_MODE){
			return true;
		}
		return card.isCardValid();
	}


	@Override
	public void chooseSession() {
		// TODO Auto-generated method stub
		if (liveFragmaent != null) {
			liveFragmaent.chooseSession(moduleID);
		}
	}
}
