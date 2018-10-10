package com.ipanel.join.chongqing.portal;

import ipanel.join.configuration.BaseConfigActivity;
import ipanel.join.configuration.ConfigParser;
import ipanel.join.configuration.ConfigState;
import ipanel.join.configuration.ConfigState.ExceptionListener;
import ipanel.join.configuration.Configuration;
import ipanel.join.configuration.UnhandledActionException;
import ipanel.join.configuration.Utils;
import ipanel.join.configuration.ViewInflater;
import ipanel.join.widget.AbsLayout;
import ipanel.join.widget.IConfigView;
import ipanel.join.widget.ImgView;
import ipanel.join.widget.TxtView;
import ipanel.join.widget.VerticalPager2;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParserException;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.live.LiveForHWActivity;
import com.ipanel.join.chongqing.portal.PortalActivity.TopMarginAnimation;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vodauth.IAuthService;
import com.ipanel.join.cqhome.view.FirstChannelView;
import com.ipanel.join.cqhome.view.MarQueeTextView;
import com.ipanel.join.cqhome.view.ShowTextView;
import com.ipanel.join.cqhome.view.TvFrameLayout;
import com.ipanel.join.cqhome.view.WeatherView;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.support.v4.view.VerticalViewPager2.OnPageScrollListener;
import android.support.v4.view.VerticalViewPager2.PageTransformer;
import android.support.v4.widget.SearchViewCompat.OnCloseListenerCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.View.OnFocusChangeListener;
import android.view.ViewParent;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.Transformation;
import android.widget.AbsoluteLayout;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.RelativeLayout.LayoutParams;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.IFrameIndicator;

/**
 * 首页
 * @author zhaochen
 */
public class PortalActivity2 extends BaseConfigActivity implements OnGlobalFocusChangeListener{
	
	private static final String TAG = PortalActivity2.class.getSimpleName();
	
	private static final long ANIMATION_TIME = 500;
	
	private String defaultConfigXml = "asset:///config-1080P.xml";

	public ViewFrameZoomIndicator mFocusFrame;
	private boolean mFreezeFrame = false;
	private final static int SET_CONFIG_VIEW = 1;
	VerticalPager2 titlePager, contentPager;
	ImageView upBg,upBg2,downBg,portal_search;
	View left_menu,off_channel;
	MarQueeTextView mail;
	//推荐二---大家都在看
	ImageView mVodRefresh;
	ShowTextView mVodRec_09,mVodRec_10,mVodRec_11,mVodRec_12,mVodRec_13;
	//欢网数据对象
	GetHwRequest getLiveRequest,getRecsRequest;
//	GetHwResponse liveRecResponse;
	GetHwResponse recPage2Response;
	
	private int leftPagePosition = 0;
	private int rightPagePosition = 0;
	int recPage = 1;
	
	TvFrameLayout tv_widget;
	VideoPanelSmall tv;
	VolumePanel volPanel;
	ImageView smallTvImg;
	
	private void initHuanWangViewAndData(){
//		contentPager.post(new Runnable() {
//			@Override
//			public void run() {
				findViewByConfigId("ID_main_hot3_4").setVisibility(View.INVISIBLE);
//			}
//		});
//		mVodRefresh = (ImageView) Utils.findViewByConfigId(page2, "ID_main_hot2_1");
//		mVodRec_09 = (ShowTextView) Utils.findViewByConfigId(page2, "ID_main_hot2_2");
//		mVodRec_10 = (ShowTextView) Utils.findViewByConfigId(page2, "ID_main_hot2_3");
//		mVodRec_11 = (ShowTextView) Utils.findViewByConfigId(page2, "ID_main_hot2_4");
//		mVodRec_12 = (ShowTextView) Utils.findViewByConfigId(page2, "ID_main_hot2_5");
//		mVodRec_13 = (ShowTextView) Utils.findViewByConfigId(page2, "ID_main_hot2_6");
//		TxtView tit = (ViewGroup)(contentPager.getChildAt(2)).;
//		Tools.showToastMessage(getApplicationContext(), findViewByConfigId("ID_main_hot2_txt").getId()+"");
//		Tools.showToastMessage(getApplicationContext(), tit.getId()+"");
//		tit.setText("----");
//		tit.setVisibility(View.INVISIBLE);
//		page2.setVisibility(View.INVISIBLE);
//		contentPager.addView(page2, 5);
//		contentPager.setVisibility(View.INVISIBLE);
//		getRecsPage2Data(recPage++);
	}
	
