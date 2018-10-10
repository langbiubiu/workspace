package com.ipanel.join.cq.back;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.back.TVAddPopupWindow.ButtonClickListener;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;

public class BackActivity extends BaseActivity {
	public static final String TAG = BackActivity.class.getSimpleName();
	private FragmentManager fm;
	private FragmentTransaction transaction;
	private ChannelFrament chFrament;//频道列表
	private ProgramFragment pFragment;//节目列表
	public static final int FRAGMENT_CHANNEL = 0;
	public static final int FRAGMENT_PROGRAM = 1;
	private TextView secondTitle;
	private View mRootView,mMainView;
	private View currentFocusView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.back_main_layout);
		fm = getFragmentManager();
		initViews();
		volPanel = new VolumePanel(this);
		showProgramFragment(FRAGMENT_CHANNEL);
	}

	public void showProgramFragment(int id) {
		switch (id) {
		case FRAGMENT_CHANNEL:
			transaction = fm.beginTransaction();
			if(chFrament == null){
				chFrament = new ChannelFrament();
			}
			transaction.add(R.id.framelayout, chFrament);
			break;
		default:
			break;
		}
		transaction.commit();
	}
	
	public void showProgramFragment(String channelId) {
		transaction = fm.beginTransaction();
		if(chFrament!=null){
			transaction.hide(chFrament);
		}
		pFragment = new ProgramFragment(channelId);
		transaction.add(R.id.framelayout, pFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}

	private void initViews() {
		mRootView = LayoutInflater.from(BackActivity.this).inflate(
				R.layout.btv_tvadd_layout, null, false);
		mMainView = findViewById(R.id.back_main);
		secondTitle = (TextView)findViewById(R.id.back_secondtitle);
		LinearLayout search_button = (LinearLayout)findViewById(R.id.search_button);
		search_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				HWDataManager.openSearchPage(getBaseContext());
			}
		});
	}
	
	public void setSecondTitle(String msg){
		secondTitle.setText(msg);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		keyCode = RcKeyEvent.getRcKeyCode(event);
		if(event.getAction()==KeyEvent.ACTION_DOWN){
			if(keyCode==KeyEvent.KEYCODE_MENU||keyCode==RcKeyEvent.KEYCODE_TV_ADD){
				hideFocus();
				TVAddPopupWindow window = new TVAddPopupWindow(BackActivity.this, new ButtonClickListener() {
					
					@Override
					public void onRecommend() {
						//推荐
						showRecommend();
					}
				});
				PopupWindow popupWindow = window.getPop();
				popupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
				popupWindow.update(0, 0, 1920, 205);
				popupWindow.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss() {
						currentFocusView.requestFocus();
						findViewById(R.id.back_title).setFocusable(false);
					}
				});
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void showRecommend() {
		hideFocus();
		RecommendWindow window = new RecommendWindow(BackActivity.this);
		PopupWindow popupWindow = window.getPop();
		popupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
		popupWindow.update(0, 0, 1920, 439);
		popupWindow.setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				currentFocusView.requestFocus();
				findViewById(R.id.back_title).setFocusable(false);
			}
		});
	}
	//隐藏ListView的焦点
	private void hideFocus() {
		currentFocusView = mMainView.findFocus();
		findViewById(R.id.back_title).setFocusable(true);
		findViewById(R.id.back_title).requestFocus();
	}
}
