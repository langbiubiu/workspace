package cn.ipanel.android.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.http.HeaderElement;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;

public class HttpUtils {
	public static String getCharSet(URLConnection connection) throws IOException {
		String contentType = connection.getContentType();
		if (contentType != null) {
			HeaderValueParser parser = new BasicHeaderValueParser();
			HeaderElement[] values = BasicHeaderValueParser.parseElements(contentType, parser);
			if (values.length > 0) {
				NameValuePair param = values[0].getParameterByName("charset");
				if (param != null) {
					return param.getValue();
				}
			}
		}
		if (connection instanceof HttpURLConnection) {
			return "UTF-8";
		} else {
			throw new IOException("Unabled to determine character encoding");
		}
	}
	
	/**
	 * 
	 * @param host
	 * @param timeOut in milliseconds
	 * @return
	 */
	public static boolean checkDNSResolve(String host, int timeOut){
		Future<InetAddress> future = sPool.submit(new DNSChecker(host));
		try {
			return future.get(timeOut, TimeUnit.MILLISECONDS) != null;
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			future.cancel(true);
		}
		return false;
	}
	
	private static ExecutorService sPool = Executors.newCachedThreadPool();
	
	public static class DNSChecker implements Callable<InetAddress>{
		private String host;
		
		public DNSChecker(String host ){
			this.host = host;
		}

		@Override
		public InetAddress call() throws Exception {
			try{
				return InetAddress.getByName(host);
			}catch(UnknownHostException e){
				e.printStackTrace();
			}
			return null;
		}
		
	}
}