	/**获取大家都在看
	 * @param requestEntity
	 */
	private void getRecPage2Request(Object requestEntity) {
		
		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(PortalDataManager.url);
		Log.d(TAG, PortalDataManager.gson.toJson(requestEntity));
	
		helper.callServiceAsync(getApplicationContext(), requestEntity,
				GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {					

					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						if (!success) {
							Log.i(TAG, "request detail data failed");
							return;
						}
						if (result == null) {
							Log.i(TAG, "failed to parse JSON data");
							Toast.makeText(getApplicationContext(), "failed to parse JSON data", Toast.LENGTH_LONG).show();
							return;
						}
						if(result.getError().getCode() == 0){
							recPage2Response = result;
							refreshPage2();
						}else{
							Tools.showToastMessage(getApplicationContext(), result.getError().getInfo());
						}
					}
				});
	}
	List<Wiki> recPage2list = new ArrayList<Wiki>();
	/**
	 * 刷新点播-大家都在看
	 * @param list
	 */
	private void refreshPage2() {
		LogHelper.d(TAG, "refreshPage2");
		recPage2list = recPage2Response.getWikis();
		if(recPage2list == null || recPage2list.size() < 5){
			Tools.showToastMessage(PortalActivity2.this, getResources().getString(R.string.no_new));
			return;
		}
		Wiki wiki1 = recPage2list.get(0);
		Wiki wiki2 = recPage2list.get(1);
		Wiki wiki3 = recPage2list.get(2);
		Wiki wiki4 = recPage2list.get(3);
		Wiki wiki5 = recPage2list.get(4);
		SharedImageFetcher.getSharedFetcher(this).loadImage(wiki1.getWikiCover(), mVodRec_09);
		SharedImageFetcher.getSharedFetcher(this).loadImage(wiki2.getWikiCover(), mVodRec_10);
		SharedImageFetcher.getSharedFetcher(this).loadImage(wiki3.getWikiCover(), mVodRec_11);
		SharedImageFetcher.getSharedFetcher(this).loadImage(wiki4.getWikiCover(), mVodRec_12);
		SharedImageFetcher.getSharedFetcher(this).loadImage(wiki5.getWikiCover(), mVodRec_13);
		LogHelper.d(TAG, "titles:" + wiki1.getTitle() + "," + wiki2.getTitle() + ","
				 + wiki3.getTitle() + "," + wiki4.getTitle() + "," + wiki5.getTitle() + ",");
		TxtView title1= (TxtView)(mVodRec_09.getChildAt(0));
		TxtView title2= (TxtView)(mVodRec_10.getChildAt(0));
		TxtView title3= (TxtView)(mVodRec_11.getChildAt(0));
		TxtView title4= (TxtView)(mVodRec_12.getChildAt(0));
		TxtView title5= (TxtView)(mVodRec_13.getChildAt(0));
		title1.setText(wiki1.getTitle());
		title2.setText(wiki2.getTitle());
		title3.setText(wiki3.getTitle());
		title4.setText(wiki4.getTitle());
		title5.setText(wiki5.getTitle());
		mVodRec_09.setTag(wiki1);
		mVodRec_10.setTag(wiki2);
		mVodRec_11.setTag(wiki3);
		mVodRec_12.setTag(wiki4);
		mVodRec_13.setTag(wiki5);
	}
	private void getRecsPage2Data(int page) {
		getRecsRequest = new GetHwRequest();
		
		getRecsRequest.setAction("GetWikisByHot");
		
		getRecsRequest.getDevice().setDnum("123");
		getRecsRequest.getUser().setUserid("123");
		getRecsRequest.getParam().setPage(page);
		getRecsRequest.getParam().setPagesize(5);
		
		getRecPage2Request(getRecsRequest);
	}

	public void startOtherApk(Context context, String packageChar,
			String classChar, Bundle extras) {// 跳转到其他Activity
		try {
			ComponentName componentName = new ComponentName(packageChar,
					classChar);
			Intent intent = new Intent();
			if (null != extras) {
				intent.putExtras(extras);
			}
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setComponent(componentName);
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	};
	
	public Handler mHandler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SET_CONFIG_VIEW:
//				initUI();
				break;
			}
		}
	};
