package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class CAT {
	public static final String TABLE_NAME = "CAT";

	public static int version_number(Section s) {
		return s.getIntValue(TABLE_NAME + ".version_number");
	}

	public static int current_next_indicator(Section s) {
		return s.getIntValue(TABLE_NAME + ".current_next_indicator");
	}

	public static int section_number(Section s) {
		return s.getIntValue(TABLE_NAME + ".section_number");
	}

	public static int last_section_number(Section s) {
		return s.getIntValue(TABLE_NAME + ".last_section_number");
	}

	public static int descriptor_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".descriptor.length");
	}

	public static Descriptor descriptor(Section s, int i) {
		Section.checkIndex(i);
		return new Descriptor(s, TABLE_NAME, i);
	}
}
