package com.ipanel.join.cq.sihua.data;


import android.text.TextUtils;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.join.cq.sihua.data.OrderRequest.Orders;
import com.ipanel.join.cq.sihua.data.OrderRequest.OrdersBody;
import com.ipanel.join.protocol.sihua.cqvod.space.CapacityQuery;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentDeleteRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentDeleteRequest.ContentDeleteRequestBody;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentDeleteRequest.ContentDeleteRequestContent;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentDeleteRequest.ContentDeleteRequestContents;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentDeleteRequest.ContentDeleteRequestRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceSearchRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentRequest.ContentDetailRequestBody;
import com.ipanel.join.protocol.sihua.cqvod.space.ContentRequest.ContentDetailRequestQuery;
import com.ipanel.join.protocol.sihua.cqvod.space.PlayUrlRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.PlayUrlRequest.PlayUrlDetailRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.PlayUrlRequest.PlayUrlDetailRequestBody;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceCapacityRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceCapacityRequest.CapacityBody;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceCapacityRequest.Queries;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceSearchRequest.SearchBody;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceSearchRequest.SearchHeader;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceSearchRequest.SearchRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.SpaceHeader;
import com.ipanel.join.protocol.sihua.cqvod.space.TaskRequest;
import com.ipanel.join.protocol.sihua.cqvod.space.TaskRequest.DetailRequestBody;
import com.ipanel.join.protocol.sihua.cqvod.space.TaskRequest.DetailRequestQuery;

/**
 * @author Administrator
 *
 */
public class SpaceRequest {

