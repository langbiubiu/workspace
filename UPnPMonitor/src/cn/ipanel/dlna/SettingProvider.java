package cn.ipanel.dlna;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public class SettingProvider extends ContentProvider {
	
	static final String COLUME_ENABLE_SERVICE = "enable_service"; 

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
			String sortOrder) {
		MatrixCursor cursor = new MatrixCursor(new String[]{COLUME_ENABLE_SERVICE});
		cursor.addRow(new Boolean[]{!PersistStore.BoolSetting.DISABLE_AUTO_START.getSetting(getContext())});
		return cursor;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		Boolean value = values.getAsBoolean(COLUME_ENABLE_SERVICE);
		if (value != null) {
			PersistStore.BoolSetting.DISABLE_AUTO_START.setSetting(getContext(), !value);
			if (value)
				getContext().startService(new Intent(getContext(), UPnPService.class));
			else
				getContext().stopService(new Intent(getContext(), UPnPService.class));
		}
		return 0;
	}

}
