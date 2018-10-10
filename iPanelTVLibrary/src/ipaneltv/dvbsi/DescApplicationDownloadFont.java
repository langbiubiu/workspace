package ipaneltv.dvbsi;

public final class DescApplicationDownloadFont extends Descriptor {
	public static final int TAG = 0x0e;

	public DescApplicationDownloadFont(Descriptor d) {
		super(d);
	}

	public String download_font_locator() {
		return download_font_locator(null);
	}

	public String download_font_locator(String enc) {
		return sec.getTextValue(makeLocator(".download_font_locator"), enc);
	}
}
