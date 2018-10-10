package com.ipanel.join.cq.vod.detail;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.content.BroadcastReceiver;
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
import android.widget.Toast;
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
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki.Info.Star;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.player.SimplePlayerActivity;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;
import com.ipanel.join.protocol.huawei.cqvod.RTSPResponse;

public class DetailActivity extends BaseActivity implements OnClickListener,OnGlobalFocusChangeListener{
	protected static final String TAG = DetailActivity.class.getSimpleName();
	private LinearLayout left_bar;// 电影
	private RelativeLayout collect;// 收藏
	private RelativeLayout play;// 播放
	private RelativeLayout alike;// 相似影片
	private RelativeLayout star_show;// 明星秀
	private VerticalViewPager2 mViewpager;
	private RelativeLayout alike_layout;//相似影片模块
	private RelativeLayout movie_content_layout;//影片介绍界面
	private ListView comment_list;//评论内容
	private TextView movie_name;//电影名称
	private TextView page;//当前页数
	private TextView total_page;//总页数
	private String name;//电影name
	private String wikiId;//wikiID
	private String vodId;//华为Id
	private List<Wiki> recommend_list = new ArrayList<Wiki>();//相似影片
	private static final int UPDATE_RECOMMEND = 0;
	private static final int SET_COMMENT = 1;//设置评论
	private static final int NO_COMMENT_DATA = 2;//无评论数据
	private static final int GOTO_MOVIEPLAY = 3;//播放电影
	private static final int UPDATE_STAR_LIST = 4;
	private static final int UPDATE_DATA = 5;
	private ImageView tv_poster;// 海报
	private TextView tv_name;// 名称
	private TextView play_times;//播放次数
	private TextView douban_score;//豆瓣评分
	private TextView update_to;//更新至
	private TextView year;//年份
	private TextView region;//地区
	private Button goodBt;//点赞
	private Button badBt;//点衰
	private TextView description;// 剧情介绍
	private LinearLayout director_ll;//导演
	private LinearLayout actor_ll;//主演
	private LinearLayout label_ll;//标签
	private ImageView collect_img;//收藏_图标
//	private List<Comment> list;//评论
//	private CommentListAdapter adapter;
	private Wiki movieDetail;//电影详情
	private ImageFetcher mImageFetcher;
	private Wiki bean;//电影列表传入
	private String playType;//播放类型
	private SimpleTab simpleTab;
	private TextView vod_collect_text;//收藏
	private int is_fav = 0;//是否为收藏节目，取值 0：否，1：是。
	private RelativeLayout star_show_layout;//明星秀
	private VerticalViewPager2 star_pager;
	private TextView star_movie_name;
	private int is_inter = 0;//互动，0否，1顶，2踩
	private List<Star> starList = new ArrayList<Star>();
	private boolean isVodId = false;
	private BroadcastReceiver receiver;
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case UPDATE_RECOMMEND:
				mViewpager.setAdapter(new VODPagerAdapter());
				refreshPage(1);
				break;
			case SET_COMMENT:
				break;
			case NO_COMMENT_DATA:
				comment_list.setVisibility(View.INVISIBLE);
				Toast.makeText(DetailActivity.this, "该节目无评论", Toast.LENGTH_SHORT).show();
				break;
			case GOTO_MOVIEPLAY:
				if(rtspResponse.getPlayFlag().equals("0")){
					Tools.showToastMessage(DetailActivity.this, rtspResponse.getMessage());
				}else{
					startToMoviePlay();
				}
				break;
			case UPDATE_DATA:
				updateDetailInfo(movieDetail);
				getRecommendData();
//				getActorsByWiki();
				break;
			case UPDATE_STAR_LIST:
				star_pager.setAdapter(new StarPagerAdapter(getBaseContext(), starList));
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_film_detail_layout);
		mImageFetcher = SharedImageFetcher.getNewFetcher(this, 3);
		volPanel = new VolumePanel(this);
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
				
				GlobalFilmData.getInstance().saveEpgUrl(context);
			}
			
		}, new IntentFilter("com.ipanel.join.cq.vodauth.EPG_URL"));
		Logger.d(TAG,""+GlobalFilmData.getInstance().getEpgUrl());
		Logger.d(TAG, ""+GlobalFilmData.getInstance().getGroupServiceId());
		findViewById(R.id.vod_file_detail_main).getViewTreeObserver().
		addOnGlobalFocusChangeListener(this);
		initViews();
		initControl();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		getIntentData();
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
	
	private void getIntentData() {
		if(getIntent() != null){
			wikiId = getIntent().getStringExtra("id");
			name = getIntent().getStringExtra("name");
			setName();
			bean =  (Wiki) getIntent().getSerializableExtra("data");
			isVodId = getIntent().getBooleanExtra("isVodId", false);
			Logger.d(TAG, "name:"+name+",id:"+wikiId+",isVodId:"+isVodId);
			if(bean != null){
				updateDetailInfo(bean);
				getRecommendData();
//				getActorsByWiki();
			}
			new Thread(initThread).start();
		}
	}
	
	//设置电影名称
	private void setName() {
		tv_name.setText(name);
		movie_name.setText(name);
		star_movie_name.setText(name);
	}
	
	private Runnable initThread = new Runnable() {
		
		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Logger.d("wuhd", "initThread start--");
					if(isVodId){
						vodId = wikiId;
						getMovieDetail(vodId);
					}else{
						getMovieDetail();
					}
				}
			});
		}
	};
	/***
	 * 获取推荐影片
	 */
	private void getRecommendData() {
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
				if(result.getError().getCode() == 0){
					recommend_list = result.getWikis();
					mHandler.obtainMessage(UPDATE_RECOMMEND).sendToTarget();
				}
			}
		});	
	}

	@Override
	protected void onNewIntent(Intent intent) {
		Logger.d("DetailActivity","onNewIntent");
		super.onNewIntent(intent);
		setIntent(intent);
	}
	
	private void initViews() {
		left_bar = (LinearLayout) this.findViewById(R.id.left_bar);
		collect = (RelativeLayout) left_bar.findViewById(R.id.line1);
		play = (RelativeLayout) left_bar.findViewById(R.id.line2);
		alike = (RelativeLayout) left_bar.findViewById(R.id.line3);
		star_show = (RelativeLayout) left_bar.findViewById(R.id.line5);
		mViewpager = (VerticalViewPager2)this.findViewById(R.id.viewpager);
		alike_layout = (RelativeLayout)this.findViewById(R.id.content_layout);
		movie_name = (TextView)this.findViewById(R.id.movie_name);
		page = (TextView)this.findViewById(R.id.movie_page);
		total_page = (TextView)this.findViewById(R.id.movie_totalpage);
		comment_list = (ListView)this.findViewById(R.id.comment_list);
		tv_poster = (ImageView)this.findViewById(R.id.tv_poster);
		tv_name = (TextView)this.findViewById(R.id.tv_name);
		play_times = (TextView)this.findViewById(R.id.play_times);//播放次数
		douban_score = (TextView)this.findViewById(R.id.douban_score);//豆瓣评分
		update_to = (TextView)this.findViewById(R.id.update_to);//更新至
		year = (TextView)this.findViewById(R.id.year);//年份
		region = (TextView)this.findViewById(R.id.region);//地区、
		goodBt = (Button)this.findViewById(R.id.good_bt);//点赞
		badBt = (Button)this.findViewById(R.id.bad_bt);;//点衰
		description = (TextView)this.findViewById(R.id.description);// 剧情介绍
		collect_img = (ImageView)findViewById(R.id.collect);
		
		director_ll = (LinearLayout)this.findViewById(R.id.director_ll);//导演
		actor_ll = (LinearLayout)this.findViewById(R.id.actor_ll);//主演
		label_ll = (LinearLayout)this.findViewById(R.id.tag_ll);//标签
		movie_content_layout = (RelativeLayout)this.findViewById(R.id.movie_intro);
		simpleTab = (SimpleTab)this.findViewById(R.id.detail_simple_tab);
		vod_collect_text = (TextView)findViewById(R.id.vod_collect_text);//收藏或者取消收藏
		
		star_show_layout = (RelativeLayout)findViewById(R.id.star_layout);
		star_movie_name = (TextView)findViewById(R.id.vod_movie_name);
		star_pager = (VerticalViewPager2)findViewById(R.id.star_viewpager);
		
	}
	
	private void initControl() {
		play.requestFocus();
		collect.setOnClickListener(this);
		play.setOnClickListener(this);
		alike.setOnClickListener(this);
		star_show.setOnClickListener(this);
		goodBt.setOnClickListener(this);
		badBt.setOnClickListener(this);
		
		mViewpager.setOnPageChangeListener(new OnPageChangeListener() {
			
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
		//设置焦点
		simpleTab.setOnTabChangeListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChange(int index, View selectedView) {
				showLayout(index);
			}
		});
		mViewpager.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
			@Override
			public void onGlobalFocusChanged(View oldFocus, View newFocus) {
				if (mViewpager.getScrollState() == VerticalViewPager2.SCROLL_STATE_IDLE) {
					if (newFocus instanceof FrameLayout) {
						moveIndicatorToFocus();
					}
				}
			}
		});
	}
	
	/**刷新界面*/
	private void updateDetailInfo(Wiki bean) {
		starList.clear();
		play.requestFocus();
		movie_content_layout.setVisibility(View.VISIBLE);
		alike_layout.setVisibility(View.INVISIBLE);
		setName();
		if(bean.getCover() != null){//设置海报
			BaseImageFetchTask task = mImageFetcher.getBaseTask(bean.getCover());
			mImageFetcher.setImageSize(321, 400);
			mImageFetcher.setLoadingImage(R.drawable.default_poster);
			mImageFetcher.loadImage(task, tv_poster);
		}else{
			tv_poster.setImageResource(R.drawable.default_poster);
		}
		description.setText(bean.getDesc());
		play_times.setText(bean.getInfo().getVod_num()+"次");
		update_to.setText(bean.getInfo().getRuntime()+"分钟");
		year.setText(bean.getInfo().getReleased());
		region.setText(bean.getInfo().getCountry());
		douban_score.setText(bean.getInfo().getAverage());
		//设置导演
		director_ll.removeAllViews();
		if(bean.getInfo().getDirector() != null && bean.getInfo().getDirector().size()>0){
			List<Star> directorList = bean.getInfo().getDirector();
			starList.addAll(directorList);
			for(int i = 0; i < directorList.size(); i++){
				String direct = directorList.get(i).getTitle();
				final View convertView = LayoutInflater.from(DetailActivity.this).inflate(R.layout.vod_label_item, null,false);
				TextView direct_name = (TextView)convertView.findViewById(R.id.vod_label_name);
				direct_name.setText(direct);
				if(convertView!=null){
					director_ll.addView(convertView);
					LinearLayout.LayoutParams para = (LinearLayout.LayoutParams) convertView
							.getLayoutParams();
					para.leftMargin = 28;
					convertView.requestLayout();
					convertView.setTag(directorList.get(i).getId());
				}
				convertView.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View arg0) {
						HWDataManager.openStarShowActivity(getBaseContext(), convertView.getTag().toString());
					}
				});
			}
		}
		List<Star> actorList = bean.getInfo().getStarring();
		actor_ll.removeAllViews();
		if(actorList!=null && actorList.size()>0){
			//设置主演
			int width = 0;
			starList.addAll(actorList);
			for(int i = 0; i < actorList.size();i++){
				String actorName = actorList.get(i).getTitle();
				final View convertView = LayoutInflater.from(DetailActivity.this).inflate(R.layout.vod_label_item, null,false);
				TextView label_name = (TextView)convertView.findViewById(R.id.vod_label_name);
				label_name.setText(actorName);
				if(convertView!=null){
					int w = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					int h = View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED);  
					convertView.measure(w, h);
					width += (convertView.getMeasuredWidth() + 28);
					if(width >= 981){
						break;
					}
					convertView.setTag(actorList.get(i).getId());
					actor_ll.addView(convertView);
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
		}
		
		//设置标签
		label_ll.removeAllViews();
		List<String> tagList = bean.getTags();
		if(tagList!=null && tagList.size()>0){
			for(int i = 0; i < tagList.size(); i++){
				final String labelName = tagList.get(i);
				View convertView = LayoutInflater.from(DetailActivity.this).inflate(R.layout.vod_label_item, null,false);
				TextView label_name = (TextView)convertView.findViewById(R.id.vod_label_name);
				label_name.setText(labelName);
				if(convertView!=null){
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
		}
		mHandler.sendEmptyMessage(UPDATE_STAR_LIST);
	}
	//设置页数
	private void refreshPage(int page) {
		this.page.setText(page+"");
		total_page.setText(String.format(getResources().getString(R.string.page), getTotalPage()));
	}
	
	//点赞或者点衰:1顶， 2踩
	private void setWikiInteractive(final int act) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_SET_WIKI_INTERACTIVE);
		request.getParam().setAct(act);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result.getError().getCode()==0){
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.line1:
			if(is_fav==0){
				//收藏
				setFavorite();
			}else{
				//取消收藏
				cancelFavorite();
			}
			break;
		case R.id.line2:
			getVodPlayUrl();
			break;
		case R.id.good_bt:
			if(is_inter==0)
				setWikiInteractive(1);
			break;
		case R.id.bad_bt:
			if(is_inter==0)
				setWikiInteractive(2);
			break;
		default:
			break;
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
	
	/***
	 * 获取电影详情
	 */
	private void getMovieDetail() {
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
				if(result.getError().getCode()==0){
					movieDetail = result.getWiki();//电视剧详情
					vodId = result.getVod().get(0).getId();
					Logger.d(TAG, "vodID"+vodId);
					updateInterInfo(movieDetail);//更新互动信息（收藏、点赞);
					if(bean == null && movieDetail != null){
						mHandler.sendEmptyMessage(UPDATE_DATA);
					}
				}else{
					Tools.showToastMessage(getBaseContext(), result.getError().getInfo());
				}
			}
		});		
	}
	/***
	 * 获取电影详情(通过华为vodId)
	 */
	private void getMovieDetail(String hwId) {
		Logger.d(TAG, "getMovieDetail:"+hwId);
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
					movieDetail = result.getWiki();//电视剧详情
					wikiId = result.getWiki().getId();
					Log.d("wuhd", "wikiId-->"+wikiId);
					updateInterInfo(movieDetail);//更新互动信息（收藏、点赞);
					if(bean == null && movieDetail != null){
						mHandler.sendEmptyMessage(UPDATE_DATA);
					}
				}else{
					Tools.showToastMessage(getBaseContext(), result.getError().getInfo());
				}
			}
		});		
	}
	//更新互动信息
	private void updateInterInfo(Wiki wiki) {
		is_fav = wiki.getInfo().getIs_fav();
		setCollectIcon();
		is_inter = wiki.getInfo().getIs_inter();
		Logger.d(TAG, "is_fav:"+is_fav+"is_inter:"+is_inter);
	}
	
	//显示收藏或已收藏
	private void setCollectIcon() {
		if(is_fav==1){
			collect_img.setImageResource(R.drawable.vod_movie_like02);
			vod_collect_text.setText(getString(R.string.collected));
		}else{
			collect_img.setImageResource(R.drawable.vod_movielike_sl);
			vod_collect_text.setText(getString(R.string.vod_collect));
		}
	}
	/**显示隐藏界面,index从0开始，0表示播放界面*/
	private void showLayout(int index){
		switch (index) {
		case 0:
			//播放界面&&详情
			movie_content_layout.setVisibility(View.VISIBLE);
			alike_layout.setVisibility(View.INVISIBLE);
			star_show_layout.setVisibility(View.INVISIBLE);
			break;
		case 1:
			//相似影片
			movie_content_layout.setVisibility(View.INVISIBLE);
			alike_layout.setVisibility(View.VISIBLE);
			star_show_layout.setVisibility(View.INVISIBLE);
			break;
		case 2:
			//明星秀
			movie_content_layout.setVisibility(View.INVISIBLE);
			star_show_layout.setVisibility(View.VISIBLE);
			alike_layout.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}
	//点击加载推荐的详情
	private void loadCurrentData(Wiki item) {
		showLayout(0);
		movieDetail = item;
		wikiId = item.getId();
		name = item.getTitle();
		updateInterInfo(movieDetail);
		mHandler.sendEmptyMessage(UPDATE_DATA);
	}
	
	//电影播放
	private void startToMoviePlay() {
		Intent intent = new Intent();
		intent.putExtra("name", name);
		intent.putExtra(
				"params",
				rtspResponse.getPlayUrl().substring(
						rtspResponse.getPlayUrl().indexOf("rtsp"),
						rtspResponse.getPlayUrl().length()));
		intent.putExtra("playType", "0");
		intent.putExtra("historyTime", 0);
		intent.putExtra("wikiId", wikiId);
		intent.setClass(DetailActivity.this, SimplePlayerActivity.class);
		DetailActivity.this.startActivity(intent);
	}
	RTSPResponse rtspResponse;
	//获取播放地址
	public void getVodPlayUrl(){
		Logger.d(TAG, "getVodplayUrl");
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie",
				GlobalFilmData.getInstance().getCookieString()) });
		serviceHelper.setRootUrl(GlobalFilmData.getInstance().getEPGBaseURL()
				+ "/go_authorization.jsp");
		serviceHelper.setSerializerType(SerializerType.JSON);
		RequestParams requestParamsfilm = new RequestParams();
		// requestParamsfilm.put("typeId", typeId);
		requestParamsfilm.put("typeId", "-1");
		requestParamsfilm.put("playType", "1");
		requestParamsfilm.put("progId", vodId);
		requestParamsfilm.put("contentType", "0");
		requestParamsfilm.put("business", "1");
		Log.d(TAG, "-->"+new Gson().toJson(requestParamsfilm));
		serviceHelper.callServiceAsync(DetailActivity.this,
				requestParamsfilm, RTSPResponse.class,
				new ResponseHandlerT<RTSPResponse>() {

					@Override
					public void onResponse(boolean success,
							RTSPResponse result) {
						if (success) {
							rtspResponse = result;
							mHandler.sendEmptyMessage(GOTO_MOVIEPLAY);
						}
					}
				});
	}
	//总viewpager页数
	private int getTotalPage(){
		int total = recommend_list == null ? 0 : recommend_list.size();
		return total % 5 == 0 ? total / 5 : total / 5 + 1; 
	}
	//获取明星列表
