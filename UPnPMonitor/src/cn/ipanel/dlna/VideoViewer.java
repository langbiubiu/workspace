package cn.ipanel.dlna;

import java.util.ArrayList;
import java.util.List;
import android.app.Activity;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import cn.ipanel.upnp.monitor.R;
import cn.ipanel.widget.MediaController;
import cn.ipanel.widget.VideoSurface;

public class VideoViewer extends Activity implements OnPreparedListener, OnErrorListener,
		OnCompletionListener, UPnPService.PlayerControlListener, View.OnClickListener {

	ArrayList<PlayEntry> entryList;
	int position;

	VideoSurface content;
	MediaController controller;

	TextView mVideoTitle;

	int previousPosition = -1;

	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.video_viewer);

		if (getIntent().hasExtra(PlayEntry.EXTRA_ENTRY_LIST)) {
			entryList = (ArrayList<PlayEntry>) getIntent().getSerializableExtra(
					PlayEntry.EXTRA_ENTRY_LIST);
			position = getIntent().getIntExtra(PlayEntry.EXTRA_POSITION, 0);
		} else {
			entryList = new ArrayList<PlayEntry>();
			entryList.add((PlayEntry) getIntent().getSerializableExtra(PlayEntry.EXTRA_ENTRY));
			position = 0;
		}

		mVideoTitle = (TextView) findViewById(R.id.video_title);

		PlayEntry currentEntry = entryList.get(position);
		mVideoTitle.setText(entryList.get(position).title);

		content = (VideoSurface) findViewById(R.id.video_view_video);
		content.setOnPreparedListener(this);
		content.setOnErrorListener(this);
		content.setOnCompletionListener(this);
		content.setKeepScreenOn(true);
		String format = currentEntry.format;
		if (format != null && format.startsWith("audio")) {
			// content.setZOrderOnTop(true);
			// content.getHolder().setFormat(PixelFormat.TRANSPARENT);
		} else {
			findViewById(R.id.bg).setVisibility(View.GONE);
		}

		content.setMediaController(controller = new MediaController(
				findViewById(R.id.video_controller), R.drawable.sl_play, R.drawable.sl_pause) {

			@Override
			public void hide() {
				mVideoTitle.setVisibility(View.GONE);
				super.hide();
			}

			@Override
			public void show(int timeout) {
				mVideoTitle.setVisibility(View.VISIBLE);
				super.show(timeout);
			}

		});
		content.setVideoURI(Uri.parse(currentEntry.url));

		controller.setLoadingView(findViewById(R.id.control_buffering));
		
		UPnPService.setPlayerListener(this);

		IconFont.applyTo(findViewById(R.id.control_next));
		IconFont.applyTo(findViewById(R.id.control_prev));
		
		findViewById(R.id.control_next).setOnClickListener(this);
		findViewById(R.id.control_prev).setOnClickListener(this);
	}

	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.close:
			finish();
			break;
		case R.id.control_next:
			playNext();
			break;
		case R.id.control_prev:
			playPrev();
			break;
		}
	}

	private void playPrev() {
		int pos = position-1;
		if (pos >= 0) {
			position = pos;
			playCurrentEntry();
		}

	}

	public void playNext() {
		int pos = position+1;
		if (pos < entryList.size()) {
			position = pos;
			playCurrentEntry();
		} else {
			finish();
		}
	}

	private void playCurrentEntry() {
		content.stopPlayback();
		PlayEntry currentEntry = entryList.get(position);
		mVideoTitle.setText(entryList.get(position).title);

		content.setVideoURI(Uri.parse(currentEntry.url));
		content.start();
	}

	@Override
	public void onPrepared(MediaPlayer player) {
		player.start();
		controller.show();
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		return false;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		previousPosition = -1;
		playNext();
	}

	@Override
	protected void onPause() {
		previousPosition = content.getCurrentPosition();
		content.pause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (previousPosition != -1)
			content.seekTo(previousPosition);
		content.start();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		UPnPService.clearPlayerListener(this);
		super.onDestroy();
	}

	@Override
	public void stop() {
		finish();
	}

	@Override
	public int getDuration() {
		return content.getDuration() / 1000;
	}

	@Override
	public int getPosition() {
		return content.getCurrentPosition() / 1000;
	}

	@Override
	public void seek(int position) {
		content.seekTo(position * 1000);
		controller.show();
	}

	@Override
	public void setPause(boolean pause) {
		if(pause){
			content.pause();
			controller.show(0);
		} else {
			content.start();
			controller.show();
		}
	}

}
