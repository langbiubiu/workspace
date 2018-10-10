package ipaneltv.dvbsi;

public final class DescDownloadLinkage extends Descriptor {
	public static final int TAG = 0xc0;

	public DescDownloadLinkage(Descriptor d) {
		super(d);
	}

	public byte[] download_linkage() {
		return sec.getBlobValue(makeLocator(".download_linkage"));
	}
}
