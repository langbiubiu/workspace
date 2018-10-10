package com.ipanel.chongqing_ipanelforhw.downloading.utils;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.IPackageDeleteObserver;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.IPackageMoveObserver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.os.RemoteException;
import android.util.Log;

public class ApkOperateManager {
	public static String TAG = "ApkOperateManager";

	/* 安装apk */
	public static void installApk(Context context, String fileName) {
		Log.v(TAG, "installApk...fileName=" + fileName);
		Intent intent = new Intent();
		intent.setAction(Intent.ACTION_VIEW);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setDataAndType(Uri.parse("file://" + fileName),
				"application/vnd.android.package-archive");
		context.startActivity(intent);
	}

	/* 卸载apk */
	public static void uninstallApk(Context context, String packageName) {
		Uri uri = Uri.parse("package:" + packageName);
		Intent intent = new Intent(Intent.ACTION_DELETE, uri);
		context.startActivity(intent);
	}

	/**
	 * 静默安装
	 * */
	public static void installApkDefaul(Context context, String fileName,
			String packageName) {
		Log.d(TAG, "jing mo an zhuang:" + packageName + ",fileName:" + fileName);
		File file = new File(fileName);
		int installFlags = 0;
		if (!file.exists())
			return;
		installFlags |= PackageManager.INSTALL_REPLACE_EXISTING;
		if (hasSdcard()) {
			// installFlags |= PackageManager.INSTALL_EXTERNAL;
		}
		PackageManager pm = context.getPackageManager();
		// try {
		// if (!packageName.equals("com.ipanel.market")
		// && pm.getPackageInfo(packageName, 0) != null) {
		// uninstallApkDefaul(context, "UNINSTALL_BEFORE_INSTALL",
		// packageName);
		// }
		// } catch (NameNotFoundException e) {
		// e.printStackTrace();
		// }
		try {
			Uri mPackageURI = Uri.fromFile(file);
			IPackageInstallObserver observer = new MyPakcageInstallObserver(
					context, fileName, packageName);
			Log.i(TAG, "########installFlags:" + installFlags + ",packagename:"
					+ packageName + ",uri:" + mPackageURI);
			pm.installPackage(mPackageURI, observer, installFlags, packageName);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/* 静默卸载 */
	public static void uninstallApkDefaul(Context context, String action,
			String packageName) {
		PackageManager pm = context.getPackageManager();
		IPackageDeleteObserver observer = new MyPackageDeleteObserver(context,
				action, packageName);
		pm.deletePackage(packageName, observer, 0);
	}

	/* 静默卸载回调 */
	private static class MyPackageDeleteObserver extends
			IPackageDeleteObserver.Stub {
		Context cxt;
		String action;
		String pkname;

		public MyPackageDeleteObserver(Context c, String action, String pkname) {
			this.cxt = c;
			this.action = action;
			this.pkname = pkname;
		}

		@Override
		public void packageDeleted(String packageName, int returnCode) {
			Log.d(TAG, "returnCode = " + returnCode + ",action:" + action
					+ ",packageName:" + packageName + ",pkname:" + pkname);// 返回1代表卸载成功
			if (returnCode == 1) {

			}
			Intent it = new Intent();
			it.setAction(action);
			it.putExtra("uninstall_returnCode", returnCode);
			cxt.sendBroadcast(it);
		}
	}

	/* 静默安装回调 */
	private static class MyPakcageInstallObserver extends
			IPackageInstallObserver.Stub {
		Context cxt;
		String filename;
		String pkname;

		public MyPakcageInstallObserver(Context c, String filename,
				String packagename) {
			this.cxt = c;
			this.filename = filename;
			this.pkname = packagename;
		}

		@Override
		public void packageInstalled(String packageName, int returnCode) {
			Log.i(TAG, "returnCode = " + returnCode + ", packageName="
					+ packageName);// 返回1代表安装成功
			Intent it = new Intent();
			
			it.setAction(Intent.ACTION_PACKAGE_ADDED);
			if (returnCode == 1) {

			} else {

			}
			File f = new File(filename);
			if (f.exists()) {
				f.delete();
			}
			cxt.sendBroadcast(it);
		}
	}

	/**
	 * sd卡不存在
	 */
	public static final int NO_SDCARD = -1;

	/**
	 * 移动应用到SD Card
	 * 
	 * @param context
	 * @param pkname
	 * @return
	 */
	public static void movePackage(Context context, String pkname) {
		PackageManager pm = context.getPackageManager();
		MovePackageObserver mpo = new MovePackageObserver();
		pm.movePackage(pkname, mpo, PackageManager.INSTALL_EXTERNAL);
	}

	/**
	 * 移动应用的回调
	 */
	public static class MovePackageObserver extends IPackageMoveObserver.Stub {

		public MovePackageObserver() {
		}

		@Override
		public void packageMoved(String packageName, int returnCode)
				throws RemoteException {
			Log.i(TAG, "packagename:" + packageName + ",returnCode:"
					+ returnCode);
		}
	}

	/**
	 * 判断有无sd卡
	 * */
	public static boolean hasSdcard() {
		String status = Environment.getExternalStorageState();
		if (status.equals(Environment.MEDIA_MOUNTED)
				|| status.equals("/mnt/sdcard")) {
			Log.i(TAG, "has sdcard....");
			return true;
		} else {
			return false;
		}
	}

	public static final String LOCAL = "installedAppInfoPrefrences";
	public static final String[] LOCAL_KEYLIST = { "appId", "appName",
			"pkname", "type_name" };

	/**
	 * sharedPrefrences 保存数据
	 * 
	 * @param context
	 * @param tag
	 * @param key
	 * @param hashMap
	 * @return
	 */
	public static boolean setLocalInfo(Context context, String tag, String key,
			HashMap<String, String> hashMap) {
		SharedPreferences sp = context.getSharedPreferences(LOCAL,
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		Editor editor = sp.edit();
		Set<String> appinfo = new HashSet<String>();
		Set<String> keys = hashMap.keySet();
		Object[] keysArr = keys.toArray();
		for (int i = 0; i < keysArr.length; i++) {
			String subkey = (String) keysArr[i];
			String value = hashMap.get(subkey);
			appinfo.add(subkey + "," + value);
		}
		Log.v(TAG, "setLocalInfo..." + key + "=" + appinfo.toString());
		editor.putStringSet(key, appinfo);
		return editor.commit();
	}

	/**
	 * sharedPrefrences 获取数据
	 * 
	 * @param context
	 * @param tag
	 * @param key
	 * @return
	 */
	public static HashMap<String, String> getLocalInfo(Context context,
			String tag, String key) {
		SharedPreferences sp = context.getSharedPreferences(LOCAL,
				Context.MODE_WORLD_WRITEABLE | Context.MODE_WORLD_READABLE);
		Set<String> appinfo = new HashSet<String>();
		appinfo = sp.getStringSet(key, appinfo);
		HashMap<String, String> hashMap = new HashMap<String, String>();
		if (appinfo.size() > 0) {
			String[] data = (String[]) appinfo.toArray(new String[appinfo
					.size()]);
			for (int i = 0; i < data.length; i++) {
				String[] info = data[i].trim().split(",");
				if (info.length > 2) {
					String subkey = info[0];
					String value = info[1];
					hashMap.put(subkey, value);
				}
			}
		}
		Log.v(TAG, "getLocalInfo..." + key + "=" + appinfo.toString());
		return hashMap;
	}

}
