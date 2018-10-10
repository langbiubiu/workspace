package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescMosaic extends Descriptor {
	public static final int TAG = 0x51;

	public static final int LINKAGE_01 = 0x01;
	public static final int LINKAGE_02 = 0x02;
	public static final int LINKAGE_03 = 0x03;
	public static final int LINKAGE_04 = 0x04;

	public DescMosaic(Descriptor d) {
		super(d);
	}

	public int mosaic_entry_point() {
		return sec.getIntValue(makeLocator(".mosaic_entry_point"));
	}

	public int number_of_horizontal_elementary_cells() {
		return sec.getIntValue(makeLocator(".number_of_horizontal_elementary_cells"));
	}

	public int number_of_vertical_elementary_cells() {
		return sec.getIntValue(makeLocator(".number_of_vertical_elementary_cells"));
	}

	public int cells_size() {
		return sec.getIntValue(makeLocator(".cells.length"));
	}

	public Cells cells(int i) {
		Section.checkIndex(i);
		return new Cells(i);
	}

	public final class Cells {
		int index;

		public final Linkage01 linkage01 = new Linkage01();

		public final Linkage02 linkage02 = new Linkage02();

		public final Linkage03 linkage03 = new Linkage03();

		public final Linkage04 linkage04 = new Linkage04();

		Cells(int i) {
			index = i;
		}

		public int logical_cell_id() {
			return sec.getIntValue(makeLocator(".logical_cell_id"));
		}

		public int logical_cell_presentation_info() {
			return sec.getIntValue(makeLocator(".logical_cell_presentation_info"));
		}

		public byte[] elementary_cell_id() {
			return sec.getBlobValue(makeLocator(".elementary_cell_id"));
		}

		public int cell_linkage_info() {
			return sec.getIntValue(makeLocator(".cell_linkage_info"));
		}

		public final class Linkage01 {
			Linkage01() {
			}

			public int bouquet_id() {
				return sec.getIntValue(makeLocator(".linkage01.bouquet_id"));
			}
		}

		public final class Linkage02 {

			public int original_network_id() {
				return sec.getIntValue(makeLocator(".linkage02.original_network_id"));
			}

			public int transport_stream_id() {
				return sec.getIntValue(makeLocator(".linkage02.transport_stream_id"));
			}

			public int service_id() {
				return sec.getIntValue(makeLocator(".linkage02.service_id"));
			}
		}

		public final class Linkage03 {

			public int original_network_id() {
				return sec.getIntValue(makeLocator(".linkage03.original_network_id"));
			}

			public int transport_stream_id() {
				return sec.getIntValue(makeLocator(".linkage03.transport_stream_id"));
			}

			public int service_id() {
				return sec.getIntValue(makeLocator(".linkage03.service_id"));
			}
		}

		public final class Linkage04 {

			public int original_network_id() {
				return sec.getIntValue(makeLocator(".linkage4.original_network_id"));
			}

			public int transport_stream_id() {
				return sec.getIntValue(makeLocator(".linkage4.transport_stream_id"));
			}

			public int service_id() {
				return sec.getIntValue(makeLocator(".linkage4.service_id"));
			}

			public int event_id() {
				return sec.getIntValue(makeLocator(".event_id"));
			}
		}

		String makeLocator(String s) {
			DescMosaic.this.setPreffixToLocator();
			sec.appendToLocator(".cells[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
