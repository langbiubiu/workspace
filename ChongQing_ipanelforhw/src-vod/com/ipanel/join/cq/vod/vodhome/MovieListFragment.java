package com.ipanel.join.cq.vod.vodhome;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;

import android.app.Activity;
import android.app.Fragment;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.ImageWorker;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.IFrameIndicator;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.MovieListResponse;
import com.ipanel.join.protocol.huawei.cqvod.MovieListResponse.MovieData;
import com.ipanel.join.protocol.huawei.cqvod.ProgramaResponse;
//电视剧电影列表界面;
public class MovieListFragment extends Fragment{
	/**
	 * 栏目数据加载完成消息
	 * */
	public static final int MSG_COLUME_DATA_LOADED = 0;
	
	/**
	 * 一页影片数据加载完成消息
	 * */
	public static final int MSG_ROW_DATA_LOAED = 1;
	/**
	 * 焦点移动消息
	 * */
	public static final int MSG_MOVE_FOCUS_INDICATOR = 2;
	/**
	 * 请求超时消息
	 * */
	public static final int MSG_REQUEST_TIME_UP = 3;
	/**
	 * 栏目分类加载
	 */
	private static final int UPDATE_TYPE_LIST = 4;
	/**
	 * 一行显示多少影片
	 * */
	public static final int ROW_SIZE = 5;
	/**
	 * 一屏显示几行
	 * */
	public static final int PAGE_ROW = 1;
	/**
	 * ViewPager一屏显示几行
	 * */
	public static final int MAX_PAGE_ROW = 2;
	/**
	 * 栏目通排标志
	 * */
	public static final String BIG_COLUME_ID = "-1";

	/**
	 * 一次性请求影片数据的最大数
	 * */
	public static final int REQUEST_DATA_SIZE = 20;

	/**
	 * 影片数据缓存
	 * */
	private final HashMap<String, MovieData[]> mDataMap = new HashMap<String, MovieData[]>();

	/**
	 * 栏目数据缓存
	 * */
	private ProgramaResponse[] programaResponse;
	/**
	 * EPG服务器地址
	 * */
	private String rootUrl;
	/**
	 * 子栏目ID
	 * */
	private String currenttypeId = "-1";
	/**
	 * 顶级栏目ID
	 * */
	private String rootTypeId = "";//栏目id
	private String columnName;//栏目名称
	/**
	 * 当前Activity是否处于前台状态
	 * */
//	private boolean mFront = false;
	/**
	 * 当前已加载栏目个数
	 * */
	private int colume_counter = 0;
	/**
	 * 根栏目是否有子栏目
	 * */
	private boolean mSub = false;
	/**
	 * 是否获取到数据
	 * */
//	private boolean hasData = false;

	private VerticalViewPager2 mViewPager;
	private TextView tvSeconditem;
	private TextView tvFirstitem;

