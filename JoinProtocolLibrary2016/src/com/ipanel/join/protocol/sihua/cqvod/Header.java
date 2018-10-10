package com.ipanel.join.protocol.sihua.cqvod;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;
@Root(name = "header")
public class Header implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2892202665316184934L;
	@Attribute(required=false)
	private String action;
	@Attribute(required=false)
	private String command;
	@Attribute(required=false,name="component-type")
	private String componentType;
	@Attribute(required=false,name="component-id")
	private String componentId;
	@Attribute(required=false,name="sequence")
	private String sequence;
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getCommand() {
		return command;
	}
	public void setCommand(String command) {
		this.command = command;
	}
	public String getComponentType() {
		return componentType;
	}
	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}
	public String getComponentId() {
		return componentId;
	}
	public void setComponentId(String componentId) {
		this.componentId = componentId;
	}
	public String getSequence() {
		return sequence;
	}
	public void setSequence(String sequence) {
		this.sequence = sequence;
	}
	@Override
	public String toString() {
		return "Header [action=" + action + ", command=" + command + ", componentType=" + componentType
				+ ", componentId=" + componentId + ", sequence=" + sequence + "]";
	}
	
	

}
