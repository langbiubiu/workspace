package com.ipanel.join.protocol.sihua.cqvod;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="media-file",strict=false)
public class MediaFile {

	@Element
	private String index;// ý���ļ����кţ������ý���ļ�ʱ���ø�ֵ��ʶ��ͬ��ý���ļ������Ǹ�ֵ��ý���ļ���ʽ���ض���ϵ
	@Element
	private String type;// ý���ļ�����ö��
	@Element(name="bit-rate")
	private String bitRate;// ý���ļ�������
	@Element(name="relative-ppvids",required=false)
	private String relativeppvids;// PPVID�б����ж���ö��ŷָ���������ڱ�ʾ��PPVID�����������ϡ�
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getBitRate() {
		return bitRate;
	}
	public void setBitRate(String bitRate) {
		this.bitRate = bitRate;
	}
	public String getRelativeppvids() {
		return relativeppvids;
	}
	public void setRelativeppvids(String relativeppvids) {
		this.relativeppvids = relativeppvids;
	}
	@Override
	public String toString() {
		return "MediaFile [index=" + index + ", type=" + type + ", bitRate=" + bitRate + ", relativeppvids="
				+ relativeppvids + "]";
	}
	
}
