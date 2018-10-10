package cn.ipanel.android.fragment;

import android.app.Fragment;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * 
 * @author Zexu
 *
 * @param <T>
 *            The holder activity
 */
public abstract class BaseFragment<T extends FragmentHolder> extends Fragment {
	protected int mAutoHideDelay = -1;

	protected Object data;

	public BaseFragment() {
		setRetainInstance(true);
	}

	public View findViewById(int id) {
		View root = getView();
		if (root != null)
			return root.findViewById(id);
		return null;
	}

	@SuppressWarnings("unchecked")
	public T getHolder() {
		return (T) getActivity();
	}

	public FragmentHelper getFragmentHelper() {
		return getHolder().getFragmentHelper();
	}
	
	public void hideSelf(){
		getFragmentHelper().hideFragmentBy(getUID());
	}

	public String getUID() {
		return this.getClass().getSimpleName();
	}

	public int getAutoHideDelay() {
		return mAutoHideDelay;
	}

	public void updateDisplay() {

	}

	public Object getData() {
		return data;
	}

	public void setData(Object data) {
		this.data = data;
	}

	public boolean onBackPressed() {
		return false;
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			return moveFocus(View.FOCUS_LEFT);
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			return moveFocus(View.FOCUS_RIGHT);
		case KeyEvent.KEYCODE_DPAD_UP:
			return moveFocus(View.FOCUS_UP);
		case KeyEvent.KEYCODE_DPAD_DOWN:
			return moveFocus(View.FOCUS_DOWN);

		}
		return false;
	}

	protected boolean moveFocus(int direction) {
		View root = getView();
		if (root instanceof ViewGroup) {
			View focused = root.findFocus();
			View next = FocusFinder.getInstance().findNextFocus((ViewGroup) getView(), focused,
					direction);
			if (next != null) {
				next.requestFocus();
				return true;
			}
		}
		return false;
	}

	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return false;
	}

}
