package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public class Descriptor {
	final String str;
	Section sec;
	final int index;

	public Descriptor(Section s, String str, int i) {
		this.str = str;
		this.sec = s;
		this.index = i;
	}

	Descriptor(Descriptor d) {
		this.sec = d.sec;
		this.str = d.str;
		this.index = d.index;
	}

	public int descriptor_length() {
		return sec.getIntValue(makeLocator(".descriptor_length"));
	}

	public int descriptor_tag() {
		return sec.getIntValue(makeLocator(".descriptor_tag"));
	}

	public byte[] read() {
		return sec.getSectionBuffer().getDescriptorByName(makeLocator(null));
	}

	void setPreffixToLocator() {
		sec.clearLocator();
		sec.appendToLocator(str);
		sec.appendToLocator(".descriptor[");
		sec.appendToLocator(index);
		sec.appendToLocator("]");
	}

	String makeLocator(String a) {
		sec.clearLocator();
		sec.appendToLocator(str);
		sec.appendToLocator(".descriptor[");
		sec.appendToLocator(index);
		sec.appendToLocator("]");
		if (a != null)
			sec.appendToLocator(a);
		return sec.getLocator();
	}

}
