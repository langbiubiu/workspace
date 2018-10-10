package com.ipanel.join.cq.vod.player;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;

import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.utils.TimeUtility;

public class HistoryFragment extends BaseFragment {
	TextView yes_btn,no_btn;
	ImageView lastview;//上次视图
	TextView tvName;//电视名称
	SeekImageWorker mSeekWorker;
	SimplePlayerActivity activity;
	
	public static final SimpleDateFormat FMT_FOLDER = new SimpleDateFormat("yyyyMMdd");
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup root) {
		activity = (SimplePlayerActivity) root.getContext();
		ViewGroup container = (ViewGroup) (inflater.inflate(
				R.layout.vod_history_fragment_layout, root, false));
		
		TextView tv = (TextView) container.findViewById(R.id.vod_continue_text);
//		tv.setText(MessageFormat.format(getString(R.string.vod_history_msg1),
//				TimeUtility.formatTime(activity.historyTime/1000)));

		yes_btn = (TextView) container.findViewById(R.id.btn_continue);
		no_btn = (TextView) container.findViewById(R.id.btn_restart);
		tvName = (TextView) container.findViewById(R.id.vod_tvname);
//		tvName.setText(activity.name);
		lastview = (ImageView)container.findViewById(R.id.img);//上次画面
		mSeekWorker = new SeekImageWorker(lastview);
		
		yes_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				activity.startPreparePlay();
			}
		});
		
		no_btn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				activity.historyTime = 0;
				activity.startPreparePlay();
			}
		});
		return container;
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void showFragment() {
		yes_btn.requestFocus();
		//加载预览图
		String str = "";
//		if (activity.playType.equals("4")) {
//			if (activity.tvplayDetail != null) {
//				long tt = activity.tvplayDetail.start_time+activity.tvplayDetail.off_time;
//				str = activity.tvplayDetail.iframe_url
//						+ FMT_FOLDER.format(new Date(tt * 1000)) + "/" + tt + ".jpg";
//			}
//		} else {
//			if(activity.movieDetail != null){
//				str = activity.movieDetail.iframe_url + activity.movieDetail.off_time + ".jpg";
//			}
//		}
		Log.i("SEEK", "seek image url == " + str);
		lastview.setVisibility(View.VISIBLE);
		mSeekWorker.load(str);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE){
//			getActivity().moveTaskToBack(true);
			getActivity().finish();
			return true;
		}
		if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT||keyCode==KeyEvent.KEYCODE_DPAD_RIGHT
				||keyCode==KeyEvent.KEYCODE_DPAD_DOWN||keyCode==KeyEvent.KEYCODE_DPAD_UP){
			changeFocus(keyCode);
			return true;
		}
		return super.onKeyDown(keyCode,event);
	}

}
