package ipaneltv.dvbsi;

public final class DescPDC extends Descriptor {
	public static final int TAG = 0x69;

	public DescPDC(Descriptor d) {
		super(d);
	}

	public int programme_identification_label() {
		return sec.getIntValue(makeLocator(".programme_identification_label"));
	}
}
