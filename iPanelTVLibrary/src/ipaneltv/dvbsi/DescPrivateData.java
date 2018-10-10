package ipaneltv.dvbsi;

public final class DescPrivateData extends Descriptor {
	public static final int TAG = 0x8a;

	public DescPrivateData(Descriptor d) {
		super(d);
	}

	public byte[] data() {
		return sec.getBlobValue(makeLocator(".data"));
	}
}
