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
 * ����ҳ��
 */
public class OrderPopUpWindow{
	private PopupWindow popWindow;//������
	private Activity mActivity;
	private View mRootView;
	private VerticalViewPager2 mViewPager;
	private ImageView arrowUp,arrowDown;//���¼�ͷ
	private TextView loadingTips;//��ʾ��Ϣ
	private String vodId;//ӰƬID
	private final static int GET_INFO_SUCCEED = 555;
	private final static int GET_INFO_FAILED = 556;
	
	public OrderPopUpWindow(Activity activity){
		mActivity = activity;
		mRootView = mActivity.getLayoutInflater().inflate(R.layout.vod_order_layout, null);
		/*
		 * ����PopupWindow
		 */
		popWindow = new PopupWindow(mRootView);
		popWindow.setFocusable(true);
        ColorDrawable dw = new ColorDrawable(0x90000000);
        popWindow.setBackgroundDrawable(dw);//���ñ��������򷵻ؼ�popupWindow������ʧ
        
		initViews();
		initControl();
		
		new Handler().postDelayed(mRunnable, 1000);
	}
	/*
	 * ��ʼ����ͼ
	 */
	private void initViews() {
		mViewPager = (VerticalViewPager2)mRootView.findViewById(R.id.order_viewpager);
		arrowUp = (ImageView)mRootView.findViewById(R.id.arrowup);
		arrowDown = (ImageView)mRootView.findViewById(R.id.arrowdown);
		loadingTips = (TextView)mRootView.findViewById(R.id.order_loading_tips);
	}
	/*
	 * ��ʼ������
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
	 * ��ȡ������Ϣ
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
	 * ��ʾ��Ϣ
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
	 * ˢ�����¼�ͷ
	 */
	private void refreshArrows(){
		arrowDown.setVisibility(mViewPager.hasDownHideItem()?View.VISIBLE:View.INVISIBLE);
		arrowUp.setVisibility(mViewPager.hasUpHideItem()?View.VISIBLE:View.INVISIBLE);
	}
	
}
