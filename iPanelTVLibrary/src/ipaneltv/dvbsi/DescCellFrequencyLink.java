package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescCellFrequencyLink extends Descriptor {
	public static final int TAG = 0x6d;

	public DescCellFrequencyLink(Descriptor d) {
		super(d);
	}

	public int cell_size() {
		return sec.getIntValue(makeLocator(".cell.length"));
	}

	public Cell getCell(int i) {
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

		public int frequency() {
			return sec.getIntValue(makeLocator(".cell_latitude"));
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

			public int transposer_frequency() {
				return sec.getIntValue(makeLocator(".transposer_frequency"));
			}

			String makeLocator(String s) {
				DescCellFrequencyLink.this.setPreffixToLocator();
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
			DescCellFrequencyLink.this.setPreffixToLocator();
			sec.appendToLocator(".cell[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
