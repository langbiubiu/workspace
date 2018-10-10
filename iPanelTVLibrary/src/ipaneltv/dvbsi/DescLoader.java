package ipaneltv.dvbsi;

public final class DescLoader extends Descriptor {

	public static final int TAG = 0xc0;

	public DescLoader(Descriptor d) {
		super(d);
	}

	public byte[] loader() {
		return sec.getBlobValue(makeLocator(".loader"));
	}
}