//	private void getActorsByWiki(){
//		GetHwRequest request = HWDataManager.getHwRequest();
//		request.setAction(HWDataManager.ACTION_GET_ACTORS_BY_WIKI);
//		request.getParam().setWikiId(vodId);
//		
//		serviceHelper.callServiceAsync(this, request, GetHwResponse.class,  new ResponseHandlerT<GetHwResponse>(){
//
//			@Override
//			public void onResponse(boolean success, GetHwResponse result) {
//				if (!success) {
//					Log.i(TAG, "request detail data failed");
//					return;
//				}
//				if (result == null) {
//					Log.i(TAG, "failed to parse JSON data");
//					showMessage(R.string.dataerror);
//					return;
//				}
//				starList.clear();
//				if(result.getWiki()!=null){
//					Starrings star = result.getWiki().getStarrings();
//					if(star!=null){
//						starList.add(star);
//						mHandler.sendEmptyMessage(UPDATE_STAR_LIST);
//					}
//				}
//			}
//		});
//	}
	private int getPageCount(int row){
		//共计行数
		int total = recommend_list == null ? 0 : recommend_list.size();
		int totalPage = total % 5 == 0 ? total / 5 : total / 5 + 1;
		if(row == totalPage - 1){
			return total % 5 == 0 ? 5 : total % 5;
		}else{
			return 5;
		}
	}
	
	class VODPagerAdapter extends PagerAdapter {

		@Override
		public float getPageWidth(int position) {
			return (1 + 0.0f) / 2;  
		}

		@Override
		public int getCount() {
			return recommend_list.size() % 5 == 0 ? recommend_list.size() / 5 : recommend_list.size() / 5 + 1;
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
			final Wiki item = recommend_list.get(index);
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
					loadCurrentData(item);
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
			if (row * 5 + position >= recommend_list.size()) {
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

	@Override
	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
		if(newFocus.getId() == R.id.line2){
			collect.setFocusable(true);
		}else if(newFocus.getId() != R.id.line1){
			collect.setFocusable(false);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
