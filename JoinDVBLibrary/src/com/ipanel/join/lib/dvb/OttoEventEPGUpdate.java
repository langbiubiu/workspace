package com.ipanel.join.lib.dvb;

import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;

public class OttoEventEPGUpdate {
	public final ChannelKey channel;

	public OttoEventEPGUpdate(ChannelKey key) {
		this.channel = key;
	}
}
