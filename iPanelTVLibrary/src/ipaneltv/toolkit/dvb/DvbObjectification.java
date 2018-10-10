package ipaneltv.toolkit.dvb;

import ipaneltv.dvbsi.BAT;
import ipaneltv.dvbsi.DescBouquetName;
import ipaneltv.dvbsi.DescCA;
import ipaneltv.dvbsi.DescCableDeliverySystem;
import ipaneltv.dvbsi.DescExtendedEvent;
import ipaneltv.dvbsi.DescMultilingualServiceName;
import ipaneltv.dvbsi.DescNetworkName;
import ipaneltv.dvbsi.DescService;
import ipaneltv.dvbsi.DescServiceList;
import ipaneltv.dvbsi.DescShortEvent;
import ipaneltv.dvbsi.DescStreamIdentifier;
import ipaneltv.dvbsi.Descriptor;
import ipaneltv.dvbsi.EIT;
import ipaneltv.dvbsi.NIT;
import ipaneltv.dvbsi.PAT;
import ipaneltv.dvbsi.PMT;
import ipaneltv.dvbsi.SDT;
import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.Section;

import java.util.Enumeration;
import java.util.Locale;
import java.util.Vector;

import android.net.telecast.FrequencyInfo;
import android.net.telecast.NetworkInterface;
import android.util.SparseIntArray;

/**
 * DVB相关数据的对象化操作
 */
public class DvbObjectification {
	protected String serviceNameEnc = null;
	protected String eventNameEnc = null;
	protected String serviceProviderNameEnc = null;
	protected String bouquetNameEnc = null;
	protected String netorkNameEnc = null;
	protected String localLanguage = "zh";
	protected String engISO639LangString = Locale.ENGLISH.getISO3Language();
	protected String localISO639LangString = Locale.getDefault().getISO3Language();
	private final static String TAG = "DvbObjectification";

	public static interface DescriptorHandler {
		void onDescriptorFound(SiObject siobj, Descriptor d);
	}

	public boolean isEnglishLanguage(String lang) {
		return engISO639LangString.equalsIgnoreCase(lang);
	}

	public boolean isLocalLanguage(String lang) {
		return localISO639LangString.equalsIgnoreCase(lang);
	}

	public void setEventNameEncoding(String enc) {
		eventNameEnc = enc;
	}

	public void setServiceNameEncoding(String enc) {
		serviceNameEnc = enc;
	}

	public void setNetworkNameEncoding(String enc) {
		netorkNameEnc = enc;
	}

	public void setServiceProviderNameEncoding(String enc) {
		serviceProviderNameEnc = enc;
	}

	public void setBouquetNameEncoding(String enc) {
		bouquetNameEnc = enc;
	}

	protected SiCabelFrequencyInfo parseDescCableDeliverySystem(Descriptor _d) {
		DescCableDeliverySystem d = new DescCableDeliverySystem(_d);
		SiCabelFrequencyInfo f = new SiCabelFrequencyInfo();
		f.delivery_type = NetworkInterface.DELIVERY_CABLE;
		int v = d.frequency();
		v = ((v >> 28) & 0xf) * 1000 + ((v >> 24) & 0xf) * 100 + ((v >> 20) & 0xf) * 10
				+ ((v >> 16) & 0xf);
		f.frequency = 1000000 * (long) v;
		int n = d.symbol_rate();
		n = ((n >> 16) & 0xf) * 1000 + ((n >> 12) & 0xf) * 100 + ((n >> 8) & 0xf) * 10
				+ ((n >> 4) & 0xf);
		f.symbol_rate = 1000 * n;
		IPanelLog.i(TAG, "DescCable fi = " + f.frequency + ",symbol_rate = " + f.symbol_rate);
		switch (d.modulation()) {
		case 0x01:
			f.modulation = "qam16";
			break;
		case 0x02:
			f.modulation = "qam32";
			break;
		case 0x03:
			f.modulation = "qam64";
			break;
		case 0x04:
			f.modulation = "qam128";
			break;
		case 0x05:
			f.modulation = "qam256";
			break;
		case 0x00:
		default:
			f.modulation = "undef";
			break;
		}
		return f;
	}

