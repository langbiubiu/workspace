package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.TimerFormater;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.dvb.DvbNetworkMapping.RegionID;
import ipaneltv.toolkit.dvb.DvbNetworkMapping.TransportStream;
import ipaneltv.toolkit.dvb.DvbObjectification.SiEITEvents;
import ipaneltv.toolkit.dvb.DvbObjectification.SiNetwork;
import ipaneltv.toolkit.dvb.DvbObjectification.SiPATServices;
import ipaneltv.toolkit.dvb.DvbObjectification.SiSDTServices;
import ipaneltv.toolkit.dvb.DvbObjectification.SiSDTServices.GroupsInfo;
import ipaneltv.toolkit.http.HttpObjectification.DVBMenuBouquet;
import ipaneltv.toolkit.http.HttpObjectification.DVBMenuBouquet.BBouquet;
import ipaneltv.toolkit.http.HttpObjectification.DVBMenuBouquet.BBouquet.BChannel;
import ipaneltv.toolkit.http.HttpObjectification.IPBouquet;
import ipaneltv.toolkit.http.HttpObjectification.IPBouquet.Bouquet;
import ipaneltv.toolkit.http.HttpObjectification.IPEITInfo;
import ipaneltv.toolkit.http.HttpObjectification.IPNetwork;
import ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream.Channel;
import ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream.Channel.Stream;
import ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream.Channel.Stream.ECM;
import ipaneltv.toolkit.wardship.ProgramWardshipDatebase.ProgramWardships;

import java.util.HashMap;
import java.util.Map;

import android.database.sqlite.SQLiteDatabase;
import android.net.telecast.FrequencyInfo;
import android.net.telecast.NetworkDatabase;
import android.net.telecast.NetworkDatabase.Groups;
import android.net.telecast.NetworkDatabase.Guides;
import android.net.telecast.ProgramInfo;
import android.net.telecast.dvb.DvbNetworkDatabase;
import android.net.telecast.dvb.DvbNetworkDatabase.TransportStreams;
import android.util.Log;
import android.util.SparseArray;

public class DvbDatabaseBuilder {
	static final String TAG = "DvbDatabaseBuilder";
	static int channalNum = 0;
	public static SparseArray<Long>freqMap = new SparseArray<Long>();
	public static SparseArray<Long>PFreqMap = new SparseArray<Long>();
	public static Map<String, Integer> channleMap = new HashMap<String, Integer>();
	public DvbDatabaseBuilder() {
	}

	public boolean storeFrequnecys(SQLiteDatabase db, DvbNetworkMapping map) {
		StringBuffer sb = new StringBuffer();
		TransportStream ts = null;
		
		int size = map.sizeOfTransportStream();

		for (int i = 0; i < size; ++i) {
			ts = map.transportStreamAt(i);
			if (ts == null || ts.siTransportStram == null)
				continue;
			FrequencyInfo fi = ts.siTransportStram.frequency_info.getFrequencyInfo();

			int dvb_tsid = ts.siTransportStram.transport_stream_id;
			SiPATServices siPATServices = ts.siPrograms;
			int pat_tsid = 0;
			if(siPATServices!=null){
				pat_tsid= ts.siPrograms.transport_stream_id;
			}

			int netid = ((SiNetwork) ts.siTransportStram.container).network_id;
			int orgid = ts.siTransportStram.original_network_id;
			IPanelLog.i(TAG, "storeFrequnecys :" + fi.getFrequency() + "," + fi.getDeliveryType()
					+ "," + dvb_tsid + "," + netid + "," + orgid);
			sb.delete(0, sb.length());
			sb.append("INSERT INTO ");
			sb.append(DvbNetworkDatabase.Frequencies.TABLE_NAME);
			sb.append(" (");
			sb.append(DvbNetworkDatabase.Frequencies.FREQUENCY).append(",");
			sb.append(DvbNetworkDatabase.Frequencies.DEVLIVERY_TYPE).append(",");
			sb.append(DvbNetworkDatabase.TransportStreams.TRANSPORT_STREAM_ID).append(",");
			sb.append(DvbNetworkDatabase.TransportStreams.MPEG_TRANSPORT_STREAM_ID).append(",");
			sb.append(DvbNetworkDatabase.TransportStreams.NETWORK_ID).append(",");
			sb.append(DvbNetworkDatabase.TransportStreams.ORIGINAL_NETWORK_ID).append(",");
			sb.append(DvbNetworkDatabase.TransportStreams.INFO_VERSION).append(",");
			sb.append(DvbNetworkDatabase.Frequencies.TUNE_PARAM).append(" )");
			sb.append(" VALUES (");
			sb.append(fi.getFrequency()).append(",");
			sb.append(fi.getDeliveryType()).append(",");
			sb.append(dvb_tsid).append(",");
			sb.append(pat_tsid).append(",");
			sb.append(netid).append(",");
			sb.append(orgid).append(",");
			sb.append(0).append(",");
			sb.append("\"").append(fi.toString()).append("\" );");
			try{
				db.execSQL(sb.toString());
			}catch(Exception e){
				IPanelLog.e(TAG, "execSql err : "+e.toString());
			}
		}

		return true;
	}
	public boolean storeFrequnecys(SQLiteDatabase db, IPNetwork map) {
		IPanelLog.i("begin-end", "store frquency");
		StringBuffer sb = new StringBuffer();
		ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream ts = null;
		if(map!=null){
			IPanelLog.i(TAG,"map.size = "+map.transport_stream.size());
		}
		if (map != null && map.transport_stream != null) {
			int size = map.transport_stream.size();		
			for (int i = 0; i < size; ++i) {
				ts = map.transport_stream.get(i);
				if (ts == null)
					continue;
				long freq = ts.ipFreq.freq;
				String mod = ts.ipFreq.mod;
				long rate = ts.ipFreq.rate;
				int dvb_tsid = ts.tsid;
				int netid = map.nid;
				int orgid = ts.onid;
				/*String tune_param = "frequency://" + freq + "?symbol_rate="
						+ rate + "&delivery=" + map.delivery + "&modulation="
						+ mod + "&frequency=" + freq;*/
				String tune_param = createTune_Param(map,ts);
				IPanelLog.i(TAG, "storeFrequnecys :" + freq + "," + dvb_tsid + ","
						+ netid + "," + orgid + ";tune_param=" + tune_param);
				
				freqMap.put(dvb_tsid, freq);
//				sb.delete(0, sb.length());
//				sb.append("INSERT INTO ");
//				sb.append(DvbNetworkDatabase.Frequencies.TABLE_NAME);
//				sb.append(" (");
//				sb.append(DvbNetworkDatabase.Frequencies.FREQUENCY).append(",");
//				sb.append(DvbNetworkDatabase.Frequencies.DEVLIVERY_TYPE)
//						.append(",");
//				sb.append(
//						DvbNetworkDatabase.TransportStreams.TRANSPORT_STREAM_ID)
//						.append(",");
//				sb.append(DvbNetworkDatabase.TransportStreams.NETWORK_ID)
//						.append(",");
//				sb.append(
//						DvbNetworkDatabase.TransportStreams.ORIGINAL_NETWORK_ID)
//						.append(",");
//				sb.append(DvbNetworkDatabase.Frequencies.TUNE_PARAM).append(
//						" )");
//				sb.append(" VALUES (");
//				sb.append(freq).append(",");
//				sb.append(67).append(",");
//				sb.append(dvb_tsid).append(",");
//				sb.append(netid).append(",");
//				sb.append(orgid).append(",");
//				sb.append("\"").append(tune_param).append("\" );");
//				sb = 
				String s = createStoreFre(freq,67,dvb_tsid,netid,orgid,tune_param);
				Log.i(TAG,"s = "+s);
				try {
					db.execSQL(s);
				} catch (Exception e) {
					IPanelLog.e(TAG, "execSql err : " + e.toString());
				}
			}
		}
		IPanelLog.i("begin-end", "end store frquency");
		return true;
	}

