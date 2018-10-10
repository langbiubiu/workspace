package cn.ipanel.android.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * Simple provider backed by SharedPreference, for simplicity everything is saved as string
 * 
 * @author Zexu
 *
 */
public class MemoryProvider extends ContentProvider {
	Map<String, String> map = new HashMap<String, String>();

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		if (projection == null)
			return null;
		MatrixCursor mc = new MatrixCursor(projection);
		List<String> values = new ArrayList<String>();
		for (String p : projection) {
			values.add(map.get(p));
		}
		mc.addRow(values);
		return mc;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		updateValues(values);
		return uri;
	}

	private int updateValues(ContentValues values) {
		if (values == null)
			return 0;
		for (String key : values.keySet()) {
			map.put(key, values.getAsString(key));
		}
		return values.keySet().size();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		return updateValues(values);
	}

}
