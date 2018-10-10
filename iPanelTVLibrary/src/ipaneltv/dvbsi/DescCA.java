package ipaneltv.dvbsi;

public final class DescCA extends Descriptor {
	public static final int TAG = 0x09;

	public DescCA(Descriptor d) {
		super(d);
	}

	public int ca_system_id() {
		return sec.getIntValue(makeLocator(".CA_system_ID"));
	}

	public int ca_pid() {
		return sec.getIntValue(makeLocator(".CA_PID"));
	}

	public byte[] private_data() {
		return sec.getBlobValue(makeLocator(".private_data"));
	}
}
