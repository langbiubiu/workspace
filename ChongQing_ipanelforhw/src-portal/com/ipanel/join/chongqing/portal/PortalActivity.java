package com.ipanel.join.chongqing.portal;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Program;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.live.SharedPreferencesMenager;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.animation.Animation;
import android.view.animation.OvershootInterpolator;
import android.view.animation.Transformation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.IFrameIndicator;
import cn.ipanel.android.widget.SimpleTab;

/**
 * 首页
 * 
 * @author zhaochen
 * 
 */
public class PortalActivity extends BaseActivity implements OnGlobalFocusChangeListener, OnClickListener{
	
	private static final String TAG = PortalActivity.class.getSimpleName();
	private static final long ANIMATION_TIME = 500;
	public static boolean hasEnteredLive = false;
	public static boolean returnFromLive = false;
	public static boolean keepVideo = false;

	SimpleTab tab;

	int tabIndex = 0;

	TextView weather_text;
	TextView weather_icon;
	
	LinearLayout portal_search;
	LinearLayout tab_b0,tab_b1,tab_b2,tab_b3,tab_b4,tab_b5;
	public View convertView0,convertView1,convertView2,convertView3,convertView4,convertView5,convertView6,convertView7;
	
	FrameLayout tv_widget_small;
	RelativeLayout portal_left_menu;
//	public VideoPanelSmall tv;
	
//	TopPopupWindow topWindow;
	MinePopupWindow topWindow;
	
	IFrameIndicator mFrameIndicator;
//	cn.ipanel.android.widget.ViewFrameZoomIndicator mFrameIndicator;
	public static String DEFAULT_PROGRAM = "680";
	
	public Handler handler = new Handler();
	private View currentFocusView;
	
	VolumePanel volPanel;
//	VoiceSearchPanel voiceSearchPanel;
	
	public VerticalViewPager2 mViewPager;
	
	String last_channel_serviceid = "";
	//直播推荐
	List<TextView> liveRecsProgram = new ArrayList<TextView>();
	List<TextView> liveRecsChannel = new ArrayList<TextView>();
	List<ProgressBar> liveRecsBar = new ArrayList<ProgressBar>();
	List<ImageView> liveRecsLogo = new ArrayList<ImageView>();
	//推荐二---大家都在看
	ImageView mVodRefresh,mVodRec_09,mVodRec_10,mVodRec_11,mVodRec_12_poster,mVodRec_13_poster;
	FrameLayout mVodRecFrameL_12,mVodRecFrameL_13;
	TextView mVodRec_12_title,mVodRec_13_title;
	HomePageAdapter mAdapter;
	
	public static final int RECOMMEND_NEW_TYPE = 1;//最新榜
	public static final int RECOMMEND_HOT_TYPE = 2;//热播榜
	public static final int RECOMMEND_STAR_TYPE = 3;//明星榜
	public static final int RECOMMEND_COLLECT_TYPE = 4;//收藏榜
	int page = 1;//大家都在看-页数
	//欢网数据对象
	GetHwRequest getLiveRequest,getRecsRequest;
	GetHwResponse liveRecResponse;
	GetHwResponse recPage2Response;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LogHelper.d("PortalActivity onCreate");
		setContentView(R.layout.portal_activity_main);
		initViews();
		getWindow().getDecorView().getViewTreeObserver().
			addOnGlobalFocusChangeListener(this);
		volPanel = new VolumePanel(this);
		initLiveRecsData();
		getRecsPage2Data(page);
	}
	
	private void initLiveRecsData() {
		getLiveRequest = new GetHwRequest();
		
		getLiveRequest.setAction("GetLiveProgramsByRecommend");
		
		getLiveRequest.getDevice().setDnum("123");
		getLiveRequest.getUser().setUserid("123");
		getLiveRequest.getParam().setPage(1);
		getLiveRequest.getParam().setPagesize(2);
		
		getLiveProgramsRequest(getLiveRequest);
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

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "onResume");
		volPanel.onResume();
		if(mTabIndexChangeCallBack != null){
			mTabIndexChangeCallBack.indexCallBack(tabIndex);
		}
		controlSmallWidget(tabIndex);
		keepVideo = false;
