package ipaneltv.toolkit.wardship2;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.wardship2.WardshipTool.Password;
import ipaneltv.toolkit.wardship2.WardshipTool.WarshipListener;

import java.util.Iterator;
import java.util.Set;

import android.util.Log;

public class WardshipIndicater {
	public static final String TAG = WardshipIndicater.class.getSimpleName();
	private Set<ChannelKey> set;
	Password password;
	WardshipTool wardshipTool;

	public WardshipIndicater() {
		wardshipTool = WardshipTool.createWardshipTool();
		wardshipTool.setWardshipListener(new WarshipListener() {

			@Override
			public void onWarshipChanged() {
				Log.d(TAG, "onWarshipChanged 11 this = "+ this);
				onLoad();
			}
		});
		Log.d(TAG, "WardshipIndicater 11 this = "+ this);
	}

	public WardshipIndicater(String root) {
		wardshipTool = WardshipTool.createWardshipTool(root);
		wardshipTool.setWardshipListener(new WarshipListener() {
			@Override
			public void onWarshipChanged() {
				Log.d(TAG, "onWarshipChanged this="+ this);
				onLoad();
			}
		});
		Log.d(TAG, "WardshipIndicater this = "+ this);
	}

	public synchronized void ensureOnload() {
		Log.d(TAG, "ensureOnload");
			onLoad();
			wardshipTool.startWatching();
	}

	public void stopWatching(){
		wardshipTool.stopWatching();
	}
	
	@Override
	protected void finalize() throws Throwable {
		wardshipTool.stopWatching();
		super.finalize();
	}

	public void onLoad() {
		set = wardshipTool.getWardshipList();
		password = wardshipTool.getPassword();
		Log.d(TAG, "onLoad this = "+ this+";set = " + set);
		if (set != null) {
			Iterator<ChannelKey> iterator = set.iterator();
			while (iterator.hasNext()) {
				Log.d(TAG, "onLoad set=" + iterator.next());
			}
		}
	}

	/**
	 * @explain 检查节目是否上锁
	 * @param key
	 * @return true 表明以上锁， false表明未上锁
	 */
	public boolean chack(ChannelKey key) {
		if (isPwdEnable()) {
			Log.d(TAG, "chack this = "+ this+";set = "+ set);
			if (set != null && set.contains(key)) {
				return true;
			}
		}
		return false;
	}

	public boolean checkPwd(String pwd) {
		String oldPwd = null;
		if (password != null) {
			oldPwd = password.getPassword();
		}
		return wardshipTool.checkPwd(pwd, oldPwd);
	}

	public boolean isPwdEnable() {
		if (password != null) {
			return password.isPwdEnable();
		}
		return false;
	}

}
