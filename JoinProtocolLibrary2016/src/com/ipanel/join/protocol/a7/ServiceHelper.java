package com.ipanel.join.protocol.a7;

import java.io.ByteArrayOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;

import com.google.gson.Gson;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import cn.ipanel.android.Logger;
import cn.ipanel.android.net.http.AsyncHttpClient;
import cn.ipanel.android.net.http.AsyncHttpResponseHandler;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.util.HttpUtils;
import cn.ipanel.android.util.IOUtils;

public class ServiceHelper {
	private static final String TAG = "ServiceHelper";

	public enum SerializerType {
		XML, JSON,TEXT
	}
	
	/**
	 * @deprecated  replaced by generic version
	 */
	@Deprecated 
	public interface ResponseHandler {
		public void onResponse(boolean success, Object result);
	}
	
	public interface ResponseHandlerT<T>{
		public void onResponse(boolean success, T result);
	}
	
	public interface GlobalResponseIntercepter{
		public boolean onResponse(boolean success, Object obj);
	}
	private static GlobalResponseIntercepter sGlobalListener;
	
	public static void setGlobalResponseListener(GlobalResponseIntercepter l){
		sGlobalListener = l;
	}
	
	private static ExecutorService sPool = Executors.newCachedThreadPool();

	AsyncHttpClient mClient = new AsyncHttpClient();

	private static final ServiceHelper sHelper = new ServiceHelper();

	public static ServiceHelper getHelper() {
		return sHelper;
	}
	
	public static ServiceHelper createOneHelper() {
		return new ServiceHelper();
	}
	private ServiceHelper(){
		mPool = sPool;
		mClient.getHttpClient().getParams().setParameter(ClientPNames.ALLOW_CIRCULAR_REDIRECTS, true);
	}
	
	@Override
	protected void finalize() throws Throwable {
		if(mPool != null && mPool != sPool){
			mPool.shutdown();
			mPool = null;
		}
		super.finalize();
	}

	public String getRootUrl() {
		return rootUrl;
	}

	public void setRootUrl(String rootUrl) {
		this.rootUrl = rootUrl;
	}

	private String rootUrl;

	private String contentType = "text/xml";

	private Header[] headers;

	private String defaultEncoding ="UTF-8";

	SerializerType serializerType = SerializerType.XML;

	Gson gson = new Gson();

	private Serializer serializer = new Persister(new Format(
			"<?xml version=\"1.0\" encoding= \"UTF-8\" ?>"));
	
	private ExecutorService mPool;
	
	private Handler uiHandler = new Handler(Looper.getMainLooper());

	public void setXMLSerializer(Serializer serializer){
		this.serializer = serializer;
	}
	
	public void cancelAllTasks(){
		setThreadPool(Executors.newFixedThreadPool(3));
	}
	
	public void setThreadPool(ExecutorService service){
		if(mPool != null && mPool != sPool)
			mPool.shutdownNow();
		this.mPool = service;
	}
	
	public SerializerType getSerializerType() {
		return serializerType;
	}

