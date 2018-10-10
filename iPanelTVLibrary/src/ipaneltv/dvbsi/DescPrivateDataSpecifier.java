package ipaneltv.dvbsi;

public final class DescPrivateDataSpecifier extends Descriptor {
	public static final int TAG = 0x5f;

	public DescPrivateDataSpecifier(Descriptor d) {
		super(d);
	}

	public int private_data_specifier() {
		return sec.getIntValue(makeLocator(".private_data_specifier"));
	}
}
