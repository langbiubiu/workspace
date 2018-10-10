package ipaneltv.dvbsi;

public final class DescAncillaryData extends Descriptor {
	public static final int TAG = 0x6b;

	public DescAncillaryData(Descriptor d) {
		super(d);
	}

	public int ancillary_data_identifier() {
		return sec.getIntValue(makeLocator(".ancillary_data_identifier"));
	}
}
