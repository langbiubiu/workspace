package com.ipanel.join.protocol.sihua.cqvod;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

import com.ipanel.join.protocol.sihua.cqvod.ImageFiles;

//��Ŀ�ϵĵ�Ӱ����
@Root(name="content",strict=false)
public class VodFilm
{
	@Element(required = false)
	String vodId;
	@Element(required = false)
	String vodName;
	@Element(required = false)
	String picPath;
	@Element(required = false)
	String playType;
	
	/**˼���ӿ�*/
	@Element
	private String  code;//���ݴ���
	@Element
	private String name;//��������
	@Element
	private String type; //����������ö��
	@Element (name="sort-index")
	private String sortIndex;//������ţ�������Ҫ��ҳ��չʾʱ���Դ����Ϊ��������
	@Element
	private String url;//���ݷ��ʵ�ַ
	@Element
	private String thumbnail;//����ͼ��ַ������ʲ�û�й���ͼƬ�ʲ���ȡϵͳ���õ�Ĭ������ͼ
	@Element(name="image-files",required=false)
	private ImageFiles imageFiles;

	public String getVodId() {
		return vodId;
	}
	public void setVodId(String vodId) {
		this.vodId = vodId;
	}
	public String getVodName() {
		return vodName;
	}
	public void setVodName(String vodName) {
		this.vodName = vodName;
	}
	public String getPicPath() {
		return picPath;
	}
	public void setPicPath(String picPath) {
		this.picPath = picPath;
	}
	public String getPlayType() {
		return playType;
	}
	public void setPlayType(String playType) {
		this.playType = playType;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getSortIndex() {
		return sortIndex;
	}
	public void setSortIndex(String sortIndex) {
		this.sortIndex = sortIndex;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getThumbnail() {
		return thumbnail;
	}
	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}
	public ImageFiles getImageFiles() {
		return imageFiles;
	}
	public void setImageFiles(ImageFiles imageFiles) {
		this.imageFiles = imageFiles;
	}
	@Override
	public String toString() {
		return "VodFilm [vodId=" + vodId + ", vodName=" + vodName + ", picPath=" + picPath + ", playType=" + playType
				+ ", code=" + code + ", name=" + name + ", type=" + type + ", sortIndex=" + sortIndex + ", url=" + url
				+ ", thumbnail=" + thumbnail + ", imageFiles=" + imageFiles + "]";
	}
	
}
