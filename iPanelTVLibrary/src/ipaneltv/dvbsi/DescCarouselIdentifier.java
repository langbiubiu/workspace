package ipaneltv.dvbsi;

public final class DescCarouselIdentifier extends Descriptor {
	public static final int TAG = 0x13;

	public static final int ID_00 = 0x00;
	public static final int ID_01 = 0x01;

	public DescCarouselIdentifier(Descriptor d) {
		super(d);
	}

	public int carousel_id() {
		return sec.getIntValue(makeLocator(".carousel_id"));
	}

	public int format_id() {
		return sec.getIntValue(makeLocator(".format_id"));
	}

	public Id00 id00() {
		return new Id00();
	}

	public Id01 id01() {
		return new Id01();
	}

	public final class Id00 {
		public byte[] private_data() {
			return sec.getBlobValue(makeLocator(".format_id_00.private_data"));
		}
	}

	public final class Id01 {
		public int module_version() {
			return sec.getIntValue(makeLocator(".private_data"));
		}

		public int module_id() {
			return sec.getIntValue(makeLocator(".module_id"));
		}

		public int block_size() {
			return sec.getIntValue(makeLocator(".block_size"));
		}

		public int module_size() {
			return sec.getIntValue(makeLocator(".module_size"));
		}

		public int compression_method() {
			return sec.getIntValue(makeLocator(".compression_method"));
		}

		public int original_size() {
			return sec.getIntValue(makeLocator(".original_size"));
		}

		public int time_out() {
			return sec.getIntValue(makeLocator(".time_out"));
		}

		public String object_key() {
			return object_key(null);
		}

		public String object_key(String enc) {
			return sec.getTextValue(makeLocator(".object_key"), enc);
		}

		String makeLocator(String s) {
			DescCarouselIdentifier.this.setPreffixToLocator();
			sec.appendToLocator("format_id_01.");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

	public byte[] private_data() {
		return sec.getBlobValue(makeLocator(".private_data"));
	}
}
