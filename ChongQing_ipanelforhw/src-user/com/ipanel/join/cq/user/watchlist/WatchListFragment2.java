package com.ipanel.join.cq.user.watchlist;

import ipanel.join.widget.ViewPager2;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
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

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Channel;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.user.BaseFragment;
import com.ipanel.join.cq.user.UserActivity;
import com.ipanel.join.cq.user.history.RespDelete;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.player.VodDataManager;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;


public class WatchListFragment2 extends BaseFragment{
	private VerticalViewPager2 viewPager;
	/**
	 * ��Ŀ���ݼ��������Ϣ
	 * */
	public static final int MSG_COLUME_DATA_LOADED = 0;
	/**
	 * һҳӰƬ���ݼ��������Ϣ
	 * */
	public static final int MSG_ROW_DATA_LOAED = 1;
	/**
	 * ����ʱ��Ϣ
	 **/
	public static final int MSG_REQUEST_TIME_UP = 3;
	/**
	 * ɾ��ӰƬ����ӰƬ��ý���
	 */
	public static final int MSG_FOCUS = 4;
	/**
	 * �Ҳ��������ý���
	 */
	public static final int MSG_REFOCUS = 5;
	/**
	 * �ı佹��
	 */
	public static final int MSG_CHANGE_FOCUS = 6;
	/**
	 * һ����ʾ����ӰƬ
	 * */
	public static final int ROW_SIZE = 5;
	/**
	 * һ����ʾ����
	 * */
	public static final int PAGE_ROW = 1;
	/**
	 * ViewPagerһ����ʾ����
	 * */
	public static final int MAX_PAGE_ROW = 2;
	
	
	private int totalMovieNumber = -1;
	private ImageFetcher mImageFetcher;
	private List<Wiki> mData;
	private Stack<SoftReference<View>> views = new Stack<SoftReference<View>>();
	View view;
	private UserActivity myActivity ;
	//private MyPopupWindow myWindow;
	private boolean isUninstallState;
	private LinearLayout delOne;
	private LinearLayout delAll;
	private Button del_cancel;
	private Button del_sure;
	private VODPagerAdapter adapter;
	int count = 0;
	int num = 0;
	View title;
	LinearLayout watch_list_linear;
	TextView user_watchlist_curpage,user_watchlist_totalpage;
	TextView watchList;
	protected ServiceHelper serviceHelper;
			
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			//��Ŀ���ݼ������
			case MSG_COLUME_DATA_LOADED:
				Logger.d("-->msg MSG_COLUME_DATA_LOADED");
				viewPager.setAdapter(adapter = new VODPagerAdapter());	
				if(totalMovieNumber > 0 ){
					watchList.setVisibility(View.VISIBLE);
				}
				user_watchlist_totalpage.setText(getTotalPageSize()+"");
				break;
			//һҳӰƬ���ݼ������	
			case MSG_ROW_DATA_LOAED:
				if (msg.obj instanceof RowRequestParam) {
					RowRequestParam param = (RowRequestParam) msg.obj;
					if (viewPager.findViewWithTag(param.row) == null) {
						return;
					}
					Logger.d("-->msg һҳӰƬ���ݼ������	");
					WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(param.row).
							findViewById(R.id.watchgrid);
					if (grid != null) {
						refeshWeightLayoutChild(param, grid);
						Logger.d("-->handlerˢ��ӰƬ��Ϣ");
					}
				}
				break;
			case MSG_FOCUS:
				View focusView = (View) msg.obj;
				focusView.requestFocus();
				Logger.d("-->msg MSG_FOCUS");
				break;

			
			case MSG_REQUEST_TIME_UP:
				//finish();
				break;
			}
		}
	};
	
	private Runnable initThread = new Runnable() {

		@Override
		public void run() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					// ��ȡ����
					initData();
				}
			});
		}
	};
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.user_watch_list_fragment, container, false);		
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		myActivity = (UserActivity) getActivity();
		mImageFetcher = SharedImageFetcher.getNewFetcher(getActivity(), 3);
		view = getView();
		serviceHelper = ServiceHelper.getHelper();
		watch_list_linear = myActivity.watch_list_linear;
		title = view.findViewById(R.id.user_watchlist_title);
		viewPager = (VerticalViewPager2)view.findViewById(R.id.watch_gd);
		user_watchlist_curpage = (TextView) view.findViewById(R.id.user_watchlist_curpage);
		user_watchlist_curpage.setText("0/");
		user_watchlist_totalpage = (TextView) view.findViewById(R.id.user_watchlist_totalpage);
		watchList = (TextView) view.findViewById(R.id.watchList);
		initControl();
		isUninstallState = false;
		Thread mThread = new Thread(initThread);
		mThread.start();	
	}
