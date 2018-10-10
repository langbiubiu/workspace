package com.ipanel.join.cq.vod.player;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ipanel.chongqing_ipanelforhw.R;

public class SoundTrackFragment extends BaseFragment {
	ImageView sound_track;
	int[] flag_res = {R.drawable.vod_icon_channel_solid,R.drawable.vod_icon_channel_left, R.drawable.vod_icon_channel_right };
	
	@Override
	public void refreshFragment() {
		super.refreshFragment();
		int track =VodPlayerManager.getInstance(getActivity()).getSoundTrackValue();
		sound_track.setImageResource(flag_res[track]);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		// TODO Auto-generated method stub
		ViewGroup container = createContainer(inflater, root,
				R.layout.vod_fragment_soundtrack);
		sound_track = (ImageView)container.findViewById(R.id.sound_track);
		return container;
	}
	@Override
	protected int getHideDelay() {
		return 3000;
	}

}
