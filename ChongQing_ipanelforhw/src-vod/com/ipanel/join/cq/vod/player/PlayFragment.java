package com.ipanel.join.cq.vod.player;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.utils.GlobalContext;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.TimeUtility;

public class PlayFragment extends BaseFragment {
	private SeekBar mSeekBar = null; // 进度条
	private RelativeLayout rlControl; // 底部容器
	private TextView mVideoName; // 当前影片名称
	private TextView mStartTime; // 当前影片的开始时间
	private TextView mDuration; // 当前影片结束时间
	private TextView mCurrentTime; // 影片当前的时间
//	private ProgressView progress = null; // 进度值
	public boolean isDraggingShow = false;
	String mDurationValue = "00:00:00";
	int mFocusColor = Color.parseColor("#FFFFFF");
	int mNormalColor = Color.parseColor("#919191");
	public Drawable mDrawableNor, mDrawableTran;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		ViewGroup container = createContainer(inflater, root, R.layout.vod_fragment_control_view);
		mSeekBar = (SeekBar) container.findViewById(R.id.seekbarcontrol);
		mStartTime = (TextView) container.findViewById(R.id.tv_starttime);
		mDuration = (TextView) container.findViewById(R.id.tv_endtime);
		rlControl = (RelativeLayout) container.findViewById(R.id.rl_control);
		mVideoName = (TextView) container.findViewById(R.id.tv_videoname);
		mCurrentTime = (TextView) container.findViewById(R.id.tv_currenttime);
		mDrawableNor = getResources().getDrawable(R.drawable.vod_slider_bg);
		mDrawableTran = getResources().getDrawable(R.drawable.vod_slider_bg);
		
		
//		progress = new ProgressView(container.getContext());
//		RelativeLayout.LayoutParams p1 = new RelativeLayout.LayoutParams(
//				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
//		p1.leftMargin = 140; // 120;
//		p1.topMargin = 565;
//		container.addView(progress, p1);
		return container;
	}

	@Override
	public void showFragment() {
		super.showFragment();
		initProgramInformation();
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
		int progressduration = ((int) (1000 * (current*1.0f / duration)));
		if(progressduration < 5){
			progressduration = 5;
		}
		mSeekBar.setProgress(progressduration);
//		progress.init(0l, duration);
//		progress.calPosition(current);
//		mCurrentTime.animate().setDuration(100);
		mCurrentTime.setTranslationX(GlobalContext.dp2px(1113) * (current*1.0f / duration));
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			changeThumbInSeekBar(mDrawableTran);
			break;
		case KeyEvent.KEYCODE_BACK:
			mFragmentFactory.showFragment(FragmentFactory.FRAGMENT_ID_EMPTY, null);
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {

		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_LEFT:
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			changeThumbInSeekBar(mDrawableNor);
			break;
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
