package cn.ipanel.android.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;

public abstract class BaseHolderActivity extends Activity implements FragmentHolder {
	protected FragmentHelper fHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		fHelper = createFragmentHelper();
	}

	protected abstract FragmentHelper createFragmentHelper();

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if(fHelper != null && event.getAction() == KeyEvent.ACTION_DOWN){
			fHelper.resetAutoHide();
		}
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (fHelper != null && fHelper.onKeyDown(keyCode, event))
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(fHelper != null)
			fHelper.resetAutoHide();
	}

	@Override
	protected void onPause() {
		if(fHelper != null)
			fHelper.removeMsgs();
		super.onPause();
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (fHelper != null && fHelper.onKeyUp(keyCode, event))
			return true;
		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		if (fHelper != null && fHelper.onBackPressed())
			return;
		super.onBackPressed();
	}

	@Override
	public FragmentHelper getFragmentHelper() {
		return fHelper;
	}

}
