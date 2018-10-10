package ipaneltv.toolkit;

import ipaneltv.dvbsi.PAT;
import ipaneltv.dvbsi.PMT;
import ipaneltv.toolkit.dvb.DvbConst;
import android.net.telecast.ProgramInfo;
import android.net.telecast.SectionFilter;
import android.net.telecast.TransportManager;

public abstract class ProgramInfoFilter {
	static final String TAG = "ProgramInfoFilter";
	int step;
	int pnum = -1; // -1表示无视这个值判定
	int pmtpid = -1;
	ProgramHandler h;
	int ver;
	SectionFilter patFilter;
	SectionFilter pmtFilter;
	SectionBuffer sb = SectionBuffer.createSectionBuffer(4096);
	Section section = new Section(sb);
	SectionFilter.SectionDisposeListener lis = new SectionFilter.SectionDisposeListener() {

		@Override
		public void onStreamLost(SectionFilter f) {
			h.onProgramFound(ver, "onStreamLost", null);
		}

		@Override
		public void onReceiveTimeout(SectionFilter f) {
			h.onProgramFound(ver, "onReceiveTimeout", null);
		}

		@Override
		public void onSectionRetrieved(SectionFilter f, int len) {
			IPanelLog.i(TAG, "ProgramInfoFilter onSectionRetrieved step = " + step);
			sb.copyFrom(f);
			if (step == 1 && section.table_id() == 0x00) {
				onPat(section);
			} else if (step == 2 && section.table_id() == 0x02) {
				onPmt(section);
			}
		}

	};

	void onPat(Section s) {
		IPanelLog.i(TAG, "ProgramInfoFilter onPat start");
		int n = PAT.program_size(s), pmtpid = -1;
		IPanelLog.i(TAG, "ProgramInfoFilter program_size = " + n + ";pnum = " + pnum+";s.crc_32()="+s.crc_32()+";s.section_length()="+s.section_length());
		for (int i = 0; i < n; i++) {
			PAT.Program p = PAT.program(s, i);
			IPanelLog.i(TAG, "ProgramInfoFilter p.num = " + p.program_number());
			if (pnum <= 0 ? true : p.program_number() == pnum) {
				if (pnum <= 0)
					pnum = p.program_number();
				if (pnum != 0) {
					pmtpid = p.program_map_pid();
					IPanelLog.i(TAG, "ProgramInfoFilter onPat pmtpid = " + pmtpid);
					break;
				}
			}
		}
		if (pmtpid >= 0) {
			step2(pmtpid);
		}
	}

	boolean onPmt(Section s) {
		IPanelLog.i(TAG, "ProgramInfoFilter onPmt start");
		ProgramInfo pi = new ProgramInfo();
		int pn = PMT.program_number(s);
		if (pnum <= 0 ? false : pn != pnum) {
			h.onProgramFound(ver, "conflict program number", pi);
			return false;
		}
		pi.setProgramNumber(pn);
		IPanelLog.i(TAG, "ProgramInfoFilter onPmt programNumber = " + pi.getProgramNumber());
		pi.setPcrPID(PMT.pcr_pid(s));
		int n = PMT.component_size(s);
		if (n < 1) { // VOD 点播必有音视频，所以其component_size值必大于2
			IPanelLog.d(TAG, "ProgramInfoFilter onPmt component_size =" + n);
			return false;
		}
		IPanelLog.d(TAG, "onPmt pn = "+ pn +";pnum = "+ pnum+";+n = "+ n);
		for (int i = 0; i < n; i++) {
			IPanelLog.d(TAG, "onPmt i = "+ i);
			PMT.Component c = PMT.component(s, i);
			IPanelLog.d(TAG, "onPmt c.stream_type()");
			String name = getStreamTypeName(c.stream_type());
			IPanelLog.d(TAG, "onPmt name = "+ name);
			if (ProgramInfo.isAudioStream(name)) {
				if (pi.getAudioPID() == ProgramInfo.PID_UNDEFINED) {
					pi.setAudioPID(c.elementary_pid());
					IPanelLog.i(TAG, "ProgramInfoFilter audioPid = " + pi.getAudioPID());
					pi.setAudioStreamType(name);
				}
			} else if (ProgramInfo.isVideoStream(name)) {
				if (pi.getVideoPID() == ProgramInfo.PID_UNDEFINED) {
					pi.setVideoPID(c.elementary_pid());
					IPanelLog.i(TAG, "ProgramInfoFilter videoPid = " + pi.getVideoPID());
					pi.setVideoStreamType(name);
				}
			} else if (ProgramInfo.isPcrStream(name)) {
				if (pi.getPcrPID() < 0) {
					pi.setPcrPID(c.elementary_pid());
					IPanelLog.i(TAG, "ProgramInfoFilter prcPid = " + pi.getPcrPID());
				}
			}
		}
		if (pi.getAudioPID() == ProgramInfo.PID_UNDEFINED
				&& pi.getVideoPID() == ProgramInfo.PID_UNDEFINED) {
			h.onProgramFound(ver, "bad pmt setion info", pi);
			return false;
		}
		IPanelLog.d(TAG, "h = "+ h+";pi = "+ pi);
		h.onProgramFound(ver, null, pi);
		return true;
	}

