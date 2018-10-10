package ipaneltv.dvbsi;

public final class DescAudioLanguageTag extends Descriptor {
	public static final int TAG = 0x0a;

	public DescAudioLanguageTag(Descriptor d) {
		super(d);
	}

	public String audio_language() {
		return audio_language(null);
	}

	public String audio_language(String enc) {
		return sec.getTextValue(makeLocator(".audio_language"), enc);
	}

	public String audio_type() {
		return audio_type(null);
	}

	public String audio_type(String enc) {
		return sec.getTextValue(makeLocator(".audio_type"), enc);
	}
}
