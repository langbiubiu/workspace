package android.net.telecast;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

import android.util.Log;

/**
 * 频点信息对象
 * <p>
 * 从数据库构建对象参考如下<br>
 * 
 * <pre>
 * <code>
 * List&lt;FrequencyInfo&gt; getFrequencyInfos(Cursor c) {
 *   List&lt;FrequencyInfo&gt; list = new ArrayList&lt;FrequencyInfo&gt;();
 *   if (c.moveToFirst()) {
 *     int iparam = c.getColumnIndex(NetworkDatabase.Frequencies.TUNE_PARAM);
 *     do {
 *       String param = c.getString(iparam);
 *       if (param != null)
 *         list.add(FrequencyInfo.fromString(param));
 *     } while (c.moveToNext());
 *   }
 *   return list;
 * }
 * </code>
 * </pre>
 */
public final class FrequencyInfo {
	static final String TAG = "[java]FrequencyInfo";

	private long f;
	private long bufsize;
	Hashtable<String, String> params = new Hashtable<String, String>();
	/** 传输方式(CTS) */
	public static final String DELIVERY = "delivery";
	/** 参数 频率 */
	public static final String FREQUENCY = "frequency";
	/** 参数 符号率(CS) */
	public static final String SYMBOL_RATE = "symbol_rate";
	/** 参数 调制方式(CST) */
	public static final String MODULATION = "modulation";
	/** 参数 设置缓冲区大小方式(主要用于远程传输网络) */
	public static final String BUFSIZE = "buffer_size";

	/** 无效频率 */
	public static final int INVALID_FREQUENCY = 0;

	/** SCHEMA for URI */
	public static final String SCHEMA = "frequency://";
	

	/**
	 * 通过字符串构建对象
	 * <p>
	 * 参考{@link #toString()}
	 * 
	 * @param s
	 *            字符串
	 * @return 对象
	 */
	public static FrequencyInfo fromString(String s) {
		FrequencyInfo fi = new FrequencyInfo();
		if (!s.startsWith(SCHEMA))
			return null;
		int st = s.indexOf('?');
		if (st < 0)
			return null;
		fi.setParameter(FREQUENCY, s.substring(SCHEMA.length(), st));
		s = s.substring(st + 1);
		String entry[] = s.split("&");
		for (int i = 0; i < entry.length; i++) {
			String kv[] = entry[i].split("=");
			if (kv.length != 2)
				throw new IllegalArgumentException("invalid string");
			fi.setParameter(kv[0], kv[1]);
		}
		if (fi.getDeliveryType() < 0) {
			Log.w(TAG, "no delivery type!");
			return null;
		}
		return fi;
	}

	/**
	 * 转为字符串形式
	 */
	public String toString() {
		boolean first = true;
		StringBuffer sb = new StringBuffer();
		sb.append(SCHEMA);
		sb.append(f).append("?");
		for (Entry<String, String> en : params.entrySet()) {
			if (!first)
				sb.append("&");
			else
				first = false;
			sb.append(en.getKey()).append("=").append(en.getValue());
		}
		return sb.toString();
	}

	/**
	 * 得到频率
	 * 
	 * @return 值,Hz单位
	 */
	public long getFrequency() {
		return f;
	}

	/**
	 * 设置频率
	 * 
	 * @param f
	 *            值，Hz单位
	 */
	public void setFrequency(long f) {
		this.f = f;
		params.put(FREQUENCY, f + "");
	}

	/**
	 * 得到缓冲区大小
	 * 
	 * @return 值,Byte字节单位
	 */
	public long getBufSize() {
		return bufsize;
	}

	/**
	 * 设置缓冲区大小
	 * 
	 * @param bs
	 *            值，Byte字节单位
	 */
	public void setBufSize(long bs) {
		this.bufsize = bs;
		params.put(BUFSIZE, bs + "");
	}

	/**
	 * 设置参数
	 * 
	 * @param name
	 *            名称
	 * @param value
	 *            值
	 */
	public void setParameter(String name, String value) {
		if (name.equals(""))
			if (value.indexOf('=') >= 0 || name.indexOf('=') >= 0 || value.indexOf(';') >= 0
					|| name.indexOf(';') >= 0)
				throw new IllegalArgumentException("'=' is invalid of param value");
		if (FREQUENCY.equalsIgnoreCase(name)) {
			char c = value.charAt(value.length() - 1);
			switch (c) {
			case 'k':
			case 'K':
				f = Long.parseLong(value.substring(0, value.length() - 1));
				f *= 1000;
				value = f + "";
				break;
			case 'm':
			case 'M':
				f = Long.parseLong(value.substring(0, value.length() - 1));
				f *= 1000000;
				value = f + "";
				break;
			default:
				f = Long.parseLong(value);
				break;
			}
		}
		params.put(name, value);
	}

	/**
	 * 设置参数
	 * 
	 * @param name
	 *            名称
	 * @param value
	 *            值
	 */
	public void setParameter(String name, int value) {
		if (FREQUENCY.equalsIgnoreCase(name)) {
			throw new IllegalArgumentException(
					"set frequency use setFrequency(long f) ; or setParameter(String name, String value) ;");
		}
		params.put(name, "" + value);
	}

	/**
	 * 得到参数值
	 * 
	 * @param name
	 *            名称
	 * @return 值
	 */
	public String getParameter(String name) {
		return params.get(name);
	}

	FrequencyInfo() {
	}

	/**
	 * 构造指定传输类型的频率
	 * 
	 * @param type
	 *            类型
	 */
	public FrequencyInfo(int type) {
		String t;
		switch (type) {
		case NetworkInterface.DELIVERY_CABLE:
			t = "cable";
			break;
		case NetworkInterface.DELIVERY_TERRESTRIAL:
			t = "terrestrial";
			break;
		case NetworkInterface.DELIVERY_SATELLITE:
			t = "satellite";
			break;
		default:
			throw new IllegalArgumentException();
		}
		params.put(DELIVERY, t);
	}

	/**
	 * 清空参数(DELIVERY除外)
	 */
	public void clear() {
		String d = getParameter(DELIVERY);
		params.clear();
		params.put(DELIVERY, d);
	}

	/**
	 * 枚举已有参数名称
	 * 
	 * @return 对象
	 */
	public Enumeration<String> getParamtersName() {
		return params.keys();
	}

	/**
	 * 得到频点的传输类型
	 * 
	 * @return 值
	 */
	public int getDeliveryType() {
		String s = getParameter(DELIVERY);
		if ("c".equalsIgnoreCase(s) || "cable".equalsIgnoreCase(s)) {
			return NetworkInterface.DELIVERY_CABLE;
		} else if ("s".equalsIgnoreCase(s) || "satellite".equalsIgnoreCase(s)) {
			return NetworkInterface.DELIVERY_SATELLITE;
		} else if ("t".equalsIgnoreCase(s) || "terrestrial".equalsIgnoreCase(s)) {
			return NetworkInterface.DELIVERY_TERRESTRIAL;
		}
		return -1;
	}
}
