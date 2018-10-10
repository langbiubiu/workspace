package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescAnnouncementSupport extends Descriptor {
	public static final int TAG = 0x6e;

	public DescAnnouncementSupport(Descriptor d) {
		super(d);
	}

	public int announcement_support_identifier() {
		return sec.getIntValue(makeLocator(".announcement_support_identifier"));
	}

	public int announcement_size() {
		return sec.getIntValue(makeLocator(".announcement.length"));
	}

	public Announcement getAnnouncement(int i) {
		Section.checkIndex(i);
		return new Announcement(i);
	}

	public final class Announcement {
		int index;

		Announcement(int i) {
			this.index = i;
		}

		public int announcement_type() {
			return sec.getIntValue(makeLocator(".announcement_type"));
		}

		public int reference_type() {
			return sec.getIntValue(makeLocator(".reference_type"));
		}

		public int original_network_id() {
			return sec.getIntValue(makeLocator(".original_network_id"));
		}

		public int transport_stream_id() {
			return sec.getIntValue(makeLocator(".transport_stream_id"));
		}

		public int service_id() {
			return sec.getIntValue(makeLocator(".service_id"));
		}

		public int component_tag() {
			return sec.getIntValue(makeLocator(".component_tag"));
		}

		String makeLocator(String s) {
			DescAnnouncementSupport.this.setPreffixToLocator();
			sec.appendToLocator(".announcement[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}

}
