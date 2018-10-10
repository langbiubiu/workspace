package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class PMT {
	public static final String TABLE_NAME = "PMT";

	public static int program_number(Section s) {
		return s.getIntValue(TABLE_NAME + ".program_number");
	}

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

	public static int pcr_pid(Section s) {
		return s.getIntValue(TABLE_NAME + ".PCR_PID");
	}

	public static int descriptor_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".descriptor.length");
	}

	public static Descriptor descriptor(Section s, int i) {
		Section.checkIndex(i);
		return new Descriptor(s, TABLE_NAME, i);
	}

	public static int component_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".component.length");
	}

	public static Component component(Section s, int i) {
		Section.checkIndex(i);
		return new Component(s, i);
	}

	public static final class Component {
		int index;
		Section s;

		Component(Section s, int index) {
			this.s = s;
			this.index = index;
		}

		public int stream_type() {
			setPriffixToLocator();
			s.appendToLocator(".stream_type");
			return s.getIntValue(null);
		}

		public int elementary_pid() {
			setPriffixToLocator();
			s.appendToLocator(".elementary_pid");
			return s.getIntValue(null);
		}

		public int descriptor_size() {
			setPriffixToLocator();
			s.appendToLocator(".descriptor.length");
			return s.getIntValue(null);
		}

		public Descriptor descriptor(int di) {
			setPriffixToLocator();
			return new Descriptor(s, s.getLocator(), di);
		}

		void setPriffixToLocator() {
			s.clearLocator();
			s.appendToLocator(TABLE_NAME);
			s.appendToLocator(".component[");
			s.appendToLocator(index);
			s.appendToLocator("]");
		}
	}

}