	public SiNetwork parseNIT(Section s, DescriptorHandler descHandler) {
		SiNetwork sin = new SiNetwork();
		sin.network_id = NIT.network_id(s);
		sin.version_num = NIT.version_number(s);
		int ds = NIT.descriptor_size(s);
		IPanelLog.d(TAG, " NIT.version_number = "+sin.version_num);
		IPanelLog.i(TAG, "SiNetwork parseNIT!!!!! sin.network_id: " + sin.network_id + " ds:" + ds);
		for (int i = 0; i < ds; i++) {
			Descriptor d = NIT.descriptor(s, i);
			IPanelLog.i(TAG, "SiNetwork parseNIT descriptor_tag: " + d.descriptor_tag());
			switch (d.descriptor_tag()) {
			case DescNetworkName.TAG: {
				DescNetworkName networkName = new DescNetworkName(d);
				sin.network_name = networkName.network_name(netorkNameEnc);
				break;
			}
			case 0xa8: {
				StringBuilder stringBuilder = new StringBuilder("");
				IPanelLog.i(TAG, "SiNetwork parseNIT network_id:" + sin.network_id);
				if (sin.network_id == 0x10) {
					byte[] info = d.read();
					for (int j = 0; j < info.length; j++) {
						IPanelLog.e(TAG, "info SiNetwork parseNIT" + Integer.parseInt(info[j] + ""));
						if(j>1){
							int v = info[j] & 0xFF;
							String hv = Integer.toHexString(v);
							if (hv.length() < 2) {
								stringBuilder.append(0);
							}
							stringBuilder.append(hv);
						}
					}
					sin.regionId = stringBuilder.toString();
					IPanelLog.e(TAG, "regionId:" + sin.regionId);
				}
			}
			default:
				break;
			}
			if (descHandler != null)
				descHandler.onDescriptorFound(sin, d);
		}

		int tss = NIT.transport_stream_size(s);
		for (int i = 0; i < tss; i++) {
			SiTransportStream ts = sin.addTransportStream();
			NIT.TransportStream nitts = NIT.transportStream(s, i);
			ts.original_network_id = nitts.original_network_id();
			ts.transport_stream_id = nitts.transport_stream_id();
			IPanelLog.d(TAG, "ts.transport_stream_id = "+ts.transport_stream_id);
			ds = nitts.descriptor_size();
			for (int j = 0; j < ds; j++) {
				Descriptor d = nitts.descriptor(j);
				int tag = d.descriptor_tag();
				switch (tag) {
				case DescCableDeliverySystem.TAG:
					ts.frequency_info = parseDescCableDeliverySystem(d);
					break;
				case DescServiceList.TAG: {
					DescServiceList desc = new DescServiceList(d);
					int serSize = desc.service_size();
					for (int k = 0; k < serSize; k++) {
						SiTransportStream.Service service = ts.new Service();
						DescServiceList.Service descS = desc.service(k);
						service.service_id = descS.service_id();
						service.service_type = descS.service_type();
						ts.addService(service);
					}
					break;
				}
				default:
					break;
				}
				if (descHandler != null)
					descHandler.onDescriptorFound(ts, d);
			}
		}
		return sin;
	}

