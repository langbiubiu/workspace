package cn.ipanel.android.ui;

import java.util.HashMap;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Looper;
import android.view.KeyEvent;
import cn.ipanel.android.LogHelper;

/**
 * 界面部件的管理类
 * */
public abstract class UIManager {
	private final static String TAG = UIManager.class.getSimpleName();
	/**
	 * 默认的界面显示时间
	 * */
	public final static int DEFAULT_DURATION_OF_SHOW_UI = 5 * 1000;
	/**
	 * 界面常显的 标志
	 * */
	public final static int FOREVER_DURATION_OF_SHOW_UI = -1;

	/** 配置列表 */
	private final HashMap<Integer, UIConfig> config = new HashMap<Integer, UIConfig>();

	/** 冲突界面的容器ＩＤ */
	public int root;

	/** 非冲突界面的容器 */
	public int mask;

	public FragmentManager fm;

	/**
	 * 配置界面
	 * */
	public abstract void initConfig(HashMap<Integer, UIConfig> config);

	public UIManager(Context cxt, int root, int mask) {
		this.root = root;
		this.mask = mask;
		fm = ((Activity) cxt).getFragmentManager();
		config.clear();
		initConfig(config);
	}

	/** 根据id获得配置数据 */
	public UIConfig getConfigUI(int id) {
		return config.get(id);
	}

	/** 根据id显示界面 */
	public void showUI(int id, Object o) {
		UIConfig ui = config.get(id);
		if (ui == null) {
			LogHelper.e(TAG,
					String.format("you hasn't config this id %s for ui", id));
			return;
		}
		showUI(ui.isExclusive(), id, o);
	}

	/** 根据id隐藏界面 */
	public void hideUI(int id) {
		UIFragment f = getFragmentByUIID(id);
		if (f != null && f.isAdded()) {
			fm.beginTransaction().detach(f).commit();
		}
	}
	/** 隐藏界面 */
	public boolean detatchExclusiveFagment(){
		Fragment rf=fm.findFragmentById(root);
		if(rf!=null&&rf.isAdded()){
			FragmentTransaction transaction=fm.beginTransaction();
			LogHelper.i(TAG, String.format(
					"detach fragment : %s", rf.getClass().getSimpleName()));
			transaction.detach(rf);
			transaction.commit();
			return true;
		}
		return false;
	}
	/** 重置界面的隐藏Timer */
	public void resetHideTimer() {
		Fragment f=getCurrentFragment() ;
		if (f != null&&f.isAdded()) {
			getCurrentFragment().resetHideTimer();
		}
	}

	/** 界面的按键派发 */
	public boolean handleKeyEvent(int keyCode, KeyEvent event) {
		UIFragment f = getCurrentFragment();
		if (f != null&&f.isAdded()) {
			if (KeyEvent.ACTION_DOWN == event.getAction()) {
				boolean flag = f.onKeyDown(keyCode, event);
				if (!flag
						&& (keyCode == KeyEvent.KEYCODE_DPAD_DOWN
								|| keyCode == KeyEvent.KEYCODE_DPAD_UP
								|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)) {
					flag = f.changeFocus(keyCode);
				}
				return flag;
			} else if (KeyEvent.ACTION_UP == event.getAction()) {
				return f.onKeyUp(keyCode, event);
			}
		}
		return false;
	}

	/** activity onPause时清楚当前的界面 */
	public void clearCurrentFragment() {
		Fragment rf=fm.findFragmentById(root);
		Fragment mf=fm.findFragmentById(mask);
		FragmentTransaction transaction=fm.beginTransaction();
		if(rf!=null){
			transaction.remove(rf);
		}
		if(mf!=null){
			transaction.remove(mf);
		}
		transaction.commit();
	}

	/** 隐藏界面 */
	public void hideFragment(UIFragment f) {
		if(f!=null){
			fm.beginTransaction().detach(f).commit();
		}
	}

	/** 获得当前界面 */
	public UIFragment getCurrentFragment() {
		return getCurrentFragment(root);
	}

	/**
	 * 派发数据变化
	 * */
	public void dispatchDataChange(final int type, final Object data) {
		final UIFragment f = getCurrentFragment();
		if (f != null&&f.isAdded()) {
			if (Looper.myLooper() != Looper.getMainLooper()) {
				f.mHandler.post(new Runnable() {
					@Override
					public void run() {
						f.onDataChange(type, data);
					}
				});
			} else {
				f.onDataChange(type, data);
			}
		}
	}
	/**
	 * 判断某个界面是否正在显示
	 * */
	public boolean isFragmentAdded(int id){
		UIFragment f = getFragmentByUIID(id);
		return f != null && f.isAdded();
	}

	private UIFragment getCurrentFragment(int id) {
		Fragment f = fm.findFragmentById(id);
		if (f instanceof UIFragment) {
			return (UIFragment) f;
		}
		if (f == null) {
			return null;
		}
		throw new IllegalStateException(String.format("invaid fragment %s", f
				.getClass().getName()));
	}

	private UIFragment getFragmentByUIID(int id) {
		Fragment f = fm.findFragmentByTag(changeIDToTag(id));
		UIFragment result = null;
		if (f instanceof UIFragment) {
			result = (UIFragment) f;
			result.setUID(id);
		}
		return result;
	}

	private UIFragment createUI(int id, Object o) {
		UIConfig ui = config.get(id);
		if (ui == null) {
			throw new IllegalStateException(String.format(
					"fail to create fragment %s ", id));
		}
		try {
			UIFragment f = (UIFragment) ui.getC().newInstance();
			f.setValue(o);
			f.setUID(id);
			return f;
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	private String changeIDToTag(int nav) {
		return "_" + nav;
	}

	private synchronized void showUI(boolean exclusive, int id, Object o) {
		LogHelper.i(TAG, String.format(
				"start show ui:id :%s with tag : %s,exclusive: %s", id, o + "",
				exclusive + ""));
		UIFragment ui=getFragmentByUIID(id);
		if(ui!=null){
			ui.setValue(o);
			if(ui.isAdded()){
				LogHelper.i(TAG, "current is need fragment");
				ui.refresh();
			}else{
				LogHelper.i(TAG, "next create from back stack");
				if(exclusive){
					detatchExclusiveFagment();
				}
				fm.beginTransaction().attach(ui).commit();

			}
		}else{
			ui = createUI(id, o);
			LogHelper.i(TAG, "create a fragment: "
					+ ui.getClass().getSimpleName());
			if(exclusive){
				detatchExclusiveFagment();
			}
			int container = exclusive ? root : mask;
			fm.beginTransaction().add(container, ui, changeIDToTag(id))
					.commit();
		}
	}

	/**
	 * 界面元素
	 * */
	public static class UIConfig {
		/** 界面的唯一ID */
		private int UID;
		/** 界面的显示类 */
		private Class c;
		/** 界面是否和其他界面冲突 */
		private boolean exclusive;
		/** 界面的显示时间 */
		private long duration;

		public UIConfig(int uID, Class c, boolean exclusive, long duration) {
			super();
			UID = uID;
			this.c = c;
			this.exclusive = exclusive;
			this.duration = duration;
		}

		public int getUID() {
			return UID;
		}

		public void setUID(int uID) {
			UID = uID;
		}

		public Class getC() {
			return c;
		}

		public void setC(Class c) {
			this.c = c;
		}

		public boolean isExclusive() {
			return exclusive;
		}

		public void setExclusive(boolean exclusive) {
			this.exclusive = exclusive;
		}

		public long getDuration() {
			return duration;
		}

		public void setDuration(long duration) {
			this.duration = duration;
		}

	}

}
