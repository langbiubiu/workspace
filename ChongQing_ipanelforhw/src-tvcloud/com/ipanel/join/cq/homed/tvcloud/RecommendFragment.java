package com.ipanel.join.cq.homed.tvcloud;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.support.v4.view.ViewPager2;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.animation.LinearInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.Logger;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.ImageWorker;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.huawei.data.HuaWeiResponse;
import com.ipanel.join.cq.huawei.data.VodProgram;
import com.ipanel.join.cq.vod.detail.DetailActivity;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

@SuppressLint("NewApi") 
public class RecommendFragment extends BaseFragment {
	
	private final static String TAG = "RecommendFragment";
	
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	/**
	 * 栏目数据加载完成消息
	 * */
	public static final int MSG_COLUME_DATA_LOADED = 0;
	/**
	 * 一页影片数据加载完成消息
	 * */
	public static final int MSG_ROW_DATA_LOAED = 1;
	/**
	 * 请求超时消息
	 **/
	public static final int MSG_REQUEST_TIME_UP = 3;
	/**
	 * 移动焦点
	 **/
	public static final int MSG_MOVE_FOCUS_INDICATOR = 10;
	/**
	 * 一行显示多少影片
	 * */
	public static final int ROW_SIZE = 5;
	/**
	 * 一页显示几行
	 * */
	public static final int PAGE_ROW = 1;
	/**
	 * ViewPager一屏显示几行
	 * */
	public static final int MAX_PAGE_ROW = 2;
	private List<VodProgram> mData = new ArrayList<VodProgram>();
	private int totalMovieNumber = -1;
	
	private ImageFetcher mImageFetcher;
	
	TVCloudActivity mActivity;
	VerticalViewPager2 mViewPager;
	VODPagerAdapter mPagerAdapter;
	TextView cloud_recommend, live_record, my_record;
	private TextView current_page,total_page; 
	String[] typeIDs = new String[]{"10000100000000090000000000100015",
			"10000100000000090000000000100018",
			"10000100000000090000000000100075",
			"10000100000000090000000000100017",
			"10000100000000090000000000100016"};
	
