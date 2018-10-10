package ipaneltv.toolkit.dvb;


public class DvbSubtitle {
	public static final String TAG = DvbSubtitle.class.getSimpleName();
	private int peer = -1;
	private boolean prepared = false, released = false;

	public DvbSubtitle() {
		synchronized (TAG) {
			ncreate();
			if (peer == -1)
				throw new RuntimeException("impl error");
		}
	}

	public boolean prepare(int fd) {
		synchronized (TAG) {
			if (isPrepared())
				return true;
			return nprepare(fd) == 1;
		}
	}

	public boolean start() {
		synchronized (TAG) {
			if (isPrepared())
				return nstart() == 1;
			return false;
		}
	}

	public boolean stop() {
		synchronized (TAG) {
			if (isPrepared())
				return nstop() == 1;
			return false;
		}
	}

	public void release() {
		synchronized (TAG) {
			if (!released) {
				released = true;
				nrelease();
			}
		}
	}

	private SubtitleListener stl;

	public void setSubtitleListener(SubtitleListener l) {
		stl = l;
	}
	
	static final int DATA_RECEIVE = 1;

	void onMessage(int msg, SubtitleData data) {
		SubtitleListener l = stl;
		if (l == null)
			return;
		switch (msg) {
		case DATA_RECEIVE:
			if (l != null) {
				l.onSubtitleDataReceived(data);
			}
			break;
		default:
			return;
		}
	}

	/**
	 * 已否已准备过资源
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isPrepared() {
		return !released && prepared;
	}

	native int ncreate();

	native int nprepare(int fd);

	native int nstart();

	native int nstop();

	native void nrelease();

	/**
	 * 字幕监听器
	 */
	public static interface SubtitleListener {
		void onSubtitleDataReceived(SubtitleData data);
	}
}
