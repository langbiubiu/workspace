package ipaneltv.dvbsi;

public final class DescComponent extends Descriptor {
	public static final int TAG = 0x50;

	public DescComponent(Descriptor d) {
		super(d);
	}

	public int stream_content() {
		return sec.getIntValue(makeLocator(".stream_content"));
	}

	public int component_type() {
		return sec.getIntValue(makeLocator(".component_type"));
	}

	public int component_tag() {
		return sec.getIntValue(makeLocator(".component_tag"));
	}

	public String language() {
		return language(null);
	}

	public String language(String enc) {
		return sec.getTextValue(makeLocator(".language"), enc);
	}

	public String text() {
		return text(null);
	}

	public String text(String enc) {
		return sec.getTextValue(makeLocator(".text"), enc);
	}
}
