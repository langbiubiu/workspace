package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescRoutingDescriptorIP4 extends Descriptor {
	public static final int TAG = 0x06;

	public DescRoutingDescriptorIP4(Descriptor d) {
		super(d);
	}

	public final class IP4 {
		int index;

		IP4(int i) {
			index = i;
		}

		public int component_tag() {
			return sec.getIntValue(makeLocator(".component_tag"));
		}

		public int address() {
			return sec.getIntValue(makeLocator(".address"));
		}

		public int port_number() {
			return sec.getIntValue(makeLocator(".port_number"));
		}

		public int address_mask() {
			return sec.getIntValue(makeLocator(".address_mask"));
		}

		String makeLocator(String s) {
			DescRoutingDescriptorIP4.this.setPreffixToLocator();
			sec.appendToLocator(".ip4[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public int ip4_size() {
		return sec.getIntValue(makeLocator(".ip4.length"));
	}

	public IP4 ip4(int i) {
		Section.checkIndex(i);
		return new IP4(i);
	}

}
