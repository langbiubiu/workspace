package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class TDT {
	public static final String TABLE_NAME = "DVBTDT";

	public static String utc_time(Section s) {
		s.clearLocator();
		s.appendToLocator(TABLE_NAME);
		s.appendToLocator(".utc_time");
		return s.getDateValue(null);
	}
}
