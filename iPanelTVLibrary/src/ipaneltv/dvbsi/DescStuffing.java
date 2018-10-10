package ipaneltv.dvbsi;

public final class DescStuffing extends Descriptor {
	public static final int TAG = 0x42;

	public DescStuffing(Descriptor d) {
		super(d);
	}

	public byte[] stuffing_byte() {
		return sec.getBlobValue(makeLocator(".stuffing_byte"));
	}
}
