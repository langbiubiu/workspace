package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescVBITeletext extends Descriptor {
	public static final int TAG = 0x46;

	public DescVBITeletext(Descriptor d) {
		super(d);
	}

	public final class Teletextes {
		int index;

		Teletextes(int i) {
			index = i;
		}

		public String language() {
			return language(null);
		}

		public String language(String enc) {
			return sec.getTextValue(makeLocator(".language"), enc);
		}

		public int teletext_type() {
			return sec.getIntValue(makeLocator(".teletext_type"));
		}

		public int teletext_magazine_number() {
			return sec.getIntValue(makeLocator(".teletext_magazine_number"));
		}

		public int teletext_page_number() {
			return sec.getIntValue(makeLocator(".teletext_page_number"));
		}

		String makeLocator(String s) {
			DescVBITeletext.this.setPreffixToLocator();
			sec.appendToLocator(".teletextes[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public int teletextes() {
		return sec.getIntValue(makeLocator(".teletextes.length"));
	}

	public Teletextes teletextes(int i) {
		Section.checkIndex(i);
		return new Teletextes(i);
	}

}
