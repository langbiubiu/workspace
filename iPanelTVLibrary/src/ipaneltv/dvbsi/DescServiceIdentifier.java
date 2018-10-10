package ipaneltv.dvbsi;

public final class DescServiceIdentifier extends Descriptor {
	public static final int TAG = 0x71;

	public DescServiceIdentifier(Descriptor d) {
		super(d);
	}

	public byte[] textual_service_identifier() {
		return sec.getBlobValue(makeLocator(".textual_service_identifier"));
	}
}
