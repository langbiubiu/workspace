package ngbj.ipaneltv.dvb;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class NgbJSITransportStream implements Parcelable{
	
	private int networkid;
	private int onid;
	

	private int tsid;
	private int[] bouquetids;
	
	/**
	 * descTags中的下表和descDatas的下表对应。 
	 */
	private int[] descTags;
	private ArrayList<Integer> descDatasLength;
	

	private ArrayList<byte[]> descDatas;
	
	public NgbJSITransportStream(){}
	
	public NgbJSITransportStream(int networkid,	int onid, int tsid,
			int[] bouquetids, int[] descTags, ArrayList<byte[]> descDatas){
		this.networkid = networkid;
		this.onid = onid;
		this.tsid = tsid;
		this.bouquetids = bouquetids;
		this.descTags = descTags;
		this.descDatas = descDatas;
	}
	
	public int getNetworkid() {
		return networkid;
	}

	public int getOnid() {
		return onid;
	}

	public int getTsid() {
		return tsid;
	}

	public int[] getBouquetids() {
		return bouquetids;
	}

	public int[] getDescTags() {
		return descTags;
	}
	
	public void setNetworkid(int networkid) {
		this.networkid = networkid;
	}

	public void setOnid(int onid) {
		this.onid = onid;
	}

	public void setTsid(int tsid) {
		this.tsid = tsid;
	}

	public void setBouquetids(int[] bouquetids) {
		this.bouquetids = bouquetids;
	}

	public void setDescTags(int[] descTags) {
		this.descTags = descTags;
	}

	public void setDescDatas(ArrayList<byte[]> descDatas) {
		this.descDatas = descDatas;
	}

	public ArrayList<byte[]> getDescDatas() {
		return descDatas;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public ArrayList<Integer> getDescDatasLength() {
		return descDatasLength;
	}

	public void setDescDatasLength(ArrayList<Integer> descDatasLength) {
		this.descDatasLength = descDatasLength;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(networkid);
		dest.writeInt(onid);
		dest.writeInt(tsid);
		if(bouquetids != null){
			dest.writeInt(bouquetids.length);
			dest.writeIntArray(bouquetids);
		}else dest.writeInt(0);
		if(descTags != null){
		dest.writeInt(descTags.length);
		dest.writeIntArray(descTags);
		}else dest.writeInt(0);
		dest.writeList(descDatasLength);
		for(int i=0;i<descDatas.size();i++){
			dest.writeByteArray(descDatas.get(i));
		}
//		dest.writeList(descDatas);
		
	}
	
	public static final Parcelable.Creator<NgbJSITransportStream> CREATOR = new Parcelable.Creator<NgbJSITransportStream>() {
		public NgbJSITransportStream createFromParcel(Parcel in) {
			NgbJSITransportStream sits = new NgbJSITransportStream();
			sits.networkid = in.readInt();
			sits.onid = in.readInt();
			sits.tsid = in.readInt();
			int bouquetidsLength = in.readInt();
			if(bouquetidsLength>0){
				sits.bouquetids = new int[bouquetidsLength];
				in.readIntArray(sits.bouquetids);
			}
			int descTagsLength = in.readInt();
			if(descTagsLength > 0){
				sits.descTags = new int[descTagsLength];
				in.readIntArray(sits.descTags);
			}
			List<Integer> descDatasLength_1 = in.readArrayList(Integer.class.getClassLoader());
			sits.descDatas = new ArrayList<byte[]>();
			for(int i=0;i<descDatasLength_1.size();i++){
				byte[] b = new byte[descDatasLength_1.get(i)];
				in.readByteArray(b);
				sits.descDatas.add(b);
			}
//			sits.descDatas = in.readArrayList(byte[].class.getClassLoader());
			return sits;
		}

		public NgbJSITransportStream[] newArray(int size) {
			return new NgbJSITransportStream[size];
		}
	};
}
