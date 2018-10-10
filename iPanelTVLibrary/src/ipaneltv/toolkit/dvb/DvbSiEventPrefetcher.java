package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Natives;
import ipaneltv.toolkit.SectionBuffer;
import android.net.telecast.SectionPrefetcher;
import android.net.telecast.TransportManager;

public class DvbSiEventPrefetcher {
	static final String TAG = "DvbSiEventPreretcher";
	int peer = 0;
	SectionPrefetcher sp;
	SectionBuffer sbuf;
	TransportManager tm;
	String uuid;
	long freq;
	String name;
	int flags;
	private boolean prepared = false;
	int buffer[] = new int[2];

	@SuppressWarnings("deprecation")
	public DvbSiEventPrefetcher(TransportManager tm, String uuid, long freq, String name, int flags) {
		this.tm = tm;
		this.uuid = uuid;
		this.freq = freq;
		this.name = name;
		this.flags = flags;
		sbuf = SectionBuffer.createDummy();
		ncreate();
		sp = tm.createPrefetcher(uuid, name, flags);
		if(sp == null){
			throw new RuntimeException();
		}
//		if (peer == 0)
//			throw new RuntimeException();
	}

	public boolean prepare() {
		synchronized (TAG) {
			IPanelLog.d(TAG, "prepare 111113 prepared = "+ prepared);
			if(!prepared){
				prepared = nprepare(sp) == 0;
			}
			IPanelLog.d(TAG, "prepare end ");
			return prepared;
		}
	}

	public SectionPrefetcher getSectionPrefetcher() {
		return sp;
	}

	@SuppressWarnings("deprecation")
	public SectionBuffer getSection(int programNumber/* service id */, int index) {
		if (prepared) {
			if (nseek(programNumber, index, buffer) == 0) {
				sbuf.setNativeBufferAddress(buffer[0], buffer[1]);
				return sbuf;
			}
		}
		return null;
	}

	public int getSectionSize(int programNumber/* service id */) {
		if (prepared)
			return ntell(programNumber);
		return -1;
	}

	public int getProgramNumberList(int i[]){
		if(prepared){
			return ngetKeys(i);
		}
		return -1;
	}
	
	public void release() {
		synchronized (TAG) {
			if (prepared) {
				prepared = false;
				nrelease();
			}
		}
	}

	native int ncreate();

	native int nrelease();

	native int nprepare(Object sp);

	native int nseek(int pn, int index, int[] b);

	native int ntell(int pn);
	
	//获取收到epg的频道号的列表
	native int ngetKeys(int[]b);
	
	static {
		init();
	}

	@SuppressWarnings("deprecation")
	static void init() {
		Natives.ensure();
	}
}
