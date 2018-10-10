package com.ipanel.join.cq.vod.utils;

import android.annotation.SuppressLint;
import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;

/**
 * 提示信息管理器
 * */
public class PromptFragment extends Fragment{
	//位置：居中
	public static final int DIRECTION_CENTER_AND_CENTER = 0;
	//位置：靠下
	public static final int DIRECTION_CENTER_AND_BOTTOM = 1;
	//位置：靠上
	public static final int DIRECTION_CENTER_AND_TOP = 2;
	//消息TextView的Tag
	public static final String TAG_MESSAGE_TEXT="msg";
	//隐藏PopWindow的消息
	private final int MESSSAGE_HIDE_POP=1;
	//Log的TAG
	private String TAG=PromptFragment.class.getSimpleName();
	//默认PopWindow的布局文件ID
	private int mDefaultLayout=-1;
	//默认的消失时间
	private int mDismissTime=3000;
	private int mStyle=-1;
	private float mDensity;
	private PopupWindow mPop;
	private View mRoot;
	private Runnable mRunnable;
	
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MESSSAGE_HIDE_POP:
				hide();
				break;
			default:
				break;
			}
		}
	};
	
	public static PromptFragment createFragemtn(View root,int layout,int style){
		PromptFragment fragment=new PromptFragment();
		fragment.init(root, layout, style);
		return fragment;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (getResources().getDisplayMetrics().heightPixels > 720) {
			mDensity = 1.5f;
		} else {
			mDensity = 1.0f;
		}
		mPop = new PopupWindow(getActivity());
		mPop.setWidth(LayoutParams.WRAP_CONTENT);  
		mPop.setHeight(LayoutParams.WRAP_CONTENT); 
		mPop.setFocusable(false);
		if(mStyle>0){
			mPop.setAnimationStyle(mStyle);
		}
		hide();
	}
	
	@Override
	public void onPause() {
		super.onPause();
		mHandler.removeMessages(MESSSAGE_HIDE_POP);
		mHandler.removeCallbacks(mRunnable);
		hide();
	}


	public PromptFragment() {
	
	}
	
	//初始化布局样式
	public void init(View root,int layout,int style){
		this.mRoot=root;
		this.mDefaultLayout=layout;
		this.mStyle=style;
	}
	//设置消失时间
	public void setDismissTime(int time){
		this.mDismissTime=time;
	}
	
	public void showDialogAndPostDelayed(String msg,Runnable runnable,long delayMillis){
		this.mRunnable=runnable;
		showDialog(mDefaultLayout, msg, DIRECTION_CENTER_AND_TOP);
		mHandler.postDelayed(mRunnable, delayMillis);
	}
	public void showDialogAndPostDelayed(int msg,Runnable runnable,long delayMillis){
		showDialogAndPostDelayed(getString(msg),runnable,delayMillis);
	}
	
	
	public void showDefaultDialog(int msgRec) {
		showDefaultDialog(getString(msgRec));
	}
	public void showDefaultDialog(String msg) {
		showDialog(mDefaultLayout, msg, DIRECTION_CENTER_AND_TOP);
	}
	public void showDefaultDialog(int msgRec, int direction) {
		showDialog(mDefaultLayout, getString(msgRec), direction);
	}
	public void showDefaultDialog(String msg, int direction) {
		showDialog(mDefaultLayout, msg, direction);
	}
	
	public void showDialog(final int viewRec, final String msg, final int direction) {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				hide();
				showDialogInteral(viewRec,msg,direction);
			}
		});
	}


	@SuppressLint("NewApi")
	public void showDialogInteral(int viewRec, String msg, int direction) {
		if(!this.isAdded()){
			LogHelper.e(TAG,String.format("can't prompt this msg %s because fragment not added to activity .",msg));
			return;
		}
		if(viewRec<=0){
			LogHelper.e(TAG,String.format("can't prompt this msg %s for bad layout : %d",msg,viewRec));
			return;
		}
		View v = View.inflate(getActivity(), viewRec, null);
		View tag = v.findViewWithTag(TAG_MESSAGE_TEXT);
		if(tag instanceof TextView){
			TextView show=(TextView) tag;
			show.setText(msg+"");
		}
		mPop.setBackgroundDrawable(new ColorDrawable(0));
		mPop.setContentView(v);
		switch (direction) {
		case DIRECTION_CENTER_AND_CENTER:
			mPop.showAtLocation(mRoot,Gravity.CENTER, 0, 0);
			break;
		case DIRECTION_CENTER_AND_BOTTOM:
			mPop.showAtLocation(mRoot,Gravity.CENTER_HORIZONTAL|Gravity.BOTTOM, 0,(int) (50*mDensity));
			break;
		case DIRECTION_CENTER_AND_TOP:
			mPop.showAtLocation(mRoot,Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, (int) (293*mDensity));
			break;
		default:
			mPop.showAtLocation(mRoot,Gravity.CENTER_HORIZONTAL|Gravity.TOP, 0, (int) (293*mDensity));
			break;
		}
		
		sendHideMessage();
	}

	public void sendHideMessage() {
		mHandler.removeMessages(MESSSAGE_HIDE_POP);
		mHandler.sendEmptyMessageDelayed(MESSSAGE_HIDE_POP, mDismissTime);
	}

	public void hide() {
		if(mPop!=null&&mPop.isShowing()){
			try{
				mPop.dismiss();
			}catch(Throwable e){
				
			}
		}
	}
}