	public boolean storeGroups(SQLiteDatabase db, DvbNetworkMapping map) {
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		TransportStream ts = null;
		SiSDTServices sdt = null;
		SiSDTServices.Service sdts = null;
		int size = map.sizeOfTransportStream(), ssize = 0;
		int groupsize = 0;
		for (int i = 0; i < size; ++i) {
			ts = map.transportStreamAt(i);
			if (ts.siTransportStram == null)
				continue;
			sdt = ts.siServices;
			if (sdt == null)
				continue;
			if (sdt.service == null) {
				continue;
			}
			ssize = sdt.service.size();
			for (int j = 0; j < ssize; j++) {
				sdts = sdt.service.elementAt(j);
				int program_number = sdts.service_id;
				if (sdts.service_classified == null)
					continue;
				freq = ts.siTransportStram.frequency_info.getFrequencyInfo().getFrequency();
				groupsize = sdts.service_classified.size();
				for (int k = 0; k < groupsize; k++) {
					GroupsInfo service_class = sdts.service_classified.elementAt(k);
					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(Groups.TABLE_NAME);
					sb.append(" (");
					sb.append(Groups.FREQUENCY).append(",");
					sb.append(Groups.GROUP_ID).append(",");
					sb.append(Groups.PROGRAM_NUMBER).append(",");
					sb.append(Groups.GROUP_NAME).append(" )");
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(service_class.bouquetId).append(",");
					sb.append(program_number).append(",");
					sb.append("\"").append(service_class.groupBame).append("\" );");
					IPanelLog.i(TAG, "sb =  "+sb.toString());
					try{
						db.execSQL(sb.toString());
					}catch(Exception e){
						IPanelLog.e(TAG, "execSql err : "+e.toString());
					}
				}
			}
		}
		return true;
	}
	public boolean storeGroups(SQLiteDatabase db, IPBouquet map) {
		IPanelLog.i("begin-end", "store group");
		db.execSQL("INSERT INTO groups (frequency,group_id,program_number,user_id,group_name ) VALUES ("+-1+","+-1+","+-1+","+-1+","+"null"+");");
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		IPBouquet.Bouquet.Channel channel = null;
		int size = map.bouquets.size();
		for (int i = 0; i < size; i++) {
			Bouquet bouquet = map.bouquets.get(i);
			if (bouquet == null)
				continue;
			int bouquet_id = bouquet.id;
			String bouquet_name = bouquet.name;
			IPanelLog.i("bouquet--dvb", "---store---bouquet_id="+bouquet_id+";bouquet.name="+bouquet.name);
			int csize = bouquet.cs.size();
			IPanelLog.i(TAG, "----------------csize="+csize);
			for (int j = csize-1; j >=0; j--) {
				channel = bouquet.cs.get(j);
				if (channel != null) {
					int tsid = channel.ipts.tsid;
					int sid = channel.service.sid;
					IPanelLog.i(TAG, "bouquet tsid=" + tsid + ";sid=" + sid
							+ ";freqMap=" + freqMap);
					if(freqMap == null || freqMap.size() == 0){
						freqMap = new SparseArray<Long>();
						fillFreqMap(db, freqMap);
					}
					freq = freqMap.get(tsid);
					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(Groups.TABLE_NAME);
					sb.append(" (");
					sb.append(Groups.FREQUENCY).append(",");
					sb.append(Groups.GROUP_ID).append(",");
					sb.append(Groups.PROGRAM_NUMBER).append(",");
					sb.append(Groups.GROUP_NAME).append(" )");
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(bouquet_id).append(",");
					sb.append(sid).append(",");
					sb.append("\"").append(bouquet_name).append("\" );");
					try {
						IPanelLog.d(TAG, "sb.toString() = " + sb.toString());
						db.execSQL(sb.toString());
					} catch (Exception e) {
						IPanelLog.e(TAG, "execSql err : " + e.toString());
					}
				}
			}	
		}
		IPanelLog.i("begin-end", "end store group");
		return true;
	}
	
	public boolean storeBGroups(SQLiteDatabase db, DVBMenuBouquet map) {
		IPanelLog.i("begin-end", "store bgroup");
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		BBouquet bbouquet = null;
		BChannel bchannel = null;
		if(map == null)
			return false;
		
		int size = map.b_bouquets.size();
		for(int i=0; i<size; i++){
			bbouquet = map.b_bouquets.get(i);
			if(bbouquet != null){
				int bouquetId = bbouquet.b_id;
				String bouquet_name = bbouquet.b_name;
				int bcsize = bbouquet.cs.size();
				for(int j=0; j< bcsize; j++){
					bchannel = bbouquet.cs.get(j);
					String channelId = bchannel.contentId;
					int programNumber = 0;
					if((programNumber = judgeChannelId(channelId)) != -1){
						IPanelLog.i(TAG, "store bbouquet is ok`");
						freq = PFreqMap.get(programNumber);
						sb.delete(0, sb.length());
						sb.append("INSERT INTO ");
						sb.append(Groups.TABLE_NAME);
						sb.append(" (");
						sb.append(Groups.FREQUENCY).append(",");
						sb.append(Groups.GROUP_ID).append(",");
						sb.append(Groups.PROGRAM_NUMBER).append(",");
						sb.append(Groups.GROUP_NAME).append(" )");
						sb.append(" VALUES (");
						sb.append(freq).append(",");
						sb.append(bouquetId).append(",");
						sb.append(programNumber).append(",");
						sb.append("\"").append(bouquet_name).append("\" );");
						try {
							db.execSQL(sb.toString());
						} catch (Exception e) {
							IPanelLog.e(TAG, "execSql err : " + e.toString());
						}
					}else{
						IPanelLog.i(TAG, "it's not exist");
					}
						
				}
			}
		}
		IPanelLog.i("begin-end", "end store bgroup");
		return true;
	}

	private int judgeChannelId(String channelId) {
		IPanelLog.i(TAG, "judge-->judgeChannelId-->" + channleMap);
		int program = (channleMap.get(channelId) == null ? -1 : channleMap.get(channelId));
		if (program < 0)
			return -1;
		return program;
	}

