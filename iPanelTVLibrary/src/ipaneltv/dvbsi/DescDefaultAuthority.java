package ipaneltv.dvbsi;

public final class DescDefaultAuthority extends Descriptor {
	public static final int TAG = 0x73;

	public DescDefaultAuthority(Descriptor d) {
		super(d);
	}

	public byte[] need_to_do() {
		return sec.getBlobValue(makeLocator(".need_to_do"));
	}
}
