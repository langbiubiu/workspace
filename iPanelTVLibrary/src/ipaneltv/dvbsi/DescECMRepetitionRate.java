package ipaneltv.dvbsi;

public final class DescECMRepetitionRate extends Descriptor {

	public static final int TAG = 0x78;

	public DescECMRepetitionRate(Descriptor d) {
		super(d);
	}

	public byte[] need_to_do() {
		return sec.getBlobValue(makeLocator(".need_to_do"));
	}
}
