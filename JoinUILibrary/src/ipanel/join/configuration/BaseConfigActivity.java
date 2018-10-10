package ipanel.join.configuration;

import ipanel.join.configuration.ConfigState.GlobalFocusFrameListener;
import ipanel.join.configuration.ExtJarLoader.ExtJarLoadListener;
import ipanel.join.widget.IConfigView;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import cn.ipanel.android.Logger;
import cn.ipanel.android.net.cache.JSONApiHelper;
import cn.ipanel.android.net.cache.JSONApiHelper.CallbackType;
import cn.ipanel.android.net.cache.JSONApiHelper.StringResponseListenerExt;
import cn.ipanel.android.net.cache.JSONApiHelper.StringResponseListener;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.util.DebugUtils;
import cn.ipanel.android.widget.IFrameIndicator;
import cn.ipanel.android.widget.ViewFrameIndicator;
import dalvik.system.DexClassLoader;

/**
 * Base activity that handles config loading automatically, also provides
 * support for focus frame
 * 
 * @author Zexu
 * 
 */
public abstract class BaseConfigActivity extends FragmentActivity implements
		OnGlobalFocusChangeListener, GlobalFocusFrameListener {

	private static final String PREF_CONF_VER = "_conf_ver";
	private String mCurrentConfigUrl;
	private Configuration mConfiguration;
	private boolean isCachedConfig = false;

	private volatile boolean mPaused = false;
	private boolean mLoadConfigWhenResume = false;

	private long lastUpdateTime;

	protected long updateInterval = 5 * 60 * 1000l;
	
	static final int MSG_LOAD_FALLBACK_CONFIG = 100;
	static final int MSG_CACHE_ALL_IMAGES = 200;
	static final int MSG_UPDATE_CONFIG = 500;
	
	Handler uiHandler = new Handler(Looper.getMainLooper()){

		@Override
		public void handleMessage(Message msg) {
			switch(msg.what){
			case MSG_LOAD_FALLBACK_CONFIG:
				if (shouldLoadFallbackConfig(mCurrentConfigUrl)) {
					loadConfig(getFallbackUrl());
				}
				break;
			case MSG_CACHE_ALL_IMAGES:
				Logger.d("cache all images, mConfiguration = "+mConfiguration);
				if(mConfiguration != null){
					ImageFetcher mFetcher = ConfigState.getInstance().getImageFetcher(getApplicationContext());
					
					//try to cache all image resources
					for(String url : mConfiguration.getAllImages()){
						//Logger.d("try to cache url="+url);
						mFetcher.loadImage(url, null);
					}
				}
				break;
			case MSG_UPDATE_CONFIG:
				mCurrentConfigUrl = null;
				String url = getIntent().getDataString();
				if (url == null)
					url = getDefaultUrl();

				loadConfig(url);
				break;
			}
		}
		
	};

	BroadcastReceiver netStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.d(intent.getAction() + ": isConnected=" + isConnected());
			uiHandler.removeMessages(MSG_LOAD_FALLBACK_CONFIG);
			if (isConnected()) {
				String url = getIntent().getDataString();
				if (url == null)
					url = getDefaultUrl();
				if (!mLoadConfigWhenResume
						&& SystemClock.elapsedRealtime() - lastUpdateTime > updateInterval) {
					mCurrentConfigUrl = null;
				}
				loadConfig(url);
			} else {
				uiHandler.sendEmptyMessageDelayed(MSG_LOAD_FALLBACK_CONFIG, 10000);
			}
		}

	};

	public boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		if(cm == null)
			return false;
		NetworkInfo info = cm.getActiveNetworkInfo();
		if(info == null)
			return false;
		return info.isConnected();
	}

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		IntentFilter filter = new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(netStateReceiver, filter);
	}

	@Override
	protected void onDestroy() {
		unregisterReceiver(netStateReceiver);
		super.onDestroy();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mPaused = false;
		ConfigState.getInstance().setFrameListener(this);
		ConfigState.getInstance().setConfiguration(mConfiguration);
		if (!mLoadConfigWhenResume
				&& SystemClock.elapsedRealtime() - lastUpdateTime > updateInterval){
			mCurrentConfigUrl = null;
		}
		String url = getIntent().getDataString();
		if (url == null)
			url = getDefaultUrl();

		loadConfig(url);
	}

	public View findViewByConfigId(String id) {
		return Utils.findViewByConfigId(getWindow().getDecorView(), id);
	}

	@Override
	protected void onPause() {
		super.onPause();
		mPaused = true;
	}

	protected void loadConfig(String url) {
		Logger.d("loadConfig loadWhenResume=" + mLoadConfigWhenResume + ", current url="
				+ mCurrentConfigUrl);
		uiHandler.removeMessages(MSG_LOAD_FALLBACK_CONFIG);
		uiHandler.removeMessages(MSG_CACHE_ALL_IMAGES);
		if (url != null && !url.equals(getFallbackUrl())
				&& ConfigState.getInstance().getPendingConfiguration() != null) {
			Logger.d("show pending configuration");
			mCurrentConfigUrl = url;
			checkAndClearCache(url, ConfigState.getInstance().getPendingConfiguration());
			showContent(ConfigState.getInstance().getPendingConfiguration());
			ConfigState.getInstance().setPendingConfiguration(null);
			return;
		}
		if (url == null || url.equals(mCurrentConfigUrl)) {
			if (mLoadConfigWhenResume && mConfiguration != null)
				showContent(mConfiguration);
			return;
		}

		uiHandler.removeMessages(MSG_UPDATE_CONFIG);
		final String furl = url;
		Logger.d("loadConfig " + url);
//		if (mCurrentConfigUrl == null && url.startsWith("http://")
//				&& !isConnected() && shouldLoadFallbackConfig(url)) {
//			loadConfig(getFallbackUrl());
//			return;
//		}
		Logger.d("loadConfig start to callJSONAPI  url = " + url + ", allowConfigCache="
				+ allowConfigCache());
		JSONApiHelper.callJSONAPI(this, allowConfigCache() ? CallbackType.ForceUpdate
				: CallbackType.NoCache, furl, null, new StringResponseListenerExt() {
			public void onResponse(String content) {

			}

			@Override
			public void onResponse(String content, boolean isCacheData) {
				Logger.d("isCacheData=" + isCacheData + ", config content:\n" + content);
				if (content != null) {
					try {
						isCachedConfig = isCacheData;
						parseConfig(content, furl);
						if (!furl.equals(getFallbackUrl()))
							lastUpdateTime = SystemClock.elapsedRealtime();
					} catch (Exception e) {
						e.printStackTrace();

						if (mLoadConfigWhenResume && mConfiguration != null) {
							showContent(mConfiguration);
							return;
						}
						if ((mConfiguration == null && getFallbackUrl() != null && !furl
								.equals(getFallbackUrl())) || shouldLoadFallbackConfig(furl)) {

							loadConfig(getFallbackUrl());
						}
					}
				} else {
					if (mLoadConfigWhenResume && mConfiguration != null) {
						showContent(mConfiguration);
						return;
					}
					if ((mConfiguration == null && getFallbackUrl() != null && !furl
							.equals(getFallbackUrl())) || shouldLoadFallbackConfig(furl)) {

						loadConfig(getFallbackUrl());
					}
				}

			}
		});

	}

	IFrameIndicator mFocusFrame;

	@Override
	public void onGlobalFocusChanged(View oldFocus, View newFocus) {
		moveFocusFrameTo(newFocus);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			View v = getCurrentFocus();
			moveFocusFrameTo(v);
		}
	}

	protected void moveFocusFrameTo(View v) {
		if (mFreezeFrame || mFocusFrame == null)
			return;

		boolean hideFrame = false;
		if (v instanceof IConfigView) {
			hideFrame = !((IConfigView) v).showFocusFrame();
		}
		mFocusFrame.moveFrameTo(v, true, hideFrame);
	}

	private boolean mFreezeFrame = false;

	@Override
	public void freezeFrame() {
		mFreezeFrame = true;
		if (mFocusFrame != null)
			mFocusFrame.hideFrame();
	}

	@Override
	public void updateFrame() {
		mFreezeFrame = false;
		View v = getCurrentFocus();
		moveFocusFrameTo(v);
	}

	@Override
	public void setScaleAnimationSize(float x, float y) {
		if (mFocusFrame != null) {
			mFocusFrame.setScaleAnimationSize(x, y);
		}
	}

	/**
	 * 
	 * @return default config url
	 */
	protected abstract String getDefaultUrl();

	/**
	 * called if cannot get configuration from default url
	 * 
	 * @return configuration url
	 */
	protected String getFallbackUrl() {
		return null;
	}
	
	/**
	 * Whether to cache configuration file locally 
	 * @return
	 */
	protected boolean allowConfigCache(){
		return true;
	}

	/**
	 * 
	 * @return Drawable for the focus image
	 */
	protected abstract Drawable getFocusDrawable();

	/**
	 * called after configuration content is retrieved and set
	 */
	protected void afterSetContent() {

	}

	/**
	 * called after configuration set before content set
	 */
	protected void afterSetConfiguration() {

	}

	protected void parseConfig(String config, String url)
			throws XmlPullParserException, IOException {

		ByteArrayInputStream is = new ByteArrayInputStream(config.getBytes());
		final Configuration conf = ConfigParser.sParser.parse(is);
		if(conf != null)
			conf.calculateScale(this);
		Logger.d("mConfiguration " + mCurrentConfigUrl);
		conf.findAllImages(this);
		Logger.d("mConfiguration>> " + mConfiguration);
		if (!mLoadConfigWhenResume && mConfiguration != null && mConfiguration.getVersion() != null
				&& mConfiguration.getVersion().equals(conf.getVersion())){
			Logger.d("config not changed, skip. version = " +conf.getVersion());
			uiHandler.sendEmptyMessageDelayed(MSG_UPDATE_CONFIG, updateInterval);
			return;
		}
		mCurrentConfigUrl = url;

		checkAndClearCache(url, conf);

		if (conf.getExtJar() != null) {
			ExtJarLoader.loadExtJar(this, conf.getExtJar(),
					new ExtJarLoadListener() {

						@Override
						public void onLoaded(DexClassLoader classLoader) {
							ConfigState.getInstance().setClassLoader(
									classLoader);
							loadResources(conf);
						}
					});
		} else {
			loadResources(conf);
		}
	}

	protected void checkAndClearCache(String url, final Configuration conf) {
		if (!url.equals(getFallbackUrl())
				&& !getPreferences(0).getString(PREF_CONF_VER, "").equals(
						conf.getVersion())) {

			SharedImageFetcher.clearDiskCache(getApplicationContext());
			SharedImageFetcher.clearMemoryCache();
		}
	}

	protected void loadResources(final Configuration configuration) {
		if (TextUtils.isEmpty(configuration.getExtRes())) {
			showContent(configuration);
		} else {
			JSONApiHelper.callJSONAPI(this, CallbackType.ForceUpdate,
					configuration.getExtRes(), null,
					new StringResponseListener() {

						@Override
						public void onResponse(String content) {
							if (content != null) {
								ResManager resManager = ResManager
										.createManager(content);
								resManager.replaceStringRefs(configuration);
								showContent(configuration);
							}

						}
					});
		}
	}

	/**
	 * By default, if activity is paused, content will be set on next resume,
	 * override this if you want content to load even after onPause. Note: some
	 * fragment transaction is not allowed after onPause
	 * 
	 * @return
	 */
	protected boolean canShowContent() {
		return !mPaused;
	}

	protected synchronized void showContent(Configuration conf) {
		mConfiguration = conf;
		afterSetConfiguration();
		if (!canShowContent()) {
			mLoadConfigWhenResume = true;
			return;
		}
		Logger.d("show config content for " + mCurrentConfigUrl);
		
		//clear old content, so it's resource can be GC
		//setContentView(new View(this));
		//stop and shutdown current fetcher
		Logger.d("shutdown current image fetcher");
		ConfigState.getInstance().resetImageFetcher(this);
		//clear cache
		Logger.d("shutdown image fetcher memory cache");
		SharedImageFetcher.clearMemoryCache();
		
		ConfigState.getInstance().setConfiguration(conf);
		View root = ViewInflater.inflateView(this, null, conf.getScreen()
				.get(0).getView());
		root.getViewTreeObserver().addOnGlobalFocusChangeListener(this);
		ViewGroup vg = (ViewGroup) findViewById(android.R.id.content);
		if (mFocusFrame != null) {
			mFocusFrame.hideFrame();
			if (mFocusFrame.getImageView() != null) {
				vg.removeView(mFocusFrame.getImageView());
			}
			DebugUtils.printAllChildren("[BEFORE CONF]", vg, false);
			mFocusFrame = null;
		}
		setContentView(root);
		root.requestFocus();

		if (mFocusFrame == null) {
			Logger.d("create frame indicator");
			mFocusFrame = createFrameIndicator();
		}
		mFocusFrame.getImageView().setBackgroundDrawable(getFocusDrawable());
		DebugUtils.printAllChildren("[AFTER CONF]", vg, false);

		View v = getCurrentFocus();
		moveFocusFrameTo(v);
		mLoadConfigWhenResume = false;

		if (mCurrentConfigUrl != null
				&& !mCurrentConfigUrl.equals(getFallbackUrl()))
			getPreferences(0).edit()
					.putString(PREF_CONF_VER, mConfiguration.getVersion())
					.commit();

		afterSetContent();
		
		if (mCurrentConfigUrl.startsWith("http://") && (!isConnected() || isCachedConfig)
				&& shouldLoadFallbackConfig(mCurrentConfigUrl)) {
			Logger.d("No connection, check if current cache is valid in 10 seconds, isCachedConfig="+isCachedConfig);
			uiHandler.sendEmptyMessageDelayed(MSG_LOAD_FALLBACK_CONFIG, 1000);
		} else {
			//try to update all images in 30 seconds
			Logger.d("Try to cache all images in 30 seconds");
			uiHandler.sendEmptyMessageDelayed(MSG_CACHE_ALL_IMAGES, 30000);
		}
		uiHandler.removeMessages(MSG_UPDATE_CONFIG);
		uiHandler.sendEmptyMessageDelayed(MSG_UPDATE_CONFIG, updateInterval);
	}

	protected IFrameIndicator createFrameIndicator() {
		return new ViewFrameIndicator(this);
	}

	
	protected IFrameIndicator getFrameIndicator() {
		return mFocusFrame;
	}

	protected boolean shouldLoadFallbackConfig(final String furl) {
		return getFallbackUrl() != null && !getFallbackUrl().equals(furl)
				&& (mConfiguration != null && !mConfiguration.isAllImageCached(this));
	}
}
