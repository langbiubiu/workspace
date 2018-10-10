package com.ipanel.hengyun.message;

import java.io.Serializable;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name = "event", strict = false)
public class Event implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1340961145834970764L;
	
	@Element(name = "type")
	public String type;
	
	@Element(name = "subtype")
	public int subType;
	
	@Element(name = "SessionId", required = false)
	public String sessionId;
	
	@Element(name = "level")
	public String level;
	
	@Element(name = "privateData", required = false)
	public String privateData;

	public String toString() {
		return "type = " + type + "\nsubType = " + subType + "\nsessionId = " + sessionId
				+ "\nlevel = " + level + "\nprivateData = " + privateData;
	}
	
}
