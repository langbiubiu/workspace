package com.ipanel.join.chongqing.myapp;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.support.v4.view.ViewPager2;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.RcKeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.animation.LinearInterpolator;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Scroller;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.myapp.WebAppResponse.WebApp;
import com.ipanel.join.chongqing.portal.AppConfig;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class OnlineAppActivity extends BaseActivity implements OnClickListener{
	public static final String TAG = OnlineAppActivity.class.getSimpleName();
	public static final String app_server = AppConfig.APP_SERVER;
	private VerticalViewPager2 viewPager;
	/**
	 * 栏目数据加载完成消息
	 * */
	public static final int MSG_COLUME_DATA_LOADED = 0;
	/**
	 * 一页影片数据加载完成消息
	 * */
	public static final int MSG_ROW_DATA_LOAED = 1;
	/**
	 * 一屏显示几行
	 * */
	public static final int PAGE_ROW = 1;
	/**
	 * ViewPager一屏显示几行
	 * */
	public static final int MAX_PAGE_ROW = 3;
	private VODPagerAdapter pagerAdapter;
	private TextView page;
	private TextView totalPage;
	private int row_size = 5;//每行默认为5个
	private ListView mTypeListView;//栏目分类
	private ImageView mImageTypeArrow;
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	
	List<String> type_list = new ArrayList<String>();
	WebAppResponse[] appsResponse;
	int left_position;
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			//数据加载完成
			case MSG_COLUME_DATA_LOADED:
				Logger.d("-->msg MSG_COLUME_DATA_LOADED");
//				if(pagerAdapter == null){
					pagerAdapter = new VODPagerAdapter();
//				}else{
//					pagerAdapter.notifyDataSetChanged();
//				}
				viewPager.setAdapter(pagerAdapter);
				refreshPage(1);
				break;
			//一页影片数据加载完成	
			case MSG_ROW_DATA_LOAED:
				break;
			}
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myapp_whole_layout);
		Log.i(TAG, "onCreate");
		volPanel = new VolumePanel(this);
		initViews();
		initControl();
		initData();
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	/**
	 * 初始化数据
	 */
	private void initData() {
		Log.i(TAG, "initData");
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		serviceHelper.setRootUrl(AppConfig.SERVER_WEBAPP_LIST_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(this, null, WebAppResponse[].class, new ServiceHelper.ResponseHandlerT<WebAppResponse[]>() {

			@Override
			public void onResponse(boolean success, WebAppResponse[] result) {
				if(result != null && result.length > 0){
					appsResponse = result;
					for(int i=0;i<result.length;i++){
						type_list.add(result[i].getName());
					}
					mTypeListView.setAdapter(new TypeAdapter(type_list));
					if(getIntent().getIntExtra("id", 0) < type_list.size()){
						mTypeListView.setSelection(getIntent().getIntExtra("id", 0));
					}
					mHandler.sendEmptyMessage(MSG_COLUME_DATA_LOADED);
				}else{
					Tools.showToastMessage(getApplicationContext(), "暂无内容");
				}
			}
		});
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
//		setIntent(null);
	}
	/**
	 * 初始化界面
	 */
	protected void initViews() {
		viewPager = (VerticalViewPager2)this.findViewById(R.id.viewpager);
		
		page = (TextView)this.findViewById(R.id.page);
		totalPage = (TextView)this.findViewById(R.id.totalpage);
		
		mTypeListView = (ListView)findViewById(R.id.sort_up);//栏目分类
		mTypeListView.setItemsCanFocus(true);
		mImageTypeArrow = (ImageView)findViewById(R.id.type_arrow);
	}
	
	/**
	 * 设置监听
	 */
	protected void initControl() {
		viewPager.setMove_flag(false);
		viewPager.setOffscreenPageLimit(1);
		viewPager.setScroller(new Scroller(this, new LinearInterpolator()));
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				refreshPage(position+1);
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager2.SCROLL_STATE_IDLE) {
					moveIndicatorToFocus();
				} else {
				}
			}
		});
		viewPager.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
			@Override
			public void onGlobalFocusChanged(View oldFocus, View newFocus) {
				if (viewPager.getScrollState() == VerticalViewPager2.SCROLL_STATE_IDLE) {
					if (newFocus instanceof FrameLayout) {
						moveIndicatorToFocus();
					}
				}
			}
		});
		
		mTypeListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long arg3) {
				Log.i(TAG, "listview onItemSelected:" + position);
				if(position < (type_list.size()-1)){
					mImageTypeArrow.setVisibility(View.VISIBLE);
				}else{
					mImageTypeArrow.setVisibility(View.INVISIBLE);
				}
				left_position = position;
				mHandler.sendEmptyMessage(MSG_COLUME_DATA_LOADED);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
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
	//刷新页数
	private void refreshPage(int i) {
		page.setText(i+"");
		totalPage.setText("/" + getTotalPageSize(left_position)+"页");
	}
	
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		if(keyCode==RcKeyEvent.KEYCODE_QUIT||keyCode == RcKeyEvent.KEYCODE_BACK){
			//返回键
		}
		return super.onKeyDown(keyCode, event);
	}
	
	/**
	 * 获得ViewPager的总行数
	 * */
	private int getTotalPageSize(int leftPosition) {
		int count = appsResponse[leftPosition].getData().length;
		return count % getPageDataCount() == 0 ? (count / getPageDataCount()) : (count / getPageDataCount() + 1);
	}
	
	/**
	 * 获得一页显示的总影片数
	 * */
	private int getPageDataCount() {
		return row_size * PAGE_ROW;
	}
	
	class VODPagerAdapter extends PagerAdapter {

		@Override
		public float getPageWidth(int position) {
			return (PAGE_ROW + 0.0f) / MAX_PAGE_ROW;  
		}

		@Override
		public int getCount() {
			return getTotalPageSize(left_position);
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
			views.push(new SoftReference<View>(v));
			container.removeView(v);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			long time=System.currentTimeMillis();
			View itemView = getViewFromSoftReference();
			if (itemView == null) {
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
			return getPageDataCount();
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
			if (row * getPageDataCount() + position >= appsResponse[left_position].getData().length) {
				return null;
			}
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplication()).inflate(R.layout.myapp_grid_item2, parent, false);
			}
			final ImageView image = (ImageView) convertView.findViewById(R.id.app_icon);
			final TextView name = (TextView) convertView.findViewById(R.id.app_name);
			final WebApp app = appsResponse[left_position].getData()[row*5 + position];
			SharedImageFetcher.getSharedFetcher(getApplicationContext()).loadImage(app_server + app.getImg(), image);
			name.setText(app.getName());
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
						try {
							ComponentName com = new ComponentName("com.ipanel.dtv.chongqing",
									"com.ipanel.dtv.chongqing.IPanel30PortalActivity");
							Intent i = new Intent();
							i.putExtra("url", app.getUrl());
							i.putExtra("tag", 1);
							i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							i.setComponent(com);
							startActivity(i);
						} catch (Exception e) {
							e.printStackTrace();
						}
				}
			});
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						name.setSelected(true);
					} else {
						name.setSelected(false);
					}
				}
			});
			convertView.setOnKeyListener(new OnKeyListener() {
				
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					//TODO 控制焦点,有子栏目
					if(event.getAction()==KeyEvent.ACTION_DOWN){
						if(keyCode==KeyEvent.KEYCODE_DPAD_LEFT){
							if(position == 0){
								mTypeListView.getSelectedView().requestFocus();
								return true;
							}
						}
					}
					return false;
				}
			});
			convertView.setTag(row + "");
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
			return row_size;
		}
		
		@Override
		public int getYSize() {
			return PAGE_ROW;
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
	public void onClick(View v) {
		switch (v.getId()) {

		default:
			break;
		}
	}
	
	class TypeAdapter extends BaseAdapter{
		
		public TypeAdapter(List<String> list) {
			this.list = list;
		}

		List<String> list;
		
		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			if(convertView == null){
				convertView = LayoutInflater.from(getBaseContext()).inflate(R.layout.vod_type_item, null,false);
			}
			TextView type = (TextView)convertView.findViewById(R.id.type_text);
			type.setText(list.get(position));
			convertView.setTag(position);
			return convertView;
		}
	}
}
