package ipaneltv.dvbsi;

public final class DescAdaptationFieldData extends Descriptor {
	public static final int TAG = 0x70;

	public DescAdaptationFieldData(Descriptor d) {
		super(d);
	}

	public int adaptation_field_data_identifier() {
		return sec.getIntValue(makeLocator(".adaptation_field_data_identifier"));
	}
}
