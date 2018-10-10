package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class SDT {
	public static final String TABLE_NAME = "DVBSDT";

	public static int transport_stream_id(Section s) {
		return s.getIntValue(TABLE_NAME + ".transport_stream_id");
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

	public static int original_network_id(Section s) {
		return s.getIntValue(TABLE_NAME + ".original_network_id");
	}

	public static int service_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".service.length");
	}

	public static Service service(Section s, int i) {
		Section.checkIndex(i);
		return new Service(s, i);
	}

	public static final class Service {
		Section s;
		int index;

		public Service(Section s, int i) {
			this.s = s;
			this.index = i;
		}

		public int service_id() {
			setPriffixToLocator();
			s.appendToLocator(".service_id");
			return s.getIntValue(null);
		}

		public int eit_schedule_flag() {
			setPriffixToLocator();
			s.appendToLocator(".eit_schedule_flag");
			return s.getIntValue(null);
		}

		public int eit_present_following_flag() {
			setPriffixToLocator();
			s.appendToLocator(".eit_present_following_flag");
			return s.getIntValue(null);
		}

		public int running_status() {
			setPriffixToLocator();
			s.appendToLocator(".running_status");
			return s.getIntValue(null);
		}

		public int free_ca_mode() {
			setPriffixToLocator();
			s.appendToLocator(".free_ca_mode");
			return s.getIntValue(null);
		}

		public int descriptor_size() {
			setPriffixToLocator();
			s.appendToLocator(".descriptor.length");
			return s.getIntValue(null);
		}

		public Descriptor descriptor(int i) {
			Section.checkIndex(i);
			setPriffixToLocator();
			return new Descriptor(s, s.getLocator(), i);
		}

		void setPriffixToLocator() {
			s.clearLocator();
			s.appendToLocator(TABLE_NAME);
			s.appendToLocator(".service[");
			s.appendToLocator(index);
			s.appendToLocator("]");
		}
	}
}
