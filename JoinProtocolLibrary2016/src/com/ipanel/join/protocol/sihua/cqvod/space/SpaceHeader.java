package com.ipanel.join.protocol.sihua.cqvod.space;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@SuppressWarnings("serial")
@Root(name = "header")
public class SpaceHeader implements Serializable{

	@Attribute(required=false)
	private String CorrelateID;
	@Attribute(required=false)
	private String RequestSystemID;
	@Attribute(required=false)
	private String TargetSystemID;
	@Attribute(required=false)
	private String Action;
	@Attribute(required=false)
	private String Command;
	@Attribute(required=false)
	private String Timestamp;
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
	public String getTimestamp() {
		return Timestamp;
	}
	public void setTimestamp(String timestamp) {
		Timestamp = timestamp;
	}
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return "Header [CorrelateID="+CorrelateID+" RequestSystemID="+RequestSystemID
				+" TargetSystemID="+TargetSystemID+" Action="+Action
				+" Command="+Command+" Timestamp="+Timestamp+"]";
	}
	
}
