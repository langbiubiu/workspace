package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescRoutingDescriptorIP6 extends Descriptor {
	public static final int TAG = 0x07;

	public DescRoutingDescriptorIP6(Descriptor d) {
		super(d);
	}

	public final class IP6 {
		int index;

		IP6(int i) {
			index = i;
		}

		public int component_tag() {
			return sec.getIntValue(makeLocator(".component_tag"));
		}

		public String address() {
			return address(null);
		}

		public String address(String enc) {
			return sec.getTextValue(makeLocator(".address"), enc);
		}

		public int port_number() {
			return sec.getIntValue(makeLocator(".port_number"));
		}

		public String address_mask() {
			return address_mask(null);
		}

		public String address_mask(String enc) {
			return sec.getTextValue(makeLocator(".address_mask"), enc);
		}

		String makeLocator(String s) {
			DescRoutingDescriptorIP6.this.setPreffixToLocator();
			sec.appendToLocator(".ip6[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public int ip6_size() {
		return sec.getIntValue(makeLocator(".ip4.length"));
	}

	public IP6 ip6(int i) {
		Section.checkIndex(i);
		return new IP6(i);
	}
}
