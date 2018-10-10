package com.ipanel.join.protocol.cq.domain;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "header")
public class Header implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8062959964542743133L;
	@Attribute(required = false)
	private String Action;
	@Attribute(required = false)
	private String Command;
	@Attribute(required = false)
	private String CorrelateID;
	@Attribute(required = false)
	private String RequestSystemID;
	@Attribute(required = false)
	private String TargetSystemID;
	@Attribute(required = false)
	private String Timestamp;

	public String getAction() {
		return Action;
	}

	public void setAction(String action) {
		Action = action;
	}

	public String getCommand() {
		return Command;
	}

	public void setCommand(String command) {
		Command = command;
	}

	public String getCorrelateID() {
		return CorrelateID;
	}

	public void setCorrelateID(String correlateID) {
		CorrelateID = correlateID;
	}

	public String getRequestSystemID() {
		return RequestSystemID;
	}

	public void setRequestSystemID(String requestSystemID) {
		RequestSystemID = requestSystemID;
	}

	public String getTargetSystemID() {
		return TargetSystemID;
	}

	public void setTargetSystemID(String targetSystemID) {
		TargetSystemID = targetSystemID;
	}

	public String getTimestamp() {
		return Timestamp;
	}

	public void setTimestamp(String timestamp) {
		Timestamp = timestamp;
	}
	
	public static Header getOneInstance(String command){
		Header header=new Header();
		header.setAction("REQUEST");
		header.setCommand(command);
		header.setRequestSystemID("iSpace");
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		header.setTimestamp(formatter.format(new Date()));
		header.setCorrelateID("1000002");
		header.setTargetSystemID("NPVR");
		return header;
	}

}
