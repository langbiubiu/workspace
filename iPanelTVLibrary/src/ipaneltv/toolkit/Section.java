package ipaneltv.toolkit;

/**
 * 段数据对象
 * <p>
 * 本对象是线程不安全的
 */
public final class Section {
	private StringBuffer sb = new StringBuffer();
	private SectionBuffer buf;
	private int sn = -1, lsn = -1, crc32 = 0;
	
	public void reset(){
		sn = lsn = -1;
		crc32 = 0;
	}
	/**
	 * 构造对象
	 * 
	 * @param buf
	 *            缓冲区
	 */
	public Section(SectionBuffer buf) {
		this.buf = buf;
	}

	/**
	 * 得到构件对象是所用的缓冲区对象
	 * 
	 * @return 对象
	 */
	public SectionBuffer getSectionBuffer() {
		return buf;
	}

	/**
	 * 清空定位器内容
	 */
	public void clearLocator() {
		int n = sb.length();
		if (n > 0)
			sb.delete(0, n);
	}

	/**
	 * 得到定位器字符串
	 * 
	 * @return 值
	 */
	public String getLocator() {
		return sb.toString();
	}

	/**
	 * 向定位其中添加字符串
	 * 
	 * @param s
	 *            值
	 */
	public void appendToLocator(String s) {
		sb.append(s);
	}

	/**
	 * 向定位其中添加整数
	 * 
	 * @param v
	 *            值
	 */
	public void appendToLocator(int v) {
		sb.append(v);
	}

	/**
	 * 向定位其中添加字符
	 * 
	 * @param ch
	 *            值
	 */
	public void appendToLocator(char ch) {
		sb.append(ch);
	}

	/**
	 * 表ID
	 * 
	 * @return 值
	 */
	public int table_id() {
		return buf.getIntByName("Section.table_id");
	}

	/**
	 * 同步标识
	 * 
	 * @return 值
	 */
	public int section_syntax_indicator() {
		return buf.getIntByName("Section.section_syntax_indicator");
	}

	/**
	 * 段数据长度
	 * 
	 * @return 值
	 */
	public int section_length() {
		return buf.getIntByName("Section.section_length");
	}

	/**
	 * 版本号
	 * 
	 * @return 值
	 */
	public int version_number() {
		return buf.getIntByName("Section.version_number");
	}

	/**
	 * 段编号
	 * 
	 * @return 值
	 */
	public int section_number() {
		if (sn >= 0)
			return sn;
		return (sn = buf.getIntByName("Section.section_number"));
	}

	/**
	 * 当前下一个标识
	 * 
	 * @return 值
	 */
	public int current_next_indicator() {
		return buf.getIntByName("Section.current_next_indicator");
	}

	/**
	 * 最后段编号
	 * 
	 * @return 值
	 */
	public int last_section_number() {
		if (lsn >= 0)
			return lsn;
		return (lsn = buf.getIntByName("Section.last_section_number"));
	}

	/**
	 * CRC32检验值
	 * 
	 * @return 值
	 */
	public int crc_32() {
		if (crc32 != 0)
			return crc32;
		return (crc32 = buf.getIntByName("Section.crc_32"));
	}

	/**
	 * 使用定位器得到整数值
	 * 
	 * @param locator
	 *            定位器,如果为null,则使用内部定位器
	 * @return 值
	 */
	public int getIntValue(String locator) {
		return buf.getIntByName(locator == null ? sb.toString() : locator);
	}

	/**
	 * 使用定位器得到文本值
	 * 
	 * @param locator
	 *            定位器,如果为null,则使用内部定位器
	 * @return 值
	 */
	public String getTextValue(String locator) {
		return getTextValue(locator, null);
	}

	/**
	 * 使用定位器得到文本值
	 * 
	 * @param locator
	 *            定位器,如果为null,则使用内部定位器
	 * @param encoding
	 *            编码类型
	 * @return 值
	 */
	public String getTextValue(String locator, String encoding) {
		return buf.getTextByName(locator == null ? sb.toString() : locator, encoding);
	}

	/**
	 * 使用定位器得到长整型
	 * 
	 * @param locator
	 *            定位器,如果为null,则使用内部定位器
	 * @return 值
	 */
	public long getLongValue(String locator) {
		return buf.getLongByName(locator == null ? sb.toString() : locator);
	}

	/**
	 * 使用定位器得到日期
	 * 
	 * @param locator
	 *            定位器,如果为null,则使用内部定位器
	 * @return 值 RFC 3339
	 */
	public String getDateValue(String locator) {
		return buf.getDateByName(locator == null ? sb.toString() : locator);
	}
	
	/**
	 * 使用定位器得到字节序列
	 * 
	 * @param locator
	 *            定位器,如果为null,则使用内部定位器
	 * @return 值,毫秒数
	 */
	public byte[] getBlobValue(String locator) {
		return buf.getBlobValue(locator == null ? sb.toString() : locator);
	}
	
	/* @hide */
	public static void checkIndex(int i) {
		if (i < 0 || i >= 65536)
			throw new IndexOutOfBoundsException("invalid index[" + i + "]");
	}
}
