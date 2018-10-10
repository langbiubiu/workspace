package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescLinkage extends Descriptor {
	public static final int TAG = 0x4a;
	public static final int TYPE_08 = 0x08;
	public static final int TYPE_09 = 0x09;
	public static final int TYPE_0A = 0x0a;
	public static final int TYPE_0B = 0x0b;
	public static final int TYPE_0C = 0x0c;
	public static final int TYPE_A0 = 0xa0;

	public final Type08 type08 = new Type08();

	public final Type09 type09 = new Type09();

	public final Type0A type0A = new Type0A();

	public final Type0B type0B = new Type0B();

	public final Type0C type0C = new Type0C();

	public final TypeA0 typeA0 = new TypeA0();

	public DescLinkage(Descriptor d) {
		super(d);
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

	public int linkage_type() {
		return sec.getIntValue(makeLocator(".linkage_type"));
	}

	public final class Type08 {
		Type08() {
		}

		public int hand_over_type() {
			return sec.getIntValue(makeLocator(".hand_over_type"));
		}

		public int origin_type() {
			return sec.getIntValue(makeLocator(".origin_type"));
		}

		public int network_id() {
			return sec.getIntValue(makeLocator(".network_id"));
		}

		public int initial_service_id() {
			return sec.getIntValue(makeLocator(".initial_service_id"));
		}

		String makeLocator(String s) {
			DescLinkage.this.setPreffixToLocator();
			sec.appendToLocator(".type08");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public final class Type09 {
		Type09() {
		}

		public int oui_size() {
			return sec.getIntValue(makeLocator(".type09.oui.length"));
		}

		public OUI getOUI(int i) {
			Section.checkIndex(i);
			return new OUI(i);
		}

		public final class OUI {
			int index;

			OUI(int i) {
				index = i;
			}

			public int selector_size() {
				return sec.getIntValue(makeLocator(".selector.length"));
			}

			public Selector selector(int i) {
				Section.checkIndex(i);
				return new Selector(i);
			}

			public int oui() {
				return sec.getIntValue(makeLocator(".oui"));
			}

			public final class Selector {
				int index;

				Selector(int i) {
					index = i;
				}

				public int update_type() {
					return sec.getIntValue(makeLocator(".update_type"));
				}

				public int component_tag() {
					return sec.getIntValue(makeLocator(".component_tag"));
				}

				public int hardware_version() {
					return sec.getIntValue(makeLocator(".hardware_version"));
				}

				public int software_type() {
					return sec.getIntValue(makeLocator(".software_type"));
				}

				public int software_version() {
					return sec.getIntValue(makeLocator(".software_version"));
				}

				public byte[] serial_number_start() {
					return sec.getBlobValue(makeLocator(".serial_number_start"));
				}

				public byte[] serial_number_end() {
					return sec.getBlobValue(makeLocator(".serial_number_end"));
				}

				public int control_code() {
					return sec.getIntValue(makeLocator(".control_code"));
				}

				public int private_data() {
					return sec.getIntValue(makeLocator(".private_data"));
				}

				String makeLocator(String s) {
					DescLinkage.this.setPreffixToLocator();
					sec.appendToLocator(".type09.oui[");
					sec.appendToLocator(OUI.this.index);
					sec.appendToLocator("].selector[");
					sec.appendToLocator(index);
					sec.appendToLocator("]");
					if (s != null)
						sec.appendToLocator(s);
					return sec.getLocator();
				}
			}

			String makeLocator(String s) {
				DescLinkage.this.setPreffixToLocator();
				sec.appendToLocator(".type09.oui[");
				sec.appendToLocator(index);
				sec.appendToLocator("]");
				if (s != null)
					sec.appendToLocator(s);
				return sec.getLocator();
			}
		}
	}

	public final class Type0A {
		public int table_type() {
			return sec.getIntValue(makeLocator(".type0A.table_type"));
		}
	}

	public final class Type0B {

		public int platform_id_size() {
			return sec.getIntValue(makeLocator(".type0B.platform_id.length"));
		}

		public PlatformId getPlatformId(int i) {
			Section.checkIndex(i);
			return new PlatformId(i);
		}

		public final class PlatformId {
			int index;

			PlatformId(int i) {
				index = i;
			}

			public String language() {
				return language(null);
			}

			public String language(String enc) {
				return sec.getTextValue(makeLocator(".language"), enc);
			}

			public String name() {
				return name(null);
			}

			public String name(String enc) {
				return sec.getTextValue(makeLocator(".name"), enc);
			}

			String makeLocator(String s) {
				DescLinkage.this.setPreffixToLocator();
				sec.appendToLocator(".type0B.platform_id[");
				sec.appendToLocator(index);
				sec.appendToLocator("]");
				if (s != null)
					sec.appendToLocator(s);
				return sec.getLocator();
			}
		}

	}

	public final class Type0C {

		public int table_type() {
			return sec.getIntValue(makeLocator(".type0C.table_type"));
		}

		public int bouquet_id() {
			return sec.getIntValue(makeLocator(".type0C.bouquet_id"));
		}

	}

	public final class TypeA0 {

		public int manufacturer_code() {
			return sec.getIntValue(makeLocator(".manufacturer_code"));
		}

		public int hardware_version() {
			return sec.getIntValue(makeLocator(".hardware_version"));
		}

		public int software_version() {
			return sec.getIntValue(makeLocator(".software_version"));
		}

		public int serial_number_start() {
			return sec.getIntValue(makeLocator(".serial_number_start"));
		}

		public int serial_number_end() {
			return sec.getIntValue(makeLocator(".serial_number_end"));
		}

		public int control_code() {
			return sec.getIntValue(makeLocator(".control_code"));
		}

		public byte[] user_defined_data() {
			return sec.getBlobValue(makeLocator(".user_defined_data"));
		}

		String makeLocator(String s) {
			DescLinkage.this.setPreffixToLocator();
			sec.appendToLocator(".typeA0");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
