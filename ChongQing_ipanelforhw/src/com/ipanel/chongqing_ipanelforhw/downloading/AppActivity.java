package com.ipanel.chongqing_ipanelforhw.downloading;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageDataObserver;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import cn.ipanel.android.widget.IFrameIndicator;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.chongqing_ipanelforhw.downloading.utils.ApkOperateManager;
import com.ipanel.chongqing_ipanelforhw.downloading.utils.AppFrameZoomIndicator;
import com.ipanel.chongqing_ipanelforhw.downloading.utils.AppNoticeDialog;
import com.ipanel.chongqing_ipanelforhw.downloading.utils.AppNoticeDialog.NoticeDialogListener;
import com.ipanel.chongqing_ipanelforhw.downloading.utils.DownLoadManagerService;

/**
 * 应用下载
 * 
 * @author liuf
 * 
 */
public class AppActivity extends Activity implements OnClickListener,
		OnFocusChangeListener {

	private String TAG = AppActivity.class.getSimpleName();
	private Context mContext = AppActivity.this;
	private ImageView appIcon, appScreenshotImg1, appScreenshotImg2,
			appScreenshotImg3;
	private TextView appNameTxt, appLikesTxt, appDownTimesTxt, appRemarksTxt,
			appDesTxt, appSizeTxt, appUpdateTimeTxt, appVersionTxt,
			appDevelopersTxt;
	private Button downBtn, clearBtn, delBtn, likesBtn;
	private ProgressBar downBar;
	private String sId, sName, sLikes, sDownTimes, sRemarks, sDes, sSize,
			sUpdateTime, sVersion, sDevelopers, packageName, apkUrl;;
	private IFrameIndicator mFocusFrame;
	private Object lock;
	private Bundle b;
	private static final String APP_KEY_ID = "app_id";
	private static final String APP_KEY_NAME = "app_name";
	private static final String APP_KEY_LIKES = "app_likes";
	private static final String APP_KEY_DOWNTIMES = "app_downtimes";
	private static final String APP_KEY_REMARKS = "app_remarks";
	private static final String APP_KEY_DES = "app_des";
	private static final String APP_KEY_SIZE = "app_size";
	private static final String APP_KEY_UPDATETIME = "app_updatetime";
	private static final String APP_KEY_VERSION = "app_version";
	private static final String APP_KEY_DEVELOPERS = "app_developers";
	private static final String APP_KEY_PACKAGENAME = "packageName";
	private static final String APP_KEY_APKURL = "apkUrl";

	public static final int DOWNLOAD_SUCCESS = 1;
	public static final int DOWNLOAD_START = 11;
	public static final int DOWNLOAD_DOING = 12;
	public static final int INSTALLED_SUCCESS = 2;
	public static final int UNINSTALL_SUCCESS = 3;
	public static final int CLEAR_SUCCESS = 4;
	public static final int DOLIKE_SUCCESS = 5;
	private Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			logg("handleMessage()msg=" + msg);
			switch (msg.what) {
			case DOWNLOAD_START:
				downBar.setMax(100);
				break;
			case DOWNLOAD_DOING:
				logg("apkDownedFileLength=" + apkDownedFileLength + ", max="
						+ downBar.getMax());
				downBar.setProgress((int) apkDownedFileLength);
				downBtn.setText(R.string.app_btn_down_doing);
				break;
			case DOWNLOAD_SUCCESS:
				isDownloadTaskOK = true;
				downBar.setProgress(100);
				downBtn.setText(R.string.app_btn_down_done);
				initProgressBar();
				prohibitFocus(true);
				installApp();
				break;
			case INSTALLED_SUCCESS:
				initDownBtnText();
				break;
			case UNINSTALL_SUCCESS:
				initDownBtnText();
				break;
			case CLEAR_SUCCESS:
				break;
			case DOLIKE_SUCCESS:
				break;
			}
			super.handleMessage(msg);
		}
	};
	private AppNoticeDialog notiDialog;
	private String apkSavePath = "", fileName = "";
	private File apkSavePathfile = null;
	private File apkfile = null;
	private long apkFileLength;
	private long apkDownedFileLength = 0;
	private InputStream inputStream;
	private URLConnection connection;
	private OutputStream outputStream;
	private Thread dpBar_Th;
	private boolean isDownloadTaskOK = false;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		logg("onCreate()mContext=" + mContext);
		setContentView(R.layout.app_activity);
		apkSavePath = "/data/data/" + getPackageName() + "/APKS/";
		initIFrameIndicator();
		getData();
		initView();
		initData();
	}

	@Override
	protected void onResume() {
		super.onResume();
		logg("onResume()mContext=" + mContext);
		/**
		 * 注册广播
		 */
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_PACKAGE_ADDED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_CHANGED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REMOVED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_REPLACED);
		intentFilter.addAction(Intent.ACTION_PACKAGE_DATA_CLEARED);
		registerReceiver(mReceiver, intentFilter);
	}

	@Override
	protected void onPause() {
		super.onPause();
		logg("onPause()mContext=" + mContext);
		/**
		 * 销毁广播
		 */
		unregisterReceiver(mReceiver);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		logg("onDestroy()mContext=" + mContext);
	}

	/**
	 * 初始化
	 */
	private void initView() {
		appIcon = (ImageView) findViewById(R.id.appIcon);
		appScreenshotImg1 = (ImageView) findViewById(R.id.appScreenshotImg1);
		appScreenshotImg1.setOnFocusChangeListener(this);
		appScreenshotImg2 = (ImageView) findViewById(R.id.appScreenshotImg2);
		appScreenshotImg2.setOnFocusChangeListener(this);
		appScreenshotImg3 = (ImageView) findViewById(R.id.appScreenshotImg3);
		appScreenshotImg3.setOnFocusChangeListener(this);

		appNameTxt = (TextView) findViewById(R.id.appNameTxt);
		appLikesTxt = (TextView) findViewById(R.id.appLikesTxt);
		appDownTimesTxt = (TextView) findViewById(R.id.appDownTimesTxt);
		appRemarksTxt = (TextView) findViewById(R.id.appRemarksTxt);
		appDesTxt = (TextView) findViewById(R.id.appDesTxt);
		appSizeTxt = (TextView) findViewById(R.id.appSizeTxt);
		appUpdateTimeTxt = (TextView) findViewById(R.id.appUpdateTimeTxt);
		appVersionTxt = (TextView) findViewById(R.id.appVersionTxt);
		appDevelopersTxt = (TextView) findViewById(R.id.appDevelopersTxt);

		downBar = (ProgressBar) findViewById(R.id.downBar);
		downBar.setMax(100);
		initProgressBar();
		downBtn = (Button) findViewById(R.id.downBtn);
		downBtn.setOnClickListener(this);
		downBtn.setOnFocusChangeListener(this);
		downBtn.requestFocus();
		initDownBtnText();
		clearBtn = (Button) findViewById(R.id.clearBtn);
		clearBtn.setOnClickListener(this);
		clearBtn.setOnFocusChangeListener(this);
		delBtn = (Button) findViewById(R.id.delBtn);
		delBtn.setOnClickListener(this);
		delBtn.setOnFocusChangeListener(this);
		likesBtn = (Button) findViewById(R.id.likesBtn);
		likesBtn.setOnClickListener(this);
		likesBtn.setOnFocusChangeListener(this);
	}

	/**
	 * 赋予数据
	 */
	private void initData() {
		appIcon.setBackgroundResource(R.drawable.app_app0);
		appScreenshotImg1.setBackgroundResource(R.drawable.app_pop_img1_1);
		appScreenshotImg2.setBackgroundResource(R.drawable.app_pop_img1_2);
		appScreenshotImg3.setBackgroundResource(R.drawable.app_pop_img1_3);

		appNameTxt.setText(sName);
		appLikesTxt.setText(getSpecialText(R.string.app_likes, sLikes));
		appDownTimesTxt.setText(getSpecialText(R.string.app_down_times,
				sDownTimes));
		appRemarksTxt.setText(sRemarks);
		appDesTxt.setText(sDes);
		appSizeTxt.setText(getSpecialText(R.string.app_size, sSize));
		appUpdateTimeTxt.setText(getSpecialText(R.string.app_update_time,
				sUpdateTime));
		appVersionTxt.setText(getSpecialText(R.string.app_version, sVersion));
		appDevelopersTxt.setText(getSpecialText(R.string.app_developers,
				sDevelopers));
	}

	/**
	 * 获取数据
	 */
	private void getData() {
		sId = "0";
		sName = getString(R.string.app_title);
		sLikes = "245";
		sDownTimes = "12543";
		sRemarks = getString(R.string.app_remarks);
		sDes = getString(R.string.app_des);
		sSize = "6736MB";
		sUpdateTime = "2016-01-12";
		sVersion = "1.1.2";
		sDevelopers = "iPanel.TV Inc.";
		packageName = "com.ipanel.game.backroom";
		apkUrl = "http://task.ipanel.cn/libdownload/downFiles2/20160111.546748.Backroom.apk/task_doc/86512";
		b = getIntent().getExtras();
		if (b == null) {
			logg("can't recevice extras");
		} else {
			if (b.containsKey(APP_KEY_ID)) {
				sId = b.getString(APP_KEY_ID);
			}
			if (b.containsKey(APP_KEY_NAME)) {
				sName = b.getString(APP_KEY_NAME);
			}
			if (b.containsKey(APP_KEY_LIKES)) {
				sLikes = b.getString(APP_KEY_LIKES);
			}
			if (b.containsKey(APP_KEY_DOWNTIMES)) {
				sDownTimes = b.getString(APP_KEY_DOWNTIMES);
			}
			if (b.containsKey(APP_KEY_REMARKS)) {
				sRemarks = b.getString(APP_KEY_REMARKS);
			}
			if (b.containsKey(APP_KEY_DES)) {
				sDes = b.getString(APP_KEY_DES);
			}
			if (b.containsKey(APP_KEY_SIZE)) {
				sSize = b.getString(APP_KEY_SIZE);
			}
			if (b.containsKey(APP_KEY_UPDATETIME)) {
				sUpdateTime = b.getString(APP_KEY_UPDATETIME);
			}
			if (b.containsKey(APP_KEY_VERSION)) {
				sVersion = b.getString(APP_KEY_VERSION);
			}
			if (b.containsKey(APP_KEY_DEVELOPERS)) {
				sDevelopers = b.getString(APP_KEY_DEVELOPERS);
			}
			if (b.containsKey(APP_KEY_PACKAGENAME)) {
				packageName = b.getString(APP_KEY_PACKAGENAME);
			}
			if (b.containsKey(APP_KEY_APKURL)) {
				apkUrl = b.getString(APP_KEY_APKURL);
			}
		}
	}

	/**
	 * 属性值替换、修改部分文字颜色
	 * 
	 * @param rid
	 * @param s
	 * @return
	 */
	private SpannableStringBuilder getSpecialText(int rid, String s) {
		String fString = String.format(getResources().getString(rid), s);
		ForegroundColorSpan what = new ForegroundColorSpan(getResources()
				.getColor(R.color.app_color_white2));
		int start = 0;
		int end = fString.length();
		int flags = Spannable.SPAN_INCLUSIVE_EXCLUSIVE;
		switch (rid) {
		case R.string.app_likes:
			what = new ForegroundColorSpan(getResources().getColor(
					R.color.app_color_orange));
			end = s.length();
			break;
		case R.string.app_down_times:
			what = new ForegroundColorSpan(getResources().getColor(
					R.color.app_color_green));
			end = s.length();
			break;
		case R.string.app_size:
			start = 2;
			break;
		case R.string.app_update_time:
			start = 4;
			break;
		case R.string.app_version:
			start = 2;
			break;
		case R.string.app_developers:
			start = 3;
			break;
		}
		SpannableStringBuilder builder = new SpannableStringBuilder(fString);
		builder.setSpan(what, start, end, flags);
		return builder;
	}

	@Override
	public void onFocusChange(View v, boolean hasFocus) {
		logg("onFocusChange()v=" + v + ",hasFocus=" + hasFocus);
		switch (v.getId()) {
		case R.id.appScreenshotImg1:
		case R.id.appScreenshotImg2:
		case R.id.appScreenshotImg3:
			moveFocusFrameTo(getCurrentFocus());
			break;
		case R.id.downBtn:
			hideFrame();
			downBar.setActivated(hasFocus);
			break;
		case R.id.clearBtn:
		case R.id.delBtn:
		case R.id.likesBtn:
			hideFrame();
			break;
		}
	}

	@Override
	public void onClick(View v) {
		logg("onClick()v=" + v);
		switch (v.getId()) {
		case R.id.downBtn:
			if (downBtn.getText().toString()
					.equals(getText(R.string.app_btn_open))) {
				openApp();
			} else {
				downBtn.setClickable(false);
				prohibitFocus(false);
				downloadApp();
			}
			break;
		case R.id.clearBtn:
			clearAppCache();
			break;
		case R.id.delBtn:
			deleteApp();
			break;
		case R.id.likesBtn:
			likesApp();
			break;
		}
	}

	/**
	 * 初始化焦点框
	 */
	private void initIFrameIndicator() {
		mFocusFrame = new AppFrameZoomIndicator(this);
		mFocusFrame.setFrameResouce(R.drawable.portal_focus);
		mFocusFrame.setPadding(12, 6, 32, 36);
	}

	/**
	 * 焦点移动
	 * 
	 * @param v
	 */
	private void moveFocusFrameTo(View v) {
		if (v == null) {
			return;
		}
		mFocusFrame.moveFrameTo(v, true, false);
	}

	/**
	 * 焦点框隐藏
	 */
	private void hideFrame() {
		mFocusFrame.hideFrame();
	}

	/**
	 * 打印
	 * 
	 * @param msg
	 */
	private void logg(String msg) {
		Log.v(TAG, msg);
	}

	/**
	 * 进度条
	 */
	private void initProgressBar() {
		// downBar.setActivated(false);
		// downBar.setMax(100);
		// downBar.setProgress(0);
	}

	/**
	 * 当在下载中时，禁止、允许焦点移动，下载完成后可以移动
	 */
	private void prohibitFocus(boolean isAllow) {
		// clearBtn.setFocusable(isAllow);
		// delBtn.setFocusable(isAllow);
		// likesBtn.setFocusable(isAllow);
		// appScreenshotImg1.setFocusable(isAllow);
		// appScreenshotImg2.setFocusable(isAllow);
		// appScreenshotImg3.setFocusable(isAllow);
	}

	/**
	 * 清除应用缓存-调用系统隐藏API
	 */
	private void clearAppCache() {
		logg("clearAppCache()-packageName=" + packageName);
		if ("".equals(packageName) || packageName == null) {
			return;
		}
		if (checkApkExist(packageName)) {
			PackageManager pm = getPackageManager();
			// ActivityManager am = (ActivityManager)
			// getSystemService(Context.ACTIVITY_SERVICE);
			// am.forceStopPackage(packageName);
			lock = new Object();
			pm.deleteApplicationCacheFiles(packageName,
					new IPackageDataObserver.Stub() {

						@Override
						public void onRemoveCompleted(String packageName,
								boolean succeeded) throws RemoteException {
							logg("onRemoveCompleted()packageName="
									+ packageName + ", succeeded=" + succeeded);
							Message msg = new Message();
							msg.what = CLEAR_SUCCESS;
							mHandler.sendMessage(msg);
							synchronized (lock) {
								lock.notify();
							}
						}
					});
			synchronized (lock) {
				try {
					lock.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 删除应用-弹出框提示
	 */
	private void deleteApp() {
		notiDialog = new AppNoticeDialog(mContext, R.style.AppNoticeDialog,
				new NoticeDialogListener() {
					@Override
					public void onClick(View view) {
						if (view.getId() == R.id.dialog_enter) {
							delApp();
						}
						notiDialog.dismiss();
					}
				});
		notiDialog.setContentView(R.layout.app_dialog_choise);
		notiDialog.show();
	}

	/**
	 * 删除应用-确认删除
	 */
	private void delApp() {
		if ("".equals(packageName) || packageName == null) {
			return;
		}
		// 系统自带的卸载
		if (checkApkExist(packageName)) {
			// Uri uri = Uri.parse("package:" + packageName);
			// Intent intent = new Intent(Intent.ACTION_DELETE, uri);
			// startActivity(intent);

			ApkOperateManager.uninstallApkDefaul(mContext,
					Intent.ACTION_PACKAGE_REMOVED, packageName);
		}
	}

	/**
	 * 顶
	 */
	private void likesApp() {

	}

	/**
	 * 安装应用
	 */
	private void installApp() {
		if ("".equals(packageName) || packageName == null) {
			return;
		}
		// 系统自带的安装
		// Uri uri = Uri.parse("package:" + packageName);
		// Intent intent = new Intent(Intent.ACTION_INSTALL_PACKAGE, uri);
		// startActivity(intent);

		// 系统自带的安装
		// Intent intent = new Intent();
		// intent.setAction(Intent.ACTION_VIEW);
		// intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// intent.setDataAndType(Uri.parse("file://" + fileName),
		// "application/vnd.android.package-archive");
		// mContext.startActivity(intent);

		try {
			// 将下载后APK存放的目录修改下权限
			String cmd1 = "chmod 755 " + apkSavePath;
			Runtime.getRuntime().exec(cmd1);

			String cmd = "chmod 755 " + fileName;
			Runtime.getRuntime().exec(cmd);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// 静默安装
		ApkOperateManager.installApkDefaul(mContext, fileName, packageName);
	}

	private DownLoadManagerService downloadService;

	/**
	 * 下载
	 */
	private void downloadApp() {
		Thread threadlength = new Thread() {
			public void run() {
				apkFileLength = getApkLength(apkUrl);
			}
		};
		threadlength.start();
		new Handler().postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					fileName = apkUrl.substring(apkUrl.lastIndexOf("/") + 1);
					logg("apkUrl=" + apkUrl);
					logg("fileName=" + fileName);
					// fileName = apkSavePath + "/Backroom.apk";
					// DownFile(apkUrl, fileName);
					updateDownloadApkLength();
					downloadService = new DownLoadManagerService(mContext);
					downloadService.requestAPK(apkUrl, packageName, fileName,
							mHandler);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 500);
	}

	/**
	 * 下载进度
	 */
	private void updateDownloadApkLength() {
		isDownloadTaskOK = false;
		final File file = new File(fileName);
		dpBar_Th = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						if (isDownloadTaskOK) {
							dpBar_Th = null;
							break;
						}
						long fl = file.length();
						logg("file.length()=" + fl);
						apkDownedFileLength = fl * 100 / apkFileLength;
						logg("apkDownedFileLength=" + apkDownedFileLength
								+ ",apkFileLength=" + apkFileLength);
						Message msg = new Message();
						msg.what = DOWNLOAD_DOING;
						mHandler.sendMessage(msg);
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		});
		dpBar_Th.start();
	}

	/**
	 * 获取网络文件的大小
	 * 
	 * @param urlString
	 */
	private long getApkLength(final String urlString) {
		apkFileLength = 0;
		try {
			URL url = new URL(urlString);
			connection = url.openConnection();
			logg("connection.getReadTimeout()=" + connection.getReadTimeout());
			if (connection.getReadTimeout() == 5) {
				logg("当前网络有问题");
			}
			apkFileLength = connection.getContentLength();
		} catch (MalformedURLException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return apkFileLength;
	}

	/**
	 * 1、连接到服务器<br>
	 * 2、文件的保存路径，如果不存在则新建<br>
	 * 3、向SD卡中写入文件，用Handle传递线程<br>
	 * 
	 * @param urlString
	 * @param filePath
	 */
	private void DownFile(final String urlString, final String filePath) {
		Thread thread = new Thread() {
			public void run() {
				logg("urlString=" + urlString + ", filePath=" + filePath);
				try {
					URL url = new URL(urlString);
					connection = url.openConnection();
					logg("connection.getReadTimeout()="
							+ connection.getReadTimeout());
					if (connection.getReadTimeout() == 5) {
						logg("当前网络有问题");
					}
					inputStream = connection.getInputStream();
				} catch (MalformedURLException e1) {
					e1.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

				apkSavePathfile = new File(apkSavePath);
				if (!apkSavePathfile.exists()) {
					apkSavePathfile.mkdir();
				}
				apkfile = new File(filePath);
				if (!apkfile.exists()) {
					try {
						apkfile.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				Message message = new Message();
				try {
					outputStream = new FileOutputStream(apkfile);
					byte[] buffer = new byte[1024 * 4];
					apkFileLength = connection.getContentLength();
					apkDownedFileLength = 0;
					message.what = DOWNLOAD_START;
					mHandler.sendMessage(message);
					while (apkDownedFileLength < apkFileLength) {
						outputStream.write(buffer);
						apkDownedFileLength += inputStream.read(buffer);
						// logg("apkDownedFileLength=" + apkDownedFileLength);
						Message message1 = new Message();
						message1.what = DOWNLOAD_DOING;
						mHandler.sendMessage(message1);
					}
					Message message2 = new Message();
					message2.what = DOWNLOAD_SUCCESS;
					mHandler.sendMessage(message2);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}

	/**
	 * 应用安装即打开，否则为下载
	 */
	private void initDownBtnText() {
		if (checkApkExist(packageName)) {
			downBtn.setText(R.string.app_btn_open);
		} else {
			downBtn.setText(R.string.app_btn_down);
		}
		downBtn.setClickable(true);
		downBar.setMax(100);
		downBar.setProgress(0);
	}

	/**
	 * 打开应用
	 */
	private void openApp() {
		PackageManager packageManager = mContext.getPackageManager();
		Intent it = packageManager.getLaunchIntentForPackage(packageName);
		if (it != null) {
			mContext.startActivity(it);
		} else {
			logg("APP not found!,pkname:" + packageName);
		}
	}

	/**
	 * 判断应用是否安装
	 * 
	 * @param packageName
	 * @return
	 */
	private boolean checkApkExist(String packageName) {
		logg("checkApkExist()packageName=" + packageName);
		if (packageName == null || "".equals(packageName))
			return false;
		try {
			ApplicationInfo info = mContext.getPackageManager()
					.getApplicationInfo(packageName, 0);
			logg("checkApkExist()info=" + info);
			if (info != null) {
				return true;
			} else {
				return false;
			}
		} catch (NameNotFoundException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 接收到的广播
	 */
	BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				logg("mReceiver...action=" + action);
				if (action.equals(Intent.ACTION_PACKAGE_ADDED)) {
					Message msg = new Message();
					msg.what = INSTALLED_SUCCESS;
					mHandler.sendMessage(msg);
				} else if (action.equals(Intent.ACTION_PACKAGE_CHANGED)) {

				} else if (action.equals(Intent.ACTION_PACKAGE_REMOVED)) {
					Message msg = new Message();
					msg.what = UNINSTALL_SUCCESS;
					mHandler.sendMessage(msg);
				} else if (action.equals(Intent.ACTION_PACKAGE_REPLACED)) {

				} else if (action.equals(Intent.ACTION_PACKAGE_DATA_CLEARED)) {

				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
}
