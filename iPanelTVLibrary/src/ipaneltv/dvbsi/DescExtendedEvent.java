package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescExtendedEvent extends Descriptor {
	public static final int TAG = 0x4e;

	public DescExtendedEvent(Descriptor d) {
		super(d);
	}

	public int descriptor_number() {
		return sec.getIntValue(makeLocator(".descriptor_number"));
	}

	public int last_descriptor_number() {
		return sec.getIntValue(makeLocator(".last_descriptor_number"));
	}

	public String language() {
		return language(null);
	}

	public String language(String enc) {
		return sec.getTextValue(makeLocator(".language"), enc);
	}

	public String text() {
		return text(null);
	}

	public String text(String enc) {
		return sec.getTextValue(makeLocator(".text"), enc);
	}

	public int item_size() {
		return sec.getIntValue(makeLocator(".item.length"));
	}

	public Item item(int i) {
		Section.checkIndex(i);
		return new Item(i);
	}

	public final class Item {
		int index;

		Item(int i) {
			index = i;
		}

		public String item_description() {
			return item_description(null);
		}

		public String item_description(String enc) {
			return sec.getTextValue(makeLocator(".item_description"), enc);
		}

		public String item() {
			return item(null);
		}

		public String item(String enc) {
			return sec.getTextValue(makeLocator(".item"), enc);
		}

		String makeLocator(String s) {
			DescExtendedEvent.this.setPreffixToLocator();
			sec.appendToLocator(".item[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
