package ipaneltv.dvbsi;

public final class DescTimeSliceFecIdentifier extends Descriptor {

	public static final int TAG = 0x77;

	public DescTimeSliceFecIdentifier(Descriptor d) {
		super(d);
	}

	public byte[] need_to_do() {
		return sec.getBlobValue(makeLocator(".need_to_do"));
	}
}
