package com.ipanel.join.cq.vod.detail;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.support.v4.view.ViewPager2;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.SimpleTab;
import cn.ipanel.android.widget.SimpleTab.OnTabChangeListener;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Vod;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki.Info.Star;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.customview.JHListView.ListFocusChangeListener;
import com.ipanel.join.cq.vod.customview.JiShuView;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;
import com.ipanel.join.protocol.huawei.cqvod.RTSPResponse;

public class TVDetailActivity extends BaseActivity implements OnClickListener,OnGlobalFocusChangeListener {
	protected static final String TAG = TVDetailActivity.class.getSimpleName();
	private RelativeLayout collect_rl;// 收藏
	private RelativeLayout play_rl;// 播放
	private RelativeLayout desc_rl;// 剧集介绍
	private RelativeLayout alike_rl;// 相似影片
//	private RelativeLayout comment_rl;// 评论
	private RelativeLayout star_show;// 明星秀
	private ImageView tv_poster;// 海报
	private TextView tv_name;// 名称
	private TextView play_times;//播放次数
	private TextView douban_score;//豆瓣评分
	private TextView total_nums;//总集数
	private TextView update_to;//更新至
	private TextView year;//年份
	private TextView region;//地区、
	private Button goodBt;//点赞
	private Button badBt;//点衰
	private TextView description;// 剧情介绍
	private TextView series_name;//相似影片界面的电影名称
	private TextView collect_name;//选集界面的电影名称
	
	private VerticalViewPager2 mViewPager;//推荐影片
	private RelativeLayout tv_content_layout;//剧集介绍界面
	private RelativeLayout tv_recommend_layout;//相似影片界面
	private RelativeLayout collect_layout;//选集界面
	
	private LinearLayout director_ll;//导演
	private LinearLayout actor_ll;//主演
	private LinearLayout label_ll;//标签
	
	private WeightGridLayout gridview;
	private JiShuView tvCollect;//集数view
	
//	private String seriesId;// 剧集Id
	private String seriesName;// 剧集名称
	private String wikiId;//ID
	private String vodId;//华为Id
	
	private Wiki seriesInfo;//电视剧信息，通过接口获取
	private ImageFetcher mImageFetcher;
	
	public static final int UPDATE_UI = 0;
	public static final int SET_RECOMMEND = 1;
	public static final int UPDATE_PAGE = 2; 
	public static final int SET_COMMENT_DATA = 3;
	public static final int NO_COMMENT_DATA = 4;//无评论
	public static final int UPDATE_STAR_LIST = 5;//明星秀
	public static final int PERFORMTVPLAY = 6;//播放
	private List<Wiki> similarWikis;//节目列表
	private List<String> allList = new ArrayList<String>();//电视剧剧集
	private List<String> invertList = new ArrayList<String>();//逆序的列表
//	private List<Comment> list = new ArrayList<Comment>();//评论
//	private CommentListAdapter listAdapter;
//	private CollectAdapter collectAdapter;//选集的适配器
	private  CollectGridAdapter collectAdapter;
	private int currentPosition = 0;
	
