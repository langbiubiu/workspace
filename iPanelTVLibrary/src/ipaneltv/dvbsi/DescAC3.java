package ipaneltv.dvbsi;

public final class DescAC3 extends Descriptor {
	public static final int TAG = 0x6a;

	public DescAC3(Descriptor d) {
		super(d);
	}

	public int component_type_flag() {
		return sec.getIntValue(makeLocator(".component_type_flag"));
	}

	public int bsid_flag() {
		return sec.getIntValue(makeLocator(".bsid_flag"));
	}

	public int mainid_flag() {
		return sec.getIntValue(makeLocator(".mainid_flag"));
	}

	public int asvc_flag() {
		return sec.getIntValue(makeLocator(".asvc_flag"));
	}

	public int component_type() {
		return sec.getIntValue(makeLocator(".component_type"));
	}

	public int bsid() {
		return sec.getIntValue(makeLocator(".bsid"));
	}

	public int mainid() {
		return sec.getIntValue(makeLocator(".mainid"));
	}

	public int asvc() {
		return sec.getIntValue(makeLocator(".asvc"));
	}

	public byte[] additional() {
		return sec.getBlobValue(makeLocator(".additional"));
	}
}
