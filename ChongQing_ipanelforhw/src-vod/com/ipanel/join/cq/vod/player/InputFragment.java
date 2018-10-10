package com.ipanel.join.cq.vod.player;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.player.TimeInputView.InputCancelListener;
import com.ipanel.join.cq.vod.player.TimeInputView.InputEnterListener;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.TimeUtility;

public class InputFragment extends BaseFragment {
	private SeekBar mSeekBar = null; // 进度条
	private RelativeLayout rlControl; // 底部容器
	private TextView mVideoName; // 当前影片名称
	private TextView mStartTime; // 当前影片的开始时间
	private TextView mDuration; // 当前影片结束时间
	private TextView mCurrentTime; // 影片当前的时间
//	private ProgressView progress = null; // 进度值
	public TimeInputView timeInput; // 跳转显示
	public boolean isDraggingShow = false;
	String mDurationValue = "00:00:00";
	int mFocusColor = Color.parseColor("#FFFFFF");
	int mNormalColor = Color.parseColor("#919191");
	public Drawable mDrawableNor, mDrawableTran;

	float dp;
	
	public int dp2px(int val){
		return (int)(val*dp+0.5f);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		ViewGroup container = createContainer(inflater, root, R.layout.vod_fragment_control_view2);
		dp = root.getResources().getDisplayMetrics().density;
//		container.setBackgroundColor(Color.WHITE);
		mSeekBar = (SeekBar) container.findViewById(R.id.seekbarcontrol);
		mStartTime = (TextView) container.findViewById(R.id.tv_starttime);
		mDuration = (TextView) container.findViewById(R.id.tv_endtime);
		rlControl = (RelativeLayout) container.findViewById(R.id.rl_control);
		mVideoName = (TextView) container.findViewById(R.id.tv_videoname);
		mCurrentTime = (TextView) container.findViewById(R.id.tv_currenttime);
		mDrawableNor = getResources().getDrawable(R.drawable.vod_slider_01);
		mDrawableTran = getResources().getDrawable(R.drawable.vod_slider_02);
//		progress = new ProgressView(container.getContext());
		
		timeInput = new TimeInputView(container.getContext());
//		timeInput.setBackgroundColor(Color.RED);
		RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(dp2px(360),
				dp2px(120));
		p.leftMargin = dp2px(920);
		p.topMargin = dp2px(467);
		container.addView(timeInput, p);
		timeInput.setCancelListener(new InputCancelListener() {
			@Override
			public void onCancel() {
				mFragmentFactory.showFragment(
						FragmentFactory.FRAGMENT_ID_EMPTY, null);
			}
		});

		timeInput.setEnterListener(new InputEnterListener() {
			@Override
			public void onEnter() {
				long l = timeInput.parseIntArrayTOLong();
				Logger.d("get input time : " + l);
				try {
					if (l < 0) {
						SimplePlayerActivity activity = (SimplePlayerActivity) getActivity();
						activity.prompt
								.showDefaultDialog(R.string.timeout);
						mFragmentFactory.showFragment(
								FragmentFactory.FRAGMENT_ID_EMPTY, null);
					} else {
						VodPlayerManager.getInstance(getActivity()).seekTo(l);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		return container;
	}

	@Override
	public void showFragment() {
		super.showFragment();
		initProgramInformation();
		timeInput.setVisibility(View.VISIBLE);
		timeInput.clear();
		long duration = VodPlayerManager.getInstance(getActivity()).getDuration();
		if (getObject() != null) {
			timeInput.init(0L, duration);
			timeInput.initValue(Integer.parseInt(getObject() + ""));
		}
	}

	@Override
	public void refreshFragment() {
		super.refreshFragment();
		refreshProgress();
	}

	private void initProgramInformation() {
		Logger.d("initProgramInfor");
		rlControl.setVisibility(View.VISIBLE);
		mStartTime.setText("00:00:00");
		mDuration.setText(mDurationValue);
	}

	@Override
	public void onDataChange(int type, Object o) {
		super.onDataChange(type, o);
		if (type == Constant.DATA_CHANGE_TIME_TICK) {
			refreshProgress();
		}
	}

	private void refreshProgress() {
		long duration = VodPlayerManager.getInstance(getActivity()).getDuration();
		long current = VodPlayerManager.getInstance(getActivity()).getElapsed();
		mDuration.setText(TimeUtility.formatTime(duration));
		mCurrentTime.setText(TimeUtility.formatTime(current));
		SimplePlayerActivity  activity = (SimplePlayerActivity) getActivity();
		mVideoName.setText(activity.getName());
		mSeekBar.setMax(1000);
		mSeekBar.setProgress(((int) (1000 * (current*1.0f / duration))));
//		progress.init(0l, duration);
//		progress.calPosition(current);
//		timeInput.init(0l, duration);重复
		mCurrentTime.setTranslationX( 1113 * getResources().getDisplayMetrics().density
				* (current * 1.0f / duration));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case RcKeyEvent.KEYCODE_QUIT:
		case KeyEvent.KEYCODE_BACK:
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
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			timeInput.onKeyDown(keyCode, event);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
		case KeyEvent.KEYCODE_DPAD_UP:
		case KeyEvent.KEYCODE_DPAD_DOWN:
		case RcKeyEvent.KEYCODE_QUIT:
		case KeyEvent.KEYCODE_BACK:
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
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			timeInput.onKeyUp(keyCode, event);
			return true;
		}
		return super.onKeyUp(keyCode, event);
	}

	public void changeThumbInSeekBar(Drawable mDrawable) {
		if (mSeekBar != null && mDrawable != null) {
			mSeekBar.setThumb(mDrawable);
		}
	}
	@Override
	protected int getHideDelay() {
		return 4000;
	}
}
