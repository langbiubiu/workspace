package ipaneltv.dvbsi;

import ipaneltv.toolkit.Section;

public final class DescLogicChannel extends Descriptor {

	public static final int TAG = 0xb4;

	public DescLogicChannel(Descriptor d) {
		super(d);
	}

	public int logic_channel_size() {
		return sec.getIntValue(makeLocator(".logic_channel.length"));
	}

	public LogicChannel logic_channel(int i) {
		Section.checkIndex(i);
		return new LogicChannel(i);
	}

	public final class LogicChannel {
		int index;

		LogicChannel(int i) {
			index = i;
		}

		public int service_id() {
			return sec.getIntValue(makeLocator(".service_id"));
		}

		public int logic_channel_number() {
			return sec.getIntValue(makeLocator(".logic_channel_number"));
		}

		String makeLocator(String s) {
			DescLogicChannel.this.setPreffixToLocator();
			sec.appendToLocator(".logic_channel[");
			sec.appendToLocator(index);
			sec.appendToLocator("]");
			if (s != null)
				sec.appendToLocator(s);
			return sec.getLocator();
		}
	}
}
