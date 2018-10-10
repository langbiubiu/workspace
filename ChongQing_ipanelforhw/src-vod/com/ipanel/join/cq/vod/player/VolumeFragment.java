package com.ipanel.join.cq.vod.player;

import android.content.Context;
import android.media.AudioManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;

public class VolumeFragment extends BaseFragment {
	private TextView volume_value;
	private SeekBar volume_progress;
	
//	ImageAdView ad;
	
	AudioManager mAudioManager;
	
	private int MAX_VOLUME = 100;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup container = (ViewGroup) (inflater.inflate(
				R.layout.live_frag_homed_volume, root, false));
		volume_progress = (SeekBar) container
				.findViewById(R.id.volume_progress);
		volume_value = (TextView) container.findViewById(R.id.volume_value);
//		ad = (ImageAdView) container.findViewById(R.id.ad);
		mAudioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
		return container;
	}
	
	@Override
	public void showFragment() {
		
//		volume_progress.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
		volume_progress.setMax(MAX_VOLUME);
//		getLiveActivity().getADManager().onShowAD(ADManager.AD_FOR_VOLOME, ad);
//		String uri="content://com.ipanel.join.admanager.AdInfoProvider/ad/?adId=%s";
//		ad.setAdUri(String.format(uri, 112+""));
		super.showFragment();
	}
	
	
	@Override
	public void refreshFragment() {
		super.refreshFragment();
		
		int value = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		int max = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
		int show_volume = value * MAX_VOLUME / max;
		int progress = Math.min(100, show_volume);
		progress = Math.max(0, show_volume);
		volume_progress.setProgress(progress);
		volume_value.setText(progress + "");
	}
	
	@Override
	protected int getHideDelay() {
		return 2000;
	}
}
