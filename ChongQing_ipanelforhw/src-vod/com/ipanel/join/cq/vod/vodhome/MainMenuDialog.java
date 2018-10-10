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
 * TV+:��ͷ�����Ƽ���ѡ�������ҵġ�����ͨ 
 * @author wuhd
 *
 */
public class MainMenuDialog extends Dialog implements OnClickListener {

	private Context context;
	boolean isTeleplay;//�Ƿ���Ӿ�
	private RelativeLayout tvShift;//��ͷ��
	private RelativeLayout tvRecommend;//�Ƽ�
	private RelativeLayout tvChoice;//ѡ����
	private RelativeLayout tvMine;//�ҵ�
	private RelativeLayout tvJst;//����ͨ
	
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
		case R.id.vod_shift_btn://��ͷ��
			mainDialogGetDataListener.tvshift();
			Tools.showToastMessage(context, "��ͷ��");
			dismiss();
			break;
		case R.id.vod_recommend_btn://�Ƽ�
			mainDialogGetDataListener.showRecommend();
			dismiss();
			break;
		case R.id.vod_series_btn://ѡ��
			dismiss();
			mainDialogGetDataListener.anthology();
			break;
		case R.id.vod_mine_btn:// �ҵ�
			dismiss();
//			HomedDataManager.startMineActivity(context);
			break;
		case R.id.vod_jst://����ͨ
			Tools.showToastMessage(context, "���ڽ�����");
			break;
		}
	}

	MainDialogGetDataListener mainDialogGetDataListener;

	public interface MainDialogGetDataListener {
		public void tvshift();//��ͷ��
		public void showRecommend();//�Ƽ�
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
			//TODO����������
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		return super.onKeyUp(keyCode, event);
	}

}
