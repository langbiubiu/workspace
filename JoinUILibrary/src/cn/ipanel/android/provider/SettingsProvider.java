package cn.ipanel.android.provider;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

/**
 * Simple provider backed by SharedPreference, for simplicity everything is
 * saved as string
 * 
 * @author Zexu
 *
 */
public class SettingsProvider extends ContentProvider {
	SharedPreferences sp;

	@Override
	public boolean onCreate() {
		sp = getPreference();
		return true;
	}

	public SharedPreferences getPreference() {
		return getContext().getSharedPreferences(SettingsProvider.class.getName(), 0);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		if (projection == null)
			return null;
		MatrixCursor mc = new MatrixCursor(projection);
		List<String> values = new ArrayList<String>();
		for (String p : projection) {
			values.add(sp.getString(p, null));
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
		Editor editor = sp.edit();
		for (String key : values.keySet()) {
			editor.putString(key, values.getAsString(key));
		}
		editor.commit();
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