	public SiBouquet parseBAT(Section s, DescriptorHandler descHandler) {
		SiBouquet bouquet = new SiBouquet();
		bouquet.bouquet_id = BAT.bouquet_id(s);
		bouquet.version = BAT.version_number(s);
		int ds = BAT.descriptor_size(s);
		IPanelLog.i(TAG, "parseBAT bouquet_id = "+bouquet.bouquet_id +" ,version_number = "+bouquet.version +" , ds = "+ds);
		for (int i = 0; i < ds; i++) {
			Descriptor d = BAT.descriptor(s, i);
			switch (d.descriptor_tag()) {
			case DescBouquetName.TAG: {
				DescBouquetName bouquetName = new DescBouquetName(d);
				bouquet.bouquet_name = bouquetName.bouquet_name(bouquetNameEnc);
				break;
			}
			default:
				break;
			}
			if (descHandler != null)
				descHandler.onDescriptorFound(bouquet, d);
		}

		int tss = BAT.transport_stream_size(s);
		for (int i = 0; i < tss; i++) {
			SiBouquet.TransportStream ts = bouquet.addTransportStream();
			BAT.TransportStream batts = BAT.getTransportStream(s, i);
			ts.original_network_id = batts.original_network_id();
			ts.transport_stream_id = batts.transport_stream_id();
			ds = batts.descriptor_size();
			SiBouquet.TransportStream.Service service = null;
			for (int j = 0; j < ds; j++) {
				Descriptor d = batts.descriptor(j);
				int tag = d.descriptor_tag();
				switch (tag) {
				case DescCableDeliverySystem.TAG:
					ts.frequency_info = parseDescCableDeliverySystem(d);
					break;
				case DescServiceList.TAG: {
					DescServiceList descSerList = new DescServiceList(d);
					int serSize = descSerList.service_size();
					for (int k = 0; k < serSize; k++) {
						DescServiceList.Service descS = descSerList.service(k);
						service = ts.addService();
						service.servie_id = descS.service_id();
						service.servie_type = descS.service_type();
					}

					break;
				}
				default:
					break;
				}
				if (descHandler != null && service != null) {
					descHandler.onDescriptorFound(ts, d);
				}
			}
		}
		return bouquet;
	}

	public SiSDTServices parseSDT(Section s, DescriptorHandler descHandler) {
		SiSDTServices serss = new SiSDTServices();
		serss.original_network_id = SDT.original_network_id(s);
		serss.transport_stream_id = SDT.transport_stream_id(s);
		int ssize = SDT.service_size(s);
		for (int i = 0; i < ssize; i++) {
			SiSDTServices.Service serv = serss.addService();
			SDT.Service sdts = SDT.service(s, i);
			serv.service_id = sdts.service_id();
			serv.eit_present_following_flag = sdts.eit_present_following_flag();
			serv.eit_schedule_flag = sdts.eit_schedule_flag();
			serv.free_ca_mode = sdts.free_ca_mode();
			serv.running_status = sdts.running_status();
			int ds = sdts.descriptor_size();
			IPanelLog.i(TAG,"ds = "+ds);
			int j;
			for (j = 0; j < ds; j++) {
				Descriptor d = sdts.descriptor(j);
				int tag = d.descriptor_tag();
				IPanelLog.d(TAG, "parseSDT tag= "+ tag);
				switch (tag) {
				case DescService.TAG: {
					DescService descService = new DescService(d);
					serv.service_type = descService.service_type();
					IPanelLog.i(TAG, "get service_name start serv.service_type = "+serv.service_type);
					int length = descService.service_name_length();
					if (length != 0)
						serv.service_name = descService.service_name(serviceNameEnc);
					int provider_name_length = descService.service_provider_name_length();
					if (provider_name_length == 0)
						serv.provider_name = descService
								.service_provider_name(serviceProviderNameEnc);
					IPanelLog.i(TAG, "service name:" + serv.service_name + ",provider name="
							+ serv.provider_name);
					break;
				}
				case DescMultilingualServiceName.TAG: {
					if (serv.service_name_en == null) {
						DescMultilingualServiceName multiName = new DescMultilingualServiceName(d);
						int size = multiName.service_name_size();
						for (int k = 0; k < size; k++) {
							DescMultilingualServiceName.ServiceName sname = multiName
									.service_name(k);
							if (isEnglishLanguage(sname.language())) {
								serv.service_name_en = sname.service_name(serviceNameEnc);
								serv.provider_name_en = sname
										.service_provider_name(serviceProviderNameEnc);
							}
						}
					}
					break;
				}
				case DescBouquetName.TAG:
					break;
				default:
					break;
				}
				if (descHandler != null)
					descHandler.onDescriptorFound(serv, d);
			}
			IPanelLog.i(TAG,"ds = j = "+j);
		}
		IPanelLog.i(TAG, "parseSDT out");
		return serss;
	}

