package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescTransportProtocol extends Descriptor {
	public static final int TAG = 0x02;
	public static final int ID_DEFAULT = 0x0000;
	public static final int ID_0001 = 0x0001;
	public static final int ID_0002 = 0x0002;
	public static final int ID_0100 = 0x0100;

	public final Id0001 id_0001 = new Id0001();
	public final Id0002 id_0002 = new Id0002();
	public final Id0100 id_0100 = new Id0100();

	public DescTransportProtocol(Descriptor d) {
		super(d);
	}

	public int protocol_id() {
		return sec.getIntValue(makeLocator(".protocol_id"));
	}

	public int transport_protocol_label() {
		return sec.getIntValue(makeLocator(".transport_protocol_label"));
	}

	public IdDefault id_default() {
		return new IdDefault();
	}

	public final class Id0001 {
		Id0001() {
		}

		public int remote_connection() {
			return sec.getIntValue(makeLocator(".remote_connection"));
		}

		public int original_network_id() {
			return sec.getIntValue(makeLocator(".original_network_id"));
		}

		public int transport_stream_id() {
			return sec.getIntValue(makeLocator(".transport_stream_id"));
		}

		public int service_id() {
			return sec.getIntValue(makeLocator(".service_id"));
		}

		public int component_tag() {
			return sec.getIntValue(makeLocator(".component_tag"));
		}

		String makeLocator(String s) {
			DescTransportProtocol.this.setPreffixToLocator();
			sec.appendToLocator(".protocol_id_0001");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public final class Id0002 {

		public int remote_connection() {
			return sec.getIntValue(makeLocator(".remote_connection"));
		}

		public int original_network_id() {
			return sec.getIntValue(makeLocator(".original_network_id"));
		}

		public int transport_stream_id() {
			return sec.getIntValue(makeLocator(".transport_stream_id"));
		}

		public int service_id() {
			return sec.getIntValue(makeLocator(".service_id"));
		}

		public int alignment_indicator() {
			return sec.getIntValue(makeLocator(".alignment_indicator"));
		}

		public int url_size() {
			return sec.getIntValue(makeLocator(".URL.length"));
		}

		public String url(int i) {
			return url(i, null);
		}

		public String url(int i, String enc) {
			Section.checkIndex(i);
			return sec.getTextValue(makeLocator(".url[" + i + "].url"), enc);
		}

		String makeLocator(String s) {
			DescTransportProtocol.this.setPreffixToLocator();
			sec.appendToLocator(".protocol_id_0002");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public final class Id0100 {

		public int remote_connection() {
			return sec.getIntValue(makeLocator(".remote_connection"));
		}

		public int original_network_id() {
			return sec.getIntValue(makeLocator(".original_network_id"));
		}

		public int transport_stream_id() {
			return sec.getIntValue(makeLocator(".transport_stream_id"));
		}

		public int service_id() {
			return sec.getIntValue(makeLocator(".service_id"));
		}

		public int component_tag() {
			return sec.getIntValue(makeLocator(".component_tag"));
		}

		String makeLocator(String s) {
			DescTransportProtocol.this.setPreffixToLocator();
			sec.appendToLocator(".protocol_id_0100");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public final class IdDefault {
		public byte[] selector() {
			return sec.getBlobValue(makeLocator(".protocol_id_default.selector"));
		}
	}

}
