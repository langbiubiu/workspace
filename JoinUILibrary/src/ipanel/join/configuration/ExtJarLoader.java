package ipanel.join.configuration;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import cn.ipanel.android.Logger;

import dalvik.system.DexClassLoader;

import android.content.Context;
import android.os.Handler;

/**
 * Utility class for download and loading external jar
 * @author Zexu
 *
 */
public class ExtJarLoader {
	public interface ExtJarLoadListener {
		public void onLoaded(DexClassLoader classLoader);
	}

	/**
	 * 
	 * @param ctx application context
	 * @param url external jar url
	 * @param listener listener to be notified after jar loaded
	 */
	public static void loadExtJar(final Context ctx, final String url,
			final ExtJarLoadListener listener) {
		final File jarFile = new File(ctx.getDir("extJar", 0), "ext"
				+ url.hashCode() + ".jar");
		if (jarFile.exists() && jarFile.length() > 0) {
			if (listener != null) {
				listener.onLoaded(loadJar(ctx, jarFile));
			}
		} else {
			final Handler uiHandler = new Handler();
			new Thread() {
				public void run() {
					DexClassLoader classLoader = null;
					try {
						HttpURLConnection conn = (HttpURLConnection) new URL(
								url).openConnection();
						Logger.d("Connect to: " + conn.getURL().toString());
						conn.setConnectTimeout(5000);
						conn.connect();
						int resCode = conn.getResponseCode();
						Logger.d("Response "+resCode +" for "+conn.getURL().toString());
						if (resCode >= 200 && resCode < 300) {
							InputStream is = conn.getInputStream();
							OutputStream os = new FileOutputStream(jarFile);
							streamCopy(is, os);
							classLoader = loadJar(ctx, jarFile);
						}
					} catch (Exception e) {
						e.printStackTrace();
						jarFile.delete();
					}
					
					final DexClassLoader fLoader = classLoader;
					if (listener != null)
						uiHandler.post(new Runnable() {

							@Override
							public void run() {
								listener.onLoaded(fLoader);

							}
						});
				}
			}.start();
		}
	}

	private static DexClassLoader loadJar(Context ctx, File jarFile) {
		Logger.d("Load external jar: "+jarFile.getAbsolutePath());
		return new DexClassLoader(jarFile.getAbsolutePath(), ctx.getDir(
				"dexout", 0).getAbsolutePath(), null, ctx.getClassLoader());
	}

	public static void streamCopy(InputStream is, OutputStream os)
			throws IOException {
		byte[] buffer = new byte[8192];
		int count;
		while ((count = is.read(buffer)) != -1) {
			os.write(buffer, 0, count);
		}
		os.flush();
		os.close();
		is.close();
	}
}
