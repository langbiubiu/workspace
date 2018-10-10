package ipanel.join.configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.content.Intent;
import cn.ipanel.android.net.imgcache.ImageFetcher;
import cn.ipanel.android.net.imgcache.SharedImageFetcher;

public class ConfigState {
	private Configuration configuration;
	
	private Configuration pendingConfiguration;
	
	public interface GlobalFocusFrameListener{
		public void freezeFrame();
		public void updateFrame();
		public void setScaleAnimationSize(float x, float y);
	}
	
	public interface ExceptionListener{
		public void onException(Exception e);
	}
	
	public interface GlobalIntentIntercepter{
		public boolean handleIntent(Intent intent);
	}
	
	private GlobalIntentIntercepter mGlobalIntentIntercepter;
	
	private ExceptionListener mExceptionListener;
	public void setExceptionListener(ExceptionListener l){
		mExceptionListener = l;
	}
	
	public void setGlobalIntentIntercepter(GlobalIntentIntercepter intentIntercepter){
		mGlobalIntentIntercepter = intentIntercepter;
	}
	
	public GlobalIntentIntercepter getGlobalIntentIntercepter(){
		return mGlobalIntentIntercepter;
	}
	
	public void notifyException(Exception e){
		if(mExceptionListener != null)
			mExceptionListener.onException(e);
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}
	
	private static ConfigState sState = new ConfigState();
	
	public static ConfigState getInstance(){
		return sState;
	}
	
	private ConfigState(){}
	
	private ImageFetcher mFetcher;
	
	private ClassLoader mClassLoader;
	
	public synchronized ImageFetcher getImageFetcher(Context ctx){
		if(mFetcher == null){
			ExecutorService executor = Executors.newFixedThreadPool(3);
			mFetcher = SharedImageFetcher.createFetcher(ctx, executor);
			mFetcher.setImageSize(ctx.getResources().getDisplayMetrics().widthPixels);
		}
		return mFetcher;
	}
	
	public synchronized void resetImageFetcher(Context ctx){
		if(mFetcher != null){
			mFetcher.stopFetcher();
			mFetcher = null;
		}
	}
	
	public ClassLoader getClassLoader() {
		return mClassLoader;
	}
	
	public void setClassLoader(ClassLoader classLoader){
		this.mClassLoader = classLoader;
	}

	public GlobalFocusFrameListener getFrameListener() {
		return mFrameListener;
	}

	public void setFrameListener(GlobalFocusFrameListener mFrameListener) {
		this.mFrameListener = mFrameListener;
	}

	public Configuration getPendingConfiguration() {
		return pendingConfiguration;
	}

	public void setPendingConfiguration(Configuration pendingConfiguration) {
		this.pendingConfiguration = pendingConfiguration;
	}

	private GlobalFocusFrameListener mFrameListener;
	
}
