package ipaneltv.dvbsi;

public final class DescJavaApplicationLocation extends Descriptor {
	public static final int TAG = 0x04;

	public DescJavaApplicationLocation(Descriptor d) {
		super(d);
	}

	public String base_directory() {
		return base_directory(null);
	}

	public String base_directory(String enc) {
		return sec.getTextValue(makeLocator(".base_directory"), enc);
	}

	public String classpath_extension() {
		return classpath_extension(null);
	}

	public String classpath_extension(String enc) {
		return sec.getTextValue(makeLocator(".classpath_extension"), enc);
	}

	public String initial_class() {
		return initial_class(null);
	}

	public String initial_class(String enc) {
		return sec.getTextValue(makeLocator(".initial_class"), enc);
	}
}
