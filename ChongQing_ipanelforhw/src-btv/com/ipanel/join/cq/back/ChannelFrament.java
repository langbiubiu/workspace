package com.ipanel.join.cq.back;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.app.Service;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Channel;
import com.ipanel.join.cq.data.BtvChannel;
import com.ipanel.join.cq.data.BtvChannel.HwChannel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

//频道列表
public class ChannelFrament extends BaseFragment {
	public static final String TAG = ChannelFrament.class.getSimpleName();
	private ListView channelTypeView;
	private List<String> typeList = new ArrayList<String>();
	private BackActivity mActivity;
	private Map<String, List<Channel>> mDataMap = new HashMap<String, List<Channel>>();
	public static final int PAGE_ROW = 1;
	public static final int MAX_PAGE_ROW = 6;
	public static final int ROW_SIZE = 2;
	public static final int UPDATE_CHANNEL = 0x01;
	public static final int NO_CHANNEL = 0x02;
	public static final int UPDATE_TYPE = 0x03;
	private TextView current_column;//当前行数
	private TextView total_columns;//总行数
	private VerticalViewPager2 mViewPager2;
	private int mIndex = 0;
	private ServiceHelper serviceHelper;
	private BtvChannel[] channelList;//回看的节目列表
	
	private String[] channel_type = {"hd","local","cctv","tv","pay","other"};//频道分类
	
