package com.ipanel.join.protocol.sihua.cqvod;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root(name="image-file",strict=false)
public class ImageFile {
	@Element
	private String index; //ͼƬ�ļ����кţ������ͼƬ�ļ�ʱ���ø�ֵ��ʶ��ͬ��ͼƬ�ļ������Ǹ�ֵ��ͼƬ�ļ���ʽ���ض���ϵ
	@Element
	private String type;//ͼƬ�ļ�����ö��
	@Element
	private String url;//ͼƬ���ʵ�ַ
	
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
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	@Override
	public String toString() {
		return "ImageFile [index=" + index + ", type=" + type + ", url=" + url + "]";
	}
	

}
