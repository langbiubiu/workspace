package ipaneltv.dvbsi;

public final class DescHTMLapplicationBoundary extends Descriptor {
	public static final int TAG = 0x0a;

	public DescHTMLapplicationBoundary(Descriptor d) {
		super(d);
	}

	public String label() {
		return label(null);
	}

	public String label(String enc) {
		return sec.getTextValue(makeLocator(".label"), enc);
	}

	public String regular_expression() {
		return regular_expression(null);
	}

	public String regular_expression(String enc) {
		return sec.getTextValue(makeLocator(".regular_expression"), enc);
	}
}
