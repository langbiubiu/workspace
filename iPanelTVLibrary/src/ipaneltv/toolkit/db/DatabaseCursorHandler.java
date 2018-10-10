package ipaneltv.toolkit.db;

import ipaneltv.toolkit.IPanelLog;
import ipaneltv.toolkit.db.DatabaseObjectification.Channel;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Ecm;
import ipaneltv.toolkit.db.DatabaseObjectification.Frequency;
import ipaneltv.toolkit.db.DatabaseObjectification.Group;
import ipaneltv.toolkit.db.DatabaseObjectification.Guide;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.db.DatabaseObjectification.Stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.NetworkDatabase.Channels;
import android.net.telecast.NetworkDatabase.Ecms;
import android.net.telecast.NetworkDatabase.Events;
import android.net.telecast.NetworkDatabase.Frequencies;
import android.net.telecast.NetworkDatabase.Groups;
import android.net.telecast.NetworkDatabase.Guides;
import android.net.telecast.NetworkDatabase.Streams;
import android.net.telecast.dvb.DvbNetworkDatabase.Services;
import android.net.telecast.dvb.DvbNetworkDatabase.TransportStreams;
import android.os.Handler;
import android.util.SparseArray;

public class DatabaseCursorHandler {

	public static class GroupCursorHandler extends CursorHandlerBase {
		protected int i_id = -1;
		protected int v_id = 0;
		protected int i_freq = -1;
		protected long v_freq = 0;
		protected int i_program = -1;
		protected int v_program = 0;
		protected int v_userid = 0;
		protected int i_name = -1;
		protected String v_name = null;
		protected SparseArray<Group> groups = new SparseArray<Group>();

		public GroupCursorHandler(Context context, Uri uri, String projection[], String selection,
				String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorNotFound() {
		}

		@Override
		public void onCursorStart(Cursor c) {
			groups.clear();
			i_id = c.getColumnIndex(Groups.GROUP_ID);
			i_freq = c.getColumnIndex(Groups.FREQUENCY);
			i_program = c.getColumnIndex(Groups.PROGRAM_NUMBER);
			i_name = c.getColumnIndex(Groups.GROUP_NAME);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_id >= 0)
				v_id = c.getInt(i_id);
			if (i_freq >= 0)
				v_freq = c.getLong(i_freq);
			if (i_program >= 0)
				v_program = c.getInt(i_program);
			if (i_name >= 0)
				v_name = c.getString(i_name);
		}

		protected Group appendRecord() {
			Group g = groups.get(v_id);
			if (g == null) {
				g = createGroup();
				g.id = v_id;
				g.name = v_name;
				g.userid = v_userid;
				groups.put(v_id, g);
			}
			g.list.add(ChannelKey.obten(v_freq, v_program));
			return g;
		}

		protected Group createGroup() {
			return new Group();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}

	}

	public static class ChannelCursorHandler extends CursorHandlerBase {
		//
		protected int i_ts_id = -1;
		protected int v_ts_id = -1;
		//
		protected int i_channel_num = -1;
		protected int v_channel_num = -1;
		//
		protected int i_is_ca_free = -1;
		protected int v_is_ca_free = -1;
		//
		protected int i_freq = -1;
		protected long v_freq = 0;
		//
		protected int i_program = -1;
		protected int v_program = 0;
		//
		protected int i_name = -1;
		protected String v_name = null;
		//
		protected int i_provider = -1;
		protected String v_provider = null;
		//
		protected int i_type = -1;
		protected int v_type = 0;
		protected HashMap<ChannelKey, Channel> channels = new HashMap<ChannelKey, Channel>();
		private ChannelKey tempKey;

		public ChannelCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorNotFound() {
		}

