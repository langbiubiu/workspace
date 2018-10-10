package com.ipanel.join.cq.vod.db;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import com.ipanel.join.cq.vod.db.VodContentProviderMetaData.AllfavoritesInfo;
import com.ipanel.join.cq.vod.db.VodContentProviderMetaData.PlayedInfo;
import com.ipanel.join.cq.vod.jsondata.WatchHistory;
import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;

public class ContentproviderHelper {
	private static ContentproviderHelper contentproviderHelper;
	private Context context;
//	SimpleDateFormat myFmt3=new SimpleDateFormat("yyyy-MM-dd E ");
	SimpleDateFormat myFmt3=new SimpleDateFormat("yyyy-MM-dd");
	
	private ContentproviderHelper() {
	}

	public static ContentproviderHelper getInstance() {
		if (contentproviderHelper == null) {
			contentproviderHelper = new ContentproviderHelper();
		}
		return contentproviderHelper;
	}

	public ContentValues getVodFavorites(MovieDetailResponse movieDetailResponse, Boolean bool, String jsonString) {
		ContentValues values = new ContentValues();
		values.put(AllfavoritesInfo.VODID, movieDetailResponse.getVodId());
		values.put(AllfavoritesInfo.TYPEID, movieDetailResponse.getTypeId());
		values.put(AllfavoritesInfo.VODNAME, movieDetailResponse.getVodName());
		values.put(AllfavoritesInfo.DIRECTOR, movieDetailResponse.getDirector());
		values.put(AllfavoritesInfo.ACTOR, movieDetailResponse.getActor());
		values.put(AllfavoritesInfo.INTR, movieDetailResponse.getIntr());
		values.put(AllfavoritesInfo.PLAYTYPE, movieDetailResponse.getPlayType());
		values.put(AllfavoritesInfo.PICPATH, movieDetailResponse.getPicPath());
		values.put(AllfavoritesInfo.ELAPSETIME, movieDetailResponse.getElapsetime());
		values.put(AllfavoritesInfo.TOTALNUM, movieDetailResponse.getTotalNum());
		StringBuffer vodIds = new StringBuffer();
		if(movieDetailResponse.getVodIdList() != null){
			int length = movieDetailResponse.getVodIdList().size();
			for (int i = 0; i < length; i++) {
				if (i != length - 1) {
					vodIds.append(movieDetailResponse.getVodIdList().get(i) + "-");
				} else {
					vodIds.append(movieDetailResponse.getVodIdList().get(i));
				}
			}
			values.put(AllfavoritesInfo.VODIDLIST, vodIds.toString());
		}
		values.put(AllfavoritesInfo.ISAFTERPLAY, bool);
		values.put(AllfavoritesInfo.JSONSTRING, jsonString);
		return values;
	}

