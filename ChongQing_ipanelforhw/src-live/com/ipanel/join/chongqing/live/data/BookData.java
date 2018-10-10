package com.ipanel.join.chongqing.live.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.ipanel.join.chongqing.live.navi.DatabaseObjects.LiveProgramEvent;
import com.ipanel.join.chongqing.live.provider._Field;
import com.ipanel.join.chongqing.live.provider._Table;

@_Table(table = "bookchannels")
public class BookData implements Parcelable {
	@_Field(field = "frequency")
	private long frequency;
	@_Field(field = "program_number")
	private int program_number;
	@_Field(field = "event_name")
	private String event_name;
	@_Field(field = "start_time")
	private String start_time;
	@_Field(field = "duration")
	private String duration;
	@_Field(field = "channel_name")
	private String channel_name;
	@_Field(field = "channel_number")
	private String channel_number;
	private int status;
	private int type;

	public long getFrequency() {
		return frequency;
	}

	public void setFrequency(long frequency) {
		this.frequency = frequency;
	}

	public int getProgram_number() {
		return program_number;
	}

	public void setProgram_number(int program_number) {
		this.program_number = program_number;
	}

	public String getEvent_name() {
		return event_name;
	}

	public void setEvent_name(String event_name) {
		this.event_name = event_name;
	}

	public String getStart_time() {
		return start_time;
	}

	public void setStart_time(String start_time) {
		this.start_time = start_time;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public String getChannel_name() {
		return channel_name;
	}

	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}

	public String getChannel_number() {
		return channel_number;
	}

	public void setChannel_number(String channel_number) {
		this.channel_number = channel_number;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public BookData(long frequency, int program_number, String event_name, String start_time,
			String duration, String channel_name, String channel_number, int type) {
		super();
		this.frequency = frequency;
		this.program_number = program_number;
		this.event_name = event_name;
		this.start_time = start_time;
		this.duration = duration;
		this.channel_name = channel_name;
		this.channel_number = channel_number;
		this.type = type;
	}

	public BookData(LiveProgramEvent e, String channel_name, String channel_number, int status) {
		super();
		this.frequency = e.getChannelKey().getFrequency();
		this.program_number = e.getChannelKey().getProgram();
		this.event_name = e.getName();
		this.start_time = e.getStart() + "";
		this.duration = e.getEnd() - e.getStart() + "";
		this.channel_name = channel_name;
		this.channel_number = channel_number;
		this.status = status;
	}

	public BookData() {

	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeLong(this.frequency);
		dest.writeInt(this.program_number);
		dest.writeString(this.event_name);
		dest.writeString(this.start_time);
		dest.writeString(this.duration);
		dest.writeString(this.channel_name);
		dest.writeString(this.channel_number);
		dest.writeInt(this.type);

	}

	// 添加一个静态成员,名为CREATOR,该对象实现了Parcelable.Creator接口
	public static final Parcelable.Creator<BookData> CREATOR = new Parcelable.Creator<BookData>() {
		@Override
		public BookData createFromParcel(Parcel source) {
			// 从Parcel中读取数据，返回person对象
			return new BookData(source.readLong(), source.readInt(), source.readString(),
					source.readString(), source.readString(), source.readString(),
					source.readString(), source.readInt());
		}

		@Override
		public BookData[] newArray(int size) {
			return new BookData[size];
		}
	};

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + program_number;
		result = prime * result + ((start_time == null) ? 0 : start_time.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		BookData other = (BookData) obj;
		if (program_number != other.program_number)
			return false;
		if (start_time == null) {
			if (other.start_time != null)
				return false;
		} else if (!start_time.equals(other.start_time))
			return false;
		return true;
	}

}