//		tv.onResume(hasEnteredLive ? null : DEFAULT_PROGRAM, true);
//		tv.onResume(null, true);
		returnFromLive = false;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		tv.onPause(!keepVideo);
	}

	private void initViews() {
		portal_left_menu = (RelativeLayout) findViewById(R.id.portal_left_menu);
		mViewPager = (VerticalViewPager2) findViewById(R.id.portal_content_pager);
		mViewPager.setPageScrollDuration(200);
//		mViewPager.setFocusItemOffset(1);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				LogHelper.d(TAG, "onPageSelected:" + position);
				if(position > 1 && position < 5){	
					tab.switchIndex(tab.getChildAt(1));
				}else{
					tab.switchIndex(tab.getChildAt(position));
				}
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				LogHelper.d(TAG, "onPageScrolled:" + position + "--positionOffset:" + positionOffset);
				getIFrameIndicator().hideFrame();
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if(state == VerticalViewPager2.SCROLL_STATE_IDLE){
					getIFrameIndicator().moveFrameTo(PortalActivity.this.getCurrentFocus(), true, false);
				}else{
					getIFrameIndicator().hideFrame();
				}
			}
		});
		tab_b0=(LinearLayout) findViewById(R.id.tab_b0);
		tab_b1=(LinearLayout) findViewById(R.id.tab_b1);
		tab_b2=(LinearLayout) findViewById(R.id.tab_b2);
		tab_b3=(LinearLayout) findViewById(R.id.tab_b3);
		tab_b4=(LinearLayout) findViewById(R.id.tab_b4);
		tab_b5=(LinearLayout) findViewById(R.id.tab_b5);
		portal_search=(LinearLayout) findViewById(R.id.portal_search);
		portal_search.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
//				Intent intent = new Intent(PortalActivity.this,SearchPage.class);
//				startActivity(intent);
			}
		});
		tab_b0.setNextFocusRightId(R.id.rec_1);
		portal_search.setNextFocusRightId(R.id.rec_1);
		
		tab = (SimpleTab) findViewById(R.id.tab);
		tab.setOnTabChangeListener(new SimpleTab.OnTabChangeListener() {

			@Override
			public void onTabChange(int index, View selectedView) {
				if(index == 1){
					findViewById(R.id.portal_bottom_cover).setVisibility(View.VISIBLE);
				}else{
					findViewById(R.id.portal_bottom_cover).setVisibility(View.GONE);
				}
				mViewPager.setCurrentItem(index);
			}
		});
		mFrameIndicator = new ViewFrameZoomIndicator(this);
//		mFrameIndicator = new cn.ipanel.android.widget.ViewFrameZoomIndicator(this);
		mFrameIndicator.setAnimationTime(50);
//		mFrameIndicator.setTargetScale(1.05f);
//		mFrameIndicator.setFrameResouce(R.drawable.portal_focus_01);
//		mFrameIndicator.setFrameResouce(R.drawable.portal_focus);
		mFrameIndicator.setFrameResouce(R.drawable.portal_new_focus);
		
		tv_widget_small = (FrameLayout) findViewById(R.id.tv_widget_small);
		
//		tv = new VideoPanelSmall(tv_widget_small);
		controlSmallWidget(0);
		tab_b0.requestFocus();
		//hisi的bug，系统没把焦点给应用的window，如果应用是system uid的，可以通过instrumentation注入按键事件到系统里
