package com.ipanel.chongqing_ipanelforhw.downloading.utils;

import java.io.File;

import com.ipanel.chongqing_ipanelforhw.downloading.AppActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownLoadManagerService {

	public static final String TAG = DownLoadManagerService.class
			.getSimpleName();
	private String DOWNLOAD_APKS_PATH = "/data/data/";
	private Context mContext;

	public DownLoadManagerService(Context context) {
		this.mContext = context;
		DOWNLOAD_APKS_PATH += mContext.getPackageName() + "/APKS/";
	}

	public void requestAPK(String url, String packagename,
			final String fileName, final Handler mHandler) {
		Log.v(TAG, "requestAPK()url=" + url);
		DownLoadToolUtil task = new DownLoadToolUtil(fileName, url, true,
				new DownLoadCallBack() {
					@Override
					public void notifydownload(DownLoadToolUtil task, int msg) {
						Log.v(TAG, "updatefile:" + task.getUpdatefile()
								+ ",msg=" + msg);
						if (msg == DownLoadCallBack.DOWNLOAD_SUCCESS) {
							Log.d(TAG, "APKœ¬‘ÿ≥…π¶,");
							Message msg1 = new Message();
							msg1.what = AppActivity.DOWNLOAD_SUCCESS;
							mHandler.sendMessage(msg1);
						} else if (msg == DownLoadCallBack.DOWNLOAD_FAILD) {
							File f = new File(fileName);
							if (f.exists()) {
								f.delete();
							}
						} else if (msg == DownLoadCallBack.DOWNLOAD_PAUSE) {

						}
					}
				});
		try {
			DownLoadManager.getInstance().addDownLoad(fileName, task,
					DownLoadManager.getInstance().apkTaskMap);
		} catch (Exception e) {
			e.printStackTrace();
			Log.d(TAG, "zhoulc exception");
		}
	}
}
