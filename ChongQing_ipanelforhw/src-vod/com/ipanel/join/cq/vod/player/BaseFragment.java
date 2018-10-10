package com.ipanel.join.cq.vod.player;

import java.lang.ref.SoftReference;

import com.ipanel.join.cq.vod.utils.Logger;

import android.animation.Animator;
import android.app.Fragment;
import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;

public abstract class BaseFragment extends Fragment {

	public static final int HIDE_TIME_NEVER = -1;
	protected boolean create_every_time = false;
	protected SimplePlayerActivity mActivity;
	protected FragmentFactory mFragmentFactory;
	private int UID = -1;
	private Object obj = null;
	private SoftReference<View> soft_view;
	private boolean showAd = false;//增加退出广告
	
	AudioManager audio;
	int max_vol;

	protected Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			handleMessageLocal(msg);
		};
	};

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		this.mActivity = (SimplePlayerActivity) this.getActivity();
		mFragmentFactory = mActivity.mFragmentFactory;
		audio = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
		max_vol = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
	};

	@Override
	public void onResume() {
		super.onResume();
		resetHideTimer();
		showFragment();
		refreshFragment();
	}

	@Override
	public void onPause() {
		super.onPause();
		removeHideTimer();
		hideFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = null;
		if (!create_every_time && soft_view != null && soft_view.get() != null
				&& soft_view.get().getParent() == null) {
			v = soft_view.get();
		} else {
			v = onCreateView(inflater, container);
			if (v != null) {
				v.setFocusable(false);
			}
			soft_view = new SoftReference<View>(v);
		}
		return v;
	}

	public abstract View onCreateView(LayoutInflater inflater,
			ViewGroup container);//

	public ViewGroup createContainer(LayoutInflater inflater, ViewGroup root,
			int resource) {
		return (ViewGroup) inflater.inflate(resource, root, false);
	}

	public void resetHideTimer() {
		if (getHideDelay() > 0) {
			mHandler.removeMessages(MessageColection.NAV_MESSAGE_HIDE_SELF);
			mHandler.sendEmptyMessageDelayed(
					MessageColection.NAV_MESSAGE_HIDE_SELF, getHideDelay());
		}
	}

	public void removeHideTimer() {
		if (getHideDelay() > 0) {
			mHandler.removeMessages(MessageColection.NAV_MESSAGE_HIDE_SELF);
		}
	}

	public int getUID() {
		return UID;
	}

	public Object getObject() {
		return obj;
	}

	public void setUID(int UID) {
		this.UID = UID;
	}

	public void setObject(Object o) {
		this.obj = o;
	}

	public void showFragment() {

	}

	public void refreshFragment() {

	}

	public void hideFragment() {

	}

	public void onDataChange(int type, Object o) {

	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
//		case KeyEvent.KEYCODE_0:
//		case KeyEvent.KEYCODE_1:
//		case KeyEvent.KEYCODE_2:
//		case KeyEvent.KEYCODE_3:
//		case KeyEvent.KEYCODE_4:
//		case KeyEvent.KEYCODE_5:
//		case KeyEvent.KEYCODE_6:
//		case KeyEvent.KEYCODE_7:
//		case KeyEvent.KEYCODE_8:
//		case KeyEvent.KEYCODE_9:
			if(!VodPlayerManager.getInstance(mActivity).isSpeeding()){
				mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_INPUT,
						keyCode - KeyEvent.KEYCODE_0);
			}

			return true;
		case RcKeyEvent.KEYCODE_QUIT:
		case KeyEvent.KEYCODE_BACK:
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_EMPTY,
					null);
			return true;
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if(Constant.isDragShiftStyle()){
				VodPlayerManager.getInstance(getActivity()).dragTime(
						keyCode == KeyEvent.KEYCODE_DPAD_LEFT);
			}else{
				mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_PLAY,null);
			}
			return true;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if(showAd){
				VodPlayerManager.getInstance(getActivity()).doPressEnterKey();
			}else{
				mActivity.onEnterPressed();//暂停
			}
			return true;
		case RcKeyEvent.KEYCODE_SOUND_TRACK:
			VodPlayerManager.getInstance(getActivity()).changeSoundTrack();
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_SOUND_TRACK,
					null);
			return true;
		case RcKeyEvent.KEYCODE_INFORMATION:
		case KeyEvent.KEYCODE_MENU:
		case 584:
		case 210:
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_PLAY,
					null);
			return true;
		case 17:
			VodPlayerManager.getInstance(getActivity()).changeScale();
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_SCALE,
					null);
			return true;
		
		case KeyEvent.KEYCODE_VOLUME_MUTE:
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			audio.setStreamMute(AudioManager.STREAM_MUSIC, false);
			int cur_vol = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, Math.max(0, cur_vol - 1), 0);
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_VOLUME, null);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			audio.setStreamMute(AudioManager.STREAM_MUSIC, false);
			int cur_vol2 = audio.getStreamVolume(AudioManager.STREAM_MUSIC);
			int max = audio.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			audio.setStreamVolume(AudioManager.STREAM_MUSIC, Math.min(max, cur_vol2 + 1), 0);
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_VOLUME, null);
			return true;
		}
		return true;
	}
	
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if(VodPlayerManager.getInstance(getActivity()).getPlayState()!=VodPlayerManager.PLAY_STATE_DESTROY){
				Logger.d("wuhd", "BaseFragment onKeyUp");
				if(Constant.isDragShiftStyle()){
					Logger.d("wuhd", "BaseFragment isDragShiftStyle");
					VodPlayerManager.getInstance(getActivity()).dragFinish();
				}else{
					VodPlayerManager.getInstance(getActivity()).changePlaySpeed(keyCode==KeyEvent.KEYCODE_DPAD_RIGHT);
				}
			}
			return true;
		default:
			break;
		}
		return true;
	}

	protected void handleMessageLocal(Message msg) {
		Logger.d("handleMessage","msg.what: "+msg.what);
		switch (msg.what) {
		case MessageColection.NAV_MESSAGE_HIDE_SELF:
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_EMPTY,
					null);
			break;
		case MessageColection.NAV_MESSAGE_UPDATE_SELF:
			refreshFragment();
			break;
		}
	}

	@Override
	public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {

		// AnimatorSet set = new AnimatorSet();
		// ObjectAnimator alpha = null;
		// if (enter) {
		// set.setDuration(getAnimationTime());
		// alpha = ObjectAnimator.ofFloat(getView(), "alpha", 0.0f, 1.0f);
		// } else {
		// set.setDuration(getAnimationTime());
		// alpha = ObjectAnimator.ofFloat(getView(), "alpha", 1.0f, 0.0f);
		// }
		// AnimatorSet.Builder builder = set.play(alpha);
		// return set;
		return super.onCreateAnimator(transit, enter, nextAnim);
	}

	protected int getHideDelay() {
		return HIDE_TIME_NEVER;
	}

	protected int getAnimationTime() {
		return 500;
	}

	public ViewGroup getCurrentView() {
		return (ViewGroup) getView();
	}

	public View changeFocus(int keyCode) {
		boolean handled = false;
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
			handled = arrowScroll(View.FOCUS_LEFT);
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			handled = arrowScroll(View.FOCUS_RIGHT);
			break;
		case KeyEvent.KEYCODE_DPAD_UP:
			handled = arrowScroll(View.FOCUS_UP);
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			handled = arrowScroll(View.FOCUS_DOWN);
			break;
			
		}
		if (handled) {
			return mActivity.getCurrentFocus();
		} else {
			return null;
		}
	}

	public boolean arrowScroll(int direction) {
		boolean handled = false;
		if (getView() != null) {
			View currentFocused = mActivity.getCurrentFocus();
			if (currentFocused == getView())
				currentFocused = null;

			View nextFocused = FocusFinder.getInstance().findNextFocus(
					(ViewGroup) getView(), currentFocused, direction);
			if (nextFocused != null && nextFocused != currentFocused) {
				handled = nextFocused.requestFocus();
			}
			if (handled) {
				getView().playSoundEffect(
						SoundEffectConstants
								.getContantForFocusDirection(direction));
			}
		}

		return handled;
	}
}
