package ipaneltv.toolkit.dvb;

import ipaneltv.toolkit.IPanelLog;

import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class MosaicChannel {
	private static final String TAG = MosaicChannel.class.getSimpleName();
	public Rect r;
	public Vector<Mosaic> mosaics = new Vector<Mosaic>();

	public Mosaic addMosaic() {
		synchronized (mosaics) {
			Mosaic m = new Mosaic();
			mosaics.add(m);
			return m;	
		}
	}
	
	public int sizeOfMosaics() {
		synchronized (mosaics) {
			return mosaics.size();
		}
	}
	
	public Mosaic MosaicAt(int index) {
		synchronized (mosaics) {
			return mosaics.get(index);
		}
	}

	public static class Rect {
		public int x = -1, y = -1, w = -1, h = -1, widget = -1, height = -1;
	}

	public class Mosaic {
		public Rect r;
		public Linkage link;
	}

	public static class Linkage {
		public int type;
	}

	public static class ChannelLinkage extends Linkage {
		public int cell_id;
		public int tId;
		public int oId;
		public long freq;
		public int pn;
		public int audio_pid;
		public String audio_type;
	}

	public static class GroupLinkage extends Linkage {
		public int cell_id;
		public int group_id;
	}

	public static class EventLinkage extends ChannelLinkage {
		public int event_id;
	}
	
	public static MosaicChannel toMosaicChannel(String json) {
		MosaicChannel channel = new MosaicChannel();
		try {
			JSONObject jobj = new JSONObject(json);
			channel.r = new Rect();
			channel.r.x = jobj.getInt("x");
			channel.r.y = jobj.getInt("y");
			channel.r.w = jobj.getInt("w");
			channel.r.h = jobj.getInt("h");
			JSONArray array = jobj.getJSONArray("mosaics");
			for (int i = 0; i < array.length(); i++) {
				JSONObject mosaicObj = array.getJSONObject(i);
				int t = mosaicObj.getInt("type");
				IPanelLog.d(TAG, "toMosaicChannel t = " + t);
				Mosaic mosaic = channel.addMosaic();
				mosaic.r = new Rect();
				mosaic.r.x = mosaicObj.getInt("x");
				mosaic.r.y = mosaicObj.getInt("y");
				mosaic.r.w = mosaicObj.getInt("w");
				mosaic.r.h = mosaicObj.getInt("h");
				if (t == 1) {
					GroupLinkage gl = new GroupLinkage();
					gl.type = t;
					gl.cell_id = mosaicObj.getInt("cell_id");
					gl.group_id = mosaicObj.getInt("group_id");
					mosaic.link = gl;
				} else if (t == 2 || t == 3) {
					ChannelLinkage cl = new ChannelLinkage();
					cl.type = t;
					cl.freq = mosaicObj.getLong("freq");
					cl.pn = mosaicObj.getInt("pn");
					cl.audio_pid = mosaicObj.getInt("audio_pid");
					cl.audio_type = mosaicObj.getString("audio_type");
					cl.cell_id = mosaicObj.getInt("cell_id");
					mosaic.link = cl;
				} else if (t == 4) {
					EventLinkage el = new EventLinkage();
					el.type = t;
					el.freq = mosaicObj.getLong("freq");
					el.pn = mosaicObj.getInt("pn");
					el.cell_id = mosaicObj.getInt("cell_id");
					el.event_id = mosaicObj.getInt("event_id");
					mosaic.link = el;
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return channel;
	}

	@Override
	public String toString() {
		JSONObject obj = new JSONObject();
		try {
			if (r != null) {
				obj.put("x", r.x);
				obj.put("y", r.y);
				obj.put("w", r.w);
				obj.put("h", r.h);
			}
			if (mosaics != null) {
				JSONArray ja = new JSONArray();
				for (Mosaic m : mosaics) {
					JSONObject jb = new JSONObject();
					jb.put("x", m.r.x);
					jb.put("y", m.r.y);
					jb.put("w", m.r.w);
					jb.put("h", m.r.h);
					if (m.link instanceof ChannelLinkage) {
						ChannelLinkage cl = (ChannelLinkage) m.link;
						jb.put("type", cl.type);
						jb.put("freq", cl.freq);
						jb.put("pn", cl.pn);
						jb.put("audio_pid", cl.audio_pid);
						jb.put("audio_type", cl.audio_type);
						jb.put("cell_id", cl.cell_id);
					} else if (m.link instanceof GroupLinkage) {
						GroupLinkage gl = (GroupLinkage) m.link;
						jb.put("type", gl.type);
						jb.put("group_id", gl.group_id);
						jb.put("cell_id", gl.cell_id);
					} else if (m.link instanceof EventLinkage) {
						EventLinkage el = (EventLinkage) m.link;
						jb.put("type", el.type);
						jb.put("event_id", el.event_id);
						jb.put("freq", el.freq);
						jb.put("pn", el.pn);
						jb.put("cell_id", el.cell_id);
					}
					ja.put(jb);
				}
				obj.put("mosaics", ja);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return obj.toString();
	}
}
