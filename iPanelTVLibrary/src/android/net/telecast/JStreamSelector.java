package android.net.telecast;

import java.io.FileDescriptor;
import java.util.HashMap;
import java.util.LinkedList;

import android.util.Log;
import android.util.SparseArray;

public class JStreamSelector extends StreamSelector {
    static final String TAG = JStreamSelector.class.toString();
	static HashMap<String, JStreamSelector> dmx = new HashMap<String, JStreamSelector>();
	static LinkedList<JSectionFilter> poll = new LinkedList<JSectionFilter>();
	static boolean pollState = false;
	static boolean first = true;
	static Thread pollThread = new Thread(new Runnable() {
		@Override
		public void run() {
			JSectionFilter f;
			for (;;) {
				synchronized (poll) {
					while (poll.isEmpty()) {
						f = null;
						try {
							poll.wait();
							Log.d(TAG, "run");
						} catch (InterruptedException e) {
						}
					}
					f = poll.pop();
				}
				//唤醒之后再等5毫秒。以免数据发送太快导致之后的filter来不及启动
				if(first){
					first = false;
					try {
						Thread.sleep(25);
					} catch (InterruptedException e1) {
						
					}	
				}
				try {
					if (f.onNextSection()) {
						synchronized (poll) {
							poll.addLast(f);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	});
	
	static void ensurePoll() {
		if (!pollState) {
			pollState = true;
			pollThread.start();
		}
	}

	static boolean filterSection(final JSectionFilter f, final int pid, final byte[] coef, final byte[] mask, final byte[] excl,
			final int depth) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				synchronized (dmx) {
					JStreamSelector ss = dmx.get(f.uuid);
					boolean empty = true;
					if (ss == null)
						return;
					synchronized (ss.mutex) {
						if (ss.freq == f.freq) {
							LinkedList<Sections> list = ss.sections.get(pid);
							if (list != null) {
								for(Sections sec:list){
									int slen;
									int off = 0;
									do {
										slen = section_len(sec, off);
										Log.d(TAG, "filterSection 1 f = "+f.getFrequency()+";pid = "+ pid+"coef[0]"+coef[0]+";sec.buf = "+sec.buf.length+";slen = "+slen);
										if (slen > 0) {
											Log.d(TAG, "section_accept sec.buf[off] = "+ sec.buf[off]+";");
											if (section_accept(sec.buf, off, coef, mask, excl, depth)) {
												Log.d(TAG, "filterSection pid111 = "+ pid);
												empty = false;
												f.addSection(sec.buf, off, slen);
												if((off+slen)>=sec.buf.length){
													f.addSection(sec.buf, off, slen);
												}
											}
											off += slen;
										}
										Log.d(TAG, "filterSection off = "+off);
									} while (off<sec.buf.length);
									synchronized (poll) {
										ensurePoll();
										poll.addLast(f);
										poll.notify();
									}	
								}
								if(empty){
									f.sdl.onReceiveTimeout(f);
								}
							}else{
								f.sdl.onReceiveTimeout(f);
							}
						}
					}
				}
				}
			}).start();

		return true;
	}

	static boolean filterSection(final JSectionFilter f, final int pid, final int tableId) {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				synchronized (dmx) {
					JStreamSelector ss = dmx.get(f.uuid);
					boolean empty = true;
					if (ss == null)
						return;
					synchronized (ss.mutex) {
						LinkedList<Sections> list = ss.sections.get(pid);
						if (list != null) {
							for(Sections sec:list){
								int slen;
								int off = 0;
								do {
									slen = section_len(sec, off);
									Log.d(TAG, "filterSection f = "+f.getFrequency()+";pid = "+ pid+";tableId = "+ tableId+";sec.buf"+sec.buf.length+";slen = "+ slen);
									if (slen > 0) {
										Log.d(TAG, "section_accept sec.buf[off] = "+ sec.buf[off]+";");
										if (section_accept(sec.buf, off, tableId)){
											Log.d(TAG, "filterSection pid111 = "+ pid);
											empty = false;
											f.addSection(sec.buf, off, slen);
											if((off+slen)>=sec.buf.length){
												f.addSection(sec.buf, off, slen);
											}
										}
										off += slen;
									}
									Log.d(TAG, "filterSection off = "+off);
								} while (off<sec.buf.length);
								synchronized (poll) {
									ensurePoll();
									poll.addLast(f);
									poll.notify();
								}
							}
							if(empty){
								f.sdl.onReceiveTimeout(f);
							}
						}else{
							f.sdl.onReceiveTimeout(f);
						}
					}
				}
			}
		}).start();
		return true;
	}

	public static void setTransportSections(String uuid, long freq, int pid, byte[] b) {
		synchronized (sss) {
			Log.d(TAG, "setTransportSections freq = "+ freq +";pid = "+ pid +";b.length = "+ b.length);
			LinkedList<Sections> list = sss.get(uuid + freq);
			if (list == null) {
				list = new LinkedList<Sections>();
				sss.put(uuid + freq, list);
			}
			Sections sections = new Sections(pid, b);
			list.addLast(sections);
		}
	}

	static HashMap<String, LinkedList<Sections>> sss = new HashMap<String, LinkedList<Sections>>();

	final Object mutex = new Object();
	long freq;
	String uuid, uuidNext;
	SparseArray<LinkedList<Sections>> sections = new SparseArray<LinkedList<Sections>>();

	@Override
	public boolean select(FrequencyInfo fi, int flags) {
		synchronized (dmx) {
			synchronized (mutex) {
				first = true;
				if (uuid != null)
					dmx.remove(uuid);
				uuid = uuidNext;
				dmx.put(uuid, this);
				this.freq = fi.getFrequency();
				LinkedList<Sections> ss = sss.get(uuid + freq);
				sections.clear();
				if (ss != null) {
					for (Sections sec : ss) {
						LinkedList<Sections> ll = sections.get(sec.pid);
						if(ll == null){
							ll = new LinkedList<Sections>();
							sections.append(sec.pid, ll);
						}
						ll.add(sec);
					}
				}
			}
		}
		return true;
	}

	@Override
	public void setNetworkUUID(String id) {
		synchronized (dmx) {
			uuidNext = id;
		}
	}

	static final int section_len(Sections s, int off) {
		if (off >= s.buf.length) {
			return -1;
		}
		int n = s.buf[off+6];
		Log.d(TAG, "section_len off = "+ off +";n = "+n+";s.buf.length = "+ s.buf.length);
		return ((s.buf[off + 1] & 0x0f) << 8 | s.buf[off + 2] & 0xff) + 3;
	}

	static final boolean section_accept(byte[] b, int off, byte[] coef, byte[] mask, byte[] excl,
			int len) {
		for (int i = 0; i < len; i++) {
			if((mask[i]&b[off+i])!=(mask[i]&coef[i])){
				return false;
			}
		}
		return true;
	}

	static final boolean section_accept(byte[] b, int off, int tableId) {
		if (tableId == b[off]) {
			return true;
		}
		return false;
	}

	public static class Sections {
		public Sections(int pid, byte[] buf) {
			this.pid = pid;
			this.buf = buf;
		}

		byte[] buf;
		int pid;
	}

	@Override
	public void release() {
		// 放空
	}

	@Override
	public SignalStatus getSignalStatus() {
		return null;
	}

	@Override
	public void setVirtualFrequency(long freq) {

	}

	@Override
	public boolean setWeakMode(boolean b) {
		return false;
	}

	@Override
	public boolean select(FileDescriptor fd, int flags) {
		return false;
	}

	@Override
	public boolean select(FileDescriptor fd, long off, long len, int flags) {
		return false;
	}

	@Override
	public boolean receive(int pid, FileDescriptor fd, int flags) {
		return false;
	}
}
