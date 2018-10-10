package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescPrefetch extends Descriptor {
	public static final int TAG = 0x0c;

	public DescPrefetch(Descriptor d) {
		super(d);
	}

	public int transport_protocol_label() {
		return sec.getIntValue(makeLocator(".transport_protocol_label"));
	}

	public final class Label {
		int index;

		Label(int i) {
			index = i;
		}

		public String label() {
			return label(null);
		}

		public String label(String enc) {
			return sec.getTextValue(makeLocator(".label"), enc);
		}

		public int prefetch_priority() {
			return sec.getIntValue(makeLocator(".prefetch_priority"));
		}

		String makeLocator(String s) {
			DescPrefetch.this.setPreffixToLocator();
			sec.appendToLocator(".label[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public int label_size() {
		return sec.getIntValue(makeLocator(".label.length"));
	}

	public Label label(int i) {
		Section.checkIndex(i);
		return new Label(i);
	}

}
