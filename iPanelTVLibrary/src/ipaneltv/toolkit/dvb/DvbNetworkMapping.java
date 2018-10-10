package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.SectionBuffer;
import ipaneltv.toolkit.dvb.DvbObjectification.SiBouquet;
import ipaneltv.toolkit.dvb.DvbObjectification.SiEITEvents;
import ipaneltv.toolkit.dvb.DvbObjectification.SiNetwork;
import ipaneltv.toolkit.dvb.DvbObjectification.SiPATServices;
import ipaneltv.toolkit.dvb.DvbObjectification.SiSDTServices;
import ipaneltv.toolkit.dvb.DvbObjectification.SiSDTServices.Service;
import ipaneltv.toolkit.dvb.DvbObjectification.SiTransportStream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import android.net.telecast.FrequencyInfo;
import android.util.Log;

public class DvbNetworkMapping {
	static final String TAG = "DvbNetworkMapping";
	private SiNetwork siNetwork;
	private final Vector<TransportStream> tss = new Vector<TransportStream>();
	private final Vector<RegionID> regs = new Vector<RegionID>();
	private TransportStream mainStream;

	public SiNetwork getSiNetwork() {
		return siNetwork;
	}

	boolean containsTranportStreamByFreq(long freq) {
		int n = tss.size();
		for (int i = 0; i < n; i++) {
			TransportStream ts = tss.elementAt(i);
			if (ts.fInfo.getFrequency() == freq)
				return true;
		}
		return false;
	}

	public Vector<TransportStream> getTss() {
		return tss;
	}

	boolean isMainFrequency(DvbObjectification.SiNetwork.TransportStream sits) {
		if (mainStream == null)
			return false;
		if (mainStream.getFrequencyInfo().getFrequency() == sits.frequency_info.getFrequencyInfo()
				.getFrequency()) {
			return true;
		}
		return false;
	}

	void buildTransportStream(SiNetwork sin) {
		synchronized (tss) {
			DvbObjectification.SiNetwork.TransportStream sits = null;
			int size = sin.transport_stream.size();
			for (int i = 0; i < size; i++) {
				sits = sin.transport_stream.get(i);
				if (sits.frequency_info != null) {
					if (!containsTranportStreamByFreq(sits.frequency_info.getFrequencyInfo()
							.getFrequency())) {
						TransportStream ts = createTransportStream(sits.frequency_info
								.getFrequencyInfo());
						ts.siTransportStram = sits;
						tss.add(ts);
					}
					if (isMainFrequency(sits)) {
						mainStream.siTransportStram = sits;
					}
				}
			}
			if (this.siNetwork == null) {
				this.siNetwork = sin;
			} else {
				this.siNetwork.transport_stream.addAll(sin.transport_stream);
			}
		}
	}

	public void setSiNetwork(SiNetwork siNetwork) {
		synchronized (tss) {
			if (this.siNetwork == null && mainStream != null) {
				tss.add(mainStream);
			}
			buildTransportStream(siNetwork);
		}
	}

	public void addMainTransportStream(TransportStream ts) {
		synchronized (tss) {
			mainStream = ts;
		}
	}

	public void addTransportStream2Head(TransportStream ts) {
		synchronized (tss) {
			if (!containsTranportStreamByFreq(ts.fInfo.getFrequency())) {
				IPanelLog.d(TAG, "addTransportStream2Head freq=" + ts.fInfo.getFrequency());
				tss.add(0, ts);
			}
		}
	}

	public void addTransportStream(TransportStream ts){
		synchronized (tss) {
			if (!containsTranportStreamByFreq(ts.fInfo.getFrequency())) {
				Log.d(TAG, "addTransportStream freq=" + ts.fInfo.getFrequency()+";tss.size() = "+ tss.size());
				if(tss.size()>0){
					tss.add(1, ts);	
				}
			}
		}
	}
	
	public boolean removeTransportStream(TransportStream ts) {
		synchronized (tss) {
			return tss.remove(ts);
		}
	}
	
	public int indexOfTransportStream(TransportStream ts) {
		synchronized (tss) {
			return tss.indexOf(ts);
		}
	}

	public int sizeOfTransportStream() {
		synchronized (tss) {
			return tss.size();
		}
	}

