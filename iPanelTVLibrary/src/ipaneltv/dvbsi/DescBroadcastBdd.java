package ipaneltv.dvbsi;

public final class DescBroadcastBdd extends Descriptor {
	public static final int TAG = 0x8b;

	public DescBroadcastBdd(Descriptor d) {
		super(d);
	}

	public byte[] data() {
		return sec.getBlobValue(makeLocator(".data"));
	}
}
