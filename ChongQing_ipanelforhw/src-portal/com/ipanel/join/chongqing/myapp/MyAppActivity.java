package com.ipanel.join.chongqing.myapp;

import java.util.ArrayList;
import java.util.List;

import com.ipanel.chongqing_ipanelforhw.R;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class MyAppActivity extends Activity{

	private static final String TAG = MyAppActivity.class.getSimpleName();
	private ArrayList<App> list = new ArrayList<App>();
	PackageManager pm ;
	PackageInfo pInfo;
	GridView mAppGridView;
	AppGridAdapter mAdapter;
	TextView mRows,mTotalRows;
	View mRootView;
	MenuPopupWindow menuWindow;
	boolean isUninstallState = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.myapp_activity_main);
		mRootView = LayoutInflater.from(this).inflate(
				R.layout.btv_tvadd_layout, null, false);
		mAppGridView = (GridView) findViewById(R.id.app_grid);
		mAppGridView.setAdapter(mAdapter = new AppGridAdapter());
		mRows = (TextView) findViewById(R.id.myapp_rows);
		mTotalRows = (TextView) findViewById(R.id.myapp_total_rows);
		mAppGridView.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mRows.setText(position/6+1 + "");
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				
			}
		});
		mAppGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if(isUninstallState){
					uninstallApp(list.get(position).getPackageName());
				}else{
					startApplication(list.get(position).getPackageName());
				}
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		getAppList();
		int totalRows = list.size()%6 == 0 ? (list.size()/6):(list.size()/6+1);
		mTotalRows.setText(String.format(getResources().getString(R.string.myapp_total_rows), totalRows));
		mAdapter.notifyDataSetChanged();
	}


	public void getAppList(){
		pm = getPackageManager();
		PackageInfo pInfo;
		List<PackageInfo> paklist = pm.getInstalledPackages(0);
		App app;
		list.clear();
		for (int i = 0; i < paklist.size(); i++) {
			pInfo = paklist.get(i);		
			if (null != pm.getLaunchIntentForPackage(pInfo.applicationInfo.packageName)
//					&&((pInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) <= 0)
					) {
				app = new App(); 
				app.setPackageName(pInfo.packageName);
				app.setIcon(pm.getApplicationIcon(pInfo.applicationInfo));
				app.setLabel(pm.getApplicationLabel(pInfo.applicationInfo).toString());
				app.setInstallTime(pInfo.firstInstallTime);
				app.setIsSystem(0);
				list.add(app);
			}
		}
	}
	
	class AppGridAdapter extends BaseAdapter{

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final Holder holder;
			if(convertView == null){
				convertView = LayoutInflater.from(getApplicationContext()).
						inflate(R.layout.myapp_grid_item, parent, false);
				holder = new Holder();
				holder.icon = (ImageView) convertView.findViewById(R.id.app_icon);
				holder.name = (TextView) convertView.findViewById(R.id.app_name);
				holder.del = (ImageView) convertView.findViewById(R.id.app_del);
				holder.uninstall_fl = (FrameLayout) convertView.findViewById(R.id.app_uninstall_fl);
			}else{
				holder = (Holder) convertView.getTag();
			}
			if(isUninstallState){
				holder.uninstall_fl.setVisibility(View.VISIBLE);
			}else{
				holder.uninstall_fl.setVisibility(View.GONE);
			}
			holder.icon.setImageDrawable(list.get(position).getIcon());
			holder.name.setText(list.get(position).getLabel());
			convertView.setTag(holder);
			return convertView;
		}
	}
	
	class Holder{
		ImageView icon;
		TextView name;
		ImageView del;
		FrameLayout uninstall_fl;
	}
	//启动应用程序
	public void startApplication(String packagename){
		try {
			Intent intent = getPackageManager().getLaunchIntentForPackage(packagename);
			if (intent != null)
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("", "no app");
		}
	}
	//卸载应用
	private void uninstallApp(String packagename) {
		// 系统自带的卸载
		Uri uri = Uri.parse("package:" + packagename);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		startActivity(intent);
//		PackageManager pm = getPackageManager();
//		pm.deletePackage(packagename, new IPackageDeleteObserver.Stub() {
//			public void packageDeleted(String arg0, int returnCode)
//					throws RemoteException {
//				Log.i(TAG, "arg0:" + arg0 + "---returnCode:" + returnCode);
//			}
//		}, 0);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode){
		case KeyEvent.KEYCODE_MENU:
			if(isMenuWindowShowing()){
				hideMenuWindow();
			}else{
				showMenuWindow();
			}
			return true;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			if(isMenuWindowShowing()){
				hideMenuWindow();
				isUninstallState = true;
				mAdapter.notifyDataSetChanged();
				return true;
			}
			break;
		case KeyEvent.KEYCODE_BACK:
		case KeyEvent.KEYCODE_ESCAPE:
			if(isUninstallState){
				isUninstallState = false;
				mAdapter.notifyDataSetChanged();
				return true;
			}
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	private void showMenuWindow() {
		if(menuWindow == null)
			menuWindow = new MenuPopupWindow(this);
		PopupWindow popupWindow = menuWindow.getPop();
		popupWindow.showAtLocation(mRootView, Gravity.BOTTOM, 0, 0);
		popupWindow.update(0, 0, 1920, 212);
	}
	
	private boolean isMenuWindowShowing(){
		if(menuWindow != null)
			return menuWindow.isMenuWindowShowing();
		return false;
	}
	
	private void hideMenuWindow(){
		if(menuWindow != null)
			menuWindow.hideMenuWindow();
	}
}
