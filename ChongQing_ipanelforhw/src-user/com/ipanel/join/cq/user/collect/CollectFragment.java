package com.ipanel.join.cq.user.collect;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.support.v4.view.ViewPager2;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.Scroller;
import android.widget.TextView;
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

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Channel;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.user.BaseFragment;
import com.ipanel.join.cq.user.UserActivity;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.detail.VodTabActivity;
import com.ipanel.join.cq.vod.player.VodDataManager;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class CollectFragment extends BaseFragment{
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
	 * 请求超时消息
	 **/
	public static final int MSG_REQUEST_TIME_UP = 3;
	/**
	 * 删除影片后新影片获得焦点
	 */
	public static final int MSG_FOCUS = 4;
	/**
	 * 右侧标题栏获得焦点
	 */
	public static final int MSG_REFOCUS = 5;
	/**
	 * 改变焦点
	 */
	public static final int MSG_CHANGE_FOCUS = 6;
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
	
	private int totalCollectionNumber = -1;
	private ImageFetcher mImageFetcher;
	private Channel[] mData;
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	View view;
	private UserActivity myActivity ;
	//private MyPopupWindow myWindow;
	private boolean isUninstallState ;
	private LinearLayout delOne;
	private LinearLayout delAll;
	private Button del_cancel;
	private Button del_sure;
	VODPagerAdapter adapter;
	int count = 0;
	int num = 0;
	LinearLayout collect_linear;
	TextView user_collect_curpage, user_collect_totalpage;
	View title;
	View nextView;
	TextView collect;
	protected ServiceHelper serviceHelper;
	
	private Map<String,Integer>map = new HashMap<String,Integer>();
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			//栏目数据加载完成
			case MSG_COLUME_DATA_LOADED:
				Logger.d("-->msg MSG_COLUME_DATA_LOADED");
				viewPager.setAdapter(adapter = new VODPagerAdapter());
				if(totalCollectionNumber > 0 ){
					collect.setVisibility(View.VISIBLE);
				}
				user_collect_totalpage.setText(getTotalPageSize()+"");
				//viewPager.requestFocus();
				break;
			//一页影片数据加载完成	
			case MSG_ROW_DATA_LOAED:
				if (msg.obj instanceof RowRequestParam) {
					RowRequestParam param = (RowRequestParam) msg.obj;
					if (viewPager.findViewWithTag(param.row) == null) {
						return;
					}
					Logger.d("-->msg 一页影片数据加载完成	");
					WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(param.row).
							findViewById(R.id.collectgrid);
					if (grid != null) {
						refeshWeightLayoutChild(param, grid,param.row);
						Logger.d("-->handler刷新影片信息");
					}
				}
				break;
			case MSG_FOCUS:
				View focusView = (View) msg.obj;
				focusView.requestFocus();
				Logger.d("-->msg MSG_FOCUS");
				break;
//			case MSG_REQUEST_TIME_UP:
//				//finish();
//				break;
//			case MSG_REFOCUS:
//				collect_linear.requestFocus();
//				break;

			}
		}
	};
	
	private Runnable initThread = new Runnable() {

		@Override
		public void run() {
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					// 获取数据
					initData();
				}

			});
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.user_collect_fragment, container, false);		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		myActivity = (UserActivity) getActivity();
		mImageFetcher = SharedImageFetcher.getNewFetcher(getActivity(), 3);
		view = getView();
		serviceHelper = ServiceHelper.getHelper();
		viewPager = (VerticalViewPager2)view.findViewById(R.id.collect_gd);
		user_collect_curpage = (TextView) view.findViewById(R.id.user_collect_curpage);
		user_collect_curpage.setText("0/");
		user_collect_totalpage = (TextView) view.findViewById(R.id.user_collect_totalpage);
		collect = (TextView) view.findViewById(R.id.collect);
		initControl();
		initBg();
		title = view.findViewById(R.id.user_collect_title);
		collect_linear = myActivity.collect_linear;
		isUninstallState = false;
		new Thread(initThread).start();
	}
	
	private void initBg(){
		map.put("0", R.drawable.user_tb_bg_1);
		map.put("1",R.drawable.user_tb_bg_2);
		map.put("2",R.drawable.user_tb_bg_3);
		map.put("3",R.drawable.user_tb_bg_4);
		map.put("4",R.drawable.user_tb_bg_5);
		map.put("5",R.drawable.user_tb_bg_6);
		map.put("6",R.drawable.user_tb_bg_7);
				
	}
	public void hideFocus(){
		title.setFocusable(true);
		title.requestFocus();
	}
	private void initControl() {
		viewPager.setMove_flag(false);
		viewPager.setOffscreenPageLimit(1);
		viewPager.setScroller(new Scroller(getActivity(), new LinearInterpolator()));
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				
			}

			@Override
			public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

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
				itemView = View.inflate(getActivity().getBaseContext() , R.layout.user_collect_griditem, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.collectgrid);
			mWeightLayout.setClipToPadding(false);
			mWeightLayout.setTag(position);		
		    mWeightLayout.setAdapter(new VODWeightGridAdapter(position,ROW_SIZE));			
			itemView.setTag(position);
			requestRowData(position);
			container.addView(itemView);
			LogHelper.e("instantiateItem view take time : "+(System.currentTimeMillis()-time));
			return itemView;
		}
	}
	
	private int getTotalFilmSize() {
		return totalCollectionNumber;
	}
	/**
	 * 获得ViewPager的总行数
	 * */
	private int getTotalPageSize() {
		int count = getTotalFilmSize();
		return count % getPageDataCount() == 0 ? (count / getPageDataCount()) : (count / getPageDataCount() + 1);
	}
	
	public void requestRowData(int row) {
		List<RowRequestParam> params = getRowRequestParam(row);
		final int count = params.size();
		for (int i = 0; i < count; i++){
			final RowRequestParam param = params.get(i);
			if (checkRowData(param)) {
				noticyRowData(param);
			}
		}
	}
	/**
	 * 计算ViewPager一行的数据请求参数
	 * */
	private List<RowRequestParam> getRowRequestParam(int row) {
		List<RowRequestParam> params = new ArrayList<RowRequestParam>();
		int start = row * getPageDataCount();
		int end = start + getPageDataCount();
		int val = totalCollectionNumber;

		RowRequestParam param = new RowRequestParam();
		param.data_start = start;
		param.view_start = 0;
		param.count = (end > val) ? val % getPageDataCount()
				: getPageDataCount();
		param.row = row;
		param.data = new Channel[param.count];
		params.add(param);

		return params;
	}
	
	/**
	 * 判断某行的数据是否完整
	 * */
	private boolean checkRowData(RowRequestParam param) {
		boolean result = true;
		for (int i = param.data_start; i < param.data_start + param.count; i++) {
			if ((param.data[i - param.data_start] = mData[i]) == null) {
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * 通知刷新界面
	 * */
	public void noticyRowData(RowRequestParam param) {
		LogHelper.i("nocity refresh row : " + param.row);
		mHandler.obtainMessage(MSG_ROW_DATA_LOAED, param).sendToTarget();
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
	class VODWeightGridAdapter extends WeightGridAdapter {
		
		int row;
		int xSize;

		public void setxSize(int xSize) {
			this.xSize = xSize;
		}
		
		public VODWeightGridAdapter(int row,int xSize) {
			this.row = row;
			this.xSize = xSize;
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
			if (convertView == null) {
				convertView = LayoutInflater.from(getActivity().getApplication()).inflate(R.layout.user_collect_pageritem, parent, false);
			}
			final ImageView image = (ImageView) convertView.findViewById(R.id.collect_pic);
			final TextView name = (TextView) convertView.findViewById(R.id.collect_name);
			final ImageView bg = (ImageView) convertView.findViewById(R.id.collect_back);
			final ImageView del = (ImageView)convertView.findViewById(R.id.user_delete6);
			final FrameLayout user_uninstall_fl = (FrameLayout)convertView.findViewById(R.id.user_uninstall_fl6);
			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					if(isUninstallState){
						//TODO 删除影片
						whichMov(row,position);
						//adapter.notifyDataSetChanged();
					}else{						
						//TODO播放影片
											
					}
				}
			});
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						user_collect_curpage.setText(row+1+"/");
						if(isUninstallState){
							user_uninstall_fl.setVisibility(View.VISIBLE);
							del.setBackgroundResource(R.drawable.user_del);
							name.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);
						}
						
					} else {
						user_uninstall_fl.setVisibility(View.GONE);
						name.setTextSize(TypedValue.COMPLEX_UNIT_PX,32);
					}
				}
			});
			convertView.setTag(row + "");
			if (row * getPageDataCount() + position >= getTotalFilmSize()) {
				convertView.setVisibility(View.GONE);
			} else {
				convertView.setVisibility(View.VISIBLE);
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

			return xSize;
		}
		
		@Override
		public int getYSize() {
			return PAGE_ROW;
		}

		@Override
		public int getYSpace() {
			return 51;
		}

		@Override
		public int getXSpace() {
			return 40;
		}

	}
	
	/**
	 * 刷新影片信息
	 * @param row 
	 * */
	private void refeshWeightLayoutChild(RowRequestParam param, WeightGridLayout layout, int row){
		int start = param.view_start;
		int end = Math.min(layout.getChildCount(), start + param.count);
		for (int i = param.view_start; i < end; i++){
			View convertView = layout.getChildAt(i);
			final Channel tvItem = param.data[(i - param.view_start)% param.data.length];
			final ImageView image = (ImageView) convertView.findViewById(R.id.collect_pic);
			final ImageView bg = (ImageView) convertView.findViewById(R.id.collect_back);
			final FrameLayout uninstall_fl = (FrameLayout) convertView.findViewById(R.id.user_uninstall_fl6);
			final TextView name = (TextView) convertView.findViewById(R.id.collect_name);

			if (tvItem.getLogo()!= null) {
				BaseImageFetchTask task = mImageFetcher.getBaseTask(tvItem.getLogo());			
				mImageFetcher.setLoadingImage(R.drawable.default_poster);
				mImageFetcher.loadImage(task, image);
			}else{
				image.setImageResource(R.drawable.default_poster);
			}
			if(isUninstallState){
				uninstall_fl.setVisibility(View.VISIBLE);
			}else{
				uninstall_fl.setVisibility(View.GONE);
			}
			int no_gb = (5*row+i)%7;
			bg.setBackgroundResource(map.get(no_gb+""));
			name.setText(tvItem.getName());
		}
	}
	/**
	 * 获得一页显示的总影片数
	 * */
	private int getPageDataCount() {
		return ROW_SIZE * PAGE_ROW;
	}
	/**
	 * 行请求参数
	 */
	public class RowRequestParam {
		public String columeID;
		public int count;
		public int data_start;
		public int view_start;
		public int row;
		public Channel[] data;
		@Override
		public String toString() {
			return "RowRequestParam [columeID=" + columeID + ", count=" + count + ", data_start=" + data_start
					+ ", view_start=" + view_start + "]";
		}
	}
	private void initData(){
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_FAV_CHANNELS_BY_USER);
		request.getParam().setPage(1);
		request.getParam().setPagesize(10);
		request.getParam().setShowlive(false);
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		serviceHelper.callServiceAsync(getActivity(), request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result.getError().getCode() == 0){
					List<Channel> list = result.getChannels();
					totalCollectionNumber = list.size();
					mData = new Channel[totalCollectionNumber];
					// 填充影片数据
					for (int i = 0; i < totalCollectionNumber; i++) {
						Channel item = list.get(i);
						mData[i] = item;
					}
					mHandler.obtainMessage(MSG_COLUME_DATA_LOADED)
					.sendToTarget();
				}else{
					Tools.showToastMessage(getActivity(), result.getError().getInfo());
				}
			}
		});
		
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View curView = getActivity().getCurrentFocus();
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:				
			showPopupWindow(curView);				
			return true;
		case KeyEvent.KEYCODE_BACK:
			if(isUninstallState){
				FrameLayout user_uninstall_fl = (FrameLayout) curView.findViewById(R.id.user_uninstall_fl6); 
				user_uninstall_fl.setVisibility(View.GONE);

				isUninstallState = false;				
				return true;
			}
			getActivity().finish();
			break;
		}		
		return false;
	}
	private void showPopupWindow(View curView ) {
		final View v = curView;
		Context mContext = getActivity().getApplicationContext();
		 View contentView = LayoutInflater.from(mContext).inflate(
	                R.layout.user_pop_window, null);		
		final PopupWindow popupWindow = new PopupWindow(contentView,
                LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, true);
		
		title.setFocusable(true);
		title.requestFocus();
		popupWindow.setOnDismissListener(new OnDismissListener() {				
			@Override
			public void onDismiss() {
				v.requestFocus();
				title.setFocusable(false);
			}
		});
		
      // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
      popupWindow.setBackgroundDrawable(getResources().getDrawable(
              R.drawable.user_buttom));
      // 设置好参数之后再show
        popupWindow.showAtLocation(view,Gravity.BOTTOM,0,0);
        
        delAll = (LinearLayout) contentView.findViewById(R.id.del_all);
		delAll.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				popupWindow.dismiss();
				deleteAll(v);					
			}				
		});
		delOne = (LinearLayout) contentView.findViewById(R.id.del_one);
		delOne.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View mov) {												
				popupWindow.dismiss();
				isUninstallState = true;
				//TODO 
				FrameLayout user_uninstall_fl = (FrameLayout) v.findViewById(R.id.user_uninstall_fl6); 
				if(user_uninstall_fl==null){
					return;
				}else{
				user_uninstall_fl.setVisibility(View.VISIBLE);
				ImageView user_delete2 = (ImageView) v.findViewById(R.id.user_delete6);
				user_delete2.setBackgroundResource(R.drawable.user_del);
				}
			}			
		});
		
    }
	protected  void deleteAll(final View v) {
		Context mContext = myActivity.getApplicationContext()  ;
		 View contentView = LayoutInflater.from(mContext).inflate(
	                R.layout.user_delall_pop, null);
		 final PopupWindow window = new PopupWindow(contentView,
	                LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		window.setBackgroundDrawable(getResources().getDrawable(
          R.color.back_gray2));
		TextView user_del_tips = (TextView) contentView.findViewById(R.id.user_del_tips);
		user_del_tips.setText(R.string.del_tips_colect);
		final View title = view.findViewById(R.id.user_collect_title);
		title.setFocusable(true);
		title.requestFocus();
		window.setOnDismissListener(new OnDismissListener() {				
			@Override
			public void onDismiss() {
				v.requestFocus();
				title.setFocusable(false);
			}
		});
		 del_cancel = (Button) contentView.findViewById(R.id.del_cancel);
		 del_cancel.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				window.dismiss();					
			}
		 });
		 del_sure = (Button) contentView.findViewById(R.id.del_sure);
		 del_sure.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				deleteMov("-1",0,0);				
			}				 
		 });
		 window.showAtLocation(view,Gravity.TOP|Gravity.LEFT,613,300);
	}
	//TODO 删除
	protected void delSubscribeTag(String channelId) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_DEL_FAV_CHANNELS_BY_USER);
		request.getParam().setChannelId(channelId);
		VodDataManager.getInstance(getActivity()).getHwData(request);
	}
	private void whichMov(int row,int position){
		Channel item = mData[row*5+position];
		deleteMov(item.getChannelId(),row,position);
	}
	
	protected  void deleteMov(final String id,final int row,final int position){
		if(id.equals("-1")){
			//全部删除
			for(int i=0;i<totalCollectionNumber;i++){
				String str = mData[i].getChannelId();
				delSubscribeTag(str);
			}
		}else{
			delSubscribeTag(id);
		}
		isUninstallState = true;
		mImageFetcher = SharedImageFetcher.getNewFetcher(myActivity, 3);
		final Thread refreash = new Thread(initThread);
		refreash.start();		
		final Timer timer = new Timer(true);
		int i = totalCollectionNumber%5==0 ? 5 :totalCollectionNumber%5;
		final int curNum = row==getTotalPageSize()-1 ? i : 5;
		timer.schedule(new TimerTask(){			
	      public void run(){
    		  if(id.equals("-1")){
    				return;
    			}
    			if(totalCollectionNumber==1){
    				return;
    			}
    		  if(position != 0 && position != curNum+1){
	    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row).
							findViewById(R.id.collectgrid);
	    			View nextView = grid.getChildAt(position); 
	    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
	    		}else if(position == 0 && curNum==1){
	    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row-1).
							findViewById(R.id.collectgrid);
	    			View nextView = grid.getChildAt(4); 
	    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
	    		}else if(position == curNum-1 && position !=0){
	    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row).
							findViewById(R.id.collectgrid);
	    			View nextView = grid.getChildAt(position - 1); 
	    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
	    		}											    	  
	       }
	   }, 500);
	}
}
