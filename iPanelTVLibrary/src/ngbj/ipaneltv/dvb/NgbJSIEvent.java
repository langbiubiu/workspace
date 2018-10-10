package ngbj.ipaneltv.dvb;

import java.util.ArrayList;
import java.util.List;

import android.R.integer;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class NgbJSIEvent implements Parcelable {

	private int onid;
	private int tsid;
	private int serviceId;
	private int eventId;
	private long duration;
	private long startTime;
	private String name;
	private String shortDescription;
	private byte[] nibbles;
	

	private String languageCode;
	private int isFreeCAMode;// 取值为0或1
	private int runingstatus;
	private int sectionnumber;

	/**
	 * descTags中的下表和descDatas的下表对应。
	 */
	private int[] descTags;
	private ArrayList<Integer> descDatasLength;
	private ArrayList<byte[]> descDatas;

	public NgbJSIEvent() {
	}

	public NgbJSIEvent(int onid, int tsid, int serviceId, int eventId, long duration,
			long startTime, String name, String shortDescription, byte[] nibbles,
			String languageCode, int isFreeCAMode, int runingstatus, int sectionnumber,
			int[] descTags, ArrayList<byte[]> descDatas) {
		this.onid = onid;
		this.tsid = tsid;
		this.serviceId = serviceId;
		this.eventId = eventId;
		this.duration = duration;
		this.startTime = startTime;
		this.name = name;
		this.shortDescription = shortDescription;
		this.nibbles = nibbles;
		this.languageCode = languageCode;
		this.isFreeCAMode = isFreeCAMode;
		this.runingstatus = runingstatus;
		this.sectionnumber = sectionnumber;
		this.descTags = descTags;
		this.descDatas = descDatas;
	}

	

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(onid);
		dest.writeInt(tsid);
		dest.writeInt(serviceId);
		dest.writeInt(eventId);
		dest.writeLong(duration);
		dest.writeLong(startTime);
		dest.writeString(name);
		dest.writeString(shortDescription);
		if(nibbles !=null){
			dest.writeInt(nibbles.length);
			dest.writeByteArray(nibbles);
		}else dest.writeInt(0);
		dest.writeString(languageCode);
		dest.writeInt(isFreeCAMode);
		dest.writeInt(runingstatus);
		dest.writeInt(sectionnumber);
		if(descTags != null){
			dest.writeInt(descTags.length);
			dest.writeIntArray(descTags);
		}else dest.writeInt(0);
		dest.writeList(descDatasLength);
		for(int i=0;i<descDatas.size();i++){
			dest.writeByteArray(descDatas.get(i));
		}
	}

	public static final Parcelable.Creator<NgbJSIEvent> CREATOR = new Parcelable.Creator<NgbJSIEvent>() {
		public NgbJSIEvent createFromParcel(Parcel in) {
			NgbJSIEvent sie = new NgbJSIEvent();
			sie.onid = in.readInt();
			sie.tsid = in.readInt();
			sie.serviceId = in.readInt();
			sie.eventId = in.readInt();
			sie.duration = in.readLong();
			sie.startTime = in.readLong();
			sie.name = in.readString();
			sie.shortDescription = in.readString();
			int nibblesLength = in.readInt();
			if(nibblesLength > 0){
				sie.nibbles = new byte[nibblesLength];
				in.readByteArray(sie.nibbles);
			}
			sie.languageCode = in.readString();
			sie.isFreeCAMode = in.readInt();
			sie.runingstatus = in.readInt();
			sie.sectionnumber = in.readInt();
			int descTagsLength = in.readInt();
			if(descTagsLength > 0){
				sie.descTags = new int[descTagsLength];
				in.readIntArray(sie.descTags);
			}
			List<Integer> descDatasLength_1 = in.readArrayList(Integer.class.getClassLoader());
			sie.descDatas = new ArrayList<byte[]>();
			//FIXME
			for(int i=0;i<descDatasLength_1.size();i++){
				byte[] b = new byte[descDatasLength_1.get(i)];
				in.readByteArray(b);
				sie.descDatas.add(b);
			}
			return sie;
		}

		@Override
		public NgbJSIEvent[] newArray(int size) {
			// TODO Auto-generated method stub
			return new NgbJSIEvent[size];
		}
	};
	
	


	public int getOnid() {
		return onid;
	}

	public void setOnid(int onid) {
		this.onid = onid;
	}

	public int getTsid() {
		return tsid;
	}

	public void setTsid(int tsid) {
		this.tsid = tsid;
	}

	public int getServiceId() {
		return serviceId;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public int getEventId() {
		return eventId;
	}

	public void setEventId(int eventId) {
		this.eventId = eventId;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortDescription() {
		return shortDescription;
	}

	public void setShortDescription(String shortDescription) {
		this.shortDescription = shortDescription;
	}

	public byte[] getNibbles() {
		return nibbles;
	}

	public void setNibbles(byte[] nibbles) {
		this.nibbles = nibbles;
	}

	public String getLanguageCode() {
		return languageCode;
	}

	public void setLanguageCode(String languageCode) {
		this.languageCode = languageCode;
	}

	public int getIsFreeCAMode() {
		return isFreeCAMode;
	}

	public void setIsFreeCAMode(int isFreeCAMode) {
		this.isFreeCAMode = isFreeCAMode;
	}

	public int getRuningstatus() {
		return runingstatus;
	}

	public void setRuningstatus(int runingstatus) {
		this.runingstatus = runingstatus;
	}

	public int getSectionnumber() {
		return sectionnumber;
	}

	public void setSectionnumber(int sectionnumber) {
		this.sectionnumber = sectionnumber;
	}

	public int[] getDescTags() {
		return descTags;
	}

	public void setDescTags(int[] descTags) {
		this.descTags = descTags;
	}

	public ArrayList<Integer> getDescDatasLength() {
		return descDatasLength;
	}

	public void setDescDatasLength(ArrayList<Integer> descDatasLength) {
		this.descDatasLength = descDatasLength;
	}

	public ArrayList<byte[]> getDescDatas() {
		return descDatas;
	}

	public void setDescDatas(ArrayList<byte[]> descDatas) {
		this.descDatas = descDatas;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
	
}
