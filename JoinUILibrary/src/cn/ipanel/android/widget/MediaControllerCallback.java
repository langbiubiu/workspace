package cn.ipanel.android.widget;

import android.widget.MediaController.MediaPlayerControl;

public interface MediaControllerCallback {

	public void hide();

	public void setMediaPlayer(MediaPlayerControl player);

	public void setEnabled(boolean inPlaybackState);

	public void show();

	public void show(int delay);

	public boolean isShowing();

	public void onInfo(int what, int extra);
	
	public void onBuffering(int percent);
}
