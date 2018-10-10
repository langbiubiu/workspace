package ipaneltv.dvbsi;

public final class DescCableDeliverySystem extends Descriptor {
	public static final int TAG = 0x44;

	public DescCableDeliverySystem(Descriptor d) {
		super(d);
	}

	public int frequency() {
		return sec.getIntValue(makeLocator(".frequency"));
	}

	public int fec_outer() {
		return sec.getIntValue(makeLocator(".FEC_outer"));
	}

	public int modulation() {
		return sec.getIntValue(makeLocator(".modulation"));
	}

	public int symbol_rate() {
		return sec.getIntValue(makeLocator(".symbol_rate"));
	}

	public int fec_inner() {
		return sec.getIntValue(makeLocator(".FEC_inner"));
	}
}
