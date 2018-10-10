package com.ipanel.join.cq.homed.tvcloud;

import java.text.DecimalFormat;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.KeyEvent.Callback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.ipanel.android.widget.SimpleTab;
import cn.ipanel.android.widget.SimpleTab.OnTabChangeListener;
import cn.ipanel.android.widget.ViewFrameZoomIndicator;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.portal.PortalDataManager;
import com.ipanel.join.cq.back.RecommendWindow;
import com.ipanel.join.cq.homed.tvcloud.db.RecordDataBaseHelper;
import com.ipanel.join.cq.sihua.data.SpaceRequest;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceCapacity;

public class TVCloudActivity extends Activity {
	
	private final static String TAG = "TVCloudActivity";
	public final static int FRAGMENT_FIRST_GET_FOCUS = 10;
	public final static String SiHuaUrl = "http://192.168.203.39/interface/communication";
	
//	private RecordDataBaseHelper mDBHelper;
	
	private RecommendFragment recommend_frag;
	private LiveRecordFragment live_frag;
	private RecordedFragment record_frag;
	
	TVCloudActivity mActivity;
	
	private Fragment fg;
	
	public ViewFrameZoomIndicator mViewIndicator;
	View root;
	SimpleTab tab;
	ProgressBar space_bar;
	TextView space_size;
	PopupWindow pop;
	TextView current_page,total_page;
	//private View currentFocusView;
	
	private double totalSize;//总空间
	private double occupySize;//已占空间
	private double laveSize;//剩余空间
	private double preSize;//预占空间
	
	Handler mHandler = new Handler();
	
	public void setHandler(Handler handler) {
		this.mHandler = handler;
	}
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tvcloud_activity_main);
        mActivity = this;        
        root = findViewById(R.id.root_view);
        tab = (SimpleTab) findViewById(R.id.cloud_tab);
        space_bar = (ProgressBar) findViewById(R.id.space_bar);
        space_size = (TextView) findViewById(R.id.space_size);
        total_page = (TextView) findViewById(R.id.total_page);
        current_page = (TextView) findViewById(R.id.current_page);
        updateView();        
        tab.setOnTabChangeListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChange(int index, View selectedView) {
				
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				switch (index) {
				case 0:
					root.setBackgroundResource(R.drawable.tvcloud_bg);
					ft.replace(R.id.content_layout, recommend_frag).commitAllowingStateLoss();
					fg = recommend_frag;
					break;
				case 1:
					root.setBackgroundResource(R.drawable.tvcloud_bg1);
					ft.replace(R.id.content_layout, live_frag).commitAllowingStateLoss();
					fg = live_frag;
					break;
				case 2:
					root.setBackgroundResource(R.drawable.tvcloud_bg2);
					ft.replace(R.id.content_layout, record_frag).commitAllowingStateLoss();
					fg = record_frag;
					break;
				}
			}
		});
        tab.getChildAt(1).setOnKeyListener(new OnKeyListener() {
			
			@Override
			public boolean onKey(View view, int code, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (code == KeyEvent.KEYCODE_DPAD_RIGHT) {
						mHandler.sendEmptyMessage(10); //发送到fragment的handler
					}
				}
				return false;
			}
		});
        
        recommend_frag = new RecommendFragment();
        live_frag = new LiveRecordFragment();
        record_frag = new RecordedFragment();
		
		mViewIndicator = new ViewFrameZoomIndicator(this);
		mViewIndicator.setFrameResouce(R.drawable.tvcloud_focus_001);
		mViewIndicator.setAnimationTime(200);
		if (getCurrentFocus() != tab || getCurrentFocus().getParent() != tab)
			tab.getChildAt(0).requestFocus();
