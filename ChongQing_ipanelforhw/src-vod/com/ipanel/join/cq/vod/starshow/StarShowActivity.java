package com.ipanel.join.cq.vod.starshow;

import java.util.ArrayList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
/**
 * 明星秀
 * @author wuhd
 *
 */
public class StarShowActivity extends BaseActivity {
	private static final String TAG = StarShowActivity.class.getSimpleName();
	public static final int SETDATA = 0;
	private TextView star_name;//明星姓名
	private TextView star_desc;//明星简介
	private TextView actor_detail;//详情介绍
	private ImageView star_poster;//明星海报
	private Button followBt;//关注按钮
	private Wiki actorInfo;//电影&资讯
	private List<Wiki> movieList;
	private List<Wiki> newsList;
	private RelativeLayout mScrollView_layout;
	private RelativeLayout mScrollView_layout2;
	private ImageFetcher mImageFetcher;
	private TextView star_movie;
	private TextView star_movie_total;
	private TextView star_news;
	private TextView star_news_total;
	private String wikiId;
	private View layout1;
	private View layout2;
	private boolean isFollowed = false;//明星是否关注
	private VerticalViewPager2 viewpager;
	private List<View> viewList;
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case SETDATA:
				updateUI();
				break;

			default:
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_starshow_layout);
		mImageFetcher = SharedImageFetcher.getNewFetcher(this, 3);
		volPanel = new VolumePanel(this);
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
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
	
	private void getIntentData() {
		if(getIntent() != null){
			wikiId = getIntent().getStringExtra("wikiId");
			getActorInfo(wikiId);
		}
	}	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
	
	private void initViews() {
		viewpager = (VerticalViewPager2)findViewById(R.id.viewpager);
		viewpager.setPageScrollDuration(300);
		
		LayoutInflater lf = getLayoutInflater().from(this);
		layout1 = lf.inflate(R.layout.vod_star_show1, null);
		layout2 = lf.inflate(R.layout.vod_star_show2, null);
		
		viewList = new ArrayList<View>();
		viewList.add(layout1);
		viewList.add(layout2);
		viewpager.setAdapter(new mPagerAdapter());
		
		star_name = (TextView)layout1.findViewById(R.id.vod_star_name);
		star_desc = (TextView)layout1.findViewById(R.id.basic_info);
		actor_detail = (TextView)layout1.findViewById(R.id.star_desc);
		star_poster = (ImageView)layout1.findViewById(R.id.vod_star_img);
		followBt = (Button)layout1.findViewById(R.id.follow);
		star_movie = (TextView)layout2.findViewById(R.id.star_movie);//影片
		star_movie_total = (TextView)layout2.findViewById(R.id.star_movie_total) ;//影片总数
		star_news = (TextView)layout2.findViewById(R.id.star_zixun);//资讯
		star_news_total = (TextView)layout2.findViewById(R.id.star_zixun_total);//资讯总数
		mScrollView_layout = (RelativeLayout)layout2.findViewById(R.id.mScrollView_layout);
		mScrollView_layout2 = (RelativeLayout)layout2.findViewById(R.id.mScrollView_layout2);
		
	}

	private void initControl() {
		followBt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(isFollowed){
					delFollowActor();//已关注，则取消关注
				}else{
					setFollowActor();//添加关注
				}
			}
		});
		
		followBt.setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
						if(movieList!=null && movieList.size()>0){
							viewpager.setCurrentItem(1);
							mScrollView_layout.getChildAt(0).requestFocus();
							return true;
						}else{
							Tools.showToastMessage(getBaseContext(), "没有影片或者资讯");
							return true;
						}
					}else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
							|| keyCode == KeyEvent.KEYCODE_DPAD_LEFT){
						return true;
					}
				}
				return false;
			}
		});
	}
	
	private void getActorInfo(String wikiId){
		Logger.d(TAG, "wikiId--"+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_ACTOR_INFO);
		request.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result != null){
					//演员详情
					if(result.getError().getCode() == 0){
						isFollowed = result.getIsFollowed() == 1;//判断是否已关注
						setButtonText();
						actorInfo = result.getWiki();
						movieList = result.getVideos();
						newsList = result.getTelevisions();
						if(actorInfo != null){
							mHandler.sendEmptyMessage(SETDATA);
						}
					}else{
						Tools.showToastMessage(getBaseContext(), result.getError().getInfo());
					}
				}else{
					Tools.showToastMessage(getBaseContext(), "获取演员详情失败");
				}
			}
		});
	}
	//刷新UI
	private void updateUI() {
		star_poster.setVisibility(View.VISIBLE);
		followBt.setVisibility(View.VISIBLE);
		followBt.requestFocus();
		if(!TextUtils.isEmpty(actorInfo.getInfo().getEnglish_name())){
			star_name.setText(actorInfo.getTitle()+"("+actorInfo.getInfo().getEnglish_name()+")");
		}else{
			star_name.setText(actorInfo.getTitle());
		}
		String birthplace = TextUtils.isEmpty(actorInfo.getInfo().getBirthplace()) ? "":actorInfo.getInfo().getBirthplace() + "/";
		String zodiac = TextUtils.isEmpty(actorInfo.getInfo().getZodiac()) ? "":actorInfo.getInfo().getZodiac() + "/";
		String birthday = TextUtils.isEmpty(actorInfo.getInfo().getBirthday()) ? "":actorInfo.getInfo().getBirthday() + "/";
		star_desc.setText(birthplace + zodiac + birthday + actorInfo.getInfo().getOccupation());
		
		actor_detail.setText(actorInfo.getDesc());
		if(actorInfo.getCover()!=null){
			mImageFetcher.loadImage(actorInfo.getCover(), star_poster);
		}else{
			star_poster.setImageResource(R.drawable.default_poster);
		}
		if(movieList!= null && movieList.size() > 0){
			star_movie.setText(getResources().getString(R.string.star_movie));
			star_movie_total.setText(movieList.size()+"部");
			mScrollView_layout.removeAllViews();
			for (int i = 0; i < movieList.size(); i++) {
				View convertView = getLayoutInflater().inflate(R.layout.vod_star_item, mScrollView_layout,false);
				final Wiki p = movieList.get(i % movieList.size());
				final ViewHold hv = new ViewHold();
				hv.image = (ImageView) convertView.findViewById(R.id.star_item_pic);
				hv.text = (TextView) convertView.findViewById(R.id.star_film_name);
				
				if (p.getCover() != null) {
					String adUrl = p.getCover();
					BaseImageFetchTask task = mImageFetcher.getBaseTask(adUrl);
					mImageFetcher.setLoadingImage(R.drawable.default_poster);
					mImageFetcher.loadImage(task, hv.image);
				}else{
					hv.image.setImageResource(R.drawable.default_poster);
				}
				hv.text.setText(p.getTitle()); 
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						if(p.getModel().equals("film") || p.getModel().equals("teleplay")){
							HWDataManager.openDetail(getBaseContext(), p.getId(), p.getTitle(), p);
						}
					}
				});
				convertView.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
						if(arg2.getAction() == KeyEvent.ACTION_DOWN ){
							if(arg1 == KeyEvent.KEYCODE_DPAD_UP){
								viewpager.setCurrentItem(0);
								followBt.requestFocus();
								return true;	
							}else if(arg1 == KeyEvent.KEYCODE_DPAD_DOWN){
								if(newsList!=null && newsList.size()>0){
									//TODO 
									Log.d(TAG, newsList.size()+"");
								}else{
									return true;
								}
							}
						}
						return false;
					}
				});
				convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if(hasFocus){
							hv.text.setSelected(true);
						}else{
							hv.text.setSelected(false);
						}
					}
				});
				if(convertView!=null){
					mScrollView_layout.addView(convertView);
					RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) convertView
							.getLayoutParams();
					para.leftMargin = StarShowActivity.this.getResources()
							.getDimensionPixelSize(R.dimen.vod_left_lenth) * i;
					convertView.requestLayout();
				}
			}
		}
		
		if(newsList!=null && newsList.size() > 0){
			star_news.setText(getResources().getString(R.string.star_zixun));
			star_news_total.setText(newsList.size()+"部");
			mScrollView_layout2.removeAllViews();
			for (int i = 0; i < newsList.size(); i++) {
				View convertView = getLayoutInflater().inflate(R.layout.vod_star_news_item, mScrollView_layout2,false);
				final Wiki p = newsList.get(i % newsList.size());
				final ViewHold hv = new ViewHold();
				hv.image = (ImageView) convertView.findViewById(R.id.star_news_pic);
				hv.text = (TextView) convertView.findViewById(R.id.star_news_name);
				
				if (p.getCover() != null) {
					String adUrl = p.getCover();
					BaseImageFetchTask task = mImageFetcher.getBaseTask(adUrl);
					mImageFetcher.setLoadingImage(R.drawable.default_poster);
					mImageFetcher.loadImage(task, hv.image);
				}else{
					hv.image.setImageResource(R.drawable.default_poster);
				}
				hv.text.setText(p.getTitle()); 
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						Tools.showToastMessage(getBaseContext(), p.getModel()+"类型暂不支持");
						return;
					}
				});
				convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
					
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if(hasFocus){
							hv.text.setSelected(true);
						}else{
							hv.text.setSelected(false);
						}
					}
				});
				if(convertView!=null){
					mScrollView_layout2.addView(convertView);
					RelativeLayout.LayoutParams para = (RelativeLayout.LayoutParams) convertView
							.getLayoutParams();
					para.leftMargin = StarShowActivity.this.getResources()
							.getDimensionPixelSize(R.dimen.vod_left_lenth2) * i;
					convertView.requestLayout();
				}
			}
		}else{
			mScrollView_layout2.setFocusable(false);
		}
	}
	/**
	 * 是否关注
	 */
	private void setButtonText() {
		if(isFollowed){
			followBt.setText("已关注");
		}else{
			followBt.setText("关注TA");
		}
	}
	/**
	 *添加关注
	 */
	private void setFollowActor(){
		Log.i(TAG, "setFollowActor"+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_SET_FOLLOW_ACTOR);
		request.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result != null){
					//演员详情
					if(result.getError().getCode() == 0){
						isFollowed = true;
						Tools.showToastMessage(getBaseContext(), "关注成功");
						setButtonText();
					}else{
						Tools.showToastMessage(getBaseContext(), "关注失败");
					}
				}else{
					Tools.showToastMessage(getBaseContext(), "关注失败");
				}
			}
		});
	}
	
	/**
	 *删除关注演员 
	 */
	private void delFollowActor(){
		Log.i(TAG, "--delFollowActor--"+wikiId);
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_DEL_FOLLOW_ACTOR);
		request.getParam().setWikiId(wikiId);
		
		serviceHelper.callServiceAsync(this, request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result != null){
					//演员详情
					if(result.getError().getCode() == 0){
						isFollowed = false;
						Tools.showToastMessage(getBaseContext(), "取消关注成功");
						setButtonText();
					}else{
						Tools.showToastMessage(getBaseContext(), "取消关注失败");
					}
				}else{
					Tools.showToastMessage(getBaseContext(), "取消关注失败");
				}
			}
		});
	}
	
	
	class ViewHold {
		ImageView image;
		TextView text;
	}
	
	class mPagerAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			return viewList.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			container.removeView(viewList.get(position));
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			container.addView(viewList.get(position));
			return viewList.get(position);
		}
		
		@Override
		public float getPageWidth(int position) {
			if(position == 0){
				return 643 / 955f;
			}else{
				return 1.0f;
			}
		}
	}
}
