package cn.ipanel.android.widget;

import java.util.Formatter;
import java.util.Locale;

import cn.ipanel.android.widget.MediaControllerCallback;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.MediaController.MediaPlayerControl;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public abstract class BaseMediaController implements MediaControllerCallback {
	private MediaPlayerControl mPlayer;

	private View mControlView;
	private int mPlayResId;
	private int mPauseResId;

	protected ProgressBar mProgress;
	protected TextView mEndTime, mCurrentTime;
	protected ImageButton mPauseButton;
	protected ImageButton mFfwdButton;
	protected ImageButton mRewButton;
	protected ImageButton mNextButton;
	protected ImageButton mPrevButton;

	Formatter mFormatter;
	StringBuilder mFormatBuilder;

	private View mLoadingView;

	private boolean mShowing;
	private boolean mDragging;
	protected int mTimeout = 5000;
	private static final int FADE_OUT = 1;
	private static final int SHOW_PROGRESS = 2;

	private int mIdPause, mIdFfwd, mIdRew, mIdDuration, mIdSeek, mIdTime;

	public BaseMediaController(View controlView, int playRes, int pauseRes, int idPause,
			int idSeek, int idFfwd, int idRew, int idDuration, int idTime) {
		this.mControlView = controlView;
		this.mPlayResId = playRes;
		this.mPauseResId = pauseRes;
		this.mIdPause = idPause;
		this.mIdFfwd = idFfwd;
		this.mIdRew = idRew;
		this.mIdDuration = idDuration;
		this.mIdSeek = idSeek;
		this.mIdTime = idTime;

		initControllerView(controlView);
		mControlView.setVisibility(View.GONE);
	}
	
	public void setFadeTimeout(int miliseconds){
		this.mTimeout = miliseconds;
	}

	public void setLoadingView(View loading) {
		this.mLoadingView = loading;
	}

	private OnSeekBarChangeListener mSeekListener = new OnSeekBarChangeListener() {
		public void onStartTrackingTouch(SeekBar bar) {
			show(3600000);

			mDragging = true;

			// By removing these pending progress messages we make sure
			// that a) we won't update the progress while the user adjusts
			// the seekbar and b) once the user is done dragging the thumb
			// we will post one of these messages to the queue again and
			// this ensures that there will be exactly one message queued up.
			mHandler.removeMessages(SHOW_PROGRESS);
		}

		public void onProgressChanged(SeekBar bar, int progress, boolean fromuser) {
			if (!fromuser) {
				// We're not interested in programmatically generated changes to
				// the progress bar's position.
				return;
			}

			long duration = mPlayer.getDuration();
			long newposition = (duration * progress) / 1000L;
			mPlayer.seekTo((int) newposition);
			if (mCurrentTime != null)
				mCurrentTime.setText(stringForTime((int) newposition));
		}

		public void onStopTrackingTouch(SeekBar bar) {
			mDragging = false;
			setProgress();
			updatePausePlay();
			show(mTimeout);

			// Ensure that progress is properly updated in the future,
			// the call to show() does not guarantee this because it is a
			// no-op if we are already showing.
			mHandler.sendEmptyMessage(SHOW_PROGRESS);
		}
	};

	private View.OnClickListener mRewListener = new View.OnClickListener() {
		public void onClick(View v) {
			int pos = mPlayer.getCurrentPosition();
			pos -= 10000; // milliseconds
			mPlayer.seekTo(pos);
			setProgress();

			show(mTimeout);
		}
	};

	private View.OnClickListener mFfwdListener = new View.OnClickListener() {
		public void onClick(View v) {
			int pos = mPlayer.getCurrentPosition();
			pos += 15000; // milliseconds
			mPlayer.seekTo(pos);
			setProgress();

			show(mTimeout);
		}
	};

	private View.OnClickListener mPauseListener = new View.OnClickListener() {
		public void onClick(View v) {
			doPauseResume();
			show(mTimeout);
		}
	};

	private void doPauseResume() {
		if (mPlayer.isPlaying()) {
			mPlayer.pause();
		} else {
			mPlayer.start();
		}
		updatePausePlay();
	}

	private void initControllerView(View v) {
		mPauseButton = (ImageButton) v.findViewById(mIdPause);
		if (mPauseButton != null) {
			mPauseButton.requestFocus();
			mPauseButton.setOnClickListener(mPauseListener);
		}

		mFfwdButton = (ImageButton) v.findViewById(mIdFfwd);
		if (mFfwdButton != null) {
			mFfwdButton.setOnClickListener(mFfwdListener);
		}

		mRewButton = (ImageButton) v.findViewById(mIdRew);
		if (mRewButton != null) {
			mRewButton.setOnClickListener(mRewListener);
		}

		mProgress = (ProgressBar) v.findViewById(mIdSeek);
		if (mProgress != null) {
			if (mProgress instanceof SeekBar) {
				SeekBar seeker = (SeekBar) mProgress;
				seeker.setOnSeekBarChangeListener(mSeekListener);
			}
			mProgress.setMax(1000);
		}

		mEndTime = (TextView) v.findViewById(mIdDuration);
		mCurrentTime = (TextView) v.findViewById(mIdTime);
		mFormatBuilder = new StringBuilder();
		mFormatter = new Formatter(mFormatBuilder, Locale.getDefault());

	}

	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int pos;
			switch (msg.what) {
			case FADE_OUT:
				hide();
				break;
			case SHOW_PROGRESS:
				pos = setProgress();
				if (!mDragging && mShowing && mPlayer != null && mPlayer.isPlaying()) {
					msg = obtainMessage(SHOW_PROGRESS);
					sendMessageDelayed(msg, 1000 - (pos % 1000));
				}
				break;
			}
		}
	};

	@Override
	public void hide() {
		if (mShowing) {
			try {
				mHandler.removeMessages(SHOW_PROGRESS);
				mControlView.setVisibility(View.GONE);
				// mWindowManager.removeView(mDecor);
			} catch (IllegalArgumentException ex) {
				Log.w("MediaController", "already removed");
			}
			mShowing = false;
		}
	}

	@Override
	public void setMediaPlayer(MediaPlayerControl player) {
		mPlayer = player;
		updatePausePlay();
	}

	private void updatePausePlay() {
		if (mControlView == null || mPauseButton == null)
			return;

		if (mPlayer != null && mPlayer.isPlaying()) {
			mPauseButton.setImageResource(mPauseResId);
		} else {
			mPauseButton.setImageResource(mPlayResId);
		}
	}

	@Override
	public void setEnabled(boolean inPlaybackState) {
		// TODO Auto-generated method stub

	}

	@Override
	public void show() {
		show(mTimeout);
	}

	@Override
	public void show(int timeout) {
		if (!mShowing) {
			setProgress();
			if (mPauseButton != null) {
				mPauseButton.requestFocus();
			}
			// disableUnsupportedButtons();
			// updateFloatingWindowLayout();
			// mWindowManager.addView(mDecor, mDecorLayoutParams);
			mControlView.setVisibility(View.VISIBLE);
			mShowing = true;
		}
		updatePausePlay();

		// cause the progress bar to be updated even if mShowing
		// was already true. This happens, for example, if we're
		// paused with the progress bar showing the user hits play.
		mHandler.sendEmptyMessage(SHOW_PROGRESS);

		Message msg = mHandler.obtainMessage(FADE_OUT);
		if (timeout != 0) {
			mHandler.removeMessages(FADE_OUT);
			mHandler.sendMessageDelayed(msg, timeout);
		}
	}

	@Override
	public boolean isShowing() {
		return mShowing;
	}

	private int setProgress() {
		if (mPlayer == null || mDragging) {
			return 0;
		}
		int position = mPlayer.getCurrentPosition();
		int duration = mPlayer.getDuration();
		if (mProgress != null) {
			if (duration > 0) {
				// use long to avoid overflow
				long pos = 1000L * position / duration;
				mProgress.setProgress((int) pos);
			}
			int percent = mPlayer.getBufferPercentage();
			mProgress.setSecondaryProgress(percent * 10);
		}

		if (mEndTime != null)
			mEndTime.setText(stringForTime(duration));
		if (mCurrentTime != null)
			mCurrentTime.setText(stringForTime(position));

		return position;
	}

	private String stringForTime(int timeMs) {
		int totalSeconds = timeMs / 1000;

		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;

		mFormatBuilder.setLength(0);
		if (hours > 0) {
			return mFormatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
		} else {
			return mFormatter.format("%02d:%02d", minutes, seconds).toString();
		}
	}

	@Override
	public void onInfo(int what, int extra) {
		switch (what) {
		case MediaPlayer.MEDIA_INFO_BUFFERING_START:
			if (mLoadingView != null)
				mLoadingView.setVisibility(View.VISIBLE);
			break;
		case MediaPlayer.MEDIA_INFO_BUFFERING_END:
			if (mLoadingView != null)
				mLoadingView.setVisibility(View.GONE);
			break;
		}

	}

	@Override
	public void onBuffering(int percent) {
		// TODO Auto-generated method stub

	}

}
