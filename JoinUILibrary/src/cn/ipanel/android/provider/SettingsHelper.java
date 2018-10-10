package cn.ipanel.android.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Helper class for access data in {@link SettingsProvider}
 * 
 * @author Zexu
 *
 */
public class SettingsHelper {
	private Uri uri;
	private Context ctx;

	public SettingsHelper(Context ctx, Uri uri) {
		this.ctx = ctx.getApplicationContext();
		this.uri = uri;
	}

	public String getPropValue(String prop) {
		ContentResolver cr = ctx.getContentResolver();
		Cursor cursor = cr.query(uri, new String[] { prop }, null, null, null);
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				return cursor.getString(0);
			}
			cursor.close();
		}
		return null;
	}

	public int getPropValueInt(String prop) {
		String val = getPropValue(prop);
		if (!TextUtils.isEmpty(val)) {
			try {
				return Integer.parseInt(val);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0;
	}

	public long getPropValueLong(String prop) {
		String val = getPropValue(prop);
		if (!TextUtils.isEmpty(val)) {
			try {
				return Long.parseLong(val);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0l;
	}

	public double getPropValueDouble(String prop) {
		String val = getPropValue(prop);
		if (!TextUtils.isEmpty(val)) {
			try {
				return Double.parseDouble(val);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0.0;
	}

	public float getPropValueFloat(String prop) {
		String val = getPropValue(prop);
		if (!TextUtils.isEmpty(val)) {
			try {
				return Float.parseFloat(val);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return 0f;
	}

	public boolean setPropValue(String prop, Object value) {
		ContentResolver cr = ctx.getContentResolver();
		ContentValues cv = new ContentValues();
		cv.put(prop, String.valueOf(value));
		return cr.update(uri, cv, null, null) > 0;
	}

	public int setPropValues(ContentValues cv) {
		ContentResolver cr = ctx.getContentResolver();
		return cr.update(uri, cv, null, null);
	}
}
