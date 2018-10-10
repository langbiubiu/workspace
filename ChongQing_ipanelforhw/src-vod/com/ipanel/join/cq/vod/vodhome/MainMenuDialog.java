package com.ipanel.join.cq.vod.vodhome;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RelativeLayout;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.utils.Tools;
/**
 * TV+:从头看、推荐、选集看、我的、家世通 
 * @author wuhd
 *
 */
public class MainMenuDialog extends Dialog implements OnClickListener {

	private Context context;
	boolean isTeleplay;//是否电视剧
	private RelativeLayout tvShift;//从头看
	private RelativeLayout tvRecommend;//推荐
	private RelativeLayout tvChoice;//选集看
	private RelativeLayout tvMine;//我的
	private RelativeLayout tvJst;//家世通
	
	public MainMenuDialog(Context context, int theme,
			MainDialogGetDataListener mainDialogGetDataListener,
			boolean isTeleplay) {
		super(context, theme);
		this.context = context;
		this.mainDialogGetDataListener = mainDialogGetDataListener;
		this.isTeleplay = isTeleplay;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_tv_add_layout);
		tvShift = (RelativeLayout) findViewById(R.id.vod_shift_btn);
		tvShift.requestFocus();
		tvRecommend = (RelativeLayout) findViewById(R.id.vod_recommend_btn);
		tvChoice = (RelativeLayout) findViewById(R.id.vod_series_btn);
		if(isTeleplay){
			tvChoice.setVisibility(View.VISIBLE);
		}else{
			tvChoice.setVisibility(View.GONE);
		}
		tvMine = (RelativeLayout) findViewById(R.id.vod_mine_btn);
		tvJst = (RelativeLayout) findViewById(R.id.vod_jst);
		
		tvShift.setOnKeyListener(mkl);
		tvRecommend.setOnKeyListener(mkl);
		tvJst.setOnKeyListener(mkl);
		tvChoice.setOnKeyListener(mkl);
		tvMine.setOnKeyListener(mkl);

		tvShift.setOnClickListener(this);
		tvRecommend.setOnClickListener(this);
		tvJst.setOnClickListener(this);
		tvChoice.setOnClickListener(this);
		tvMine.setOnClickListener(this);
	}

	View.OnKeyListener mkl = new View.OnKeyListener() {

		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {

				keyCode = RcKeyEvent.getRcKeyCode(event);
				if (keyCode == RcKeyEvent.KEYCODE_QUIT
						|| keyCode == RcKeyEvent.KEYCODE_BACK
						|| keyCode == RcKeyEvent.KEYCODE_MENU
						|| keyCode == RcKeyEvent.KEYCODE_TV_ADD) {
					if (isShowing()) {
						dismiss();
					}
					return true;
				}
			}
			return false;
		}
	};

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.vod_shift_btn://从头看
			mainDialogGetDataListener.tvshift();
			Tools.showToastMessage(context, "从头看");
			dismiss();
			break;
		case R.id.vod_recommend_btn://推荐
			mainDialogGetDataListener.showRecommend();
			dismiss();
			break;
		case R.id.vod_series_btn://选集
			dismiss();
			mainDialogGetDataListener.anthology();
			break;
		case R.id.vod_mine_btn:// 我的
			dismiss();
//			HomedDataManager.startMineActivity(context);
			break;
		case R.id.vod_jst://家世通
			Tools.showToastMessage(context, "正在建设中");
			break;
		}
	}

	MainDialogGetDataListener mainDialogGetDataListener;

	public interface MainDialogGetDataListener {
		public void tvshift();//从头看
		public void showRecommend();//推荐
		public void anthology();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU
				|| keyCode == RcKeyEvent.KEYCODE_TV_ADD) {
			this.dismiss();
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_MUTE
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP
				|| keyCode == KeyEvent.KEYCODE_MUTE){
			//TODO　音量控制
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

}
