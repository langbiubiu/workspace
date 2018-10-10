package ipaneltv.toolkit;

import ipaneltv.dvbsi.DescCA;
import ipaneltv.dvbsi.DescStreamIdentifier;
import ipaneltv.dvbsi.Descriptor;
import ipaneltv.dvbsi.PAT;
import ipaneltv.dvbsi.PMT;
import ipaneltv.toolkit.dvb.DvbConst;

import java.util.Vector;

import android.content.Context;
import android.net.telecast.SectionFilter;
import android.net.telecast.TransportManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.SparseArray;
import android.util.SparseIntArray;

/**
 * 节目信息监控,pat及pmt一起监控
 * 
 * @author liruiy
 * 
 */
public abstract class ProgramMoniterFilter {
	private static final String TAG = ProgramMoniterFilter.class.getSimpleName();
	SectionFilter patFilter;
	SparseArray<SectionFilter> pmtFilters;
	protected Context ctx;
	String uuid;
	TransportManager tsManager;
	SectionBuffer sb = SectionBuffer.createSectionBuffer(4096);
	HandlerThread proThread = new HandlerThread("proThread");
	Handler pHandler;
	Section section = new Section(sb);
	long currentFreq = 0;
	int currentPn = 0;

	public ProgramMoniterFilter() {
	}

	public ProgramMoniterFilter(String uuid, Context ctx, int flags) {
		this.ctx = ctx;
		this.uuid = uuid;
		proThread.start();
		pHandler = new Handler(proThread.getLooper());
		tsManager = TransportManager.getInstance(ctx);
		patFilter = tsManager.createFilter(uuid, 0);
		patFilter.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
		patFilter.setCARequired(false);
		patFilter.setTimeout(10 * 1000);
		patFilter.setSectionDisposeListener(lis);
		pmtFilters = new SparseArray<SectionFilter>();
	}

	/**
	 * 开始监控
	 * 
	 * @param f
	 *            当前的频点
	 */
	public void start(final long f) {
		postPorc(new Runnable() {

			@Override
			public void run() {
				Log.i(TAG, "start......AAAA");
				stop(FLAG_STOP_PAT_FILTER);
				currentFreq = f;
				currentPn = -1;
				patFilter.setFrequency(f);
				patFilter.start(DvbConst.PID_PAT, DvbConst.TID_PAT);
			}
		});
	}

	/**
	 * 开始监控
	 * 
	 * @param f
	 *            当前的频点
	 */
	public void start(final long f, final int pn) {
		postPorc(new Runnable() {

			@Override
			public void run() {
				Log.i(TAG, "start......AAAA");
				stop(FLAG_STOP_PAT_FILTER);
				currentFreq = f;
				currentPn = pn;
				patFilter.setFrequency(f);
				patFilter.start(DvbConst.PID_PAT, DvbConst.TID_PAT);
			}
		});
	}

	/**
	 * 添加一个pmt的接收器。
	 * 
	 * @param f
	 *            锁在的频点
	 * @param pid
	 *            对应的pid
	 */
	public void addPmtReceiver(final long f, final int pid) {
		postPorc(new Runnable() {

			@Override
			public void run() {
				if (pmtFilters.size() < getMaxFilterSize() && currentFreq == f
						&& pmtFilters.get(pid) == null) {
					SectionFilter sf = tsManager.createFilter(uuid, 0);
					sf.setAcceptionMode(SectionFilter.ACCEPT_UPDATED);
					sf.setCARequired(false);
					sf.setTimeout(10 * 1000);
					sf.setSectionDisposeListener(lis);
					sf.setFrequency(f);
					sf.start(pid, DvbConst.TID_PMT);
					pmtFilters.append(pid, sf);
				}
			}
		});
	}

	public static final int FLAG_STOP_PAT_FILTER = 0x1;
	public static final int FLAG_STOP_PMT_FILTER = 0x2;

	/**
	 * 停止监控,包括pat以及pmt的监控。
	 */
	public void stop() {
		postPorc(new Runnable() {

			@Override
			public void run() {
				currentFreq = -1;
				currentPn = -1;
				stop(FLAG_STOP_PAT_FILTER | FLAG_STOP_PMT_FILTER);
			}
		});
	}

