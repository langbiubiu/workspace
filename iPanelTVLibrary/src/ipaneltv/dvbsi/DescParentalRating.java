package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescParentalRating extends Descriptor {

	public static final int TAG = 0x55;

	public DescParentalRating(Descriptor d) {
		super(d);
	}

	public int parental_rating_size() {
		return sec.getIntValue(makeLocator(".parental_rating.length"));
	}

	public ParentalRating parental_rating(int i) {
		Section.checkIndex(i);
		return new ParentalRating(i);
	}

	public final class ParentalRating {
		int index;

		ParentalRating(int i) {
			index = i;
		}

		public int rating() {
			return sec.getIntValue(makeLocator(".rating"));
		}

		public String country_code() {
			return country_code(null);
		}

		public String country_code(String enc) {
			return sec.getTextValue(makeLocator(".country_code"), enc);
		}

		String makeLocator(String s) {
			DescParentalRating.this.setPreffixToLocator();
			sec.appendToLocator(".parental_rating[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
