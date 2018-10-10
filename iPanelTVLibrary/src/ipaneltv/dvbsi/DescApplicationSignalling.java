package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescApplicationSignalling extends Descriptor {
	public static final int TAG = 0x6f;

	public DescApplicationSignalling(Descriptor d) {
		super(d);
	}

	public int signalling_size() {
		return sec.getIntValue(makeLocator(".signalling.length"));
	}

	public Signalling signalling(int i) {
		Section.checkIndex(i);
		return new Signalling(i);
	}

	public final class Signalling {
		int index;

		Signalling(int i) {
			index = i;
		}

		public int application_type() {
			return sec.getIntValue(makeLocator(".application_type"));
		}

		public int ait_version_number() {
			return sec.getIntValue(makeLocator(".AIT_version_number"));
		}

		String makeLocator(String s) {
			DescApplicationSignalling.this.setPreffixToLocator();
			sec.appendToLocator(".signalling[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
