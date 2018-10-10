package ipaneltv.dvbsi;

public final class DescRelatedContent extends Descriptor {
	public static final int TAG = 0x74;

	public DescRelatedContent(Descriptor d) {
		super(d);
	}

	public byte[] need_to_do() {
		return sec.getBlobValue(makeLocator(".need_to_do"));
	}
}