	private static String normalUUID = "1500968038";
	//空间容量查询
	public static SpaceCapacityRequest setSpaceCapacityRequest(){
		SpaceCapacityRequest spaceCapacityRequest = new SpaceCapacityRequest();
		spaceCapacityRequest.setVersion("1.0");
		
		SpaceHeader spaceHeader = new SpaceHeader();
		spaceHeader.setCorrelateID("1000002");
		spaceHeader.setRequestSystemID("iSpace");
		spaceHeader.setTargetSystemID("NPVR");
		spaceHeader.setAction("REQUEST");
		spaceHeader.setCommand("USER_SPACE_PROFILE_QUERY");
		spaceHeader.setTimestamp("2014-4-1 00:00:00");
		spaceCapacityRequest.setHeader(spaceHeader);
		
		CapacityBody capacityBody = new CapacityBody();
		Queries queries = new Queries();
		CapacityQuery query = new CapacityQuery();
		query.setUUID(TextUtils.isEmpty(CQApplication.getInstance().getUid())?normalUUID:CQApplication.getInstance().getUid());
		query.setSPID("sp_00001");
		query.setAppID("app_00001");
		query.setAccessToken(CQApplication.getInstance().getAuthToken());
		queries.setQuery(query);
		capacityBody.setQueries(queries);
		spaceCapacityRequest.setBody(capacityBody);
		
		return spaceCapacityRequest;
		
	}
	//任务查询请求参数
	public static TaskRequest setTaskRequest(String pageIndex){
		TaskRequest taskRequest = new TaskRequest();
		taskRequest.setVersion("1.0");
		
		SpaceHeader spaceHeader = new SpaceHeader();
		spaceHeader.setCorrelateID("1000002");
		spaceHeader.setRequestSystemID("ID");
		spaceHeader.setTargetSystemID("ID");
		spaceHeader.setAction("REQUEST");
		spaceHeader.setCommand("ISPACE_USER_TASK_QUERY");
		spaceHeader.setTimestamp("2014-4-1 00:00:00");
		taskRequest.setHeader(spaceHeader);
		
		DetailRequestBody detailRequestBody = new DetailRequestBody();
		DetailRequestQuery detailRequestQuery = new DetailRequestQuery();
		detailRequestQuery.setUUID(TextUtils.isEmpty(CQApplication.getInstance().getUid())?normalUUID:CQApplication.getInstance().getUid());
		detailRequestQuery.setSPID("sp_00001");
		detailRequestQuery.setAppID("app_00001");
		detailRequestQuery.setAccessToken(CQApplication.getInstance().getAuthToken());
		detailRequestQuery.setStatus("2");//进行中
		detailRequestQuery.setType("1");
		detailRequestQuery.setPageNo(pageIndex);
		detailRequestQuery.setPageSize("99");
		detailRequestBody.setQuery(detailRequestQuery);
		taskRequest.setBody(detailRequestBody);
		
		return taskRequest;
		
	}
	
	
	//内容查询请求参数
	public static ContentRequest setContentRequest(String pageIndex,String type,String fileType,String sortType){
	
		ContentRequest contentRequest = new ContentRequest();
		contentRequest.setVersion("1.0");
		
		SpaceHeader spaceHeader = new SpaceHeader();
		spaceHeader.setCorrelateID("1000002");
		spaceHeader.setRequestSystemID("iSpace");
		spaceHeader.setTargetSystemID("NPVR");
		spaceHeader.setAction("REQUEST");
		spaceHeader.setCommand("ISPACE_USER_CONTENT_QUERY");
		spaceHeader.setTimestamp("2014-4-1 00:00:00");
		contentRequest.setHeader(spaceHeader);
		
		ContentDetailRequestBody contentDetailRequestBody = new ContentDetailRequestBody();
		ContentDetailRequestQuery contentDetailRequestQuery = new ContentDetailRequestQuery();
		contentDetailRequestQuery.setUUID(TextUtils.isEmpty(CQApplication.getInstance().getUid())?normalUUID:CQApplication.getInstance().getUid());
		contentDetailRequestQuery.setSPID("sp_00001");
		contentDetailRequestQuery.setAppID("app_00001");
		contentDetailRequestQuery.setAccessToken(CQApplication.getInstance().getAuthToken());
		contentDetailRequestQuery.setType(type);
		contentDetailRequestQuery.setFileType(fileType);
		contentDetailRequestQuery.setSortType(sortType);
		contentDetailRequestQuery.setStatus("0");
		contentDetailRequestQuery.setPageNo(pageIndex);
		if(fileType.equals("1")){
			contentDetailRequestQuery.setPageSize("99");
		}else if(fileType.equals("3")){
			contentDetailRequestQuery.setPageSize("12");
		}		
		contentDetailRequestBody.setQuery(contentDetailRequestQuery);
		contentRequest.setBody(contentDetailRequestBody);
		
		return contentRequest;
		
	}
	
	//云盘内容播放地址查询
	public static PlayUrlRequest setPlayUrlRequest(String contentID){
		PlayUrlRequest playUrlRequest = new PlayUrlRequest();
		playUrlRequest.setVersion("1.0");
		
		SpaceHeader spaceHeader = new SpaceHeader();
		spaceHeader.setCorrelateID("1000002");
		spaceHeader.setRequestSystemID("iSpace");
		spaceHeader.setTargetSystemID("NPVR");
		spaceHeader.setAction("REQUEST");
		spaceHeader.setCommand("ISPACE_CONTENT_PLAYURL_QUERY");
		spaceHeader.setTimestamp("2014-4-1 00:00:00");
		playUrlRequest.setHeader(spaceHeader);
		
		PlayUrlDetailRequestBody body = new PlayUrlDetailRequestBody();		
		PlayUrlDetailRequest request = new PlayUrlDetailRequest();
		request.setUuid(TextUtils.isEmpty(CQApplication.getInstance().getUid())?normalUUID:CQApplication.getInstance().getUid());
		request.setSpId("sp_00001");
		request.setAppId("app_00001");
		request.setAccessToken(CQApplication.getInstance().getAuthToken());
		request.setPlayTerminalApp("0");
		request.setContentID(contentID);
		request.setIsIPQAM("1");
//		request.setProtocol("http");
		request.setIsHD("");
		request.setCodec("");
		request.setOtherParam("");
		body.setRequest(request);
		playUrlRequest.setBody(body);
		return playUrlRequest;
	}
	