//		new Thread(new Runnable(){
//			public void run(){
//				try {
//					Thread.sleep(1000);
//					Log.i("Focus", "requestFocus");
//					Instrumentation in = new Instrumentation();
//					in.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
//					in.sendKeyDownUpSync(KeyEvent.KEYCODE_DPAD_RIGHT);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//			}
//		}).start();
		
		mViewPager.setAdapter(new HomePageAdapter());
	}
	

	private void controlSmallWidget(int index) {
		if(index != 0 && tabIndex == 0){
			largeToSmall();
		}else if(index == 0){
			smallTolarge();
		}
	}


	@Override
	protected boolean useDelayedHide() {
		if(keepVideo)
			return false;
		return super.useDelayedHide();
	}
	
	private TabIndexChangeCallBack mTabIndexChangeCallBack;

	public void setmTabIndexChangeCallBack(
			TabIndexChangeCallBack mTabIndexChangeCallBack) {
		this.mTabIndexChangeCallBack = mTabIndexChangeCallBack;
	}
	
	public  interface TabIndexChangeCallBack{
		public void indexCallBack(int tbIndex);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (volPanel.onKeyDown(keyCode, event))
			return true;
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU://顶部弹出框
			Log.d(TAG, "KEYCODE_MENU");
			currentFocusView = findViewById(R.id.home).findFocus();
			findViewById(R.id.portal_logo).setFocusable(true);
			findViewById(R.id.portal_logo).requestFocus();
			mFrameIndicator.hideFrame();
			showPopWindow();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	public void setTabFocusable(boolean b){
		tab_b0.setFocusable(b);
		tab_b1.setFocusable(b);
		tab_b2.setFocusable(b);
		tab_b3.setFocusable(b);
		tab_b4.setFocusable(b);
		tab_b5.setFocusable(b);
		tab.setFocusable(b);
		if(!b)
			portal_search.setFocusable(b);
	}

	public void showPopWindow() {
		if(topWindow == null){
//			topWindow = new TopPopupWindow(this);
			topWindow = new MinePopupWindow(this,volPanel);
		}
		topWindow.getPop().setOnDismissListener(new OnDismissListener() {
			
			@Override
			public void onDismiss() {
				currentFocusView.requestFocus();
				findViewById(R.id.portal_logo).setFocusable(false);
			}
		});
		if(topWindow.isShowing())
			topWindow.hideTopView();
		else
			topWindow.showTopView(this);
	}

	@Override
	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
		Log.i(TAG, "onGlobalFocusChanged");
		if(oldFocus instanceof LinearLayout && newFocus instanceof LinearLayout){
			return;
		}
		if(oldFocus instanceof LinearLayout && !(newFocus instanceof LinearLayout)){
			portal_search.setFocusable(false);
			if(newFocus != null && newFocus.getTag() != null && newFocus.getTag().toString().equals("widget"))
				mFrameIndicator.hideFrame();
			else{	
				mFrameIndicator.moveFrameTo(newFocus, true, false);
			}
			return;
		}
		if(!(oldFocus instanceof LinearLayout) && newFocus instanceof LinearLayout){
			portal_search.setFocusable(true);
			mFrameIndicator.hideFrame();
		}
		if(!(oldFocus instanceof LinearLayout) && !(newFocus instanceof LinearLayout)){
			if(newFocus != null && newFocus.getTag() != null && newFocus.getTag().toString().equals("widget"))
				mFrameIndicator.hideFrame();
			else{	
				mFrameIndicator.moveFrameTo(newFocus, true, false);
			}
			return;
		}
	}
	
	public IFrameIndicator getIFrameIndicator(){
		return mFrameIndicator;
	}
	
	/**获取直播推荐
	 * @param requestEntity
	 */
	private void getLiveProgramsRequest(Object requestEntity) {
		
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
						if(result.getError() != null){
							Tools.showToastMessage(getApplicationContext(), result.getError().getInfo());
							return;
						}
						liveRecResponse = result;
						refreshRecViews();
					}
				});
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
						if(result.getError() != null){
							Tools.showToastMessage(getApplicationContext(), result.getError().getInfo());
							return;
						}
						recPage2Response = result;
						refreshPage2();
					}
				});
	}
	
	@Override
	public void onBackPressed() {
//		PortalActivity.keepVideo = true;
//		DisplayMetrics dm = getResources().getDisplayMetrics();
//		tv.tv.setDisplay(0, 0, dm.widthPixels, dm.heightPixels);
//		Intent intent = new Intent(this, LiveForCQHomedActivity.class);
//		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		if (!hasEnteredLive)
//			intent.putExtra("program", DEFAULT_PROGRAM);
//		intent.putExtra("noPlay", true);
//		startActivity(intent);
	}
	
	//左上角视窗 移至大窗口
	private void smallTolarge() {
//		RelativeLayout.LayoutParams params = (LayoutParams) portal_search.getLayoutParams();
//		params.topMargin = 120;
		portal_left_menu.clearAnimation();
		startAnimation(120);
		handler.removeCallbacks(largeToSmall);
		handler.removeCallbacks(smallToLarge);
		handler.post(smallToLarge);
	}

	private void startAnimation(int toTopMargin) {
		TopMarginAnimation animation = new TopMarginAnimation(toTopMargin);
		animation.setDuration(ANIMATION_TIME);
		portal_left_menu.startAnimation(animation);
	}

	//大窗口 移至左上角视窗
	private void largeToSmall() {
//		RelativeLayout.LayoutParams params1 = (LayoutParams) portal_search.getLayoutParams();
//		params1.topMargin = 275;
		portal_left_menu.clearAnimation();
		startAnimation(275);
		handler.removeCallbacks(largeToSmall);
		hideWidgetView();
		handler.postDelayed(largeToSmall,ANIMATION_TIME);
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
		RelativeLayout.LayoutParams tv_params = (LayoutParams) tv_widget_small.getLayoutParams();
		tv_params.width = 0;
		tv_params.height = 0;
		tv_params.leftMargin = 0;
		tv_params.topMargin = 0;
//		tv.updateDisplay();
	}
	//大窗口
	private void updateWidgetLargeView() {
		RelativeLayout.LayoutParams tv_params = (LayoutParams) tv_widget_small.getLayoutParams();
		tv_params.width = 760;
		tv_params.height = 430;
		tv_params.leftMargin = 328;
		tv_params.topMargin = 141;
//		tv.updateDisplay();
	}
	//小窗口
	private void updateWidgetSmallView() {
		RelativeLayout.LayoutParams tv_params = (LayoutParams) tv_widget_small.getLayoutParams();
		tv_params.width = 272;
		tv_params.height = 170;
		tv_params.leftMargin = 16;
		tv_params.topMargin = 108;
//		tv.updateDisplay();
	}
	
	OnKeyListener tabOnKeyListener = new OnKeyListener() {
		
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			if(event.getAction() == KeyEvent.ACTION_DOWN){
				switch(keyCode){
				case KeyEvent.KEYCODE_DPAD_RIGHT:
					setTabFocusable(false);
					break;
				}
			}
			return false;
		}
	};
	
	class TopMarginAnimation extends Animation{
		int target;
		int from;
		RelativeLayout.LayoutParams params;
		public TopMarginAnimation(int toTopMargin) {
			this.target = toTopMargin;
			params = (LayoutParams) portal_left_menu.getLayoutParams();
			from = params.topMargin;
		}
		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			int topmargin = (int) (from + interpolatedTime * (target - from));
			params.topMargin = topmargin;
			portal_left_menu.setLayoutParams(params);
		}
	}
	
	class HomePageAdapter extends PagerAdapter {
		
		Stack<View> cache = new Stack<View>();
		
		@Override
		public int getCount() {
			return 8;
		}

		@Override
		public float getPageWidth(int position) {
			switch(position){
			case 1:
				return (1080-70) / 1080f;
			case 2:
				return (1080-70) / 1080f;
			case 3:
				return (1080-300) / 1080f;
			}
			return 1;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			View v = (View) object;
			container.removeView(v);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			switch(position){
			case 0:
				convertView0 = LayoutInflater.from(container.getContext()).
						inflate(R.layout.portal_fragment_live, null,false);
				initPageView0(convertView0);
				container.addView(convertView0, 0);
				convertView0.setTag(position);
				return convertView0;
			case 1:
				convertView1 = LayoutInflater.from(container.getContext()).
						inflate(R.layout.portal_fragment_recommend_page1, null,false);
				initPageView1(convertView1);
				container.addView(convertView1, 0);
				convertView1.setTag(position);
				return convertView1;
			case 2:
				convertView2 = LayoutInflater.from(container.getContext()).
						inflate(R.layout.portal_fragment_recommend_page2, null,false);
				initPageView2(convertView2);
				container.addView(convertView2, 0);
				convertView2.setTag(position);
				return convertView2;
			case 3:
				convertView3 = LayoutInflater.from(container.getContext()).
						inflate(R.layout.portal_fragment_recommend_page3, null,false);
				initPageView3(convertView3);
				container.addView(convertView3, 0);
				convertView3.setTag(position);
				return convertView3;
			case 4:
				convertView4 = LayoutInflater.from(container.getContext()).
						inflate(R.layout.portal_fragment_vod, null,false);
				initPageView4(convertView4);
				container.addView(convertView4, 0);
				convertView4.setTag(position);
				return convertView4;
			case 5:
				convertView5 = LayoutInflater.from(container.getContext()).
						inflate(R.layout.portal_fragment_area, null,false);
				initPageView5(convertView5);
				container.addView(convertView5, 0);
				convertView5.setTag(position);
				return convertView5;
			case 6:
				convertView6 = LayoutInflater.from(container.getContext()).
						inflate(R.layout.portal_fragment_app, null,false);
				initPageView6(convertView6);
				container.addView(convertView6, 0);
				convertView6.setTag(position);
				return convertView6;
			case 7:
				convertView7 = LayoutInflater.from(container.getContext()).
						inflate(R.layout.portal_fragment_edu, null,false);
				initPageView7(convertView7);
				container.addView(convertView7, 0);
				convertView7.setTag(position);
				return convertView7;
			}
			return null;
		}

		@Override
		public void finishUpdate(ViewGroup container) {
			super.finishUpdate(container);
			SharedImageFetcher.getSharedFetcher(PortalActivity.this).setPauseWork(false);
		}

		@Override
		public void startUpdate(ViewGroup container) {
			super.startUpdate(container);
			SharedImageFetcher.getSharedFetcher(PortalActivity.this).setPauseWork(true);
		}

	}
	//刷新上次观看频道
	private void refreshLastChannel(int i) {
		SharedPreferencesMenager manager = SharedPreferencesMenager.getInstance(this);
		String channelName = manager.getSaveChannelName();
		last_channel_serviceid = manager.getSaveProg() + "";
		if (channelName == null) {
			channelName = "";
		}
		liveRecsProgram.get(i).setText(channelName);
	}
	//直播
	private void initPageView0(View convertView) {
		FrameLayout liveRec_1,liveRec_2,liveRec_3;
		ImageView ad2 = (ImageView) findViewById(R.id.ad2);
		liveRec_1 = (FrameLayout) findViewById(R.id.rec_1);
		liveRec_2 = (FrameLayout) findViewById(R.id.rec_2);
		liveRec_3 = (FrameLayout) findViewById(R.id.rec_3);
		ImageView recChannellogo1 = (ImageView) liveRec_1.findViewById(R.id.rec_channellogo_1);
		ImageView recChannellogo2 = (ImageView) liveRec_2.findViewById(R.id.rec_channellogo_2);
		ImageView recChannellogo3 = (ImageView) liveRec_3.findViewById(R.id.rec_channellogo_3);
		TextView recProgram1 = (TextView) liveRec_1.findViewById(R.id.rec_1_program);
		TextView recProgram2 = (TextView) liveRec_2.findViewById(R.id.rec_2_program);
		TextView recProgram3 = (TextView) liveRec_3.findViewById(R.id.rec_3_program);
		TextView recChannel1 = (TextView) liveRec_1.findViewById(R.id.rec_1_channel);
		TextView recChannel2 = (TextView) liveRec_2.findViewById(R.id.rec_2_channel);
		TextView recChannel3 = (TextView) liveRec_3.findViewById(R.id.rec_3_channel);
		ProgressBar recBar1 = (ProgressBar) liveRec_1.findViewById(R.id.rec_1_bar);
		ProgressBar recBar2 = (ProgressBar) liveRec_2.findViewById(R.id.rec_2_bar);
		ProgressBar recBar3 = (ProgressBar) liveRec_3.findViewById(R.id.rec_3_bar);
		liveRecsProgram.add(recProgram1);
		liveRecsProgram.add(recProgram2);
		liveRecsProgram.add(recProgram3);
		liveRecsChannel.add(recChannel1);
		liveRecsChannel.add(recChannel2);
		liveRecsChannel.add(recChannel3);
		liveRecsLogo.add(recChannellogo1);
		liveRecsLogo.add(recChannellogo2);
		liveRecsLogo.add(recChannellogo3);
		liveRecsBar.add(recBar1);
		liveRecsBar.add(recBar2);
		liveRecsBar.add(recBar3);
		
		findViewById(R.id.lookback).setOnClickListener(this);
		findViewById(R.id.mine).setOnClickListener(this);
		liveRec_1.setOnClickListener(this);
		liveRec_2.setOnClickListener(this);
		liveRec_3.setOnClickListener(this);
	}
	
	List<Wiki> recPage1list = new ArrayList<Wiki>();
	//推荐一
	private void initPageView1(View convertView) {
		ImageView ad,ad2;
		ImageView mVodRec_00,mVodRec_01,mVodRec_02,mVodRec_03,mVodRec_04,
		mVodRec_05,mVodRec_06,mVodRec_07,mVodRec_08;
		SharedImageFetcher.getSharedFetcher(this).setLoadingImage(R.drawable.default_poster);
		mVodRec_00 = (ImageView) convertView.findViewById(R.id.rec_00);
		mVodRec_01 = (ImageView) convertView.findViewById(R.id.rec_01);
		mVodRec_02 = (ImageView) convertView.findViewById(R.id.rec_02);
		mVodRec_03 = (ImageView) convertView.findViewById(R.id.rec_03);
		mVodRec_04 = (ImageView) convertView.findViewById(R.id.rec_04);
		mVodRec_05 = (ImageView) convertView.findViewById(R.id.rec_05);
		mVodRec_06 = (ImageView) convertView.findViewById(R.id.rec_06);
		mVodRec_07 = (ImageView) convertView.findViewById(R.id.rec_07);
		mVodRec_08 = (ImageView) convertView.findViewById(R.id.rec_08);
		ad = (ImageView) convertView.findViewById(R.id.ad);
		ad2 = (ImageView) convertView.findViewById(R.id.ad2);
		if(recPage1list != null && recPage1list.size() >= 9){
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(0).getWikiCover(), mVodRec_00);
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(1).getWikiCover(), mVodRec_01);
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(2).getWikiCover(), mVodRec_02);
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(3).getWikiCover(), mVodRec_03);
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(4).getWikiCover(), mVodRec_04);
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(5).getWikiCover(), mVodRec_05);
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(6).getWikiCover(), mVodRec_06);
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(7).getWikiCover(), mVodRec_07);
			SharedImageFetcher.getSharedFetcher(this).loadImage(recPage1list.get(8).getWikiCover(), mVodRec_08);
			mVodRec_00.setTag(recPage1list.get(0));
			mVodRec_01.setTag(recPage1list.get(1));
			mVodRec_02.setTag(recPage1list.get(2));
			mVodRec_03.setTag(recPage1list.get(3));
			mVodRec_04.setTag(recPage1list.get(4));
			mVodRec_05.setTag(recPage1list.get(5));
			mVodRec_06.setTag(recPage1list.get(6));
			mVodRec_07.setTag(recPage1list.get(7));
			mVodRec_08.setTag(recPage1list.get(8));
			mVodRec_00.setOnClickListener(this);
			mVodRec_01.setOnClickListener(this);
			mVodRec_02.setOnClickListener(this);
			mVodRec_03.setOnClickListener(this);
			mVodRec_04.setOnClickListener(this);
			mVodRec_05.setOnClickListener(this);
			mVodRec_06.setOnClickListener(this);
			mVodRec_07.setOnClickListener(this);
			mVodRec_08.setOnClickListener(this);
		}
	}
	List<Wiki> recPage2list = new ArrayList<Wiki>();
	//推荐二
	private void initPageView2(View convertView) {
		mVodRefresh = (ImageView) convertView.findViewById(R.id.refresh);
		mVodRec_09 = (ImageView) convertView.findViewById(R.id.rec_09);
		mVodRec_10 = (ImageView) convertView.findViewById(R.id.rec_10);
		mVodRec_11 = (ImageView) convertView.findViewById(R.id.rec_11);
		mVodRecFrameL_12 = (FrameLayout) convertView.findViewById(R.id.rec_12);
		mVodRecFrameL_13 = (FrameLayout) convertView.findViewById(R.id.rec_13);
		mVodRec_12_poster = (ImageView) convertView.findViewById(R.id.rec_12_poster);
		mVodRec_13_poster = (ImageView) convertView.findViewById(R.id.rec_13_poster);
		mVodRec_12_title = (TextView) convertView.findViewById(R.id.rec_12_title);
		mVodRec_13_title = (TextView) convertView.findViewById(R.id.rec_13_title);
		mVodRec_09.setOnClickListener(this);
		mVodRec_10.setOnClickListener(this);
		mVodRec_11.setOnClickListener(this);
		mVodRecFrameL_12.setOnClickListener(this);
		mVodRecFrameL_13.setOnClickListener(this);
		mVodRefresh.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				getRecsPage2Data(page++);
			}
		});
	}
	//推荐三
	private void initPageView3(View convertView) {
		//推荐三---排行榜
		FrameLayout mVodRecFrameL_14,mVodRecFrameL_15,mVodRecFrameL_16,mVodRecFrameL_17;
		mVodRecFrameL_14 = (FrameLayout) convertView.findViewById(R.id.rec_14);
		mVodRecFrameL_15 = (FrameLayout) convertView.findViewById(R.id.rec_15);
		mVodRecFrameL_16 = (FrameLayout) convertView.findViewById(R.id.rec_16);
		mVodRecFrameL_17 = (FrameLayout) convertView.findViewById(R.id.rec_17);
		mVodRecFrameL_14.setOnClickListener(this);
		mVodRecFrameL_15.setOnClickListener(this);
		mVodRecFrameL_16.setOnClickListener(this);
		mVodRecFrameL_17.setOnClickListener(this);
	}
	//点播
	private void initPageView4(View convertView) {
		ImageView mVodChongQing,mVodMovie,mVodTVPlay,mVodAllcq,mVodSpecial,mVod4K,
		mVodNews,mVodChildren,mVodRecord,mVodFreeArea,mVod3D,mVodDolby,
		mVodEntertainment,mVodSports,mVodColumns,mVodLife;
		ImageView vod_ad,vod_ad2;
		vod_ad = (ImageView) findViewById(R.id.vod_ad);
		vod_ad2 = (ImageView) findViewById(R.id.vod_ad2);
		mVodChongQing = (ImageView) findViewById(R.id.vod_chongqing);
		mVodMovie = (ImageView) findViewById(R.id.vod_movie);
		mVodTVPlay = (ImageView) findViewById(R.id.vod_tvplay);
		mVodAllcq = (ImageView) findViewById(R.id.vod_all_cq);
		mVodSpecial = (ImageView) findViewById(R.id.vod_special);
		mVod4K = (ImageView) findViewById(R.id.vod_4K);
		mVodNews = (ImageView) findViewById(R.id.vod_news);
		mVodChildren = (ImageView) findViewById(R.id.vod_children);
		mVodRecord = (ImageView) findViewById(R.id.vod_record);
		mVodFreeArea = (ImageView) findViewById(R.id.vod_free_area);
		mVod3D = (ImageView) findViewById(R.id.vod_3D);
		mVodDolby = (ImageView) findViewById(R.id.vod_dolby);
		mVodEntertainment = (ImageView) findViewById(R.id.vod_entertainment);
		mVodSports = (ImageView) findViewById(R.id.vod_sports);
		mVodColumns = (ImageView) findViewById(R.id.vod_columns);
		mVodLife = (ImageView) findViewById(R.id.vod_life); 
		
		mVodChongQing.setOnClickListener(this);
		mVodAllcq.setOnClickListener(this);
		mVodSpecial.setOnClickListener(this);
		mVod4K.setOnClickListener(this);
		mVodFreeArea.setOnClickListener(this);
		mVod3D.setOnClickListener(this);
		mVodDolby.setOnClickListener(this);
		mVodColumns.setOnClickListener(this);
		mVodLife.setOnClickListener(this);
		
		mVodMovie.setOnClickListener(this);
		mVodTVPlay.setOnClickListener(this);
		mVodNews.setOnClickListener(this);
		mVodChildren.setOnClickListener(this);
		mVodRecord.setOnClickListener(this);
		mVodEntertainment.setOnClickListener(this);
		mVodSports.setOnClickListener(this);
//		mVodMovie.setTag(HomedDataManager.VOD_RECOMMEND_TYPEID);
//		mVodTVPlay.setTag(HomedDataManager.TVPLAY_TYPEID);
//		mVodNews.setTag(HomedDataManager.NEWS_TYPEID);
//		mVodChildren.setTag(HomedDataManager.CHILDREN_TYPEID);
//		mVodRecord.setTag(HomedDataManager.RECORD_TYPEID);
//		mVodEntertainment.setTag(HomedDataManager.ENTERTAINMENT_TYPEID);
//		mVodSports.setTag(HomedDataManager.SPORTS_TYPEID);
	}
	//专区
	private void initPageView5(View convertView) {
		FrameLayout mAreaCtv,mAreaDrama,mAreaDance,mAreaHollywood,mAreaFenghuang,mAreaTvb,mAreaKorean,mAreaSofa;
		ImageView area_ad,area_ad2;
		area_ad = (ImageView) findViewById(R.id.area_ad);
		area_ad2 = (ImageView) findViewById(R.id.area_ad2);
		mAreaCtv = (FrameLayout) findViewById(R.id.area_ctv);
		mAreaDrama = (FrameLayout) findViewById(R.id.area_drama);
		mAreaDance = (FrameLayout) findViewById(R.id.area_dance);
		mAreaHollywood = (FrameLayout) findViewById(R.id.area_hollywood);
		mAreaFenghuang = (FrameLayout) findViewById(R.id.area_fenghuang);
		mAreaTvb = (FrameLayout) findViewById(R.id.area_tvb);
		mAreaKorean = (FrameLayout) findViewById(R.id.area_korean);
		mAreaSofa = (FrameLayout) findViewById(R.id.area_sofa);
		
		mAreaCtv.setOnClickListener(this);
		mAreaDrama.setOnClickListener(this);
		mAreaDance.setOnClickListener(this);
		mAreaHollywood.setOnClickListener(this);
		mAreaFenghuang.setOnClickListener(this);
		mAreaTvb.setOnClickListener(this);
		mAreaKorean.setOnClickListener(this);
		mAreaSofa.setOnClickListener(this);
		
		mAreaCtv.setNextFocusDownId(R.id.area_fenghuang);
		mAreaFenghuang.setNextFocusDownId(R.id.area_tvb);
		mAreaFenghuang.setNextFocusUpId(R.id.area_ctv);
		mAreaTvb.setNextFocusUpId(R.id.area_fenghuang);
	}
	//应用
	private void initPageView6(View convertView) {
		ImageView mAppAd,mAppComic,mAppFinance,mAppGame,mAppHealthy,mAppEntertainment,
		mAppTech,mAppTVLibrary,mAppRec1,mAppRec2,mAppTotal,mAppRec3,mAppRec4,mAppRec5,mAppRec6;
		mAppAd = (ImageView) findViewById(R.id.app_ad);
		mAppComic = (ImageView) findViewById(R.id.app_comic);
		mAppFinance = (ImageView) findViewById(R.id.app_finance);
		mAppGame = (ImageView) findViewById(R.id.app_game);
		mAppHealthy = (ImageView) findViewById(R.id.app_healthy);
		mAppEntertainment = (ImageView) findViewById(R.id.app_entertainment);
		mAppTech = (ImageView) findViewById(R.id.app_tech);
		mAppTVLibrary = (ImageView) findViewById(R.id.app_TVlibrary);
		mAppRec1 = (ImageView) findViewById(R.id.app_rec1);
		mAppRec2 = (ImageView) findViewById(R.id.app_rec2);
		mAppTotal = (ImageView) findViewById(R.id.app_total);
		mAppRec3 = (ImageView) findViewById(R.id.app_rec3);
		mAppRec4 = (ImageView) findViewById(R.id.app_rec4);
		mAppRec5 = (ImageView) findViewById(R.id.app_rec5);
		mAppRec6 = (ImageView) findViewById(R.id.app_rec6);
		
		mAppAd.setOnClickListener(this);
		mAppComic.setOnClickListener(this);
		mAppFinance.setOnClickListener(this);
		mAppGame.setOnClickListener(this);
		mAppHealthy.setOnClickListener(this);
		mAppEntertainment.setOnClickListener(this);
		mAppTVLibrary.setOnClickListener(this);
		mAppRec1.setOnClickListener(this);
		mAppRec2.setOnClickListener(this);
		mAppTotal.setOnClickListener(this);
		mAppRec3.setOnClickListener(this);
		mAppRec4.setOnClickListener(this);
		mAppRec5.setOnClickListener(this);
		mAppRec6.setOnClickListener(this);
	}
	//党教
	private void initPageView7(View convertView) {
		ImageView mPartyEduImg;
		ImageView edu_ad;
		edu_ad = (ImageView) findViewById(R.id.edu_ad);
		mPartyEduImg = (ImageView) findViewById(R.id.distance_edu);
		mPartyEduImg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Tools.showToastMessage(PortalActivity.this, getResources().getString(R.string.is_developing));
			}
		});
	}
	
	List<Program> liveList = new ArrayList<GetHwResponse.Program>();
	SimpleDateFormat format = new SimpleDateFormat("mm:ss");
	/**
	 * 刷新直播推荐
	 */
	private void refreshRecViews() {
		liveList = liveRecResponse.getPrograms();
		if(liveList == null && liveList.size() < 2){
			Tools.showToastMessage(this, getResources().getString(R.string.no_new));
			return;
		}
		for(int i=0;i<3;i++){
			if(i==0){	//第一个推荐位为上次观看频道
				refreshLastChannel(i);
			}else{
				SharedImageFetcher.getSharedFetcher(this).
					loadImage(liveList.get(i-1).getCover(), liveRecsLogo.get(i));
				liveRecsChannel.get(i).setText(liveList.get(i-1).getChannelName());
				liveRecsProgram.get(i).setText(liveList.get(i-1).getCurName());
				try {
					long starttime = format.parse(liveList.get(i-1).getStartTime()).getTime();
					long endtime = format.parse(liveList.get(i-1).getEndTime()).getTime();
					long currenttime = System.currentTimeMillis();
					int progress = (int) (100.0f * (currenttime - starttime) / (endtime - starttime));
					liveRecsBar.get(i).setProgress(progress);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				liveRecsProgram.get(i).setTag(liveList.get(i-1));
			}
		}
	}
	
	/**
	 * 刷新点播-大家都在看
	 * @param list
	 */
	private void refreshPage2() {
		recPage2list = recPage2Response.getWikis();
		if(recPage2list == null || recPage2list.size() < 5){
			Tools.showToastMessage(PortalActivity.this, getResources().getString(R.string.no_new));
			return;
		}
		SharedImageFetcher.getSharedFetcher(this).loadImage(recPage2list.get(0).getWikiCover(), mVodRec_09);
		SharedImageFetcher.getSharedFetcher(this).loadImage(recPage2list.get(1).getWikiCover(), mVodRec_10);
		SharedImageFetcher.getSharedFetcher(this).loadImage(recPage2list.get(2).getWikiCover(), mVodRec_11);
		SharedImageFetcher.getSharedFetcher(this).loadImage(recPage2list.get(3).getWikiCover(), mVodRec_12_poster);
		SharedImageFetcher.getSharedFetcher(this).loadImage(recPage2list.get(4).getWikiCover(), mVodRec_13_poster);
		mVodRec_12_title.setText(recPage2list.get(3).getTitle());
		mVodRec_13_title.setText(recPage2list.get(4).getTitle());
		mVodRec_09.setTag(recPage2list.get(0));
		mVodRec_10.setTag(recPage2list.get(1));
		mVodRec_11.setTag(recPage2list.get(2));
		mVodRecFrameL_12.setTag(recPage2list.get(3));
		mVodRecFrameL_13.setTag(recPage2list.get(4));
	}

	@Override
	public void onClick(View v) {
		int type = -1;
		switch(v.getId()){
		case R.id.lookback://回看
			PortalDataManager.goToLookback(this);
			return;
		case R.id.mine://我的
			PortalDataManager.startMineActivity(this);
			return;
		case R.id.rec_1://直播推荐位一——当前频道
			PortalDataManager.goToLiveActivityVarServiceId(this,last_channel_serviceid);
			return;
		case R.id.rec_2://直播推荐位二
			if(liveList==null || liveList.size() == 0){
				return;
			}
			PortalDataManager.goToLiveActivityVarServiceId(this,liveList.get(0).getServiceId());
			return;
		case R.id.rec_3://直播推荐位三
			if(liveList==null || liveList.size() <= 1){
				return;
			}
			PortalDataManager.goToLiveActivityVarServiceId(this,liveList.get(1).getServiceId());
			return;
		case R.id.rec_14:
			type = RECOMMEND_NEW_TYPE;
			break;
		case R.id.rec_15:
			type = RECOMMEND_HOT_TYPE;
			break;
		case R.id.rec_16:
			type = RECOMMEND_STAR_TYPE;
			break;
		case R.id.rec_17:
			type = RECOMMEND_COLLECT_TYPE;
			break;
		}
//		if(type != -1){
//			Intent intent = new Intent(mActivity,VodRankListActivity.class);
//			intent.putExtra("type", type);
//			mActivity.startActivity(intent);
//			return;
//		}
		if(v.getTag() != null){
			try{
				Wiki wiki = (Wiki) v.getTag();
				PortalDataManager.openMovieDetail(this, wiki);
			}catch(Exception e){
				try {
					PortalDataManager.goToVodActivity(this,(Integer) v.getTag());
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
		}
	}
}