	public TransportStream transportStreamAt(int index) {
		synchronized (tss) {
			return tss.get(index);
		}
	}

	public TransportStream findTransportStreamByID(int tsid) {
		synchronized (tss) {
			for (int i = 0; i < tss.size(); i++) {
				TransportStream ts = tss.get(i);
				if (ts != null) {
					if (ts.siTransportStram != null) {
						if (ts.siTransportStram.transport_stream_id == tsid)
							return ts;
					}
				}
			}
		}
		return null;
	}

	public TransportStream findTransportStreamByFreq(long f) {
		synchronized (tss) {
			for (TransportStream ts : tss) {
				if (ts.fInfo != null)
					if (ts.fInfo.getFrequency() == f)
						return ts;
			}
		}
		return null;
	}

	public TransportStream[] listTransportStreams() {
		TransportStream[] ret = new TransportStream[tss.size()];
		tss.copyInto(ret);
		return ret;
	}

	public TransportStream createTransportStream(FrequencyInfo fInfo) {
		TransportStream ts = new TransportStream();
		ts.setFrequencyInfo(fInfo);
		return ts;
	}

	public class TransportStream {
		public SiTransportStream siTransportStram;
		SiNetwork siNetwork;
		SiNetwork siNetworkOther;
		SiBouquet siBouquet;
		SiBouquet siBouquetOther;
		public SiSDTServices siServices;
		public SiSDTServices siServicesOther;
		public SiPATServices siPrograms;
		SiEITEvents siEventsPF[] = new SiEITEvents[2];
		public int si_eit_last_tid = 0x50, si_eit_last_sn = 0;
		public int si_eit_other_last_tid = 0x60, si_eit_other_last_sn = 0;
		private SiEITEvents[/* tid-0x50 */][/* sn */] siEventsSchedule;
		private SiEITEvents[/* tid-0x60 */][/* sn */] siEventsScheduleOther;
		HashMap<Long, SectionBuffer> sections;
		// ConcurrentHashMap<Long, SectionBuffer> sections;
		public FrequencyInfo fInfo;

		public DvbNetworkMapping getDvbNetworkMap() {
			return DvbNetworkMapping.this;
		}

		public SectionBuffer getSectionBuffer(int pid, int tableId, int section_number) {
			return getSectionBuffer(pid, tableId, section_number, 0);
		}

		public SectionBuffer getSectionBuffer(int pid, int tableId, int section_number, int crc) {
			long key = (((long) crc) << 32) | (((long) pid) << 16) | (((long) tableId) << 8)
					| (section_number & 0xff);
			if (sections == null)
				sections = new HashMap<Long, SectionBuffer>();
			synchronized (sections) {
				return sections.get(key);
			}
		}

		public void setSectionBuffer(int pid, int tableId, int section_number, SectionBuffer sb) {
			setSectionBuffer(pid, tableId, section_number, 0, sb);
		}

		public void setSectionBuffer(int pid, int tableId, int section_number, int crc,
				SectionBuffer sb) {
			long key = (((long) crc) << 32) | (((long) pid) << 16) | (((long) tableId) << 8)
					| (section_number & 0xff);
			if (sections == null)
				sections = new HashMap<Long, SectionBuffer>();
			synchronized (sections) {
				sections.put(key, sb);
			}
		}

		public void setFrequencyInfo(FrequencyInfo fi) {
			fInfo = fi;
		}

		public FrequencyInfo getFrequencyInfo() {
			return fInfo;
		}

		public void clearSections() {
			sections.clear();
		}

		public SiTransportStream getSiTransportStream() {
			return siTransportStram;
		}

		public void setSiTransportStram(SiTransportStream t) {
			this.siTransportStram = t;
		}

		public SiNetwork getSiNetwork() {
			return siNetwork;
		}

		public void setSiNetwork(SiNetwork s) {
			this.siNetwork = s;
		}

		public SiNetwork getSiNetworkOther() {
			return siNetworkOther;
		}

		public void setSiNetworkOther(SiNetwork s) {
			this.siNetworkOther = s;
		}

		public SiBouquet getSiBouquet() {
			return siBouquet;
		}

