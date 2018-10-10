package com.ipanel.join.cq.homed.tvcloud.db;

public class RecordEvent {
	
	public long event_id;
	public String event_name;
	long start_time;
	int event_size;
	String channel_name;
	
	public long getEvent_id() {
		return event_id;
	}
	public void setEvent_id(long event_id) {
		this.event_id = event_id;
	}
	public String getEvent_name() {
		return event_name;
	}
	public void setEvent_name(String event_name) {
		this.event_name = event_name;
	}
	public long getStart_time() {
		return start_time;
	}
	public void setStart_time(long start_time) {
		this.start_time = start_time;
	}
	public int getEvent_size() {
		return event_size;
	}
	public void setEvent_size(int event_size) {
		this.event_size = event_size;
	}
	
	@Override
	public String toString() {
		return "RecordEvent [event_id=" + event_id + ", event_name="
				+ event_name + ", start_time=" + start_time + ", event_size="
				+ event_size + "]";
	}
	public String getChannel_name() {
		return channel_name;
	}
	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}
	
}
