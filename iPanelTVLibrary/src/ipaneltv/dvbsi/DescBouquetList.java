package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescBouquetList extends Descriptor {
	public static final int TAG = 0xb4;

	public DescBouquetList(Descriptor d) {
		super(d);
	}

	public int bouquet_size() {
		return sec.getIntValue(makeLocator(".bouquet.length"));
	}

	public Bouquet bouquet(int i) {
		Section.checkIndex(i);
		return new Bouquet(i);
	}

	public final class Bouquet {
		int index;

		Bouquet(int i) {
			index = i;
		}

		public int bouquet_id() {
			return sec.getIntValue(makeLocator(".bouquet_id"));
		}

		public int region_id() {
			return sec.getIntValue(makeLocator(".region_id"));
		}

		public int class_id() {
			return sec.getIntValue(makeLocator(".class_id"));
		}

		String makeLocator(String s) {
			DescBouquetList.this.setPreffixToLocator();
			sec.appendToLocator(".bouquet[");// TODO х╥хооб
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
