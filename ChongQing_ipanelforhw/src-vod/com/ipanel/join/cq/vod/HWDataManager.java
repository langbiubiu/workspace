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
 * �������ݹ���
 *
 */
public class HWDataManager {
	/**
	 * �������Ե�ַ
	 */
	public static final String ROOT_URL = "http://117.59.6.101/json";
	/**
	 * 1.��ȡƵ���б�GetChannels
	 */
	public static final String ACTION_GET_CHANNELS = "GetChannels";
	/**
	 * 2.��ȡֱ���Ƽ�GetLiveProgramsByRecommend
	 */
	public static final String ACTION_GET_LIVE_PROGRAM_BY_RECOMMEND = "GetLiveProgramsByRecommend";
	/**
	 * 3.��ȡ��Ҷ��ڿ�GetWikisByHot
	 */
	public static final String ACTION_GET_WIKIS_BY_HOT = "GetWikisByHot";
	/**
	 * ��ȡwiki����
	 */
	public static final String ACTION_GET_WIKI_INFO = "GetWikiInfo";
	/**
	 * ��ȡ���°��б�
	 */
	public static final String ACTION_GET_WIKIS_BY_NEW = "GetWikisByNew";
	/**
	 * ��ȡ�Ȳ����б�
	 */
	public static final String ACTION_GET_WIKIS_BY_HIT = "GetWikisByHit";
	/**
	 * ��ȡ��ע�����ǰ��б�
	 */
	public static final String ACTION_GET_ACTORS_BY_FOLLOW = "GetActorsByFollow";
	/**
	 * ��ȡ�ղذ��б�
	 */
	public static final String ACTION_GET_WIKIS_BY_FAV = "GetWikisByFav";
	/**
	 * ��ȡ��Ա����ҳ��
	 */
	public static final String ACTION_GET_ACTOR_INFO = "GetActorInfo";
	/**
	 * wiki����ɸѡ
	 */
	public static final String ACTION_GET_WIKIS_BY_CATEGORY = "GetWikisByCategory";
	/**
	 * ����ά�������ࡢ ����ĸ�� �ؼ��֣�
	 */
	public static final String ACTION_SEARCH_WIKIS = "SearchWikis";
	/**
	 * ��ȡ���������ؼ����б�
	 */
	public static final String ACTION_GET_SEARCH_KEYWORD = "GetSearchKeyword";
	/**
	 * 14.��ȡWiki�����б�GetSimilarWikis
	 */
	public static final String ACTION_GET_SIMILAR_WIKIS = "GetSimilarWikis";
	/**
	 * 15.��ȡά������Ա�б�GetActorsByWiki(�ϳ�)
	 */
	public static final String ACTION_GET_ACTORS_BY_WIKI = "GetActorsByWiki";
	/**
	 * 17.��ȡ�û�ϲ����Ƶ��GetFavChannelsByUser
	 */
	public static final String ACTION_GET_FAV_CHANNELS_BY_USER = "GetFavChannelsByUser";
	/**
	 * 18.ɾ���ҵ��ղ�DelFavChannelByUser
	 */
	public static final String ACTION_DEL_FAV_CHANNELS_BY_USER = "DelFavChannelByUser";
	/**
	 * 19.���wiki�ղ�SetFavWikiByUser
	 */
	public static final String ACTION_SET_FAV_WIKI_BY_USER = "SetFavWikiByUser";
	/**
	 * 20.��ȡ���ղص�wiki�б��ɵ㲥��GetFavWikisByUser
	 */
	public static final String ACTION_GET_FAV_WIKIS_BY_USER = "GetFavWikisByUser";
	/**
	 * 21.ɾ���ҵ�wiki�ղ�DelFavWiki
	 */
	public static final String ACTION_DEL_FAV_WIKI = "DelFavWiki";
	/**
	 * 22.����wiki����(������)SetWikiInteractive
	 */
	public static final String ACTION_SET_WIKI_INTERACTIVE = "SetWikiInteractive";
	/**
	 * 23.��ӹ�ע��ԱSetFollowActor
	 */
	public static final String ACTION_SET_FOLLOW_ACTOR = "SetFollowActor";
	/**
	 * 24.��ȡ�ҹ�ע����Ա�б�GetMyFollowActors
	 */
	public static final String ACTION_GET_MY_FOLLOW_ACTORS = "GetMyFollowActors";
	/**
	 * 25.ɾ���ҹ�ע����ԱDelFollowActor
	 */
	public static final String ACTION_DEL_FOLLOW_ACTOR = "DelFollowActor";
	/**
	 * 26.��Ӷ��ı�ǩSetSubscribeTag
	 */
	public static final String ACTION_SET_SUBSCIRBE_TAG = "SetSubscribeTag";
	/**
	 * 27.��ȡ�Ҷ��ĵ�wiki�б�GetWikisBySubscription
	 */
	public static final String ACTION_GET_WIKIS_BY_SUBSCRIPTION = "GetWikisBySubscription";
	/**
	 * 28.���wiki����ɸѡ��������GetCategory
	 */
	public static final String ACTION_GET_CATEGORY = "GetCategory";
	/**
	 * 29.��ȡƵ��ĳ��Ľ�ĿGetDayProgramsByChannel
	 */
	public static final String ACTION_GET_DAY_PROGRAMS_BY_CHANNEL = "GetDayProgramsByChannel";
	/**
	 * 31.���ݵ㲥Id��ȡ����ά����ϸ��ϢGetWikiByVodId
	 */
	public static final String ACTION_GET_WIKI_BY_VODID = "GetWikiByVodId";
	/**
	 * CH32 DelSubscribeTag��ɾ�����ı�ǩ��
	 */
	public static final String ACTION_DEL_SUBSCRIBE_TAG = "DelSubscribeTag";
	/**
	 * ���ı�ǩ�б�
	 */
	public static final String ACTION_GET_SUBSCRIBE_TAGS = "GetSubscribeTags";
	/**
	 * �豸Num
	 */
	public static final String DEVICE_NUM = "123";
	/**
	 * �û�ID
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
	 * �����������
	 * @return GetHwRequest
	 */
	public static GetHwRequest getHwRequest(){
		GetHwRequest req = new GetHwRequest();
		fillCommonParams(req);
		return req;
	}
	/**
	 * ����ͨ�õĲ���
	 * @param req
	 */
	public static void fillCommonParams(GetHwRequest req) {
		req.getDevice().setDnum(DEVICE_NUM);
		req.getUser().setUserid(USER_ID);
	}
	/**
	 * ��Ӱ���顣
	 * @param ctx
	 * @param id wikiId
	 * @param title ӰƬ����
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
	 * ��Ӱ���顣
	 * @param ctx
	 * @param id wikiId
	 * @param title ӰƬ����
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
	 * ����
	 * @param ctx
	 */
	public static void openSearchPage(Context ctx){
		Intent intent = new Intent(ctx, SearchPage.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
	/**
	 * ��ǩ����
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
	 * ������
	 * @param ctx
	 * @param wikiId
	 */
	public static void openStarShowActivity(Context ctx,String wikiId){
		if(TextUtils.isEmpty(wikiId)){
			Tools.showToastMessage(ctx, "������Ա����");
			return;
		}
		Intent intent = new Intent(ctx, StarShowActivity.class);
		intent.putExtra("wikiId", wikiId);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		ctx.startActivity(intent);
	}
}