	public ContentValues getVodPlayedInfo(MovieDetailResponse movieDetailResponse,int count,String code,int isOver) {
		ContentValues values = new ContentValues();//������Ĳ�����������Ϣת����contentValue
		String[] vodIdsTimes = null;
		values.put(PlayedInfo.VODID, movieDetailResponse.getVodId());
		values.put(PlayedInfo.TYPEID, movieDetailResponse.getTypeId());
		values.put(PlayedInfo.VODNAME, movieDetailResponse.getVodName());
		values.put(PlayedInfo.DIRECTOR, movieDetailResponse.getDirector());
		values.put(PlayedInfo.ACTOR, movieDetailResponse.getActor());
		values.put(PlayedInfo.INTR, movieDetailResponse.getIntr());
		values.put(PlayedInfo.PLAYTYPE, movieDetailResponse.getPlayType());
		values.put(PlayedInfo.PICPATH, movieDetailResponse.getPicPath());
		values.put(PlayedInfo.ELAPSETIME, movieDetailResponse.getElapsetime());
		values.put(PlayedInfo.TOTALNUM, movieDetailResponse.getTotalNum());
		values.put(PlayedInfo.TIME, myFmt3.format(System.currentTimeMillis()));//��ϵͳ�ĵ�ǰʱ�任��
		values.put(PlayedInfo.SHOWTIME, System.currentTimeMillis());
		values.put(PlayedInfo.ISOVER, movieDetailResponse.isOver());
		StringBuffer vodIds = new StringBuffer();
		StringBuffer vodIdsTime = new StringBuffer();
		if(code !=null){
			vodIdsTimes = code.split("-");
		}
		if(movieDetailResponse.getVodIdList() != null){
			int length = movieDetailResponse.getVodIdList().size();
			for (int i = 0; i < length; i++) {
				if (i != length - 1) {
					vodIds.append(movieDetailResponse.getVodIdList().get(i) + "-");
					if(i+1 == Integer.parseInt(movieDetailResponse.getEpisodes())){
						vodIdsTime.append(movieDetailResponse.getVodIdList().get(i)+":"+movieDetailResponse.getPlayedTime()+":"+isOver+"-");
					}else{
						if(count == 0){
							vodIdsTime.append(movieDetailResponse.getVodIdList().get(i)+"-");
						}else{
							vodIdsTime.append(vodIdsTimes[i%vodIdsTimes.length]+"-");
						}
					}
				} else {
					vodIds.append(movieDetailResponse.getVodIdList().get(i));
					if(i+1 == Integer.parseInt(movieDetailResponse.getEpisodes())){
						vodIdsTime.append(movieDetailResponse.getVodIdList().get(i)+":"+movieDetailResponse.getPlayedTime()+":"+isOver);
					}else{
						if(count == 0){
							vodIdsTime.append(movieDetailResponse.getVodIdList().get(i));
						}else{
							vodIdsTime.append(vodIdsTimes[i%vodIdsTimes.length]);
						}
					}
					
				}
			}
			values.put(PlayedInfo.VODIDLIST, vodIds.toString());
			/** �����Ӿ� ÿһ����Ӧ�� id�������ݿ� */
			values.put(PlayedInfo.CODE, vodIdsTime.toString());
			
		}
		values.put(PlayedInfo.LASTEPOSDIDE, movieDetailResponse.getEpisodes());
		values.put(PlayedInfo.WATCHTIME, movieDetailResponse.getPlayedTime());
	
		return values;
	}
	public List<MovieDetailResponse> getAllFavoritesList(boolean flag) {
		List<MovieDetailResponse> movieDetailResponseList = new ArrayList<MovieDetailResponse>();
		String selection = "isafterplay=?";
		String[] selectionArgs = new String[] { flag == true ? "0" : "1" };
		Uri uri = AllfavoritesInfo.CONTENT_URI;
		Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);
		while (cursor != null && cursor.moveToNext()) {
			MovieDetailResponse movieDetailResponse = new MovieDetailResponse();
			movieDetailResponse.setVodId(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.VODID)));
			movieDetailResponse.setTypeId(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.TYPEID)));
			movieDetailResponse.setVodName(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.VODNAME)));
			movieDetailResponse.setDirector(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.DIRECTOR)));
			movieDetailResponse.setActor(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.ACTOR)));
			movieDetailResponse.setIntr(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.INTR)));
			movieDetailResponse.setPlayType(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.PLAYTYPE)));
			movieDetailResponse.setPicPath(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.PICPATH)));
			movieDetailResponse.setElapsetime(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.ELAPSETIME)));
			movieDetailResponse.setTotalNum(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.TOTALNUM)));
			if(cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.VODIDLIST)) != null){
				String[] vodIds = (cursor.getString(cursor.getColumnIndex(AllfavoritesInfo.VODIDLIST))).split("-");
				List<String> list = Arrays.asList(vodIds);
				movieDetailResponse.setVodIdList(list);
			}
			movieDetailResponseList.add(movieDetailResponse);
		}
		if (cursor != null) {
			cursor.close();
		}
		return movieDetailResponseList;
	}

	/**
	 * ��ʷ��¼���췵��
	 * @return
	 */
	public List<WatchHistory> getAllHistoryDataByDate() {  //
		List<WatchHistory> watchs = new ArrayList<WatchHistory>();
		String sql = "SELECT " + PlayedInfo.TIME + " FROM " + PlayedInfo.PLAYEDINFO_TABLE_NAME//�����������д��
				+ " WHERE (1=1) GROUP BY (time) ORDER BY showTime DESC limit 6";//�������ȡ�������ʷ��¼
		Cursor c = DbHelper.getInstance(context).getDb().rawQuery(sql, null);//��ѯsql
		for (; c.moveToNext();) {
			WatchHistory watch = new WatchHistory();//���ɶ���
			watch.setTime(c.getString(c.getColumnIndexOrThrow(PlayedInfo.TIME)));//�ڶ��������ò�ѯ������ʱ������ÿ��
			watch.setVodPlayInfos(new ArrayList<MovieDetailResponse>());//����һ�����鷵�ص��б�������ʵ����
			String selectplayinfoData = "SELECT * FROM  playedinfo WHERE  time  = "
					+ '"' + c.getString(c.getColumnIndexOrThrow(PlayedInfo.TIME)) + '"' 
					+ " ORDER BY showTime DESC";//��ѯƴ����Ϣ��ʱ��ݼ�
			Cursor dataCursor = DbHelper.getInstance(context).getDb().rawQuery(selectplayinfoData, null);//����ƴ����Ϣ
			for (; dataCursor.moveToNext();) {
				if(dataCursor.getString(dataCursor.getColumnIndexOrThrow(PlayedInfo.ISOVER)).equals("1")){
					//���˵����Ӿ�ĵ�������
					continue;
				}
				MovieDetailResponse movieDetailResponse = new MovieDetailResponse();//���ɲ�ѯ����
				movieDetailResponse.setVodId(dataCursor.getString(dataCursor.getColumnIndexOrThrow(PlayedInfo.VODID)));//�ò�ѯ��������Ϣ�������
				movieDetailResponse
						.setTypeId(dataCursor.getString(dataCursor.getColumnIndexOrThrow(PlayedInfo.TYPEID)));
				movieDetailResponse.setVodName(dataCursor.getString(dataCursor
						.getColumnIndexOrThrow(PlayedInfo.VODNAME)));
				movieDetailResponse.setDirector(dataCursor.getString(dataCursor
						.getColumnIndexOrThrow(PlayedInfo.DIRECTOR)));
				movieDetailResponse.setActor(dataCursor.getString(dataCursor.getColumnIndexOrThrow(PlayedInfo.ACTOR)));
				movieDetailResponse.setIntr(dataCursor.getString(dataCursor.getColumnIndexOrThrow(PlayedInfo.INTR)));
				movieDetailResponse.setPlayType(dataCursor.getString(dataCursor
						.getColumnIndexOrThrow(PlayedInfo.PLAYTYPE)));
				movieDetailResponse.setPicPath(dataCursor.getString(dataCursor
						.getColumnIndexOrThrow(PlayedInfo.PICPATH)));
				movieDetailResponse.setElapsetime(dataCursor.getString(dataCursor
						.getColumnIndexOrThrow(PlayedInfo.ELAPSETIME)));
				movieDetailResponse.setTotalNum(dataCursor.getString(dataCursor
						.getColumnIndexOrThrow(PlayedInfo.TOTALNUM)));
				movieDetailResponse.setPlayedTime(dataCursor.getString(dataCursor
						.getColumnIndexOrThrow(PlayedInfo.WATCHTIME)));
				movieDetailResponse.setEpisodes(dataCursor.getString(dataCursor
						.getColumnIndexOrThrow(PlayedInfo.LASTEPOSDIDE)));// ���ŵ�����һ��
				if(dataCursor.getString(dataCursor.getColumnIndex(PlayedInfo.VODIDLIST)) != null){
					String[] vodIds = (dataCursor.getString(dataCursor.getColumnIndex(PlayedInfo.VODIDLIST))).split("-");
					List<String> list = Arrays.asList(vodIds);
					movieDetailResponse.setVodIdList(list);
				}
				watch.getVodPlayInfos().add(movieDetailResponse);
			}
			watchs.add(watch);
			if (dataCursor != null) {
				dataCursor.close();
			}
		}
		if (c != null) {
			c.close();
		} 
		return watchs;
	}

	public void setContext(Context context) {
		this.context = context;
	}
	
	public void deleteHistoryTable()
	{
		String sql = "Delete From " + PlayedInfo.PLAYEDINFO_TABLE_NAME;
		DbHelper.getInstance(context).getDb().execSQL(sql);			//ִ��SQL���
	}
	/**
	 * ��vodIdɾ�����ż�¼
	 */
	public void deleteHistoryById(String vodId){
		String sql = "Delete From "+ PlayedInfo.PLAYEDINFO_TABLE_NAME+" where vodId = " + vodId;
		DbHelper.getInstance(context).getDb().execSQL(sql);
	}
}
