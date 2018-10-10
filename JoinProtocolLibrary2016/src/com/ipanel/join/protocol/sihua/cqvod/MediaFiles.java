package com.ipanel.join.protocol.sihua.cqvod;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;
@Root(name="media-files",strict=false)
public class MediaFiles {
	@Attribute
	private String items;// 媒体文件个数，如果无媒体文件，该值为0，且不再显示media-file标签
	@ElementList(inline=true)
	private List<MediaFile> mediaFileList;

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public List<MediaFile> getMediaFileList() {
		return mediaFileList;
	}

	public void setMediaFileList(List<MediaFile> mediaFileList) {
		this.mediaFileList = mediaFileList;
	}

	@Override
	public String toString() {
		return "MediaFiles [items=" + items + ", mediaFileList=" + mediaFileList + "]";
	}

}