	public boolean storeChannels(SQLiteDatabase db, DvbNetworkMapping map) {
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		int tsid = 0;
		TransportStream ts = null;
		SiSDTServices sdt = null;
		SiSDTServices.Service sdts = null;
		int size = map.sizeOfTransportStream(), ssize = 0;
		for (int i = 0; i < size; ++i) {
			ts = map.transportStreamAt(i);
			if (ts.siTransportStram == null){
				IPanelLog.i(TAG, "------ts.siTransportStram == null");
				continue;
			}
			freq = ts.siTransportStram.frequency_info.getFrequencyInfo().getFrequency();
			tsid = ts.siTransportStram.transport_stream_id;
			if (ts.siServices == null) {
				IPanelLog.i(TAG, "------ts.siServices == null");
				continue;
			}
			sdt = ts.siServices;
			if (sdt == null) {
				IPanelLog.i(TAG, "------sdt == null");
				continue;
			}
			if (sdt.service == null) {
				IPanelLog.i(TAG, "------sdt.service == null");
				continue;
			}

			ssize = sdt.service.size();
			for (int j = 0; j < ssize; j++) {
				sdts = sdt.service.elementAt(j);
				if (sdts == null) {
					IPanelLog.i(TAG, "------sdts == null");
					continue;
				}
				int program_number = sdts.service_id;
				IPanelLog.i(TAG, "-----------ts.siPrograms="+ts.siPrograms);
				if (ts.siPrograms != null) {
					int psize = ts.siPrograms.programSize();
					for (int k = 0; k < psize; k++) {
						int isHaveStream = 0;
						if (program_number == ts.siPrograms.program.get(k).program_number)
							isHaveStream++;
						if (isHaveStream == 0)
							continue;
					}
				}
				int channel_type = serviceType2ChannelType(sdts.service_type);
				String channel_name = sdts.service_name;
				String channel_name_en = sdts.service_name_en;
				String provider_name = sdts.provider_name;
				

				
				int servive_type = sdts.service_type;

				if (channel_name != null) {
					if (channel_name.indexOf("$") != -1) {
						continue;
					}
				}
				int eit_pf_flag = sdts.eit_present_following_flag;
				int eit_sch_flag = sdts.eit_schedule_flag;
				int free_ca = sdts.free_ca_mode;
				int vedio_mode = sdts.video_mode;
				String short_service_name = sdts.short_service_name;
				String short_provide_name = sdts.short_provider_name;

				IPanelLog.i(TAG, "storeChannels :" + freq + "," + program_number + "," + channel_type
						+ "," + channel_name + "," + channel_name_en + "," + provider_name + ","
						+ servive_type + "," + eit_pf_flag + "," + eit_sch_flag + "," + free_ca
						+ "," + short_service_name + "," + short_provide_name);

				sb.delete(0, sb.length());
				sb.append("INSERT INTO ");
				sb.append(DvbNetworkDatabase.Channels.TABLE_NAME);
				sb.append(" (");
				sb.append(DvbNetworkDatabase.Channels.FREQUENCY).append(",");
				sb.append(TransportStreams.TRANSPORT_STREAM_ID).append(",");
				sb.append(DvbNetworkDatabase.Channels.PROGRAM_NUMBER).append(",");
				sb.append(DvbNetworkDatabase.Channels.CHANNEL_TYPE).append(",");
				 sb.append(DvbNetworkDatabase.Channels.CHANNEL_NUMBER).append(",");
				sb.append(DvbNetworkDatabase.Channels.CHANNEL_NAME).append(",");
				sb.append(DvbNetworkDatabase.Channels.CHANNEL_NAME_EN).append(",");
				sb.append(DvbNetworkDatabase.Channels.PROVIDER_NAME).append(",");
				sb.append(DvbNetworkDatabase.Services.SERVICE_TYPE).append(",");
				sb.append(DvbNetworkDatabase.Services.EIT_PF_FLAG).append(",");
				sb.append(DvbNetworkDatabase.Services.EIT_SCHEDULE_FLAG).append(",");
				sb.append(DvbNetworkDatabase.Services.IS_FREE_CA).append(",");
				sb.append(DvbNetworkDatabase.Streams.PRESENTING_FORM).append(",");
				sb.append(DvbNetworkDatabase.Services.SHORT_SERVICE_NAME).append(",");
				sb.append(DvbNetworkDatabase.Services.SHORT_PROVIDER_NAME).append(" )");
				sb.append(" VALUES (");
				sb.append(freq).append(",");
				sb.append(tsid).append(",");
				sb.append(program_number).append(",");
				sb.append(channel_type).append(",");
				 sb.append(sdts.channel_number).append(",");
				sb.append("\"").append(channel_name).append("\",");
				sb.append("\"").append(channel_name_en).append("\",");
				sb.append("\"").append(provider_name).append("\",");
				sb.append(servive_type).append(",");
				sb.append(eit_pf_flag).append(",");
				sb.append(eit_sch_flag).append(",");
				sb.append(free_ca).append(",");
				sb.append(vedio_mode).append(",");
				sb.append("\"").append(short_service_name).append("\",");
				sb.append("\"").append(short_provide_name).append("\" );");
				try{
					db.execSQL(sb.toString());
				}catch(Exception e){
					IPanelLog.e(TAG, "execSql err : "+e.toString());
				}
				IPanelLog.i(TAG, "channel :" + channel_name + " is end");
			}
		}

		return true;
	}
	public boolean storeChannels(SQLiteDatabase db, IPNetwork map) {
		IPanelLog.i("begin-end", "store channel map="+map+";map.transport_stream="+(map == null ? "map transportstream is null" : map.transport_stream));
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		int tsid = 0;
		ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream ts = null;
		Channel channel = null;
		if(map != null && map.transport_stream != null){
			int size = map.transport_stream.size();
			IPanelLog.i(TAG, "ip-->store channel transport_stream size="+size);
			for (int i = 0; i < size; ++i) {
				ts = map.transport_stream.get(i);
				if (ts == null){
					IPanelLog.i(TAG, "------ts.siTransportStram == null");
					continue;
				}
					
				freq = ts.ipFreq.freq;
				tsid = ts.tsid;
				IPanelLog.i(TAG, "ip-->store channel transport_stream freq="+freq);
				int csize = ts.channels.size();
				IPanelLog.i(TAG, "ip-->store channel transport_stream Channels="+ts.channels);
				IPanelLog.i(TAG, "ip-->store channel transport_stream channels size="+csize);
				for(int j=0; j<csize; j++){
					channel = ts.channels.get(j);
					String channel_name = channel.name;
					int service_type = channel.sdtService.type;
					int channel_type = serviceType2ChannelType(service_type);
					String channel_name_en = "";
					String provider_name = "";
					int program_number = channel.sdtService.sid;
					int channel_number = channel.number;
					String channelId = channel.channelId;
					IPanelLog.i(TAG, "ip-->storeChannels :" + freq + "," + program_number + "," + channel_type
							+ "," + channel_name + "," + channel_name_en + "," + provider_name );
					channleMap.put(channelId, program_number);
					PFreqMap.put(program_number, freq);
					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(DvbNetworkDatabase.Channels.TABLE_NAME);
					sb.append(" (");
					sb.append(DvbNetworkDatabase.Channels.FREQUENCY).append(",");
					sb.append(TransportStreams.TRANSPORT_STREAM_ID).append(",");
					sb.append(DvbNetworkDatabase.Channels.PROGRAM_NUMBER).append(",");
					sb.append(DvbNetworkDatabase.Channels.CHANNEL_TYPE).append(",");
					sb.append(DvbNetworkDatabase.Channels.CHANNEL_NUMBER).append(",");
					sb.append(DvbNetworkDatabase.Channels.CHANNEL_NAME).append(",");
					sb.append(DvbNetworkDatabase.Channels.CHANNEL_NAME_EN).append(",");
					sb.append(DvbNetworkDatabase.Channels.PROVIDER_NAME).append(",");
					sb.append(DvbNetworkDatabase.Services.SERVICE_TYPE).append(")");				
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(tsid).append(",");
					sb.append(program_number).append(",");
					sb.append(channel_type).append(",");
				    sb.append(channel_number).append(",");
					sb.append("\"").append(channel_name).append("\",");
					sb.append("\"").append(channel_name_en).append("\",");
					sb.append("\"").append(provider_name).append("\",");
					sb.append(service_type).append(");");
				
					try{
						db.execSQL(sb.toString());
					}catch(Exception e){
						IPanelLog.e(TAG, "execSql err : "+e.toString());
					}
					IPanelLog.i(TAG, "ip-->channel :" + channel_name + " is end");
				}
			}
		}
		IPanelLog.i("begin-end", "end store channel");
		return true;
	}

