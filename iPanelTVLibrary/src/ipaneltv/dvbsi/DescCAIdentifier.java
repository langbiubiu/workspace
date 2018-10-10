package ipaneltv.dvbsi;

public final class DescCAIdentifier extends Descriptor {
	public static final int TAG = 0x53;

	public DescCAIdentifier(Descriptor d) {
		super(d);
	}

	public int ca_system_id() {
		return sec.getIntValue(makeLocator(".CA_system_id"));
	}
}
