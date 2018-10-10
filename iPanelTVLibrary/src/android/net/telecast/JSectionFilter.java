package android.net.telecast;

import java.util.LinkedList;

import android.util.Log;

public class JSectionFilter extends SectionFilter {

	public static JSectionFilter createInstance(String uuid) {
		return new JSectionFilter(uuid);
	}

	String uuid;
	LinkedList<byte[]> sections = new LinkedList<byte[]>();
	byte[] currentSection = null;
	long freq = 0;
	boolean flag = false;
	private int pid;
	Object o = new Object(); 
	
	@Override
	public void setAcceptionMode(int mode) {
		
	}
	
	@Override
	public void setTimeout(int t) {
		
	}
	
	JSectionFilter(String uuid) {
		this.uuid = uuid;
	}

	@Override
	public void setFrequency(long f) {
		this.freq = f;
	}

	@Override
	public long getFrequency() {
		return freq;
	}

	@Override
	public boolean start(int pid, byte[] coef, byte[] mask, byte[] excl, int len) {
		synchronized (o) {
			Log.d(TAG, "start 1");
			flag = true;
			this.pid = pid;
		}
		return JStreamSelector.filterSection(this, pid, coef, mask, excl, len);
	}

	@Override
	public boolean start(int pid, int tableId) {
		synchronized (o) {
			Log.d(TAG, "start1");
			flag = true;
			this.pid = pid;
		}
		return JStreamSelector.filterSection(this, pid, tableId);
	}
	
	@Override
	public int readSection(byte[] buf, int off, int len) throws IndexOutOfBoundsException {
		if (currentSection == null)
			return -1;
		if(buf == null ||buf.length<(off+len)){
			return -1;
		}
		System.arraycopy(currentSection, 0, buf, off, len);
		return currentSection.length;
	}

	@Override
	public int getStreamPID() {
		return pid;
	}
	
	public byte[] peekSection() {
		return currentSection;
	}

	boolean onNextSection() {
		if (!sections.isEmpty()) {
			currentSection = sections.pop();
			synchronized (o) {
				Log.d(TAG, "onNextSection flag = "+ flag);
				if(flag){
					this.sdl.onSectionRetrieved(this, currentSection.length);	
				}	
			}
			return true;
		}
		return false;
	}

	void addSection(byte[] b, int off, int len) {
		byte[] sb = new byte[len];
		System.arraycopy(b, off, sb, 0, len);
		sections.addLast(sb);
	}

	class Section {
		byte[] b;
		int off, len;
	}
	
	@Override
	public void stop() {
		synchronized (o) {
			Log.d(TAG, "stop");
			flag = false;
		}
	}
	
	@Override
	public void release() {
		synchronized (o) {
			Log.d(TAG, "release");
			flag = false;
		}
	}
}
