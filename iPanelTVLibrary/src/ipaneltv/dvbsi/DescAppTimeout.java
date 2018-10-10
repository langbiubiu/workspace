package ipaneltv.dvbsi;

public final class DescAppTimeout extends Descriptor {

	public static final int TAG = 0xfc;

	public DescAppTimeout(Descriptor d) {
		super(d);
	}

	public byte[] app_timeout() {
		return sec.getBlobValue(makeLocator(".app_timeout"));
	}
}
