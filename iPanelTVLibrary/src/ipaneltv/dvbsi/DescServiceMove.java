package ipaneltv.dvbsi;

public final class DescServiceMove extends Descriptor {
	public static final int TAG = 0x60;

	public DescServiceMove(Descriptor d) {
		super(d);
	}

	public int new_original_network_id() {
		return sec.getIntValue(makeLocator(".new_original_network_id"));
	}

	public int new_transport_stream_id() {
		return sec.getIntValue(makeLocator(".new_transport_stream_id"));
	}

	public int new_service_id() {
		return sec.getIntValue(makeLocator(".new_service_id"));
	}

}
