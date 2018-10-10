package ipaneltv.toolkit.db;

import ipaneltv.toolkit.db.DatabaseObjectification.Channel;
import ipaneltv.toolkit.db.DatabaseObjectification.Frequency;
import ipaneltv.toolkit.db.DatabaseObjectification.Program;
import ipaneltv.toolkit.db.DatabaseObjectification.Stream;
import ipaneltv.toolkit.db.DvbDatabaseObjectification.Bouquet;
import ipaneltv.toolkit.db.DvbDatabaseObjectification.ElementaryStream;
import ipaneltv.toolkit.db.DvbDatabaseObjectification.Network;
import ipaneltv.toolkit.db.DvbDatabaseObjectification.ProgramEvent;
import ipaneltv.toolkit.db.DvbDatabaseObjectification.Service;
import ipaneltv.toolkit.db.DvbDatabaseObjectification.TransportStream;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.dvb.DvbNetworkDatabase.Bouquets;
import android.net.telecast.dvb.DvbNetworkDatabase.ElementaryStreams;
import android.net.telecast.dvb.DvbNetworkDatabase.Networks;
import android.net.telecast.dvb.DvbNetworkDatabase.ServiceEvents;
import android.net.telecast.dvb.DvbNetworkDatabase.Services;
import android.net.telecast.dvb.DvbNetworkDatabase.TransportStreams;
import android.os.Handler;
import android.util.SparseArray;

public class DvbDatabaseCursorHandler extends DatabaseCursorHandler {

	public static class BouquetCursorHandler extends CursorHandlerBase {
		protected int i_id = -1;
		protected int v_id;
		protected int i_name = -1;
		protected String v_name;
		protected int i_short_name = -1;
		protected String v_short_name;
		protected int i_tsid = -1;
		protected int v_tsid;
		protected int i_service_id = -1;
		protected int v_service_id;
		protected SparseArray<Bouquet> bqs = new SparseArray<Bouquet>();

		public BouquetCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorNotFound() {
		}

		@Override
		public void onCursorStart(Cursor c) {
			i_name = c.getColumnIndex(Bouquets.BOUQUET_NAME);
			i_id = c.getColumnIndex(Bouquets.BOUQUET_ID);
			i_short_name = c.getColumnIndex(Bouquets.SHORT_BOUQUET_NAME);
			i_tsid = c.getColumnIndex(Bouquets.TS_ID);
			i_service_id = c.getColumnIndex(Bouquets.SERVICE_ID);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_id >= 0)
				v_id = c.getInt(i_id);
			if (i_name >= 0)
				v_name = c.getString(i_name);
			if (i_short_name >= 0)
				v_short_name = c.getString(i_short_name);
			if (i_tsid >= 0)
				v_tsid = c.getInt(i_tsid);
			if (i_service_id >= 0)
				v_service_id = c.getInt(i_service_id);
		}

		protected Bouquet appendRecord() {
			Bouquet b = createBouquet();
			b.id = v_id;
			b.name = v_name;
			b.serviceId = v_service_id;
			b.shortName = v_short_name;
			b.transportStreamId = v_tsid;
			bqs.put(v_id, b);
			return b;
		}

