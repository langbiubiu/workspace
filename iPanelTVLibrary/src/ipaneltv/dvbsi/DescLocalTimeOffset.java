package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescLocalTimeOffset extends Descriptor {

	public static final int TAG = 0x58;

	public DescLocalTimeOffset(Descriptor d) {
		super(d);
	}

	public int time_offset_size() {
		return sec.getIntValue(makeLocator(".time_offset.length"));
	}

	public TimeOffset getTimeOffset(int i) {
		Section.checkIndex(i);
		return new TimeOffset(i);
	}

	public final class TimeOffset {
		int index;

		TimeOffset(int i) {
			index = i;
		}

		public int country_region_id() {
			return sec.getIntValue(makeLocator(".country_region_id"));
		}

		public int local_time_offset_polarity() {
			return sec.getIntValue(makeLocator(".local_time_offset_polarity"));
		}

		public int local_time_offset() {
			return sec.getIntValue(makeLocator(".local_time_offset"));
		}

		public int time_of_change() {
			return sec.getIntValue(makeLocator(".time_of_change"));
		}

		public int next_time_offset() {
			return sec.getIntValue(makeLocator(".next_time_offset"));
		}

		public String country_code() {
			return country_code(null);
		}

		public String country_code(String enc) {
			return sec.getTextValue(makeLocator(".country_code"), enc);
		}

		String makeLocator(String s) {
			DescLocalTimeOffset.this.setPreffixToLocator();
			sec.appendToLocator(".time_offset[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
