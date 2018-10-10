package ipaneltv.dvbsi;

public final class DescGraphicsConstraints extends Descriptor {
	public static final int TAG = 0x14;

	public DescGraphicsConstraints(Descriptor d) {
		super(d);
	}

	public int can_run_without_visible_ui() {
		return sec.getIntValue(makeLocator(".can_run_without_visible_ui"));
	}

	public int handles_configuration_changed() {
		return sec.getIntValue(makeLocator(".handles_configuration_changed"));
	}

	public int handles_externally_controlled_video() {
		return sec.getIntValue(makeLocator(".handles_externally_controlled_video"));
	}

	public byte[] graphics_configuration() {
		return sec.getBlobValue(makeLocator(".graphics_configuration"));
	}

}
