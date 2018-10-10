package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescSubtitling extends Descriptor {

	public static final int TAG = 0x59;

	public DescSubtitling(Descriptor d) {
		super(d);
	}

	public int subtitling_size() {
		return sec.getIntValue(makeLocator(".subtitling.length"));
	}

	public Subtitling subtitling(int i) {
		Section.checkIndex(i);
		return new Subtitling(i);
	}

	public final class Subtitling {
		int index;

		Subtitling(int i) {
			index = i;
		}

		public int ancillary_page_id() {
			return sec.getIntValue(makeLocator(".ancillary_page_id"));
		}

		public int composition_page_id() {
			return sec.getIntValue(makeLocator(".composition_page_id"));
		}

		public int subtitling_type() {
			return sec.getIntValue(makeLocator(".subtitling_type"));
		}

		public String language() {
			return language(null);
		}

		public String language(String enc) {
			return sec.getTextValue(makeLocator(".iso_639_language_code"), enc);
		}

		String makeLocator(String s) {
			DescSubtitling.this.setPreffixToLocator();
			sec.appendToLocator(".subtitling[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
