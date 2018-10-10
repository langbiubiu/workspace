package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescApplicationName extends Descriptor {
	public static final int TAG = 0x01;

	public DescApplicationName(Descriptor d) {
		super(d);
	}

	public final class Name {
		int index;

		Name(int i) {
			index = i;
		}

		public String language() {
			return language(null);
		}

		public String language(String enc) {
			return sec.getTextValue(makeLocator(".language"), enc);
		}

		public String application_name() {
			return application_name(null);
		}

		public String application_name(String enc) {
			return sec.getTextValue(makeLocator(".application_name"), enc);
		}

		String makeLocator(String s) {
			DescApplicationName.this.setPreffixToLocator();
			sec.appendToLocator(".name[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public int name_size() {
		return sec.getIntValue(makeLocator(".name.length"));
	}

	public Name name(int i) {
		Section.checkIndex(i);
		return new Name(i);
	}
}
