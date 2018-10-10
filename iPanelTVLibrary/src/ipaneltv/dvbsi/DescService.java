package ipaneltv.dvbsi;

public final class DescService extends Descriptor {
	public static final int TAG = 0x48;

	public DescService(Descriptor d) {
		super(d);
	}

	public int service_type() {
		return sec.getIntValue(makeLocator(".service_type"));
	}
	
	public int service_provider_name_length() {
		return sec.getIntValue(makeLocator(".service_provider_name_length"));
	}
	
	public String service_provider_name() {
		return service_provider_name(null);
	}

	public String service_provider_name(String enc) {
		return sec.getTextValue(makeLocator(".service_provider_name"), enc);
	}
	
	public int service_name_length() {
		return sec.getIntValue(makeLocator(".service_name_length"));
	}
	
	public String service_name() {
		return service_name(null);
	}

	public String service_name(String enc) {
		return sec.getTextValue(makeLocator(".service_name"), enc);
	}
}
