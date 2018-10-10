package android.net.telecast.ca;

import android.net.Uri;
import android.net.telecast.ca.EntitlementDatabase.ProductUriSchema;

/**
 * Ƶ����ȨUri��Ϣ
 */
public class ChannelEntitlementUri {
	private long freq = 0;
	private int program = 0;

	/**
	 * �õ�Uri�ַ���
	 * 
	 * @param freq
	 *            Ƶ��
	 * @param program
	 *            ��Ŀ
	 * @return �ַ���
	 */
	public final static String createUriString(long freq, int program) {
		return ProductUriSchema.CHANNEL + freq + "-" + program;
	}

	/**
	 * ����Ϊ��ǰ�ַ���URi��ָ������
	 * 
	 * @param uriString
	 *            uri
	 */
	public void set(String uriString) {
		if (!uriString.startsWith(ProductUriSchema.CHANNEL))
			throw new IllegalArgumentException();
		String[] x = uriString.substring(ProductUriSchema.CHANNEL.length()).split("-");
		freq = Long.parseLong(x[0]);
		program = Integer.parseInt(x[1]);
	}

	/**
	 * ���ý�Ŀ
	 * 
	 * @param program
	 *            ��Ŀ��(program number/ service id)
	 */
	public void setProgram(int program) {
		this.program = program;
	}

	/**
	 * ����Ƶ��
	 * 
	 * @param freq
	 */
	public void setFrequency(long freq) {
		this.freq = freq;
	}

	/**
	 * �õ���Ŀ��
	 * 
	 * @return ֵ
	 */
	public int getProgram() {
		return program;
	}

	/**
	 * �õ�Ƶ��
	 * 
	 * @return ֵ
	 */
	public long getFrequency() {
		return freq;
	}

	@Override
	public String toString() {
		return createUriString(freq, program);
	}

	/**
	 * �õ���Ӧ�õ�Uri
	 * 
	 * @return ����
	 */
	public Uri getUri() {
		return Uri.parse(toString());
	}
}
