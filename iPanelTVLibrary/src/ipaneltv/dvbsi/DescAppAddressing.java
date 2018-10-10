package ipaneltv.dvbsi;

public final class DescAppAddressing extends Descriptor {

	public static final int TAG = 0xb4;

	public DescAppAddressing(Descriptor d) {
		super(d);
	}

	public byte[] app_addressing() {
		return sec.getBlobValue(makeLocator(".app_addressing"));
	}
}
