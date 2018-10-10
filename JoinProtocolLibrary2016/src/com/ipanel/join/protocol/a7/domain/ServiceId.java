package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
@Root(name = "ServiceId")
public class ServiceId implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6193120671859875709L;
	@Attribute(required=false)
	private String origin;
	@Attribute(required=false)
	private String name;
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return "ServiceId [origin=" + origin + ", name=" + name + "]";
	}
	
	

}
