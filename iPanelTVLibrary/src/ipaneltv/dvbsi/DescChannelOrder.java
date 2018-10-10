package ipaneltv.dvbsi;

public final class DescChannelOrder extends Descriptor {

	public static final int TAG = 0x82;

	public DescChannelOrder(Descriptor d) {
		super(d);
	}

	public byte[] channel_order() {
		return sec.getBlobValue(makeLocator(".channel_order"));
	}
}
