package ipaneltv.dvbsi;

public final class DescDeferredAssociationTags extends Descriptor {
	public static final int TAG = 0x15;

	public DescDeferredAssociationTags(Descriptor d) {
		super(d);
	}

	public int transport_stream_id_size() {
		return sec.getIntValue(makeLocator(".transport_stream_id.length"));
	}

	public int transport_stream_id(int i) {
		return sec.getIntValue(makeLocator(".transport_stream_id[" + i + "]"));
	}

	public int association_tag_size() {
		return sec.getIntValue(makeLocator(".association_tag.length"));
	}

	public int program_number() {
		return sec.getIntValue(makeLocator(".program_number"));
	}

	public byte[] private_data() {
		return sec.getBlobValue(makeLocator(".private_data"));
	}

}
