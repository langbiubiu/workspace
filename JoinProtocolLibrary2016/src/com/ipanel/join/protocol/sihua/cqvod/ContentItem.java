package com.ipanel.join.protocol.sihua.cqvod;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="content-item",strict=false)
public class ContentItem {

	@Attribute
	private String index;// �����統ǰ��������һ��ʱ��ֵΪ1
	@Attribute
	private String duration;// ������ʱ������λΪ��
	@Element
	private String code;//�����絥�������ݴ���
	@Element(name="media-files",required=false)
	private MediaFiles mediaFiles;
	@Element(name="image-files",required=false)
	private ImageFiles imageFiles;
	
	public String getIndex() {
		return index;
	}
	public void setIndex(String index) {
		this.index = index;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public MediaFiles getMediaFiles() {
		return mediaFiles;
	}
	public void setMediaFiles(MediaFiles mediaFiles) {
		this.mediaFiles = mediaFiles;
	}
	public ImageFiles getImageFiles() {
		return imageFiles;
	}
	public void setImageFiles(ImageFiles imageFiles) {
		this.imageFiles = imageFiles;
	}
	@Override
	public String toString() {
		return "ContentItem [index=" + index + ", duration=" + duration + ", code=" + code + ", mediaFiles="
				+ mediaFiles + ", imageFiles=" + imageFiles + "]";
	}
	
	
	
	
	
}