		@Override
		public void onCursorStart(Cursor c) {
			channels.clear();
			i_ts_id = c.getColumnIndex(TransportStreams.TRANSPORT_STREAM_ID);
			i_channel_num = c.getColumnIndex(Channels.CHANNEL_NUMBER);
			i_freq = c.getColumnIndex(Channels.FREQUENCY);
			i_program = c.getColumnIndex(Channels.PROGRAM_NUMBER);
			i_name = c.getColumnIndex(Channels.CHANNEL_NAME);
			i_provider = c.getColumnIndex(Channels.PROVIDER_NAME);
			i_type = c.getColumnIndex(Channels.CHANNEL_TYPE);
			i_is_ca_free = c.getColumnIndex(Services.IS_FREE_CA);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_ts_id >= 0)
				v_ts_id = c.getInt(i_ts_id);
			if (i_channel_num >= 0)
				v_channel_num = c.getInt(i_channel_num);
			if (i_freq >= 0)
				v_freq = c.getLong(i_freq);
			if (i_program >= 0)
				v_program = c.getInt(i_program);
			if (i_name >= 0)
				v_name = c.getString(i_name);
			if (i_provider >= 0)
				v_provider = c.getString(i_provider);
			if (i_type >= 0)
				v_type = c.getInt(i_type);
			if (i_is_ca_free >= 0)
				v_is_ca_free = c.getInt(i_is_ca_free);
		}

		protected Channel appendRecord() {
			if (tempKey != null ? tempKey.freq != v_freq || tempKey.program != v_program : true)
				tempKey = null;
			if (tempKey == null)
				tempKey = ChannelKey.obten(v_freq, v_program);
			Channel ch = createChannel();
			ch.tsId = v_ts_id;
			ch.channemNumber = v_channel_num;
			ch.key = tempKey;
			ch.name = v_name;
			ch.type = v_type;
			ch.provider = v_provider;
			ch.is_free_ca = v_is_ca_free;
			channels.put(tempKey, ch);
			return ch;
		}

