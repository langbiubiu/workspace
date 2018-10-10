package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

//TODO 所有的类可以final，加快速度,包括内部类
public final class DescApplication extends Descriptor {

	public static final int TAG = 0x00;

	public DescApplication(Descriptor d) {
		super(d);
	}

	public int profiles_size() {
		return sec.getIntValue(makeLocator(".application_profiles.length"));
	}

	public int service_bound_flag() {
		return sec.getIntValue(makeLocator(".service_bound_flag"));
	}

	public int visibility() {
		return sec.getIntValue(makeLocator(".visibility"));
	}

	public int application_priority() {
		return sec.getIntValue(makeLocator(".application_priority"));
	}
	
	public int ProtocolLables_size() {
		return sec.getIntValue(makeLocator(".transport_protocols.length"));
	}
	
	public ProtocolLables getProtocolLables(int i) {
		Section.checkIndex(i);
		return new ProtocolLables(i);
	}

	public final class ProtocolLables{
		int index;

		ProtocolLables(int i) {
			index = i;
		}

		public int transport_protocol_label() {
			return sec.getIntValue(makeLocator(".transport_protocol_label"));
		}

		String makeLocator(String s) {
			DescApplication.this.setPreffixToLocator();
			sec.appendToLocator(".transport_protocols[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
	
	public Profiles getProfiles(int i) {
		Section.checkIndex(i);
		return new Profiles(i);
	}

	public final class Profiles {
		int index;

		Profiles(int i) {
			index = i;
		}

		public int application_profile() {
			return sec.getIntValue(makeLocator(".application_profile"));
		}

		public int version_major() {
			return sec.getIntValue(makeLocator(".version_major"));
		}

		public int version_minor() {
			return sec.getIntValue(makeLocator(".version_minor"));
		}

		public int version_micro() {
			return sec.getIntValue(makeLocator(".version_micro"));
		}

		String makeLocator(String s) {
			DescApplication.this.setPreffixToLocator();
			sec.appendToLocator(".application_profiles[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
