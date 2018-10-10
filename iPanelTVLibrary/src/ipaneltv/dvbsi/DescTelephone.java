package ipaneltv.dvbsi;

public final class DescTelephone extends Descriptor {
	public static final int TAG = 0x57;

	public DescTelephone(Descriptor d) {
		super(d);
	}

	public int foreign_availability() {
		return sec.getIntValue(makeLocator(".foreign_availability"));
	}

	public int connection_type() {
		return sec.getIntValue(makeLocator(".connection_type"));
	}

	public int operator_code_length() {
		return sec.getIntValue(makeLocator(".operator_code_length"));
	}

	public String country_prefix() {
		return country_prefix(null);
	}

	public String country_prefix(String enc) {
		return sec.getTextValue(makeLocator(".country_prefix"), enc);
	}

	public String international_area_code() {
		return international_area_code(null);
	}

	public String international_area_code(String enc) {
		return sec.getTextValue(makeLocator(".international_area_code"), enc);
	}

	public String national_area_code() {
		return national_area_code(null);
	}

	public String national_area_code(String enc) {
		return sec.getTextValue(makeLocator(".national_area_code"), enc);
	}

	public String core_number() {
		return core_number(null);
	}

	public String core_number(String enc) {
		return sec.getTextValue(makeLocator(".core_number"), enc);
	}
}