	public static SpaceSearchRequest setSearchRequesParameters(String keywords){
		SpaceSearchRequest spaceSearchRequest = new SpaceSearchRequest();
		SearchHeader header = new SearchHeader();
		spaceSearchRequest.setHeader(header);
		
		SearchBody body = new SearchBody();
		SearchRequest request = new SearchRequest();
		request.setUuid(TextUtils.isEmpty(CQApplication.getInstance().getUid())?normalUUID:CQApplication.getInstance().getUid());
		request.setSearchType("1");
		request.setMatchType("1");
		request.setSearchName(keywords);
		request.setOtherParam("");
		request.setCommand("USER_ISPACE_CONTENT_QUERY");
		body.setRequest(request);
		spaceSearchRequest.setBody(body);
		
		return spaceSearchRequest;
	}
	
	//内容删除接口
	public static ContentDeleteRequest setContentDeleteRequest(String contentID){
		ContentDeleteRequest contentDeleteRequest = new ContentDeleteRequest();
		SpaceHeader spaceHeader = new SpaceHeader();
		spaceHeader.setCorrelateID("1000002");
		spaceHeader.setRequestSystemID("iSpace");
		spaceHeader.setTargetSystemID("NPVR");
		spaceHeader.setAction("REQUEST");
		spaceHeader.setCommand("ISPACE_USER_CONTENT_DELETE");
		spaceHeader.setTimestamp("2014-4-1 00:00:00");
		contentDeleteRequest.setHeader(spaceHeader);
		
		ContentDeleteRequestBody body = new ContentDeleteRequestBody();
		ContentDeleteRequestRequest request = new ContentDeleteRequestRequest();
		request.setAction("PhsicalDelete");
		request.setUUID(TextUtils.isEmpty(CQApplication.getInstance().getUid())?normalUUID:CQApplication.getInstance().getUid());
		request.setSPID("sp_00001");
		request.setAppID("app_00001");
		request.setAccessToken(CQApplication.getInstance().getUid());
		
		ContentDeleteRequestContents contents = new ContentDeleteRequestContents();
		ContentDeleteRequestContent content =new ContentDeleteRequestContent();
		content.setContentID(contentID);
		contents.setContent(content);
		
		request.setContents(contents);
		body.setRequest(request);
		contentDeleteRequest.setBody(body);
		
		return contentDeleteRequest;
		
	}
	
	//订录接口(订录删除接口)
	public static OrderRequest setOrderRequest(String channelID,String programID,String orderTime,String action){
		OrderRequest orderRequest = new OrderRequest();
		orderRequest.setVersion("1.0");
		
		SpaceHeader spaceHeader = new SpaceHeader();
		spaceHeader.setCorrelateID("1000002");
		spaceHeader.setRequestSystemID("iSpace");
		spaceHeader.setTargetSystemID("NPVR");
		spaceHeader.setAction("REQUEST");
		spaceHeader.setCommand("NPVR_ORDER_REQUEST");
		spaceHeader.setTimestamp("2014-4-1 00:00:00");
		orderRequest.setHeader(spaceHeader);
		
		OrdersBody body = new OrdersBody();
		Orders orders = new Orders();
		Order order = new Order();
		order.setAction(action.equals("0")?"REGIST":"DELETE");
		order.setUUID(TextUtils.isEmpty(CQApplication.getInstance().getUid())?normalUUID:CQApplication.getInstance().getUid());
		order.setSPID("sp_00001");
		order.setAppID("app_00001");
		order.setCode("000011");
		order.setChannelID(channelID);
		order.setRecordType("1");
		order.setProgramID(programID);
		order.setAccessToken(CQApplication.getInstance().getUid());
		order.setOrderTime(orderTime);
		orders.setOrder(order);
		body.setOrders(orders);
		orderRequest.setBody(body);
		return orderRequest;
	}
}








