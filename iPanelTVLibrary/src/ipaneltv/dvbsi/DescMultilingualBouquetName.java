package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescMultilingualBouquetName extends Descriptor {

	public static final int TAG = 0x5C;

	public DescMultilingualBouquetName(Descriptor d) {
		super(d);
	}

	public int bouquet_name_size() {
		return sec.getIntValue(makeLocator(".bouquet_name.length"));
	}

	public BouquetName bouquet_name(int i) {
		Section.checkIndex(i);
		return new BouquetName(i);
	}

	public final class BouquetName {
		int index;

		BouquetName(int i) {
			index = i;
		}

		public String language() {
			return language(null);
		}

		public String language(String enc) {
			return sec.getTextValue(makeLocator(".iso_639_language_code"), enc);
		}

		public String name() {
			return name(null);
		}

		public String name(String enc) {
			return sec.getTextValue(makeLocator(".name"), enc);
		}

		String makeLocator(String s) {
			DescMultilingualBouquetName.this.setPreffixToLocator();
			sec.appendToLocator(".bouquet_name[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
