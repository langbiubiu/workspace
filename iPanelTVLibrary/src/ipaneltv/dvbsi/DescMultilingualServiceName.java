package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescMultilingualServiceName extends Descriptor {

	public static final int TAG = 0x5D;

	public DescMultilingualServiceName(Descriptor d) {
		super(d);
	}

	public int service_name_size() {
		return sec.getIntValue(makeLocator(".service_name.length"));
	}

	public ServiceName service_name(int i) {
		Section.checkIndex(i);
		return new ServiceName(i);
	}

	public final class ServiceName {
		int index;

		ServiceName(int i) {
			index = i;
		}

		public String language() {
			return language(null);
		}

		public String language(String enc) {
			return sec.getTextValue(makeLocator(".language"), enc);
		}

		public String service_provider_name() {
			return service_provider_name(null);
		}

		public String service_provider_name(String enc) {
			return sec.getTextValue(makeLocator(".service_provider_name"), enc);
		}

		public String service_name() {
			return service_name(null);
		}

		public String service_name(String enc) {
			return sec.getTextValue(makeLocator(".service_name"), enc);
		}

		String makeLocator(String s) {
			DescMultilingualServiceName.this.setPreffixToLocator();
			sec.appendToLocator(".service_name[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
