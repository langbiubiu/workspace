package com.ipanel.join.cq.global;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.IFrameIndicator;
import cn.ipanel.android.widget.ViewFrameIndicator;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.myapp.WebAppResponse;
import com.ipanel.join.chongqing.myapp.WebAppResponse.WebApp;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * 全域重庆界面
 * @author Administrator
 *
 */
public class GlobalActivity extends Activity implements OnClickListener, OnGlobalFocusChangeListener {
	private String TAG = "GlobalActivity";
	private ImageView mRecommendPoster; // 推荐海报
	private TextView mRecommendName; // 推荐名称
	private ImageView mArrowUp; // 向上箭头
	private ImageView mArrowDown; // 向下箭头
	private VerticalViewPager2 mViewPager; // 列表
	private TextView mCurrent; // 当前页
	private TextView mTotal; // 总页
	private LinearLayout mBackTop; // 回顶部
	private Button mHome; // 首页
	private Button mBack; // 返回
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	private List<WebApp> list; // 列表数据源
	private List<WebApp> adList; // 广告数据源
	private String url = "http://ipanel.vod.cqcnt.com/dvbottapp/"; // 列表的服务端地址
	private int mTotalPage = 0; // 总页码
	private IFrameIndicator mIndicator; // 焦点

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.global_activity);
		mIndicator = new ViewFrameIndicator(GlobalActivity.this);
		mIndicator.setFrameResouce(R.drawable.global_focus);
		bindViews();
		setOnClickListener();
		init();
		loadData();
		Log.i(TAG, "onCreate");
	}
	
	
	private void bindViews(){
		mRecommendPoster = (ImageView)findViewById(R.id.global_activity_recommend_poster);
		mRecommendName = (TextView)findViewById(R.id.global_activity_recommend_name);
		mArrowUp = (ImageView)findViewById(R.id.global_activity_up);
		mArrowDown = (ImageView)findViewById(R.id.global_activity_down);
		mViewPager = (VerticalViewPager2)findViewById(R.id.global_activity_viewpager);
		mCurrent = (TextView)findViewById(R.id.global_activity_current);
		mTotal = (TextView)findViewById(R.id.global_activity_total);
		mBackTop = (LinearLayout)findViewById(R.id.global_activity_backtop);
		mHome = (Button)findViewById(R.id.global_activity_home);
		mBack = (Button)findViewById(R.id.global_activity_back);
	}
	
	private void setOnClickListener(){
		mRecommendPoster.setOnClickListener(this);
		mBackTop.setOnClickListener(this);
		mHome.setOnClickListener(this);
		mBack.setOnClickListener(this);
	}
	
	private void init(){
		mViewPager.getViewTreeObserver().addOnGlobalFocusChangeListener(this);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				// TODO Auto-generated method stub
				mCurrent.setText(String.valueOf(position + 1));
				refreshArrows();
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				// TODO Auto-generated method stub
				if (state == VerticalViewPager2.SCROLL_STATE_IDLE) {
					if (getCurrentFocus() != null && getCurrentFocus() instanceof LinearLayout) {
						mIndicator.hideFrame();
					}else {
						mIndicator.moveFrmaeTo(getCurrentFocus());
					}
				}else {
					mIndicator.hideFrame();
				}
			}
		});
		Log.i(TAG, "init");
	}

	/**
	 * 获取数据
	 */
	private void loadData(){
		Log.i(TAG, "loadData");
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setRootUrl(url + "qycqList.txt");
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(this, null, WebAppResponse[].class, new ServiceHelper.ResponseHandlerT<WebAppResponse[]>() {

			@Override
			public void onResponse(boolean success, WebAppResponse[] result) {
				Log.i(TAG, "onResponse");
				if(result != null && result.length > 0){
					updateRecommend(result[0]); // 第一个是推荐位数据
					if (result.length > 1) {
						getListTotalPager(result[1]); // 第二个是栏目列表数据
						setPage(1);
					}
					if (result.length > 2 && result[2] != null) {
						adList = Arrays.asList(result[2].getData()); // 第三个是广告数据列表
					}
					Log.i(TAG, "onResponse end");
					mViewPager.setAdapter(new PagersAdapter());
				}
			}
		});
	}
	 
	/**
	 * 更新推荐位数据
	 * @param response 推荐位数据
	 */
	private void updateRecommend(WebAppResponse response){
		if (response == null || response.getData() == null || response.getData().length <= 0) {
			return;
		}
		Log.i(TAG, "updateRecommend");
		WebApp[] app = response.getData(); 
		mRecommendName.setText(app[0].getName());
		SharedImageFetcher.getSharedFetcher(getApplicationContext()).loadImage(url + app[0].getImg(),
				mRecommendPoster);
		mRecommendPoster.setTag(app[0]);
	}
	
	/**
	 * 计算列表数据总页码
	 * @param response 列表数据
	 * @return
	 */
	private int getListTotalPager(WebAppResponse response){
		if (response == null || response.getData() == null || response.getData().length <= 0) {
			return mTotalPage;
		}
		Log.i(TAG, "getListTotalPager");
		list = Arrays.asList(response.getData());
		if (list != null && list.size() > 0) {
			mTotalPage = list.size() % 9 == 0 ? list.size() / 9 : list.size() / 9 + 1;
			return mTotalPage;
		}else {
			return mTotalPage;
		}
	} 
	
	/**
	 * 显示当前页和总页码
	 * @param position 当前页
	 */
	private void setPage(int position){
		mCurrent.setText(""+position);
		mTotal.setText(""+mTotalPage);
	}
	
	/**
	 * 刷新上下箭头
	 */
	private void refreshArrows(){
		if (mViewPager.getCurrentItem() == 0) {
			mArrowUp.setVisibility(View.INVISIBLE);
			mArrowDown.setVisibility(View.VISIBLE);
		}else if (mViewPager.getCurrentItem() == mTotalPage - 1) {
			mArrowUp.setVisibility(View.VISIBLE);
			mArrowDown.setVisibility(View.INVISIBLE);
		}else {
			mArrowDown.setVisibility(View.VISIBLE);
			mArrowUp.setVisibility(View.VISIBLE);
		}
	}
	
	/**
	 * 返回页面顶部，第一项获得焦点
	 */
	private void backTop(){
		mViewPager.setCurrentItem(0); // 选中第一页
		ViewGroup linear = (ViewGroup) mViewPager.getCurrentView(); // FrameLayout
		Log.i(TAG, "--->linear = " + linear);
		ViewGroup grid = (ViewGroup) linear.getChildAt(0); // WeightGridLayout
		Log.i(TAG, "--->grid = " + grid);
		grid.requestFocus();
	}
	
	/**
	 * 启动3.0apk
	 * @param url 访问链接
	 */
	private void startPortalActivity(String url){
		ComponentName com = new ComponentName("com.ipanel.dtv.chongqing",
				"com.ipanel.dtv.chongqing.IPanel30PortalActivity");
		Intent i = new Intent();
		i.putExtra("url", url);
		i.putExtra("tag", 1);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.setComponent(com);
		startActivity(i);
	}
	
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.global_activity_recommend_poster:
			WebApp app = (WebApp) v.getTag();
			if (app != null && app.getUrl() != null && !"".equals(app.getUrl())) {
				startPortalActivity(app.getUrl());
			}
			break;
		case R.id.global_activity_backtop:
			backTop();
			break;
		case R.id.global_activity_home:
			GlobalActivity.this.finish();
			break;
		case R.id.global_activity_back:
			GlobalActivity.this.finish();
			break;
		default:
			break;
		}
	}
	
	@Override
	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
		// TODO Auto-generated method stub
		if (newFocus != null) {
			if (newFocus instanceof Button || newFocus instanceof LinearLayout
					 || newFocus instanceof VerticalViewPager2) {
				mIndicator.hideFrame();
			}else {
				if (mViewPager.getScrollState() == VerticalViewPager2.SCROLL_STATE_IDLE) {
					mIndicator.moveFrmaeTo(newFocus);
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
	
	/**
	 * 页码适配器
	 * @author Administrator
	 *
	 */
	class PagersAdapter extends PagerAdapter{

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mTotalPage;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			// TODO Auto-generated method stub
			return arg0 == arg1;
		}
		
		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
			// TODO Auto-generated method stub
			ViewGroup v = (ViewGroup) object;
			views.push(new SoftReference<View>(v));
			container.removeView(v);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			// TODO Auto-generated method stub
			View itemView = getViewFromSoftReference();
			if (itemView == null) {
				 itemView = View.inflate(getBaseContext(), R.layout.global_page_item, null);
			}
			// 栏目列表
			WeightGridLayout listWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.global_list_grid);
			listWeightLayout.setClipToPadding(false);
			listWeightLayout.setTag(position);
			int lastIndex = list.size(); 
			int start = position * 9;
			int end = (position * 9 + 9) > lastIndex ? lastIndex : (position * 9 + 9);
			Log.d(TAG, "-->lastIndex = "+lastIndex +";start = "+start+";end = "+end+";size = "+list.subList(start, end).size());
			listWeightLayout.setAdapter(new ListGridAdapter(list.subList(start, end)));
			
			// 广告列表
			WeightGridLayout adWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.global_ad_grid);
			adWeightLayout.setClipToPadding(false);
			adWeightLayout.setTag(position);
			int adLastIndex = list.size(); 
			int adStart = position * 2;
			int adEnd = (position * 2 + 2) > adLastIndex ? adLastIndex : (position * 2 + 2);
			Log.d(TAG, "-->adLastIndex = "+adLastIndex +";adStart = "+adStart+";adEnd = "+adEnd+";size = "+adList.subList(adStart, adEnd).size());
			adWeightLayout.setAdapter(new ADGridAdapter(adList.subList(adStart, adEnd)));
			
			itemView.setTag(position);
			container.addView(itemView);
			
			return itemView;
		}
	}
	
	/**
	 * 栏目列表适配器
	 * @author Administrator
	 *
	 */
	class ListGridAdapter extends WeightGridAdapter{
		private List<WebApp> list;

		public ListGridAdapter(List<WebApp> list) {
			super();
			this.list = list;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final WebApp app = list.get(position);
			final Holder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.global_list_item, parent, false);
				holder = new Holder();
				holder.imageView = (ImageView) convertView.findViewById(R.id.global_list_item_poster); 
				holder.textView = (TextView) convertView.findViewById(R.id.global_list_item_name); 
				convertView.setTag(holder);
			}else {
				holder = (Holder) convertView.getTag();
			}
			holder.imageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.i("ListGridAdapter onClick ====", "ListGridAdapter onClick ====");
					if (app != null && app.getUrl() != null && !"".equals(app.getUrl())) {
						startPortalActivity(app.getUrl());
					}
				}
			});
			if (app != null) {
				holder.textView.setText(app.getName());
				SharedImageFetcher.getSharedFetcher(getApplicationContext()).loadImage(url + app.getImg(),
						holder.imageView);
			}
			
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getXSize() {
			// TODO Auto-generated method stub
			return 3;
		}

		@Override
		public int getYSize() {
			// TODO Auto-generated method stub
			return 3;
		}
		
		@Override
		public int getXSpace() {
			return 12;
		}
		
		@Override
		public int getYSpace() {
			return 15;
		}
		
		class Holder {
			public ImageView imageView;
			public TextView textView;
		}
		
	}
	
	/**
	 * 广告列表适配器
	 * @author Administrator
	 *
	 */
	class ADGridAdapter extends WeightGridAdapter{
		private List<WebApp> ad;
		
		public ADGridAdapter(List<WebApp> ad) {
			super();
			this.ad = ad;
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			if (ad == null || ad.size() <= 0) {
				return 0;
			}
			return ad.size();
		}

		@Override
		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			final WebApp app = ad.get(position);
			final Holder holder;
			if (convertView == null) {
				convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.global_ad_item, parent, false);
				holder = new Holder();
				holder.imageView = (ImageView) convertView.findViewById(R.id.global_ad_item_poster); 
				convertView.setTag(holder);
			}else {
				holder = (Holder) convertView.getTag();
			}
			holder.imageView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					Log.i("ADGridAdapter onClick ====", "ADGridAdapter onClick ====");
					if (app != null && app.getUrl() != null && !"".equals(app.getUrl())) {
						startPortalActivity(app.getUrl());
					}
				}
			});
			if (app != null) {
				SharedImageFetcher.getSharedFetcher(getApplicationContext()).loadImage(url + app.getImg(),
						holder.imageView);
			}
			
			return convertView;
		}

		@Override
		public int getChildXSize(int position) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getChildYSize(int position) {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getXSize() {
			// TODO Auto-generated method stub
			return 1;
		}

		@Override
		public int getYSize() {
			// TODO Auto-generated method stub
			return 2;
		}
		
		@Override
		public int getYSpace() {
			return 15;
		}
		
		class Holder {
			public ImageView imageView;
		}
	}
}