//	@Override
//	public void onHiddenChanged(boolean hide){
//		if(hide){
//		}
//		else{
//			watch_list_linear.requestFocus();
//		}
//	}
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

//	public void hideFocus(){
//		title.setFocusable(true);
//		title.requestFocus();
//	}
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
			return POSITION_NONE;
			//return super.getItemPosition(object);
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
			Log.i("time",time+"");
			View itemView = getViewFromSoftReference();
			if (itemView == null) {
				itemView = View.inflate(getActivity().getBaseContext() , R.layout.user_watch_griditem, null);
			}
			WeightGridLayout mWeightLayout = (WeightGridLayout) itemView.findViewById(R.id.watchgrid);
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
		Log.i("totalMovieNumber",totalMovieNumber+"");
		return totalMovieNumber;
	}
	/**
	 * ���ViewPager��������
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
	 * ����ViewPagerһ�е������������
	 * */
	private List<RowRequestParam> getRowRequestParam(int row) {
		List<RowRequestParam> params = new ArrayList<RowRequestParam>();
		int start = row * getPageDataCount();
		int end = start + getPageDataCount();
		int val = totalMovieNumber;

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
	
	/**
	 * �ж�ĳ�е������Ƿ�����
	 * */
	private boolean checkRowData(RowRequestParam param) {
		boolean result = true;
		for (int i = param.data_start; i < param.data_start + param.count; i++) {
			if ((param.data[i - param.data_start] = mData.get(i)) == null) {
				result = false;
			}
		}
		return result;
	}
	
	/**
	 * ֪ͨˢ�½���
	 * */
	public void noticyRowData(RowRequestParam param) {
		LogHelper.i("nocity refresh row : " + param.row);
		mHandler.obtainMessage(MSG_ROW_DATA_LOAED, param).sendToTarget();
	}

	/**
	 * �����Ч��ͼƬ��������
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
				convertView = LayoutInflater.from(getActivity().getApplication()).inflate(R.layout.user_hot_element2, parent, false);
			}
			final View curView = convertView;
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final FrameLayout user_uninstall_fl = (FrameLayout) convertView.findViewById(R.id.user_uninstall_fl2);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			final ImageView del = (ImageView) convertView.findViewById(R.id.user_delete2);
			convertView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					if(isUninstallState){
						//TODO ɾ��ӰƬ						
						whichMov(row,position);						
					}else{						
						Wiki item = mData.get(row*5+position);
						Context ctx = getActivity().getApplicationContext();
//						HomedDataManager.openMovieDetail(ctx,item.id+"",item.getTitle(),null);
					}
				}
			});
			
			convertView.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						user_watchlist_curpage.setText(row+1+"/");
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
	 * ˢ��ӰƬ��Ϣ
	 * */
	private void refeshWeightLayoutChild(RowRequestParam param, WeightGridLayout layout){
		int start = param.view_start;
		int end = Math.min(layout.getChildCount(), start + param.count);
		for (int i = param.view_start; i < end; i++){
			View convertView = layout.getChildAt(i);
			final Wiki watchItem = param.data[(i - param.view_start)% param.data.length];
			final ImageView image = (ImageView) convertView.findViewById(R.id.film_img);
			final TextView name = (TextView) convertView.findViewById(R.id.film_name);
			if (watchItem.getCover() != null) {
				BaseImageFetchTask task = mImageFetcher.getBaseTask(watchItem.getCover());
				mImageFetcher.setImageSize(241, 300);
				mImageFetcher.setLoadingImage(R.drawable.default_poster);
				mImageFetcher.loadImage(task, image);				
			}else{
				image.setImageResource(R.drawable.default_poster);
			}
			name.setText(watchItem.getTitle());
		}
	}
	/**
	 * ���һҳ��ʾ����ӰƬ��
	 * */
	private int getPageDataCount() {
		return ROW_SIZE * PAGE_ROW;
	}
	/**
	 * ���������
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
	private void initData(){
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_GET_FAV_WIKIS_BY_USER);
		request.getParam().setPage(1);
		request.getParam().setPagesize(10);
		serviceHelper.setRootUrl(HWDataManager.ROOT_URL);
		serviceHelper.setSerializerType(SerializerType.JSON);
		//request.getParam().setShowlive(false);
		serviceHelper.callServiceAsync(getActivity(), request, GetHwResponse.class, new ResponseHandlerT<GetHwResponse>() {
			
			@Override
			public void onResponse(boolean success, GetHwResponse result) {
				if(success && result.getError().getCode() == 0){
					List<Wiki> list = result.getWikis();
					int total = list.size();
					mData = new ArrayList<Wiki>();
					for(int i=0;i<total;i++){
						if(list.get(i).getModel().equals("film"))
							mData.add(list.get(i));
					}
					totalMovieNumber = mData.size();					
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
				FrameLayout user_uninstall_fl = (FrameLayout) curView.findViewById(R.id.user_uninstall_fl2); 
				user_uninstall_fl.setVisibility(View.GONE);
				isUninstallState = false;
				return true;
			}
			getActivity().finish();
			break;
		}		
		return false;
	}
	private void showPopupWindow(View view2) {
		final View v = view2;
		Context mContext = getActivity().getApplicationContext()  ;
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
      // ���������PopupWindow�ı����������ǵ���ⲿ������Back�����޷�dismiss����
      popupWindow.setBackgroundDrawable(getResources().getDrawable(
              R.drawable.user_buttom));
      // ���úò���֮����show
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
				FrameLayout user_uninstall_fl = (FrameLayout) v.findViewById(R.id.user_uninstall_fl2); 
				if(user_uninstall_fl==null){
					return;
				}else{
				user_uninstall_fl.setVisibility(View.VISIBLE);
				ImageView user_delete2 = (ImageView) v.findViewById(R.id.user_delete2);
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
		user_del_tips.setText(R.string.del_tips_watchList);
		final View title = view.findViewById(R.id.user_watchlist_title);
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
	private void whichMov(int row,int position){
		Wiki item = mData.get(row*5+position);
		deleteMov(item.getId(),row,position);
	}
	protected void delSubscribeTag(String id) {
		GetHwRequest request = HWDataManager.getHwRequest();
		request.setAction(HWDataManager.ACTION_DEL_FAV_WIKI);
		request.getParam().setWikiId(id);
		VodDataManager.getInstance(getActivity()).getHwData(request);
	}
	protected  void deleteMov(final String id,final int row,final int position){
		if(id.equals("-1")){
			//ȫ��ɾ��
			for(int i=0;i<totalMovieNumber;i++){
				String str = mData.get(i).getId();
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
		int i = totalMovieNumber%5==0 ? 5 :totalMovieNumber%5;
		final int curNum = row==getTotalPageSize()-1 ? i : 5;
		timer.schedule(new TimerTask(){			
	      public void run(){		    	  
    		    if(id.equals("-1")){
					return;
				}
				if(totalMovieNumber==1){
					return;
				}
    		  if(position != 0 && position != curNum+1){	    			    
    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row).
						findViewById(R.id.watchgrid);
    			View nextView = grid.getChildAt(position); 
    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
	    		}else if(position == 0 && curNum==1){
	    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row-1).
							findViewById(R.id.watchgrid);
	    			View nextView = grid.getChildAt(4); 
	    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
	    		}else if(position == curNum-1 && position !=0){
	    			WeightGridLayout grid = (WeightGridLayout) viewPager.findViewWithTag(row).
							findViewById(R.id.watchgrid);
	    			View nextView = grid.getChildAt(position - 1); 
	    			mHandler.obtainMessage(MSG_FOCUS, nextView).sendToTarget();
	    		}							    	  
	       }
		}, 500);
	}
}
