package ipaneltv.toolkit.ca;

public class CaCardSlot {
	public static final int STATE_ABSENT = 0;
	public static final int STATE_MUTED = 1;
	public static final int STATE_PRESENT = 2;
	public static final int STATE_READY = 3;
	public static final int STATE_VERIFIED = 4;
	int id;
	int state;
	CaModule current;

	public CaModule getCaModule() {
		return current;
	}

	public int getId() {
		return id;
	}

	public int getState() {
		return state;
	}
}
