package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescCellList extends Descriptor {
	public static final int TAG = 0x6c;

	public DescCellList(Descriptor d) {
		super(d);
	}

	public int cell_size() {
		return sec.getIntValue(makeLocator(".cell.length"));
	}

	public Cell cell(int i) {
		Section.checkIndex(i);
		return new Cell(i);
	}

	public final class Cell {
		int index;

		Cell(int i) {
			index = i;
		}

		public int cell_id() {
			return sec.getIntValue(makeLocator(".cell_id"));
		}

		public int cell_latitude() {
			return sec.getIntValue(makeLocator(".cell_latitude"));
		}

		public int cell_lonitude() {
			return sec.getIntValue(makeLocator(".cell_lonitude"));
		}

		public int cell_extend_of_latitude() {
			return sec.getIntValue(makeLocator(".cell_extend_of_latitude"));
		}

		public int cell_extend_of_longitude() {
			return sec.getIntValue(makeLocator(".cell_extend_of_longitude"));
		}

		public int subcell_info_size() {
			return sec.getIntValue(makeLocator(".subcell_info.length"));
		}

		public SubcellInfo getSubcellInfo(int i) {
			Section.checkIndex(i);
			return new SubcellInfo(i);
		}

		public final class SubcellInfo {
			int index;

			SubcellInfo(int i) {
				index = i;
			}

			public int cell_id_extension() {
				return sec.getIntValue(makeLocator(".cell_id_extension"));
			}

			public int subcell_latitude() {
				return sec.getIntValue(makeLocator(".subcell_latitude"));
			}

			public int subcell_longitude() {
				return sec.getIntValue(makeLocator(".subcell_longitude"));
			}

			public int subcell_extend_of_latitude() {
				return sec.getIntValue(makeLocator(".subcell_extend_of_latitude"));
			}

			public int subcell_extend_of_longitude() {
				return sec.getIntValue(makeLocator(".subcell_extend_of_longitude"));
			}

			String makeLocator(String s) {
				DescCellList.this.setPreffixToLocator();
				sec.appendToLocator(".cell[");
				sec.appendToLocator(Cell.this.index);
				sec.appendToLocator("]");
				sec.appendToLocator(".subcell_info[");
				sec.appendToLocator(index);
				sec.appendToLocator("]");
				if (s != null)
					sec.appendToLocator(s);
				return sec.getLocator();
			}
		}

		String makeLocator(String s) {
			DescCellList.this.setPreffixToLocator();
			sec.appendToLocator(".cell[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
