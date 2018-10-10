package ipaneltv.dvbsi;

public final class DescBouquetName extends Descriptor {
	public static final int TAG = 0x47;

	public DescBouquetName(Descriptor d) {
		super(d);
	}

	public String bouquet_name() {
		return bouquet_name(null);
	}

	public String bouquet_name(String enc) {
		return sec.getTextValue(makeLocator(".bouquet_name"), enc);
	}
}
