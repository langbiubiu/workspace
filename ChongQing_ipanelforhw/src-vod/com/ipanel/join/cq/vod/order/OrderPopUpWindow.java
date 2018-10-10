package com.ipanel.join.cq.vod.order;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.support.v4.view.VerticalViewPager2;
import android.support.v4.view.VerticalViewPager2.OnPageChangeListener;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.ipanel.chongqing_ipanelforhw.R;

/**
 * 订购页面
 */
public class OrderPopUpWindow{
	private PopupWindow popWindow;//弹出框
	private Activity mActivity;
	private View mRootView;
	private VerticalViewPager2 mViewPager;
	private ImageView arrowUp,arrowDown;//上下箭头
	private TextView loadingTips;//提示信息
	private String vodId;//影片ID
	private final static int GET_INFO_SUCCEED = 555;
	private final static int GET_INFO_FAILED = 556;
	
	public OrderPopUpWindow(Activity activity){
		mActivity = activity;
		mRootView = mActivity.getLayoutInflater().inflate(R.layout.vod_order_layout, null);
		/*
		 * 设置PopupWindow
		 */
		popWindow = new PopupWindow(mRootView);
		popWindow.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x90000000);
        popWindow.setBackgroundDrawable(dw);//设置背景，否则返回键popupWindow不会消失
        
		initViews();
		initControl();
		
		new Handler().postDelayed(mRunnable, 1000);
	}
	/*
	 * 初始化视图
	 */
	private void initViews() {
		mViewPager = (VerticalViewPager2)mRootView.findViewById(R.id.order_viewpager);
		arrowUp = (ImageView)mRootView.findViewById(R.id.arrowup);
		arrowDown = (ImageView)mRootView.findViewById(R.id.arrowdown);
		loadingTips = (TextView)mRootView.findViewById(R.id.order_loading_tips);
	}
	/*
	 * 初始化监听
	 */
	private void initControl() {
		mViewPager.setPageScrollDuration(300);
		mViewPager.setOffscreenPageLimit(3);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			
			@Override
			public void onPageSelected(int position) {
				refreshArrows();
			}
			
			@Override
			public void onPageScrolled(int position, float positionOffset,
					int positionOffsetPixels) {
				
			}
			
			@Override
			public void onPageScrollStateChanged(int state) {
				if(state == VerticalViewPager2.SCROLL_STATE_IDLE){
					refreshArrows();
				}
			}
		});
	}
	/*
	 * 获取订单信息
	 */
	protected void getOrderInfo(){
		showMessage(GET_INFO_SUCCEED);
		mViewPager.setAdapter(new OrderPagerAdapter(mActivity, VodOrderManager.getOrderList()));
		mViewPager.setVisibility(View.VISIBLE);
		refreshArrows();
	}
	
	private Runnable mRunnable = new Runnable() {
		
		@Override
		public void run() {
			getOrderInfo();
		}
	};
	
	public PopupWindow getPopupWindow() {
        return popWindow;
    }
	/*
	 * 显示信息
	 */
	private void showMessage(int index){
		switch (index) {
		case GET_INFO_FAILED:
			loadingTips.setVisibility(View.VISIBLE);
			loadingTips.setText(mActivity.getResources().getString(R.string.order_get_failed));
			break;
		case GET_INFO_SUCCEED:
			loadingTips.setVisibility(View.INVISIBLE);
			break;
		default:
			break;
		}
	}
	/*
	 * 刷新上下箭头
	 */
	private void refreshArrows(){
		arrowDown.setVisibility(mViewPager.hasDownHideItem()?View.VISIBLE:View.INVISIBLE);
		arrowUp.setVisibility(mViewPager.hasUpHideItem()?View.VISIBLE:View.INVISIBLE);
	}
	
}
