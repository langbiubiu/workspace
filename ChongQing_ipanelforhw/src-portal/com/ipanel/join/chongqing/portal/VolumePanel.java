package com.ipanel.join.chongqing.portal;

import com.ipanel.chongqing_ipanelforhw.R;

import cn.ipanel.android.reflect.SysUtils;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.SeekBar;
import android.widget.TextView;

public class VolumePanel {
	private static final int AUTO_HIDE_DELAY = 5000;
	private static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;
	SeekBar vol;
	View mute, volPanel;
	TextView volValue;

	AudioManager am;

	public VolumePanel(Activity activity) {
		View v = activity.getLayoutInflater().inflate(R.layout.common_volume_panel, null);
		activity.addContentView(v, new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT));
		vol = (SeekBar) v.findViewById(R.id.volume_progress);
		volValue = (TextView) v.findViewById(R.id.volume_value);
		vol.setFocusable(false);
		volPanel = v.findViewById(R.id.common_vol_panel);
		mute = v.findViewById(R.id.common_mute);

		volPanel.setVisibility(View.GONE);
		am = (AudioManager) activity.getSystemService(Context.AUDIO_SERVICE);
		updateMuteUI();
	}

	public void onResume() {
		Intent intentb = new Intent();
		intentb.setAction("com.ipanel.SYSTEM_HIDE_VOLUME_BAR");
		vol.getContext().sendBroadcast(intentb);

		updateMuteUI();
	}

	private void updateMuteUI() {

		boolean isMute = SysUtils.isStreamMute(am, STREAM_TYPE);
		mute.setVisibility(isMute ? View.VISIBLE : View.GONE);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		resetHide();
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if (volPanel.getVisibility() == View.VISIBLE) {
				volPanel.setVisibility(View.GONE);
				mHandler.removeMessages(MSG_HIDE_VOL_PANEL);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			toggleMute();
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			changeVolume(1);
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			changeVolume(-1);
			return true;
		default:
			if (volPanel.getVisibility() == View.VISIBLE) {
				volPanel.setVisibility(View.GONE);
				mHandler.removeMessages(MSG_HIDE_VOL_PANEL);
			}
			break;
		}
		return false;
	}

	private void changeVolume(int i) {
		if (i > 0) {
			am.setStreamMute(STREAM_TYPE, false);
		}
		int max = am.getStreamMaxVolume(STREAM_TYPE);
		int current = am.getStreamVolume(STREAM_TYPE);
		current += i;
		current = Math.max(0, Math.min(max, current));
		am.setStreamMute(STREAM_TYPE, current == 0);
		am.setStreamVolume(STREAM_TYPE, current, 0);
		vol.setMax(max);
		vol.setProgress(current);
		volValue.setText("" + current);
		volPanel.setVisibility(View.VISIBLE);
		resetHide();
		updateMuteUI();
	}

	private void toggleMute() {
		boolean isMute = SysUtils.isStreamMute(am, STREAM_TYPE);
		am.setStreamMute(STREAM_TYPE, !isMute);
		if (isMute) {
			changeVolume(0);
		}
		updateMuteUI();
	}

	void resetHide() {
		if (volPanel.getVisibility() == View.VISIBLE) {
			mHandler.removeMessages(MSG_HIDE_VOL_PANEL);
			mHandler.sendEmptyMessageDelayed(MSG_HIDE_VOL_PANEL, AUTO_HIDE_DELAY);
		}
	}

	static final int MSG_HIDE_VOL_PANEL = 1;
	Handler mHandler = new Handler(Looper.getMainLooper()) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_HIDE_VOL_PANEL:
				volPanel.setVisibility(View.GONE);
				break;

			default:
				break;
			}
		}

	};
}
