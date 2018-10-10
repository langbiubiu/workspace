package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescFrequencyList extends Descriptor {
	public static final int TAG = 0x62;

	public DescFrequencyList(Descriptor d) {
		super(d);
	}

	public int coding_type() {
		return sec.getIntValue(makeLocator(".coding_type"));
	}

	public int frequency_size() {
		return sec.getIntValue(makeLocator(".frequency.length"));
	}

	public int frequency_centre_frequency(int i) {
		Section.checkIndex(i);
		return sec.getIntValue(makeLocator(".frequency[" + i + "].centre_frequency"));
	}
}
