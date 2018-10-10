package com.ipanel.join.chongqing.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.SeekBar;

public class LiveSeekBar extends SeekBar {
	long anim_time=400;

	public LiveSeekBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		
	}
	public void setLiveProgress(int progress){
		this.clearAnimation();
		this.startAnimation(new ProgressAnimation(this.getProgress(),progress,this));
	}
	
	@Override
	public synchronized void setProgress(int progress) {
		super.setProgress(progress);
	}
	class ProgressAnimation extends Animation {
		private int start,end;
		private SeekBar bar;
		public ProgressAnimation(int start,int end,SeekBar bar){
			this.setDuration(anim_time);
			this.start=start;
			this.end=end;
			this.bar=bar;
		}

		@Override
		protected void applyTransformation(float fraction, Transformation t) {
			bar.setProgress((int) (start+(end-start)*fraction));
		}

	}
}
