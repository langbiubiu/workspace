package ipaneltv.dvbsi;

public final class DescDataBroadcast extends Descriptor {
	public static final int TAG = 0x64;

	public DescDataBroadcast(Descriptor d) {
		super(d);
	}

	public int data_broadcast_id() {
		return sec.getIntValue(makeLocator(".data_broadcast_id"));
	}

	public int component_tag() {
		return sec.getIntValue(makeLocator(".component_tag"));
	}

	public byte[] selector() {
		return sec.getBlobValue(makeLocator(".selector"));
	}

	public String language() {
		return language(null);
	}

	public String language(String enc) {
		return sec.getTextValue(makeLocator(".language"), enc);
	}

	public String text() {
		return text(null);
	}

	public String text(String enc) {
		return sec.getTextValue(makeLocator(".text"), enc);
	}

}
