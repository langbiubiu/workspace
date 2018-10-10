package cn.ipanel.dlna.db;

import android.database.sqlite.SQLiteDatabase;

public class DeviceTable {
	public static final String TABLE_NAME = "devices";

	public static final String COLUMN_ID = "_id";
	public static final String COLUMN_UDN = "UDN";
	public static final String COLUMN_NAME = "name";
	public static final String COLUMN_UPNP_CLASS = "upnp_class";
	public static final String COLUMN_LOCATION = "location";

	  // Database creation SQL statement
	  private static final String DATABASE_CREATE = "create table " 
	      + TABLE_NAME
	      + "(" 
	      + COLUMN_ID + " integer primary key autoincrement, " 
	      + COLUMN_UDN + " text not null, " 
	      + COLUMN_UPNP_CLASS + " text not null," 
	      + COLUMN_LOCATION + " text not null," 
	      + COLUMN_NAME
	      + " text not null" 
	      + ");";

	  public static void onCreate(SQLiteDatabase database) {
	    database.execSQL(DATABASE_CREATE);
	  }

	  public static void onUpgrade(SQLiteDatabase database, int oldVersion,
	      int newVersion) {
	    database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
	    onCreate(database);
	  }
}
