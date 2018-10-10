package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;


@Root(name="message")
public class SpaceCapacity implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -8105886429889766053L;
	@Attribute(required=false)
	private String version;
	@Element(required=false)
	private SpaceHeader header;
	@Element(required=false)
	private CapacityBody  body;
	
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
		private static final long serialVersionUID = 4787863750371431400L;
		@Element
		private SpaceMessageList spaceMessageList;

		public SpaceMessageList getSpaceMessageList() {
			return spaceMessageList;
		}

		public void setSpaceMessageList(SpaceMessageList spaceMessageList) {
			this.spaceMessageList = spaceMessageList;
		}
		
			
	}
	
	@Root(name="spaceMessageList",strict=false)
	public static class SpaceMessageList implements Serializable{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3054103131767953210L;
		@Element(required=false)
		private SpaceMessage spaceMessage;

		public SpaceMessage getSpaceMessage() {
			return spaceMessage;
		}

		public void setSpaceMessage(SpaceMessage spaceMessage) {
			this.spaceMessage = spaceMessage;
		}
		
	}
}