	/**
	 * store program_warship table
	 * 
	 * @param db
	 * @param map
	 * @return
	 */
	public boolean storeProgramWardship(SQLiteDatabase db, DvbNetworkMapping map) {
		IPanelLog.d(TAG, "call storeProgramWardship");
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		TransportStream ts = null;
		SiSDTServices sdt = null;
		SiSDTServices.Service sdts = null;
		int size = map.sizeOfTransportStream(), ssize = 0;
		for (int i = 0; i < size; ++i) {
			ts = map.transportStreamAt(i);
			if (ts.siTransportStram == null){
				IPanelLog.i(TAG, "------ts.siTransportStram == null");
				continue;
			}
				
			freq = ts.siTransportStram.frequency_info.getFrequencyInfo().getFrequency();
			if (ts.siServices == null) {
				IPanelLog.i(TAG, "------ts.siServices == null");
				continue;
			}
			sdt = ts.siServices;
			if (sdt == null) {
				IPanelLog.i(TAG, "------sdt == null");
				continue;
			}
			if (sdt.service == null) {
				IPanelLog.i(TAG, "------sdt.service == null");
				continue;
			}

			ssize = sdt.service.size();
			for (int j = 0; j < ssize; j++) {
				sdts = sdt.service.elementAt(j);
				if (sdts == null) {
					IPanelLog.i(TAG, "------sdts == null");
					continue;
				}
				int program_number = sdts.service_id;
				int psize = ts.siPrograms.programSize();
				for (int k = 0; k < psize; k++) {
					int isHaveStream = 0;
					if(program_number == ts.siPrograms.program.get(k).program_number)
						isHaveStream++;
					if(isHaveStream == 0)
						continue;
				}
				
				int service_type = sdts.service_type;
				String channel_name = sdts.service_name;				
				IPanelLog.i(TAG, "service_type = "+service_type+";channel_name="+channel_name);
				if(channel_name == null)
					continue;
				if(service_type==2)
					continue;//过滤广播 
					
				if (service_type != 253 && service_type != 130 && service_type != 12
						&& !channel_name.contains("null")
						&& !channel_name.contains("?")) {
					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(ProgramWardships.TABLE_NAME);
					sb.append(" (");
					sb.append(ProgramWardships.FREQUENCY).append(",");
					sb.append(ProgramWardships.CHANNEL_NUMBER).append(",");
					sb.append(ProgramWardships.PROGRAM_NUMBER).append(",");
					sb.append(ProgramWardships.CHANNEL_NAME).append(",");
					sb.append(ProgramWardships.WARDSHIP).append(")");
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(sdts.channel_number).append(",");
					sb.append(program_number).append(",\"");
					sb.append(channel_name).append("\",");
					sb.append(0).append(");");
					try {
						db.execSQL(sb.toString());
					} catch (Exception e) {
						IPanelLog.e(TAG, "execSql err : " + e.toString());
					}
				}
				IPanelLog.i(TAG, "p_channel :" + channel_name + " is end");
			}
		}		
		return true;		
	}
	public boolean storeStreams(SQLiteDatabase db, IPNetwork map) {
		IPanelLog.i("begin-end", "store stream----------------");
		StringBuffer sb = new StringBuffer();	
		ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream ts = null;
		Channel channel = null;
		Stream stream = null;
		long freq = 0;
		int size = map.transport_stream.size();
		for (int i = 0; i < size; ++i) {
			ts = map.transport_stream.get(i);
			if (ts == null)
				continue;
			freq = ts.ipFreq.freq;			
			int ssize = ts.channels.size();
			for (int k = 0; k < ssize; k++) {
				channel = ts.channels.get(k);
				if (channel== null)
					continue;
				int program_number = channel.sdtService.sid;
				int pcrpid = channel.pcrpid;
				IPanelLog.i(TAG, "------------------------");
				String stream_type_name = getStreamTypeName(0xA0);
					
				IPanelLog.i(TAG, "storeStreams :" + freq + "," + program_number+";stream_type_name="+stream_type_name );

				sb.delete(0, sb.length());
				sb.append("INSERT INTO ");
				sb.append(NetworkDatabase.Streams.TABLE_NAME);
				sb.append(" (");
				sb.append(DvbNetworkDatabase.ElementaryStreams.FREQUENCY).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.PROGRAM_NUMBER).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_TYPE).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_PID).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.ASSOCIATION_TAG).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.COMPONENT_TAG).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_TYPE_NAME).append(" )");
				sb.append(" VALUES (");
				sb.append(freq).append(",");
				sb.append(program_number).append(",");
				sb.append(0xA0).append(",");
				sb.append(pcrpid).append(",");	
				sb.append(0).append(",");
				sb.append(0).append(",");
				sb.append("\"").append(stream_type_name).append("\" );");
				db.execSQL(sb.toString());
				
				int stsize = channel.streams.size();
				for(int j=0; j<stsize; j++){
					stream = channel.streams.get(j);
					if(stream == null){
						continue;
					}
					int stream_pid = stream.pid;
					int stream_type = stream.type;
					
					stream_type_name = getStreamTypeName(stream_type);

					IPanelLog.i(TAG, "storeStreams :" + freq + "," + program_number +";stream_type_name = "+ stream_type_name);

					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(NetworkDatabase.Streams.TABLE_NAME);
					sb.append(" (");
					sb.append(DvbNetworkDatabase.ElementaryStreams.FREQUENCY).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.PROGRAM_NUMBER).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_TYPE).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_PID).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.ASSOCIATION_TAG).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.COMPONENT_TAG).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_TYPE_NAME).append(")");
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(program_number).append(",");
					sb.append(stream_type).append(",");
					sb.append(stream_pid).append(",");
					sb.append(0).append(",");
					sb.append(0).append(",");
					sb.append("\"").append(stream_type_name).append("\" );");
					try{
						db.execSQL(sb.toString());
					}catch(Exception e){
						IPanelLog.e(TAG, "execSql err : "+e.toString());
					}
					
				}		
			}
		}
		IPanelLog.i("begin-end", "end store stream");
		return true;		
	}
	
	public boolean storeStreams(SQLiteDatabase db, DvbNetworkMapping map) {
		StringBuffer sb = new StringBuffer();
		TransportStream ts = null;
		SiPATServices.Program pats = null;
		SiPATServices.Program.Stream patstream = null;
		long freq = 0;
		int size = map.sizeOfTransportStream();

		for (int i = 0; i < size; ++i) {
			ts = map.transportStreamAt(i);
			if (ts.siTransportStram == null)
				continue;
			freq = ts.siTransportStram.frequency_info.getFrequencyInfo().getFrequency();
			if (ts.siPrograms == null || ts.siPrograms.program == null)
				continue;
			int ssize = ts.siPrograms.programSize();
			for (int k = 0; k < ssize; k++) {
				pats = ts.siPrograms.program.get(k);
				if (pats.stream == null)
					continue;
				int program_number = pats.program_number;
				int pcr_pid = pats.pcr_pid;

				int pcr_stream_type = 0xA0;
				String stream_type_name = getStreamTypeName(pcr_stream_type);
				IPanelLog.i(TAG, "storeStreams :" + freq + "," + program_number + "," + pcr_pid);
				sb.delete(0, sb.length());
				sb.append("INSERT INTO ");
				sb.append(NetworkDatabase.Streams.TABLE_NAME);
				sb.append(" (");
				sb.append(DvbNetworkDatabase.ElementaryStreams.FREQUENCY).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.PROGRAM_NUMBER).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_TYPE).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_PID).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.ASSOCIATION_TAG).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.COMPONENT_TAG).append(",");
				sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_TYPE_NAME).append(" )");
				sb.append(" VALUES (");
				sb.append(freq).append(",");
				sb.append(program_number).append(",");
				sb.append(pcr_stream_type).append(",");
				sb.append(pcr_pid).append(",");	
				sb.append(0).append(",");
				sb.append(0).append(",");
				sb.append("\"").append(stream_type_name).append("\" );");
				Log.i(TAG,"storeStream sql = " + sb.toString());
				db.execSQL(sb.toString());
				int spsize = pats.stream.size();
				for (int l = 0; l < spsize; l++) {
					patstream = pats.stream.get(l);
					int stream_pid = patstream.stream_pid;
					int stream_type = patstream.stream_type;
					int componet_tag = patstream.component_tag;
					stream_type_name = getStreamTypeName(patstream.stream_type);
					Log.i(TAG,"stream_type = " + patstream.stream_type + ";stream_type_name = " + stream_type_name);
					IPanelLog.i(TAG, "storeStreams :" + freq + "," + program_number + "," + pcr_pid);
					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(NetworkDatabase.Streams.TABLE_NAME);
					sb.append(" (");
					sb.append(DvbNetworkDatabase.ElementaryStreams.FREQUENCY).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.PROGRAM_NUMBER).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_TYPE).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_PID).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.ASSOCIATION_TAG).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.COMPONENT_TAG).append(",");
					sb.append(DvbNetworkDatabase.ElementaryStreams.STREAM_TYPE_NAME).append(")");
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(program_number).append(",");
					sb.append(stream_type).append(",");
					sb.append(stream_pid).append(",");
					sb.append(0).append(",");
					sb.append(componet_tag).append(",");
					sb.append("\"").append(stream_type_name).append("\" );");
					try{
						Log.i(TAG, "storeStream sql = " + sb.toString());
						db.execSQL(sb.toString());
					}catch(Exception e){
						IPanelLog.e(TAG, "execSql err : "+e.toString());
					}
				}
			}
		}

		return true;
	}

	public boolean storeRegionID(SQLiteDatabase db, DvbNetworkMapping map) {
		StringBuffer sb = new StringBuffer();
		long vodFreq = 0;
		String regionID = "";
		RegionID reg;

		int size = map.sizeOfRegionID();

		IPanelLog.i(TAG, "DvbDatabaseBuilder size:" + size);
		
		for (int i = 0; i < size; i++) {
			
			reg = map.regionIDAt(i);
			vodFreq = reg.getFreqency();
			IPanelLog.i(TAG,"storeRegionID vodFreq:" + vodFreq);
			regionID = reg.getRegionId();
			IPanelLog.i(TAG,"storeRegionID regionID:" + regionID);

			sb.delete(0, sb.length());
			sb.append("INSERT INTO ");
			sb.append("regionIds");
			sb.append(" (");
			sb.append("vod_main_frequency").append(",");
			sb.append("region_id").append(")");
			sb.append(" VALUES (");
			sb.append(vodFreq).append(",");
			sb.append("\"" + regionID + "\"").append(" );");
			IPanelLog.i(TAG, "regionID sb.toString:" + sb.toString());
			try {
				db.execSQL(sb.toString());
			} catch (Exception e) {
				IPanelLog.e(TAG, "execSql err : " + e.toString());
			}
		}
		return true;
		
	}
	public boolean storeEvents(SQLiteDatabase db, DvbNetworkMapping map) {
		StringBuffer sb = new StringBuffer();
		TransportStream ts = null;
		long freq = 0;
		int size = map.sizeOfTransportStream();
		IPanelLog.d(TAG, "storeEvents size = " + size);
		for (int i = 0; i < size; ++i) {
			ts = map.transportStreamAt(i);
			freq = ts.siTransportStram.frequency_info.getFrequencyInfo().getFrequency();			
			for (int tableid = 0x50; tableid < ts.si_eit_last_tid; tableid++) {
				for (int j = 0; j < ts.si_eit_last_sn; j++) {
					SiEITEvents seit = ts.getSiEventsSchedule(tableid, j);

					int program_number = seit.service_id;
					int ssize = seit.event.size();
					for (int k = 0; k < ssize; k++) {
						SiEITEvents.Event sss = seit.event.get(k);
						String event_name = sss.event_name;
						String event_name_en = sss.event_name_en;
						long start_time = TimerFormater.rfc3339tolong(sss.start_time); 
						long duration = sss.duration;
						long end_time = start_time + duration;

						int event_id = sss.event_id;
						String short_event_name = sss.short_event_name;
						int free_ca = sss.free_ca_mode;
						int run_status = sss.running_status;
						
						sb.delete(0, sb.length());
						sb.append("INSERT INTO ");
						sb.append(NetworkDatabase.Events.TABLE_NAME);
						sb.append(" (");
						sb.append(DvbNetworkDatabase.Events.FREQUENCY).append(",");
						sb.append(DvbNetworkDatabase.Events.PROGRAM_NUMBER).append(",");
						sb.append(DvbNetworkDatabase.Events.EVENT_NAME).append(",");
						sb.append(DvbNetworkDatabase.Events.EVENT_NAME_EN).append(",");
						sb.append(DvbNetworkDatabase.Events.START_TIME).append(",");
						sb.append(DvbNetworkDatabase.Events.END_TIME).append(",");
						sb.append(DvbNetworkDatabase.Events.DURATION).append(",");
						sb.append(DvbNetworkDatabase.ServiceEvents.EVENT_ID).append(",");
						sb.append(DvbNetworkDatabase.ServiceEvents.IS_FREE_CA).append(",");
						sb.append(DvbNetworkDatabase.ServiceEvents.RUNNING_STATUS).append(",");
						sb.append(DvbNetworkDatabase.ServiceEvents.SHORT_EVENT_NAME).append(" )");
						sb.append(" VALUES (");
						sb.append(freq).append(",");
						sb.append(program_number).append(",");
						sb.append("\"").append(event_name).append("\",");
						sb.append("\"").append(event_name_en).append("\",");
						sb.append(start_time).append(",");
						sb.append(end_time).append(",");
						sb.append(duration).append(",");
						sb.append(event_id).append(",");
						sb.append(free_ca).append(",");
						sb.append(run_status).append(",");
						sb.append("\"").append(short_event_name).append("\" );");
						try {
							db.execSQL(sb.toString());
						} catch (Exception e) {
							IPanelLog.e(TAG, "storeEvents execSql err : " + e.toString());
							e.printStackTrace();
						}
					}
				}
			}
		}

		return true;
	}

	public boolean storeEvents(SQLiteDatabase db, IPEITInfo map) {
		IPanelLog.i(TAG, "store event");
		StringBuffer sb = new StringBuffer();
		IPEITInfo.Channel eChannel = null;
		IPEITInfo.Channel.Event event = null;
		long freq = 0;	
		int size = map.channels.size();
		IPanelLog.i(TAG, "map.channels.size() = "+size);
		for (int i = 0; i < size; ++i) {
			eChannel = map.channels.get(i);
			IPanelLog.i(TAG, "eChannel = "+eChannel);
			if (eChannel != null) {
				int program_number = eChannel.service.sid;
				int tsid = eChannel.ipts.tsid;
				if(freqMap == null || freqMap.size() == 0){
					freqMap = new SparseArray<Long>();
					fillFreqMap(db, freqMap);
				}
				freq = freqMap.get(tsid);
				int esize = eChannel.events.size();
				IPanelLog.i(TAG, "eChannel.events.size() = "+esize);
				for(int j=0; j<esize; j++){
					event = eChannel.events.get(j);
					String name = event.name;
					long st = event.st;
					long et = event.et;
					
					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(NetworkDatabase.Events.TABLE_NAME);
					sb.append(" (");
					sb.append(DvbNetworkDatabase.Events.FREQUENCY).append(",");
					sb.append(DvbNetworkDatabase.Events.PROGRAM_NUMBER).append(",");
					sb.append(DvbNetworkDatabase.Events.EVENT_NAME).append(",");
					sb.append(DvbNetworkDatabase.Events.EVENT_NAME_EN).append(",");
					sb.append(DvbNetworkDatabase.Events.START_TIME).append(",");
					sb.append(DvbNetworkDatabase.Events.END_TIME).append(",");
					sb.append(DvbNetworkDatabase.Events.DURATION).append(",");
					sb.append(DvbNetworkDatabase.ServiceEvents.EVENT_ID).append(",");
					sb.append(DvbNetworkDatabase.ServiceEvents.IS_FREE_CA).append(",");
					sb.append(DvbNetworkDatabase.ServiceEvents.RUNNING_STATUS).append(",");
					sb.append(DvbNetworkDatabase.ServiceEvents.SHORT_EVENT_NAME).append(" )");
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(program_number).append(",");
					sb.append("\"").append(name).append("\",");
					sb.append("\"").append(name).append("\",");
					sb.append(st).append(",");
					sb.append(et).append(",");
					sb.append((et-st)).append(",");
					sb.append(1).append(",");
					sb.append(0).append(",");
					sb.append(0).append(",");
					sb.append("\"").append("ENG").append("\" );");
					IPanelLog.i(TAG,"storeEvents sb ="+sb.toString());
					try{
						db.execSQL(sb.toString());
					}catch(Exception e){
						IPanelLog.e(TAG, "execSql err : "+e.toString());
					}
				}
			}
		}
		IPanelLog.i("begin-end", "end store event");
		return true;
	}
	
	public boolean storeGuied(SQLiteDatabase db,DvbNetworkMapping map){
		IPanelLog.i("begin-end", "store guied");
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		@SuppressWarnings("unused")
		int tsid = 0;
		TransportStream ts = null;
		SiSDTServices sdt = null;
		SiSDTServices.Service sdts = null;
		int size = map.sizeOfTransportStream(), ssize = 0;/* , classsize; */
		for (int i = 0; i < size; ++i) {
			ts = map.transportStreamAt(i);
			if (ts.siTransportStram == null){
				IPanelLog.i(TAG, "------ts.siTransportStram == null");
				continue;
			}
			freq = ts.siTransportStram.frequency_info.getFrequencyInfo().getFrequency();
			tsid = ts.siTransportStram.transport_stream_id;
			if (ts.siServices == null) {
				IPanelLog.i(TAG, "------ts.siServices == null");
				continue;
			}
			sdt = ts.siServices;
			if (sdt == null) {
				IPanelLog.i(TAG, "------sdt == null");
				continue;
			}
			if (sdt.service == null) {
				IPanelLog.i(TAG, "------sdt.service == null");
				continue;
			}

			ssize = sdt.service.size();
			for (int j = 0; j < ssize; j++) {
				sdts = sdt.service.elementAt(j);
				if (sdts == null) {
					IPanelLog.i(TAG, "------sdts == null");
					continue;
				}
				int program_number = sdts.service_id;
				IPanelLog.i(TAG, "storeChannels :" + freq + "," + program_number );
				sb.delete(0, sb.length());
				sb.append("INSERT INTO ");
				sb.append(Guides.TABLE_NAME);
				sb.append(" (");
				sb.append(Guides.FREQUENCY).append(",");
				sb.append(Guides.PROGRAM_NUMBER).append(",");
				sb.append(Guides.VERSION).append(" )");
				sb.append(" VALUES (");
				sb.append(freq).append(",");
				sb.append(program_number).append(",");
				sb.append(0).append(");");
				try{
					db.execSQL(sb.toString());
				}catch(Exception e){
					IPanelLog.e(TAG, "execSql err : "+e.toString());
				}
			}
		}
		IPanelLog.i("begin-end", "end store guied");
		return true;
	}
	
	public boolean storeGuides(SQLiteDatabase db, IPEITInfo map) {
		IPanelLog.i("begin-end", "store guides map="+map);
		StringBuffer sb = new StringBuffer();
		IPEITInfo.Channel eChannel = null;
		@SuppressWarnings("unused")
		IPEITInfo.Channel.Event event = null;
		long freq = 0;	
		int size = map.channels.size();
		IPanelLog.i(TAG, "guides size="+size);
		for (int i = 0; i < size; ++i) {
			eChannel = map.channels.get(i);
			if (eChannel != null) {
				int program_number = eChannel.service.sid;
				int tsid = eChannel.ipts.tsid;
				if(freqMap == null || freqMap.size() == 0){
					freqMap = new SparseArray<Long>();
					fillFreqMap(db, freqMap);
				}
				freq = freqMap.get(tsid);
				sb.delete(0, sb.length());
				sb.append("INSERT INTO ");
				sb.append(Guides.TABLE_NAME);
				sb.append(" (");
				sb.append(Guides.FREQUENCY).append(",");
				sb.append(Guides.PROGRAM_NUMBER).append(",");
				sb.append(Guides.VERSION).append(" )");
				sb.append(" VALUES (");
				sb.append(freq).append(",");
				sb.append(program_number).append(",");
				sb.append(0).append(");");
				try{
					db.execSQL(sb.toString());
				}catch(Exception e){
					IPanelLog.e(TAG, "execSql err : "+e.toString());
				}
			}
		}
		IPanelLog.i("begin-end", "end store guides");
		return true;
	}
	
	public boolean storeGuides(SQLiteDatabase db, IPNetwork map) {
		Log.i("begin-end", "store guides map="+map);
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		int tsid = 0;
		int program_number = 0;
		ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream ts = null;
		Channel channel = null;
		if(map != null && map.transport_stream != null){
			int size = map.transport_stream.size();
			Log.i(TAG, "ip-->store channel transport_stream size="+size);
			for (int i = 0; i < size; ++i) {
				ts = map.transport_stream.get(i);
				if (ts == null){
					Log.i(TAG, "------ts.siTransportStram == null");
					continue;
				}
					
				freq = ts.ipFreq.freq;
				tsid = ts.tsid;
				Log.i(TAG, "ip-->store channel transport_stream freq="+freq+";tsid="+tsid);
				int csize = ts.channels.size();
				Log.i(TAG, "ip-->store channel transport_stream channels size="+csize);
				for(int j=0; j<csize; j++){
					channel = ts.channels.get(j);
					if(channel == null || channel.streams.size() == 0){
						Log.i(TAG, "storeGuides--->invalid channel");
						continue;
					}					
					program_number = channel.sdtService.sid;
					Log.i(TAG, "-------guides programnumber="+program_number);
					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(Guides.TABLE_NAME);
					sb.append(" (");
					sb.append(Guides.FREQUENCY).append(",");
					sb.append(Guides.PROGRAM_NUMBER).append(",");
					sb.append(Guides.VERSION).append(" )");
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(program_number).append(",");
					sb.append(0).append(");");
					try{
						db.execSQL(sb.toString());
					}catch(Exception e){
						Log.e(TAG, "execSql err : "+e.toString());
					}
				}
			}
		}
		Log.i("begin-end", "end store guides");
		return true;
	}
	
	public boolean storeGuides(SQLiteDatabase db, IPNetwork map, Map<ChannelKey, Integer>loaddedGuideVersions) {
		Log.i("begin-end", "store guides map="+map+";loaddedGuideVersions size="+loaddedGuideVersions.size());
		StringBuffer sb = new StringBuffer();
		long freq = 0;
		int tsid = 0;
		int program_number = 0;
		ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream ts = null;
		Channel channel = null;
		if(map != null && map.transport_stream != null){
			int size = map.transport_stream.size();
			Log.i(TAG, "ip-->store channel transport_stream size="+size);
			for (int i = 0; i < size; ++i) {
				ts = map.transport_stream.get(i);
				if (ts == null){
					Log.i(TAG, "------ts.siTransportStram == null");
					continue;
				}
					
				freq = ts.ipFreq.freq;
				tsid = ts.tsid;
				Log.i(TAG, "ip-->store channel transport_stream freq="+freq+";tsid="+tsid);
				int csize = ts.channels.size();
				Log.i(TAG, "ip-->store channel transport_stream channels size="+csize);
				for(int j=0; j<csize; j++){
					channel = ts.channels.get(j);
					if(channel == null || channel.streams.size() == 0){
						Log.i(TAG, "storeGuides--->invalid channel");
						continue;
					}				
					program_number = channel.sdtService.sid;
					ChannelKey currentKey = new ChannelKey(freq, program_number);
					
					int version = judgeVersionByRecord(currentKey, loaddedGuideVersions);
					
					Log.i(TAG, "-------guides programnumber="+program_number+";version="+version);
					sb.delete(0, sb.length());
					sb.append("INSERT INTO ");
					sb.append(Guides.TABLE_NAME);
					sb.append(" (");
					sb.append(Guides.FREQUENCY).append(",");
					sb.append(Guides.PROGRAM_NUMBER).append(",");
					sb.append(Guides.VERSION).append(" )");
					sb.append(" VALUES (");
					sb.append(freq).append(",");
					sb.append(program_number).append(",");
					sb.append(version).append(");");
					try{
						db.execSQL(sb.toString());
					}catch(Exception e){
						Log.e(TAG, "execSql err : "+e.toString());
					}
				}
			}
		}
		//此时 loaddedGuideVersions size为0
		Log.i("begin-end", "end store guides loaddedGuideVersions size="+loaddedGuideVersions.size());
		return true;
	}
	
	protected int judgeVersionByRecord(ChannelKey currentKey,
			Map<ChannelKey, Integer> loaddedGuideVersions) {
		return 0;
	}
	
	public boolean storeEcm(SQLiteDatabase db, DvbNetworkMapping map) {
		IPanelLog.i("---begin ecm", "ecm store go in");
		StringBuffer sb = new StringBuffer();
		TransportStream ts = null;
		SiPATServices.Program pats = null;
		SiPATServices.Program.Stream pmtStream = null;
		SiPATServices.Program.Stream.Ecm pmtEcm = null;
		
		long freq = 0;
		int size = map.sizeOfTransportStream();

		for (int i = 0; i < size; ++i) {
			ts = map.transportStreamAt(i);
			if (ts.siTransportStram == null)
				continue;
			freq = ts.siTransportStram.frequency_info.getFrequencyInfo().getFrequency();
			if (ts.siPrograms == null) {
				IPanelLog.i(TAG, "ecm ts.siPrograms == null");
				continue;
			}
			int ssize = ts.siPrograms.programSize();
			for (int k = 0; k < ssize; k++) {
				pats = ts.siPrograms.program.get(k);
				if (pats.stream == null)
					continue;
				int program_number = pats.program_number;
				
				int spsize = pats.streamSize();
				for (int j = 0; j < spsize; j++) {
					pmtStream = pats.stream.get(j);
					if(pmtStream == null)
						continue;
					int esize = pmtStream.ecmSize();
					for (int l = 0; l < esize; l++) {
						pmtEcm = pmtStream.ecm.get(l);
						int ca_system_id = pmtEcm.ca_system_id;
						int ecm_pid = pmtEcm.ecm_pid;
						int stream_pid = pmtStream.stream_pid;
						IPanelLog.d(TAG, "ca_system_id = "+pmtEcm.ca_system_id+"  ecm_pid = "+pmtEcm.ecm_pid);
						sb.delete(0, sb.length());
						sb.append("INSERT INTO ");
						sb.append(NetworkDatabase.Ecms.TABLE_NAME);
						sb.append(" (");
						sb.append(DvbNetworkDatabase.Ecms.CA_SYSTEM_ID).append(",");
						sb.append(DvbNetworkDatabase.Ecms.FREQUENCY).append(",");
						sb.append(DvbNetworkDatabase.Ecms.PROGRAM_NUMBER).append(",");
						sb.append(DvbNetworkDatabase.Ecms.STREAM_PID).append(",");
						sb.append(DvbNetworkDatabase.Ecms.ECM_PID).append(" )");
						sb.append(" VALUES (");
						sb.append(ca_system_id).append(",");
						sb.append(freq).append(",");
						sb.append(program_number).append(",");
						sb.append(stream_pid).append(",");
						sb.append(ecm_pid).append(" );");
						try{
							db.execSQL(sb.toString());
						}catch(Exception e){
							IPanelLog.e(TAG, "execSql err : "+e.toString());
						}
					}					
				}
			}
		}
		IPanelLog.i("---begin ecm", "ecm store end");
		return true;
	}
	
	public boolean storeEcm(SQLiteDatabase db, IPNetwork map) {
		StringBuffer sb = new StringBuffer();
		ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream ts = null;
		Channel channel = null;
		Stream stream = null;
		ECM ecm = null;
		
		long freq = 0;
		if (map != null && map.transport_stream != null) {
			int size = map.transport_stream.size();		
			for (int i = 0; i < size; ++i) {
				ts = map.transport_stream.get(i);
				if (ts == null)
					continue;
				freq = ts.ipFreq.freq;
				int csize = ts.channels.size();
				for (int k = 0; k < csize; k++) {
					channel = ts.channels.get(k);
					int service_id = channel.sdtService.sid;
					if(channel != null){
						int ssize = channel.streams.size();
						for(int j=0; j<ssize; j++){
							stream = channel.streams.get(j);
							if(stream != null){
								int stream_id = stream.pid;
								int stsize = stream.ecms.size();
								for(int n=0; n<stsize; n++){
									ecm = stream.ecms.get(n);
									int ecm_id = ecm.pid;
									int caids = ecm.caids;									
									sb.delete(0, sb.length());
									sb.append("INSERT INTO ");
									sb.append(NetworkDatabase.Ecms.TABLE_NAME);
									sb.append(" (");
									sb.append(DvbNetworkDatabase.Ecms.CA_SYSTEM_ID).append(",");
									sb.append(DvbNetworkDatabase.Ecms.FREQUENCY).append(",");
									sb.append(DvbNetworkDatabase.Ecms.PROGRAM_NUMBER).append(",");
									sb.append(DvbNetworkDatabase.Ecms.STREAM_PID).append(",");
									sb.append(DvbNetworkDatabase.Ecms.ECM_PID).append(" )");
									sb.append(" VALUES (");
									sb.append(caids).append(",");
									sb.append(freq).append(",");
									sb.append(service_id).append(",");
									sb.append(stream_id).append(",");
									sb.append(ecm_id).append(" );");
									try{
										db.execSQL(sb.toString());
									}catch(Exception e){
										IPanelLog.e(TAG, "execSql err : "+e.toString());
									}
									
								}
							}
						}
					}					
				}
			}
		}
		return true;
	}
	
	public boolean storeEvent(SQLiteDatabase db,long freq,
			DvbNetworkMapping.TransportStream ts) {
		StringBuffer sb = new StringBuffer();
		IPanelLog.d(TAG, "storeEvent ts.si_eit_last_tid = " + ts.si_eit_last_tid
				+ ";ts.si_eit_last_sn = " + ts.si_eit_last_sn);
		for (int tableid = 0x50; tableid <= ts.si_eit_last_tid; tableid++) {
			for (int j = 0; j < (ts.si_eit_last_sn/8); j++) {
				IPanelLog.d(TAG, "tableid = "+tableid +"j*8 = " + j*8);
				SiEITEvents seit = ts.getSiEventsSchedule(tableid, j * 8);
				IPanelLog.d(TAG, " seit = " + seit);
				if(seit == null){
					break;
				}
				int program_number = seit.service_id;
				IPanelLog.d(TAG, "seit.event = "+ seit.event +";program_number = "+program_number);
				if (seit.event != null) {
					int ssize = seit.event.size();
					IPanelLog.d(TAG, "ssize = " + ssize);
					for (int k = 0; k < ssize; k++) {
						SiEITEvents.Event sss = seit.event.get(k);
						String event_name = sss.event_name;
						String event_name_en = sss.event_name_en;
						long start_time = TimerFormater.rfc3339tolong(sss.start_time);
						long duration = TimerFormater.formatDuration(sss.duration);
						long end_time = start_time + duration;
						int event_id = sss.event_id;
						String short_event_name = sss.short_event_name;
						int free_ca = sss.free_ca_mode;
						int run_status = sss.running_status;

						sb.delete(0, sb.length());
						sb.append("INSERT INTO ");
						sb.append(NetworkDatabase.Events.TABLE_NAME);
						sb.append(" (");
						sb.append(DvbNetworkDatabase.Events.FREQUENCY).append(
								",");
						sb.append(DvbNetworkDatabase.Events.PROGRAM_NUMBER)
								.append(",");
						sb.append(DvbNetworkDatabase.Events.EVENT_NAME).append(
								",");
						sb.append(DvbNetworkDatabase.Events.EVENT_NAME_EN)
								.append(",");
						sb.append(DvbNetworkDatabase.Events.START_TIME).append(
								",");
						sb.append(DvbNetworkDatabase.Events.END_TIME).append(
								",");
						sb.append(DvbNetworkDatabase.Events.DURATION).append(
								",");
						sb.append(DvbNetworkDatabase.ServiceEvents.EVENT_ID)
								.append(",");
						sb.append(DvbNetworkDatabase.ServiceEvents.IS_FREE_CA)
								.append(",");
						sb.append(
								DvbNetworkDatabase.ServiceEvents.RUNNING_STATUS)
								.append(",");
						sb.append(
								DvbNetworkDatabase.ServiceEvents.SHORT_EVENT_NAME)
								.append(" )");
						sb.append(" VALUES (");
						sb.append(freq).append(",");
						sb.append(program_number).append(",");
						sb.append("\"").append(event_name).append("\",");
						sb.append("\"").append(event_name_en).append("\",");
						sb.append(start_time).append(",");
						sb.append(end_time).append(",");
						sb.append(duration).append(",");
						sb.append(event_id).append(",");
						sb.append(free_ca).append(",");
						sb.append(run_status).append(",");
						sb.append("\"").append(short_event_name).append("\" );");
						IPanelLog.d(TAG, "sb.toString() = " + sb.toString());
						try{
							db.execSQL(sb.toString());
						}catch(Exception e){
							IPanelLog.e(TAG, "execSql err : "+e.toString());
						}
					}
				}
			}
		}

		return true;
	}
	
	public boolean dropEvents(SQLiteDatabase db, long freq, int program_num) {
		StringBuffer sb = new StringBuffer();
		sb.append("DELETE FROM ");
		sb.append(NetworkDatabase.Events.TABLE_NAME);
		sb.append(" WHERE " + NetworkDatabase.Events.FREQUENCY + " = " + freq);
		if (program_num > 0)
			sb.append(" and " + NetworkDatabase.Events.PROGRAM_NUMBER + " =" + program_num);
		sb.append(";");
		try{
			db.execSQL(sb.toString());
		}catch(Exception e){
			IPanelLog.e(TAG, "execSql err : "+e.toString());
		}
		return true;
	}

	public boolean beginBatabase(SQLiteDatabase db) {
		try {
			db.beginTransaction();
			return true;
		} catch (Exception e) {
			IPanelLog.e("", "error Exception = " + e.getMessage());
		}
		return false;
	}

	public boolean endBatabase(SQLiteDatabase db, boolean bsucc) {
		try {
			if (bsucc)
				db.setTransactionSuccessful(); // 成功才会将之前的数据真正写入数据库
		} catch (Exception e) {
			IPanelLog.e(TAG, "endBatabase failed" + e.getMessage());
		} finally {
			db.endTransaction(); // 处理完成,如果不设置setTransactionSuccessful数据将被丢弃
		}
		return true;
	}

	public int serviceType2ChannelType(int service_type) {
		int channel_type = ProgramInfo.ChannelTypeEnum.DIGITAL_TV;
		switch (service_type) {
		case 1: // SERVICE_TYPE_DIGITAL_TV
			channel_type = ProgramInfo.ChannelTypeEnum.DIGITAL_TV;
			break;
		case 2: // SERVICE_TYPE_DIGITAL_RADIO
			channel_type = ProgramInfo.ChannelTypeEnum.DIGITAL_RADIO;
			break;
		default:
			channel_type = ProgramInfo.ChannelTypeEnum.OTHER;
			break;
		}
		return channel_type;
	}

	public String getStreamTypeName(int stream_type) {
		IPanelLog.i(TAG, "----------------k4 type="+stream_type);
		String typeName = ProgramInfo.getMpegAVStreamTypeName(stream_type);
		IPanelLog.i(TAG, "----------------k4 typeName="+typeName);
		if (typeName != null)
			return typeName;
		
		switch (stream_type) {
		case 0x02:
			typeName = "video_mpeg2";
			break;
		case 0x04:
			typeName = "audio_mpeg2";
			break;
		// video
		case 0x1B:
			typeName = ProgramInfo.StreamTypeNameEnum.VIDEO_H264;
			break;
		case 0x42:
			typeName = "video_avs";
			break;
		case 0x10:
			typeName = "video_mpeg4";
			break;
		case 0xEA:
			typeName = "video_vc1";
			break;

		// audio
		case 0x81:
			typeName = ProgramInfo.StreamTypeNameEnum.AUDIO_AC3;
			break;
		case 0x11:
			typeName = ProgramInfo.StreamTypeNameEnum.AUDIO_AC3_PLUS;
			break;
		case 0x0F:
		case 0x80:
			typeName = ProgramInfo.StreamTypeNameEnum.AUDIO_AAC;
			break;
		case 0x82:
		case 0x86:
			typeName = ProgramInfo.StreamTypeNameEnum.AUDIO_DTS;
			break;
		case 0x83:
			typeName = "audio_dolby";
			break;

		// pcr
		case 0xA0:
			typeName = ProgramInfo.StreamTypeNameEnum.PCR;
			break;
		default:
			typeName = "unknown";
			break;
		}
		return typeName;
	}

	public long formatDuration(long duration) {
		int flag = 1;
		long j = 0;
		String s = Long.toHexString(duration);
		int i = s.length();
		while (i > 0) {
			if (flag > 3600) {
				break;
			}
			String s1 = s.substring(i - 2 > 0 ? i - 2 : 0, i);
			j = j + Integer.parseInt(s1) * flag;
			i = i - 2;
			flag = flag * 60;
		}
		j = j * 1000;
		return j;
	}
	
	public String createTune_Param(IPNetwork map,ipaneltv.toolkit.http.HttpObjectification.IPNetwork.TransportStream ts){
		String tune_param = "frequency://" + ts.ipFreq.freq + "?symbol_rate="
				+ ts.ipFreq.rate + "&delivery=" + map.delivery + "&modulation="
				+ ts.ipFreq.mod + "&frequency=" + ts.ipFreq.freq;
		return tune_param;
	}
	
	public String createStoreFre(long fre,int delivery_type,int dvb_tsid,int netid,int orgid,String tune_param){
		StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(DvbNetworkDatabase.Frequencies.TABLE_NAME);
		sb.append(" (");
		sb.append(DvbNetworkDatabase.Frequencies.FREQUENCY).append(",");
		sb.append(DvbNetworkDatabase.Frequencies.DEVLIVERY_TYPE)
				.append(",");
		sb.append(
				DvbNetworkDatabase.TransportStreams.TRANSPORT_STREAM_ID)
				.append(",");
		sb.append(DvbNetworkDatabase.TransportStreams.NETWORK_ID)
				.append(",");
		sb.append(
				DvbNetworkDatabase.TransportStreams.ORIGINAL_NETWORK_ID)
				.append(",");
		sb.append(DvbNetworkDatabase.Frequencies.TUNE_PARAM).append(
				" )");
		sb.append(" VALUES (");
		sb.append(fre).append(",");
		sb.append(67).append(",");
		sb.append(dvb_tsid).append(",");
		sb.append(netid).append(",");
		sb.append(orgid).append(",");
		sb.append("\"").append(tune_param).append("\" );");
		return sb.toString();
		
	}
	protected void fillFreqMap(SQLiteDatabase db, SparseArray<Long> freqMap) {
		
	}
}
