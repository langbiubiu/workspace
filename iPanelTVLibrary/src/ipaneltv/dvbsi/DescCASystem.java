package ipaneltv.dvbsi;

public final class DescCASystem extends Descriptor {
	public static final int TAG = 0x65;

	public DescCASystem(Descriptor d) {
		super(d);
	}

	public byte[] ca_system() {
		return sec.getBlobValue(makeLocator(".ca_system"));
	}
}
