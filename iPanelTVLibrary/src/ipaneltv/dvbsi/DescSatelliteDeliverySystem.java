package ipaneltv.dvbsi;

public final class DescSatelliteDeliverySystem extends Descriptor {
	public static final int TAG = 0x43;

	public DescSatelliteDeliverySystem(Descriptor d) {
		super(d);
	}

	public int frequency() {
		return sec.getIntValue(makeLocator(".frequency"));
	}

	public int orbital_position() {
		return sec.getIntValue(makeLocator(".orbital_position"));
	}

	public int west_east_flag() {
		return sec.getIntValue(makeLocator(".west_east_flag"));
	}

	public int polarization() {
		return sec.getIntValue(makeLocator(".polarization"));
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