	public void setSerializerType(SerializerType serializerType) {
		this.serializerType = serializerType;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public Header[] getHeaders() {
		return headers;
	}

	public void setHeaders(Header[] headers) {
		this.headers = headers;
	}

	public void setDefaultEncoding(String encoding) {
		this.defaultEncoding = encoding;
	}
	
	public <E> Future<E> callServiceAsync(Context ctx, final RequestParams params, final Class<E> type, final ResponseHandlerT<E> handler){
		if (getRootUrl().startsWith("asset://")) {
			return callServiceAsyncTest(ctx, null, type, handler);
		}
		final String url = rootUrl;
		final Header[] tmpHeaders = headers;
		final String tmpEnc = defaultEncoding;
		final SerializerType tmpSerializerType = serializerType;
		
		return mPool.submit(new Callable<E>() {

			@SuppressWarnings("unchecked")
			@Override
			public E call() throws Exception {
				final String urlWithQueryString = AsyncHttpClient.getUrlWithQueryString(url, params);
				LogcatUtils.splitAndLog(TAG,
						"request url = " + urlWithQueryString);
				//String content = mClient.syncGet(url, params, tmpHeaders, tmpEnc);
				String content = HttpUtils.syncGet(urlWithQueryString, tmpHeaders, tmpEnc);
				LogcatUtils.splitAndLog(TAG,"response " + content);
				try {
					final E result;
					if (tmpSerializerType == SerializerType.XML)
						result = serializer.read(type, content);
					else if(tmpSerializerType == SerializerType.JSON){
						result = gson.fromJson(content, type);
					} else {
						result = ((E) content);
					}
					if (handler != null)
						uiHandler.post(new Runnable() {
							@Override
							public void run() {
								try{
									if(sGlobalListener != null && sGlobalListener.onResponse(true, result))
										return;
									handler.onResponse(true, result);
								}catch(Exception e){
									LogcatUtils.splitAndLog(TAG, "exception for url="+urlWithQueryString);
									e.printStackTrace();
								}
							}
						});
					return result;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (handler != null)
					uiHandler.post(new Runnable() {
						@Override
						public void run() {
							try{
								handler.onResponse(false, null);
							}catch(Exception e){
								LogcatUtils.splitAndLog(TAG, "exception for url="+urlWithQueryString);
								e.printStackTrace();
							}
						}
					});
				return null;
			}
		});
	}

	/**
	 * @deprecated  replaced by generic version
	 */
	@Deprecated 	
	public void callServiceAsync(Context ctx, RequestParams params, final Class<?> type,
			final ResponseHandler handler) {
		if (getRootUrl().startsWith("asset://")) {
			callServiceAsyncTest(ctx, null, type, handler);
			return;
		}

		mClient.get(ctx, rootUrl, headers, params, new AsyncHttpResponseHandler() {

			@Override
			public String getDefaultEncoding() {
				if (defaultEncoding != null)
					return defaultEncoding;
				return super.getDefaultEncoding();
			}

			@Override
			public void onSuccess(int statusCode, String content) {
				Logger.d("response " + statusCode + ", " + content);
				try {
					Object result;
					if (serializerType == SerializerType.XML)
						result = serializer.read(type, content);
					else if(serializerType == SerializerType.JSON){
						result = gson.fromJson(content, type);
					}else{
						result=content;
					}
					if (handler != null)
						handler.onResponse(true, result);
					return;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (handler != null)
					handler.onResponse(false, null);

			}

			@Override
			public void onFailure(Throwable error, String content) {
				Logger.d("onFailure " + error + ", " + content);
				if (error != null)
					error.printStackTrace();
				if (handler != null)
					handler.onResponse(false, null);
			}

		});
	}
	
	/**
	 * @deprecated  replaced by generic version
	 */
	@Deprecated 
	public void callServiceAsync(Context ctx, Object request, final Class<?> type,
			final ResponseHandler handler) {
		if (getRootUrl().startsWith("asset://")) {
			callServiceAsyncTest(ctx, request, type, handler);
			return;
		}

		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		try {
			HttpEntity entity;
			if (serializerType == SerializerType.XML) {
				serializer.write(request, bao);
				entity = new ByteArrayEntity(bao.toByteArray());
			} else {
				entity = new StringEntity(gson.toJson(request));
			}
			Logger.d("post " + rootUrl);
			Logger.d(new String(bao.toByteArray()));
			mClient.post(ctx, rootUrl, headers, entity, contentType,
					new AsyncHttpResponseHandler() {

						@Override
						public String getDefaultEncoding() {
							if (defaultEncoding != null)
								return defaultEncoding;
							return super.getDefaultEncoding();
						}

						@Override
						public void onSuccess(int statusCode, String content) {
							Logger.d("response " + statusCode + ", " + content);
							try {
								Object result;
								if (serializerType == SerializerType.XML)
									result = serializer.read(type, content);
								else if(serializerType == SerializerType.JSON){
									result = gson.fromJson(content, type);
								}else{
									result=content;
								}
								if (handler != null)
									handler.onResponse(true, result);
								return;
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							if (handler != null)
								handler.onResponse(false, null);

						}

						@Override
						public void onFailure(Throwable error, String content) {
							Logger.d("onFailure " + error + ", " + content);
							if (error != null)
								error.printStackTrace();
							if (handler != null)
								handler.onResponse(false, null);
						}

					});
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	public <E> Future<E> callServiceAsync(Context ctx, final Object request, final Class<E> type,
			final ResponseHandlerT<E> handler) {
		if (getRootUrl().startsWith("asset://")) {
			return callServiceAsyncTest(ctx, request, type, handler);
		}
		LogcatUtils.splitAndLog("post url: ", "callServiceAsync 22 rootUrl " + rootUrl);
		final String url = rootUrl;
		final Header[] tmpHeaders = headers;
		final String tmpType = contentType;
		final String tmpEnc = defaultEncoding;
		final SerializerType tmpSerializerType = serializerType;
		
		return mPool.submit(new Callable<E>() {

			@SuppressWarnings("unchecked")
			@Override
			public E call() throws Exception {
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				try {
					HttpEntity entity;

					if(request instanceof String){
						entity = new StringEntity(request+"","UTF-8");
					}else{
						if (tmpSerializerType == SerializerType.XML) {
							serializer.write(request, bao);
							entity = new ByteArrayEntity(bao.toByteArray());
						} else {
							entity = new StringEntity(gson.toJson(request), "UTF-8");
						}
					}

					LogcatUtils.splitAndLog("post url: ", "post " + url);
					Logger.d(new String(bao.toByteArray()));
					LogcatUtils.splitAndLog("entity", "entity@: " + new String(IOUtils.IS2ByteArray(entity.getContent())));

//					String content = mClient.syncPost(url, tmpHeaders, entity, tmpType,
//							tmpEnc);
					String content = HttpUtils.syncPost(url, tmpHeaders, entity, tmpType, tmpEnc); 
					LogcatUtils.splitAndLog("ServiceHelper:", "response " + content);
					if (content != null) {
						try {
							final E result;
							if (type == String.class) {
								result = ((E) content);
							}else{
								if (tmpSerializerType == SerializerType.XML)
									result = serializer.read(type, content);
								else if (tmpSerializerType == SerializerType.JSON) {
									result = gson.fromJson(content, type);
								} else {
									result = ((E) content);
								}
							}
							if (handler != null)
								uiHandler.post(new Runnable() {

									@Override
									public void run() {
										try{
											if(sGlobalListener != null && sGlobalListener.onResponse(true, result))
												return;
											handler.onResponse(true, result);
										}catch(Exception e){
											LogcatUtils.splitAndLog(TAG, "exception for url=" + url);
											e.printStackTrace();
										}
									}
								});
							return result;
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (handler != null)
					uiHandler.post(new Runnable() {

						@Override
						public void run() {
							try{
								handler.onResponse(false, null);
							} catch (Exception e){
								LogcatUtils.splitAndLog(TAG, "exception for url=" + url);
								e.printStackTrace();
							}
						}
					});
				return null;
			}
		});
	}
	
	public <E> Future<E> callServiceAsyncTest(final Context ctx, final Object request, final Class<E> type,
			final ResponseHandlerT<E> handler) {
		return mPool.submit(new Callable<E>() {

			@SuppressWarnings("unchecked")
			@Override
			public E call() throws Exception {
				ByteArrayOutputStream bao = new ByteArrayOutputStream();
				try {
					if (request != null) {
						if (serializerType == SerializerType.XML) {
							serializer.write(request, bao);
							Logger.d("Post: " + new String(bao.toByteArray()));
						} else {
							Logger.d("Post: " + gson.toJson(request));
						}
					}
					Uri uri = Uri.parse(rootUrl);
					String content = IOUtils.loadAssetText(ctx, uri.getLastPathSegment());
					try {
						final E result;
						if (serializerType == SerializerType.XML)
							result = serializer.read(type, content);
						else if(serializerType == SerializerType.JSON){
							result = gson.fromJson(content, type);
						}else{
							result=((E) content);
						}
						if (handler != null)
							uiHandler.post(new Runnable() {
								
								@Override
								public void run() {
									handler.onResponse(true, result);
								}
							});
						return result;
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (handler != null)
					uiHandler.post(new Runnable() {
						
						@Override
						public void run() {
							handler.onResponse(false, null);
						}
					});
				return null;
			}
		});
	}

	public void callServiceAsyncTest(Context ctx, Object request, final Class<?> type,
			final ResponseHandler handler) {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		try {
			if (request != null) {
				if (serializerType == SerializerType.XML) {
					serializer.write(request, bao);
					Logger.d("Post: " + new String(bao.toByteArray()));
				} else {
					Logger.d("Post: " + gson.toJson(request));
				}
			}
			Uri uri = Uri.parse(rootUrl);
			String content = IOUtils.loadAssetText(ctx, uri.getLastPathSegment());
			try {
				Object result = null; 
				if (serializerType == SerializerType.XML)
					result = serializer.read(type, content);
				else if(serializerType == SerializerType.JSON){
					result = gson.fromJson(content, type);
				}else{
					result=content;
				}
				if (handler != null)
					handler.onResponse(true, result);
				return;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