		protected Channel createChannel() {
			return new Channel();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}

	}

	public static class GuideCursorHandler extends CursorHandlerBase {
		protected int i_freq = -1;
		protected long v_freq = 0;
		protected int i_program = -1;
		protected int v_program = 0;
		protected int i_v = -1;
		protected int v_v = 0;
		protected HashMap<ChannelKey, Guide> guides = new HashMap<ChannelKey, Guide>();

		public GuideCursorHandler(Context context, Uri uri, String projection[], String selection,
				String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorNotFound() {
		}

		@Override
		public void onCursorStart(Cursor c) {
			i_freq = c.getColumnIndex(Guides.FREQUENCY);
			i_program = c.getColumnIndex(Guides.PROGRAM_NUMBER);
			i_v = c.getColumnIndex(Guides.VERSION);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_freq >= 0)
				v_freq = c.getLong(i_freq);
			if (i_program >= 0)
				v_program = c.getInt(i_program);
			if (i_v >= 0)
				v_v = c.getInt(i_v);
		}

		protected Guide appendRecord() {
			Guide g = createGuide();
			g.key = ChannelKey.obten(v_freq, v_program);
			g.version = v_v;
			guides.put(g.key, g);
			return g;
		}

		@Override
		public void onCursorEnd(Cursor c) {
			IPanelLog.i("navigation", "go in GuideCursorHandler onCursorEnd method");
		}

		protected Guide createGuide() {
			return new Guide();
		}
	}

	public static class EventCursorHandler extends CursorHandlerBase {
		protected int i_freq = -1;
		protected long v_freq = 0;
		//
		protected int i_name = -1;
		protected String v_name = null;
		//
		protected int i_program = -1;
		protected int v_program = 0;
		//
		protected int i_start = -1;
		protected long v_start = 0;
		//
		protected int i_end = -1;
		protected long v_end = 0;
		protected HashMap<ChannelKey, List<Program>> programs = new HashMap<ChannelKey, List<Program>>();

		private ChannelKey tempKey;
		private long nowTimeMillis;

		public EventCursorHandler(Context context, Uri uri, String projection[], String selection,
				String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			programs.clear();
			i_freq = c.getColumnIndex(Events.FREQUENCY);
			i_program = c.getColumnIndex(Events.PROGRAM_NUMBER);
			i_name = c.getColumnIndex(Events.EVENT_NAME);
			i_start = c.getColumnIndex(Events.START_TIME);
			i_end = c.getColumnIndex(Events.END_TIME);
			nowTimeMillis = System.currentTimeMillis();
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_freq >= 0)
				v_freq = c.getLong(i_freq);
			if (i_program >= 0)
				v_program = c.getInt(i_program);
			if (i_name >= 0)
				v_name = c.getString(i_name);
			if (i_start >= 0)
				v_start = c.getLong(i_start);
			if (i_end >= 0)
				v_end = c.getLong(i_end);

		}

		protected Program appendRecord() {
			if (tempKey != null ? tempKey.freq != v_freq || tempKey.program != v_program : true)
				tempKey = null;
			if (tempKey == null)
				tempKey = ChannelKey.obten(v_freq, v_program);
			List<Program> chp = programs.get(tempKey);
			if (chp == null) {
				programs.put(tempKey, chp = new ArrayList<Program>());
				chp.add(null);// 0
				chp.add(null);// 1
			}
			Program p = createProgram();
			p.key = tempKey;
			p.name = v_name;
			p.start = v_start;
			p.end = v_end;
			p.desc = null;
			chp.add(p);
			return p;
		}

		protected void optAppendPresentFollow(Program p) {
			if (p.end > nowTimeMillis) {// ÉÐÎ´½áÊø
				List<Program> list = programs.get(p.key);
				if (p.start <= nowTimeMillis) {// present
					list.set(0, p);
				} else {// more following
					Program p2 = list.get(1);
					if (p2 == null) {
						list.set(1, p);
					} else if (p2.start > p.start) {
						list.set(1, p);
					}
				}
			}
		}

		protected Program createProgram() {
			return new Program();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}

		@Override
		public void onCursorNotFound() {
		}
	}

	public static class FrequencyCursorHandler extends CursorHandlerBase {
		protected int i_freq = -1;
		protected long v_freq = 0;
		//
		protected int i_param = -1;
		protected String v_param = null;
		//
		protected int i_delivery = -1;
		protected int v_delivery = 0;
		//
		protected int i_version = -1;
		protected int v_version = 0;
		//
		protected int i_tsid = -1;
		protected int v_tsid = 0;
		SparseArray<Frequency> freqs = new SparseArray<Frequency>();

		public FrequencyCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			freqs.clear();
			i_freq = c.getColumnIndex(Frequencies.FREQUENCY);
			i_param = c.getColumnIndex(Frequencies.TUNE_PARAM);
			i_delivery = c.getColumnIndex(Frequencies.DEVLIVERY_TYPE);
			i_version = c.getColumnIndex(Frequencies.INFO_VERSION);
			i_tsid = c.getColumnIndex(TransportStreams.TRANSPORT_STREAM_ID);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_freq >= 0)
				v_freq = c.getLong(i_freq);
			if (i_param >= 0)
				v_param = c.getString(i_param);
			if (i_delivery >= 0)
				v_delivery = c.getInt(i_delivery);
			if (i_version >= 0)
				v_version = c.getInt(i_version);
			if (i_tsid >= 0)
				v_tsid = c.getInt(i_tsid);
		}

		protected Frequency appendRecord() {
			Frequency f = createFrequency();
			f.delivery = v_delivery;
			f.freq = v_freq;
			f.param = v_param;
			f.tsid = v_tsid;
			freqs.put(Frequency.getSparseKey(v_freq), f);
			return f;
		}

		protected Frequency createFrequency() {
			return new Frequency();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}

		@Override
		public void onCursorNotFound() {
		}
	}

	public static class StreamCursorHandler extends CursorHandlerBase {
		protected int i_pid = -1;
		protected int v_pid = 0;
		protected int i_type = -1;
		protected int v_type = 0;
		protected int i_form = -1;
		protected int v_form = 0;
		protected int i_tag = -1;
		protected int v_tag = 0;
		protected int i_freq = -1;
		protected long v_freq = 0;
		protected int i_program = -1;
		protected int v_program = 0;
		protected int i_type_name = -1;
		protected String v_type_name = null;
		protected HashMap<ChannelKey, List<Stream>> streams = new HashMap<ChannelKey, List<Stream>>();
		private ChannelKey tempKey;

		public StreamCursorHandler(Context context, Uri uri, String projection[], String selection,
				String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorNotFound() {
		}

		@Override
		public void onCursorStart(Cursor c) {
			i_freq = c.getColumnIndex(Streams.FREQUENCY);
			i_program = c.getColumnIndex(Streams.PROGRAM_NUMBER);
			i_pid = c.getColumnIndex(Streams.STREAM_PID);
			i_type = c.getColumnIndex(Streams.STREAM_TYPE);
			i_type_name = c.getColumnIndex(Streams.STREAM_TYPE_NAME);
			i_form = c.getColumnIndex(Streams.PRESENTING_FORM);
			i_tag = c.getColumnIndex(Streams.ASSOCIATION_TAG);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_program >= 0)
				v_program = c.getInt(i_program);
			if (i_freq >= 0)
				v_freq = c.getLong(i_freq);
			if (i_pid >= 0)
				v_pid = c.getInt(i_pid);
			if (i_type >= 0)
				v_type = c.getInt(i_type);
			if (i_form >= 0)
				v_form = c.getInt(i_form);
			if (i_tag >= 0)
				v_tag = c.getInt(i_tag);
			if (i_type_name >= 0)
				v_type_name = c.getString(i_type_name);
		}

		protected Stream appendRecord() {
			if (tempKey != null ? tempKey.freq != v_freq || tempKey.program != v_program : true)
				tempKey = null;
			if (tempKey == null)
				tempKey = ChannelKey.obten(v_freq, v_program);
			List<Stream> ss = streams.get(tempKey);
			if (ss == null)
				streams.put(tempKey, ss = new ArrayList<Stream>());
			Stream s = createStream();
			s.key = tempKey;
			s.pid = v_pid;
			s.componentTag = v_tag;
			s.form = v_form;
			s.typeName = v_type_name;
			ss.add(s);
			return s;
		}

		protected Stream createStream() {
			return new Stream();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}

	}

	public static class EcmCursorHandler extends CursorHandlerBase {
		protected int i_ecm_pid = -1;
		protected int v_ecm_pid = 0;
		protected int i_freq = -1;
		protected long v_freq = 0;
		protected int i_program = -1;
		protected int v_program = 0;
		protected int i_stream_pid = -1;
		protected int v_stream_pid = 0;
		protected int i_caid = -1;
		protected int v_caid = 0;
		protected HashMap<ChannelKey, List<Ecm>> ecms = new HashMap<ChannelKey, List<Ecm>>();
		private ChannelKey tempKey;

		public EcmCursorHandler(Context context, Uri uri, String projection[], String selection,
				String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorNotFound() {
		}

		@Override
		public void onCursorStart(Cursor c) {
			i_ecm_pid = c.getColumnIndex(Ecms.ECM_PID);
			i_freq = c.getColumnIndex(Ecms.FREQUENCY);
			i_program = c.getColumnIndex(Ecms.PROGRAM_NUMBER);
			i_stream_pid = c.getColumnIndex(Ecms.STREAM_PID);
			i_caid = c.getColumnIndex(Ecms.CA_SYSTEM_ID);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_ecm_pid >= 0)
				v_ecm_pid = c.getInt(i_ecm_pid);
			if (i_freq >= 0)
				v_freq = c.getLong(i_freq);
			if (i_program >= 0)
				v_program = c.getInt(i_program);
			if (i_stream_pid >= 0)
				v_stream_pid = c.getInt(i_stream_pid);
			if (i_caid >= 0)
				v_caid = c.getInt(i_caid);
		}

		protected Ecm appendRecord() {
			if (tempKey != null ? tempKey.freq != v_freq || tempKey.program != v_program : true)
				tempKey = null;
			if (tempKey == null)
				tempKey = ChannelKey.obten(v_freq, v_program);
			List<Ecm> es = ecms.get(tempKey);
			if (es == null)
				ecms.put(tempKey, es = new ArrayList<Ecm>());
			Ecm ecm = createEcm();
			ecm.casysid = v_caid;
			ecm.ecmpid = v_ecm_pid;
			ecm.key = tempKey;
			ecm.spid = v_stream_pid;
			es.add(ecm);
			return ecm;
		}

		protected Ecm createEcm() {
			return new Ecm();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}

	}
}
