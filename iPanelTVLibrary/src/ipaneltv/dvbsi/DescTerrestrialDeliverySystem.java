package ipaneltv.dvbsi;

public final class DescTerrestrialDeliverySystem extends Descriptor {
	public static final int TAG = 0x5a;

	public DescTerrestrialDeliverySystem(Descriptor d) {
		super(d);
	}

	public long centre_frequency() {
		return sec.getLongValue(makeLocator(".centre_frequency"));
	}

	public int bandwidth() {
		return sec.getIntValue(makeLocator(".bandwidth"));
	}

	public int constellation() {
		return sec.getIntValue(makeLocator(".constellation"));
	}

	public int hierarchy_information() {
		return sec.getIntValue(makeLocator(".hierarchy_information"));
	}

	public int code_rate_hp_stream() {
		return sec.getIntValue(makeLocator(".code_rate_HP_stream"));
	}

	public int code_rate_lp_stream() {
		return sec.getIntValue(makeLocator(".code_rate_LP_stream"));
	}

	public int guard_interval() {
		return sec.getIntValue(makeLocator(".guard_interval"));
	}

	public int transmission_mode() {
		return sec.getIntValue(makeLocator(".transmission_mode"));
	}

	public int other_frequency_flag() {
		return sec.getIntValue(makeLocator(".other_frequency_flag"));
	}
}