	private void stop(int flags) {
		if ((flags & FLAG_STOP_PAT_FILTER) == FLAG_STOP_PAT_FILTER) {
			patFilter.stop();
		}
		if ((flags & FLAG_STOP_PMT_FILTER) == FLAG_STOP_PMT_FILTER) {
			for (int i = 0; i < pmtFilters.size(); i++) {
				pmtFilters.get(pmtFilters.keyAt(i)).setSectionDisposeListener(null);
				pmtFilters.get(pmtFilters.keyAt(i)).release();
			}
			pmtFilters.clear();
		}
	}

	private void postPorc(Runnable r) {
		try {
			pHandler.post(r);
		} catch (Exception e) {
			Log.e(TAG, "postPorc error = " + e.getMessage());
		}
	}

	public void release() {
		stop();
		proThread.quit();
	}

	/**
	 * 开启的filter上限数，子类可重写以修改。
	 * 
	 * @return
	 */
	protected int getMaxFilterSize() {
		return 10;
	}

	/**
	 * 获取指定pmt的版本号，子类可重写以实现
	 * 
	 * @param f
	 *            频点
	 * @param pn
	 *            节目号
	 * @param pid
	 *            pid
	 * @return -1 则为没有获取到。
	 */
	protected int getPmtVersion(long f, int pn, int pid) {
		return -1;
	}

	/**
	 * 子类可重写以设置pmt的版本
	 * 
	 * @param f
	 * @param pn
	 * @param pid
	 */
	protected void setPmtVersion(long f, int pn, int pid, int version) {

	}

	public abstract boolean onStreamsReceived(Programs ps);
	public void onStreamsNotFound(long f,int pn){
		
	}

	SectionFilter.SectionDisposeListener lis = new SectionFilter.SectionDisposeListener() {

		@Override
		public void onStreamLost(SectionFilter f) {
			Log.d(TAG, "onStreamLost");
		}

		@Override
		public void onReceiveTimeout(SectionFilter f) {
			Log.d(TAG, "onReceiveTimeout");
		}

		@Override
		public void onSectionRetrieved(final SectionFilter f, int len) {
			IPanelLog.i(TAG, "onSectionRetrieved 1");
			sb.copyFrom(f);
			section.reset();
			byte[] b = new byte[section.getSectionBuffer().getDataLength() - 4];
			section.getSectionBuffer().read(b);
			int crc = SectionBuilder.calculateCRC32(b, b.length);
			IPanelLog.d(TAG,"onSectionRetrieved crc = "+ crc +";section.crc_32() = "+ section.crc_32());
			if(crc != section.crc_32()){
				IPanelLog.d(TAG,"invilid section");
				return;
			}
			if (section.table_id() == DvbConst.TID_PAT) {
				onPatReceived(f.getFrequency(), section);
			} else if (section.table_id() == DvbConst.TID_PMT) {
				onPmtReceived(f.getFrequency(), f.getStreamPID(), section);
			}
		}

	};

	public void onPatReceived(final long f, final Section s) {
		if (currentFreq == f) {
			// 收到pat，将pmt的filter清空重新收取。
			stop(FLAG_STOP_PMT_FILTER);
			int n = PAT.program_size(s);
			boolean found = false;
			Log.i(TAG, "onPatReceived n = " + n);
			for (int i = 0; i < n; i++) {
				PAT.Program p = PAT.program(s, i);
				IPanelLog.i(
						TAG,
						"onPatReceived p.num = " + p.program_number() + ";p = "
								+ p.program_map_pid());
				if (p.program_number() == currentPn || currentPn <= 0) {
					found = true;
					addPmtReceiver(f, p.program_map_pid());
				}
			}
			if (!found) {
				onStreamsNotFound(f,currentPn);
			}
		}
	}

