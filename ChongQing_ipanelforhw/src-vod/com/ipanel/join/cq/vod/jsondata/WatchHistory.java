package com.ipanel.join.cq.vod.jsondata;

import java.util.List;

import com.ipanel.join.protocol.huawei.cqvod.MovieDetailResponse;

public class WatchHistory {

	private String time;
	private List<MovieDetailResponse> vodPlayInfos;

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public List<MovieDetailResponse> getVodPlayInfos() {
		return vodPlayInfos;
	}

	public void setVodPlayInfos(List<MovieDetailResponse> vodPlayInfos) {
		this.vodPlayInfos = vodPlayInfos;
	}

}
