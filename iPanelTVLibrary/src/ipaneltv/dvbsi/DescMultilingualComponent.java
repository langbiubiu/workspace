package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescMultilingualComponent extends Descriptor {
	public static final int TAG = 0x5e;

	public DescMultilingualComponent(Descriptor d) {
		super(d);
	}

	public int component_tag() {
		return sec.getIntValue(makeLocator(".component_tag"));
	}

	public int component_size() {
		return sec.getIntValue(makeLocator(".component.length"));
	}

	public Component component(int i) {
		Section.checkIndex(i);
		return new Component(i);
	}

	public final class Component {
		int index;

		Component(int i) {
			index = i;
		}

		public String language() {
			return language(null);
		}

		public String language(String enc) {
			return sec.getTextValue(makeLocator(".language"), enc);
		}

		public String text_description() {
			return text_description(null);
		}

		public String text_description(String enc) {
			return sec.getTextValue(makeLocator(".text_description"), enc);
		}

		String makeLocator(String s) {
			DescMultilingualComponent.this.setPreffixToLocator();
			sec.appendToLocator(".component[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