		protected Bouquet createBouquet() {
			return new Bouquet();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}
	}

	public static class NetworkCursorHandler extends CursorHandlerBase {
		protected int i_id = -1;
		protected int v_id;
		protected int i_name = -1;
		protected String v_name;
		protected int i_short_name = -1;
		protected String v_short_name;
		protected SparseArray<Network> nws = new SparseArray<Network>();

		public NetworkCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorNotFound() {
		}

		@Override
		public void onCursorStart(Cursor c) {
			i_name = c.getColumnIndex(Networks.NETWORK_NAME);
			i_id = c.getColumnIndex(Networks.NETWORK_ID);
			i_short_name = c.getColumnIndex(Networks.SHORT_NETWORK_NAME);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_id >= 0)
				v_id = c.getInt(i_id);
			if (i_name >= 0)
				v_name = c.getString(i_name);
			if (i_short_name >= 0)
				v_short_name = c.getString(i_short_name);
		}

		protected Network appendRecord() {
			Network b = createBouquet();
			b.id = v_id;
			b.name = v_name;
			b.shortName = v_short_name;
			nws.put(v_id, b);
			return b;
		}

		protected Network createBouquet() {
			return new Network();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}
	}

	public static class TransportStreamCursorHandler extends FrequencyCursorHandler {
		protected int i_tsid = -1;
		protected int v_tsid;
		protected int i_nid = -1;
		protected int v_nid;
		protected int i_onid = -1;
		protected int v_onid;

		public TransportStreamCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
			i_tsid = c.getColumnIndex(TransportStreams.TRANSPORT_STREAM_ID);
			i_nid = c.getColumnIndex(TransportStreams.NETWORK_ID);
			i_onid = c.getColumnIndex(TransportStreams.ORIGINAL_NETWORK_ID);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			if (i_tsid >= 0)
				v_tsid = c.getInt(i_tsid);
			if (i_nid >= 0)
				v_nid = c.getInt(i_nid);
			if (i_onid >= 0)
				v_onid = c.getInt(i_onid);
		}

		@Override
		protected Frequency createFrequency() {
			return new TransportStream();
		}

		@Override
		protected Frequency appendRecord() {
			TransportStream ts = (TransportStream) super.appendRecord();
			ts.transportStreamId = v_tsid;
			ts.networkId = v_nid;
			ts.originalNetworkId = v_onid;
			return ts;
		}
	}

	public static class ServiceCursorHandler extends ChannelCursorHandler {
		protected int i_service_type = -1;
		protected int v_service_type;
		protected int i_eit_pf_flag = -1;
		protected int v_eit_pf_flag;
		protected int i_eit_schedule_flag = -1;
		protected int v_eit_schedule_flag;
		protected int i_is_ca_free = -1;
		protected int v_is_ca_free;
		protected int i_short_service_name = -1;
		protected String v_short_service_name;
		protected int i_short_provider_name = -1;
		protected String v_short_provider_name;

		public ServiceCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
			i_service_type = c.getColumnIndex(Services.SERVICE_TYPE);
			i_eit_pf_flag = c.getColumnIndex(Services.SERVICE_TYPE);
			i_eit_schedule_flag = c.getColumnIndex(Services.SERVICE_TYPE);
			i_is_ca_free = c.getColumnIndex(Services.SERVICE_TYPE);
			i_short_service_name = c.getColumnIndex(Services.SERVICE_TYPE);
			i_short_provider_name = c.getColumnIndex(Services.SERVICE_TYPE);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			if (i_service_type >= 0)
				v_service_type = c.getInt(i_service_type);
			if (i_eit_pf_flag >= 0)
				v_eit_pf_flag = c.getInt(i_eit_pf_flag);
			if (i_eit_schedule_flag >= 0)
				v_eit_schedule_flag = c.getInt(i_eit_schedule_flag);
			if (i_is_ca_free >= 0)
				v_is_ca_free = c.getInt(i_is_ca_free);
			if (i_short_service_name >= 0)
				v_short_service_name = c.getString(i_short_service_name);
			if (i_short_provider_name >= 0)
				v_short_provider_name = c.getString(i_short_provider_name);
		}

		@Override
		protected Channel createChannel() {
			return new Service();
		}

		@Override
		protected Channel appendRecord() {
			Service s = (Service) super.appendRecord();
			s.eitPfFlag = v_eit_pf_flag;
			s.eitScheduleFlag = v_eit_schedule_flag;
			s.isCaFree = v_is_ca_free;
			s.serviceType = v_service_type;
			s.shortProvider_name = v_short_provider_name;
			s.shortServiceName = v_short_service_name;
			return s;
		}
	}

	public static class ServiceEventCursorHandler extends EventCursorHandler {
		protected int i_event_id = -1;
		protected int v_event_id;
		protected int i_is_ca_free = -1;
		protected int v_is_ca_free;
		protected int i_short_event_name = -1;
		protected String v_short_event_name;
		protected int i_running_status = -1;
		protected int v_running_status;

		public ServiceEventCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
			i_event_id = c.getColumnIndex(ServiceEvents.EVENT_ID);
			i_is_ca_free = c.getColumnIndex(ServiceEvents.IS_FREE_CA);
			i_short_event_name = c.getColumnIndex(ServiceEvents.SHORT_EVENT_NAME);
			i_running_status = c.getColumnIndex(ServiceEvents.RUNNING_STATUS);

		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			if (i_event_id >= 0)
				v_event_id = c.getInt(i_event_id);
			if (i_is_ca_free >= 0)
				v_is_ca_free = c.getInt(i_is_ca_free);
			if (i_short_event_name >= 0)
				v_short_event_name = c.getString(i_short_event_name);
			if (i_running_status >= 0)
				v_running_status = c.getInt(i_running_status);
		}

		@Override
		protected Program createProgram() {
			return new ProgramEvent();
		}

		@Override
		protected Program appendRecord() {
			ProgramEvent e = (ProgramEvent) super.appendRecord();
			e.eventId = v_event_id;
			e.isCaFree = v_is_ca_free;
			e.runningStatus = v_running_status;
			e.shortEventName = v_short_event_name;
			return e;
		}
	}

	public static class ElementaryStreamCursorHandler extends StreamCursorHandler {
		protected int i_component_tag = -1;
		protected int v_component_tag;

		public ElementaryStreamCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorStart(Cursor c) {
			super.onCursorStart(c);
			i_component_tag = c.getColumnIndex(ElementaryStreams.COMPONENT_TAG);
		}

		@Override
		public void onRecordFound(Cursor c) {
			super.onRecordFound(c);
			if (i_component_tag >= 0)
				v_component_tag = c.getInt(i_component_tag);
		}

		@Override
		protected Stream createStream() {
			return new ElementaryStream();

		}

		@Override
		protected Stream appendRecord() {
			ElementaryStream es = (ElementaryStream) super.appendRecord();
			es.componentTag = v_component_tag;
			return es;
		}
	}

}
