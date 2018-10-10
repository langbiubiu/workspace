package com.ipanel.join.protocol.huawei.yn;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "Header")
public class Header implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8062959964542743133L;
	@Attribute(required = false)
	private String action;
	@Attribute(required = false)
	private String command;

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

	public static Header getOneInstance(String command) {
		Header header = new Header();
		header.setAction("REQUEST");
		header.setCommand(command);
		return header;
	}

}