	Handler mHandler = new Handler() {
		
		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			//栏目数据加载完成
			case MSG_COLUME_DATA_LOADED:
				Log.i(TAG, "-->msg MSG_COLUME_DATA_LOADED");
				if(mPagerAdapter == null){
					mPagerAdapter = new VODPagerAdapter();
				}else{
					mPagerAdapter.notifyDataSetChanged();
				}
				mViewPager.setAdapter(mPagerAdapter);
				break;
			//一页影片数据加载完成	
			case MSG_ROW_DATA_LOAED:
				break;
			case MSG_REQUEST_TIME_UP:
				break;
			case MSG_MOVE_FOCUS_INDICATOR:
				//moveIndicatorToFocus();
				break;
			}
		}
	};
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mActivity == null)
			mActivity = (TVCloudActivity) getActivity();
		current_page = mActivity.current_page;
		total_page = mActivity.total_page;
		current_page.setText("1");
		mImageFetcher = SharedImageFetcher.getNewFetcher(mActivity, 3);
		ViewGroup vg = (ViewGroup) inflater.inflate(R.layout.tvcloud_recommend_fragment, container, false);
		mViewPager = (VerticalViewPager2) vg.findViewById(R.id.viewpager);
		mViewPager.setMove_flag(false);
		mViewPager.setOffscreenPageLimit(1);
		mViewPager.setScroller(new Scroller(getActivity(), new LinearInterpolator()));
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
			}
			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
			}
			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager2.SCROLL_STATE_IDLE) {
					LogHelper.i("viewpager scroll complete");
					//moveFocus();
					moveIndicatorToFocus();
					mImageFetcher.setPauseWork(false);
				} else {
					mImageFetcher.setPauseWork(true);
					mActivity.mViewIndicator.hideFrame();
				}
			}
		});
		mViewPager.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
			@Override
			public void onGlobalFocusChanged(View oldFocus, View newFocus) {
				Logger.d("ViewPager oldFoucs " + oldFocus + " newFocus " + newFocus);
				if (mViewPager.getScrollState() == VerticalViewPager2.SCROLL_STATE_IDLE) {
					if (newFocus instanceof FrameLayout) {
						//moveFocus();
						moveIndicatorToFocus();
					} else {
						mActivity.mViewIndicator.hideFrame();
					}
				}
			}
		});
		
		new Thread(initThread).start();
		return vg;
	}

	@Override
	public void onResume() {
		if (mData == null || mData.size() ==0)
			new Thread(initThread).start();
		super.onResume();
	}
	
	private Runnable initThread = new Runnable() {
		
		@Override
		public void run() {
			Log.i(TAG, "initThread initData() start----------");
			initData();
		}
	};
	
	private void initData() {
		Random r = new Random();
		String typeID = typeIDs[r.nextInt(5)];
		RequestParams requestParams = new RequestParams();
		requestParams.put("typeID", typeID); // 栏目id
		requestParams.put("start", "0"); // 开始位置
		requestParams.put("size", "99"); // 每次返回数据条数
		requestParams.put("imgType", "1"); // 图片类型；0: 缩略图,1: 海报,2: 剧照,3: 图标,7: 背景图
		requestParams.put("defaultImgPath", ""); // 默认图片
		requestParams.put("icon", "1");// 查询图标 ;0或不传:不查询,1：查询出
		requestParams.put("intro", "1");// 栏目简介,1表示显示栏目简介,其它值或不传表示不显示栏目简介
		requestParams.put("platform", "android"); // 平台标识ipanel,android
		ServiceHelper service = ServiceHelper.getHelper();
		service.setRootUrl(CQApplication.getInstance().getEpgUrl() + "/defaultHD/en" + "/datajspHD/queryVodData.jsp");
		service.setHeaders(new Header[] {new BasicHeader("Cookie", CQApplication.getInstance().getCookieString())});
		service.setSerializerType(SerializerType.JSON);
		service.callServiceAsync(mActivity, requestParams, HuaWeiResponse.class, new ResponseHandlerT<HuaWeiResponse>() {
			
			@Override
			public void onResponse(boolean success, HuaWeiResponse result) {
				if (success) {
					if(result != null && result.getVod() != null && result.getVod().size() > 0){
						List<VodProgram> list = result.getVod();
						totalMovieNumber = list.size();
						Log.i(TAG, "电影总数" + totalMovieNumber);
						mData.addAll(list);
						int totalPage = getTotalPageSize();
						total_page.setText(totalPage+"");
						mHandler.obtainMessage(MSG_COLUME_DATA_LOADED).sendToTarget();
					}else if(result == null || result.getVod() == null){
						Tools.showToastMessage(getActivity(), getResources().getString(R.string.fail_read));
					}else if(result.getVod().size() == 0){
						Tools.showToastMessage(getActivity(), "暂无推荐数据");
					}
				} else {
					Tools.showToastMessage(getActivity(), getResources().getString(R.string.fail_read));
				}
			}
		});
		
	}
	
	private int getTotalFilmSize() {
		return totalMovieNumber;
	}
	
	/**
	 * 获得一页显示的总影片数
	 * */
	private int getPageDataCount() {
		return ROW_SIZE * PAGE_ROW;
	}
	
	/**
	 * 获得ViewPager的总行数
	 * */
	private int getTotalPageSize() {
		int count = getTotalFilmSize();
		return count % getPageDataCount() == 0 ? (count / getPageDataCount()) : (count / getPageDataCount() + 1);
	}
	
	/**
	 * 清除无效的图片加载任务
	 * */
	private void clearTasks(ViewGroup container) {
		int N = container.getChildCount();
		for (int i = 0; i < N; i++) {
			if (container.getChildAt(i).getTag() instanceof BaseImageFetchTask) {
				ImageWorker.cancelPotentialWork((ImageFetchTask) container.getChildAt(i).getTag(),
						container.getChildAt(i));
			} else {
				if (container.getChildAt(i) instanceof ViewGroup) {
					clearTasks((ViewGroup) container.getChildAt(i));
				}
			}
		}
	}
	
	private View getViewFromSoftReference() {
		View view = null;
		while (!views.empty()) {
			view = views.pop().get();
			if (view != null)
				return view;
		}
		return null;
	}
	
	class VODPagerAdapter extends PagerAdapter {

		@Override
		public float getPageWidth(int position) {
			return (PAGE_ROW + 0.0f) / MAX_PAGE_ROW;  
		}

		@Override
		public int getCount() {
			return getTotalPageSize();
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
			clearTasks(v);
			views.push(new SoftReference<View>(v));
			container.removeView(v);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			long time=System.currentTimeMillis();
			View itemView = getViewFromSoftReference();
			if (itemView == null) {
				itemView = View.inflate(mActivity, R.layout.tvcloud_recommend_grid, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.hotgrid);
			mWeightLayout.setClipToPadding(false);
			mWeightLayout.setTag(position);
			mWeightLayout.setAdapter(new VODWeightGridAdapter(position,ROW_SIZE));
			
			itemView.setTag(position);
			container.addView(itemView);
			LogHelper.e("instantiateItem view take time : "+(System.currentTimeMillis()-time));
			return itemView;
		}
	}
	
	class VODWeightGridAdapter extends WeightGridAdapter {
		
		int row;
		int xSize;

		public void setxSize(int xSize) {
			this.xSize = xSize;
		}
		
		public VODWeightGridAdapter(int row,int xSize) {
			this.row = row; //处于ViewPager的第几页
			this.xSize = xSize; //每行显示的item个数
		}

		@Override
		public int getCount() {
			return ((row+1) * getPageDataCount() > getTotalFilmSize()) ? (getTotalFilmSize() - row * getPageDataCount()) : getPageDataCount();
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
			if (convertView == null) {
				convertView = LayoutInflater.from(mActivity).inflate(R.layout.tvcloud_item_grid, parent, false);
			}
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_poster);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			final VodProgram film = mData.get(row * getPageDataCount() + position);
			if (film == null) {
				image.setImageResource(R.drawable.default_poster);
				//image.setImageResource(R.drawable.tvcloud_poster_default);
			} else {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						Intent intent = new Intent(getActivity(),DetailActivity.class);
						intent.putExtra("id", film.getVodID());
						intent.putExtra("name", film.getName());
						intent.putExtra("isVodId", true);
						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						startActivity(intent);
					}
				});
				convertView.setOnFocusChangeListener(new OnFocusChangeListener() {

					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							current_page.setText(row+1+"");
							name.setSelected(true);
						} else {
							name.setSelected(false);
						}
					}
				});
				convertView.setTag(row + "");
				name.setText(film.getName());
				image.setBackgroundColor(Color.TRANSPARENT);
				if (film.getImg() != null) {
					BaseImageFetchTask task = mImageFetcher.getBaseTask(film.getImg());					
					mImageFetcher.setImageSize(240, 300);
					//mImageFetcher.setLoadingImage(R.drawable.tvcloud_poster_default);
					mImageFetcher.setLoadingImage(R.drawable.default_poster);
					mImageFetcher.loadImage(task, image);
				} else {
					image.setImageResource(R.drawable.default_poster);
//					image.setImageResource(R.drawable.tvcloud_poster_default);
				}
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
			//return ROW_SIZE;
			return xSize;
		}
		
		@Override
		public int getYSize() {
			return PAGE_ROW;
		}

		@Override
		public int getYSpace() {
			return 10;
		}

		@Override
		public int getXSpace() {
			return 10;
		}

	}
	
}
