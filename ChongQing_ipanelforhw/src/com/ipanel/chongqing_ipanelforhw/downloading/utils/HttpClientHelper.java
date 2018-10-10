package com.ipanel.chongqing_ipanelforhw.downloading.utils;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.util.Log;

/**
 * �����࣬Ϊ����Ӧ�ó����ṩΨһ��һ��HttpClient����
 * ���������һЩ��ʼ���������������ԣ���Щ���Կ��Ա�HttpGet��HttpPost�����Ը���
 * 
 * @author zhoulc
 * 
 */
public class HttpClientHelper {

	private static HttpClient httpClient;

	private HttpClientHelper() {
	}

	public static String TAG = "HttpClientHelper";

	public static synchronized HttpClient getHttpClient() {

		if (null == httpClient) {
			// ��ʼ������
			try {
				Log.d(TAG, "001 ; getHttpClient()");
				KeyStore trustStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				Log.d(TAG, "002 ; getHttpClient()");
				trustStore.load(null, null);
				Log.d(TAG, "003 ; getHttpClient()");
				SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
				Log.d(TAG, "004 ; getHttpClient()");
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				Log.d(TAG, "005 ; getHttpClient()");
				HttpParams params = new BasicHttpParams();

				Log.d(TAG, "006 ; getHttpClient()");
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				Log.d(TAG, "007 ; getHttpClient()");
				HttpProtocolParams.setContentCharset(params,
						HTTP.DEFAULT_CONTENT_CHARSET);
				Log.d(TAG, "008 ; getHttpClient()");
				HttpProtocolParams.setUseExpectContinue(params, true);

				Log.d(TAG, "009 ; getHttpClient()");
				// �������ӹ������ĳ�ʱ
				ConnManagerParams.setTimeout(params, 30000);
				Log.d(TAG, "010 ; getHttpClient()");
				// �������ӳ�ʱ
				HttpConnectionParams.setConnectionTimeout(params, 30000);
				Log.d(TAG, "011 ; getHttpClient()");
				// ����socket��ʱ
				HttpConnectionParams.setSoTimeout(params, 30000);

				Log.d(TAG, "012 ; getHttpClient()");
				// ����http https֧��
				SchemeRegistry schReg = new SchemeRegistry();
				Log.d(TAG, "013 ; getHttpClient()");
				schReg.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				Log.d(TAG, "014 ; getHttpClient()");
				schReg.register(new Scheme("https", sf, 443));

				Log.d(TAG, "015 ; getHttpClient()");
				ClientConnectionManager conManager = new ThreadSafeClientConnManager(
						params, schReg);

				Log.d(TAG, "016 ; getHttpClient()");
				httpClient = new DefaultHttpClient(conManager, params);
			} catch (Exception e) {
				Log.d(TAG, "017 ; getHttpClient() ; throw exception!!!!");
				e.printStackTrace();
				return new DefaultHttpClient();
			}
		}
		return httpClient;
	}

	public static synchronized HttpClient getHttpClientFromOld() {

		if (null == httpClient) {
			// ��ʼ������
			try {
				Log.d(TAG, "001 ; getHttpClientFromOld()");
				KeyStore trustStore = KeyStore.getInstance(KeyStore
						.getDefaultType());
				Log.d(TAG, "002 ; getHttpClientFromOld()");
				trustStore.load(null, null);
				Log.d(TAG, "003 ; getHttpClientFromOld()");
				SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
				Log.d(TAG, "004 ; getHttpClientFromOld()");
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

				Log.d(TAG, "005 ; getHttpClientFromOld()");
				HttpParams params = new BasicHttpParams();

				Log.d(TAG, "006 ; getHttpClientFromOld()");
				HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
				Log.d(TAG, "007 ; getHttpClientFromOld()");
				HttpProtocolParams.setContentCharset(params,
						HTTP.DEFAULT_CONTENT_CHARSET);
				Log.d(TAG, "008 ; getHttpClientFromOld()");
				HttpProtocolParams.setUseExpectContinue(params, true);

				Log.d(TAG, "009 ; getHttpClientFromOld()");
				// �������ӹ������ĳ�ʱ
				ConnManagerParams.setTimeout(params, 10000);
				Log.d(TAG, "010 ; getHttpClientFromOld()");
				// �������ӳ�ʱ
				HttpConnectionParams.setConnectionTimeout(params, 10000);
				Log.d(TAG, "011 ; getHttpClientFromOld()");
				// ����socket��ʱ
				HttpConnectionParams.setSoTimeout(params, 10000);

				Log.d(TAG, "012 ; getHttpClientFromOld()");
				// ����http https֧��
				SchemeRegistry schReg = new SchemeRegistry();
				Log.d(TAG, "013 ; getHttpClientFromOld()");
				schReg.register(new Scheme("http", PlainSocketFactory
						.getSocketFactory(), 80));
				Log.d(TAG, "014 ; getHttpClientFromOld()");
				schReg.register(new Scheme("https", sf, 443));

				Log.d(TAG, "015 ; getHttpClientFromOld()");
				ClientConnectionManager conManager = new ThreadSafeClientConnManager(
						params, schReg);

				Log.d(TAG, "016 ; getHttpClientFromOld()");
				httpClient = new DefaultHttpClient(conManager, params);
			} catch (Exception e) {
				Log.d(TAG,
						"017  ; getHttpClientFromOld() ; throw exception!!!!");
				e.printStackTrace();
				return new DefaultHttpClient();
			}
		}
		return httpClient;
	}

}

class SSLSocketFactoryEx extends SSLSocketFactory {

	SSLContext sslContext = SSLContext.getInstance("TLS");

	public SSLSocketFactoryEx(KeyStore truststore)
			throws NoSuchAlgorithmException, KeyManagementException,
			KeyStoreException, UnrecoverableKeyException {
		super(truststore);

		TrustManager tm = new X509TrustManager() {

			@Override
			public java.security.cert.X509Certificate[] getAcceptedIssuers() {
				return null;
			}

			@Override
			public void checkClientTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {

			}

			@Override
			public void checkServerTrusted(
					java.security.cert.X509Certificate[] chain, String authType)
					throws java.security.cert.CertificateException {

			}
		};

		sslContext.init(null, new TrustManager[] { tm }, null);
	}

	@Override
	public Socket createSocket(Socket socket, String host, int port,
			boolean autoClose) throws IOException, UnknownHostException {
		return sslContext.getSocketFactory().createSocket(socket, host, port,
				autoClose);
	}

	@Override
	public Socket createSocket() throws IOException {
		return sslContext.getSocketFactory().createSocket();
	}
}
