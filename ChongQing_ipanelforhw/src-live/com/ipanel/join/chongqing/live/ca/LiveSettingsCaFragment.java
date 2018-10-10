package com.ipanel.join.chongqing.live.ca;

import ipaneltv.toolkit.media.SettingsCaSessionFragment;

import java.util.HashMap;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ipanel.join.chongqing.live.Constant;

public class LiveSettingsCaFragment extends SettingsCaSessionFragment {
	WasuSettingsListener listener;
	private Object mutex = new Object();

	public static LiveSettingsCaFragment createInstance() {
		LiveSettingsCaFragment ret = new LiveSettingsCaFragment();
		Bundle b = new Bundle();
		b.putString(ARG_NAME_UUID, Constant.UUID);
		ret.setArguments(b);
		return ret;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO ´´½¨VIEW
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	protected void onSessionReady(ipaneltv.toolkit.media.CaSessionFragment.Session s) {
		Log.d(this.toString(), "onSessionReady s = "+ s);
		synchronized (mutex) {
			WasuSettingsListener lis = listener;
			if (lis != null) {
				lis.onSessionReady((Session)s);
			}
		}
	}
	
	@Override
	public void onReadableEntries(HashMap<String, String> entries) {
		Log.d(this.toString(), "onReadableEntries listener = "+ listener);
		synchronized (mutex) {
			WasuSettingsListener lis = listener;
			if (lis != null) {
				lis.onReadableEntries(entries);
			}
		}
	}
	
	@Override
	public void onResponseBuyEntitlement(String uri, String err) {
		
	}
	
	@Override
	public void onResponseQuerySettings(Bundle b) {
		synchronized (mutex) {
			WasuSettingsListener lis = listener;
			if (lis != null) {
				lis.onResponseQuerySettings(null, b);
			}
		}
	}
	
	@Override
	public void onResponseQuerySettings(String token, Bundle b) {
		synchronized (mutex) {
			WasuSettingsListener lis = listener;
			if (lis != null) {
				lis.onResponseQuerySettings(token, b);
			}
		}
	}
	
	@Override
	public void onResponseUpdateSettings(String token, String err) {
		synchronized (mutex) {
			WasuSettingsListener lis = listener;
			if (lis != null) {
				lis.onResponseUpdateSettings(token, err);
			}
		}
		Log.i("onResponseUpdateSettings", "token="+token+"  "+"err="+err);
	}
	
	public void setListener(WasuSettingsListener lis) {
		this.listener = lis;
	}

	public static interface WasuSettingsListener {
		void onSessionReady(Session s);
		void onReadableEntries(HashMap<String, String> entries);
		void onResponseUpdateSettings(String token, String err);
		void onResponseQuerySettings(String token, Bundle b);
	}
}
