package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import android.util.SparseArray;
import android.util.SparseIntArray;

/**
 * 表管理对象，对象是保存对应版本号的section是否收齐
 * 
 * 设置service的版本，和service对应的section总数
 */
public class DvbSiTable {
	static final String TAG = DvbSiTable.class.getSimpleName();

	private SparseArray<boolean[]> gotFlag = null;
	private SparseIntArray mversion = null;
	private SparseIntArray gotNum = null;
	private SparseIntArray allNum = null;
//	private SparseArray<byte[]> allSection = null;

	public DvbSiTable() {
		gotFlag = new SparseArray<boolean[]>();
		mversion = new SparseIntArray();
		gotNum = new SparseIntArray();
		allNum = new SparseIntArray();
		//只监测section是否收满，不保存section.
//		allSection = new SparseArray<byte[]>();      
	}

	/* synchronized */void setSectionNum(int service, int version, int num) {
		int v = mversion.get(service, -1);
		IPanelLog.d(TAG, "setSectionNum service ="+service+ "version = "+ version+";num = "+ num+";v = "+ v);
		if (v == -1) {
			mversion.put(service, version);
			allNum.put(service, num);
		} else {
			if (v != version) {
				mversion.put(service, version);
				allNum.put(service, num);// 此service有多少个section,若果有这种情况需要进行完善。
			}else{
				if(num > allNum.get(service)){
					allNum.put(version, num);
				}
			}
		}
	}

	/* synchronized */boolean setSectionFlag(int serviceId, int version, int sectionNum, int tid) {
		int got;
		boolean[] g = gotFlag.get(serviceId);
		if (g == null) {
			g = new boolean[512];
		}
		if (!g[sectionNum]) {
			g[sectionNum] = true;
			got = gotNum.get(serviceId);
			got++;
			IPanelLog.e(TAG, "receive a new section, SId = " + serviceId + ",Tid" + tid + ",SNum = "
					+ sectionNum + ",gotNum = " + got);
			gotNum.put(serviceId, got);
			gotFlag.put(serviceId, g);
			return true;
		} else {
			gotFlag.put(serviceId, g);
			IPanelLog.e(TAG, "receive a old section!");
			return false;
		}
	}

//	/* synchronized */void setSectionBuffer(int service, int number, Section s, int len) {
//		byte[] ld = new byte[4096];
//		s.getSectionBuffer().read(ld);
//		allSection.put(service, ld);
//	}

	public/* synchronized */boolean isFull(int service) {
		int got = gotNum.get(service), num = allNum.get(service);
		IPanelLog.d(TAG, "programnum = " + service + " is full got =  " + got + "all = " + num);
		if (got > 0 && got == num) {
			return true;
		}
		return false;
	}

	public/* synchronized */boolean isReady() {
		for(int i = 0 ;i < allNum.size();i++){
			int key = allNum.keyAt(i);
			if(allNum.get(key) != gotNum.get(key)){
				IPanelLog.d(TAG, "isReady key = "+ key +";allNum.get(key) = "+ allNum.get(key) +";gotNum.get(key) = "+ gotNum.get(key));
				return false;
			}
		}
		return true;
	}

	public/* synchronized */int addSections(Section s, int serviceid) {
		int version = s.version_number(), number = s.section_number();
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
			
			IPanelLog.d(TAG, "addSections serviceid = "+ serviceid + ";section_number = "+ section_number);
		} else {
			section_number = last + 1;
		}

		setSectionNum(serviceid, version, section_number);
		
		if (setSectionFlag(serviceid, version, number, s.table_id())) {
//			setSectionBuffer(serviceid, number, s, s.section_length());
			return number;
		}
		IPanelLog.d(TAG, "addSections return value -2");
		return -2;
	}

//	public/* synchronized */byte[] getSections(int service) {
//		byte[] got = allSection.get(service);
//		return got;
//	}

}
