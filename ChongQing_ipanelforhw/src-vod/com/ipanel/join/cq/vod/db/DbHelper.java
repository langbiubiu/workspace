package com.ipanel.join.cq.vod.db;

import java.text.SimpleDateFormat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.ipanel.join.cq.vod.db.VodContentProviderMetaData.PlayedInfo;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;

public final class DbHelper {
	private static DbHelper dbHelper;
	private SQLiteDatabase db;
	private SQLiteHelper openHelper;
	private static Context context;
	SimpleDateFormat dateformat = new SimpleDateFormat(  
            "yyyy-MM-dd  E");
	public static final String AUTHORIY = "vod.contentprovider";
	
	private DbHelper(Context c) {
		openHelper = new SQLiteHelper(c, "vod.db", null, 2);
		this.db = openHelper.getWritableDatabase();
	};

	public static DbHelper getInstance(Context c) {
		if (dbHelper == null) {
			context = c;
			dbHelper = new DbHelper(c);
		}
		return dbHelper;
	}

	public void closeDb() {
		if (db != null) {
			db.close();
		}
		setDbHelper(null);
	}
	public static void setDbHelper(DbHelper dbHelper) {
		DbHelper.dbHelper = dbHelper;
	}
	/**
	 * ���ݵ��Ӿ��vodId ��ȡ���ݿ��е���һ�β��ŵļ���
	 * @param televisionid
	 * @return
	 */
	public String getLastTelevisonNumberByVodId(String vodId){
		String sql = "select *  from " + PlayedInfo.PLAYEDINFO_TABLE_NAME + " where "+ PlayedInfo.VODID +" = "+ '"' + vodId + '"';
		Cursor c = db.rawQuery(sql, null);
		while(c.moveToNext()){
			String lastEposdide  =c.getString(c.getColumnIndex(PlayedInfo.LASTEPOSDIDE));
			return lastEposdide;
		}
		return "1";
	}
	/**
	 * ����vodId��ȡ����ʱ��
	 * @param vodId
	 * @return
	 */
	public String getWatchTimeByVodId(String vodId){
		if(TextUtils.isEmpty(vodId)){
			return "0";
		}
		Log.i("", "vodId-->"+vodId);
		String sql = "select *  from " + PlayedInfo.PLAYEDINFO_TABLE_NAME + " where "+ PlayedInfo.VODID + " = " + '"' + vodId + '"';
		Cursor c = db.rawQuery(sql, null);
		while(c.moveToNext()){
			String watchTime  = c.getString(c.getColumnIndex(PlayedInfo.WATCHTIME));
			String duration = c.getString(c.getColumnIndex(PlayedInfo.ELAPSETIME));
			if(watchTime.equals(duration)){
				return "0";
			}
			return watchTime;
		}
		return "0";
	}
	/**
	 * ���ݻ���ID,���ϴβ��ż���
	 * @param vodId
	 * @return
	 */
	public String getLastEposide(String wikiId){
		if(TextUtils.isEmpty(wikiId)){
			return "1";
		}
		Log.i("", "wikiId-->"+wikiId);
		String sql = "select *  from " + PlayedInfo.PLAYEDINFO_TABLE_NAME + " where "+ PlayedInfo.DIRECTOR + " = " + '"' +wikiId + '"';
		Cursor c = db.rawQuery(sql, null);
		while(c.moveToNext()){
			String lastEposide  = c.getString(c.getColumnIndex(PlayedInfo.LASTEPOSDIDE));
			return lastEposide;
		}
		return "1";
	}
	/**
	 * ����һ���ۿ�����ʷ��¼
	 * @param code
	 * @param playedTime
	 * @param jsonString
	 * @param totalTime
	 */
	public void insertVodPlayedData(long playedTime,long duration,String lastEposdide,MovieDetailResponse movieDetailResponse,int isOver){
		Logger.d(String.format("playedTime: %s duration: %s", playedTime,duration));
		Logger.d(String.format("lastEposdide: %s MovieDetailResponse: ", lastEposdide,movieDetailResponse.toString()));
		String sql = "SELECT * FROM PLAYEDINFO WHERE "+ PlayedInfo.VODID +" = "+'"'+movieDetailResponse.getVodId()+'"';
		String code = null;
		Cursor c = db.rawQuery(sql, null);
		c.moveToFirst();
		int count = c.getCount();
		movieDetailResponse.setElapsetime(String.valueOf(duration));
		movieDetailResponse.setPlayedTime(String.valueOf(playedTime));
		movieDetailResponse.setEpisodes(lastEposdide);//���β��ŵļ���
		movieDetailResponse.setOver(isOver);
		if(count != 0){
			code = c.getString(c.getColumnIndexOrThrow(PlayedInfo.CODE));
		}
		ContentValues contentValues = ContentproviderHelper.getInstance().getVodPlayedInfo(movieDetailResponse,count,code,isOver);
		if(count == 0){
			db.insert(PlayedInfo.PLAYEDINFO_TABLE_NAME, null, contentValues);
		}else{
			db.update(PlayedInfo.PLAYEDINFO_TABLE_NAME, contentValues, PlayedInfo.VODID +" = ? ", new String[] {movieDetailResponse.getVodId()});
		}
		context.getContentResolver().notifyChange(PlayedInfo.CONTENT_URI, null);
	}

	public SQLiteDatabase getDb() {
		return db;
	}
}
