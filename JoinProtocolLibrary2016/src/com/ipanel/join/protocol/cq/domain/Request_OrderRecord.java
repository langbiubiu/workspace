package com.ipanel.join.protocol.cq.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name = "message")
public class Request_OrderRecord implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8806397142728142097L;
	
	@Element(required = false)
	private Header header;
	@Element(required = false)
	private Body body;
	@Attribute(required = false)
	private String version="1.0";

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public Header getHeader() {
		return header;
	}

	public void setHeader(Header header) {
		this.header = header;
	}
	
	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}
	
	@Root(name = "boty")
	public static class Body implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4256909225202355130L;
		@Element(required = false)
		private Orders orders;
		public Orders getOrders() {
			return orders;
		}
		public void setOrders(Orders orders) {
			this.orders = orders;
		}
		
		public static Body createOneInstace(Order order){
			Orders orders=new Orders();
			orders.setOrderlist(new ArrayList<Order>());
			orders.getOrderlist().add(order);
			Body body=new Body();
			body.setOrders(orders);
			return body;
		}
		
		
	}
	@Root(name = "orders")
	public static class Orders implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -5301075333629026547L;
		@ElementList(inline = true, entry = "order")
		private List<Order> orderlist;
		public List<Order> getOrderlist() {
			return orderlist;
		}
		public void setOrderlist(List<Order> orderlist) {
			this.orderlist = orderlist;
		}
		
	}
	@Root(name = "order")
	public static class Order implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = -8974316535807968463L;
		@Attribute(required = false)
		private String UUID;
		@Attribute(required = false)
		private String SPID;
		@Attribute(required = false)
		private String AppID;
		@Attribute(required = false)
		private String Action;
		@Attribute(required = false)
		private String Code;
		@Attribute(required = false)
		private String ChannelID;
		@Attribute(required = false)
		private String ProgramID;
		@Attribute(required = false)
		private String RecordType;
		@Attribute(required = false)
		private String BeginTime;
		@Attribute(required = false)
		private String EndTime;
		@Attribute(required = false)
		private String OrderTime;
		public String getUUID() {
			return UUID;
		}
		public void setUUID(String uUID) {
			UUID = uUID;
		}
		public String getSPID() {
			return SPID;
		}
		public void setSPID(String sPID) {
			SPID = sPID;
		}
		public String getAppID() {
			return AppID;
		}
		public void setAppID(String appID) {
			AppID = appID;
		}
		public String getAction() {
			return Action;
		}
		public void setAction(String action) {
			Action = action;
		}
		public String getCode() {
			return Code;
		}
		public void setCode(String code) {
			Code = code;
		}
		public String getChannelID() {
			return ChannelID;
		}
		public void setChannelID(String channelID) {
			ChannelID = channelID;
		}
		public String getProgramID() {
			return ProgramID;
		}
		public void setProgramID(String programID) {
			ProgramID = programID;
		}
		public String getRecordType() {
			return RecordType;
		}
		public void setRecordType(String recordType) {
			RecordType = recordType;
		}
		public String getBeginTime() {
			return BeginTime;
		}
		public void setBeginTime(String beginTime) {
			BeginTime = beginTime;
		}
		public String getEndTime() {
			return EndTime;
		}
		public void setEndTime(String endTime) {
			EndTime = endTime;
		}
		public String getOrderTime() {
			return OrderTime;
		}
		public void setOrderTime(String orderTime) {
			OrderTime = orderTime;
		}
		
		
	}
}