//	
//	private void initUI() {
//		ConfigState.getInstance().setConfiguration(conf);
//		View root = ViewInflater.inflateView(this, null, conf.getScreen().get(0).getView());
//		ConfigState.getInstance().setFrameListener(this);
//		setContentView(root);
//	}
	private static boolean firstRun = true;
	public static final String AUTH_SERVICE_NAME = "com.ipanel.join.cq.vodauth.IAuthService";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate");
		ImageView view=new ImageView(this);
		view.setImageResource(R.drawable.portal_background);
		setContentView(view,new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
		if (firstRun) {
			try {
				Intent intent = new Intent(
						"com.ipanel.join.cq.vodauth.action.DO_AUTHENTICATION");
				startActivityForResult(intent, 0);
				
				//XMPP服务启动-微信电视
//				Intent xmppIntent = new Intent();
//				ComponentName comp = new ComponentName("com.chongqing.mobile", "com.chongqing.mobile.CQXMPPService");
//				xmppIntent.setComponent(comp);
//				xmppIntent.setAction("com.chongqing.mobile.XMPPService");
//				startService(xmppIntent);
				firstRun = false;
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
//		bindService(new Intent(AUTH_SERVICE_NAME), mAuthConnection,
//				Context.BIND_AUTO_CREATE);
		String BROADCAST_CHECK_ACTION = "com.ipanel.join.cq.vodauth.action.CHECK";
		Intent i = new Intent(BROADCAST_CHECK_ACTION);
		sendBroadcast(i);
		
//		readLocalConfig();
	}
//	IAuthService authService;
//	private ServiceConnection mAuthConnection = new ServiceConnection() {// CA获取sercice
//		public void onServiceConnected(ComponentName className, IBinder service) {
//			authService = (IAuthService.Stub.asInterface(service));
//		}
//
//		public void onServiceDisconnected(ComponentName className) {
//			authService = null;
//		}
//	};
//	private Configuration conf;
//	private void readLocalConfig() {
//		try {
//			if (this.getResources().getDisplayMetrics().widthPixels > 1280) {
//				conf = ConfigParser.sParser.parse(getAssets().open("config-1080P.xml"));
//			}else{
//				conf = ConfigParser.sParser.parse(getAssets().open("config.xml"));
//			}
//		} catch (XmlPullParserException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		mHandler.sendEmptyMessage(SET_CONFIG_VIEW);
//	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			if (freezeUpDown)
				return true;
		}
		return super.dispatchKeyEvent(event);
	}
	boolean freezeUpDown;
	OnPageScrollListener mOnPageScrollListener;
	// 初始化所有数据
	public void inItHomeAllViewAndData() {
		portal_search = (ImageView) findViewByConfigId("ID_main_search");
		upBg = (ImageView) findViewByConfigId("ID_main_up_bg");
		upBg2 = (ImageView) findViewByConfigId("ID_main_up_bg_2");
		downBg = (ImageView) findViewByConfigId("ID_main_down_bg");
		mFocusFrame = (ViewFrameZoomIndicator) getFrameIndicator();
		left_menu = findViewByConfigId("main_left_menu");
		tv_widget = (TvFrameLayout) findViewByConfigId("ID_main_tv_widget");
		titlePager = (VerticalPager2) findViewByConfigId("title_pager");
		contentPager = (VerticalPager2) findViewByConfigId("content_pager");
		smallTvImg = (ImageView) findViewByConfigId("ID_main_tv_widget_small");
		mail = (MarQueeTextView) findViewByConfigId("ID_main_new");
		volPanel = new VolumePanel(this);
		volPanel.onResume();
		if(portal_search != null)
			portal_search.setNextFocusRightId(R.id.last_channel);
		if(tv_widget != null){
			tv = new VideoPanelSmall(tv_widget);
			tv.onResume(null, true);
		}
//		requestMailData();
//		contentPager.setOffscreenPageLimit(5);
//		initHuanWangViewAndData();
		if(titlePager != null){
			titlePager.setControlFrame(false);
			titlePager.post(new Runnable() {
				
				@Override
				public void run() {
					findViewByConfigId("title_0_wrap").setSelected(true);
				}
			});
			titlePager.setOnPageChangeListener(new OnPageChangeListener() {
				@Override
				public void onPageSelected(int position) {
					switch(position){
					case 0://直播
						contentPager.setCurrentItem(0);
						break;
					case 1://推荐
						if(rightPagePosition == 3)
							break;
						contentPager.setCurrentItem(1);
						break;
					case 2://点播
						if(rightPagePosition == 5)
							break;
						contentPager.setCurrentItem(4);
						break;
					case 3://专区
						contentPager.setCurrentItem(6);
						break;
					case 4://应用
						contentPager.setCurrentItem(7);
						break;
					case 5://党员远教
						contentPager.setCurrentItem(8);
						break;
					}
					controlSmallWidget(position);
					leftPagePosition = position;
					if(position == 0)
						portal_search.setFocusable(true);
					else
						portal_search.setFocusable(false);
				}

				@Override
				public void onPageScrolled(int position, float positionOffset,
						int positionOffsetPixels) {

				}

				@Override
				public void onPageScrollStateChanged(int state) {
					if (state == VerticalPager2.SCROLL_STATE_SETTLING)
						freezeUpDown = true;
					if (state == VerticalPager2.SCROLL_STATE_IDLE)
						freezeUpDown = false;
				}
			});
		}
		if(contentPager != null){
			contentPager.post(new Runnable() {
				
				@Override
				public void run() {
					off_channel = findViewByConfigId("ID_main_1_5");
					off_channel.requestFocus();
				}
			});
			contentPager.setOnPageChangeListener(new OnPageChangeListener() {
				
				@Override
				public void onPageSelected(int position) {
					switch(position){
					case 0://直播
						titlePager.setCurrentItem(0);
						rightPagePosition = 0;
						break;
					case 1://推荐一
						rightPagePosition = 1;
						titlePager.setCurrentItem(1);
						break;
					case 2://推荐二
						rightPagePosition = 2;
						break;
					case 3://推荐三
						rightPagePosition = 3;
						titlePager.setCurrentItem(1);
						contentPager.findViewWithTag("tag_vod_txt").setVisibility(View.VISIBLE);
						break;
					case 4://点播一
						titlePager.setCurrentItem(2);
						rightPagePosition = 4;
						contentPager.findViewWithTag("tag_vod_txt").setVisibility(View.INVISIBLE);
						break;
					case 5://点播二
						rightPagePosition = 5;
						titlePager.setCurrentItem(2);
						contentPager.findViewWithTag("tag_area_txt").setVisibility(View.VISIBLE);
						break;
					case 6://专区
						titlePager.setCurrentItem(3);
						rightPagePosition = 6;
						contentPager.findViewWithTag("tag_area_txt").setVisibility(View.INVISIBLE);
						contentPager.findViewWithTag("tag_app_txt").setVisibility(View.VISIBLE);
						break;
					case 7://应用
						titlePager.setCurrentItem(4);
						rightPagePosition = 7;
						contentPager.findViewWithTag("tag_app_txt").setVisibility(View.INVISIBLE);
						break;
					case 8://党员远教
						titlePager.setCurrentItem(5);
						rightPagePosition = 8;
						break;
					}
					if(position == 0 || 
							(titlePager.getChildCount() == 5 && position == 7) ||
							(titlePager.getChildCount() == 6 && position == 8)){
						downBg.setVisibility(View.INVISIBLE);
					}else{
						downBg.setVisibility(View.VISIBLE);
					}
				}
				
				@Override
				public void onPageScrolled(int position, float positionOffset,
						int positionOffsetPixels) {
					Log.d(TAG, "contentPager onPageScrolled:" + position + "---positionOffset:" + positionOffset + "---positionOffsetPixels:" + positionOffsetPixels);
				}
				
				@Override
				public void onPageScrollStateChanged(int state) {
					Log.d(TAG, "contentPager onPageScrollStateChanged:" + state);
					if (state == VerticalPager2.SCROLL_STATE_SETTLING){
						upBg.setVisibility(View.VISIBLE);
					}else if (state == VerticalPager2.SCROLL_STATE_IDLE){
						upBg.setVisibility(View.INVISIBLE);
					}
				}
			});
		}
		if(off_channel != null)
			off_channel.requestFocus();
	}


	@Override
	protected void onResume() {
		Log.d(TAG, "onResume");
		super.onResume();
		if(tv != null){
			controlSmallWidget(rightPagePosition);
			tv.onResume(null, true);
		}
		if(volPanel != null){
			volPanel.onResume();
		}
		Intent i = new Intent(FirstChannelView.RECEIVE_DATA);
		sendBroadcast(i);
	}
	//获得消息
	private void requestMailData(){
		ServiceHelper helper = ServiceHelper.createOneHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl("http://192.168.49.49/MailSystem/getMailInfo");
		RequestParams param = new RequestParams();
		param.put("cardId", "9950000000946804");
		helper.callServiceAsync(this, param, MailData.class,
				new ResponseHandlerT<MailData>() {
					@Override
					public void onResponse(boolean success, MailData result) {
						if (success && result != null
								&& !"".equals(result.getEmergencyMailId())) {
							Log.d(TAG, "mail content:" + result.getEmergencyMailContent());
							mail.setText(result.getEmergencyMailContent());
						}
				}
		});
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(tv != null)
			tv.onPause(true);
	}
	
	private void controlSmallWidget(int index) {
		if(index != 0 && leftPagePosition == 0){
			largeToSmall();
		}else if(index == 0){
			if(leftPagePosition == 0)
				return;
			smallTolarge();
		}
	}
	//左上角视窗 移至大窗口
		private void smallTolarge() {
			if(isfirst){
				isfirst = false;
				return;
			}
			upBg2.setVisibility(View.VISIBLE);
			left_menu.clearAnimation();
			startAnimation(150,-40);
			mHandler.removeCallbacks(largeToSmall);
			mHandler.removeCallbacks(smallToLarge);
			hideWidgetView();
			mHandler.postDelayed(smallToLarge,ANIMATION_TIME);//翻页动画300毫秒完成
		}

		private void startAnimation(int toTopMargin,int height) {
			TopMarginAnimation animation = new TopMarginAnimation(toTopMargin);
			HeightAnimation imgAni = new HeightAnimation(height);
			imgAni.setDuration(ANIMATION_TIME);
			animation.setDuration(ANIMATION_TIME);
			left_menu.startAnimation(animation);
			smallTvImg.startAnimation(imgAni);
			imgAni.setAnimationListener(new AnimationListener() {
				
				@Override
				public void onAnimationStart(Animation arg0) {
					// TODO Auto-generated method stub
					smallTvImg.setVisibility(View.VISIBLE);
				}
				
				@Override
				public void onAnimationRepeat(Animation arg0) {
					// TODO Auto-generated method stub
					
				}
				
				@Override
				public void onAnimationEnd(Animation arg0) {
					// TODO Auto-generated method stub
					if(leftPagePosition == 0){
						smallTvImg.setVisibility(View.INVISIBLE);
					}
				}
			});
		}
		static boolean isfirst = true;
		//大窗口 移至左上角视窗
		private void largeToSmall() {
			isfirst = false;
			upBg2.setVisibility(View.VISIBLE);
			left_menu.clearAnimation();
			startAnimation(340,150);
			mHandler.removeCallbacks(largeToSmall);
			mHandler.removeCallbacks(smallToLarge);
			hideWidgetView();
			mHandler.postDelayed(largeToSmall,ANIMATION_TIME);
		}

		Runnable largeToSmall = new Runnable() {
			
			@Override
			public void run() {
				updateWidgetSmallView();
			}
		};
		Runnable smallToLarge = new Runnable() {
			
			@Override
			public void run() {
				updateWidgetLargeView();
			}
		};
		private void hideWidgetView(){
			AbsLayout.LayoutParams tv_params = (android.widget.AbsoluteLayout.LayoutParams) tv_widget.getLayoutParams();
			tv_params.width = 0;
			tv_params.height = 0;
			tv_params.x = 0;
			tv_params.y = 0;
//			tv.updateDisplay();
		}
		//大窗口
		private void updateWidgetLargeView() {
			AbsLayout.LayoutParams tv_params = (android.widget.AbsoluteLayout.LayoutParams) tv_widget.getLayoutParams();
			tv_params.width = 760;
			tv_params.height = 430;
			tv_params.x = 360;
			tv_params.y = 150;
			tv_widget.setLayoutParams(tv_params);
//			tv.updateDisplay();
		}
		//小窗口
		private void updateWidgetSmallView() {
			AbsLayout.LayoutParams tv_params = (android.widget.AbsoluteLayout.LayoutParams) tv_widget.getLayoutParams();
			tv_params.width = 260;
			tv_params.height = 150;
			tv_params.x = 60;
			tv_params.y = 150;
			tv_widget.setLayoutParams(tv_params);
//			tv.updateDisplay();
		}
	
	@Override
	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
		if(hasGivenParent(titlePager, newFocus)){
			mFocusFrame.hideFrame();
		}
		moveFocusFrameTo(newFocus);
	}

	public void moveFocusFrameTo(View v) {
		if (mFreezeFrame || mFocusFrame == null)
			return;

		boolean hideFrame = false;
		if (v instanceof IConfigView) {
			hideFrame = !((IConfigView) v).showFocusFrame();
		}
		if (mFocusFrame == null) {
			return;
		}
		mFocusFrame.moveFrameTo(v, true, hideFrame);
	}

	@Override
	public void freezeFrame() {
		mFreezeFrame = true;
		if (mFocusFrame != null)
			mFocusFrame.hideFrame();
	}
	private boolean hasGivenParent(View gv, View v) {
		if (gv == null || v == null) {
			return false;
		}
		while (true) {
			if (v.equals(gv)) {
				return true;
			}

			ViewParent parent = v.getParent();
			if (parent == null) {
				return false;
			}
			if (parent.equals(v.getRootView())) {
				return false;
			}
			if (parent instanceof View) {
				v = (View) parent;
			} else {
				return false;
			}
		}
	}

	@Override
	public void updateFrame() {
		mFreezeFrame = false;
		View v = getCurrentFocus();
		moveFocusFrameTo(v);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

	}

	@Override
	public void onBackPressed() {
		// super.onBackPressed();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (volPanel.onKeyDown(keyCode, event))
			return true;
		keyCode = RcKeyEvent.getRcKeyCode(event);
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
		case RcKeyEvent.KEYCODE_QUIT:
			try {
//				ComponentName com = new ComponentName(
//						"com.ipanel.chongqing_ipanelforhw",
//						"com.ipanel.join.chongqing.live.LiveForHWActivity");
				Intent i = new Intent(this,LiveForHWActivity.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				i.setComponent(com);
				PortalActivity2.this.startActivity(i);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return true;
		default:
			break;
		}
		return super.onKeyDown(keyCode, event);
	}


	public static final String URL = "http://192.168.33.75/home/home-1080P-.xml";//home的地址

	@Override
	protected String getDefaultUrl() {
		return URL;
	}

	@Override
	protected String getFallbackUrl() {
		return defaultConfigXml;
	}

	@Override
	protected Drawable getFocusDrawable() {
		return getResources().getDrawable(R.drawable.focus);
	}

	@Override
	protected void afterSetConfiguration() {
		super.afterSetConfiguration();
	}

	@Override
	protected IFrameIndicator createFrameIndicator() {
		return new ViewFrameZoomIndicator(this);
	}

	@Override
	protected void afterSetContent() {
		super.afterSetContent();
		inItHomeAllViewAndData();

	}

	class TopMarginAnimation extends Animation{
		int target;
		int from;
		AbsLayout.LayoutParams params;
		public TopMarginAnimation(int toTopMargin) {
			this.target = toTopMargin;
			params = (android.widget.AbsoluteLayout.LayoutParams) left_menu.getLayoutParams();
			from = params.y;
		}
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			int topmargin = (int) (from + interpolatedTime * (target - from));
			params.y = topmargin;
			left_menu.setLayoutParams(params);
		}
	}
	class HeightAnimation extends Animation{
		int target;
		int from;
		AbsLayout.LayoutParams params;
		public HeightAnimation(int toHeight) {
			this.target = toHeight;
			params = (android.widget.AbsoluteLayout.LayoutParams) smallTvImg.getLayoutParams();
			from = params.y;
		}
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			int height = (int) (from + interpolatedTime * (target - from));
			params.y = height;
			smallTvImg.setLayoutParams(params);
		}
	}
}
