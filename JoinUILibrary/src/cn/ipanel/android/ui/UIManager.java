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
 * ���沿���Ĺ�����
 * */
public abstract class UIManager {
	private final static String TAG = UIManager.class.getSimpleName();
	/**
	 * Ĭ�ϵĽ�����ʾʱ��
	 * */
	public final static int DEFAULT_DURATION_OF_SHOW_UI = 5 * 1000;
	/**
	 * ���泣�Ե� ��־
	 * */
	public final static int FOREVER_DURATION_OF_SHOW_UI = -1;

	/** �����б� */
	private final HashMap<Integer, UIConfig> config = new HashMap<Integer, UIConfig>();

	/** ��ͻ����������ɣ� */
	public int root;

	/** �ǳ�ͻ��������� */
	public int mask;

	public FragmentManager fm;

	/**
	 * ���ý���
	 * */
	public abstract void initConfig(HashMap<Integer, UIConfig> config);

	public UIManager(Context cxt, int root, int mask) {
		this.root = root;
		this.mask = mask;
		fm = ((Activity) cxt).getFragmentManager();
		config.clear();
		initConfig(config);
	}

	/** ����id����������� */
	public UIConfig getConfigUI(int id) {
		return config.get(id);
	}

	/** ����id��ʾ���� */
	public void showUI(int id, Object o) {
		UIConfig ui = config.get(id);
		if (ui == null) {
			LogHelper.e(TAG,
					String.format("you hasn't config this id %s for ui", id));
			return;
		}
		showUI(ui.isExclusive(), id, o);
	}

	/** ����id���ؽ��� */
	public void hideUI(int id) {
		UIFragment f = getFragmentByUIID(id);
		if (f != null && f.isAdded()) {
			fm.beginTransaction().detach(f).commit();
		}
	}
	/** ���ؽ��� */
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
	/** ���ý��������Timer */
	public void resetHideTimer() {
		Fragment f=getCurrentFragment() ;
		if (f != null&&f.isAdded()) {
			getCurrentFragment().resetHideTimer();
		}
	}

	/** ����İ����ɷ� */
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

	/** activity onPauseʱ�����ǰ�Ľ��� */
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

	/** ���ؽ��� */
	public void hideFragment(UIFragment f) {
		if(f!=null){
			fm.beginTransaction().detach(f).commit();
		}
	}

	/** ��õ�ǰ���� */
	public UIFragment getCurrentFragment() {
		return getCurrentFragment(root);
	}

	/**
	 * �ɷ����ݱ仯
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
	 * �ж�ĳ�������Ƿ�������ʾ
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
	 * ����Ԫ��
	 * */
	public static class UIConfig {
		/** �����ΨһID */
		private int UID;
		/** �������ʾ�� */
		private Class c;
		/** �����Ƿ�����������ͻ */
		private boolean exclusive;
		/** �������ʾʱ�� */
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
