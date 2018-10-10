package ipaneltv.dvbsi;

public final class DescTimeShiftedService extends Descriptor {
	public static final int TAG = 0x4c;

	public DescTimeShiftedService(Descriptor d) {
		super(d);
	}

	public int reference_service_id() {
		return sec.getIntValue(makeLocator(".reference_service_id"));
	}
}
