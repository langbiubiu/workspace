package ipaneltv.toolkit.dvb;

import ipaneltv.dvbsi.Descriptor;
import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;
import ipaneltv.toolkit.TimerFormater;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.dvb.DvbObjectification.SiEITEvents;
import ipaneltv.toolkit.dvb.DvbObjectification.SiObject;

import java.util.HashMap;

import android.content.Context;
import android.net.telecast.TransportManager;
import android.text.format.Time;

public class DvbPfSearch {
	private static final String TAG = "NewDvbPfSearch";
	TransportManager tsManager;
	DvbSiEventReceiveToolkit toolkit;
	private long curFreq = 0;
	private int programNum = 0;
	private Object mutex = new Object();
	boolean FreqChanged = false;
	boolean requiredData = false;
	private DvbObjectification mObjectf = null;
	private DvbSiEventPFSearch mPfSearch = null;
	private HashMap<Long, HashMap<Integer, Program[]>> presentAndFollow = new HashMap<Long, HashMap<Integer, Program[]>>();
	private HashMap<Integer, Program[]> pfMap = new HashMap<Integer, Program[]>();
	OnPfInfoListener searLis;
	Time t = new Time();

	public DvbPfSearch(Context ctx, String uuid) {
		mObjectf = new DvbObjectification();
		mPfSearch = DvbSiEventPFSearch.createInstance(ctx, uuid);
	}
	 
	public void setEnCoding(String coding){
		mObjectf.setBouquetNameEncoding(coding);
		mObjectf.setEventNameEncoding(coding);
		mObjectf.setNetworkNameEncoding(coding);
		mObjectf.setServiceNameEncoding(coding);
		mObjectf.setServiceProviderNameEncoding(coding);
		IPanelLog.i(TAG,"coding 22 = "+coding);
	}
	 
	public void setCoding(String coding){
		mObjectf.setBouquetNameEncoding(coding);
		mObjectf.setEventNameEncoding(coding);
		mObjectf.setNetworkNameEncoding(coding);
		mObjectf.setServiceNameEncoding(coding);
		IPanelLog.i(TAG,"coding 11 = "+coding);
	}
	public static interface OnPfInfoListener {
		void onPfInfoUpdated(Program present, Program follow);
	}

	public void setOnPfInfoListener(OnPfInfoListener searLis) {
		this.searLis = searLis;
	}

	void pfInfoNotify(Program present, Program follow) {
		OnPfInfoListener lis = this.searLis;
		if (lis != null) {
			lis.onPfInfoUpdated(present, follow);
		}
	}

	// Section数据接收状态
	class PFStateListener implements PFSearchToolkit.OnRetrieveStateListener {

