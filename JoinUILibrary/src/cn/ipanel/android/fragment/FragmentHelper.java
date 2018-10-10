package cn.ipanel.android.fragment;

import java.util.HashMap;
import java.util.Map;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;

/**
 * Simple helper to delegate event to active fragment
 * 
 * @author Zexu
 *
 */
public class FragmentHelper {
	protected final Activity activity;
	protected final int containerId;
	protected FragmentManager fm;
	protected Map<String, BaseFragment<?>> fMaps = new HashMap<String, BaseFragment<?>>();

	public static final int MSG_AUTO_HIDE = 1;
	protected Handler uiHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_AUTO_HIDE:
				hideCurrentFragment();
				break;
			}
		}

	};

	public FragmentHelper(Activity activity, int containerId) {
		this.activity = activity;
		this.containerId = containerId;
		fm = activity.getFragmentManager();
	}

	public void hideFragmentBy(String tag) {
		BaseFragment<?> f = (BaseFragment<?>) fm.findFragmentByTag(tag);
		if (f != null && f.isVisible())
			fm.beginTransaction().detach(f).commitAllowingStateLoss();
	}
	
	public void hideAllFragments(){
		FragmentTransaction ft = fm.beginTransaction();
		for(String tag : fMaps.keySet()){
			Fragment f = fm.findFragmentByTag(tag);
			if (f != null && f.isVisible())
				ft.detach(f);
		}
		ft.commitAllowingStateLoss();
	}

	public void hideCurrentFragment() {
		BaseFragment<?> current = getCurrentFragment();
		if (current != null) {
			fm.beginTransaction().detach(current).commitAllowingStateLoss();
		}
	}

	public void resetAutoHide() {
		BaseFragment<?> current = getCurrentFragment();
		if (current != null)
			autoHide(current.getAutoHideDelay());
	}

	public boolean onBackPressed() {
		BaseFragment<?> current = getCurrentFragment();
		if (current != null) {
			if (current.onBackPressed())
				return true;
			hideCurrentFragment();
			return true;
		}
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View container = activity.findViewById(containerId);
		if(activity.getCurrentFocus() != null && !container.hasFocus())
			return false;
		BaseFragment<?> current = getCurrentFragment();
		if (current != null && current.onKeyDown(keyCode, event))
			return true;
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		View container = activity.findViewById(containerId);
		if(activity.getCurrentFocus() != null && !container.hasFocus())
			return false;
		
		BaseFragment<?> current = getCurrentFragment();
		if (current != null && current.onKeyUp(keyCode, event))
			return true;
		return false;
	}

	public void autoHide(int delay) {
		uiHandler.removeMessages(MSG_AUTO_HIDE);
		if (delay >= 0)
			uiHandler.sendEmptyMessageDelayed(MSG_AUTO_HIDE, delay);
	}
	
	public void removeMsgs(){
		uiHandler.removeCallbacksAndMessages(null);
	}

	public FragmentHelper register(BaseFragment<?> fragment) {
		return register(fragment.getUID(), fragment);
	}

	private FragmentHelper register(String tag, BaseFragment<?> fragment) {
		synchronized (fMaps) {
			fMaps.put(tag, fragment);
		}
		return this;
	}

	public BaseFragment<?> getCurrentFragment() {
		BaseFragment<?> f = (BaseFragment<?>) fm.findFragmentById(containerId);
		if (f != null && f.isVisible())
			return f;
		return null;
	}

	public void showFragment(String tag) {
		showFragment(tag, null, true);
	}
	
	public void showFragment(String tag, boolean replace) {
		showFragment(tag, null, replace);
	}
	
	public void showFragment(String tag, final Object data, boolean replace) {
		BaseFragment<?> f = fMaps.get(tag);
		if (f == null)
			return;
		BaseFragment<?> current = getCurrentFragment();
		if (current == null || !current.getUID().equals(f.getUID())) {
			FragmentTransaction ft = fm.beginTransaction();
			if (current != null && replace)
				ft.detach(current);
			BaseFragment<?> in = (BaseFragment<?>) fm.findFragmentByTag(tag);
			if (in != null) {
				in.setData(data);
				if(in.isVisible())
					in.updateDisplay();
				else
					ft.attach(in);
			} else {
				f.setData(data);
				ft.add(containerId, f, tag);
			}
			ft.commitAllowingStateLoss();
		} else {
			current.setData(data);
			if (!current.isVisible()) {
				fm.beginTransaction().attach(current).commitAllowingStateLoss();
			} else {
				current.updateDisplay();
			}
		}
		autoHide(f.getAutoHideDelay());

	}
}
