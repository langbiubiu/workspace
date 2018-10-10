package ipaneltv.dvbsi;

public final class DescStreamIdentifier extends Descriptor {
	public static final int TAG = 0x52;

	public DescStreamIdentifier(Descriptor d) {
		super(d);
	}

	public int component_tag() {
		return sec.getIntValue(makeLocator(".component_tag"));
	}
}
