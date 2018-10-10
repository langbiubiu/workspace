package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescMultilingualNetworkName extends Descriptor {

	public static final int TAG = 0x5b;

	public DescMultilingualNetworkName(Descriptor d) {
		super(d);
	}

	public int network_name_size() {
		return sec.getIntValue(makeLocator(".network_name.length"));
	}

	public NetworkName network_name(int i) {
		Section.checkIndex(i);
		return new NetworkName(i);
	}

	public final class NetworkName {
		int index;

		NetworkName(int i) {
			index = i;
		}

		public String language() {
			return language(null);
		}

		public String language(String enc) {
			return sec.getTextValue(makeLocator(".iso_639_language_code"), enc);
		}

		public String name() {
			return name(null);
		}

		public String name(String enc) {
			return sec.getTextValue(makeLocator(".name"), enc);
		}

		String makeLocator(String s) {
			DescMultilingualNetworkName.this.setPreffixToLocator();
			sec.appendToLocator(".network_name[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
