package com.ipanel.join.cq.vod.rank;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
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
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.vodhome.BaseActivity;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
/**
 * 排行榜；收藏榜；最新榜；明星榜
 * @author wuhd
 *
 */
public class VodRankListActivity extends BaseActivity{
	public static final String TAG = "VodRankListActivity";
	private TextView vod_rank_list1;
	private TextView vod_rank_list2;
	private TextView vod_rank_list3;
	private TextView vod_rank_list4;
	private VerticalViewPager2 mViewPager;
	private SimpleTab simpleTab;
	private int rank_index = 1;//从0开始
	private String action = "";
	private Map<String, List<Wiki>> mData = new HashMap<String, List<Wiki>>();
	private ImageFetcher mImageFetcher;
	private static final int SET_PROGRAM_DATA = 0;
	private VODPagerAdapter pagerAdapter;
	private int mTotalNumber;
	private TextView vod_rank_totalPage;
	private TextView vod_current_page;
	
	private Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case SET_PROGRAM_DATA:
				if(mData.get(action)!=null && mData.get(action).size()>0){
					mTotalNumber = mData.get(action).size();
					if(pagerAdapter == null){
						pagerAdapter = new VODPagerAdapter(mData.get(action));
					}else{
						pagerAdapter.setList(mData.get(action));
					}
					mViewPager.setAdapter(pagerAdapter);
					refreshPage(1);
				}
				break;
			}
		}
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.vod_ranklist_activity);
		mImageFetcher = SharedImageFetcher.getNewFetcher(this, 3);
		volPanel = new VolumePanel(this);
		initViews();
		initControl();
		getIntentData();
	}
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	private void getIntentData() {
		rank_index = getIntent().getIntExtra("type", 1);
		updateUI();
		getRankListData();
	}
	private void initControl() {
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
				
			}
		});
		simpleTab.setOnTabChangeListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChange(int index, View selectedView) {
				switch (index) {
				//最新榜
				case 0:
					action = HWDataManager.ACTION_GET_WIKIS_BY_NEW;
					getRankListData();
					break;
				//热播榜
				case 1:
					action = HWDataManager.ACTION_GET_WIKIS_BY_HIT;
					getRankListData();
					break;
				//明星榜
				case 2:
					action = HWDataManager.ACTION_GET_ACTORS_BY_FOLLOW;
					getRankListData();
					break;
				//收藏榜
				case 3:
					action = HWDataManager.ACTION_GET_WIKIS_BY_FAV;
					getRankListData();
					break;

				default:
					break;
				}
			}
		});
	}
	
	private void getRankListData() {
		if(mData.get(action)!=null && mData.get(action).size()>0){
			//说明有缓存
			mHandler.sendEmptyMessage(SET_PROGRAM_DATA);
		}else{
			getHomedProgram();
		}
	}
	Gson gson = new Gson();
	//获取数据
	private void getHomedProgram() {
		GetHwRequest req = HWDataManager.getHwRequest();
		req.setAction(action);
		req.getParam().setPage(1);
		req.getParam().setPagesize(20);//暂时先取30部影片
		
		serviceHelper.callServiceAsync(getApplicationContext(), req,
				GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {					

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
						Logger.d("gson-->"+gson.toJson(result));
						fillData(result);
						mHandler.sendEmptyMessage(SET_PROGRAM_DATA);
					}
				});
	}
	//处理数据
	private void fillData(GetHwResponse result) {
		mData.put(action, result.getWikis());
	}
	
	//更新UI
	private void updateUI() {
		simpleTab.setCurrentIndex(rank_index-1);
		switch (rank_index) {
		case 1:
			vod_rank_list1.requestFocus();
			action = "GetWikisByNew";
			break;
		case 2:
			vod_rank_list2.requestFocus();
			action = "GetWikisByHit";
			break;
		case 3:
			vod_rank_list3.requestFocus();
			action = "GetActorsByFollow";
			break;
		case 4:
			vod_rank_list4.requestFocus();
			action = "GetWikisByFav";
			break;
		}
	}
	
	/**
	 * 获得ViewPager的总页数
	 * */
	private int getTotalPageSize() {
		int count = mTotalNumber;
		return count / 5 + (count % 5 == 0 ? 0 : 1);
	}
	
	/**
	 * 获得一页显示的影片数
	 * @param pager 
	 * */
	private int getPageDataCount(int pager) {
		if(pager == getTotalPageSize() - 1){
			return mTotalNumber % 5 == 0 ? 5 : mTotalNumber % 5;//最后一页个数
		}else{
			return 5;
		}
	}
	
	private void initViews() {
		vod_rank_list1 = (TextView)this.findViewById(R.id.vod_rank_list1);
		vod_rank_list2=  (TextView)this.findViewById(R.id.vod_rank_list2);
		vod_rank_list3 = (TextView)this.findViewById(R.id.vod_rank_list3);
		vod_rank_list4 = (TextView)this.findViewById(R.id.vod_rank_list4);
		vod_rank_totalPage = (TextView)this.findViewById(R.id.vod_rank_totalpage);
		vod_current_page = (TextView)this.findViewById(R.id.vod_rank_page);
		simpleTab = (SimpleTab)this.findViewById(R.id.simple_tab);
		mViewPager = (VerticalViewPager2)this.findViewById(R.id.vod_rank_viewpager);
	}
	//刷新页数
	private void refreshPage(int page){
		vod_rank_totalPage.setText("/"+getTotalPageSize()+"行");
		vod_current_page.setText(""+page);
	}
	
	class VODPagerAdapter extends PagerAdapter {
		private List<Wiki> list;
		
		public void setList(List<Wiki> list) {
			this.list = list;
			notifyDataSetChanged();
		}

		public VODPagerAdapter(List<Wiki> list) {
			this.list = list;
		}

		@Override
		public float getPageWidth(int position) {
			return (1 + 0.0f) / 2;  
		}

		@Override
		public int getCount() {
			return list.size() % 5 == 0 ? list.size() / 5 : list.size() / 5 + 1;
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
			mWeightLayout.setAdapter(new VODWeightGridAdapter(position,list));
			itemView.setTag(position);
			container.addView(itemView);
			LogHelper.e("instantiateItem view take time : "+(System.currentTimeMillis()-time));
			return itemView;
		}
	}
	
	class VODWeightGridAdapter extends WeightGridAdapter {
		private int row;
		private List<Wiki> list;
		
		public VODWeightGridAdapter(int row,List<Wiki> list) {
			this.row = row;
			this.list = list;
		}

		@Override
		public int getCount() {
			return getPageDataCount(row);
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
			final Wiki item = list.get(index);
			if (convertView == null) {
				convertView = LayoutInflater.from(getApplication()).inflate(R.layout.vod_hot_element3, parent, false);
			}
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					//TODO 点击跳转
					if(item.getModel().equals("teleplay")){
						//电视剧
						HWDataManager.openDetail(getBaseContext(), item.getId(), item.getTitle(), item);
					}else if(item.getModel().equals("film")){
						HWDataManager.openDetail(getBaseContext(), item.getId(), item.getTitle(),item);
					}else if(item.getModel().equals("television")){
						//综艺节目
					}else if(item.getModel().equals("actor")){
						HWDataManager.openStarShowActivity(getBaseContext(),item.getId());
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
			convertView.setTag(row + "");
			if (row * 5 + position >= list.size()) {
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
			return 45;
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
