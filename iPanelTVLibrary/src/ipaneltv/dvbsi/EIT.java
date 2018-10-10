package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class EIT {
	public static final String TABLE_NAME = "DVBEIT";

	public static int service_id(Section s) {
		return s.getIntValue(TABLE_NAME + ".service_id");
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

	public static int transport_stream_id(Section s) {
		return s.getIntValue(TABLE_NAME + ".transport_stream_id");
	}

	public static int original_network_id(Section s) {
		return s.getIntValue(TABLE_NAME + ".original_network_id");
	}

	public static int segment_last_section_number(Section s) {
		return s.getIntValue(TABLE_NAME + ".segment_last_section_number");
	}

	public static int last_table_id(Section s) {
		return s.getIntValue(TABLE_NAME + ".last_table_id");
	}

	public static int event_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".event.length");
	}

	public static Event event(Section s, int i) {
		Section.checkIndex(i);
		return new Event(s, i);
	}

	public static final class Event {
		Section s;
		int index;

		Event(Section s, int i) {
			this.s = s;
			this.index = i;// = TABLE_NAME + "event[" + i + "].";
		}

		public int event_id() {
			setPriffixToLocator();
			s.appendToLocator(".event_id");
			return s.getIntValue(null);
		}

		/**
		 * 
		 * @return RFC 3339Ê±¼ä
		 */
		public String start_time() {
			setPriffixToLocator();
			s.appendToLocator(".start_time");
			return s.getDateValue(null);
		}

		public int duration() {
			setPriffixToLocator();
			s.appendToLocator(".duration");
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

		public int descriptor_loop_size() {
			setPriffixToLocator();
			s.appendToLocator(".descriptor_loop_length");
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
			s.appendToLocator(".event[");
			s.appendToLocator(index);
			s.appendToLocator("]");
		}
	}
}
