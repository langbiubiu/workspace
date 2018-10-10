package ipaneltv.toolkit.entitlement;

import ipaneltv.toolkit.db.CursorHandlerBase;
import ipaneltv.toolkit.db.DatabaseObjectification.ChannelKey;
import ipaneltv.toolkit.db.DatabaseObjectification.Objectification;

import java.util.HashMap;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.telecast.NetworkDatabase;
import android.net.telecast.ca.EntitlementDatabase.Entitlements;
import android.os.Handler;

public class EntitlementDatabaseObjects {

	public static class Entitlement extends Objectification {

		int value, productId, type, moduleSn, opid;
		long start, end;
		String uri;
		ChannelKey ch;

		public Entitlement() {
		}

		public Entitlement(int productId, int ent, long start, long end) {
			value = ent;
			this.productId = productId;
			this.start = start;
			this.end = end;
		}
		public Entitlement(int productId, int ent, long start, long end,int opid) {
			value = ent;
			this.productId = productId;
			this.start = start;
			this.end = end;
			this.opid = opid;
		}

		public int getProductId() {
			return productId;
		}

		public int getValue() {
			return value;
		}

		public int getType() {
			return type;
		}

		public ChannelKey getChannelKey() {
			return ch;
		}

		public int getModuleSN() {
			return moduleSn;
		}

		public long getStart() {
			return start;
		}

		public long getEnd() {
			return end;
		}

		public int getOperatorId() {
			return opid;
		}
	}

	public static class Product extends Objectification {
		ChannelKey key;
		int productId, caSystemId;

		public ChannelKey getChannelKey() {
			return key;
		}

		public int getProductId() {
			return productId;
		}

		public int getCaSystemId() {
			return caSystemId;
		}
	}

	public static class ChannelEntitlement extends Entitlement {
		ChannelKey key;

		public synchronized ChannelKey getChannelKey() {
			if (key == null)
				key = ChannelKey.fromString(uri);
			return key;
		}
	}

	public static class EntitlementCursorHandler extends CursorHandlerBase {
		protected int i_uri = -1;
		protected String v_uri;
		protected int i_type = -1;
		protected int v_type;
		protected int i_id = -1;
		protected int v_id;
		protected int i_ent = -1;
		protected int v_ent;
		protected int i_msn = -1;
		protected int v_msn;
		protected int i_start = -1;
		protected long v_start;
		protected int i_end = -1;
		protected long v_end;
		protected int i_opid = -1;
		protected int v_opid;
		
		protected ChannelKey v_ck;

		protected HashMap<String, Entitlement> ents = new HashMap<String, Entitlement>();

		public EntitlementCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		@Override
		public void onCursorNotFound() {
		}

		@Override
		public void onCursorStart(Cursor c) {
			i_uri = c.getColumnIndex(Entitlements.PRODUCT_URI);
			i_type = c.getColumnIndex(Entitlements.PRODUCT_TYPE);
			i_id = c.getColumnIndex(Entitlements.PRODUCT_ID);
			i_ent = c.getColumnIndex(Entitlements.ENTITLEMENT);
			i_msn = c.getColumnIndex(Entitlements.MODULE_SN);
			i_start = c.getColumnIndex(Entitlements.START_TIME);
			i_end = c.getColumnIndex(Entitlements.END_TIME);
			i_opid = c.getColumnIndex(Entitlements.NETWORK_OPERATOR_ID);
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_uri >= 0){
				v_uri = c.getString(i_uri);
				v_ck = EntitlementObserver.channelUriToChannelKey(v_uri);
			}
			if (i_type >= 0)
				v_type = c.getInt(i_type);
			if (i_id >= 0)
				v_id = c.getInt(i_id);
			if (i_ent >= 0)
				v_ent = c.getInt(i_ent);
			if (i_msn >= 0)
				v_msn = c.getInt(i_msn);
			if (i_start >= 0)
				v_start = c.getLong(i_start);
			if (i_end >= 0)
				v_end = c.getLong(i_end);
			if (i_opid >= 0)
				v_opid = c.getInt(i_opid);
			
		}

		protected Entitlement appendRecord() {
			Entitlement e = createEntitlement();
			e.productId = v_id;
			e.value = v_ent;
			e.type = v_type;
			e.uri = v_uri;
			e.moduleSn = v_msn;
			e.start = v_start;
			e.end = v_end;
			e.opid = v_opid;
			e.ch = v_ck;
			ents.put(e.uri, e);
			return e;
		}

		protected Entitlement createEntitlement() {
			return new Entitlement();
		}

		@Override
		public void onCursorEnd(Cursor c) {
		}
	}

	public abstract static class ProductRecordCursorHandler extends CursorHandlerBase {
		int i_freq;
		long v_freq;
		int i_program;
		int v_program;
		int i_product;
		int v_product;
		int i_ca_system_id;
		int v_ca_system_id;
		protected HashMap<ChannelKey, Product> products = new HashMap<ChannelKey, Product>();

		public ProductRecordCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		public abstract String productColumeName();

		public abstract String frequencyColumeName();

		public abstract String programColumeName();

		public abstract String caSystemIdColumeName();

		@Override
		public void onCursorStart(Cursor c) {
			i_freq = c.getColumnIndex(frequencyColumeName());
			i_program = c.getColumnIndex(programColumeName());
			i_product = c.getColumnIndex(productColumeName());
			i_ca_system_id = c.getColumnIndex(caSystemIdColumeName());
		}

		@Override
		public void onRecordFound(Cursor c) {
			if (i_freq >= 0)
				v_freq = c.getLong(i_freq);
			if (i_program >= 0)
				v_program = c.getInt(i_program);
			if (i_product >= 0)
				v_product = c.getInt(i_product);
			if (i_ca_system_id >= 0)
				v_ca_system_id = c.getInt(i_ca_system_id);
		}

		protected Product appendRecord() {
			ChannelKey key = ChannelKey.obten(v_freq, v_program);
			Product p = products.get(key);
			if (p != null)
				return null;// ÖØ¸´µÄ
			p = createProduct();
			p.key = key;
			p.productId = v_product;
			p.caSystemId = v_ca_system_id;
			products.put(key, p);
			return p;
		}

		protected Product createProduct() {
			return new Product();
		}
	}

	public abstract static class ProductEcmCursorHandler extends ProductRecordCursorHandler {
		public ProductEcmCursorHandler(Context context, Uri uri, String projection[],
				String selection, String[] selectionArgs, String sortOrder, Handler handler) {
			super(context, uri, projection, selection, selectionArgs, sortOrder, handler);
		}

		public String frequencyColumeName() {
			return NetworkDatabase.Ecms.FREQUENCY;
		}

		public String programColumeName() {
			return NetworkDatabase.Ecms.PROGRAM_NUMBER;
		}
	}
}
