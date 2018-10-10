package ipaneltv.dvbsi;

public final class DescShortEvent extends Descriptor {
	public static final int TAG = 0x4d;

	public DescShortEvent(Descriptor d) {
		super(d);
	}

	public String language() {
		return language(null);
	}

	public String language(String enc) {
		return sec.getTextValue(makeLocator(".language"), enc);
	}

	public String event_name() {
		return event_name(null);
	}

	public String event_name(String enc) {
		return sec.getTextValue(makeLocator(".event_name"), enc);
	}

	public String text() {
		return text(null);
	}

	public String text(String enc) {
		return sec.getTextValue(makeLocator(".text"), enc);
	}

}
