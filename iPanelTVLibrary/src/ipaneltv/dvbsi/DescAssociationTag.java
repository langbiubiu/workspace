package ipaneltv.dvbsi;

public final class DescAssociationTag extends Descriptor {
	public static final int TAG = 0x14;

	public DescAssociationTag(Descriptor d) {
		super(d);
	}

	public byte[] association_tag() {
		return sec.getBlobValue(makeLocator(".association_tag"));
	}
}
