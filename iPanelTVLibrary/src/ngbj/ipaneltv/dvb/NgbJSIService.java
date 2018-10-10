package ngbj.ipaneltv.dvb;

import java.util.ArrayList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

public class NgbJSIService implements Parcelable{
	private int logicNum;
	private int networkId;
	private int onid;
	private int tsid;
	private int serviceId;
	private int serviceType;
	private int hasPresentFollowing;//取0 1
	private int hasSchedule;//取0 1
	private int isFreeCAModle;//取0 1
	private int pcrPID;	
	private String[] names;
	private String[] shortNames;
	private String[] providerNames;
	private String[] shortProviderNames;	
	
	/**
	 * descTags中的下表和descDatas的下表对应。 
	 */
	private int[] descTags;
	private ArrayList<Integer> descDatasLength;
	private ArrayList<byte[]> descDatas;
	
	public NgbJSIService(){}
	
	public NgbJSIService(int logicNum,
					int networkId,
	 				int onid,
	 				int tsid,
	 				int serviceId,
	 				int serviceType,
	 				int hasPresentFollowing,
	 				int hasSchedule,
	 				int isFreeCAModle,
	 				int pcrPID,
	 				String[] names,
	 				String[] shortNames,
	 				String[] providerNames,
	 				String[] shortProviderNames,	
	 				int[] descTags,
	 				ArrayList<byte[]> descDatas
	 				){
		this.networkId = networkId;
		this.onid = onid;
		this.tsid = tsid;
		this.serviceId = serviceId;
		this.serviceType = serviceType;
		this.hasPresentFollowing = hasPresentFollowing;
		this.hasSchedule = hasSchedule;
		this.isFreeCAModle = isFreeCAModle;
		this.pcrPID = pcrPID;
		this.names = names;
		this.shortNames = shortNames;
		this.providerNames = providerNames;
		this.shortProviderNames = shortProviderNames;
		this.descTags = descTags;
	}
	
	public int getLogicNum() {
		return logicNum;
	}

	public int getNetworkId() {
		return networkId;
	}

	public int getOnid() {
		return onid;
	}

	public int getTsid() {
		return tsid;
	}

	public int getServiceId() {
		return serviceId;
	}

	public int getServiceType() {
		return serviceType;
	}

	public int isHasPresentFollowing() {
		return hasPresentFollowing;
	}

	public int isHasSchedule() {
		return hasSchedule;
	}

	public int isFreeCAModle() {
		return isFreeCAModle;
	}

	public int getPcrPID() {
		return pcrPID;
	}

	public String[] getNames() {
		return names;
	}

	public String[] getShortNames() {
		return shortNames;
	}

	public String[] getProviderNames() {
		return providerNames;
	}

	public String[] getShortProviderNames() {
		return shortProviderNames;
	}

	public int[] getDescTags() {
		return descTags;
	}
	
	public ArrayList<byte[]> getDescDatas(){
		return descDatas;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(networkId);
		dest.writeInt(onid);
		dest.writeInt(tsid);
		dest.writeInt(serviceId);
		dest.writeInt(serviceType);
		dest.writeInt(hasPresentFollowing);
		dest.writeInt(hasSchedule);
		dest.writeInt(isFreeCAModle);
		dest.writeInt(pcrPID);
		if(names!=null){dest.writeInt(names.length);dest.writeStringArray(names);}else dest.writeInt(0);
		if(shortNames!=null){dest.writeInt(shortNames.length);dest.writeStringArray(shortNames);}else dest.writeInt(0);
		if(providerNames!=null){dest.writeInt(providerNames.length);dest.writeStringArray(providerNames);}else dest.writeInt(0);
		if(shortProviderNames!=null){dest.writeInt(shortProviderNames.length);dest.writeStringArray(shortProviderNames);}else dest.writeInt(0);
		if(descTags!=null){dest.writeInt(descTags.length);dest.writeIntArray(descTags);}else dest.writeInt(0);
		dest.writeList(descDatasLength);
		for(int i=0;i<descDatas.size();i++){
			dest.writeByteArray(descDatas.get(i));
		}
	}
	
