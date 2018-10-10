package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class PAT {

	public static final String TABLE_NAME = "PAT";

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

	public static int program_size(Section s) {
		return s.getIntValue(TABLE_NAME + ".program.length");
	}

	public static Program program(Section s, int i) {
		Section.checkIndex(i);
		return new Program(s, i);
	}

	public static final class Program {
		Section s;
		int index;

		Program(Section s, int index) {
			this.s = s;
			this.index = index;
		}

		public int program_number() {
			setPriffixToLocator();
			s.appendToLocator(".program_number");
			return s.getIntValue(null);
		}

		public int network_id() {
			setPriffixToLocator();
			s.appendToLocator(".network_id");
			return s.getIntValue(null);
		}

		public int program_map_pid() {
			setPriffixToLocator();
			s.appendToLocator(".program_map_PID");
			return s.getIntValue(null);
		}

		void setPriffixToLocator() {
			s.clearLocator();
			s.appendToLocator(TABLE_NAME);
			s.appendToLocator(".program[");
			s.appendToLocator(index);
			s.appendToLocator("]");
		}
	}

}
