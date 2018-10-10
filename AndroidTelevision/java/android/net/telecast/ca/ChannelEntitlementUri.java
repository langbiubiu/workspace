package android.net.telecast.ca;

import android.net.Uri;
import android.net.telecast.ca.EntitlementDatabase.ProductUriSchema;

/**
 * 频道授权Uri信息
 */
public class ChannelEntitlementUri {
	private long freq = 0;
	private int program = 0;

	/**
	 * 得到Uri字符串
	 * 
	 * @param freq
	 *            频率
	 * @param program
	 *            节目
	 * @return 字符串
	 */
	public final static String createUriString(long freq, int program) {
		return ProductUriSchema.CHANNEL + freq + "-" + program;
	}

	/**
	 * 设置为当前字符串URi所指的内容
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
	 * 设置节目
	 * 
	 * @param program
	 *            节目号(program number/ service id)
	 */
	public void setProgram(int program) {
		this.program = program;
	}

	/**
	 * 设置频率
	 * 
	 * @param freq
	 */
	public void setFrequency(long freq) {
		this.freq = freq;
	}

	/**
	 * 得到节目号
	 * 
	 * @return 值
	 */
	public int getProgram() {
		return program;
	}

	/**
	 * 得到频率
	 * 
	 * @return 值
	 */
	public long getFrequency() {
		return freq;
	}

	@Override
	public String toString() {
		return createUriString(freq, program);
	}

	/**
	 * 得到对应用的Uri
	 * 
	 * @return 对象
	 */
	public Uri getUri() {
		return Uri.parse(toString());
	}
}
