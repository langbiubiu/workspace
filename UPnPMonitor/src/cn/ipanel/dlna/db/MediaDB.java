package cn.ipanel.dlna.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MediaDB extends SQLiteOpenHelper {
	  private static final String DATABASE_NAME = "dlna.db";
	  private static final int DATABASE_VERSION = 1;
	  
	public MediaDB(Context context){
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		DeviceTable.onCreate(db);
		
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		DeviceTable.onUpgrade(db, oldVersion, newVersion);
		
	}

}
