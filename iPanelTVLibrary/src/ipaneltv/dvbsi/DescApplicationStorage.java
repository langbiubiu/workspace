package ipaneltv.dvbsi;

public final class DescApplicationStorage extends Descriptor {
	public static final int TAG = 0x10;

	public DescApplicationStorage(Descriptor d) {
		super(d);
	}

	public int storage_property() {
		return sec.getIntValue(makeLocator(".storage_property"));
	}

	public int not_launchable_from_broadcast() {
		return sec.getIntValue(makeLocator(".not_launchable_from_broadcast"));
	}

	public int launchable_completely_from_cache() {
		return sec.getIntValue(makeLocator(".launchable_completely_from_cache"));
	}

	public int is_launchable_with_older_version() {
		return sec.getIntValue(makeLocator(".is_launchable_with_older_version"));
	}

	public int version() {
		return sec.getIntValue(makeLocator(".version"));
	}

	public int priority() {
		return sec.getIntValue(makeLocator(".priority"));
	}
}
