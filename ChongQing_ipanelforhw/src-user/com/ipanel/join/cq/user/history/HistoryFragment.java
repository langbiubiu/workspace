package com.ipanel.join.cq.user.history;
import ipanel.join.widget.ViewPager2;

import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.util.Log;
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
import android.widget.ProgressBar;
import android.widget.Scroller;
import android.widget.TextView;
import android.widget.Toast;
import cn.ipanel.android.LogHelper;
import cn.ipanel.android.Logger;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.net.imgcache.BaseImageFetchTask;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.WeightGridLayout;
import cn.ipanel.android.widget.WeightGridLayout.WeightGridAdapter;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.user.BaseFragment;
import com.ipanel.join.cq.user.UserActivity;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class HistoryFragment extends BaseFragment {
	private TodayAdapter adapter1;
	private EarlyAdapter adapter2;
	private VerticalViewPager2 viewPager;
	private LayoutInflater mInflater;
	private LinkedHashMap<String, List<Wiki>> listPictureInfos; 
	private LinkedHashMap<String, List<Wiki>> pictureHashMap;
	private ImageFetcher mImageFetcher;
	private LinearLayout delOne; 
	private LinearLayout delAll;
	private Button del_cancel;
	private Button del_sure;
	private View view;
	int count = 0;
	int num = 0;
	LinearLayout history_linear,watch_list_linear;
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	private boolean isUninstallState = false;
	private UserActivity myActivity;
	private List<Wiki> todayList;
	private List<Wiki> earlyList;
	int todayTotal,earlyTotal;
	TextView user_history_curpage,user_history_totalpage;
	View title;
	View nextView;
	private int ROW_SIZE = 5;
	/**
	 * 一屏显示几行
	 * */
	public static final int PAGE_ROW = 1;
	/**
	 * 栏目数据加载完成消息
	 * */
	public static final int MSG_COLUME_DATA_LOADED = 0;
	/**
	 * 一页影片数据加载完成消息
	 * */
	public static final int MSG_ROW_DATA_LOAED = 1;
	/**
	 * ViewPager一屏显示几行
	 * */
	public static final int MAX_PAGE_ROW = 2;
	/**
	 * 删除影片后新影片获得焦点
	 */
	public static final int MSG_FOCUS = 4;
	/**
	 * 右侧标题栏获得焦点
	 */
	public static final int MSG_REFOCUS = 5;
	/**
	 * 刷新焦点
	 */

	public static final int MSG_CHANGE_FOCUS=6;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		 View v = (inflater.inflate(R.layout.user_history_fragment, container, false));
		 return v;
	}
	
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			//栏目数据加载完成
			case MSG_COLUME_DATA_LOADED:
				Logger.d("-->msg MSG_COLUME_DATA_LOADED");