	private ListView comment_listview;//评论
	private String playType;
	private TextView page;//页码
	private TextView totalPage;//总页数
	private ImageView collect_icon;//收藏或者取消收藏的图标
	private TextView collect_text;//收藏或者取消收藏
	private SimpleTab simpleTab;
	private int is_fav = 0;//是否收藏,0表示未收藏，1已收藏
	private RelativeLayout star_show_layout;//明星秀
	private VerticalViewPager2 star_pager;
	private TextView star_movie_name;
	private boolean isInverted = false;//顺序或者逆序
	private ImageView[] vod_line = new ImageView[4];
	private int[] lines = {R.id.vod_line1,R.id.vod_line2,R.id.vod_line3,R.id.vod_line4};
	private ImageView sort_icon;//排序图标
	private Wiki bean;//通过Intent传入
	private List<Star> starList = new ArrayList<Star>();//明星列表
	private int is_inter = 0;//互动，0否，1顶，2踩
	private boolean hasVodId = false;//通过华为Id
	private List<Vod> vodList;//电视剧剧集
	private GetHwResponse hwResponse;
	private BroadcastReceiver receiver;
	private int number;//播放的集数,从1开始
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case UPDATE_UI:
				updateUI(seriesInfo);
				getSimilarWikis();
				break;
			case SET_RECOMMEND:
				mViewPager.setAdapter(new VODPagerAdapter());
				refreshPage(1);
				break;
			case SET_COMMENT_DATA:
				break;
			case NO_COMMENT_DATA:
				comment_listview.setVisibility(View.INVISIBLE);
				break;
			case UPDATE_STAR_LIST:
				if(starList != null && starList.size() > 0){
					star_pager.setAdapter(new StarPagerAdapter(getBaseContext(), starList));
				}
				break;
			case PERFORMTVPLAY:
				if ("0".equals(rtspResponse.getPlayFlag())) {
					Tools.showToastMessage(TVDetailActivity.this,rtspResponse.getMessage());
				} else {
					startToTVPlay();
				}
				break;
			default:
				break;
			}
			
		}

	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_tv_detail_layout);
		mImageFetcher = SharedImageFetcher.getNewFetcher(this, 3);
		volPanel = new VolumePanel(this);
		findViewById(R.id.vod_tv_detail_layout).getViewTreeObserver().
			addOnGlobalFocusChangeListener(this);
		this.registerReceiver(receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Logger.d("已经接收到更新广播CookieString = " + intent.getExtras().getString("CookieString").toString());
				GlobalFilmData.getInstance().setCookieString(intent.getExtras().getString("CookieString"));
				String epg = intent.getExtras().getString("EPG");
				String serviceGroupId = "" + intent.getExtras().getLong("ServiceGroupId");
				String smartcard = intent.getExtras().getString("smartcard");
				String authToken = intent.getStringExtra("authToken");

				GlobalFilmData.getInstance().setEpgUrl(epg);
				GlobalFilmData.getInstance().setGroupServiceId(serviceGroupId);
				GlobalFilmData.getInstance().setCardID(smartcard);
				GlobalFilmData.getInstance().setIcState(intent.getExtras().getString("icState"));
				GlobalFilmData.getInstance().cardID=intent.getStringExtra("smartcard");
				GlobalFilmData.getInstance().uid=intent.getStringExtra("unitUserId");
				
				GlobalFilmData.getInstance().setAaa_state(intent.getStringExtra("aaa_state"));
				Logger.d("已经接收到更新广播intent.getStringExtra(unitUserId) = " + intent.getStringExtra("unitUserId"));
				GlobalFilmData.getInstance().setAuthToken(authToken);
				Logger.d("已经接收到更新广播intent.getStringExtra(authToken) = " + intent.getStringExtra("authToken"));

				GlobalFilmData.getInstance().setServicegroup(intent.getLongExtra("ServiceGroupId",0)+"");
			}
			
		}, new IntentFilter("com.ipanel.join.cq.vodauth.EPG_URL"));
		initViews();
		initControl();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getIntentData();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
	@Override
	protected void onPause() {
		super.onPause();
		setIntent(null);
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}
	//获取详情
	private void getTVDetail() {
		GetHwRequest req = HWDataManager.getHwRequest();
		req.setAction(HWDataManager.ACTION_GET_WIKI_INFO);
		req.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					showMessage(R.string.dataerror);
					return;
				}
				if(result.getError().getCode() == 0){
					hwResponse = result;
					seriesInfo = result.getWiki();//电视剧详情
					updateInterInfo(seriesInfo);//更新互动信息（收藏、点赞);
					vodList = reSort(result.getVod());
					if(vodList!=null && vodList.size()>0){
						setCollectLayout(vodList);
					}
					if(bean == null && seriesInfo != null){
						mHandler.sendEmptyMessage(UPDATE_UI);
					}
				}else{
					Tools.showToastMessage(getBaseContext(), result.getError().getInfo());
				}
			}

		});
	}
	//获取详情通过华为Id
	private void getTVDetail(String hwId) {
		Log.d(TAG, "hwid"+hwId);
		GetHwRequest req = HWDataManager.getHwRequest();
		req.setAction(HWDataManager.ACTION_GET_WIKI_BY_VODID);
		req.getParam().setId(hwId);
		req.getDeveloper().setApikey(HWDataManager.APIKEY);
		req.getDeveloper().setSecretkey(HWDataManager.SECRETKEY);
		
		serviceHelper.callServiceAsync(this, req, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					showMessage(R.string.dataerror);
					return;
				}
				if(result.getError().getCode()==0){
					hwResponse = result;
					seriesInfo = result.getWiki();//电视剧详情
					if(seriesInfo!=null)
						updateInterInfo(seriesInfo);//更新互动信息（收藏、点赞);
					vodList = reSort(result.getVod());
					wikiId = result.getWiki().getId();
					if(vodList != null && vodList.size() > 0){
						setCollectLayout(vodList);
					}
					if(bean == null && seriesInfo != null){
						mHandler.sendEmptyMessage(UPDATE_UI);
					}
				}else{
					Tools.showToastMessage(getBaseContext(), result.getError().getInfo());
				}
			}

		});
	}
	
	protected List<Vod> reSort(List<Vod> vod) {
		//欢网把高清与标清数据放到了一起，需过滤
		if(vod == null || vod.size() == 0){
			return null;
		}
		List<Vod> mViedoList = new ArrayList<Vod>();
		mViedoList.add(vod.get(0));
		for (int i = 1; i < vod.size(); i++) {
			if(!vod.get(i).getMark().equals(mViedoList.get(mViedoList.size()-1).getMark())){
				mViedoList.add(vod.get(i));
			}
		}
		return mViedoList;
	}

	//更新互动信息
	private void updateInterInfo(Wiki wiki) {
		is_fav = wiki.getInfo().getIs_fav();
		setCollectIcon();
		is_inter = wiki.getInfo().getIs_inter();
		Logger.d(TAG, "is_fav:"+is_fav+"is_inter:"+is_inter);
	}
	
	private void getIntentData() {
		if(getIntent() != null){
			seriesName = getIntent().getStringExtra("name");
			setSeriesName();
			wikiId = getIntent().getStringExtra("id");
			bean = (Wiki)getIntent().getSerializableExtra("data");
			hasVodId = getIntent().getBooleanExtra("isVodId", false);
			Logger.d(TAG, "seriesName:"+seriesName+",id:"+wikiId+",hasVodId"+hasVodId);
			if(bean != null){
				updateUI(bean);
				getSimilarWikis();//获取推荐数据
			}
			if(hasVodId){//如果是华为Id。
				vodId = wikiId;
				getTVDetail(vodId);
			}else{
				getTVDetail();
			}
		}
	}
	//刷新界面
	private void updateUI(Wiki wiki) {
		starList.clear();
		showLayoutNeeded(2);
		desc_rl.requestFocus();
		simpleTab.setCurrentIndex(1);//焦点移动到剧集介绍
		setSeriesName();
		if(wiki.getCover()!=null){//设置海报
			BaseImageFetchTask task = mImageFetcher.getBaseTask(wiki.getCover());
			mImageFetcher.setImageSize(321, 400);
			mImageFetcher.setLoadingImage(R.drawable.default_poster);
			mImageFetcher.loadImage(task, tv_poster);
		}else{
			tv_poster.setImageResource(R.drawable.default_poster);
		}
		total_nums.setText(wiki.getInfo().getEpisodes()+"集");
		description.setText(wiki.getDesc());
		play_times.setText(wiki.getInfo().getVod_num()+"次");
		douban_score.setText(wiki.getInfo().getAverage()+"");
		region.setText(wiki.getInfo().getCountry());
		year.setText(wiki.getInfo().getReleased());
		//设置导演
		List<Star> diretorList = wiki.getInfo().getDirector();
		director_ll.removeAllViews();
		starList.addAll(diretorList);
		for(int i = 0; i < diretorList.size(); i++) {
			String direct = diretorList.get(i).getTitle();
			final View convertView = LayoutInflater.from(TVDetailActivity.this).inflate(R.layout.vod_label_item, null,false);
			TextView direct_name = (TextView)convertView.findViewById(R.id.vod_label_name);
			direct_name.setText(direct);
			if(convertView!=null){
				director_ll.addView(convertView);
				LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
						.getLayoutParams();
				para.leftMargin = 28;
				convertView.requestLayout();
				convertView.setTag(diretorList.get(i).getId());
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					HWDataManager.openStarShowActivity(getBaseContext(), convertView.getTag().toString());
				}
			});
		}
		//设置主演
		actor_ll.removeAllViews();
		List<Star> actorList = wiki.getInfo().getStarring();
		starList.addAll(actorList);
		int width = 0;
		for(int i = 0 ; i < actorList.size(); i++){
			String actorName = actorList.get(i).getTitle();
			final View convertView = LayoutInflater.from(TVDetailActivity.this).inflate(R.layout.vod_label_item, null,false);
			TextView label_name = (TextView)convertView.findViewById(R.id.vod_label_name);
			label_name.setText(actorName);
			if(convertView!=null){
				int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
				int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
				convertView.measure(w, h);
				width += (convertView.getMeasuredWidth() + 28);
				Logger.d(TAG, "width:"+width+",actor_ll:"+actor_ll.getWidth());
				if(width >= 981){
					break;
				}
				actor_ll.addView(convertView);
				convertView.setTag(actorList.get(i).getId());
				LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
						.getLayoutParams();
				para.leftMargin = 28;
				convertView.requestLayout();
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					HWDataManager.openStarShowActivity(getBaseContext(), convertView.getTag().toString());
				}
			});
		}
		//设置标签
		label_ll.removeAllViews();
		List<String> tagList = wiki.getTags();
		for(int i = 0 ; i < tagList.size(); i++){
			final String labelName = tagList.get(i);
			View convertView = LayoutInflater.from(TVDetailActivity.this).inflate(R.layout.vod_label_item, null,false);
			TextView label_name = (TextView)convertView.findViewById(R.id.vod_label_name);
			label_name.setText(labelName);
			if(convertView != null){
				label_ll.addView(convertView);
				LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
						.getLayoutParams();
				para.leftMargin = 28;
				convertView.requestLayout();
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					HWDataManager.openVodTabActivity(getBaseContext(), labelName);
				}
			});
		}
		mHandler.sendEmptyMessage(UPDATE_STAR_LIST);
	}

	private void setSeriesName() {
		tv_name.setText(seriesName);
		collect_name.setText(seriesName);
		star_movie_name.setText(seriesName);
		series_name.setText(seriesName);
	}
	//显示收藏或已收藏
	private void setCollectIcon() {
		if(is_fav==1){
			collect_icon.setImageResource(R.drawable.vod_movie_like02);
			collect_text.setText(getString(R.string.collected));
		}else{
			collect_icon.setImageResource(R.drawable.vod_movielike_sl);
			collect_text.setText(getString(R.string.vod_collect));
		}
	}
	
	//初始化控件
	private void initControl() {
		collect_rl.setOnClickListener(this);
		play_rl.setOnClickListener(this);
		desc_rl.setOnClickListener(this);
		alike_rl.setOnClickListener(this);
		star_show.setOnClickListener(this);
		goodBt.setOnClickListener(this);
		badBt.setOnClickListener(this);
		
		tvCollect.setListFocusChangeListener(new ListFocusChangeListener() {
			
			@Override
			public void onFocusChange(int focus) {
				currentPosition = focus;
				int mod = allList.size() % 30;
				if (mod == 0) {
					if(isInverted){
						collectAdapter.setList(invertList.subList(focus * 30, (focus + 1) * 30));
					}else{
						collectAdapter.setList(allList.subList(focus * 30, (focus + 1) * 30));
					}
				} else {
					if (focus < allList.size() / 30) {
						if(isInverted){
							collectAdapter.setList(invertList.subList(focus * 30, (focus + 1) * 30));
						}else{
							collectAdapter.setList(allList.subList(focus * 30, (focus + 1) * 30));
						}
					} else {
						if(isInverted){
							collectAdapter.setList(invertList.subList(focus * 30, focus * 30 + mod));
						}else{
							collectAdapter.setList(allList.subList(focus * 30, focus * 30 + mod));
						}
					}
				}
			}
		});
		tvCollect.setOnFocusChangeListener(new OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				tvCollect.invalidateAll();
			}
		});
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				refreshPage(position+1);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
			
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager2.SCROLL_STATE_IDLE) {
					moveIndicatorToFocus();
					mImageFetcher.setPauseWork(false);
				} else {
					mImageFetcher.setPauseWork(true);
				}
				
			}
		});
		mViewPager.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
			@Override
			public void onGlobalFocusChanged(View oldFocus, View newFocus) {
				if (mViewPager.getScrollState() == VerticalViewPager2.SCROLL_STATE_IDLE) {
					if (newFocus instanceof FrameLayout) {
						moveIndicatorToFocus();
					}
				}
			}
		});
		simpleTab.setOnTabChangeListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChange(int index, View selectedView) {
				switch (index) {
				case 0:
					showLayoutNeeded(1);//选集
					break;
				case 1:
					//剧集介绍
					showLayoutNeeded(2);
					break;
				case 2:
					//相似影片
					showLayoutNeeded(3);
					break;
				case 3:
					//明星秀
					showLayoutNeeded(4);
					break;
				default:
					break;
				}
			}
		});
		//控制让sortIcon获取焦点
		tvCollect.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction()==KeyEvent.ACTION_DOWN){
					if(keyCode==KeyEvent.KEYCODE_DPAD_UP && currentPosition == 0){
						sort_icon.requestFocus();
					}
				}
				return false;
			}
		});
	}
	
	//刷新页码
	private void refreshPage(int position) {
		page.setText(position+"");
		totalPage.setText("/"+getTotalPage()+"页");
	}
	//判断是否逆序
	private void getPlayNum(int index) {
		int size = allList.size();
		//判断是否逆序
		if(isInverted){
			number = size - index;
		}else{
			number = index + 1;
		}
		getPlayUrl(number-1);
	}
	
	RTSPResponse rtspResponse;
	
	//获取播放地址
	private void getPlayUrl(int i) {
		Logger.d("getPlayUrl:"+i);
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
				GlobalFilmData.getInstance().getCookieString()) });
		serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()
				+ "/go_authorization.jsp");
		serviceHelper.setSerializerType(SerializerType.JSON);
		RequestParams requestParams = new RequestParams();
		requestParams.put("typeId", "-1");
		requestParams.put("playType", "11");
		requestParams.put(
				"progId",
				vodList.get(i).getId());
		requestParams.put("parentVodId", vodList.get(i).getParent_id()+"");
		requestParams.put("contentType", "0");
		requestParams.put("business", "1");
		serviceHelper.callServiceAsync(TVDetailActivity.this, requestParams,
				RTSPResponse.class, new ResponseHandlerT<RTSPResponse>() {

					@Override
					public void onResponse(boolean success,
							RTSPResponse result) {
						if (success) {
							rtspResponse = result;
							Logger.d("wuhd", String
									.format("rtspResponse: %s",
											rtspResponse.toString()));
							mHandler.sendEmptyMessage(PERFORMTVPLAY);
						}
					}
				});
	}
	
	//跳转到播放
	private void startToTVPlay() {
		Intent intent = new Intent();
		intent.putExtra("name", vodList.get(number-1).getName());
		intent.putExtra("params",
				rtspResponse.getPlayUrl().substring(
						rtspResponse.getPlayUrl().indexOf("rtsp"),
						rtspResponse.getPlayUrl().length()));
		intent.putExtra("playType", "1");
		intent.putExtra("historyTime", 0);
		intent.putExtra("hwResponse", hwResponse);
		intent.setClass(TVDetailActivity.this, SimplePlayerActivity.class);
		TVDetailActivity.this.startActivity(intent);
	}
	
	/**
	 * 播放器调用说明
	 */
	private void jumpToMediaPlayer(){
		ComponentName componentName = new ComponentName("com.ipanel.chongqing_ipanelforhw",
				"com.ipanel.join.cq.vod.player.SimplePlayerActivity");//前面为包名。后面为播放器的完整类名。
		Intent intent = new Intent();
		intent.setComponent(componentName);
		intent.putExtra("params", "");//传入播放地址，类型为字符串
		intent.putExtra("name", "");//影片名称，字符串；电视剧则传入单集名称如：琅琊榜（5）
		intent.putExtra("playType", "RTSP");//播放类型，传入RTSP。注意大写。
		startActivity(intent);
	}
	//初始化视图
	private void initViews() {
		collect_rl = (RelativeLayout) this.findViewById(R.id.tv_line1);
		play_rl = (RelativeLayout) this.findViewById(R.id.tv_line2);
		desc_rl = (RelativeLayout) this.findViewById(R.id.tv_line3);
		desc_rl.requestFocus();//第一次由剧集介绍获取焦点
		alike_rl = (RelativeLayout) this.findViewById(R.id.tv_line4);
//		comment_rl = (RelativeLayout) this.findViewById(R.id.tv_line5);
		star_show = (RelativeLayout) this.findViewById(R.id.tv_line6);
		
		tv_poster = (ImageView)this.findViewById(R.id.tv_poster);
		tv_name = (TextView)this.findViewById(R.id.tv_name);
		play_times = (TextView)this.findViewById(R.id.play_times);//播放次数
		douban_score = (TextView)this.findViewById(R.id.douban_score);//豆瓣评分
		total_nums = (TextView)this.findViewById(R.id.total_nums);//总集数
		update_to = (TextView)this.findViewById(R.id.update_to);//更新至
		year = (TextView)this.findViewById(R.id.year);//年份
		region = (TextView)this.findViewById(R.id.region);//地区、
		goodBt = (Button)this.findViewById(R.id.good_bt);//点赞
		badBt = (Button)this.findViewById(R.id.bad_bt);;//点衰
		description = (TextView)this.findViewById(R.id.description);// 剧情介绍
		
		director_ll = (LinearLayout)this.findViewById(R.id.director_ll);//导演
		actor_ll = (LinearLayout)this.findViewById(R.id.actor_ll);//主演
		label_ll = (LinearLayout)this.findViewById(R.id.tag_ll);//标签
		
		tv_content_layout = (RelativeLayout)this.findViewById(R.id.tv_intro);
		tv_recommend_layout = (RelativeLayout)this.findViewById(R.id.tv_recommend_layout);
		collect_layout = (RelativeLayout)this.findViewById(R.id.collect_layout);
		
		series_name = (TextView)this.findViewById(R.id.series_name);
		collect_name =(TextView)this.findViewById(R.id.collect_name);
		gridview = (WeightGridLayout)this.findViewById(R.id.collect_grid);
		tvCollect = (JiShuView)this.findViewById(R.id.tvcollect);
		comment_listview = (ListView)this.findViewById(R.id.comment_list);
		mViewPager = (VerticalViewPager2)this.findViewById(R.id.tv_viewpager);
		page = (TextView)findViewById(R.id.series_page);
		totalPage = (TextView)findViewById(R.id.series_totalpage);
		
		simpleTab = (SimpleTab)findViewById(R.id.tv_simple_tab);
		simpleTab.setCurrentIndex(1);//剧集介绍获取焦点
		collect_icon = (ImageView)findViewById(R.id.tv_collect);
		collect_text = (TextView)findViewById(R.id.tv_collect_text);
		
		star_show_layout = (RelativeLayout)findViewById(R.id.star_layout);
		star_movie_name = (TextView)findViewById(R.id.vod_movie_name);
		star_pager = (VerticalViewPager2)findViewById(R.id.star_viewpager);
		for (int i = 0; i < vod_line.length; i++) {
			vod_line[i] = (ImageView)findViewById(lines[i]);
		}
		sort_icon = (ImageView)findViewById(R.id.vod_sort_icon);
	}
	/**
	 * 获取相似影片
	 */
	private void getSimilarWikis() {
		Log.d(TAG, "getSimilarWikis--"+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_SIMILAR_WIKIS);
		request.getParam().setWikiId(wikiId);
		request.getParam().setPage(1);
		request.getParam().setPagesize(10);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					showMessage(R.string.dataerror);
					return;
				}
				if(result.getError().getCode()==0){
					similarWikis = result.getWikis();
					mHandler.obtainMessage(SET_RECOMMEND).sendToTarget();
				}
			}
		});
	}
	//设置选集界面
	private void setCollectLayout(List<Vod> video_list){
		update_to.setText(String.format(getResources().getString(R.string.update), video_list.size()));//更新至
		if(seriesInfo != null && video_list.size() > 0){
			allList.clear();
			invertList.clear();
			for (int i = 0; i < video_list.size(); i++) {
				allList.add(""+(i+1));
				invertList.add((video_list.size()-i)+"");//逆序
			}
			if(collectAdapter == null){
				collectAdapter = new CollectGridAdapter(TVDetailActivity.this);
			}
			if(allList.size() >= 30){
				collectAdapter.setList(allList.subList(0, 30));
			}else{
				collectAdapter.setList(allList);
			}
			tvCollect.setShow(allList.size(),30,isInverted);
			gridview.setAdapter(collectAdapter);
			showLines();
		}
	}
	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.tv_line1:
			if(is_fav==0){
				//收藏
				setFavorite();
			}else{
				//取消收藏
				cancelFavorite();
			}
			break;
		case R.id.good_bt:
			if(is_inter==0){
				setWikiInteractive(1);
			}else{
				showMessage("重复操作");
			}
			break;
		case R.id.bad_bt:
			if(is_inter==0){
				setWikiInteractive(2);
			}else{
				showMessage("重复操作");
			}
			break;
		default:
			break;
		}
	}

	private void showLayoutNeeded(int id) {
		//设置显示隐藏
		if(id == 1){
			collect_layout.setVisibility(View.VISIBLE);
		}else{
			collect_layout.setVisibility(View.INVISIBLE);
		}
		if(id == 2){
			tv_content_layout.setVisibility(View.VISIBLE);
		}else{
			tv_content_layout.setVisibility(View.INVISIBLE);
		}
		if(id == 3){
			tv_recommend_layout.setVisibility(View.VISIBLE);
		}else{
			tv_recommend_layout.setVisibility(View.INVISIBLE);
		}
		if(id == 4){
			star_show_layout.setVisibility(View.VISIBLE);
		}else{
			star_show_layout.setVisibility(View.INVISIBLE);
		}
	}
	
	/**
	 * 取消收藏
	 */
	private void cancelFavorite() {
		Logger.d("wuhd", "cancelFavorite-->"+is_fav+","+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_DEL_FAV_WIKI);
		request.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success&&result.getError().getCode()==0){
					//取消收藏成功
					showMessage(R.string.cancel_fav_succeed);
					is_fav = 0;
					setCollectIcon();
				}else{
					showMessage(R.string.cancel_fav_failed);
				}
			}
		});
	}

	/**
	 * 收藏
	 */
	private void setFavorite() {
		Logger.d("wuhd", "setFavorite-->"+is_fav+","+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_SET_FAV_WIKI_BY_USER);
		request.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success&&result.getError().getCode()==0){
					//收藏成功
					showMessage(R.string.fav_succeed);
					is_fav = 1;
					setCollectIcon();
				}else{
					showMessage(R.string.fav_failed);
				}
			}
		});
	}
	
	//点赞或者点衰:1顶， 2踩
	private void setWikiInteractive(final int act) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_SET_WIKI_INTERACTIVE);
		request.getParam().setAct(act);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(result.getError().getCode()==0){
					is_inter = act;
					if(act == 1){
						showMessage(getString(R.string.good_succeed));
					}else{
						showMessage(getString(R.string.bad_succeed));
					}
				}else{
					if(act == 1){
						showMessage(getString(R.string.good_failed));
					}else{
						showMessage(getString(R.string.bad_failed));
					}
				}
			}
		});
	}
	
	private int getTotalPage(){
		int total = similarWikis == null ? 0 : similarWikis.size();
		int totalPage = total % 5 == 0 ? total / 5 : total / 5 + 1;
		return totalPage;
	}
	
	private int getPageCount(int row){
		int total = similarWikis == null ? 0 : similarWikis.size();
		int totalPage = getTotalPage();
		if(row == totalPage - 1){
			return total % 5 == 0 ? 5 : total % 5;
		}else{
			return 5;
		}
	}
	//点击推荐影片
	private void reFreshCurrentPage(Wiki item) {
		wikiId = item.getId();
		seriesName = item.getTitle();
		bean = item;
		updateUI(bean);
		getSimilarWikis();//获取推荐数据
		getTVDetail();//获取详情
	}
	
	class VODPagerAdapter extends PagerAdapter {

		@Override
		public float getPageWidth(int position) {
			return (1 + 0.0f) / 2;  
		}

		@Override
		public int getCount() {
			return similarWikis.size() % 5 == 0 ? similarWikis.size() / 5 : similarWikis.size() / 5 + 1;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			ViewGroup v = (ViewGroup) object;
			container.removeView(v);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			long time=System.currentTimeMillis();
			View itemView = null;//getViewFromSoftReference();
			if (itemView == null) {
				LogHelper.e("inflate from layout ");
				itemView = View.inflate(getBaseContext(), R.layout.vod_hotpageritem, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.hotgrid);
			mWeightLayout.setClipToPadding(false);
			mWeightLayout.setTag(position);
			mWeightLayout.setAdapter(new VODWeightGridAdapter(position));
			itemView.setTag(position);
			container.addView(itemView);
			LogHelper.e("instantiateItem view take time : "+(System.currentTimeMillis()-time));
			return itemView;
		}
	}
	
	class VODWeightGridAdapter extends WeightGridAdapter {
		
		int row;
		public VODWeightGridAdapter(int row) {
			this.row = row;
		}

		@Override
		public int getCount() {
			return getPageCount(row);
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			int index = 5 * row + position;
			final Wiki item = similarWikis.get(index);
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplication()).inflate(R.layout.vod_hot_element3, parent, false);
			}
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			final RelativeLayout score_layout = (RelativeLayout)convertView.findViewById(R.id.score_layout);
			final TextView score = (TextView) convertView.findViewById(R.id.score_text);
			final RatingBar ratingbar = (RatingBar)convertView.findViewById(R.id.score_ratingbar);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					 reFreshCurrentPage(item);
				}
			});
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						name.setSelected(true);
						score_layout.setVisibility(View.VISIBLE);
					} else {
						name.setSelected(false);
						score_layout.setVisibility(View.INVISIBLE);
					}
				}
			});
			convertView.setTag(row + "");
			if (row * 5 + position >= similarWikis.size()) {
				convertView.setVisibility(View.GONE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}
			if (item.getCover() != null
					&& image.getVisibility() == View.VISIBLE) {
				BaseImageFetchTask task = mImageFetcher.getBaseTask(item.getCover());
				mImageFetcher.setImageSize(241, 300);
				mImageFetcher.setLoadingImage(R.drawable.default_poster);
				mImageFetcher.loadImage(task, image);
			} else {
				image.setBackgroundResource(R.drawable.default_poster);
			}
			name.setText(item.getTitle());
			float rate = 0f;
			if(item.getInfo()!=null && item.getInfo().getAverage()!=null){
				if(!TextUtils.isEmpty(item.getInfo().getAverage())){
					rate = Float.parseFloat(item.getInfo().getAverage());
				}
				score.setText(item.getInfo().getAverage());
			}else{
				score.setText("0.0");
			}
			ratingbar.setRating(rate/2.0f);//设置评分
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			return 1;
		}

		@Override
		public int getXSize() {
			return 5;
		}
		
		@Override
		public int getYSize() {
			return 1;
		}

		@Override
		public int getYSpace() {
			return 35;
		}

		@Override
		public int getXSpace() {
			return 35;
		}

	}
	
	class CollectGridAdapter extends WeightGridAdapter {
		private List<String> list;
		private LayoutInflater mInflater;
		private Context context;
		
		public CollectGridAdapter(Context context) {
			this.context = context;
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		public void setList(List<String> list){
			this.list = list;
			notifyDataSetChanged();
		}
		@Override
		public int getCount() {
			return list.size();
			
		}
		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			Holder holder;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.vod_collect_grid_item, null);
				holder = new Holder();
				holder.tvNumber = (TextView) convertView.findViewById(R.id.tv_number);
				convertView.setTag(holder);
			} else {
				holder = (Holder) convertView.getTag();
			}
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					int index = position + 30 * currentPosition;
					getPlayNum(index);
				}
			});
			String info = list.get(position);
			if (info != null) {
				holder.tvNumber.setText(info);
			}
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			return 1;
		}

		@Override
		public int getXSize() {
			return 5;
		}
		
		@Override
		public int getYSize() {
			return 6;
		}

		@Override
		public int getYSpace() {
			return 0;
		}

		@Override
		public int getXSpace() {
			return 0;
		}
		class Holder {
			TextView tvNumber;
		}
	}
	@Override
	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
		if(newFocus.getId() == R.id.tv_line2){
			collect_rl.setFocusable(true);
		}else if(newFocus.getId() != R.id.tv_line1){
			collect_rl.setFocusable(false);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	//逆序选集
	public void reSort(View v){
		//TODO选集
		if(!isInverted){
			//如果之前为正序，则逆序
			isInverted = true;
			sort_icon.setImageResource(R.drawable.vod_sort_down_sl);
			if(allList.size() >= 30){
				collectAdapter.setList(invertList.subList(0, 30));
			}else{
				collectAdapter.setList(invertList);
			}
			tvCollect.setShow(invertList.size(), 30,isInverted);
			gridview.setAdapter(collectAdapter);
		}else{
			//如果之前为逆序，则正序
			isInverted = false;
			sort_icon.setImageResource(R.drawable.vod_sort_up_sl);
			if(allList.size() >= 30){
				collectAdapter.setList(allList.subList(0, 30));
			}else{
				collectAdapter.setList(allList);
			}
			tvCollect.setShow(allList.size(), 30,isInverted);
			gridview.setAdapter(collectAdapter);
		}
	}
	//手动控制竖线的显示与隐藏
	private void showLines() {
		int tag = allList.size() % 30 == 0 ? allList.size() / 30 : allList.size() / 30 + 1;
		if(tag > 5)
			tag = 5;
		if(tag <= 1){
			for(int i = 0; i < 4;i++ ){
				vod_line[i].setVisibility(View.INVISIBLE);
			}
		}
		if(tag > 1 && tag <= 5){
			for (int i = 0; i < tag - 1; i++) {
				vod_line[i].setVisibility(View.VISIBLE);
			}
			for(int i = tag - 1; i < 4;i++ ){
				vod_line[i].setVisibility(View.INVISIBLE);
			}
		}
	}
}
