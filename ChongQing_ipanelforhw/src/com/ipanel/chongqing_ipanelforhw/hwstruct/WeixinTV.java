package com.ipanel.chongqing_ipanelforhw.hwstruct;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.User;
import com.ipanel.join.chongqing.myapp.MenuPopupWindow;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

public class WeixinTV extends Activity {

	private String url = "http://117.59.6.101/json";
	private final static String TAG = "WeixinTV";
	
	ViewPager userViewPager,pushImageViewPager;
	
	

	GetHwRequest getShowCodeRequest,getUserListRequest;// 传参类
	GetHwResponse getShowCodeResponse,getUserListResponse;// 取值类

	private static final int SHOWCODE = 555;
	private static final int USERLIST = 5556;
	
	private List<User> users;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case SHOWCODE:
				SharedImageFetcher.getSharedFetcher(WeixinTV.this)
				.loadImage(getShowCodeResponse.getQrcode().getUrl(), showcode);

				break;
			case USERLIST:
				userViewPager.setAdapter(userPagerAdapter);
//				users=getUserListResponse.getUsers();
//				if (users!=null) {
//					userViewPager.setAdapter(userPagerAdapter);
//				}
				setHideAnimation(weixintip,1000);
				break;
			default:
				break;
			}
		}

	};
	
	private AlphaAnimation mHideAnimation= null;
	private void setHideAnimation( View view, int duration ){

	    if( null == view || duration < 0 ){

	        return;

	    }

	    if( null != mHideAnimation ){

	        mHideAnimation.cancel( );

	    }

	    mHideAnimation = new AlphaAnimation(1.0f, 0.0f);

	    mHideAnimation.setDuration( duration );

	    mHideAnimation.setFillAfter( true );

	    view.startAnimation( mHideAnimation );

	}
	
	TextView responseText;
	ImageView showcode,weixintip;

	View mRootView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wechat_tv_activity);

		mRootView = LayoutInflater.from(this).inflate(R.layout.btv_tvadd_layout, null, false);
		userViewPager=(ViewPager) findViewById(R.id.viewpager);
		responseText = (TextView) findViewById(R.id.weixincode);
		showcode = (ImageView) findViewById(R.id.showcode);
		weixintip = (ImageView) findViewById(R.id.weixintip);
		
		// 设置发送的Json数据模块
		getShowCodeRequest = new GetHwRequest();
		getShowCodeRequest.setAction("GetWechatDynamicQrcode");

		getShowCodeRequest.getDeveloper().setApikey("SHMFX2NF");
		getShowCodeRequest.getDeveloper().setSecretkey("28a95fbbb5eb415a9736d98929a802c3");
		getShowCodeRequest.getDevice().setDnum("123");
		getShowCodeRequest.getUser().setUserid("9950000000946808");
		getShowCodeRequest.setParam(null);
		getHwRequest(getShowCodeRequest,SHOWCODE);
		
//		getShowCodeRequest.setAction("GetWechatBoundUsers");
//		getHwRequest(getShowCodeRequest,USERLIST);
		
	}

	Gson gson = new Gson();

	/**
	 * 欢网对接方法,对所有接口通用，传入的数据结构跟数据结构相同，只需要根据接口文档传入和获取对应的数值即可
	 **/
	private void getHwRequest(Object requestEntity,final int msg) {

		ServiceHelper helper = ServiceHelper.getHelper();
		helper.setSerializerType(SerializerType.JSON);
		helper.setRootUrl(url);
		Log.d(TAG, gson.toJson(requestEntity));

		helper.callServiceAsync(getApplicationContext(), requestEntity, GetHwResponse.class,
				new ResponseHandlerT<GetHwResponse>() {

					@Override
					public void onResponse(boolean success, GetHwResponse result) {
						// TODO Auto-generated method stub
						if (!success) {
							Log.i(TAG, "request detail data failed");

							return;
						}
						if (result == null) {
							Log.i(TAG, "failed to parse JSON data");
							Toast.makeText(WeixinTV.this, "failed to parse JSON data", 5555).show();
							return;
						}

						// 这里获取到返回的Json模块结果
						getShowCodeResponse = result;
					    mHandler.sendEmptyMessage(msg);
					}
				});
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
        public int getItemPosition(Object object) {  

            return super.getItemPosition(object);  
        }  

 

        @Override  
        public Object instantiateItem(ViewGroup container, int position) {  
        
            View view = LayoutInflater.from(getApplication()).inflate(
					R.layout.wechat_user_item, container, false);
            ImageView userImage=(ImageView)view.findViewById(R.id.userimage);
            TextView userName=(TextView)view.findViewById(R.id.username);
            
//            SharedImageFetcher.getSharedFetcher(WeixinTV.this)
//			.loadImage(users.get(position%users.size()).getBackgroudurl(), view);
//            SharedImageFetcher.getSharedFetcher(WeixinTV.this)
//			.loadImage(users.get(position%users.size()).getHeadimgurl(), userImage);
//            userName.setText(users.get(position%users.size()).getNickname());
            
            container.addView(view);
            return view;  
        }  

    };  
    
	PagerAdapter userImageAdapter = new PagerAdapter() {  
		  
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
        public int getItemPosition(Object object) {  

            return super.getItemPosition(object);  
        }  

 

        @Override  
        public Object instantiateItem(ViewGroup container, int position) {  
        
            View view = LayoutInflater.from(getApplication()).inflate(
					R.layout.wechat_user_item, container, false);
            ImageView userImage=(ImageView)view.findViewById(R.id.userimage);
            TextView userName=(TextView)view.findViewById(R.id.username);
            AbsoluteLayout userbar=(AbsoluteLayout)view.findViewById(R.id.userbar);
            userbar.setVisibility(View.GONE);
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
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			if (isMenuWindowShowing()) {
				hideMenuWindow();
			} else {
				showMenuWindow();
			}
			return true;

		}
		return super.onKeyDown(keyCode, event);
	}

	MenuPopupWindow menuWindow;

	private void showMenuWindow() {
		if (menuWindow == null)
			menuWindow = new MenuPopupWindow(this);
		PopupWindow popupWindow = menuWindow.getPop();
		popupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
		popupWindow.update(0, 0, 1920, 242);
	}

	private boolean isMenuWindowShowing() {
		if (menuWindow != null)
			return menuWindow.isMenuWindowShowing();
		return false;
	}

	private void hideMenuWindow() {
		if (menuWindow != null)
			menuWindow.hideMenuWindow();
	}

}
