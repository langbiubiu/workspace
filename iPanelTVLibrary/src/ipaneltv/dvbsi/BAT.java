package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class BAT {
	public static final String TABLE_NAME = "DVBBAT";

	public static int bouquet_id(Section s) {
		return s.getIntValue(TABLE_NAME + ".bouquet_id");
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

	public static int transport_stream_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".transport_stream.length");
	}

	public static TransportStream getTransportStream(Section s, int i) {
		Section.checkIndex(i);
		return new TransportStream(s, i);
	}

	public static final class TransportStream {
		Section s;
		int index;

		TransportStream(Section s, int i) {
			this.s = s;
			this.index = i;
		}

		public int transport_stream_id() {
			setPriffixToLocator();
			s.appendToLocator(".transport_stream_id");
			return s.getIntValue(null);
		}

		public int original_network_id() {
			setPriffixToLocator();
			s.appendToLocator(".original_network_id");
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
			s.appendToLocator(".transport_stream[");
			s.appendToLocator(index);
			s.appendToLocator("]");
		}
	}

}
