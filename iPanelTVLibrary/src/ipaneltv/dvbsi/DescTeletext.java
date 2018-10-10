package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescTeletext extends Descriptor {

	public static final int TAG = 0x56;

	public DescTeletext(Descriptor d) {
		super(d);
	}

	public int teletext_size() {
		return sec.getIntValue(makeLocator(".teletext.length"));
	}

	public Teletext teletext(int i) {
		Section.checkIndex(i);
		return new Teletext(i);
	}

	public final class Teletext {
		int index;

		Teletext(int i) {
			index = i;
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

		public String language(String enc) {
			return sec.getTextValue(makeLocator(".language"), enc);
		}

		public String language() {
			return language(null);
		}

		String makeLocator(String s) {
			DescTeletext.this.setPreffixToLocator();
			sec.appendToLocator(".teletext[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