//				hideFocus();
				viewPager.setAdapter(new GuidePageAdapter());
				
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
							findViewById(R.id.gvfirst);
					if (grid != null) {
						refeshWeightLayoutChild(param, grid);
						Logger.d("-->handler刷新影片信息");
					}
				}
				break;
			case MSG_FOCUS:
				View focusView = (View) msg.obj;
				focusView.requestFocus();
				Log.i("nextFocus",focusView.toString());
				break;
			case MSG_REFOCUS:
				history_linear.requestFocus();
				break;
			}
		}
	};
	
	@SuppressLint("NewApi")
	@Override
	public void onViewCreated(View view1, Bundle savedInstanceState) {
		super.onViewCreated(view1, savedInstanceState);
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		view = getView();
		viewPager = (VerticalViewPager2) view.findViewById(R.id.user_history_viewpager);
		mInflater = (LayoutInflater) getActivity().getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		myActivity = (UserActivity) getActivity();
		user_history_totalpage = (TextView) view.findViewById(R.id.user_history_totalpage);
		user_history_curpage = (TextView) view.findViewById(R.id.user_history_curpage);
		user_history_curpage.setText("0/");
		mImageFetcher = SharedImageFetcher.getNewFetcher(getActivity(), 3);
		isUninstallState = false;
		history_linear = myActivity.history_linear;
		watch_list_linear = myActivity.watch_list_linear;
		title = findViewById(R.id.user_history_title); 
		initControl();
		new Thread(initThread).start();	
		Message message = mHandler.obtainMessage(MSG_REFOCUS);  
//		mHandler.sendMessageDelayed(message,400);
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
//	@Override
//	public void onHiddenChanged(boolean hide){
//		if(hide){
//			mImageFetcher = SharedImageFetcher.getNewFetcher(getActivity(), 3);
//			isUninstallState = false;
////			hideFocus();
//			new Thread(initThread).start();
////			watch_list_linear.requestFocus();
//		}
//	}
	public void hideFocus(){
		title.setFocusable(true);
		title.requestFocus();
	}
	private  Runnable initThread = new Runnable() {
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
		
	private  class GuidePageAdapter extends PagerAdapter {
		
		@Override
		public int getCount() {
			//return getEarlyRow();
			return getTodayRow()+getEarlyRow();
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
			View v = (View) object;
			container.removeView(v);
		}

		@Override
		public Object instantiateItem(ViewGroup container, final int position) {
			View itemView = getViewFromSoftReference();
			if (itemView == null) {
				itemView = View.inflate(getActivity().getBaseContext(), R.layout.user_history_common, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.gvfirst);
			mWeightLayout.setClipToPadding(false);
			mWeightLayout.setTag(position);	
			final TextView time = (TextView) itemView.findViewById(R.id.time_first);											
			if(position < getTodayRow()){
				if(position == 0){
					time.setText("今天");
				}else{
					time.setVisibility(View.GONE);
				}				
				adapter1 = new TodayAdapter(position, ROW_SIZE);				
				mWeightLayout.setAdapter(adapter1);
			}else{
				if(position == getTodayRow()){
					time.setText("更早");			
				}else{
					time.setVisibility(View.GONE);
				}

				adapter2 = new EarlyAdapter(position, ROW_SIZE);				
				mWeightLayout.setAdapter(adapter2);
			}						
			itemView.setTag(position);
			requestRowData(position);
			container.addView(itemView);
			return itemView;								
		}

		@Override
		public float getPageWidth(int position) {
			return (PAGE_ROW + 0.0f) / MAX_PAGE_ROW;  
		}
	}
	private  void initData(){/*
		pictureHashMap = new LinkedHashMap<String, List<Wiki>>();
		String url = "http://slave.homed.me/history/get_list";
		RequestParams req = new RequestParams();
		req.put("pageidx", "1");
		req.put("pagenum", "100");
		req.put("accesstoken", HomedDataManager.getToken());
		req.put("type", "2");
		req.put("postersize","336x406");
		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(url);
		
		helper.callServiceAsync(myActivity.getBaseContext(), req, RespHistoryList.class, new ResponseHandlerT<RespHistoryList>() {
						
			@Override
			public void onResponse(boolean success, RespHistoryList result) {
				if(!success){
					return;
				}
					if(result==null){
						//Toast.makeText(myActivity, "没有历史观看记录",Toast.LENGTH_SHORT).show();
						return;
					}
				
				if (result.list != null){
					List<Wiki> list = result.list;
					int totalMovieNumber = list.size();
					earlyList = new ArrayList<Wiki>();
					todayList = new ArrayList<Wiki>();
					for(int i = 0; i<totalMovieNumber; i++){
						Wiki item = list.get(i);
						if(isToday(item.last_used_time)){
							todayList.add(item);
						}else{
							earlyList.add(item);
						}
					}
					todayTotal = todayList.size();
					earlyTotal = earlyList.size();
					Log.i("today",todayTotal+"");
					Log.i("early",earlyTotal+"");
					if(todayList.size()==0){
						pictureHashMap.put("更早", todayList);
					}else{
						pictureHashMap.put("今天", todayList);
						pictureHashMap.put("更早", earlyList);
					}
									
					mHandler.removeMessages(MSG_COLUME_DATA_LOADED);
					Message message = mHandler.obtainMessage();
					message.what = MSG_COLUME_DATA_LOADED;
					message.obj = pictureHashMap;
					mHandler.sendMessage(message);
				}else{
					Tools.showToastMessage(getActivity(), getResources().getString(R.string.no_history));
				}
				if(todayList.size()==0){
					user_history_totalpage.setText(getEarlyRow()+"");
				}else{
					user_history_totalpage.setText(getTodayRow()+getEarlyRow()+"");
				}
			}
		});
				
	*/}
	
	//判断影片是否为今天
	public  boolean isToday(long time){
		SimpleDateFormat sf = null;
		Date d = new Date((time)*1000);
		sf = new SimpleDateFormat("yyyy_MM_dd");
		String str1 = sf.format(d); 
		Date curDate = new Date(System.currentTimeMillis());//获取当前时间 
		String str2 = sf.format(curDate);
		if(str1.equals(str2)){
			return true;       	
		}else{
			return false;
		}
	}
			
	protected  void deleteAll(final View curView) {
		Context mContext = myActivity.getApplicationContext();
		View contentView = LayoutInflater.from(mContext).inflate(
			   R.layout.user_delall_pop, null);
		final PopupWindow window = new PopupWindow(contentView,
				LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT, true);
		window.setBackgroundDrawable(getResources().getDrawable(
	           R.color.back_gray2));
		TextView user_del_tips = (TextView)contentView.findViewById(R.id.user_del_tips);
		user_del_tips.setText(R.string.del_tips_history);
		
		title.setFocusable(true);
		title.requestFocus();
		window.setOnDismissListener(new OnDismissListener() {						
				@Override
				public void onDismiss() {
					curView.requestFocus();	
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
				deleteMov(null,-1,-1);				
			}				 
		});
		window.showAtLocation(view,Gravity.TOP|Gravity.LEFT,613,300);
	}
			protected  void deleteMov(final String folderName,final int row, final int position){/*
				final int num = row*ROW_SIZE+position;
				String url = "http://slave.homed.me/history/delete";
				RequestParams req = new RequestParams();
				req.put("accesstoken", HomedDataManager.getToken());
				if (row == -1 && position ==-1){
					req.put("id", -1+"");
				}else{
					Wiki item ;					
					if(folderName.equals("今天")){
					item = todayList.get(num);
					}else{	
						item = earlyList.get(num-getTodayRow()*ROW_SIZE);
					}
					Long id = item.id;
					req.put("id", id+"");
				}
				ServiceHelper helper = ServiceHelper.getHelper();
				helper.setSerializerType(SerializerType.JSON);
				helper.setRootUrl(url);
				helper.callServiceAsync(myActivity.getBaseContext(), req, RespDelete.class, new ResponseHandlerT<RespDelete>() {
					@Override
					public void onResponse(boolean success, RespDelete result) {
						if(!success){
							return;
						}
						if (result.ret == 0){
							final int curNum;
							Toast.makeText(myActivity, "删除成功",
									Toast.LENGTH_SHORT).show();
							
							mImageFetcher = SharedImageFetcher.getNewFetcher(myActivity, 3);
							final Thread refreash = new Thread(initThread);
							refreash.start();
							if(position == -1&& row == -1){
								return;								
							}
							if( todayTotal + earlyTotal ==1){
								return;
							}
							if(folderName.equals("今天")){
				    			  int i = todayTotal%5 ==0 ? 5: todayTotal%5;
				    			  curNum = row==getTodayRow()-1 ? i : 5;
				    		  }else{
				    			  int i = earlyTotal%5 ==0 ? 5: earlyTotal%5;
				    			  curNum = row==getEarlyRow()-1 ? i : 5;
				    		  }
							//TODO
							final Timer timer = new Timer();					
							timer.schedule(new TimerTask(){
							      public void run(){
							    	  while(refreash.isAlive() == false && count == 0){							    		  
							    		  if(todayTotal == 0 && row ==0){
							    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row).
													findViewById(R.id.gvfirst);
							    			View nextView = grid.getChildAt(0); 
							    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
							    			
							    		  }else if(earlyTotal ==0 && row == getTodayRow()){
							    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row-1).
							    					findViewById(R.id.gvfirst);					    			  
							    			View nextView = grid.getChildAt(0); 
							    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
							    			//count ++;
							    		  }else if(position != curNum-1){
							    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row).
													findViewById(R.id.gvfirst);
							    			View nextView = grid.getChildAt(position); 
							    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
								    	  }else if(position == 0 && curNum==1){
							    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row-1).
													findViewById(R.id.gvfirst);
							    			View nextView = grid.getChildAt(4); 
							    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
							    			//count ++;
								    	  }else if(position == curNum-1){
							    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row).
													findViewById(R.id.gvfirst);
							    			View nextView = grid.getChildAt(position - 1); 
							    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
							    			//count ++;
								    	  }
							    		  count ++;
										}
							    	  if(count!=0){
							    		  timer.cancel(); 
							    	  }							          
							       }
							   }, 400,500);
						}else{
							Toast.makeText(myActivity, "删除失败",
									Toast.LENGTH_SHORT).show();
						}
					}
				});
				
			*/}

			
	public boolean onKeyDown(int keyCode, KeyEvent event) {	
		View curView = getActivity().getCurrentFocus();
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:			
			showPopupWindow(curView);				
			return true;
		case KeyEvent.KEYCODE_BACK:	
			if(isUninstallState){
				isUninstallState = false;
				FrameLayout user_uninstall_fl = (FrameLayout) curView.findViewById(R.id.user_uninstall_fl); 
				user_uninstall_fl.setVisibility(View.GONE);
				if(adapter1!=null && adapter2!=null){
					//adapter1.notifyDataSetChanged();	
					//adapter2.notifyDataSetChanged();
				}else if(adapter1==null && adapter2!=null){
					//adapter2.notifyDataSetChanged();
				}else if(adapter1!=null && adapter2==null){
					//adapter1.notifyDataSetChanged();
				}				
				return true;
			}	
			getActivity().finish();
			break;
		}		
		return false;
		
	}

		private void showPopupWindow(final View curView) {
			Context mContext = getActivity().getApplicationContext()  ;
			 View contentView = LayoutInflater.from(mContext).inflate(
		                R.layout.user_pop_window, null);		
			final PopupWindow popupWindow = new PopupWindow(contentView,
	                LayoutParams.MATCH_PARENT,LayoutParams.WRAP_CONTENT, true);
			popupWindow.setFocusable(true);					
	      // 如果不设置PopupWindow的背景，无论是点击外部区域还是Back键都无法dismiss弹框
	      popupWindow.setBackgroundDrawable(getResources().getDrawable(
	              R.drawable.user_buttom));
	     final View title = findViewById(R.id.user_history_title); 
	     title.setFocusable(true);
		 title.requestFocus();
		 popupWindow.setOnDismissListener(new OnDismissListener() {				
				@Override
				public void onDismiss() {
					curView.requestFocus();	
					title.setFocusable(false);
				}
			});
	      // 设置好参数之后再show
	       popupWindow.showAtLocation(view,Gravity.BOTTOM,0,0);	        
	       delAll = (LinearLayout) contentView.findViewById(R.id.del_all);
		   delAll.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					popupWindow.dismiss();
					deleteAll(curView);					
				}				
			});
			delOne = (LinearLayout) contentView.findViewById(R.id.del_one);
			delOne.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View mov) {
					popupWindow.dismiss();
					isUninstallState = true;
					FrameLayout user_uninstall_fl = (FrameLayout) curView.findViewById(R.id.user_uninstall_fl); 
					if(user_uninstall_fl==null){
						return;
					}else{
						user_uninstall_fl.setVisibility(View.VISIBLE);
						ImageView user_delete2 = (ImageView) curView.findViewById(R.id.user_delete);
						user_delete2.setBackgroundResource(R.drawable.user_del);
					}
					popupWindow.dismiss();
					if(adapter1!=null){
						isUninstallState = true;
						//adapter1.notifyDataSetChanged();
					}
					if(adapter2!=null){							
						isUninstallState = true;									
						//adapter2.notifyDataSetChanged();
					}									

				}			
			});
	    }
		class TodayAdapter extends WeightGridAdapter{
			int row;
			int xSize;			
			
			public void setxSize(int xSize) {
				this.xSize = xSize;
			}
			
			public TodayAdapter(int row, int xSize){
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
					convertView = LayoutInflater.from(getActivity().getApplication()).inflate(R.layout.user_history_element, parent, false);
				}				
									
				final ImageView user_delete = (ImageView) convertView.findViewById(R.id.user_delete);				
				final TextView historyName = (TextView) convertView.findViewById(R.id.history_name);
				
				final FrameLayout user_uninstall_fl = (FrameLayout) convertView.findViewById(R.id.user_uninstall_fl);								
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(isUninstallState){
							//TODO 删除影片
							deleteMov("今天",row,position);
						}else{/*	
							Wiki item = todayList.get(row*ROW_SIZE+position);
							Context ctx = getActivity().getApplicationContext();
							if(item.series_total > 1|| item.type == 4){
								HomedDataManager.startPlayer(ctx,item.id+"",item.series_id+"",item.name,item.type+"",0);
							}else{
								HomedDataManager.startPlayer(ctx,item.id+"","",item.name,item.type+"",0);
							}
						*/}
					}
				});
				

				convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							user_history_curpage.setText(row+1+"/");
							if(isUninstallState){
								user_uninstall_fl.setVisibility(View.VISIBLE);
								user_delete.setBackgroundResource(R.drawable.user_del);													
							}else{
								user_uninstall_fl.setVisibility(View.GONE);
							}
							historyName.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);
						} else {
							user_uninstall_fl.setVisibility(View.GONE);
							historyName.setTextSize(TypedValue.COMPLEX_UNIT_PX,32);
						}
					}
				});
				convertView.setTag(row + "");
				
				if (row * getPageDataCount() + position >= todayList.size()) {
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
		class EarlyAdapter extends WeightGridAdapter{
			int row;
			int xSize;
			
			public void setxSize(int xSize) {
				this.xSize = xSize;
			}
			
			public EarlyAdapter(int row, int xSize){
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
					convertView = LayoutInflater.from(getActivity().getApplication()).inflate(R.layout.user_history_element, parent, false);
				}				
				convertView = mInflater.inflate(R.layout.user_history_element, null);					
				final ImageView user_delete = (ImageView) convertView.findViewById(R.id.user_delete);
				final TextView historyName = (TextView) convertView.findViewById(R.id.history_name);
				final FrameLayout user_uninstall_fl = (FrameLayout) convertView.findViewById(R.id.user_uninstall_fl);
				convertView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if(isUninstallState){
							//TODO 删除影片
							deleteMov("更早",row,position);
						}else{/*	
							//TODO播放
							Wiki item = earlyList.get((row-getTodayRow())*ROW_SIZE+position);
							Context ctx = getActivity().getApplicationContext();
							if(item.series_total > 1|| item.type == 4){
								HomedDataManager.startPlayer(ctx,item.id+"",item.series_id+"",item.name,item.type+"",0);
							}else{
								HomedDataManager.startPlayer(ctx,item.id+"","",item.name,item.type+"",0);
							}
						*/}
					}
				});
				convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
					@Override
					public void onFocusChange(View v, boolean hasFocus) {
						if (hasFocus) {
							user_history_curpage.setText(row+1+"/");
							if(isUninstallState){
								user_uninstall_fl.setVisibility(View.VISIBLE);
								user_delete.setBackgroundResource(R.drawable.user_del);													
							}else{
								user_uninstall_fl.setVisibility(View.GONE);
							}
							historyName.setTextSize(TypedValue.COMPLEX_UNIT_PX,36);
						} else {
							user_uninstall_fl.setVisibility(View.GONE);
							historyName.setTextSize(TypedValue.COMPLEX_UNIT_PX,32);
						}
					}
				});
				
				convertView.setTag(row + "");
				
				if ((row-getTodayRow()) * getPageDataCount() + position >= earlyList.size()) {
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
		 * 判断某行的数据是否完整
		 * */
		private boolean checkTodayRowData(RowRequestParam param) {
			boolean result = true;
			for (int i = param.data_start; i < param.data_start + param.count; i++) {
				if ((param.data[i - param.data_start] = todayList.get(i)) == null) {
					result = false;
				}
			}
			return result;
		}
		private boolean checkEarlyRowData(RowRequestParam param) {
			boolean result = true;
			for (int i = param.data_start; i < param.data_start + param.count; i++) {
				if ((param.data[i - param.data_start] = earlyList.get(i-getTodayRow()*ROW_SIZE)) == null) {
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
		 * 行请求参数
		 */
		public class RowRequestParam {
			public String columeID;
			public int count;
			public int data_start;
			public int view_start;
			public int row;
			public Wiki[] data;
			@Override
			public String toString() {
				return "RowRequestParam [columeID=" + columeID + ", count=" + count + ", data_start=" + data_start
						+ ", view_start=" + view_start + "]";
			}
		}
		/**
		 * 刷新影片信息
		 * */
		private void refeshWeightLayoutChild(RowRequestParam param, WeightGridLayout layout){
			int start = param.view_start;
			int end = Math.min(layout.getChildCount(), start + param.count);
			for (int i = param.view_start; i < end; i++){
				View convertView = layout.getChildAt(i);
				final Wiki item = param.data[(i - param.view_start)% param.data.length];
				final ImageView image = (ImageView) convertView.findViewById(R.id.history_img);
				final TextView name = (TextView) convertView.findViewById(R.id.history_name);
				final ProgressBar bar = (ProgressBar) convertView.findViewById(R.id.bar);
				if (item.getCover() != null) {
					BaseImageFetchTask task = mImageFetcher.getBaseTask(item.getCover());
					mImageFetcher.setImageSize(241, 300);
					mImageFetcher.setLoadingImage(R.drawable.default_poster);
					mImageFetcher.loadImage(task, image);					
				}else{
					image.setImageResource(R.drawable.default_poster);
				}
//				int progress = (int)((item.offtime *10000 /item.duration));
//				bar.setProgress(progress);
				name.setText(item.getTitle());
			}
		}
		/**
		 * 获得一页显示的总影片数
		 * */
		private int getPageDataCount() {
			return ROW_SIZE * PAGE_ROW;
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
		public void requestRowData(int row) {
			List<RowRequestParam> params = getRowRequestParam(row);
			final int count = params.size();
			if(row < getTodayRow()){
				for (int i = 0; i < count; i++){
					final RowRequestParam param = params.get(i);
					if (checkTodayRowData(param)) {
						noticyRowData(param);
					}
				}
			}else{
				for (int i = 0; i < count; i++){
					final RowRequestParam param = params.get(i);
					if (checkEarlyRowData(param)) {
						noticyRowData(param);
					}
				}
			}			
		}
		/**
		 * 计算ViewPager一行的数据请求参数
		 * */
		private List<RowRequestParam> getRowRequestParam(int row) {
			List<RowRequestParam> params = new ArrayList<RowRequestParam>();
			int start,end,val;
			if(row < getTodayRow()){
				start = row * getPageDataCount();
				end = start + getPageDataCount();
				val = todayTotal;
			}else{
				start = row * getPageDataCount();
				end = start + getPageDataCount();
				val =getTodayRow()*ROW_SIZE + earlyTotal;
			}
			
			RowRequestParam param = new RowRequestParam();
			param.data_start = start;
			param.view_start = 0;
			param.count = (end > val) ? val % getPageDataCount()
					: getPageDataCount();
			param.row = row;
			param.data = new Wiki[param.count];
			params.add(param);
			return params;
		}
		public int getTodayRow(){
			int i = todayList.size();			
			return i%ROW_SIZE == 0 ? i/ROW_SIZE : i/ROW_SIZE + 1;
		}
		public int getEarlyRow(){
			int j = earlyList.size();
			return j%ROW_SIZE == 0 ? j/ROW_SIZE : j/ROW_SIZE + 1;
		}
		
	}
