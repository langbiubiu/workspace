package ipaneltv.dvbsi;

public final class DescJavaApplication extends Descriptor {
	public static final int TAG = 0x03;

	public DescJavaApplication(Descriptor d) {
		super(d);
	}

	public byte[] parameter() {
		return sec.getBlobValue(makeLocator(".parameter"));
	}

}
