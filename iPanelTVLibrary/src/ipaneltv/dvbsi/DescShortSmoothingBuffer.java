package ipaneltv.dvbsi;

public final class DescShortSmoothingBuffer extends Descriptor {
	public static final int TAG = 0x61;

	public DescShortSmoothingBuffer(Descriptor d) {
		super(d);
	}

	public int sb_size() {
		return sec.getIntValue(makeLocator(".sb_size"));
	}

	public int sb_leak_rate() {
		return sec.getIntValue(makeLocator(".sb_leak_rate"));
	}

}
