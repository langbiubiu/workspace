package com.ipanel.join.cq.vod.vodhome;

import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.chongqing.portal.VolumePanel;
import com.ipanel.join.cq.vod.HWDataManager;
import com.ipanel.join.cq.vod.jsondata.GlobalFilmData;
import com.ipanel.join.cq.vod.utils.GlobalContext;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.protocol.a7.ServiceHelper;


public class FilmListActivity extends BaseActivity{
	public static final String TAG = FilmListActivity.class.getSimpleName();
	private String columnName;//栏目名称
	private String typeId;//栏目Id
	private SortFragment sortFragment;
	/**
	 * 根栏目是否有子栏目
	 * */
	private boolean mSub = false;
	
	private BroadcastReceiver receiver;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.vod_main_layout);
		GlobalContext.init(FilmListActivity.this);
		volPanel = new VolumePanel(this);
		Logger.d(TAG, "start receiver");
		this.registerReceiver(receiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				Logger.d("已经接收到更新广播CookieString = " + intent.getExtras().getString("CookieString").toString());
				GlobalFilmData.getInstance().setCookieString(intent.getExtras().getString("CookieString"));
				// 还需要添加EPG的即时更新
				String epg = intent.getExtras().getString("EPG");
				String serviceGroupId = "" + intent.getExtras().getLong("ServiceGroupId");
				String smartcard = intent.getExtras().getString("smartcard");
				GlobalFilmData.getInstance().setEpgUrl(epg);
				GlobalFilmData.getInstance().setGroupServiceId(serviceGroupId);
				GlobalFilmData.getInstance().setCardID(smartcard);
				GlobalFilmData.getInstance().setIcState(intent.getExtras().getString("icState"));

				Logger.d("icState: " + intent.getExtras().getString("icState"));
			}
		}, new IntentFilter("com.ipanel.join.cq.vodauth.EPG_URL"));
		super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void onResume() {
		getIntentData();
		super.onResume();
	}

	private void getIntentData() {
		if(getIntent() != null){
			columnName = getIntent().getStringExtra("name");
			typeId = getIntent().getStringExtra("params");
			mSub = "1".equals(getIntent().getStringExtra("sub"));
			Logger.d(TAG, "columnName:"+columnName+"typeId:"+typeId);
			showMovieListFragment();
		}
	}
	MovieListFragment movieFragment;
	private void showMovieListFragment() {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		movieFragment = new MovieListFragment(columnName,typeId,mSub);
		transaction.add(R.id.frameLayout, movieFragment);
		transaction.commit();
	}

	@Override
	protected void onPause() {
		setIntent(null);
		super.onPause();
	}
	
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		super.onNewIntent(intent);
	}
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		ServiceHelper.getHelper().cancelAllTasks();
		unregisterReceiver(receiver);
	}
	
	public void buttonClick(View v){
		switch (v.getId()) {
		case R.id.vod_filter:
			if(getColumnName().equals("电影")||getColumnName().equals("电视剧")){
				showSortFragment();
			}
			break;
		case R.id.vod_search:
			HWDataManager.openSearchPage(getBaseContext());
			break;
		default:
			break;
		}
	}

	private void showSortFragment() {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		transaction.hide(movieFragment);
		if(sortFragment == null){
			sortFragment = new SortFragment();
		}
		transaction.add(R.id.frameLayout, sortFragment);
		transaction.addToBackStack(null);
		transaction.commit();
	}
	
	public String getColumnName(){
		return columnName;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if(volPanel.onKeyDown(keyCode, event)){
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