	protected abstract String getStreamTypeName(int stream_type);
	
	void step2(int pid) {
		IPanelLog.i(TAG, "ProgramInfoFilter step2 start");
		step = 2;
		if (!pmtFilter.start(pid, DvbConst.TID_PMT)) {
			h.onProgramFound(ver, "start pmt failed", null);
			return;
		}
	}

	void step1() {
		IPanelLog.i(TAG, "ProgramInfoFilter step1 start");
		step = 1;
		if (!patFilter.start(DvbConst.PID_PAT, DvbConst.TID_PAT)) {
			h.onProgramFound(ver, "start pmt failed", null);
			return;
		}
	}

	public ProgramInfoFilter(String uuid, TransportManager tm) {
		patFilter = tm.createFilter(uuid, 0);
		patFilter.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
		patFilter.setCARequired(false);
		patFilter.setTimeout(10 * 1000);
		patFilter.setSectionDisposeListener(lis);
		pmtFilter = tm.createFilter(uuid, 0);
		pmtFilter.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
		pmtFilter.setCARequired(false);
		pmtFilter.setTimeout(10 * 1000);
		pmtFilter.setSectionDisposeListener(lis);
	}
	
	public ProgramInfoFilter(String uuid, TransportManager tm,int timeout) {
		IPanelLog.i(TAG, "ProgramInfoFilter timeout = "+ timeout);
		patFilter = tm.createFilter(uuid, 0);
		patFilter.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
		patFilter.setCARequired(false);
		patFilter.setTimeout(timeout*1000);
		patFilter.setSectionDisposeListener(lis);
		pmtFilter = tm.createFilter(uuid, 0);
		pmtFilter.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
		pmtFilter.setCARequired(false);
		pmtFilter.setTimeout(timeout*1000);
		pmtFilter.setSectionDisposeListener(lis);
	}

	public synchronized void start(long f, int pn, int ver, ProgramHandler h) {
		IPanelLog.i(TAG, "ProgramInfoFilter start f = " + f + "; pn = " + pn);
		if (pn <= 0 ? false : pn >= 65536) {
			h.onProgramFound(ver, "bad program number", null);
			return;
		}
		this.h = h;
		this.ver = ver;
		this.pnum = pn;
		patFilter.setFrequency(f);
		pmtFilter.setFrequency(f);
		step1();
	}

	public synchronized void start2(long f, int pmt_pid, int ver, ProgramHandler h) {
		IPanelLog.i(TAG, "ProgramInfoFilter start f = " + f + "; pmt_pid = " + pmt_pid);
		if (pmt_pid < 0 || pmt_pid >= 8192) {
			h.onProgramFound(ver, "bad pmt_pid value", null);
			return;
		}
		this.h = h;
		this.ver = ver;
		this.pnum = -1;
		this.pmtpid = pmt_pid;
		pmtFilter.setFrequency(f);
		step2(pmt_pid);
	}

	public synchronized void startPmt(long f, int pmtpid, int ver, ProgramHandler h) {
		this.h = h;
		this.ver = ver;
		pmtFilter.setFrequency(f);
		step2(pmtpid);
	}

	public synchronized void stop() {
		if(patFilter != null){
			patFilter.stop();	
		}
		if(pmtFilter != null){
			pmtFilter.stop();
		}
	}

	Object tag;

	public void setTag(Object o) {
		tag = o;
	}

	public Object getTag() {
		return tag;
	}

	public synchronized void release() {
		if(patFilter != null){
			patFilter.release();
			patFilter = null;
		}
		if(pmtFilter != null){
			pmtFilter.release();
			pmtFilter = null;
		}
	}

	public static interface ProgramHandler {
		public void onProgramFound(int ver, String err, ProgramInfo info);
	}
}
