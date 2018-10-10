package com.ipanel.join.chongqing.live.ca;

import ipaneltv.toolkit.media.LiveCaSessionFragment;
import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ipanel.join.chongqing.live.Constant;

public class LiveLiveCaFragment extends LiveCaSessionFragment {
	private Object mutex = new Object();
	WasuLiveCaListener listener;

	public static LiveLiveCaFragment createInstance() {
		LiveLiveCaFragment ret = new LiveLiveCaFragment();
		Bundle b = new Bundle();
		b.putString(ARG_NAME_UUID, Constant.UUID);
		ret.setArguments(b);
		return ret;
	}

	private static final String TAG = "LiveLiveCaFragment_2";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate===");
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onResume() {
		Log.i(TAG, "onResume===");
		super.onResume();
	}
	
	@Override
	public void onAttach(Activity activity) {
		Log.i(TAG, "onAttach===");
		super.onAttach(activity);
	}
	
	@Override
	public void onPause() {
		Log.i(TAG, "onPause===");
		super.onPause();
	}
	@Override
	public void onStop() {
		Log.i(TAG, "onStop===");
		super.onStop();
	}
	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy===");
		super.onDestroy();
	}
	
	@Override
	public void onDetach() {
		Log.i(TAG, "onDetach===");
		super.onDetach();
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// TODO ´´½¨VIEW
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
	@Override
	protected void onScrollMessage(String msg) {
		Log.d(this.toString(), "onScrollMessage msg = "+ msg);
		synchronized (mutex) {
			WasuLiveCaListener lis = listener;
			if (lis != null) {
				lis.onScrollMessage(msg);
			}
		}
	}
	
	@Override
	protected void onNativeSessionReady(Session s) {
		Log.d(this.toString(), "onSessionReady s = "+ s);
		synchronized (mutex) {
			WasuLiveCaListener lis = listener;
			if (lis != null) {
				lis.onSessionReady(s);
			}
		}
	}
	
	@Override
	protected void onUnreadMailSize(int size) {
		Log.d(this.toString(), "onUnreadMailSize size = "+ size);
		synchronized (mutex) {
			WasuLiveCaListener lis = listener;
			if (lis != null) {
				lis.onUnreadMailSize(size);
			}
		}
	}
	
	@Override
	protected void onUrgencyMails(String token, Bundle b) {
		synchronized (mutex) {
			WasuLiveCaListener lis = listener;
			if (lis != null) {
				lis.onUrgencyMails(token,b);
			}
		}
	}
	
	public void setLiveCaListener(WasuLiveCaListener lis) {
		this.listener = lis;
	}
	
	public static interface WasuLiveCaListener {
		void onSessionReady(Session s);
		void onScrollMessage(String msg);
		void onUrgencyMails(String token, Bundle b);
		void onUnreadMailSize(int size);
	}
}
