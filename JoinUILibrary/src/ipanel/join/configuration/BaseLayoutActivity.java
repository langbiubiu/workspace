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
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalFocusChangeListener;
import cn.ipanel.android.Logger;
import cn.ipanel.android.net.cache.JSONApiHelper;
import cn.ipanel.android.net.cache.JSONApiHelper.CallbackType;
import cn.ipanel.android.net.cache.JSONApiHelper.StringResponseListener;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;
import cn.ipanel.android.widget.IFrameIndicator;
import cn.ipanel.android.widget.ViewFrameIndicator;
import dalvik.system.DexClassLoader;

public abstract class BaseLayoutActivity extends FragmentActivity implements
		OnGlobalFocusChangeListener, GlobalFocusFrameListener {

	private static final String PREF_CONF_VER = "_conf_ver";
	private String mCurrentConfigUrl;
	private Configuration mConfiguration;

	private volatile boolean mPaused = false;
	private boolean mLoadConfigWhenResume = false;

	private long lastUpdateTime;

	protected long updateInterval = 60 * 60 * 1000l;

	BroadcastReceiver netStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			Logger.d(intent.getAction() + ": isConnected=" + isConnected());
			if (isConnected()) {
				String url = getIntent().getDataString();
				if (url == null)
					url = getDefaultUrl();
				mCurrentConfigUrl = null;
				loadConfig(url);
			} else if (mCurrentConfigUrl != null
					&& !mCurrentConfigUrl.equals(getFallbackUrl())) {
				loadConfig(getFallbackUrl());
			}
		}

	};

	public boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null
				&& cm.getActiveNetworkInfo().isConnected();
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
		if (SystemClock.elapsedRealtime() - lastUpdateTime > updateInterval)
			mCurrentConfigUrl = null;
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
		if (url != null && !url.equals(getFallbackUrl())
				&& ConfigState.getInstance().getPendingConfiguration() != null) {
			Logger.d("show pending configuration");
			mCurrentConfigUrl = url;
			checkAndClearCache(url, ConfigState.getInstance()
					.getPendingConfiguration());
			showContent(ConfigState.getInstance().getPendingConfiguration());
			ConfigState.getInstance().setPendingConfiguration(null);
			return;
		}
		if (url == null || url.equals(mCurrentConfigUrl)) {
			if (mLoadConfigWhenResume && mConfiguration != null)
				showContent(mConfiguration);
			return;
		}

		final String furl = url;
		Logger.d("loadConfig " + url);
		if (mCurrentConfigUrl == null && url.startsWith("http://")
				&& !isConnected() && !url.equals(getFallbackUrl())) {
			loadConfig(getFallbackUrl());
			return;
		}
		JSONApiHelper.callJSONAPI(this, CallbackType.NoCache, furl, null,
				new StringResponseListener() {

					@Override
					public void onResponse(String content) {
						Logger.d("config content\n" + content);
						if (content != null) {
							try {
								parseConfig(content, furl);
								if (!furl.equals(getFallbackUrl()))
									lastUpdateTime = SystemClock
											.elapsedRealtime();
							} catch (Exception e) {
								e.printStackTrace();

								if (getFallbackUrl() != null
										&& !furl.equals(getFallbackUrl())) {

									loadConfig(getFallbackUrl());
								}
							}
						} else {

							if (getFallbackUrl() != null
									&& !furl.equals(getFallbackUrl())) {

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
		if (v != null) {
			if (v instanceof IConfigView) {
				hideFrame = !((IConfigView) v).showFocusFrame();
			}
			mFocusFrame.moveFrameTo(v, true, hideFrame);
		}
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
		if (mConfiguration != null && mConfiguration.getVersion() != null
				&& mConfiguration.getVersion().equals(conf.getVersion()))
			return;
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
		ConfigState.getInstance().setConfiguration(conf);

		View root = ViewInflater.inflateView(this, null, conf.getScreen()
				.get(0).getView());
		root.getViewTreeObserver().addOnGlobalFocusChangeListener(this);
		if (mFocusFrame != null) {
			ViewGroup vg = (ViewGroup) findViewById(android.R.id.content);
			vg.removeView(mFocusFrame.getImageView());
			mFocusFrame = null;
		}
		setContentView(root);
		root.requestFocus();

		if (mFocusFrame == null) {
			Logger.d("create frame indicator");
			mFocusFrame = createFrameIndicator();
		}
		mFocusFrame.getImageView().setBackgroundDrawable(getFocusDrawable());

		View v = getCurrentFocus();
		moveFocusFrameTo(v);

		mLoadConfigWhenResume = false;

		if (mCurrentConfigUrl != null
				&& !mCurrentConfigUrl.equals(getFallbackUrl()))
			getPreferences(0).edit()
					.putString(PREF_CONF_VER, mConfiguration.getVersion())
					.commit();

		afterSetContent();
	}

	protected void setFocus() {				
		if (mFocusFrame == null) {
			Logger.d("create frame indicator");
			mFocusFrame = createFrameIndicator();
		}
		mFocusFrame.getImageView().setBackgroundDrawable(getFocusDrawable());	
		View v = getCurrentFocus();
		moveFocusFrameTo(v);
	}

	protected IFrameIndicator createFrameIndicator() {
		return new ViewFrameIndicator(this);
	}

}