package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescExternalApplicationAuthorisation extends Descriptor {
	public static final int TAG = 0x05;

	public DescExternalApplicationAuthorisation(Descriptor d) {
		super(d);
	}

	public final class Authorisation {
		int index;

		Authorisation(int i) {
			index = i;
		}

		public int organization_id() {
			return sec.getIntValue(makeLocator(".organization_id"));
		}

		public int application_id() {
			return sec.getIntValue(makeLocator(".application_id"));
		}

		public int application_priority() {
			return sec.getIntValue(makeLocator(".application_priority"));
		}

		String makeLocator(String s) {
			DescExternalApplicationAuthorisation.this.setPreffixToLocator();
			sec.appendToLocator(".authorisation[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public int authorisation_size() {
		return sec.getIntValue(makeLocator(".authorisation.length"));
	}

	public Authorisation authorisation(int i) {
		Section.checkIndex(i);
		return new Authorisation(i);
	}

}
