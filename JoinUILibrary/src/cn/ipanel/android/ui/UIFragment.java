package cn.ipanel.android.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.ui.UIManager.UIConfig;

/**
 * 界面部件的基类
 * */
public abstract class UIFragment extends Fragment implements IUIControl {
	/** 隐藏界面的消息 */
	public static final int MSG_HIDE_UI = 100;
	public String TAG = "";
	private Object value;
	private int UID;

	public abstract UIManager getUIManager();

	protected Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			handleFragmentMessage(msg);
		};
	};

	public UIFragment() {
		TAG = getClass().getSimpleName();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		LogHelper.i(TAG, "onCreateView");
		return onCreateView(inflater, container);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		LogHelper.i(TAG, "onCreate");

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		LogHelper.i(TAG, "onDestroy");

	}

	@Override
	public void onResume() {
		super.onResume();
		LogHelper.i(TAG, "onResume");
		show();
	}

	@Override
	public void onPause() {
		super.onPause();
		LogHelper.i(TAG, "onPause");
		hide();
	}

	public final void show() {
		onShow();
		onRefresh();
		resetHideTimer();
	}

	public final void hide() {
		cancelHideTimer();
		onHide();
	}
	
	public final void refresh(){
		onRefresh();
		resetHideTimer();
	}

	/**
	 * 重置消失的Timer
	 * */
	public void resetHideTimer() {
		long duration = getUIManager().getConfigUI(getUID()).getDuration();
		if (duration > 0) {
			mHandler.removeMessages(MSG_HIDE_UI);
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_UI, duration);
		}
	}

	/**
	 * 取消消失的Timer
	 * */
	public void cancelHideTimer() {
		mHandler.removeMessages(MSG_HIDE_UI);
	}

	public void handleFragmentMessage(android.os.Message msg) {
		switch (msg.what) {
		case MSG_HIDE_UI:
			LogHelper.i(TAG, "handle msg of hide");
			getUIManager().hideFragment(this);
			break;
		}
	}

	public void setValue(Object o) {
		value = o;
	}

	public Object getValue() {
		return value;
	}

	/**
	 * 获得界面的唯一标示符
	 * */
	public int getUID() {
		return UID;
	}

	public void setUID(int id) {
		UID = id;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

	public boolean changeFocus(int keyCode) {
		View next_focus;
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			next_focus = findNextFocus(View.FOCUS_LEFT);
			if (next_focus != null) {
				next_focus.requestFocus();
				return true;
			}
			break;

		case KeyEvent.KEYCODE_DPAD_RIGHT:
			next_focus = findNextFocus(View.FOCUS_RIGHT);
			if (next_focus != null) {
				next_focus.requestFocus();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			next_focus = findNextFocus(View.FOCUS_DOWN);
			if (next_focus != null) {
				next_focus.requestFocus();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			next_focus = findNextFocus(View.FOCUS_UP);
			if (next_focus != null) {
				next_focus.requestFocus();
				return true;
			}
			break;
		}
		return false;
	}
	
	public boolean isInSelf(){
		UIManager manager=getUIManager();
		if(manager!=null){
			UIConfig ui=getUIManager().getConfigUI(getUID());
			if(ui.isExclusive()){
				UIFragment f=manager.getCurrentFragment();
				if(f!=null){
					return f.getUID()==getUID();
				}
			}
		}
		return false;
	}

	private View findNextFocus(int direction) {
		if(getView()==null){
			return null;
		}
		return FocusFinder.getInstance().findNextFocus((ViewGroup) getView(),
				getActivity().getCurrentFocus(), direction);
	}

}