	private ServiceHelper serviceHelper;
	private ImageFetcher mImageFetcher;
	private VODFrameIndicator indicator;
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	private ListView mTypeListView;//栏目分类
	private ImageView mImageTypeArrow;
	private TextView secondTitle;//小标题
	private TextView page;
	private TextView totalPage;
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_COLUME_DATA_LOADED:
				mViewPager.setAdapter(new VODPagerAdapter());
				//mViewPager.requestFocus();
				refreshPage(1);
				moveFocus();
				break;
			case MSG_ROW_DATA_LOAED:
				if (msg.obj instanceof RowRequestParam) {
					RowRequestParam param = (RowRequestParam) msg.obj;
					if (mViewPager.findViewWithTag(param.row) == null) {
						return;
					}
					WeightGridLayout grid = (WeightGridLayout) mViewPager.findViewWithTag(param.row).findViewById(
							R.id.hotgrid);
					if (grid != null) {
						refeshWeightLayoutChild(param, grid);
					}
				}
				break;
			case MSG_MOVE_FOCUS_INDICATOR:
				moveIndicatorToFocus();
				break;
			case MSG_REQUEST_TIME_UP:
				Tools.showToastMessage(getActivity(),"网络异常，请稍后重试");
				getActivity().finish();
				break;
			case UPDATE_TYPE_LIST:
				if (programaResponse != null && programaResponse.length > 0
						&& !mSub) {
					mTypeListView.setAdapter(new TypeAdapter());
				}
				break;
			}
		}
	};

	private Runnable initThread = new Runnable() {
		@Override
		public void run() {
			Log.i("", "GlobalFilmData.getInstance().getCardID() start");
			int i = 0;
			do {
//				if (!mFront) {
//					Logger.d("initThread return");
//					return;
//				}
				try {
					Thread.sleep(100);
					if (i > 50) {
						break;
					} else {
						i++;
					}
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			} while (GlobalFilmData.getInstance().getCardID() == null);
			Log.i("", "GlobalFilmData.getInstance().getCardID() ="+GlobalFilmData.getInstance().getCardID());
			if (i > 50) {
				Logger.d("i>50");
			} else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						initHuaWeiData();
					}
				});
			}
		}
	};
	
	public MovieListFragment(String volumeName,String typeId,boolean mSub){
		this.columnName = volumeName;
		this.rootTypeId = typeId;
		this.mSub = mSub;
	}
	
	@Override
	public void onAttach(Activity activity) {
		//当Fragment与Activity发生关联时调用。
		super.onAttach(activity);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		//创建该Fragment的视图
		View view = inflater.inflate(R.layout.vod_filmlist_layout_01, container,false);
		initControl(view);
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		//当Activity的onCreate方法返回时调用
		super.onActivityCreated(savedInstanceState);
		mDataMap.clear();
		tvFirstitem.setText(columnName);
		serviceHelper = ServiceHelper.createOneHelper();
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.cancelAllTasks();
		mImageFetcher = SharedImageFetcher.getSharedFetcher(getActivity());
		//mHandler.sendEmptyMessageDelayed(MSG_REQUEST_TIME_UP, 20*1000);
		new Thread(initThread).start();
	}
	
	@Override
	public void onStart() {
		super.onStart();
	}
	
	@Override
	public void onResume() {
		super.onResume();
	}
	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	public void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}
	@Override
	public void onDestroyView() {
		// 与onCreateView对应，当该Fragment的视图被移除时调用
		super.onDestroyView();
	}
	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	@Override
	public void onDetach() {
		// 与onAttach相对应，当Fragment与Activity关联被取消时调用
		super.onDetach();
	}
	/**
	 * 翻页
	 * */
	private void changePage(boolean down) {

		int max = getTotalPageSize();
		int current = mViewPager.getCurrentItem();
		if (down) {
			if (current < max - 1) {
				mViewPager.setCurrentItem(++current, true);
			}
		} else {
			if (current >= 0) {
				mViewPager.setCurrentItem(--current, true);
			}
		}
	}

	public void initControl(View view) {
		mViewPager = (VerticalViewPager2)view.findViewById(R.id.viewpager);
		tvFirstitem = (TextView)view.findViewById(R.id.title);
		tvSeconditem = (TextView)view.findViewById(R.id.secondtitle);
		mTypeListView = (ListView)view.findViewById(R.id.vod_sort_up);//栏目分类
		mTypeListView.setItemsCanFocus(true);
		mImageTypeArrow = (ImageView)view.findViewById(R.id.vod_type_arrow);
		secondTitle = (TextView)view.findViewById(R.id.secondtitle);
		page = (TextView)view.findViewById(R.id.page);
		totalPage = (TextView)view.findViewById(R.id.totalpage);
		
		mViewPager.setMove_flag(false);
		mViewPager.setOffscreenPageLimit(2);
		mViewPager.setPageScrollDuration(100);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

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
					moveFocus();
					mImageFetcher.setPauseWork(false);
				} else {
					mImageFetcher.setPauseWork(true);
					getFrameIndicator().hideFrame();
				}
			}
		});
		mViewPager.getViewTreeObserver().addOnGlobalFocusChangeListener(new OnGlobalFocusChangeListener() {
			@Override
			public void onGlobalFocusChanged(View oldFocus, View newFocus) {
				if (mViewPager.getScrollState() == VerticalViewPager2.SCROLL_STATE_IDLE) {
					if (newFocus instanceof FrameLayout) {
						moveFocus();
					}
				}
			}
		});
		
		mTypeListView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int position, long arg3) {
				if(programaResponse != null){
					currenttypeId = programaResponse[position].getTypeId();
					secondTitle.setText(programaResponse[position].getTypeName());
					if(programaResponse!=null && programaResponse.length > 6){
						if(position < (programaResponse.length-1)){
							mImageTypeArrow.setVisibility(View.VISIBLE);
						}else{
							mImageTypeArrow.setVisibility(View.INVISIBLE);
						}
					}
					mHandler.obtainMessage(MSG_COLUME_DATA_LOADED).sendToTarget();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
		});
	}

	/**
	 * 请求刷新某一行的界面
	 * */
	public void requestRowData(int row) {
		List<RowRequestParam> params = getRowRequestParam(row);
		final int count = params.size();
		LogHelper.i(String.format("-----------------start reqeust row : %s -----------------", row + ""));
		for (int i = 0; i < count; i++) {
			final RowRequestParam param = params.get(i);
			LogHelper.i(param.toString());
			if (checkRowData(param)) {
				noticyRowData(param);
			} else {
				RequestParams requestParams = new RequestParams();
				requestParams.put("centerTypeId", param.columeID);
				requestParams.put("pageNo", param.data_start + "");
				int req_count = Math.min(getColumeFilmCount(param.columeID) - param.data_start, REQUEST_DATA_SIZE);
				LogHelper
						.i(String.format("request %s from :%s size : %s", param.columeID, param.data_start, req_count));
				requestParams.put("showNums", req_count + "");
				serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", GlobalFilmData.getInstance()
						.getCookieString()) });
				serviceHelper.setRootUrl(rootUrl + "/datajspHD/android_getVodListByNum_data.jsp");
				serviceHelper.callServiceAsync(getActivity(), requestParams, MovieListResponse.class,
						new ResponseHandlerT<MovieListResponse>() {
							@Override
							public void onResponse(boolean success, MovieListResponse result) {
								if (success && result != null && result.getMovieList() != null) {
									fillColumeData(result, param);
								}
							}
						});
			}

		}
		LogHelper.i(String.format("-----------------end reqeust row : %s -----------------", row + ""));
	}

	/**
	 * 判断某行的数据是否完整
	 * */
	private boolean checkRowData(RowRequestParam param) {
		MovieData[] data = mDataMap.get(param.columeID);
		boolean result = true;
		for (int i = param.data_start; i < param.data_start + param.count; i++) {
			if ((param.data[i - param.data_start] = data[i]) == null) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * 填充影片数据
	 * */
	public void fillColumeData(MovieListResponse result, RowRequestParam param) {
		LogHelper.i(String.format("fill %s  ' colume film data %s  betwen %s and %s", param.columeID, result
				.getMovieList().size(), param.data_start, param.data_start + param.count));
		MovieData[] data = mDataMap.get(param.columeID);
		int sub_count = result.getMovieList().size();
		int count = getColumeFilmCount(param.columeID);
		for (int i = 0; i < param.count; i++) {
			data[(param.data_start + i) % count] = result.getMovieList().get(i % sub_count);
		}
		if (checkRowData(param)) {
			noticyRowData(param);
		}
	}

	/**
	 * 通知刷新界面
	 * */
	public void noticyRowData(RowRequestParam param) {
		LogHelper.i("nocity refresh row : " + param.row);
		mHandler.obtainMessage(MSG_ROW_DATA_LOAED, param).sendToTarget();
	}

	/**
	 * 初始化数据
	 * */
	public void initHuaWeiData() {
		Logger.d("initHuaWeiData start: " + System.currentTimeMillis());
//		if (!"0".equals(GlobalFilmData.getInstance().getIcState())) {
//			Tools.showToastMessage(this, getResources().getString(R.string.icerror));
//			finish();
//		}
		Logger.d("initHuaWeiData()");
		rootUrl = GlobalFilmData.getInstance().getEPGBaseURL();
		serviceHelper.setRootUrl(rootUrl + "/datajspHD/android_getTypeList_data.jsp");

		if ("-1".equals(rootTypeId)) {
			ServiceHelper.getHelper().setRootUrl("asset:///hanju_colume.xml");
		}

		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", GlobalFilmData.getInstance()
				.getCookieString()) });
		if (mSub) {
			programaResponse = new ProgramaResponse[1];
			ProgramaResponse colume = new ProgramaResponse();
			colume.setTypeName("");
			colume.setTypeId(rootTypeId);
			colume.setIsSubType("1");
			programaResponse[0] = colume;
			requestAllColumeFilmCount();
		} else {
			final RequestParams requestParams = new RequestParams();
			requestParams.put("typeId", rootTypeId);
			Gson gson = new Gson();
			Logger.d("MovieListFragment", "request-->"+gson.toJson(requestParams));
			serviceHelper.callServiceAsync(getActivity(), requestParams, ProgramaResponse[].class,
					new ResponseHandlerT<ProgramaResponse[]>() {
						@Override
						public void onResponse(boolean success, ProgramaResponse[] result) {
							if (success) {
								if (result != null && result.length > 0) {
//									hasData = true;
									programaResponse = result;
									currenttypeId = programaResponse[0].getTypeId();
									requestAllColumeFilmCount();
									return;
								}
							}
							Tools.showToastMessage(getActivity(), "该栏目暂无内容");
							LogHelper.i("fail to get columes");
							getActivity().finish();
						}
					});
		}

	}

	public void requestAllColumeFilmCount() {
		colume_counter = 0;

		for (ProgramaResponse p : programaResponse) {
			requestColumeFilmCount(p);
		}
	}

	/**
	 * 请求某个栏目数据
	 * */
	private void requestColumeFilmCount(ProgramaResponse program) {
		LogHelper.i("request colume " + program.getTypeId());

		final String key = program.getTypeId();
		RequestParams requestParamsMovieList = new RequestParams();
		requestParamsMovieList.put("centerTypeId", key);
		requestParamsMovieList.put("pageNo", "1");
		requestParamsMovieList.put("showNums", "1");
		serviceHelper.setRootUrl(rootUrl + "/datajspHD/android_getVodList_data.jsp");
		serviceHelper.setHeaders(new Header[] { new BasicHeader("Cookie", GlobalFilmData.getInstance()
				.getCookieString()) });
		serviceHelper.callServiceAsync(getActivity(), requestParamsMovieList, MovieListResponse.class,
				new ResponseHandlerT<MovieListResponse>() {
					@Override
					public void onResponse(boolean success, MovieListResponse result) {
						int count = Integer.parseInt(result == null ? "0" : result.getTotalNums());
						mDataMap.put(key, new MovieData[count]);
						noticyColumeCount(key);
					}
				});
	}

	/**
	 * 获得某个影片的影片数
	 * */
	public int getColumeFilmCount(String key) {
		return mDataMap.get(key) == null ? 0 : mDataMap.get(key).length;
	}

	/**
	 * 通知某个栏目数据加载完成
	 * */
	public void noticyColumeCount(String key) {
		LogHelper.i(String.format("nocicy film colume %s : %s", key, getColumeFilmCount(key)));
		colume_counter++;
		LogHelper.i("colume_counter: " + colume_counter);
		if (colume_counter >= programaResponse.length) {
			LogHelper.i("colume load finish ");
			mHandler.removeMessages(MSG_REQUEST_TIME_UP);
			mHandler.sendEmptyMessage(UPDATE_TYPE_LIST);
			mHandler.obtainMessage(MSG_COLUME_DATA_LOADED).sendToTarget();
		}
	}

	/**
	 * 初始化焦点
	 * */
	protected IFrameIndicator getFrameIndicator() {
		if (indicator == null) {
			indicator = new VODFrameIndicator(getActivity());
			indicator.setFrameResouce(R.color.transparent);
		}
		return indicator;
	}
	/**
	 * 刷新行数
	 */
	private void refreshPage(int i) {
		page.setText(i+"");
		totalPage.setText("/"+getTotalPageSize()+"页");
	}
	/**
	 * 获得ViewPager的总行数
	 * */
	private int getTotalPageSize() {
		int count = getTotalFilmSize();
		return count % getPageDataCount() == 0 ? (count / getPageDataCount()) : (count / getPageDataCount() + 1);
	}

	/**
	 * 获得栏目总影片数
	 * */
	private int getTotalFilmSize() {
		int count = 0;

		if (getColumeIndex() < 0) {
			Iterator iter = mDataMap.entrySet().iterator();
			while (iter.hasNext()) {
				Map.Entry entry = (Map.Entry) iter.next();
				String key = entry.getKey() + "";
				int val = getColumeFilmCount(key);
				count += val;
			}
		} else {
			Log.i("", "mDataMap="+mDataMap);
			Log.i("", "mDataMap.get(currenttypeId)="+mDataMap.get(currenttypeId));
			if (mDataMap.get(currenttypeId)==null) {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						initHuaWeiData();
					}
				});
			}
			else
			count = mDataMap.get(currenttypeId).length;
		}

		return count;
	}

	/**
	 * 计算ViewPager一行的数据请求参数
	 * */
	private List<RowRequestParam> getRowRequestParam(int row) {
		List<RowRequestParam> params = new ArrayList<RowRequestParam>();
		int current = 0;
		int start = row * getPageDataCount();
		int end = start + getPageDataCount();
		int columes = getColumecCount();
		int index = getColumeIndex();
		if (index < 0) {
			for (int i = 0; i < columes; i++) {
				String key = programaResponse[i].getTypeId() + "";
				int val = getColumeFilmCount(key);
				if (current + val < start) {
					current += val;
				} else {
					int offset = current + val - start;
					if (offset >= end - start) {
						RowRequestParam param = new RowRequestParam();
						param.columeID = key;
						param.data_start = start - current;
						param.view_start = getPageDataCount() - end + start;
						param.count = end - start;
						param.row = row;
						param.data = new MovieData[param.count];
						params.add(param);
						break;
					} else {
						RowRequestParam param = new RowRequestParam();
						param.columeID = key;
						param.data_start = start - current;
						param.view_start = getPageDataCount() - end + start;
						param.count = offset;
						param.data = new MovieData[param.count];
						param.row = row;
						params.add(param);
						start = start + offset;
						current += val;
					}
				}
			}
		} else {
			String key = programaResponse[index].getTypeId() + "";
			int val = getColumeFilmCount(key);
			RowRequestParam param = new RowRequestParam();
			param.columeID = key;
			param.data_start = start;
			param.view_start = 0;
			param.count = (end > val) ? val % getPageDataCount() : getPageDataCount();
			param.row = row;
			param.data = new MovieData[param.count];
			params.add(param);
		}

		return params;
	}

	/**
	 * 获得一页显示的总影片数
	 * */
	private int getPageDataCount() {
		return ROW_SIZE * PAGE_ROW;
	}

	/**
	 * 移动焦点
	 * **/
	public void moveFocus() {
		mHandler.removeMessages(MSG_MOVE_FOCUS_INDICATOR);
		mHandler.sendEmptyMessageDelayed(MSG_MOVE_FOCUS_INDICATOR, 30);
	}

	/**
	 * 跳转到详情页
	 * */
	public void startToDetail(String vodId, String name,String playType) {
		HWDataManager.openDetail(getActivity(), vodId, name, playType, true);
	}

	/**
	 * 清除无效的图片加载任务
	 * */
	private void cleaTasks(ViewGroup container) {
		int N = container.getChildCount();
		for (int i = 0; i < N; i++) {
			if (container.getChildAt(i).getTag() instanceof BaseImageFetchTask) {
				ImageWorker.cancelPotentialWork((ImageFetchTask) container.getChildAt(i).getTag(),
						container.getChildAt(i));
			} else {
				if (container.getChildAt(i) instanceof ViewGroup) {
					cleaTasks((ViewGroup) container.getChildAt(i));
				}
			}
		}
	}

	/**
	 * 刷新子栏目名称
	 * */
	private void refreshSecondItem() {
		int index = getColumeIndex();
		if (index < 0) {
			tvSeconditem.setText("");
		} else {
			tvSeconditem.setText(programaResponse[index].getTypeName());
		}
	}

	/**
	 * 获取当前栏目的索引，如果大循环返回-1
	 * */
	private int getColumeIndex() {
		int count = getColumecCount();
		for (int i = 0; i < count; i++) {
			if (currenttypeId.equals(programaResponse[i].getTypeId())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * 获取栏目总数
	 * */
	private int getColumecCount() {
		return programaResponse == null ? 0 : programaResponse.length;
	}

	/**
	 * 移动焦点
	 * */
	public void moveIndicatorToFocus() {
		if(isAdded()){
			View focus = getActivity().getCurrentFocus();
			if (focus instanceof FrameLayout) {
				getFrameIndicator().moveFrameTo(focus, true, false);
				focus.findViewById(R.id.film_name).setSelected(true);
			}
		}
	}

	/**
	 * 刷新影片信息
	 * */
	private void refeshWeightLayoutChild(RowRequestParam param, WeightGridLayout layout) {
		int start = param.view_start;
		int end = Math.min(layout.getChildCount(), start + param.count);
		for (int i = param.view_start; i < end; i++) {
			View convertView = layout.getChildAt(i);
			final MovieData vodFilm = param.data[(i - param.view_start) % param.data.length];
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);

			if (vodFilm == null) {
				image.setBackgroundResource(R.drawable.default_poster);
			} else {
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (Tools.isOnline(getActivity()) == false) {
							Tools.showToastMessage(getActivity(), getResources().getString(R.string.networkoff));
							getActivity().finish();
						} else {
							GlobalFilmData.getInstance().setVodId(vodFilm.getVodId());
							startToDetail(vodFilm.getVodId(), vodFilm.getVodName(),vodFilm.getPlayType());
						}
					}
				});
				name.setText(vodFilm.getVodName());
				if (vodFilm.getPicPath() != null) {
					BaseImageFetchTask alphaTask = mImageFetcher.getBaseTask(Tools.replace(vodFilm.getPicPath(),
							"../..", GlobalFilmData.getInstance().getEpgUrl()));
					mImageFetcher.setLoadingImage(R.drawable.default_poster);
					mImageFetcher.setImageSize(241, 301);
					mImageFetcher.loadImage(alphaTask, image);
					image.setTag(alphaTask);
				} else {
					image.setBackgroundResource(R.drawable.default_poster);
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
			// TODO Auto-generated method stub
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
			cleaTasks(v);
			views.push(new SoftReference<View>(v));
			container.removeView(v);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			Logger.d("enter instantiateItem: " + position);
			long time=System.currentTimeMillis();
			View itemView = getViewFromSoftReference();
			if (itemView == null) {
				LogHelper.e("inflate from layout ");
				itemView = View.inflate(getActivity(), R.layout.vod_hotpageritem, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.hotgrid);
			mWeightLayout.setClipToPadding(false);
			mWeightLayout.setTag(position);
			mWeightLayout.setAdapter(new VODWeightGridAdapter(position));
			itemView.setTag(position);
			requestRowData(position);
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
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.vod_hot_element3, parent, false);
			}
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
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

			// convertView.setTag(position / ROW_SIZE + "");
			convertView.setTag(row + "");
			if (row * getPageDataCount() + position >= getTotalFilmSize()) {
				convertView.setVisibility(View.GONE);
			} else {
				convertView.setVisibility(View.VISIBLE);
			}

			image.setBackgroundResource(R.drawable.default_poster);
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
		public int getYSpace() {
			return 35;
		}

		@Override
		public int getXSpace() {
			return 45;
		}

	}
	
	class TypeAdapter extends BaseAdapter{
		
		@Override
		public int getCount() {
			return programaResponse.length;
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
		public View getView(int position, View convertView, ViewGroup container) {
			if(convertView == null){
				convertView = LayoutInflater.from(getActivity()).inflate(R.layout.vod_type_item, null,false);
			}
			TextView type = (TextView)convertView.findViewById(R.id.type_text);
			type.setText(programaResponse[position].getTypeName());
			convertView.setTag(position);
			return convertView;
		}
	}
	public class RowRequestParam {
		public String columeID;
		public int count;
		public int data_start;
		public int view_start;
		public int row;
		public MovieData[] data;

		@Override
		public String toString() {
			return "RowRequestParam [columeID=" + columeID + ", count=" + count + ", data_start=" + data_start
					+ ", view_start=" + view_start + "]";
		}
	}
}
