package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescNVODReference extends Descriptor {
	public static final int TAG = 0x4b;

	public DescNVODReference(Descriptor d) {
		super(d);
	}

	public int references_size() {
		return sec.getIntValue(makeLocator(".reference.length"));
	}

	public References references(int i) {
		Section.checkIndex(i);
		return new References(i);
	}

	public final class References {
		int index;

		References(int i) {
			index = i;
		}

		public int transport_stream_id() {
			return sec.getIntValue(makeLocator(".transport_stream_id"));
		}

		public int original_network_id() {
			return sec.getIntValue(makeLocator(".original_network_id"));
		}

		public int service_id() {
			return sec.getIntValue(makeLocator(".service_id"));
		}

		String makeLocator(String s) {
			DescNVODReference.this.setPreffixToLocator();
			sec.appendToLocator(".reference[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
