package ipaneltv.dvbsi;

public final class DescHTMLApplication extends Descriptor {
	public static final int TAG = 0x08;

	public DescHTMLApplication(Descriptor d) {
		super(d);
	}

	public byte[] application_id() {
		return sec.getBlobValue(makeLocator(".application_id"));
	}

	public byte[] parameter() {
		return sec.getBlobValue(makeLocator(".parameter"));
	}
}