	public void onPmtReceived(final long f, final int pid, final Section s) {
		if (currentFreq == f) {
			int pn = PMT.program_number(s);
			int version = PMT.version_number(s);
			int section_number = PMT.section_number(s);
			int ov = getPmtVersion(f, pn, pid);
			Log.i(TAG, "TAGprogram_number = " + pn + " pid = " + pid + " section_number = "
					+ section_number + " version = " + version);
			byte[] b = new byte[s.getSectionBuffer().getDataLength() - 4];
			s.getSectionBuffer().read(b);
			int crc = SectionBuilder.calculateCRC32(b, b.length);
			IPanelLog.d(TAG,"onSectionRetrieved crc = "+ crc +";s.crc_32() = "+ s.crc_32());
			if(crc != s.crc_32()){
				IPanelLog.d(TAG,"invilid section");
				return;
			}
			if (ov != -1 && ov == version) {
				Log.d(TAG, "onPmtReceived pn = " + pn + " version is the same。");
				return;
			}
			SparseIntArray cainfo = new SparseIntArray();
			Programs ps = new Programs();
			ps.pcr_pid = PMT.pcr_pid(s);
			ps.pn = pn;
			ps.f = f;
			ps.pmtpid = pid;
			int ds = PMT.descriptor_size(s);
			for (int i = 0; i < ds; i++) {
				Descriptor d = PMT.descriptor(s, i);
				switch (d.descriptor_tag()) {
				case DescCA.TAG:
					Log.d(TAG,
							"parsePMT ds = " + ds + ";d.descriptor_tag() = " + d.descriptor_tag());
					parseDescCA(cainfo, null, d);
					break;
				default:
					break;
				}
			}

			int compSize = PMT.component_size(s);
			Log.i(TAG, " XX ps.pcr_pid = " + ps.pcr_pid + " compSize = " + compSize);
			Log.i(TAG, " PMT.program_number(s) = " + PMT.program_number(s)
					+ " PMT.version_number(s) = " + PMT.version_number(s));
			for (int i = 0; i < compSize; i++) {
				Stream stream = new Stream();
				PMT.Component comp = PMT.component(s, i);
				stream.stream_pid = comp.elementary_pid();
				stream.stream_type = comp.stream_type();
				Log.d(TAG, "XX PMT stream=" + stream + ";stream_pid = " + stream.stream_pid
						+ ";stream_type=" + stream.stream_type + ";pn=" + pn);
				for (int j = 0; j < cainfo.size(); j++) {
					stream.addEcm(cainfo.valueAt(j), cainfo.keyAt(j), stream.stream_pid);
				}
				ds = comp.descriptor_size();
				Log.d(TAG, "ds = " + ds);
				for (int j = 0; j < ds; j++) {
					Descriptor d = comp.descriptor(j);
					Log.i(TAG, "d.descriptor_tag() = " + d.descriptor_tag());
					switch (d.descriptor_tag()) {
					case DescStreamIdentifier.TAG:
						DescStreamIdentifier desc = new DescStreamIdentifier(d);
						stream.component_tag = desc.component_tag();
						Log.i(TAG, "desc.component_tag() = " + desc.component_tag());
						break;
					case DescCA.TAG:
						Log.d(TAG, "PMT.descriptor_tag= " + d.descriptor_tag());
						parseDescCA(null, stream, d);
						break;
					default:
						break;
					}
				}
				ps.addStream(stream);
			}
			if(onStreamsReceived(ps)){
				setPmtVersion(f, pn, pid, version);	
			}
		}
	}

	protected void parseDescCA(SparseIntArray cainfo, Stream s, Descriptor d) {
		DescCA des = new DescCA(d);
		Log.d(TAG, "" + "parsePmt ca_pid = " + des.ca_pid() + ",ca_system_id" + des.ca_system_id());
		if (cainfo != null) {
			cainfo.put(des.ca_pid(), des.ca_system_id());
		}
		if (s != null) {
			s.addEcm(des.ca_system_id(), des.ca_pid(), s.stream_pid);
		}
	}

	public static class Programs {
		public long f;
		public int pcr_pid;
		public int pn;
		public int pmtpid;
		public SparseArray<Stream> sia;

		public Programs() {
			sia = new SparseArray<Stream>();
		}

		public void addStream(Stream s) {
			sia.append(s.stream_pid, s);
		}

		public Stream getStream(int pid) {
			return sia.get(pid);
		}
	}

	public class Stream {
		public int stream_type;
		public int stream_pid;
		public String stream_type_name;
		public int component_tag;
		public Vector<Ecm> ecms;

		public int ecmSize() {
			if (ecms != null)
				return ecms.size();
			return 0;
		}

		public class Ecm {
			public int ca_system_id;
			public int ecm_pid = -1;
			public int stream_pid = -1;
		}

		void addEcm(int casysid, int ecmpid, int stream_pid) {
			Ecm e;
			if (ecms != null) {
				for (int i = 0; i < ecms.size(); i++) {
					if ((e = ecms.get(i)).ca_system_id == casysid) {
						e.ecm_pid = ecmpid;
						e.stream_pid = stream_pid;
						return;
					}
				}
			}
			e = new Ecm();
			if (ecms == null)
				ecms = new Vector<Ecm>();
			ecms.add(e);
			e.ca_system_id = casysid;
			e.ecm_pid = ecmpid;
			e.stream_pid = stream_pid;
		}
	}
}