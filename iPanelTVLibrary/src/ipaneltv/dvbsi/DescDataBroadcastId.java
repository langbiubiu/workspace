package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescDataBroadcastId extends Descriptor {
	public static final int TAG = 0x66;

	public DescDataBroadcastId(Descriptor d) {
		super(d);
	}

	public int data_broadcast_id() {
		return sec.getIntValue(makeLocator(".data_broadcast_id"));
	}

	public int application_type_size() {
		return sec.getIntValue(makeLocator(".application.length"));
	}

	public int application_type(int i) {
		Section.checkIndex(i);
		return sec.getIntValue(makeLocator(".application_type[" + i + "]"));
	}

	public byte[] id_selector() {
		return sec.getBlobValue(makeLocator(".id_selector"));
	}

}