	public SiPATServices parsePAT(Section s, DescriptorHandler descHandler) {
		SiPATServices patSer = new SiPATServices();
		patSer.transport_stream_id = PAT.transport_stream_id(s);

		int pmSize = PAT.program_size(s);
		for (int i = 0; i < pmSize; i++) {
			PAT.Program patPm = PAT.program(s, i);
			SiPATServices.Program program = patSer.addProgram();
			program.program_number = patPm.program_number();
			program.pmt_pid = patPm.program_map_pid();
		}

		return patSer;// TODO
	}

	void parseDescCA(SparseIntArray info, SiPATServices.Program.Stream s, Descriptor d) {
		DescCA des = new DescCA(d);
		IPanelLog.d(TAG, "" + "parsePmt ca_pid = " + des.ca_pid() + ",ca_system_id" + des.ca_system_id());
		if (info != null) {
			info.put(des.ca_pid(), des.ca_system_id());
		}
		if (s != null) {
			s.addEcm(des.ca_system_id(), des.ca_pid());
		}
	}

	public void parsePMT(Section s, SiPATServices patServices, DescriptorHandler descHandler) {
		SparseIntArray cainfo = new SparseIntArray();
		Enumeration<SiPATServices.Program> pms = patServices.program.elements();
		while (pms.hasMoreElements()) {
			int ds = 0;
			SiPATServices.Program pm = pms.nextElement();
			int pn = PMT.program_number(s);
			IPanelLog.d(TAG, "PMT pn = " + pn);
			if (pn == pm.program_number) {
				pm.pcr_pid = PMT.pcr_pid(s);
				IPanelLog.d(TAG, "PMT pm.pcr_pid = " + pm.pcr_pid);
				ds = PMT.descriptor_size(s);
				for (int i = 0; i < ds; i++) {
					Descriptor d = PMT.descriptor(s, i);
					switch (d.descriptor_tag()) {
					case DescCA.TAG:
						IPanelLog.d(TAG, "PMT program_num = " + pm.program_number);
						parseDescCA(cainfo, null, d);
						break;
					default:
						break;
					}					
					descHandler.onDescriptorFound(pm, d);
				}
				
				int compSize = PMT.component_size(s);
				for (int i = 0; i < compSize; i++) {
					SiPATServices.Program.Stream stream = pm.addStream();
					PMT.Component comp = PMT.component(s, i);
					stream.stream_pid = comp.elementary_pid();
					stream.stream_type = comp.stream_type();
					IPanelLog.d(TAG, "PMT stream.stream_pid = " + stream.stream_pid);
					for (int j = 0; j < cainfo.size(); j++) {
						stream.addEcm(cainfo.valueAt(j), cainfo.keyAt(j));
					}
					ds = comp.descriptor_size();
					IPanelLog.d(TAG, "PMT.descriptor_size(s) = "+ds);
					for (int j = 0; j < ds; j++) {
						Descriptor d = comp.descriptor(j);
						switch (d.descriptor_tag()) {
						case DescStreamIdentifier.TAG:
							DescStreamIdentifier desc = new DescStreamIdentifier(d);
							stream.component_tag = desc.component_tag();
							break;
						case DescCA.TAG:
							IPanelLog.d(TAG, "program_num = " + pm.program_number);
							parseDescCA(null, stream, d);
							break;
						default:
							break;
						}

						descHandler.onDescriptorFound(stream, d);
					}
				}
				break;
			}
		}

		return;// TODO
	}

