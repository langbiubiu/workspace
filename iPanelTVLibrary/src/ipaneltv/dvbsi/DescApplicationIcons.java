package ipaneltv.dvbsi;

public final class DescApplicationIcons extends Descriptor {
	public static final int TAG = 0x0b;

	public DescApplicationIcons(Descriptor d) {
		super(d);
	}

	public String icon_locator() {
		return icon_locator(null);
	}

	public String icon_locator(String enc) {
		return sec.getTextValue(makeLocator(".icon_locator"), enc);
	}

	public int icon_flags() {
		return sec.getIntValue(makeLocator(".icon_flags"));
	}
}
