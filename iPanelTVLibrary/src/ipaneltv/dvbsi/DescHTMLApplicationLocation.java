package ipaneltv.dvbsi;

public final class DescHTMLApplicationLocation extends Descriptor {
	public static final int TAG = 0x09;

	public DescHTMLApplicationLocation(Descriptor d) {
		super(d);
	}

	public String physical_root() {
		return physical_root(null);
	}

	public String physical_root(String enc) {
		return sec.getTextValue(makeLocator(".physical_root"), enc);
	}

	public String initial_path() {
		return initial_path(null);
	}

	public String initial_path(String enc) {
		return sec.getTextValue(makeLocator(".initial_path"), enc);
	}
}
