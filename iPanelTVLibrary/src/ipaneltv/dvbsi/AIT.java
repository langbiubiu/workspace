package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class AIT {
	public static final String TABLE_NAME = "DVBAIT";

	public static int application_type(Section s) {
		return s.getIntValue(TABLE_NAME + ".application_type");
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

	public static int descriptor_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".descriptor.length");
	}

	public static Descriptor descriptor(Section s, int i) {
		Section.checkIndex(i);
		return new Descriptor(s, TABLE_NAME, i);
	}

	public static int application_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".application.length");
	}

	public static AIT.Application getApplication(Section s, int i) {
		Section.checkIndex(i);
		return new AIT.Application(s, i);
	}

	public static final class Application {
		Section s;
		int index;

		Application(Section s, int i) {
			this.s = s;
			this.index = i;
		}

		public int organization_id() {
			setPriffixToLocator();
			s.appendToLocator(".organization_id");
			return s.getIntValue(null);
		}

		public int application_id() {
			setPriffixToLocator();
			s.appendToLocator(".application_id");
			return s.getIntValue(null);
		}

		public int control_code() {
			setPriffixToLocator();
			s.appendToLocator(".control_code");
			return s.getIntValue(null);
		}

		public int descriptor_size(Section s) {
			setPriffixToLocator();
			s.appendToLocator(".descriptor_size");
			return s.getIntValue(null);
		}

		public Descriptor descriptor_size(Section s, int i) {
			Section.checkIndex(i);
			setPriffixToLocator();
			return new Descriptor(s, s.getLocator(), i);
		}

		void setPriffixToLocator() {
			s.clearLocator();
			s.appendToLocator(TABLE_NAME);
			s.appendToLocator(".application[");
			s.appendToLocator(index);
			s.appendToLocator("]");
		}
	}

}
