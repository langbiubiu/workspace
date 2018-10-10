package ipaneltv.dvbsi;

public final class DescTimeShiftedEvent extends Descriptor {
	public static final int TAG = 0x4f;

	public DescTimeShiftedEvent(Descriptor d) {
		super(d);
	}

	public int reference_service_id() {
		return sec.getIntValue(makeLocator(".reference_service_id"));
	}

	public int reference_event_id() {
		return sec.getIntValue(makeLocator(".reference_event_id"));
	}
}
