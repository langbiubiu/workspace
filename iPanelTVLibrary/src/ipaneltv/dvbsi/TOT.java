package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class TOT {
	public static final String TABLE_NAME = "DVBTOT";

	public static String utc_time(Section s) {
		s.clearLocator();
		s.appendToLocator(TABLE_NAME);
		s.appendToLocator(".utc_time");
		return s.getDateValue(null);
	}

	public static int descriptor_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".descriptor.length");
	}

	public static Descriptor descriptor(Section s, int i) {
		Section.checkIndex(i);
		return new Descriptor(s, TABLE_NAME, i);
	}
}
