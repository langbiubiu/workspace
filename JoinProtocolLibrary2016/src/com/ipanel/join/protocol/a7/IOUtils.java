package com.ipanel.join.protocol.a7;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.util.Map;
import java.util.Map.Entry;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class IOUtils {

	public static String loadAssetText(Context ctx, String fileName) {
		return loadAssetText(ctx, fileName, null);

	}

	public static String loadAssetText(Context ctx, String fileName, String encoding) {
		try {
			InputStream is = ctx.getAssets().open(fileName);
			String content = encoding != null ? new String(IS2ByteArray(is), encoding)
					: new String(IS2ByteArray(is));
			is.close();
			return content;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static byte[] IS2ByteArray(InputStream is) throws IOException {
		ByteArrayOutputStream bao = new ByteArrayOutputStream();
		streamCopy(is, bao);
		byte[] result = bao.toByteArray();
		return result;
	}

	public static void streamCopy(InputStream is, OutputStream os) throws IOException {
		byte[] buf = new byte[8 * 1024];
		int len;
		while ((len = is.read(buf)) != -1) {
			os.write(buf, 0, len);
		}
		os.close();
		is.close();
	}
	
	public static void fileCopy(File from, File to) throws IOException {
		FileInputStream is = new FileInputStream(from);
		FileOutputStream os = new FileOutputStream(to);

		long offset = 0;
		long len = from.length();

		FileChannel isFc = is.getChannel();
		FileChannel osFc = os.getChannel();
		try {
			isFc.transferTo(offset, len, osFc);
		} finally {
			osFc.close();
			isFc.close();
			os.close();
			is.close();
		}
	}

	public static boolean saveSharedPreferencesToFile(SharedPreferences pref, File dst) {
		boolean res = false;
		ObjectOutputStream output = null;
		try {
			output = new ObjectOutputStream(new FileOutputStream(dst));

			output.writeObject(pref.getAll());

			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (output != null) {
					output.flush();
					output.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;
	}

	@SuppressWarnings({ "unchecked" })
	public static boolean loadSharedPreferencesFromFile(SharedPreferences pref, File src) {
		boolean res = false;
		ObjectInputStream input = null;
		try {
			input = new ObjectInputStream(new FileInputStream(src));
			Editor prefEdit = pref.edit();
			prefEdit.clear();
			Map<String, ?> entries = (Map<String, ?>) input.readObject();
			for (Entry<String, ?> entry : entries.entrySet()) {
				Object v = entry.getValue();
				String key = entry.getKey();

				if (v instanceof Boolean)
					prefEdit.putBoolean(key, ((Boolean) v).booleanValue());
				else if (v instanceof Float)
					prefEdit.putFloat(key, ((Float) v).floatValue());
				else if (v instanceof Integer)
					prefEdit.putInt(key, ((Integer) v).intValue());
				else if (v instanceof Long)
					prefEdit.putLong(key, ((Long) v).longValue());
				else if (v instanceof String)
					prefEdit.putString(key, ((String) v));
			}
			prefEdit.commit();
			res = true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} finally {
			try {
				if (input != null) {
					input.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
		return res;
	}
	
	
}
