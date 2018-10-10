package com.ipanel.join.cq.sihua.data;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.ipanel.join.protocol.sihua.cqvod.space.SpaceHeader;



@Root(name="message")
public class OrderRequest implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -2712477972250145658L;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private OrdersBody body;
	
	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public SpaceHeader getHeader() {
		return header;
	}

	public void setHeader(SpaceHeader header) {
		this.header = header;
	}

	public OrdersBody getBody() {
		return body;
	}

	public void setBody(OrdersBody body) {
		this.body = body;
	}

	@Root(name="body",strict=false)
	public static class OrdersBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 2950812162723913840L;
		@Element
		private Orders orders;
		public Orders getOrders() {
			return orders;
		}
		public void setOrders(Orders orders) {
			this.orders = orders;
		}
			
	}
	
	@Root(name="queries",strict=false)
	public static class Orders implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -7143669875253742068L;
		@Element
		private Order order;
		public Order getOrder() {
			return order;
		}
		public void setOrder(Order order) {
			this.order = order;
		}
		
	}
}
