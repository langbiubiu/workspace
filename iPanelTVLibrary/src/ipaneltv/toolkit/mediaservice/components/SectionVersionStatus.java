package ipaneltv.toolkit.mediaservice.components;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.dvb.DvbConst;
import android.util.SparseArray;
import android.util.SparseIntArray;

public class SectionVersionStatus {
	static final String TAG = SectionVersionStatus.class.getSimpleName();
	private SparseArray<SparseIntArray> allNum = null;
	private SparseArray<SparseIntArray> gotNum = null;
	private SparseArray<SparseArray<boolean[]>> gotFlag = null;
	
	public SectionVersionStatus() {
		allNum = new SparseArray<SparseIntArray>();
		gotNum = new SparseArray<SparseIntArray>();
		gotFlag = new SparseArray<SparseArray<boolean[]>>();
	}
	
	public int addSections(Section s, int serviceid) {
		int version = s.version_number(), number = s.section_number(),tableId = s.table_id();
		int last = s.last_section_number(), section_number = 0;
		if (s.table_id() < 0 || version < 0 || number < 0) {
			IPanelLog.e(TAG, "section number error!");
			return -1;
		}
		if (number > last) {
			IPanelLog.e(TAG, "section number error!");
			return -1;
		}
		if (s.table_id() >= DvbConst.TID_EIT_ACTUAL_PF
				&& s.table_id() <= DvbConst.TID_EIT_OTHER_LAST) {
			if (last % 8 == 0)
				section_number = last / 8 + 1;
			else
				section_number = last / 8 + 2;
			
//			IPanelLog.d(TAG, "addSections serviceid = "+ serviceid + ";section_number = "+ section_number);
		} else {
			section_number = last + 1;
		}

		setSectionNum(tableId,serviceid,section_number);
		
		if (setSectionFlag(serviceid, version, number, tableId)) {
			return number;
		}
//		IPanelLog.d(TAG, "addSections return value -2");
		return -2;
	}
	
	void setSectionNum(int tableId, int service,int num) {
		SparseIntArray v = allNum.get(tableId);
		if(v == null){
			v = new SparseIntArray();
			v.put(service, num);
			allNum.put(tableId, v);
		}else{
			if(v.get(service,-1)==-1){
				v.put(service, num);
			}
		}
//		IPanelLog.d(TAG, "setSectionNum service ="+service+ ";num = "+ num+";v = "+ v);
	}
	
	boolean setSectionFlag(int serviceId, int version, int sectionNum, int tid) {
		int got;
		SparseArray<boolean[]> gotFlagArray = gotFlag.get(tid);
		if(gotFlagArray == null){
			gotFlagArray = new SparseArray<boolean[]>();
			gotFlag.put(tid, gotFlagArray);
		}
		boolean[] g = gotFlagArray.get(serviceId);
		if (g == null) {
			g = new boolean[512];
			gotFlagArray.put(serviceId, g);
		}
//		IPanelLog.d(TAG, "setSectionFlag g[sectionNum] = "+ g[sectionNum]+";tid = "+ tid);
		if (!g[sectionNum]) {
			g[sectionNum] = true;
			SparseIntArray gotArray = gotNum.get(tid);
			if(gotArray == null){
				gotArray = new SparseIntArray();
				gotNum.put(tid, gotArray);
			}
			got = gotArray.get(serviceId);
			got++;
			IPanelLog.e(TAG, "receive a new section, SId = " + serviceId + ",Tid" + tid + ",SNum = "
					+ sectionNum + ",gotNum = " + got);
			gotArray.put(serviceId, got);
			return true;
		}
		return false;
	}

	public boolean isFull(int service) {
		for(int i = 0 ;i < allNum.size();i++){
			int key = allNum.keyAt(i);
			SparseIntArray array = allNum.get(key);
			SparseIntArray gotArray = gotNum.get(key);
			if(gotArray != null){
				if(array.get(service,-1) != gotArray.get(service,-1)){
					return false;
				}	
			}
		}
		return true;
	}

	public boolean isReady() {
		for(int i = 0 ;i < allNum.size();i++){
			int key = allNum.keyAt(i);
			SparseIntArray array = allNum.get(key);
			SparseIntArray getArray = gotNum.get(key);
//			IPanelLog.d(TAG, "isReady key = "+ key+";array = "+ array+";getArray = "+ getArray);
			if(getArray !=null){
				for(int j = 0;j < array.size();j++){
					int k = array.keyAt(j);
					IPanelLog.d(TAG, "isReady key = "+ key+";k = "+ k+";array.get(k) = "+ array.get(k)+";getArray.get(k) = "+ getArray.get(k));
					if(array.get(k,-1)!= getArray.get(k,-1)){
						return false;
					}
				}	
			}else{
				return false;
			}

		}
		return true;
	}
}