	public SiEITEvents parseEITEvents(Section s, DescriptorHandler descHandler) {
		SiEITEvents sies = new SiEITEvents();
		sies.original_network_id = EIT.original_network_id(s);
		sies.service_id = EIT.service_id(s);
		sies.transport_stream_id = EIT.transport_stream_id(s);
		int esize = EIT.event_size(s);
		IPanelLog.d(TAG, "parseEITEvents service_id = +"+sies.service_id +";s.table_id"+s.table_id()+";esize = "+ esize +";sn=" + s.section_number());
		for (int i = 0; i < esize; i++) {
			SiEITEvents.Event evt = sies.addEvent();
			EIT.Event eit_evt = EIT.event(s, i);
			evt.event_id = eit_evt.event_id();
			evt.free_ca_mode = eit_evt.free_ca_mode();
			evt.running_status = eit_evt.running_status();
			evt.start_time = eit_evt.start_time();
			evt.duration = eit_evt.duration();
			IPanelLog.d(TAG, "parseEITEvents tableid=" + s.table_id() + ",service_id=" + sies.service_id
					+ ",start_time=" + evt.start_time);
			String name = null;
			int ds = eit_evt.descriptor_size();
			for (int j = 0; j < ds; j++) {
				Descriptor d = eit_evt.descriptor(j);
				IPanelLog.d(TAG, "EIT d.descriptor_tag() = "+ d.descriptor_tag());
				switch (d.descriptor_tag()) {
				case DescShortEvent.TAG:
					if (evt.event_name == null || evt.event_name_en == null) {
						DescShortEvent descShortEvent = new DescShortEvent(d);
						String lang = descShortEvent.language();
//						String text = descShortEvent.text(eventNameEnc);
						name = descShortEvent.event_name(eventNameEnc);
						IPanelLog.d(TAG, "EIT lang = "+ lang +";name = "+ name);
						if (isLocalLanguage(lang)) {
							if (evt.event_name == null)
								evt.event_name = name;
						} else if (isEnglishLanguage(lang)) {
							if (evt.event_name_en == null)
								evt.event_name_en = name;
						}
//						IPanelLog.d(TAG, "parseEITEvents event_name" + evt.event_name);
					}
					break;
				case DescExtendedEvent.TAG:
					IPanelLog.d(TAG, "111111111");
					break;
				default:
					break;
				}
				if (descHandler != null)
					descHandler.onDescriptorFound(evt, d);
			}
			if (evt.event_name == null)
				evt.event_name = name;
		}
		return sies;
	}

	public static class SiObject {
		public Object attach;
	}

	public static class SiDescriptor extends SiObject {
		public static SiDescriptor dump(Descriptor d) {
			return new SiDescriptor(d.descriptor_tag(), d.read());
		}

		SiDescriptor(int tag, byte[] b) {
			this.tag = tag;
			content = b;
		}

		public int tag;
		public byte[] content;
	}

	public static class SiFrequencyInfo extends SiObject {
		public int delivery_type;

		public FrequencyInfo getFrequencyInfo() {
			return null;
		}
	}

	public static class SiCabelFrequencyInfo extends SiFrequencyInfo {
		public long frequency;
		public int symbol_rate;
		public String modulation;

		public FrequencyInfo getFrequencyInfo() {
			FrequencyInfo fi = new FrequencyInfo(NetworkInterface.DELIVERY_CABLE);
			fi.setFrequency(frequency);
			fi.setParameter(FrequencyInfo.MODULATION, modulation);
			fi.setParameter(FrequencyInfo.SYMBOL_RATE, symbol_rate);
			return fi;
		}
	}

	public static class SiTransportStream extends SiObject {
		public SiTransportStream(SiObject container) {
			this.container = container;
		}

		public SiObject container;
		public int transport_stream_id;
		public int original_network_id;
		public SiFrequencyInfo frequency_info;
		public Vector<Service> service;

		void addService(Service s) {
			if (service == null)
				service = new Vector<Service>();
			service.add(s);
		}

		public class Service extends SiObject {
			public int service_id;
			public int service_type;
		}
	}

	public static class SiNetwork extends SiObject {
		public int network_id;
		public String regionId;
		public String network_name;
		public String short_network_name;
		public int version_num;
		public Vector<TransportStream> transport_stream = new Vector<TransportStream>();

		public TransportStream addTransportStream() {
			TransportStream ts = new TransportStream();
			transport_stream.add(ts);
			return ts;
		}

		public class TransportStream extends SiTransportStream {
			public TransportStream() {
				super(SiNetwork.this);
			}
		}
	}

