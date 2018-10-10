package com.ipanel.join.cq.vod.db;

import com.ipanel.join.cq.vod.db.VodContentProviderMetaData.AllfavoritesInfo;
import com.ipanel.join.cq.vod.db.VodContentProviderMetaData.PlayedInfo;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
/**
 * Helper类，用于建立、更新和打开数据库
 */

public class SQLiteHelper extends SQLiteOpenHelper {
	SQLiteDatabase sqLiteDatabase;
	/** 创建我的收藏 数据表*/
	private static final String DB_CREATE_FAVORITES = "CREATE TABLE " 
	+ AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME
	+ " (" 
	+ AllfavoritesInfo._ID + " integer primary key autoincrement, "
	+ AllfavoritesInfo.VODID + " text, " 
	+ AllfavoritesInfo.TYPEID + " text, " 
	+ AllfavoritesInfo.VODNAME + " text,  "
	+ AllfavoritesInfo.DIRECTOR + " text,  "
	+ AllfavoritesInfo.ACTOR + " text,  "
	+ AllfavoritesInfo.INTR + " text,  "
	+ AllfavoritesInfo.PLAYTYPE + " text,  "
	+ AllfavoritesInfo.PICPATH + " text,  "
	+ AllfavoritesInfo.TOTALNUM + " text,  "
	+ AllfavoritesInfo.CODE + " text,  "
	+ AllfavoritesInfo.VODIDLIST + " text,  "
	+ AllfavoritesInfo.JSONSTRING + " text,  "
	+ AllfavoritesInfo.ISAFTERPLAY + " boolean default false,  "
	+ AllfavoritesInfo.ELAPSETIME+ " text "
	+	");";
	
	private static final String DB_CREATE_PLAY = "CREATE TABLE " 
	+PlayedInfo.PLAYEDINFO_TABLE_NAME
	+ " (" 
	+ PlayedInfo._ID + " integer primary key autoincrement, "
	+ PlayedInfo.VODID + " text, " 
	+ PlayedInfo.TYPEID + " text,  " 
	+ PlayedInfo.VODNAME + " text,  "
	+ PlayedInfo.DIRECTOR + " text,  "
	+ PlayedInfo.ACTOR + " text,  "
	+ PlayedInfo.INTR + " text,  "
	+ PlayedInfo.PLAYTYPE + " text,  "
	+ PlayedInfo.PICPATH + " text,  "
	+ PlayedInfo.ELAPSETIME + " text,  "
	+ PlayedInfo.TOTALNUM + " text ,  "
	+ PlayedInfo.VODIDLIST + " text ,  "
	+ PlayedInfo.ISOVER + " integer ,  "
	+ PlayedInfo.LASTEPOSDIDE + " text ,  "
	+ PlayedInfo.URL + " text ,  "
	+ PlayedInfo.TIME + " text ,  "
	+ PlayedInfo.SHOWTIME + " text ,  "
	+ PlayedInfo.WATCHTIME + " text ,   "
	+ PlayedInfo.TIMERATIO + " text ,  "
	+ PlayedInfo.CODE + " text , "
	+ PlayedInfo.JSONSTRING + " text  "
	+		");";

	public SQLiteHelper(Context context, String name, CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(DB_CREATE_FAVORITES);
		db.execSQL(DB_CREATE_PLAY);
		this.sqLiteDatabase=db;
	}
	

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + AllfavoritesInfo.ALLFAVORITESINFO_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS " + PlayedInfo.PLAYEDINFO_TABLE_NAME);
		onCreate(db);
	}

}
