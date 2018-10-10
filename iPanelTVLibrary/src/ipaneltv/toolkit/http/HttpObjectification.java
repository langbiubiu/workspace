package ipaneltv.toolkit.http;

import java.util.HashMap;
import java.util.Vector;

import android.util.SparseIntArray;

public class HttpObjectification {
	
	public static class IPObject {
		public Object attach;
	}
	
	public static class IPNetwork extends IPObject{
		public int nid;
		public String name;
		public String delivery;
		public int version;
	
		public Vector<TransportStream>transport_stream = new Vector<TransportStream>();
		public TransportStream addTransportStream(){
			TransportStream t = createInstace();
			transport_stream.add(t);
			return t;
		}
		public static class TransportStream extends IPObject{
			public int onid;
			public int tsid;
			public IPFrequencyInfo ipFreq;
			public Vector<Channel>channels = new Vector<Channel>();
			public Channel addChannel(){
				Channel c = new Channel();
				channels.add(c);
				return c;
			}
			public class Channel extends IPObject{
				public String name;				
				public int number;
				public int tstf;
				public String channelId;
				public int pcrpid;
				public IPSDTService sdtService;
				public Vector<Stream>streams = new Vector<Stream>();
				public Stream addStream(){
					Stream s = new Stream();
					streams.add(s);
					return s;
				}
				public class Stream extends IPObject{
					public int pid;
					public int type;					
					public Vector<ECM>ecms = new Vector<ECM>();
					public ECM addECM(){
						ECM e = new ECM();
						ecms.add(e);
						return e;
					}
					public class ECM{
						public int pid;
						public int caids;
						public String capid;
					}
					
				}
			}
		}
		public IPNetwork.TransportStream createInstace(){
			return new HttpObjectification.IPNetwork.TransportStream();
		}
	}
	
	public static class IPFrequencyInfo extends IPObject {
		public long freq;
		public String mod;			
		public long rate;		
	}
	
	public static class IPSDTService extends IPObject{
		public int sid;
		public int type;
	}
	
	public static class IPBouquet extends IPObject{
		public int version;
		public Vector<Bouquet>bouquets = new Vector<Bouquet>();
		
		public Bouquet addBouquet(){
			Bouquet b = new Bouquet();
			bouquets.add(b);
			return b;
		}
		
		public class Bouquet extends IPObject{
			public int id;
			public String name;
			public Vector<Channel>cs = new Vector<Channel>();
			public Channel addChannel(){
				Channel c = new Channel();
				cs.add(c);
				return c;
			}
			public class Channel extends IPObject{
				public IPTransportStream ipts;
				public SdtService service;
			}			
		}
	}
	public static class DVBMenuBouquet extends IPObject{
		public int b_version;
		public Vector<BBouquet>b_bouquets = new Vector<BBouquet>();
		public BBouquet addBouquet(){
			BBouquet b = new BBouquet();
			b_bouquets.add(b);
			return b;
		}
		public class BBouquet extends IPObject{
			public int b_id;
			public String b_name;
			public int level;
			public Vector<BChannel>cs = new Vector<BChannel>();
			public BChannel addChannel(){
				BChannel c = new BChannel();
				cs.add(c);
				return c;
			}
			public class BChannel extends IPObject{
				public String contentName;
				public String contentId;
				public int serviceId;
				public int tsId;
			}
		}
	}
	
	public static class IPTransportStream extends IPObject{
		public int tsid;
	}
	public static class AllIPEITInfo extends IPObject{
		public int version;
		public Vector<IPEITInfoAppoint>eitinfos = new Vector<IPEITInfoAppoint>();
		public IPEITInfoAppoint addIPEITInfo(){
			IPEITInfoAppoint c = new IPEITInfoAppoint();
			eitinfos.add(c);
			return c;
		}
	}
	
	public static class IPEITInfo extends IPObject{
		public int version;
		public long st;
		public long et;
		public Vector<Channel>channels = new Vector<Channel>();
		public Channel addChannel(){
			Channel c = new Channel();
			channels.add(c);
			return c;
		}
		public class Channel extends IPObject{
			public IPTransportStream ipts;
			public SdtService service;
			public Vector<Event>events = new Vector<Event>();
			
			public Event addEvent(){
				Event e = new Event();
				events.add(e);
				return e;
			}
			public class Event extends IPObject{
				public String name;
				public String desc;
				public long st;
				public long et;
			}
		}
	}
	public static class IPEITInfoAppoint extends IPObject{
		public int version;
		public Vector<IPChannelAppoint>channels = new Vector<IPChannelAppoint>();
		public IPChannelAppoint addChannel(){
			IPChannelAppoint c = new IPChannelAppoint();
			channels.add(c);
			return c;
		}
	}
	
	public static class IPChannelAppoint extends IPObject{
		public int tsid;
		public int sid;
		public Vector<AEvent>events = new Vector<AEvent>();
		public AEvent addEvent(){
			AEvent e = new AEvent();
			events.add(e);
			return e;
		}		
		public class AEvent extends IPObject{
			public String name;
			public String desc;
			public long st;
			public long et;			
		}
	}
	
	public static class SdtService extends IPSDTService{
		public int sid;
	}
	
	public static class VersionRegister extends IPObject{
		public int network;
		public int eit_today;
		public int eit_tsid_sid;
		public int bouquet;
		HashMap<Integer, SparseIntArray> eit_versions = new HashMap<Integer, SparseIntArray>();
		
		void addVersions(Integer tsid, int sid, int version) {
			SparseIntArray c_version = eit_versions.get(tsid);
			if (c_version == null) {
				c_version = new SparseIntArray();
				eit_versions.put(tsid, c_version);
			}
			c_version.put(sid, version);
		}
	}
	
	public static int parseString2Int(String val){
		int value = 0;
		if(val == null || "".equals(val) ||"".equals(val.trim())){
			return -1;
		}
		if(val.startsWith("0x") || val.startsWith("0X")){
			value = Integer.parseInt(val.replaceAll("0[x|X]", ""), 16);
		}else{
			value = Integer.parseInt(val);
		}
		return value;
	}
	
	public static long parseString2Long(String val){
		if(val == null || "".equals(val) || "".equals(val.trim())){
			return -1;
		}
		return Long.valueOf(val);
	}
}
