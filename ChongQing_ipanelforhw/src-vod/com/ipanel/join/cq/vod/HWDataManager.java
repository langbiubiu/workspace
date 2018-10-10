package com.ipanel.join.cq.vod;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwRequest;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse;
import com.ipanel.chongqing_ipanelforhw.hwstruct.GetHwResponse.Wiki;
import com.ipanel.join.cq.vod.detail.DetailActivity;
import com.ipanel.join.cq.vod.detail.TVDetailActivity;
import com.ipanel.join.cq.vod.detail.VodTabActivity;
import com.ipanel.join.cq.vod.searchpage.SearchPage;
import com.ipanel.join.cq.vod.starshow.StarShowActivity;
import com.ipanel.join.cq.vod.utils.Tools;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;

/**
 * 欢网数据管理
 *
 */
public class HWDataManager {
	/**
	 * 欢网测试地址
	 */
	public static final String ROOT_URL = "http://117.59.6.101/json";
	/**
	 * 1.获取频道列表GetChannels
	 */
	public static final String ACTION_GET_CHANNELS = "GetChannels";
	/**
	 * 2.获取直播推荐GetLiveProgramsByRecommend
	 */
	public static final String ACTION_GET_LIVE_PROGRAM_BY_RECOMMEND = "GetLiveProgramsByRecommend";
	/**
	 * 3.获取大家都在看GetWikisByHot
	 */
	public static final String ACTION_GET_WIKIS_BY_HOT = "GetWikisByHot";
	/**
	 * 获取wiki详情
	 */
	public static final String ACTION_GET_WIKI_INFO = "GetWikiInfo";
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
	public static final String ACTION_GET_ACTORS_BY_FOLLOW = "GetActorsByFollow";
	/**
	 * 获取收藏榜列表
	 */
	public static final String ACTION_GET_WIKIS_BY_FAV = "GetWikisByFav";
	/**
	 * 获取演员详情页面
	 */
	public static final String ACTION_GET_ACTOR_INFO = "GetActorInfo";
	/**
	 * wiki分类筛选
	 */
	public static final String ACTION_GET_WIKIS_BY_CATEGORY = "GetWikisByCategory";
	/**
	 * 检索维基（分类、 首字母、 关键字）
	 */
	public static final String ACTION_SEARCH_WIKIS = "SearchWikis";
	/**
	 * 获取热门搜索关键字列表
	 */
	public static final String ACTION_GET_SEARCH_KEYWORD = "GetSearchKeyword";
	/**
	 * 14.获取Wiki相似列表GetSimilarWikis
	 */
	public static final String ACTION_GET_SIMILAR_WIKIS = "GetSimilarWikis";
	/**
	 * 15.获取维基的演员列表GetActorsByWiki(废除)
	 */
	public static final String ACTION_GET_ACTORS_BY_WIKI = "GetActorsByWiki";
	/**
	 * 17.获取用户喜欢的频道GetFavChannelsByUser
	 */
	public static final String ACTION_GET_FAV_CHANNELS_BY_USER = "GetFavChannelsByUser";
	/**
	 * 18.删除我的收藏DelFavChannelByUser
	 */
	public static final String ACTION_DEL_FAV_CHANNELS_BY_USER = "DelFavChannelByUser";
	/**
	 * 19.添加wiki收藏SetFavWikiByUser
	 */
	public static final String ACTION_SET_FAV_WIKI_BY_USER = "SetFavWikiByUser";
	/**
	 * 20.获取我收藏的wiki列表（可点播）GetFavWikisByUser
	 */
	public static final String ACTION_GET_FAV_WIKIS_BY_USER = "GetFavWikisByUser";
	/**
	 * 21.删除我的wiki收藏DelFavWiki
	 */
	public static final String ACTION_DEL_FAV_WIKI = "DelFavWiki";
	/**
	 * 22.设置wiki互动(顶、踩)SetWikiInteractive
	 */
	public static final String ACTION_SET_WIKI_INTERACTIVE = "SetWikiInteractive";
	/**
	 * 23.添加关注演员SetFollowActor
	 */
	public static final String ACTION_SET_FOLLOW_ACTOR = "SetFollowActor";
	/**
	 * 24.获取我关注的演员列表GetMyFollowActors
	 */
	public static final String ACTION_GET_MY_FOLLOW_ACTORS = "GetMyFollowActors";
	/**
	 * 25.删除我关注的演员DelFollowActor
	 */
	public static final String ACTION_DEL_FOLLOW_ACTOR = "DelFollowActor";
	/**
	 * 26.添加订阅标签SetSubscribeTag
	 */
	public static final String ACTION_SET_SUBSCIRBE_TAG = "SetSubscribeTag";
	/**
	 * 27.获取我订阅的wiki列表GetWikisBySubscription
	 */
	public static final String ACTION_GET_WIKIS_BY_SUBSCRIPTION = "GetWikisBySubscription";
	/**
	 * 28.获得wiki分类筛选条件参数GetCategory
	 */
	public static final String ACTION_GET_CATEGORY = "GetCategory";
	/**
	 * 29.获取频道某天的节目GetDayProgramsByChannel
	 */
	public static final String ACTION_GET_DAY_PROGRAMS_BY_CHANNEL = "GetDayProgramsByChannel";
	/**
	 * 31.根据点播Id获取单条维基详细信息GetWikiByVodId
	 */
	public static final String ACTION_GET_WIKI_BY_VODID = "GetWikiByVodId";
	/**
	 * CH32 DelSubscribeTag【删除订阅标签】
	 */
	public static final String ACTION_DEL_SUBSCRIBE_TAG = "DelSubscribeTag";
	/**
	 * 订阅标签列表
	 */
	public static final String ACTION_GET_SUBSCRIBE_TAGS = "GetSubscribeTags";
	/**
	 * 设备Num
	 */
	public static final String DEVICE_NUM = "123";
	/**
	 * 用户ID
	 */
	public static final String USER_ID = "123";
	/**
	 * APIKEY
	 */
	public static final String APIKEY = "SHMFX2NF";
	/**
	 * SECRETKEY
	 */
	public static final String SECRETKEY = "28a95fbbb5eb415a9736d98929a802c3";
	/**
	 * 欢网请求参数
	 * @return GetHwRequest
	 */
	public static GetHwRequest getHwRequest(){
		GetHwRequest req = new GetHwRequest();
		fillCommonParams(req);
		return req;
	}
	/**
	 * 设置通用的参数
	 * @param req
	 */
	public static void fillCommonParams(GetHwRequest req) {
		req.getDevice().setDnum(DEVICE_NUM);
		req.getUser().setUserid(USER_ID);
	}
	/**
	 * 电影详情。
	 * @param ctx
	 * @param id wikiId
	 * @param title 影片名称
	 */
	public static void openDetail(Context ctx,String id,String title,Wiki data){
		Intent intent = null;
		if(data.getModel().equals("film")){
			intent = new Intent(ctx, DetailActivity.class);
		}else if(data.getModel().equals("teleplay")){
			intent = new Intent(ctx, TVDetailActivity.class);
		}else{
			return;
		}
		intent.putExtra("id", id);
		intent.putExtra("name", title);
		intent.putExtra("data", data);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
	/**
	 * 电影详情。
	 * @param ctx
	 * @param id wikiId
	 * @param title 影片名称
	 */
	public static void openDetail(Context ctx,String id,String title,String playType,boolean isVodId){
		Intent intent = new Intent();
		if(playType.equals("0")){
			intent = new Intent(ctx, DetailActivity.class);
		}else{
			intent = new Intent(ctx, TVDetailActivity.class);
		}
		intent.putExtra("id", id);
		intent.putExtra("name", title);
		intent.putExtra("isVodId", isVodId);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
	/**
	 * 搜索
	 * @param ctx
	 */
	public static void openSearchPage(Context ctx){
		Intent intent = new Intent(ctx, SearchPage.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
	/**
	 * 标签订阅
	 * @param ctx
	 * @param tag
	 */
	public static void openVodTabActivity(Context ctx,String tag){
		Intent intent = new Intent(ctx, VodTabActivity.class);
		intent.putExtra("tag", tag);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
	/**
	 * 明星秀
	 * @param ctx
	 * @param wikiId
	 */
	public static void openStarShowActivity(Context ctx,String wikiId){
		if(TextUtils.isEmpty(wikiId)){
			Tools.showToastMessage(ctx, "暂无演员详情");
			return;
		}
		Intent intent = new Intent(ctx, StarShowActivity.class);
		intent.putExtra("wikiId", wikiId);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
}
