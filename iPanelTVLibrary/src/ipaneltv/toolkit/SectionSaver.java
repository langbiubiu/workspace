package ipaneltv.toolkit;

import ipaneltv.toolkit.dvb.DvbNetworkMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.net.telecast.SectionStorage;

public class SectionSaver {
	public static final int CRC_CHECK = 1;
	public static final int NO_CRC_CHECK = -1;
	public static final int OTHER_CHECK = 0;
	
	public static void saveSections(SectionStorage ss,DvbNetworkMapping.TransportStream ts) {
		saveSections(ss,new DvbNetworkMapping.TransportStream[]{ts},false);
	}
	
	public static void saveSections(SectionStorage ss,DvbNetworkMapping.TransportStream[] ts) {
		saveSections(ss,ts,false);
	}
	
	@SuppressWarnings("unused")
	public static void saveSections(SectionStorage sectionStorage,DvbNetworkMapping.TransportStream[] tss, boolean total) {
		Long key = 0l;
		SectionBuffer buf = null;
		int pid = -1;
		int tid = -1;
		int crc = 0;
		int section_number = 0;
		int[]vals;
		
		try {
			for(DvbNetworkMapping.TransportStream ts : tss){
//				HashMap<String,List> map = new HashMap<String, List>();
//				List<SectionBuffer>list = null;
				List<String> ptid = new ArrayList<String>();
				List<List<SectionBuffer>> lists = new ArrayList<List<SectionBuffer>>();
				HashMap<Long, SectionBuffer> sh = ts.getTransportStreamSections();
				if(sh != null){
					Set<Entry<Long,SectionBuffer>>entrySet = sh.entrySet();
					for(Entry<Long,SectionBuffer>entry : entrySet){
						key = entry.getKey();
						buf = entry.getValue();
						if(key != null){
							vals = getSectionParams(key);
							crc = vals[0];
							pid = vals[1];
							tid = vals[2];
							section_number = vals[3];
							String ptidstr = pid+"-"+tid;
							if(ptid.contains(ptidstr)){
								int index = ptid.indexOf(ptidstr);
								List<SectionBuffer> list = lists.get(index);
								list.add(buf);
								lists.set(index, list);//¸²¸Ç
							}else{
								List<SectionBuffer> list = new ArrayList<SectionBuffer>();
								list.add(buf);
								lists.add(list);
								ptid.add(ptidstr);
							}
							
						}
					}
					for(int i=0;i<ptid.size();i++){
						String [] str = ptid.get(i).split("-");
						int p = Integer.parseInt(str[0]);
						int t = Integer.parseInt(str[1]);
						SectionBuffer.saveToSectionStorage(sectionStorage, ts.getFrequencyInfo().getFrequency(),p , t, lists.get(i));
					}
					
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void saveSections(SectionStorage ss,DvbNetworkMapping map) {
		saveSections(ss,map.listTransportStreams(),true);
	}
	
	public static int[] getSectionParams(long val){
		int[]vals = new int[10];
		vals[0] = (int) ((val >> 32) & 0xffffffff);
		vals[1] = (int) ((val >> 16) & 0xffff);
		vals[2] = (int) ((val >> 8) & 0xff);
		vals[3] = (int) (val & 0xff);
		return vals;
	}
}
