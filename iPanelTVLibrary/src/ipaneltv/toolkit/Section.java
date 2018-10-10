package ipaneltv.toolkit;

/**
 * �����ݶ���
 * <p>
 * ���������̲߳���ȫ��
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
	 * �������
	 * 
	 * @param buf
	 *            ������
	 */
	public Section(SectionBuffer buf) {
		this.buf = buf;
	}

	/**
	 * �õ��������������õĻ���������
	 * 
	 * @return ����
	 */
	public SectionBuffer getSectionBuffer() {
		return buf;
	}

	/**
	 * ��ն�λ������
	 */
	public void clearLocator() {
		int n = sb.length();
		if (n > 0)
			sb.delete(0, n);
	}

	/**
	 * �õ���λ���ַ���
	 * 
	 * @return ֵ
	 */
	public String getLocator() {
		return sb.toString();
	}

	/**
	 * ��λ��������ַ���
	 * 
	 * @param s
	 *            ֵ
	 */
	public void appendToLocator(String s) {
		sb.append(s);
	}

	/**
	 * ��λ�����������
	 * 
	 * @param v
	 *            ֵ
	 */
	public void appendToLocator(int v) {
		sb.append(v);
	}

	/**
	 * ��λ��������ַ�
	 * 
	 * @param ch
	 *            ֵ
	 */
	public void appendToLocator(char ch) {
		sb.append(ch);
	}

	/**
	 * ��ID
	 * 
	 * @return ֵ
	 */
	public int table_id() {
		return buf.getIntByName("Section.table_id");
	}

	/**
	 * ͬ����ʶ
	 * 
	 * @return ֵ
	 */
	public int section_syntax_indicator() {
		return buf.getIntByName("Section.section_syntax_indicator");
	}

	/**
	 * �����ݳ���
	 * 
	 * @return ֵ
	 */
	public int section_length() {
		return buf.getIntByName("Section.section_length");
	}

	/**
	 * �汾��
	 * 
	 * @return ֵ
	 */
	public int version_number() {
		return buf.getIntByName("Section.version_number");
	}

	/**
	 * �α��
	 * 
	 * @return ֵ
	 */
	public int section_number() {
		if (sn >= 0)
			return sn;
		return (sn = buf.getIntByName("Section.section_number"));
	}

	/**
	 * ��ǰ��һ����ʶ
	 * 
	 * @return ֵ
	 */
	public int current_next_indicator() {
		return buf.getIntByName("Section.current_next_indicator");
	}

	/**
	 * ���α��
	 * 
	 * @return ֵ
	 */
	public int last_section_number() {
		if (lsn >= 0)
			return lsn;
		return (lsn = buf.getIntByName("Section.last_section_number"));
	}

	/**
	 * CRC32����ֵ
	 * 
	 * @return ֵ
	 */
	public int crc_32() {
		if (crc32 != 0)
			return crc32;
		return (crc32 = buf.getIntByName("Section.crc_32"));
	}

	/**
	 * ʹ�ö�λ���õ�����ֵ
	 * 
	 * @param locator
	 *            ��λ��,���Ϊnull,��ʹ���ڲ���λ��
	 * @return ֵ
	 */
	public int getIntValue(String locator) {
		return buf.getIntByName(locator == null ? sb.toString() : locator);
	}

	/**
	 * ʹ�ö�λ���õ��ı�ֵ
	 * 
	 * @param locator
	 *            ��λ��,���Ϊnull,��ʹ���ڲ���λ��
	 * @return ֵ
	 */
	public String getTextValue(String locator) {
		return getTextValue(locator, null);
	}

	/**
	 * ʹ�ö�λ���õ��ı�ֵ
	 * 
	 * @param locator
	 *            ��λ��,���Ϊnull,��ʹ���ڲ���λ��
	 * @param encoding
	 *            ��������
	 * @return ֵ
	 */
	public String getTextValue(String locator, String encoding) {
		return buf.getTextByName(locator == null ? sb.toString() : locator, encoding);
	}

	/**
	 * ʹ�ö�λ���õ�������
	 * 
	 * @param locator
	 *            ��λ��,���Ϊnull,��ʹ���ڲ���λ��
	 * @return ֵ
	 */
	public long getLongValue(String locator) {
		return buf.getLongByName(locator == null ? sb.toString() : locator);
	}

	/**
	 * ʹ�ö�λ���õ�����
	 * 
	 * @param locator
	 *            ��λ��,���Ϊnull,��ʹ���ڲ���λ��
	 * @return ֵ RFC 3339
	 */
	public String getDateValue(String locator) {
		return buf.getDateByName(locator == null ? sb.toString() : locator);
	}
	
	/**
	 * ʹ�ö�λ���õ��ֽ�����
	 * 
	 * @param locator
	 *            ��λ��,���Ϊnull,��ʹ���ڲ���λ��
	 * @return ֵ,������
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
