package com.ipanel.join.chongqing.portal;


import com.ipanel.join.cqhome.view.TvFrameLayout;

import android.widget.FrameLayout;


public class VideoPanelSmall {
	public TeeveeWidgetHolder tv;
	public FrameLayout wrap;

	public VideoPanelSmall(TvFrameLayout wrap) {
		this.wrap = wrap;
		tv = new TeeveeWidgetHolder(wrap.getContext(),wrap.getFre(),wrap.getProgram());
		wrap.setFocusable(false);
		wrap.addView(tv);
	}

	public void onResume(String program, boolean play) {
		if (program != null) {
			tv.setProgram(program);
		} else {
			tv.updateLiveSmallVideoData();
		}
		tv.initRemoteView(tv.getContext());
		if(play)
			tv.playProgram();
		updateDisplay();
	}

	public void onPause(boolean stop) {
		if (tv.hostView != null && stop) {
			tv.hostView.toChannel("channelId://-1");
		}
		if(stop)
			tv.deleteWidget(false);
		else
			tv.suspendWidget();
	}

	public void updateDisplay() {
		tv.updateTvRect();
	}
}