//		mDBHelper = RecordDataBaseHelper.getInstance();
//		mDBHelper.init(getApplicationContext());
		
		getSpaceCapacity();
    }
    
    private void updateView() {
    	totalSize = 1000;
        preSize = 270;
        occupySize = 499.7;
        laveSize = totalSize - occupySize;
        int progress = (int)((laveSize /totalSize)*10000);
        int secondaryProgress = (int)((preSize /totalSize)*10000);
        space_bar.setProgress(progress);
        space_bar.setSecondaryProgress(secondaryProgress);
        space_size.setText("总空间"+totalSize+"G/剩余"+laveSize+"G");
		
	}

	@Override
    protected void onResume() {
//    	mDBHelper.updateData();
    	super.onResume();
    }
	
	//获得用户空间信息
	public void getSpaceCapacity(){
		ServiceHelper service = ServiceHelper.getHelper();
		service.setRootUrl(TVCloudActivity.SiHuaUrl);
		service.setSerializerType(SerializerType.XML);
		service.callServiceAsync(this, SpaceRequest.setSpaceCapacityRequest(), SpaceCapacity.class,new ResponseHandlerT<SpaceCapacity>() {
			
			@Override
			public void onResponse(boolean success, SpaceCapacity result) {
				if(success){
					if(result.getBody().getSpaceMessageList() == null || 
							result.getBody().getSpaceMessageList().getSpaceMessage() == null){
						Tools.showToastMessage(getApplicationContext(), "5010网络异常");
						return;
					}
					try{
						if("0".equals(result.getBody().getSpaceMessageList().getSpaceMessage().getSpaceStatus())){
							Tools.showToastMessage(getApplicationContext(), getResources().getString(R.string.tvclound_tip8));
						}else if("1".equals(result.getBody().getSpaceMessageList().getSpaceMessage().getSpaceStatus())){
							String totalSpace = result.getBody().getSpaceMessageList().getSpaceMessage().getTotalSpace();
							String usedSpace = result.getBody().getSpaceMessageList().getSpaceMessage().getUsedSpace();
							String lockedSpace = result.getBody().getSpaceMessageList().getSpaceMessage().getLockedSpace();
							Log.d(TAG,"onResponse:result.getTotalSpace="+totalSpace);
							double totalSpaceG = Double.parseDouble(totalSpace)/1024;
							double usedSpaceG = Double.parseDouble(usedSpace)/1024;
							double usedPercent = Double.parseDouble(usedSpace)/ Double.parseDouble(totalSpace);
							double lockdPercent = Double.parseDouble(lockedSpace)/ Double.parseDouble(totalSpace);
							DecimalFormat df = new DecimalFormat("#.00");
							totalSize = Double.parseDouble(df.format(totalSpaceG));
							laveSize = Double.parseDouble(df.format(totalSpaceG - usedSpaceG));
							Log.d(TAG,"onResponse:result.usedPercent="+usedPercent+";lockdPercent="+lockdPercent);
							int progress = (int)(usedPercent * 10000);
					        int secondaryProgress = (int)(lockdPercent * 10000);
					        space_bar.setProgress(progress);
					        space_bar.setSecondaryProgress(secondaryProgress);
					        space_size.setText("总空间" + totalSize + "G/剩余" + laveSize + "G");
						}else if("2".equals(result.getBody().getSpaceMessageList().getSpaceMessage().getSpaceStatus())){
							Tools.showToastMessage(getApplicationContext(), getResources().getString(R.string.tvclound_tip_sihua1));
						}
						
					}catch(Exception e){
						e.printStackTrace();
					}
				}else{
					Tools.showToastMessage(getApplicationContext(), getResources().getString(R.string.tvclound_tip7));
				}
			}
		});
	}
    
    @Override
    protected void onPause() {
    	if (pop != null && pop.isShowing()) 
			pop.dismiss();
    	super.onPause();
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			if (pop != null && pop.isShowing()) {
				pop.dismiss();
			} else {
				final View view = LayoutInflater.from(this).inflate(R.layout.tvcloud_tvadd_layout, null);
				if (pop == null) {
					pop = new PopupWindow(view);
					pop.setFocusable(true);
					pop.setBackgroundDrawable(new BitmapDrawable());
				}
//				root.clearFocus();
//				mViewIndicator.hideFrame();
				final View currentFocusView = root.findFocus();
				findViewById(R.id.logo).setFocusable(true);
				findViewById(R.id.logo).requestFocus();
				pop.setOnDismissListener(new OnDismissListener() {
					
					@Override
					public void onDismiss() {
						currentFocusView.requestFocus();
						findViewById(R.id.logo).setFocusable(false);
					}
				});
				pop.showAtLocation(root, Gravity.BOTTOM, 0, 0);
				pop.update(0, 0, 1920, 209);
				LinearLayout recommend = (LinearLayout) view.findViewById(R.id.tvcloud_recommend);
				LinearLayout mine = (LinearLayout) view.findViewById(R.id.tvcloud_mine);
				LinearLayout jst = (LinearLayout) view.findViewById(R.id.tvcloud_jst);
				recommend.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						pop.dismiss();						
						findViewById(R.id.logo).setFocusable(true);
						findViewById(R.id.logo).requestFocus();
						TVRecommendWindow window = new TVRecommendWindow(mActivity);
						PopupWindow popupWindow = window.getPop();
						popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
						popupWindow.update(0, 0, 1920, 439);
						popupWindow.setOnDismissListener(new OnDismissListener() {						
							@Override
							public void onDismiss() {
								currentFocusView.requestFocus();
								findViewById(R.id.logo).setFocusable(false);							}
						});
					}
				});
				mine.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						PortalDataManager.startMineActivity(TVCloudActivity.this);
					}
				});
				jst.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						Tools.showToastMessage(TVCloudActivity.this, getResources().getString(R.string.is_developing));
					}
				});
				recommend.requestFocus();
			}
			break;
		case KeyEvent.KEYCODE_BACK:
			if (pop != null && pop.isShowing())
				pop.dismiss();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			if(fg instanceof RecordedFragment){
				return ((RecordedFragment) fg).onKeyDown(keyCode,event);
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
}