	Handler mHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what) {
			case UPDATE_CHANNEL:
				if(mIndex == 0){
					mViewPager2.setAdapter(new ChannelPagerAdapter(mDataMap.get("fav")));
					setCurrentPage(1, true);
				}
				else{
					mViewPager2.setAdapter(new ChannelPagerAdapter(mDataMap.get(channel_type[mIndex-1])));
					setCurrentPage(1, true);
				}
				break;
			case NO_CHANNEL:
				mViewPager2.removeAllViews();
				setCurrentPage(0, false);
			case UPDATE_TYPE:
				channelTypeView.setAdapter(new ChannelTypeListAdapter());
				break;
			default:
				break;
			}
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.back_channel_fragment, container,
				false);
		mActivity = (BackActivity) getActivity();
		serviceHelper = ServiceHelper.getHelper();
		initViews(view);
		initControl();
		initData();
		return view;
	}
	//初始化数据
	private void initData() {
		String[] type = getActivity().getResources().getStringArray(R.array.channel_type_array);
		String type1 = "收藏";
//		for (int i = 0; i < type.length; i++) {
//			typeList.add(type[i]);
//		}
		typeList.add(type1);
		getChannelList();
	}
	//初始化界面
	private void initViews(View view) {
		channelTypeView = (ListView)view.findViewById(R.id.channel_type_list);
		channelTypeView.setItemsCanFocus(true);
		mViewPager2 = (VerticalViewPager2)view.findViewById(R.id.channel_viewpager);
		mViewPager2.setOffscreenPageLimit(3);
		current_column = (TextView)view.findViewById(R.id.channel_column);
		total_columns = (TextView)view.findViewById(R.id.channel_total_column);
		channelTypeView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long arg3) {
				mIndex = position;
				String tag = (String) v.getTag();
				mActivity.setSecondTitle(tag);
				if(position==0){
					getFavChannel();
				}else{
//					getChannels(position-1);
					setListView(position-1);
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
	}
	//设置华为的频道列表
	protected void setListView(int i) {
		Logger.d(TAG, "i="+i);
		BtvChannel channel = channelList[i];
		Logger.d(TAG, "channel"+channel.getCataName());
		List<HwChannel> hwChannelList = channel.getChannelList();
		if(hwChannelList!=null && hwChannelList.size() > 0){
			mViewPager2.setAdapter(new HwChannelPagerAdapter(hwChannelList));
			setCurrentPage(1, true);
		}else{
			mHandler.sendEmptyMessage(NO_CHANNEL);
		}
	}
	
	private void initControl() {
		mViewPager2.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				setCurrentPage(position+1,true);
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				
			}
		});
	}
	//设置页码
	private void setCurrentPage(int i,boolean hasData) {
		if(!hasData){
			current_column.setText(""+0);
			total_columns.setText("/"+0+"行");
		}else{
			current_column.setText(""+i);
			if(mIndex==0){
				int total = mDataMap.get("fav").size();
				int page = total % 2 == 0 ? total / 2 : total / 2 + 1;
				total_columns.setText("/"+page+"行");
			}else{
				//int total = mDataMap.get(channel_type[mIndex-1]).size();
				int total = channelList[mIndex-1].getChannelList().size();
				int page = total % 2 == 0 ? total / 2 : total / 2 + 1;
				total_columns.setText("/"+page+"行");
			}
		}
	}
	//收藏的频道
	private void getFavChannel(){
		//请求参数
		Logger.d(TAG, "getFavChannel");
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_FAV_CHANNELS_BY_USER);
		request.getParam().setShowlive(false);
		request.getParam().setPage(1);
		request.getParam().setPagesize(30);
		//返回
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getActivity(), request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					return;
				}
				if(result.getError().getCode()==0){
					List<Channel> favChannels = result.getChannels();
					mDataMap.put("fav", favChannels);
					if(favChannels!=null&&favChannels.size()>0){
						mHandler.removeMessages(UPDATE_CHANNEL);
						mHandler.sendEmptyMessageDelayed(UPDATE_CHANNEL, 100);
					}else{
						mHandler.sendEmptyMessage(NO_CHANNEL);
					}
				}
			}
		});
	}
	Gson gson = new Gson();
	//获取频道列表
	private void getChannels(final int i){
		String type = "";
		if(i >= 0 && i <= 5){
			type = channel_type[i];
		}else{
			return;
		}
		Logger.d(TAG, "type-->"+type);
		//请求参数
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_CHANNELS);
		request.getParam().setType(type);
		request.getParam().setShowlive(false);
		request.getParam().setOrder(0);
		request.getParam().setPage(1);
		request.getParam().setPagesize(30);
		Logger.d(TAG, "---"+gson.toJson(request));
		//返回
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getActivity(), request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>(){

			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if (!success) {
					Log.i(TAG, "request detail data failed");
					return;
				}
				if (result == null) {
					Log.i(TAG, "failed to parse JSON data");
					return;
				}
				if(result.getError().getCode()==0){
					List<Channel> mChannels = result.getChannels();
					mDataMap.put(channel_type[i], mChannels);
					if(mChannels!=null && mChannels.size()>0){
						mHandler.removeMessages(UPDATE_CHANNEL);
						mHandler.sendEmptyMessageDelayed(UPDATE_CHANNEL,100);
					}else{
						mHandler.sendEmptyMessage(NO_CHANNEL);
						Tools.showToastMessage(getActivity(), "该分类没有频道列表");
					}
				}
			}
		});
	}
	//获取华为的频道列表
	private void getChannelList(){
		Log.i(TAG, "get channel list---");
		String url = GlobalFilmData.getInstance().getEpgUrl() +"/defaultHD/en/datajspHD/getRecChan.jsp";
		Log.d(TAG, "url=" + url);
		String EPG_COOKIE = GlobalFilmData.getInstance().getCookieString();
		serviceHelper.setRootUrl(url);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", EPG_COOKIE) });
		RequestParams requestParams = new RequestParams();
		
		serviceHelper.callServiceAsync(getActivity(), requestParams, BtvChannel[].class, new ResponseHandlerT<BtvChannel[]>() {
			
			@Override
			public void onResponse(boolean success, BtvChannel[] result) {
				if(!success){
					Tools.showToastMessage(mActivity, "获取数据失败");
					return;
				}
				if(result != null){
					Log.d(TAG, "result-->"+gson.toJson(result));
					channelList = result;
					if(channelList != null && channelList.length > 0){
						for (int i = 0; i < channelList.length; i++) {
							typeList.add(channelList[i].getCataName());
						}
						Log.i(TAG, typeList.size()+"");
						mHandler.sendEmptyMessage(UPDATE_TYPE);
					}else{
						Tools.showToastMessage(mActivity, "获取节目列表失败，请重试");
						mActivity.finish();
					}
				}
			}
		});
	}
	//频道分类
	class ChannelTypeListAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			 return typeList.size();
		}

		@Override
		public Object getItem(int position) {
			 return typeList.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(
						R.layout.btv_channel_type_item, null, false);
			}
			TextView channelType = (TextView) convertView
					.findViewById(R.id.channel_type_name);
			channelType.setText(typeList.get(position));
			convertView.setTag(typeList.get(position));
			return convertView;
		}
	}
	//欢网的数据
	class ChannelPagerAdapter extends PagerAdapter {
		private List<Channel> list;
		
		public ChannelPagerAdapter(List<Channel> list){
			this.list = list;
		}
		
		@Override
		public float getPageWidth(int position) {
			return (PAGE_ROW + 0.0f) / MAX_PAGE_ROW;
		}

		@Override
		public int getCount() {
			int count = list == null ? 0 : list.size();
			int rowDataCount = ROW_SIZE * PAGE_ROW;
			return count % rowDataCount == 0 ? count / rowDataCount : (count / rowDataCount + 1);
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
			View view = (View)object;
			container.removeView(view);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View itemView = View.inflate(mActivity, R.layout.live_series_grid, null);
			WeightGridLayout grid = (WeightGridLayout) itemView.findViewById(R.id.series_grid);
			grid.setClipToPadding(false);
			grid.setTag(position);
			grid.setAdapter(new ChnWeightGridAdapter(list,position));
			itemView.setTag(position);
			container.addView(itemView);
			return itemView;
		}
		
	}
	/**
	 * 获得一页显示的总影片数
	 * */
	private int getPageDataCount() {
		return ROW_SIZE * PAGE_ROW;
	}
	//欢网的数据
	class ChnWeightGridAdapter extends WeightGridAdapter {
		private List<Channel> list;
		int row = 0;
		
		public ChnWeightGridAdapter(List<Channel> list,int position) {
			this.list = list;
			this.row = position;
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
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.btv_channel_item, parent, false);
			TextView channelName = (TextView) convertView.findViewById(R.id.channel_name);
			ImageView channelIcon = (ImageView)convertView.findViewById(R.id.channel_icon);
			final Channel channel = list.get(row * getPageDataCount() + position);
			String name = channel.getName();
			channelName.setText(name);
			ImageFetcher mFetcher = SharedImageFetcher.getSharedFetcher(mActivity);
			mFetcher.loadImage(channel.getLogo(), channelIcon);
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					//点击跳到节目列表
					mActivity.setSecondTitle(channel.getName());
					mActivity.showProgramFragment("475");
				}
			});
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
			return ROW_SIZE;
		}

		@Override
		public int getYSize() {
			return PAGE_ROW;
		}
		
		@Override
		public int getXSpace() {
			return 10;
		}
		
		@Override
		public int getYSpace() {
			return 0;
		}
		
		private int getTotalFilmSize() {
			return list.size();
		}
	}
	
	
	//华为的数据
	class HwChannelPagerAdapter extends PagerAdapter {
		private List<HwChannel> list;
		
		public HwChannelPagerAdapter(List<HwChannel> list){
			this.list = list;
		}
		
		@Override
		public float getPageWidth(int position) {
			return (PAGE_ROW + 0.0f) / MAX_PAGE_ROW;
		}

		@Override
		public int getCount() {
			int count = list == null ? 0 : list.size();
			int rowDataCount = ROW_SIZE * PAGE_ROW;
			return count % rowDataCount == 0 ? count / rowDataCount : (count / rowDataCount + 1);
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
			View view = (View)object;
			container.removeView(view);
		}
		
		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View itemView = View.inflate(mActivity, R.layout.live_series_grid, null);
			WeightGridLayout grid = (WeightGridLayout) itemView.findViewById(R.id.series_grid);
			grid.setClipToPadding(false);
			grid.setTag(position);
			grid.setAdapter(new HwWeightGridAdapter(list,position));
			itemView.setTag(position);
			container.addView(itemView);
			return itemView;
		}
	}
	//华为的数据
	class HwWeightGridAdapter extends WeightGridAdapter {
		private List<HwChannel> list;
		int row = 0;
		
		public HwWeightGridAdapter(List<HwChannel> list,int position) {
			this.list = list;
			this.row = position;
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
			convertView = LayoutInflater.from(mActivity).inflate(R.layout.btv_channel_item, parent, false);
			TextView channelName = (TextView) convertView.findViewById(R.id.channel_name);
			ImageView channelIcon = (ImageView)convertView.findViewById(R.id.channel_icon);
			final HwChannel channel = list.get(row * getPageDataCount() + position);
			String name = channel.getChannelName();
			channelName.setText(name);
			ImageFetcher mFetcher = SharedImageFetcher.getSharedFetcher(mActivity);
			mFetcher.loadImage(channel.getLogo(), channelIcon);
			convertView.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					//点击跳到节目列表
					mActivity.setSecondTitle(channel.getChannelName());
					mActivity.showProgramFragment(channel.getChannelId());
				}
			});
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
			return ROW_SIZE;
		}

		@Override
		public int getYSize() {
			return PAGE_ROW;
		}
		
		@Override
		public int getXSpace() {
			return 10;
		}
		
		@Override
		public int getYSpace() {
			return 0;
		}
		
		private int getTotalFilmSize() {
			return list.size();
		}
	}
}