	public static class SiBouquet extends SiObject {
		public int bouquet_id;
		public int version;
		public String bouquet_name;
		public String short_bouquet_name;
		public Vector<TransportStream> transport_stream = new Vector<TransportStream>();

		TransportStream addTransportStream() {
			TransportStream ts = new TransportStream();
			transport_stream.add(ts);
			return ts;
		}

		public class TransportStream extends SiTransportStream {
			public TransportStream() {
				super(SiBouquet.this);
			}

			public Vector<Service> service;

			Service addService() {
				if (service == null)
					service = new Vector<Service>();
				Service s = new Service();
				service.add(s);
				return s;
			}

			public class Service extends SiObject {
				public int servie_type;
				public int servie_id;
			}
		}
	}

	public static class SiSDTServices extends SiObject {
		public int transport_stream_id;
		public int original_network_id;
		public Vector<Service> service;

		public Service addService() {
			Service s = new Service();
			if (service == null)
				service = new Vector<Service>();
			service.add(s);
			return s;
		}

		public class Service extends SiObject {
			public int service_id;
			public int service_type;
			public int eit_schedule_flag;
			public int eit_present_following_flag;
			public int running_status;
			public int free_ca_mode;
			public int channel_number = -1;
			public int video_mode;
			public String service_name;
			public String service_name_en;
			public String short_service_name;
			public String provider_name;
			public String provider_name_en;
			public String short_provider_name;
			public Vector<GroupsInfo> service_classified;
		}

		public static  class GroupsInfo{
			public GroupsInfo(){}
			public int bouquetId;
			public String groupBame;
		}
		
	}

	public static class SiPATServices extends SiObject {
		public int transport_stream_id;
		public Vector<Program> program;
		
		public int programSize(){
			if(program != null)
				return program.size();
			return 0;
		}
		Program addProgram() {
			Program p = new Program();
			if (program == null)
				program = new Vector<Program>();
			program.add(p);
			return p;
		}

		public Program getProgram(int program_number) {
			if(program == null){
				return null;
			}
			int size = program.size();
			Program p = null;
			for (int i = 0; i < size; i++) {
				p = program.get(i);
				if (program_number == p.program_number)
					return p;
			}
			return null;
		}

		public class Program extends SiObject {
			public int program_number;
			public int pmt_pid;
			public int pcr_pid;
			public Vector<Stream> stream;
			
			public int streamSize(){
				if(stream != null)
					return stream.size();
				return 0;
			}
			Stream addStream() {
				Stream s = new Stream();
				if (stream == null)
					stream = new Vector<Stream>();
				stream.add(s);
				return s;
			}
			
			public class Stream extends SiObject {
				public int stream_type;
				public int stream_pid;
				public int component_tag;
				public Vector<Ecm> ecm;

				public int ecmSize(){
					if(ecm != null)
						return ecm.size();
					return 0;
				}
				public class Ecm extends SiObject {
					public int ca_system_id;
					public int ecm_pid;
				}

				void addEcm(int casysid, int ecmpid) {
					Ecm e;
					if (ecm != null) {
						for (int i = 0; i < ecm.size(); i++) {
							if ((e = ecm.get(i)).ca_system_id == casysid) {
								e.ecm_pid = ecmpid;
								return;
							}
						}
					}
					e = new Ecm();
					if (ecm == null)
						ecm = new Vector<Ecm>();
					ecm.add(e);
					e.ca_system_id = casysid;
					e.ecm_pid = ecmpid;
				}
			}			
		}
	}

	public static class SiEITEvents extends SiObject {
		public int service_id;
		public int transport_stream_id;
		public int original_network_id;
		public Vector<Event> event;

		public Event addEvent() {
			Event e = new Event();
			if (event == null)
				event = new Vector<Event>();
			event.add(e);
			return e;
		}

		public class Event extends SiObject {
			public int event_id;
			public String start_time;// android.text.format.Time.parse3339()
			public long duration;
			public int running_status;
			public int free_ca_mode;
			public String event_name;
			public String event_name_en;
			public String short_event_name;
		}
	}

}
