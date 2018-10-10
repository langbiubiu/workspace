package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescVBIData extends Descriptor {
	public static final int TAG = 0x45;

	public DescVBIData(Descriptor d) {
		super(d);
	}

	public final class DataServices {
		int index;

		DataServices(int i) {
			index = i;
		}

		public int data_service_id() {
			return sec.getIntValue(makeLocator(".data_service_id"));
		}

		public int field_parity() {
			return sec.getIntValue(makeLocator(".field_parity"));
		}

		public int line_offset() {
			return sec.getIntValue(makeLocator(".line_offset"));
		}

		String makeLocator(String s) {
			DescVBIData.this.setPreffixToLocator();
			sec.appendToLocator(".data_services[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public DataServices data_services(int i) {
		Section.checkIndex(i);
		return new DataServices(i);
	}

	public int data_services_size() {
		return sec.getIntValue(makeLocator(".data_services.length"));
	}

}
