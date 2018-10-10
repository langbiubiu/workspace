package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescContent extends Descriptor {
	public static final int TAG = 0x54;

	public DescContent(Descriptor d) {
		super(d);
	}

	public int content_size() {
		return sec.getIntValue(makeLocator(".content.length"));
	}

	public Content content(int i) {
		Section.checkIndex(i);
		return new Content(i);
	}

	public final class Content {
		int index;

		Content(int i) {
			index = i;
		}

		public int content_nibble_level_1() {
			return sec.getIntValue(makeLocator(".content_nibble_level_1"));
		}

		public int content_nibble_level_2() {
			return sec.getIntValue(makeLocator(".content_nibble_level_2"));
		}

		public int user_nibble_1() {
			return sec.getIntValue(makeLocator(".user_nibble_1"));
		}

		public int user_nibble_2() {
			return sec.getIntValue(makeLocator(".user_nibble_2"));
		}

		String makeLocator(String s) {
			DescContent.this.setPreffixToLocator();
			sec.appendToLocator(".content[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
