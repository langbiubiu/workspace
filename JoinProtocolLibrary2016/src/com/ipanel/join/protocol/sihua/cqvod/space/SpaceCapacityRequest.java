package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;



@Root(name="message")
public class SpaceCapacityRequest implements Serializable{
	

	/**
	 * 
	 */
	private static final long serialVersionUID = -2712477972250145658L;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private CapacityBody body;
	
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

	public CapacityBody getBody() {
		return body;
	}

	public void setBody(CapacityBody body) {
		this.body = body;
	}

	@Root(name="body",strict=false)
	public static class CapacityBody implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = 2950812162723913840L;
		@Element
		private Queries queries;
		public Queries getQueries() {
			return queries;
		}
		public void setQueries(Queries queries) {
			this.queries = queries;
		}
			
	}
	
	@Root(name="queries",strict=false)
	public static class Queries implements Serializable{

		/**
		 * 
		 */
		private static final long serialVersionUID = -7143669875253742068L;
		@Element
		private CapacityQuery query;
		public CapacityQuery getQuery() {
			return query;
		}
		public void setQuery(CapacityQuery query) {
			this.query = query;
		}
		
	}
}
