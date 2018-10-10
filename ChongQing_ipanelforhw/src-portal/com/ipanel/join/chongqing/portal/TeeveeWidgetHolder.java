package com.ipanel.join.chongqing.portal;

import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;

import com.ipanel.join.chongqing.live.SharedPreferencesMenager;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.appwidget.JXTeeveeWidgetHost;
import android.appwidget.JXTeeveeWidgetHostView;
import android.appwidget.TeeveeWidgetHost;
import android.appwidget.TeeveeWidgetHostView;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.telecast.NetworkManager;
import android.os.DisplayManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class TeeveeWidgetHolder extends FrameLayout {
	static String TAG = "TVWidget";
	private static final int APPWIDGET_HOST_ID = 0x100;
	private NetworkManager networkManager;

	private static final String UUID = PortalDataManager.getDtvUUID();

	private static TeeveeWidgetHost mAppWidgetHost;
	private AppWidgetManager mAppWidgetManager;
//	public TeeveeWidgetHostView hostView;
	public JXTeeveeWidgetHostView hostView;
	private AppWidgetProviderInfo appWidgetInfo;

	private int appWidgetId;

	private String fre = "";
	public String program = "";
	public String channel;//channelId

	private ImageView mImageView;
	private View programInfoView;
	
	public TeeveeWidgetHolder(Context context, String fre, String program) {
		super(context);

		mImageView = new ImageView(context);
		addView(mImageView, android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.MATCH_PARENT);

		this.fre = fre;
		this.program = program;
		
		if (hostView != null) {
			playProgram();
		}
	}
	
	public TeeveeWidgetHolder(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public void initRemoteView(Context context) {
		networkManager = NetworkManager.getInstance(context);
		Log.d(TAG, "networkManager = " + networkManager);

		mAppWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
		Log.d(TAG, "mAppWidgetManager = " + mAppWidgetManager);
		if (mAppWidgetHost == null) {
			mAppWidgetHost = new JXTeeveeWidgetHost(context.getApplicationContext(),
					APPWIDGET_HOST_ID);
			mAppWidgetHost.startListening();
		}
		Log.d(TAG, "mAppWidgetHost = " + mAppWidgetHost);

		appWidgetId = mAppWidgetHost.allocateAppWidgetId();
		Log.d(TAG, "appWidgetId = " + appWidgetId);
		boolean b = networkManager.bindNetworkTeeveeWidgetId(UUID, appWidgetId,
				NetworkManager.PROPERTY_TEEVEE_WIDGET_SMALL);
		Log.d(TAG, "b = " + b);
		if (b) {
			appWidgetInfo = mAppWidgetManager.getAppWidgetInfo(appWidgetId);
			Log.d(TAG, "appWidgetInfo = " + appWidgetInfo + ";appWidgetId = " + appWidgetId);
			hostView = (JXTeeveeWidgetHostView) mAppWidgetHost.createView(context, appWidgetId,
					appWidgetInfo);
			hostView.setId(appWidgetId);
		}

		Log.d(TAG, "hostView = " + hostView);
		if (hostView != null) {
			addView(hostView, 0);
		}
//		this.setOnClickListener(new OnClickListener() {
//			
//			@Override
//			public void onClick(View arg0) {
//				gotoLiveActivity();
//			}
//		});
	}

	public void setProgram(String program){
		this.program = program;
	}
	
	public void updateLiveSmallVideoData() {
		Log.i(TAG, "updateLiveSmallVideoData");
//		SharedPreferencesMenager manager = SharedPreferencesMenager.getInstance(getContext());

//		int saved_program = manager.getSaveProg();
//		long frequency = manager.getSaveFreq();
//		channel = manager.getSaveChannel()+"";

//		Log.i(TAG, "saved_program:" + saved_program);
//		Log.i(TAG, "saved_channelId:" + channel);
		if (program.equals("-1")) { //默认 重庆卫视
			program = "1104";
			fre = "427000000";
		} 
//		else {
//			program = saved_program + "";
//			fre = frequency + "";
//		}
	}

	void stopWidgetPlay() {
		getContext().sendBroadcast(new Intent("com.ipanel.tvplayer.stop"));
	}

	public void fadeOut() {
		if(mImageView != null){
			mImageView.setBackgroundColor(Color.BLACK);
			mImageView.setAlpha(1f);
			mImageView.animate().alpha(0f).setDuration(300).start();
		}
	}

	@Override
	protected void onDetachedFromWindow() {
		Log.d(TAG, "onDetachedFromWindow " + appWidgetId);
		deleteWidget(true);
		super.onDetachedFromWindow();
	}

	@Override
	protected void onAttachedToWindow() {
		Log.i(TAG, "onAttachedToWindow");
		super.onAttachedToWindow();
	}

	public void setVolume(float vol) {
		if (hostView != null)
			hostView.setVolume(vol);
	}

	public void playProgram() {
		Log.d(TAG, "playProgram " + appWidgetId);
		playUri();
		if (hostView != null) {
			hostView.setVisibility(VISIBLE);
			attachePlayer();
		}
		if(mImageView != null){
			mImageView.setBackgroundDrawable(getBackground());
			mImageView.setAlpha(1f);
			mImageView.animate().alpha(0f).setDuration(800).setStartDelay(300).start();
		}
		// setVolume(((HomeActivity)mContext).getVolume(programNumber));
		autoAdjustBounds();
	}

	private void autoAdjustBounds() {
		if (hostView != null) {
			hostView.setAutoVideoBounds();
		}
	}

	private void playUri() {
		if (program != null) {
			String url = getPlayUrl();
			Log.i(TAG, url);
			if (hostView != null) {
				hostView.setVolume(0.50f);
				hostView.toChannel(url);
			}
		}
	}

	public String getPlayUrl() {
		if (program != null) {
			return "channelId://" + fre + "-" + program;//pingyao的方法
//			return "channelId://" + program;//dalian的方法
		}
		return "channelId://-1";
	}
	
	public void suspendWidget(){
		if (mAppWidgetHost != null) {
			if (hostView != null && hostView.getParent() != null){
				removeView(hostView);
			}
			mAppWidgetHost.stopListening();
		}
	}

	public void deleteWidget(boolean flag) {
		Log.d(TAG, "deleteWidget 1 " + appWidgetId);
		if (mAppWidgetHost != null) {
			if (hostView == null) {
				Log.d(TAG, "deleteWidget  hostView null ");
			} else {
				Log.d(TAG, "deleteWidget  hostView not null ");
			}
			// if(hostView != null)
			// hostView.stop();
			mAppWidgetHost.stopListening();
			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			// AppWidgetHost.deleteAllHosts();
			// mAppWidgetHost.deleteHost();
			if (hostView != null && hostView.getParent() != null)
				removeView(hostView);
			// if(flag){
			// mAppWidgetHost = null;

		}
	}

	public void attachePlayer() {
		Log.d(TAG, "attachePlayer ====");
		// if(mTeeveePlayManager!=null){
		// releasePlayer();
		// }
		// if(mTeeveePlayManager == null){
		// mTeeveePlayManager = TeeveePlayManager.createInstance(mActivity,
		// networkManager, UUID);
		// Log.d(TAG, "mTeeveePlayManager = " + mTeeveePlayManager);
		//
		// mTeeveePlayManager.setOnPlayListener(playListener);
		// mTeeveePlayManager.setCaModuleManagerListener(caListener);
		// Log.d(TAG, "attach playMnanager: " + mTeeveePlayManager.attach());
		// }
	}

	static ScheduledExecutorService mPool = Executors.newSingleThreadScheduledExecutor();
	Future<?> mTask;
	volatile boolean mReleasing = false;
	boolean mPrepared = false;

	// TeeveePlayManager.OnPlayListener playListener = new TeeveePlayManager.OnPlayListener() {
	//
	// @Override
	// public void onPlayError(int code) {
	// // TODO Auto-generated method stub
	// Log.d(TAG, "onPlayError code = " + code);
	// }
	//
	// @Override
	// public void onMessageShow(String msg, String solveURI) {
	// // TODO Auto-generated method stub
	// Log.d(TAG, "onMessageShow msg = " + msg + ";solveURI = " + solveURI);
	// }
	//
	// @Override
	// public void onMessageHide() {
	// // TODO Auto-generated method stub
	// Log.d(TAG, "onMessageHide");
	// }
	//
	// @Override
	// public void onControlChange(int state) {
	// Log.d(TAG, "onControlChange state = " + state);
	// if (state == TeeveePlayManager.OnPlayListener.CTRL_STATE_GAIN) {
	// if(mTask != null && !mTask.isDone())
	// mTask.cancel(true);
	// mTask = mPool.schedule(new Runnable(){
	// public void run(){
	// Thread.currentThread().setPriority(Thread.MIN_PRIORITY);
	// if(mReleasing || mTeeveePlayManager == null)
	// return;
	// Log.d(TAG, "p qqqqq");
	// mPrepared = mTeeveePlayManager.prepare();
	// if(mPrepared){
	// mTeeveePlayManager.setCaModuleManagerListener(caListener);
	// }
	// if (mPrepared && !mReleasing) {
	// Log.d(TAG, "p = " + mPrepared);
	// Log.d(TAG, "freqN[0] = " + fre + ";programN[0] = "
	// + program);
	// updateTvRect();
	// mTeeveePlayManager.select(fre, StreamSelector.SELECT_FLAG_FORCE, program, 0);
	// }
	// // try {
	// // Thread.sleep(300);
	// // } catch (InterruptedException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // }
	// }
	// }, 500, TimeUnit.MILLISECONDS);
	// } else if (state == TeeveePlayManager.OnPlayListener.CTRL_STATE_LOST) {
	//
	// }
	// }
	//
	// @Override
	// public void onPFProgramChange(int number) {
	// // TODO Auto-generated method stub
	//
	// }
	// };
	//
	// TeeveePlayManager.CaModuleManagerListener caListener = new
	// TeeveePlayManager.CaModuleManagerListener(){
	//
	// @Override
	// public void onCAModuleSwitched() {
	// // TODO Auto-generated method stub
	//
	// }
	//
	// @Override
	// public void onMessage(String code, String msg) {
	// // TODO Auto-generated method stub
	// Log.d(TAG,"onMessage code:" + code+" msg:" + msg);
	// if(code.equals("1010")){
	// mActivity.uiHandler.sendEmptyMessage(HomeActivity.CHANGE_MAIL_STATE);
	// }
	// }
	//
	// };

	public void updateTvRect() {
		if (hostView != null) {
			// int[] xy = new int[2];
			// hostView.getLocationInWindow(xy);
			// xy[0] += 20;
			Rect r = new Rect();
			boolean visible = hostView.getGlobalVisibleRect(r);
			visible = visible && r.width() == hostView.getWidth()
					&& r.height() == hostView.getHeight();
			Log.d(TAG,
					String.format("TV x: %d, y: %d, width: %d, height: %d", r.left, r.top,
							r.width(), r.height()));
			if (visible)
				setTVRect(r.left, r.top, r.width(), r.height());
		}

	}
	
	public void setDisplay(int x, int y, int w, int h){
		if (hostView != null)
			hostView.setVideoBounds(new Rect(x, y, w, h));
	}
	
	 DisplayManager dm = new DisplayManager();
	public void setTVRect(int x, int y, int w, int h) {
		Rect r0 = null;
		 try{
		 dm.getOutRange();
		 }catch(Throwable t){
		 //ignore
		 }
		if (r0 == null) {
			// if (mTeeveePlayManager != null)
			// mTeeveePlayManager.setVideoDisplay(x, y, w, h);
			return;
		}
		Log.d(TAG, String.format("left %d, top %d, right %d, bottom %d", r0.left, r0.top, r0.right,
				r0.bottom));
		float left = r0.left;
		float top = r0.top;
		float width = r0.width();
		float height = r0.height();
		float scale = 1f;
		 if(dm.getFmt() < 7){
		 left = left*2/3;
		 top = top*2/3;
		 width = width*2/3;
		 height = height*2/3;
		 scale = 1.5f;
		 }
		Log.d(TAG, String.format("left %f, top %f, width %f, height %f", left, top, width, height));
		float sx = (left + width) / 1280f;
		float sy = (top + height) / 720f;
		w *= sx;
		h *= sy;
		Log.d(TAG, String.format("sx %f, sy %f", sx, sy));
		x = (int) (x * sx + r0.left / scale);
		y = (int) (y * sy + r0.top / scale);
		// if (mTeeveePlayManager != null)
		// mTeeveePlayManager.setVideoDisplay(x, y, w, h);
		if (hostView != null)
			hostView.setVideoBounds(new Rect(x, y, w, h));
	}

	public void release() {
		// if(mAppWidgetHost!=null)
		// mAppWidgetHost.deleteAppWidgetId(appWidgetId);
		if (mAppWidgetHost != null) {
			if (hostView == null) {
				Log.d(TAG, "deleteWidget  hostView null ");
			} else {
				Log.d(TAG, "deleteWidget  hostView not null ");
			}
			// if(hostView != null)
			// hostView.stop();
			// mAppWidgetHost.stopListening();
			mAppWidgetHost.deleteAppWidgetId(appWidgetId);
			// mAppWidgetHost.deleteHost();
			if (hostView != null && hostView.getParent() != null)
				removeView(hostView);
			// mAppWidgetHost = null;

		}
		releasePlayer();
	}

	public void releasePlayer() {
		Log.d(TAG, "release player start");
		mReleasing = true;
		if (mTask != null && !mTask.isDone()) {
			// mTask.cancel(true);
			try {
				mTask.get();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Log.d(TAG, "release player, wait for prepare finished done");
		// if(mTeeveePlayManager != null){
		// Log.d(TAG, "Release Player");
		// mTeeveePlayManager.setOnPlayListener(null);
		// mTeeveePlayManager.stop();
		// // if(mPrepared)
		// // mTeeveePlayManager.dispose();
		// Log.d(TAG, "release player, dispose done");
		// mPrepared = false;
		// mTeeveePlayManager.detach();
		// mTeeveePlayManager = null;
		// }
		mReleasing = false;
		Log.d(TAG, "release player done");
	}
}