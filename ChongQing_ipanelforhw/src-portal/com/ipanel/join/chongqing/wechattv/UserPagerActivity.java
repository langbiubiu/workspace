package com.ipanel.join.chongqing.wechattv;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.ipanel.chongqing_ipanelforhw.R;

public class UserPagerActivity extends Activity{
	ViewPager user_viewpager;
	boolean isUninstallState = false;
	View mRootView;
	View v;
	ImageView wechat_title;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		mRootView = LayoutInflater.from(UserPagerActivity.this).inflate(R.layout.btv_tvadd_layout, null, false);
		setContentView(R.layout.userpager_activity);
		user_viewpager = (ViewPager) findViewById(R.id.user_viewpager);
		wechat_title = (ImageView) findViewById(R.id.wechat1_title);
		initImage();
		user_viewpager.setAdapter(userPagerAdapter);
		user_viewpager.setOffscreenPageLimit(3); 
		user_viewpager.setPageMargin(50); 
	}
	
	/*根据点击的用户初始化图片*/
	private void initImage() {
		String user = getIntent().getExtras().getString("user");		
		
	}
	
	PagerAdapter userPagerAdapter = new PagerAdapter() {  
		  
        @Override  
        public boolean isViewFromObject(View arg0, Object arg1) {  
            return arg0 == arg1;  
        }  

        @Override  
        public int getCount() {  
//            return users.size();  
            return 5;  
        }  
        @Override
        public void destroyItem(View container, int position, Object object){
        	((ViewPager) container).removeView((View) object);
        }
        @Override  
        public int getItemPosition(Object object) {  
            return super.getItemPosition(object);  
        }  
 
        @Override  
        public Object instantiateItem(ViewGroup container, final int position) {          
            View view = LayoutInflater.from(getApplication()).inflate(
					R.layout.wechat_userpager_item, container, false);
            ImageView userpager_item_pic = (ImageView) view.findViewById(R.id.userpager_item_pic);
            final FrameLayout userpager_item_del = (FrameLayout) view.findViewById(R.id.userpager_item_del);
           
            view.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View arg0) {
					if(isUninstallState){
						/*删除*/						
					}else{
						/*进入图片播放*/
						Intent intent = new Intent(UserPagerActivity.this,PicScanActivity.class);
						intent.putExtra("num", position);
						startActivity(intent);
					}
				}
            	
            });
            view.setOnFocusChangeListener(new OnFocusChangeListener(){

    			@Override
    			public void onFocusChange(View arg0, boolean hasFocus) {
    				if(hasFocus){
    					if(isUninstallState){
    						userpager_item_del.setVisibility(View.VISIBLE);
    					}else{
    						userpager_item_del.setVisibility(View.GONE);
    					}
    				}
    				
    			}
            	   
               });
            
//            SharedImageFetcher.getSharedFetcher(WeixinTV.this)
//			.loadImage(users.get(position%users.size()).getBackgroudurl(), view);
//            SharedImageFetcher.getSharedFetcher(WeixinTV.this)
//			.loadImage(users.get(position%users.size()).getHeadimgurl(), userImage);
//            userName.setText(users.get(position%users.size()).getNickname());
            
            container.addView(view);
            return view;  
        }  

    }; 
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		View curView = this.getCurrentFocus(); 
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			if (isMenuWindowShowing()) {
				isUninstallState = true;
				FrameLayout del = (FrameLayout)v.findViewById(R.id.userpager_item_del);
				del.setVisibility(View.VISIBLE);
				hideMenuWindow();
				return true;
			} else {
				showMenuWindow(curView);
				return true;
			}
			
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if(isMenuWindowShowing()){
				isUninstallState = true;
				FrameLayout del = (FrameLayout)v.findViewById(R.id.userpager_item_del);
				del.setVisibility(View.VISIBLE);
				hideMenuWindow();				
				return true;
			}
			break;
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_ESCAPE:
			if(isUninstallState){
				isUninstallState = false;
				FrameLayout del = (FrameLayout)curView.findViewById(R.id.userpager_item_del);
				if(del!=null)
				del.setVisibility(View.GONE);
				return true;
			}
			break;

		}
		return super.onKeyDown(keyCode, event);
	}
	WechatPopupWindow menuWindow;
	
	private void showMenuWindow(View view) {
		if (menuWindow == null)
			menuWindow = new WechatPopupWindow(this);
		PopupWindow popupWindow = menuWindow.getPop();
		popupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
		popupWindow.update(0, 0, 1920, 242);
		wechat_title.setFocusable(true);
		wechat_title.requestFocus();
		v = view;
	}
	private boolean isMenuWindowShowing() {
		if (menuWindow != null)
			return menuWindow.isMenuWindowShowing();
		return false;
	}

	private void hideMenuWindow() {
		if (menuWindow != null)
			menuWindow.hideMenuWindow();
		wechat_title.setFocusable(false);
	}

}
