package ipaneltv.toolkit.dvb;

import android.net.telecast.FrequencyInfo;

public class DvbNetworkAutoSearch {
	String uuid;
	DvbSearchToolkit toolkit;
	FrequencyInfo mainfi;
	DvbNetworkMapping netMapping;

	public DvbNetworkAutoSearch(String uuid) {
		this.uuid = uuid;
		toolkit = DvbSearchToolkit.createInstance(uuid);
	}

	public void setMainFreq(FrequencyInfo mainfi) {
		this.mainfi = mainfi;
	}

	public boolean start(FrequencyInfo fi) {
		return toolkit.startFreqSearch(fi);
	}

	public void stop() {
		toolkit.stopFreqSearch();
	}
}
