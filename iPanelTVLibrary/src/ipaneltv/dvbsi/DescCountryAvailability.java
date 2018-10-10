package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescCountryAvailability extends Descriptor {
	public static final int TAG = 0x49;

	public DescCountryAvailability(Descriptor d) {
		super(d);
	}

	public int country_availability_tag() {
		return sec.getIntValue(makeLocator(".country_availability_tag"));
	}

	public String country_code(int i) {
		return country_code(i, null);
	}

	public String country_code(int i, String enc) {
		Section.checkIndex(i);
		return sec.getTextValue(makeLocator(".country[" + i + "].country_code"), enc);
	}

	public int country_size() {
		return sec.getIntValue(makeLocator(".country.length"));
	}

}
