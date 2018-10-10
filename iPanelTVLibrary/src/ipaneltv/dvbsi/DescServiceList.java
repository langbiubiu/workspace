package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescServiceList extends Descriptor {
	public static final int TAG = 0x41;

	public DescServiceList(Descriptor d) {
		super(d);
	}

	public final class Service {
		int index;

		Service(int i) {
			index = i;
		}

		public int service_id() {
			return sec.getIntValue(makeLocator(".service_id"));
		}

		public int service_type() {
			return sec.getIntValue(makeLocator(".service_type"));
		}

		String makeLocator(String s) {
			DescServiceList.this.setPreffixToLocator();
			sec.appendToLocator(".service[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public int service_size() {
		return sec.getIntValue(makeLocator(".service.length"));
	}

	public Service service(int i) {
		Section.checkIndex(i);
		return new Service(i);
	}
}
