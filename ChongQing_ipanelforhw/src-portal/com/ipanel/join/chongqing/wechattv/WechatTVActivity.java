package com.ipanel.join.chongqing.wechattv;



import java.util.List;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.User;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

public class WechatTVActivity extends Activity {

	private String url = "http://117.59.6.101/json";
	private final static String TAG = "WeixinTV";
	
	ViewPager userViewPager,pushImageViewPager;
	boolean isUninstallState = false; 
	

	GetHwRequest getShowCodeRequest,getUserListRequest;// 传参类
	GetHwResponse getShowCodeResponse,getUserListResponse;// 取值类

	private static final int INITVIEW = 5555;
	private static final int SHOWCODE = 555;
	private static final int USERLIST = 5556;
	
	private String userId="9950000000946808";
	private List<User> users;
	Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			
			case INITVIEW:
				mRootView = LayoutInflater.from(WechatTVActivity.this).inflate(R.layout.btv_tvadd_layout, null, false);
				userViewPager=(ViewPager) findViewById(R.id.viewpager);
				responseText = (TextView) findViewById(R.id.weixincode);
				showcode = (ImageView) findViewById(R.id.showcode);
				weixintip = (ImageView) findViewById(R.id.weixintip);
				wechat_title = (ImageView)findViewById(R.id.wechat_title);
				
				// 设置发送的Json数据模块
				getShowCodeRequest = new GetHwRequest();
				getShowCodeRequest.setAction("GetWechatDynamicQrcode");

				getShowCodeRequest.getDeveloper().setApikey("SHMFX2NF");
				getShowCodeRequest.getDeveloper().setSecretkey("28a95fbbb5eb415a9736d98929a802c3");
				getShowCodeRequest.getDevice().setDnum("123");
				getShowCodeRequest.getUser().setUserid(userId);
				getShowCodeRequest.setParam(null);
				getHwRequest(getShowCodeRequest,SHOWCODE);
				
				getUserListRequest= new GetHwRequest();
				getUserListRequest.setAction("GetWechatBoundUsers");
				getUserListRequest.getDeveloper().setApikey("SHMFX2NF");
				getUserListRequest.getDeveloper().setSecretkey("28a95fbbb5eb415a9736d98929a802c3");
				getUserListRequest.getDevice().setDnum("123");
				getUserListRequest.getUser().setUserid(userId);
				getUserListRequest.setParam(null);
				getHwRequest(getUserListRequest,USERLIST);
				
				
				break;
//			case SHOWCODE:
//				SharedImageFetcher.getSharedFetcher(WechatTVActivity.this)
//				.loadImage(getShowCodeResponse.getQrcode().getUrl(), showcode);
//
//				break;
			case USERLIST:
				
				userViewPager.setAdapter(userPagerAdapter);
				userViewPager.setOffscreenPageLimit(3); 
				userViewPager.setPageMargin(50); 
//				users=getUserListResponse.getUsers();
//				if (users!=null) {
//					userViewPager.setAdapter(userPagerAdapter);
//				}
				setHideAnimation(weixintip,3000);
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
	    userViewPager.requestFocus();
	}
	
	TextView responseText;
	ImageView showcode,weixintip,wechat_title;
	View v;

	View mRootView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.wechat_tv_activity);
		mHandler.sendEmptyMessage(INITVIEW);
		
		this.registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Logger.d("wechat已经接收到更新广播CookieString = " + intent.getExtras().getString("CookieString").toString());
//				GlobalFilmData.getInstance().setCookieString(intent.getExtras().getString("CookieString"));
//				// 还需要添加EPG的即时更新
//				String epg = intent.getExtras().getString("EPG");
//				String serviceGroupId = "" + intent.getExtras().getLong("ServiceGroupId");
//				String smartcard = intent.getExtras().getString("smartcard");
//				GlobalFilmData.getInstance().setEpgUrl(epg);
//				GlobalFilmData.getInstance().setGroupServiceId(serviceGroupId);
//				GlobalFilmData.getInstance().setCardID(smartcard);
//				GlobalFilmData.getInstance().setIcState(intent.getExtras().getString("icState"));
				userId=intent.getExtras().getString("smartcard");
				mHandler.sendEmptyMessage(INITVIEW);
			}
		}, new IntentFilter("com.ipanel.join.cq.vodauth.EPG_URL"));

//		Animation animation1 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.wechat_tvhide);
//		weixintip.startAnimation(animation1);
//		Animation animation2 = AnimationUtils.loadAnimation(getBaseContext(), R.anim.wechat_tvshow);
//		userViewPager.startAnimation(animation2);
		
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
							Toast.makeText(WechatTVActivity.this, "failed to parse JSON data", 5555).show();
							return;
						}

						// 这里获取到返回的Json模块结果
						getShowCodeResponse = result;
					    mHandler.sendEmptyMessage(msg);
					}
				});
		mHandler.sendEmptyMessage(msg);
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
        public Object instantiateItem(ViewGroup container, int position) {  
        
            View view = LayoutInflater.from(getApplication()).inflate(
					R.layout.wechat_user_item, container, false);
            ImageView wechatuser_item_pic = (ImageView)view.findViewById(R.id.wechatuser_item_pic);
            final FrameLayout wechatuser_item_del = (FrameLayout)view.findViewById(R.id.wechatuser_item_del);
            TextView userName=(TextView)view.findViewById(R.id.username);
            view.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View arg0) {
					if(!isUninstallState){
						Intent intent = new Intent(WechatTVActivity.this,UserPagerActivity.class);
						/*传递数据，点击的是哪个用户*/
						intent.putExtra("user", "str");  
						startActivity(intent);
					}else if(isUninstallState){
						/*删除*/
					}
					
				}
            	
            });
           view.setOnFocusChangeListener(new OnFocusChangeListener(){

			@Override
			public void onFocusChange(View arg0, boolean hasFocus) {
				if(hasFocus){
					if(isUninstallState){
		            	wechatuser_item_del.setVisibility(View.VISIBLE);
					}else{
		            	wechatuser_item_del.setVisibility(View.GONE);
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
				FrameLayout del = (FrameLayout)v.findViewById(R.id.wechatuser_item_del);
				del.setVisibility(View.VISIBLE);
				hideMenuWindow();
				v.requestFocus();
				return true;
			} else {
				showMenuWindow(curView);
				return true;
			}
			
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if(isMenuWindowShowing()){
				isUninstallState = true;
				FrameLayout del = (FrameLayout)v.findViewById(R.id.wechatuser_item_del);
				del.setVisibility(View.VISIBLE);
				hideMenuWindow();
				v.requestFocus();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_ESCAPE:
			if(isUninstallState){
				isUninstallState = false;
				FrameLayout del = (FrameLayout)curView.findViewById(R.id.wechatuser_item_del);
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
