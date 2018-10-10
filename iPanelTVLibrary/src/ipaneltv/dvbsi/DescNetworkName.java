package ipaneltv.dvbsi;

public final class DescNetworkName extends Descriptor {
	public static final int TAG = 0x40;

	public DescNetworkName(Descriptor d) {
		super(d);
	}

	public String network_name() {
		return network_name(null);
	}

	public String network_name(String enc) {
		return sec.getTextValue(makeLocator(".network_name"), enc);
	}
}
