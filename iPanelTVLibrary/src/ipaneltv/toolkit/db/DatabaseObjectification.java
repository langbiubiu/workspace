package ipaneltv.toolkit.db;

import java.util.ArrayList;
import java.util.List;

import android.text.format.Time;
import android.util.Log;
import android.util.SparseArray;

public class DatabaseObjectification {
	private final static SparseArray<SparseArray<ChannelKey>> keys = new SparseArray<SparseArray<ChannelKey>>();

	public static class Objectification {
		protected Object tag;

		public Object getTag() {
			return tag;
		}

		public void setTag(Object t) {
			tag = t;
		}
	}

	public static class ChannelKey {
		protected long freq;
		protected int program;

		public static ChannelKey obten(long freq, int program) {
			synchronized (keys) {
				int fv = (int) (freq / 1000);
				SparseArray<ChannelKey> k2 = keys.get(fv);
				if (k2 == null) {
					k2 = new SparseArray<ChannelKey>();
					keys.put(fv, k2);
				}
				ChannelKey ret = k2.get(program);
				if (ret == null) {
					ret = new ChannelKey(freq, program);
					k2.put(program, ret);
				}
				return ret;
			}
		}

		public ChannelKey(long f, int p) {
			freq = f;
			program = p;
		}

		public ChannelKey(ChannelKey key) {
			freq = key.freq;
			program = key.program;
		}

		public long getFrequency() {
			return freq;
		}

		public int getProgram() {
			return program;
		}

		@Override
		public int hashCode() {
			return program;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ChannelKey) {
				return ((ChannelKey) o).program == program;
			}
			return false;
		}

		@Override
		public String toString() {
			return freq + "-" + program;
		}

		public static ChannelKey fromString(String s) {
			try {
				String[] ss = s.split("-");
				if (ss.length < 2)
					return null;
				return ChannelKey.obten(Long.parseLong(ss[0]), Integer.parseInt(ss[1]));
			} catch (Exception e) {
				Log.e("ChannelKey", "fromString error = "+ e.getMessage());
			}
			return null;
		}
	}

	/** 分组 */
	public static class Group extends Objectification {
		protected String name;
		protected int id;
		protected int userid;
		protected List<ChannelKey> list = new ArrayList<ChannelKey>();

		public String getName() {
			return name;
		}

		public int getId() {
			return id;
		}

		public int getUserid() {
			return userid;
		}

		public void setId(int id) {
			this.id = id;
		}

		public void setUserid(int userid) {
			this.userid = userid;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<ChannelKey> getChannelKeys() {
			return list;
		}
	}

	/** 频道 */
	public static class Channel extends Objectification {
		protected int tsId;
		protected int channemNumber;
		protected ChannelKey key;
		protected String name, provider;
		protected Program present, follow;
		protected int type, guide_version;
		protected int is_free_ca;

		public int getTsId() {
			return tsId;
		}

		public int getChannelNumber() {
			return channemNumber;
		}

		public boolean isFreeCa() {
			if (is_free_ca == 0) {
				return true;
			}
			return false;
		}

		public ChannelKey getChannelKey() {
			return key;
		}

		public String getName() {
			return name;
		}

		public Program getPresent() {
			return present;
		}

		public Program getFollow() {
			return follow;
		}

		public String getProvider() {
			return provider;
		}

		public int getType() {
			return type;
		}

		public int getGuideVersion() {
			return guide_version;
		}
	}

	/** 指南 */
	public static class Guide extends Objectification {
		protected ChannelKey key;
		protected int version;
		protected int isPFLoadedNum = 0;
		List<Program> list;
		List<Program> list2;
		Program[] pf;

		public ChannelKey getChannelKey() {
			return key;
		}

		public int getVersion() {
			return version;
		}
	}

	/** 节目 */
	public static class Program extends Objectification {
		protected ChannelKey key;
		protected String name;
		protected long start, end;
		protected String desc;

		public ChannelKey getChannelKey() {
			return key;
		}

		public void setChannelKey(ChannelKey key) {
			this.key = key;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public void getStartTime(Time t) {
			t.set(start);
		}

		public void getEndTime(Time t) {
			t.set(end);
		}

		public void setStart(long start) {
			this.start = start;
		}

		public long getStart() {
			return start;
		}

		public void setEnd(long end) {
			this.end = end;
		}

		public long getEnd() {
			return end;
		}

		public String getDesciption() {
			return desc;
		}

		boolean isPresent(long now) {
			return start <= now && end > now;
		}
	}

	/** 频率 */
	public static class Frequency extends Objectification {
		protected long freq;
		protected int delivery;
		protected int tsid;
		protected String param;

		public final static int getSparseKey(long f) {
			return (int) (f / 1000);
		}

		public final int getSparseKey() {
			return (int) (freq / 1000);
		}

		public long getFrequency() {
			return freq;
		}

		public int getDeliveryType() {
			return delivery;
		}

		public String getTuneParams() {
			return param;
		}
		
		public int getTsid(){
			return tsid;
		}
	}

	/** 流 */
	public static class Stream extends Objectification {
		protected ChannelKey key;
		protected int pid, type, componentTag, form;
		protected String typeName;

		public ChannelKey getChannelKey() {
			return key;
		}

		public int getStreamPId() {
			return pid;
		}

		public int getStreamType() {
			return type;
		}

		public String getStreamTypeName() {
			return typeName;
		}

		public int getcomponentTag() {
			return componentTag;
		}

		public int getPresentingForm() {
			return form;
		}
	}

	/** ECM */
	public static class Ecm extends Objectification {
		protected ChannelKey key;
		protected int spid, ecmpid, casysid, pmtpid;

		public int getPmtpid() {
			return pmtpid;
		}

		public void setPmtpid(int pmtpid) {
			this.pmtpid = pmtpid;
		}

		public ChannelKey getChannelKey() {
			return key;
		}

		public int getStreamPId() {
			return spid;
		}

		public int getEcmPId() {
			return ecmpid;
		}

		public int getCaSystemId() {
			return casysid;
		}

		public void setChannelKey(ChannelKey key) {
			this.key = key;
		}

		public void setStreamPId(int spid) {
			this.spid = spid;
		}

		public void setEcmpid(int ecmpid) {
			this.ecmpid = ecmpid;
		}


		public void setCaSystemId(int casysid) {
			this.casysid = casysid;
		}
	}
}