	public static final Parcelable.Creator<NgbJSIService> CREATOR = new Parcelable.Creator<NgbJSIService>() {
		public NgbJSIService createFromParcel(Parcel in) {
			NgbJSIService sis = new NgbJSIService();
			sis.networkId = in.readInt();
			sis.onid = in.readInt();
			sis.tsid = in.readInt();
			sis.serviceId = in.readInt();
			sis.serviceType = in.readInt();
			sis.hasPresentFollowing = in.readInt();
			sis.hasSchedule = in.readInt();
			sis.isFreeCAModle = in.readInt();
			sis.pcrPID = in.readInt();
			int namesLength = in.readInt();
			if(namesLength >0){sis.names = new String[namesLength];in.readStringArray(sis.names);}
			int shortNamesLength = in.readInt();
			if(shortNamesLength >0){sis.shortNames = new String[shortNamesLength];in.readStringArray(sis.shortNames);}
			int providerNamesLength = in.readInt();
			if(providerNamesLength >0){sis.providerNames = new String[providerNamesLength];in.readStringArray(sis.providerNames);}
			int shortProviderNamesLength = in.readInt();
			if(shortProviderNamesLength >0){sis.shortProviderNames = new String[shortProviderNamesLength];
											in.readStringArray(sis.shortProviderNames);}
			int descTagsLength = in.readInt();
			if(descTagsLength >0){sis.descTags = new int[descTagsLength];in.readIntArray(sis.descTags);}
			List<Integer> descDatasLength_1 = in.readArrayList(Integer.class.getClassLoader());
			sis.descDatas = new ArrayList<byte[]>();
			//FIXME
			for(int i=0;i<descDatasLength_1.size();i++){
				byte[] b = new byte[descDatasLength_1.get(i)];
				in.readByteArray(b);
				sis.descDatas.add(b);
			}
			
			return sis;
		}

		public NgbJSIService[] newArray(int size) {
			return new NgbJSIService[size];
		}
	};

	public int getHasPresentFollowing() {
		return hasPresentFollowing;
	}

	public void setHasPresentFollowing(int hasPresentFollowing) {
		this.hasPresentFollowing = hasPresentFollowing;
	}

	public int getHasSchedule() {
		return hasSchedule;
	}

	public void setHasSchedule(int hasSchedule) {
		this.hasSchedule = hasSchedule;
	}

	public int getIsFreeCAModle() {
		return isFreeCAModle;
	}

	public void setIsFreeCAModle(int isFreeCAModle) {
		this.isFreeCAModle = isFreeCAModle;
	}

	public ArrayList<Integer> getDescDatasLength() {
		return descDatasLength;
	}

	public void setDescDatasLength(ArrayList<Integer> descDatasLength) {
		this.descDatasLength = descDatasLength;
	}

	public void setLogicNum(int logicNum) {
		this.logicNum = logicNum;
	}

	public void setNetworkId(int networkId) {
		this.networkId = networkId;
	}

	public void setOnid(int onid) {
		this.onid = onid;
	}

	public void setTsid(int tsid) {
		this.tsid = tsid;
	}

	public void setServiceId(int serviceId) {
		this.serviceId = serviceId;
	}

	public void setServiceType(int serviceType) {
		this.serviceType = serviceType;
	}

	public void setPcrPID(int pcrPID) {
		this.pcrPID = pcrPID;
	}

	public void setNames(String[] names) {
		this.names = names;
	}

	public void setShortNames(String[] shortNames) {
		this.shortNames = shortNames;
	}

	public void setProviderNames(String[] providerNames) {
		this.providerNames = providerNames;
	}

	public void setShortProviderNames(String[] shortProviderNames) {
		this.shortProviderNames = shortProviderNames;
	}

	public void setDescTags(int[] descTags) {
		this.descTags = descTags;
	}

	public void setDescDatas(ArrayList<byte[]> descDatas) {
		this.descDatas = descDatas;
	}
	
	
}