		@Override
		public boolean onSectionRetrieved(int pid, int tableid, Section s,boolean full) {
			IPanelLog.i(TAG, "onSectionRetrieved 22 pf start full = "+ full);
			synchronized (mutex) {
				DvbObjectification.SiEITEvents se = mObjectf.parseEITEvents(s, descHanlder);
				if (se == null ? true : se.event == null) {
					IPanelLog.d(TAG, "OnPfInfoLisener se=" + se + ",se.event=" + se.event);
					return false;
				}

				IPanelLog.d(TAG, "pn = " + se.service_id + "sn =" + s.section_number());
				Program[] tPrograms = pfMap.get(se.service_id);
				if (tPrograms == null) {
					tPrograms = new Program[2];
					tPrograms[0] = new Program();
					tPrograms[1] = new Program();
					pfMap.put(se.service_id, tPrograms);
				}

				IPanelLog.d(TAG, "onSectionRetrieved event.size()=" + se.event.size());
				for (int i = 0; i < se.event.size(); i++) {
					SiEITEvents.Event sss = se.event.get(i);
					IPanelLog.d(TAG, "pn=" + se.service_id + ";start_time=" + sss.start_time
							+ ";duration = " + sss.duration + ";event_name = " + sss.event_name);

					long start_time = TimerFormater.rfc3339tolong(sss.start_time);
					long duration = formatDuration(sss.duration);
					long end_time = start_time + duration;
					IPanelLog.d(TAG, "serviceid 1=" + se.service_id + ",start_time=" + start_time
							+ ",end_time=" + end_time);
					if (s.section_number() == 0) { // present
						tPrograms[0].setName(sss.event_name);
						tPrograms[0].setStart(start_time);
						tPrograms[0].setEnd(end_time);
						tPrograms[0].setChannelKey(ChannelKey.obten(curFreq, se.service_id));
					} else if (s.section_number() == 1) { // follow
						tPrograms[1].setName(sss.event_name);
						tPrograms[1].setStart(start_time);
						tPrograms[1].setEnd(end_time);
						tPrograms[1].setChannelKey(ChannelKey.obten(curFreq, se.service_id));
					}
				}
				IPanelLog.d(TAG, "pn = " + programNum + "; curFreq = " + curFreq + ";requiredData = "
						+ requiredData);
				if (requiredData) {
					if (mPfSearch.isFull(programNum)) {
						try {
							IPanelLog.d(TAG, "noticyPF receive full programNum=" + programNum);
							HashMap<Integer, Program[]> hpC = presentAndFollow.get(curFreq);
							if (hpC == null) {
								hpC = new HashMap<Integer, Program[]>();
								presentAndFollow.put(curFreq, hpC);
							}
							Program[] tps = hpC.get(programNum);
							if (tps == null) {
								tps = new Program[2];
								tps[0] = new Program();
								tps[1] = new Program();
								hpC.put(programNum, tps);
							}
							Program[] required = pfMap.get(programNum);
							if (required != null) {
								long startTime = tps[0].getStart();
								long startTimeN = required[0].getStart();
								IPanelLog.d(TAG, "startTime = " + startTime + ";startTimeN = "
										+ startTimeN);
								if (startTimeN <= 0 || startTime == startTimeN) {
									IPanelLog.d(TAG, "Not't need update startTimeN = " + startTimeN
											+ ";startTime = " + startTime);
									requiredData = false;
									return false;
								}
								IPanelLog.d(TAG, "noticyPF required = " + required[0].getName()
										+ ";programNum = " + programNum + "; se.service_id = "
										+ se.service_id);
								requiredData = false;
								pfInfoNotify(required[0], required[1]);
							}
						} catch (Exception e) {
							IPanelLog.d(TAG, "noticyPF failed e=" + e);
							e.printStackTrace();
						}
					}
				}

				if (full) {
					IPanelLog.d(TAG, "requiredData = " + requiredData);
					if (requiredData)
						pfInfoNotify(null, null);
					HashMap<Integer, Program[]> hp = presentAndFollow.get(curFreq);
					if (hp == null) {
						hp = new HashMap<Integer, Program[]>();
						presentAndFollow.put(curFreq, hp);
					}
					hp.putAll(pfMap);
					pfMap.clear();
				}
			}
			return true;
		}

		@Override
		public void onTimeout(int pid, int tableid) {
			synchronized (mutex) {
				IPanelLog.d(TAG, "onTimeout");
				pfInfoNotify(null, null);
			}
		}
	}

	public long formatDuration(long duration) {
		int flag = 1;
		long j = 0;
		String s = Long.toHexString(duration);
		int i = s.length();
		while (i > 0) {
			if (flag > 3600) {
				break;
			}
			String s1 = s.substring(i - 2 > 0 ? i - 2 : 0, i);
			j = j + Integer.parseInt(s1) * flag;
			i = i - 2;
			flag = flag * 60;
		}
		j = j * 1000;
		return j;
	}

	public void getPresentAndFollow(long freq, int program) {
		synchronized (mutex) {
			IPanelLog.d(TAG, "getPresentAndFollow----- freq = " + freq + ";program_number = " + program);
			new SearchThread(freq).start();
			requiredData = true;
			curFreq = freq;
			programNum = program;

			/** 数据缓存起来备下次使用 */
			HashMap<Integer, Program[]> h = presentAndFollow.get(freq);
			if (h == null) {
				IPanelLog.d(TAG, "return null freq change");
				return;
			}

			Program[] p = h.get(program);
			if (p == null) {
				IPanelLog.d(TAG, "return null Program");
				return;
			}

			pfInfoNotify(p[0], p[1]);
		}
	}

	public boolean stopsearch() {
		mPfSearch.stop();
		return true;
	}

	public void release() {
		mPfSearch.release();
	}

	DvbObjectification.DescriptorHandler descHanlder = new DvbObjectification.DescriptorHandler() {
		@Override
		public void onDescriptorFound(SiObject siobj, Descriptor d) {
		}
	};

	class SearchThread extends Thread {
		private long freq;

		public SearchThread(long freq) {
			this.freq = freq;
		}

		@Override
		public void run() {
			IPanelLog.d(TAG, "SearchThread run freq =" + freq);
			synchronized (TAG) {
				IPanelLog.d(TAG, "SearchThread run 111");
				if(!mPfSearch.isFreqUnderSearch(freq)){
					mPfSearch.stop();
					if (mPfSearch.startSearchPF(freq, new PFStateListener())) {
						IPanelLog.d(TAG, "SearchThread run 2222");
					} else {
						mPfSearch.stop();
					}
					IPanelLog.d(TAG, "SearchThread end");
				}	
				IPanelLog.d(TAG, "freq = "+ freq+" is searching");
			}
		}
	}
}
