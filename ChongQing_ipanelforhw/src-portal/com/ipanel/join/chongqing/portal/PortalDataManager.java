package com.ipanel.join.chongqing.portal;

import com.google.gson.Gson;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.chongqing.live.LiveForHWActivity;
import com.ipanel.join.cq.vod.detail.DetailActivity;
import com.ipanel.join.cq.vod.detail.TVDetailActivity;
import com.ipanel.join.cq.vod.vodhome.FilmListActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;


import ipaneltv.uuids.ChongqingUUIDs;

public class PortalDataManager {

	public static Gson gson = new Gson();
	
	public static String url = "http://117.59.6.101/json"; //欢网-测试地址
	
	/**
	 * 获取直播推荐
	 */
	public static final String ACTION_GET_LIVE_PROGRAMS_BY_RECOMMEND = "GetLiveProgramsByRecommend";
	/**
	 * 获取大家都在看
	 */
	public static final String ACTION_GET_WIKIS_BY_HOT = "GetWikisByHot";
	/**
	 * 获取最新榜列表
	 */
	public static final String ACTION_GET_WIKIS_BY_NEW = "GetWikisByNew";
	/**
	 * 获取热播榜列表
	 */
	public static final String ACTION_GET_WIKIS_BY_HIT = "GetWikisByHit";
	/**
	 * 获取关注的明星榜列表
	 */
	public static final String ACTION_GET_ACTORS_BY_Follow = "GetActorsByFollow";
	/**
	 * 获取收藏榜列表
	 */
	public static final String ACTION_GET_WIKIS_BY_FAV = "GetWikisByFav";
	
	
	public static String getDtvNetworkPkg(){
		return "com.ipanel.join.network.dl";
	}
	
	public static String getDtvNetworkActivity(){
		return "cn.dalian.tvapps.network.SearchActivity";
	}
	
	public static String getDtvPlayServiceName(){
		return "cn.ipanel.tvapps.network.NcPlayService";
	}
	
	public static String getDtvSourceServiceName(){
		return "com.ipanel.apps.common.tsvodsrcservice";
	}
	
	public static String getBrowserPkg(){
		return "com.test.ipanel.advanced";
	}
	
	public static String getSettingsPkg(){
//		return "com.ipanel.join.settings.dl";
		return "com.ipanel.join.cq.settings";
	}
	
	public static String getMyAppPkg(){
		//return "tv.ipanel.android.chongqing.myapphome";
		return "com.ipanel.join.launcher.dl";
	}

	public static String getCityCode() {
		return "411";
	}
	
	/**
	 * 启动我的页面
	 * 
	 * @param context
	 */
	public static void startMineActivity(Context context){
		Intent intent = new Intent(context,MineActivity.class);
		context.startActivity(intent);
	}
	
	/**
	 * 跳转点播
	 * 
	 * @param label
	 */
	public static void goToVodActivity(Context context,int label){
		Intent intent = new Intent(context,FilmListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("label", label);
		context.startActivity(intent);
	}
	
	/**
	 * 跳转到回看
	 * 
	 * @param context
	 */
	public static void goToLookback(Context context){
		try {
			ComponentName com = new ComponentName("com.ipanel.chongqing_ipanelforhw",
					"com.ipanel.join.cq.back.BackActivity");
			Intent i = new Intent();
			i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			i.setComponent(com);
			context.startActivity(i);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 打开点播详情(大家都在看)
	 * 
	 * @param ctx
	 * @param program
	 */
	public static void openMovieDetail(Context ctx, Wiki program){
		Intent intent = null;
		if(program.getModel().equals("film")){//电影
			intent = new Intent(ctx,DetailActivity.class);
		}else if(program.getModel().equals("teleplay")){//电视剧
			intent = new Intent(ctx,TVDetailActivity.class);
		}else{
			return;
		}
		intent.putExtra("data", program);
		intent.putExtra("id", program.getId());
		intent.putExtra("name", program.getTitle());
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
	
	/**
	 * 通过serviceId跳转到直播
	 * 
	 * @param context
	 * @param serviceId
	 */
	public static void goToLiveActivityVarServiceId(Context context,String serviceId) {
		Intent i = new Intent(context,LiveForHWActivity.class);
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		i.putExtra("live_tag", 1);
		i.putExtra("live_program_tag", serviceId);
//		i.putExtra("live_tag", 3);
//		i.putExtra("live_channel_name_tag", program.name);
		Log.i("HomedDataManager","livePortalFragment into live-----1---->"+System.currentTimeMillis());
		context.startActivity(i);
	}
	
	public static String getDtvUUID(){
		return ChongqingUUIDs.ID;
	}
}
