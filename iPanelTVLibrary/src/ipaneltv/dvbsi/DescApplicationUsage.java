package ipaneltv.dvbsi;

public final class DescApplicationUsage extends Descriptor {
	public static final int TAG = 0x16;

	public DescApplicationUsage(Descriptor d) {
		super(d);
	}

	public int usage() {
		return sec.getIntValue(makeLocator(".usage"));
	}
}