		public void setSiBouquet(SiBouquet s) {
			this.siBouquet = s;
		}

		public SiBouquet getSiBouquetOther() {
			return siBouquetOther;
		}

		public void setSiBouquetOther(SiBouquet s) {
			this.siBouquetOther = s;
		}

		public SiSDTServices getSiServices() {
			return siServices;
		}

		public void setSiServices(SiSDTServices s) {
			this.siServices = s;
		}

		public void appendSiService(SiSDTServices s) {
			if (this.siServices == null)
				setSiServices(s);
			else {
				SiSDTServices si = this.siServices;
				for (int i = 0; i < s.service.size(); i++) {
					Service service = s.service.get(i);
					if (service != null)
						si.service.add(service);
				}
			}
		}

		public SiSDTServices getSiServicesOther() {
			return siServicesOther;
		}

		public void setSiServicesOther(SiSDTServices siServicesOther) {
			this.siServicesOther = siServicesOther;
		}

		public SiPATServices getSiPrograms() {
			return siPrograms;
		}

		public void setSiPrograms(SiPATServices s) {
			this.siPrograms = s;
		}

		public SiEITEvents getSiEventsPresent() {
			return siEventsPF[0];
		}

		public SiEITEvents getSiEventsFollow() {
			return siEventsPF[1];
		}

		public void setSiEventsPresent(SiEITEvents e) {
			siEventsPF[0] = e;
		}

		public void setSiEventsFollow(SiEITEvents e) {
			siEventsPF[1] = e;
		}

		public SiEITEvents getSiEventsSchedule(int tableid, int section_number) {
			if (siEventsSchedule != null)
				return siEventsSchedule[tableid - 0x50][section_number];
			return null;
		}

		public void setSiEventsSchedule(int tableid, int section_number, SiEITEvents e) {
			if (siEventsSchedule == null)
				siEventsSchedule = new SiEITEvents[16][256];
			siEventsSchedule[tableid - 0x50][section_number] = e;
			if (si_eit_last_tid < tableid)
				si_eit_last_tid = tableid;
			if (si_eit_last_sn < section_number)
				si_eit_last_sn = section_number;
		}

		public SiEITEvents getSiEventsScheduleOther(int tableid, int section_number) {
			if (siEventsScheduleOther == null)
				return null;
			return siEventsScheduleOther[tableid - 0x60][section_number];
		}

		public void setSiEventsScheduleOther(int tableid, int section_number, SiEITEvents e) {
			if (siEventsScheduleOther == null)
				siEventsScheduleOther = new SiEITEvents[16][256];
			siEventsScheduleOther[tableid - 0x60][section_number] = e;
			if (si_eit_other_last_tid < tableid)
				si_eit_other_last_tid = tableid;
			if (si_eit_other_last_sn < tableid)
				si_eit_other_last_sn = tableid;
		}

		public HashMap<Long, SectionBuffer> getTransportStreamSections() {
			return sections;
		}

		public void releaseSections() {
			if (sections == null)
				return;
			int size = sections.size();
			if (size > 0) {
				Set<Long> keys = sections.keySet();
				for (Iterator<Long> iterator = keys.iterator(); iterator.hasNext();) {
					long key = (Long) iterator.next();
					sections.get(key).release();
				}
			}
		}
	}

	public class RegionID {
		long freqency;
		String regionId;

		public RegionID() {
		}

		public RegionID(long freqency, String regionId) {
			this.freqency = freqency;
			this.regionId = regionId;
		}

		public long getFreqency() {
			return freqency;
		}

		public void setFreqency(long freqency) {
			this.freqency = freqency;
		}

		public String getRegionId() {
			return regionId;
		}

		public void setRegionId(String regionId) {
			this.regionId = regionId;
		}
	}

	public RegionID createRegionID() {
		RegionID rd = new RegionID();
		return rd;
	}

	public int sizeOfRegionID() {
		synchronized (regs) {
			return regs.size();
		}
	}

	public void addRegionID(RegionID reg) {
		synchronized (regs) {
			regs.add(reg);
		}
	}

	public RegionID regionIDAt(int index) {
		synchronized (regs) {
			return regs.get(index);
		}

	}
}
