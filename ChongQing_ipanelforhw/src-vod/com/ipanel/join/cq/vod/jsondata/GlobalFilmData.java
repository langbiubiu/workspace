package com.ipanel.join.cq.vod.jsondata;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.text.TextUtils;

import com.ipanel.join.chongqing.live.SharedPreferencesMenager;
import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;

public class GlobalFilmData {

	private static GlobalFilmData instance;
	public String cardID;
	public String uid;
	public String weibo;
	public boolean eds;
	
	public String flag = "0";// 0为电影，1为电视剧
	public int currentTime = 0;
	public int currentENum = 1;
	public String epgUrl = null;
	private List<String> collectList;
	private String typeId;
	private String vodId;
	private String playType;
	private MovieDetailResponse movieDetailResponse;
	private String accessToken;
	private String jsonString;
	private String icState;
	private String authToken;
	private String servicegroup;

	private long historyTime;
	private long durationTime;
	
	
	private String aaa_state;

	public String getAaa_state() {
		return aaa_state;
	}

	public void setAaa_state(String aaa_state) {
		this.aaa_state = aaa_state;
	}

	public long getHistoryTime() {
		return historyTime;
	}

	public void setHistoryTime(long historyTime) {
		this.historyTime = historyTime;
	}

	public long getDurationTime() {
		return durationTime;
	}

	public void setDurationTime(long durationTime) {
		this.durationTime = durationTime;
	}

	public List<String> getCollectList() {
		return collectList;
	}

	public void setCollectList(List<String> collectList) {
		this.collectList = collectList;
	}

	public String getCardID() {
		return cardID;
	}

	public void setCardID(String cardID) {
		this.cardID = cardID;
	}

	public String groupServiceId;

	public String getGroupServiceId() {
		return groupServiceId;
	}

	public void setGroupServiceId(String string) {
		this.groupServiceId = string;
	}

	public ContentUrl contentUrl;
	

	public ContentUrl getContentUrl() {
		return contentUrl;
	}

	public void setContentUrl(ContentUrl contentUrl) {
		this.contentUrl = contentUrl;
	}

	public String getEpgUrl() {
		return epgUrl;
	}
	
	public String getEPGBaseURL(){
		return getEpgUrl()+"/defaultHD/en";
	}

	public void setEpgUrl(String epgUrl) {
		this.epgUrl = epgUrl;
	}

	public int getCurrentENum() {
		return currentENum;
	}

	public void setCurrentENum(int currentENum) {
		this.currentENum = currentENum;
	}

	public boolean isFinished = true;

	public int getCurrentTime() {
		return currentTime;
	}

	public void setCurrentTime(int currentTime) {
		this.currentTime = currentTime;
	}

	public boolean isFinished() {
		return isFinished;
	}

	public void setFinished(boolean isFinished) {
		this.isFinished = isFinished;
	}

	public String getFlag() {
		return flag;
	}

	public void setFlag(String flag) {
		this.flag = flag;
	}
	public synchronized static GlobalFilmData getInstance() {
		if (instance == null) {
			instance = new GlobalFilmData();
		}
		return instance;
	}

	private String cookieString;

	public String getCookieString() {
		return cookieString;
	}

	public void setCookieString(String cookieString) {
		this.cookieString = cookieString;
	}
	private List<String> firstMenuText = new ArrayList<String>();
	private List<List<String>> secondMenuText = new ArrayList<List<String>>();

	private List<String> firstMenuID = new ArrayList<String>();
	private List<List<String>> secondMenuID = new ArrayList<List<String>>();

	public List<String> getFirstMenuID() {
		return firstMenuID;
	}

	public void setFirstMenuID(List<String> firstMenuID) {
		this.firstMenuID = firstMenuID;
	}

	public List<String> getFirstMenuText() {
		return firstMenuText;
	}

	public void setFirstMenuText(List<String> firstMenuText) {
		this.firstMenuText = firstMenuText;
	}

	public List<List<String>> getSecondMenuText() {
		return secondMenuText;
	}

	public void setSecondMenuText(List<List<String>> secondMenuText) {
		this.secondMenuText = secondMenuText;
	}

	public List<List<String>> getSecondMenuID() {
		return secondMenuID;
	}

	public void setSecondMenuID(List<List<String>> secondMenuID) {
		this.secondMenuID = secondMenuID;
	}

	public String getTypeId() {
		return typeId;
	}

	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}

	public MovieDetailResponse getMovieDetailResponse() {
		return movieDetailResponse;
	}

	public void setMovieDetailResponse(MovieDetailResponse movieDetailResponse) {
		this.movieDetailResponse = movieDetailResponse;
	}

	public String getVodId() {
		return vodId;
	}

	public void setVodId(String vodId) {
		this.vodId = vodId;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getPlayType() {
		return playType;
	}

	public void setPlayType(String playType) {
		this.playType = playType;
	}

	public String getJsonString() {
		return jsonString;
	}

	public void setJsonString(String jsonString) {
		this.jsonString = jsonString;
	}

	public String getIcState() {
		return icState;
	}

	public void setIcState(String icState) {
		this.icState = icState;
	}

	public String getAuthToken() {
		return authToken;
	}

	public void setAuthToken(String authToken) {
		this.authToken = authToken;
	}


	public String getServicegroup() {
		return servicegroup;
	}

	public void setServicegroup(String servicegroup) {
		this.servicegroup = servicegroup;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
	
	public boolean isWeiboBinded(){
		return !TextUtils.isEmpty(weibo)&&!"".equals(weibo);
	}
	
	public boolean isAAAValidState(){
		return "1".equals(aaa_state);
	}
	
	public void saveEpgUrl(Context context){
		SharedPreferencesMenager pref = SharedPreferencesMenager.getInstance(context);
		pref.putValueString("vod_epgurl", getEPGBaseURL());
		pref.putValueString("cookie", getCookieString());
		pref.saveData();
	}
}
