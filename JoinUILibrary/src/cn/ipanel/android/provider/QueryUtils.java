package cn.ipanel.android.provider;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Handle serialize/deserialize of data from ContentResolver
 * 
 * @author Zexu
 *
 */
public class QueryUtils {
	static final String TAG = QueryUtils.class.getSimpleName();

	/**
	 * @see #loadItemsFrom(Context, String, Class, String, String[], String)
	 * @param ctx
	 * @param uri
	 * @param typeOfT
	 * @return
	 */
	public static <T> List<T> loadItemsFrom(Context ctx, String uri, Class<T> typeOfT) {
		return loadItemsFrom(ctx, uri, typeOfT, null, null, null);
	}

	/**
	 * Deserialize content provider data to java object (all fields name must be public and has the
	 * same name as the column)
	 * 
	 * @param ctx
	 * @param uri
	 * @param typeOfT
	 * @param selection
	 * @param selectionArgs
	 * @return
	 */
	public static <T> List<T> loadItemsFrom(Context ctx, String uri, Class<T> typeOfT,
			String selection, String[] selectionArgs, String sortOrder) {
		List<T> list = new ArrayList<T>();
		Field[] fields = typeOfT.getFields();
		ContentResolver cr = ctx.getContentResolver();
		String[] projection = new String[fields.length];
		for (int i = 0; i < projection.length; i++) {
			projection[i] = fields[i].getName();
		}
		Log.d(TAG, "uri = " + uri + ", projection = " + Arrays.toString(projection));
		Cursor cursor = null;
		try {
			cursor = cr.query(Uri.parse(uri), projection, selection, selectionArgs, sortOrder);
			if (cursor != null && cursor.moveToFirst()) {
				Log.d(TAG, "uri = " + uri + ", projection = " + Arrays.toString(projection)
						+ ", count=" + cursor.getCount());
				do {
					T obj = typeOfT.newInstance();
					for (int i = 0; i < fields.length; i++) {
						switch (cursor.getType(i)) {
						case Cursor.FIELD_TYPE_STRING:
							fields[i].set(obj, cursor.getString(i));
							break;
						case Cursor.FIELD_TYPE_FLOAT:
							if (fields[i].getType() == Double.class
									|| fields[i].getType() == double.class)
								fields[i].set(obj, cursor.getDouble(i));
							else
								fields[i].set(obj, cursor.getFloat(i));
							break;
						case Cursor.FIELD_TYPE_INTEGER:
							if (fields[i].getType() == Long.class
									|| fields[i].getType() == long.class)
								fields[i].set(obj, cursor.getLong(i));
							else
								fields[i].set(obj, cursor.getInt(i));
							break;
						case Cursor.FIELD_TYPE_BLOB:
							fields[i].set(obj, cursor.getBlob(i));
							break;
						case Cursor.FIELD_TYPE_NULL:
							fields[i].set(obj, null);
							break;
						}
					}
					list.add(obj);
				} while (cursor.moveToNext());
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (cursor != null)
				try {
					cursor.close();
				} catch (Exception e) {
					// ignore
				}
		}
		return list;
	}
}
