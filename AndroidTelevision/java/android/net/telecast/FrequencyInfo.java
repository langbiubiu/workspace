package android.net.telecast;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map.Entry;

import android.util.Log;

/**
 * Ƶ����Ϣ����
 * <p>
 * �����ݿ⹹������ο�����<br>
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
	/** ���䷽ʽ(CTS) */
	public static final String DELIVERY = "delivery";
	/** ���� Ƶ�� */
	public static final String FREQUENCY = "frequency";
	/** ���� ������(CS) */
	public static final String SYMBOL_RATE = "symbol_rate";
	/** ���� ���Ʒ�ʽ(CST) */
	public static final String MODULATION = "modulation";
	/** ���� ���û�������С��ʽ(��Ҫ����Զ�̴�������) */
	public static final String BUFSIZE = "buffer_size";

	/** ��ЧƵ�� */
	public static final int INVALID_FREQUENCY = 0;

	/** SCHEMA for URI */
	public static final String SCHEMA = "frequency://";
	

	/**
	 * ͨ���ַ�����������
	 * <p>
	 * �ο�{@link #toString()}
	 * 
	 * @param s
	 *            �ַ���
	 * @return ����
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
	 * תΪ�ַ�����ʽ
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
	 * �õ�Ƶ��
	 * 
	 * @return ֵ,Hz��λ
	 */
	public long getFrequency() {
		return f;
	}

	/**
	 * ����Ƶ��
	 * 
	 * @param f
	 *            ֵ��Hz��λ
	 */
	public void setFrequency(long f) {
		this.f = f;
		params.put(FREQUENCY, f + "");
	}

	/**
	 * �õ���������С
	 * 
	 * @return ֵ,Byte�ֽڵ�λ
	 */
	public long getBufSize() {
		return bufsize;
	}

	/**
	 * ���û�������С
	 * 
	 * @param bs
	 *            ֵ��Byte�ֽڵ�λ
	 */
	public void setBufSize(long bs) {
		this.bufsize = bs;
		params.put(BUFSIZE, bs + "");
	}

	/**
	 * ���ò���
	 * 
	 * @param name
	 *            ����
	 * @param value
	 *            ֵ
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
	 * ���ò���
	 * 
	 * @param name
	 *            ����
	 * @param value
	 *            ֵ
	 */
	public void setParameter(String name, int value) {
		if (FREQUENCY.equalsIgnoreCase(name)) {
			throw new IllegalArgumentException(
					"set frequency use setFrequency(long f) ; or setParameter(String name, String value) ;");
		}
		params.put(name, "" + value);
	}

	/**
	 * �õ�����ֵ
	 * 
	 * @param name
	 *            ����
	 * @return ֵ
	 */
	public String getParameter(String name) {
		return params.get(name);
	}

	FrequencyInfo() {
	}

	/**
	 * ����ָ���������͵�Ƶ��
	 * 
	 * @param type
	 *            ����
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
	 * ��ղ���(DELIVERY����)
	 */
	public void clear() {
		String d = getParameter(DELIVERY);
		params.clear();
		params.put(DELIVERY, d);
	}

	/**
	 * ö�����в�������
	 * 
	 * @return ����
	 */
	public Enumeration<String> getParamtersName() {
		return params.keys();
	}

	/**
	 * �õ�Ƶ��Ĵ�������
	 * 
	 * @return ֵ
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
