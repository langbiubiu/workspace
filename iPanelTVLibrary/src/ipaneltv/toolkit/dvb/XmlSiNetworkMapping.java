package ipaneltv.toolkit.dvb;


import ipaneltv.toolkit.dvb.DvbObjectification.SiNetwork;

import java.util.ArrayList;
import java.util.List;

public class XmlSiNetworkMapping {
	SiNetwork siNetwork;
	List<TransportStream> tss = new ArrayList<TransportStream>();
	
	public class TransportStream {
		
	}
	
}
/*
class IpNetInfoXmlHandler extends DefaultHandler {
	static final String TAG = "IpNetInfoXmlHandler";
	IpSiNetworkMapping map;
	TransportStream ts;
	SiNetwork sinet;
	SiTransportStream sits;
	String uuid;

	public IpNetInfoXmlHandler(String uuid, IpSiNetworkMapping map) {
		this.map = map;
		this.uuid = uuid;
	}

	@Override
	public void startElement(String uri, String localName, String qName, Attributes attr)
			throws SAXException {
		try {
			if (localName.equalsIgnoreCase("net")) {
				String nid = attr.getValue("nid");
				sinet = new SiNetwork();
				sinet.network_name = attr.getValue("name");
				sinet.network_id = Integer.parseInt(nid);
				sinet.regionId = attr.getValue("region");
				sinet.short_network_name = attr.getValue("short_name");
				map.setSiNetwork(sinet);
			} else if (localName.equalsIgnoreCase("ts")) {

				String onid = attr.getValue("onid");
				String tsid = attr.getValue("tsid");
				String freq = attr.getValue("freq");
				String param = attr.getValue("param");
				FrequencyInfo fi = FrequencyInfo.fromString(param);
				ts = map.createTransportStream(fi);
				ts.siTransportStram.original_network_id = Integer.parseInt(onid);
				if (map.sizeOfTransportStream() == 0) {
					map.addMainTransportStream(ts);
				} else {

				}
			} else if (localName.equalsIgnoreCase("serv")) {
				String sid = attr.getValue("sid");
				String name = attr.getValue("name");
				String chn = attr.getValue("chn");
			} else if (localName.equalsIgnoreCase("es")) {
				String pid = attr.getValue("pid");
				String type = attr.getValue("type");

			}
		} catch (Exception e) {
			IPanelLog.e(TAG, "startElement[" + localName + "] error:" + e);
		}
	}
}*/