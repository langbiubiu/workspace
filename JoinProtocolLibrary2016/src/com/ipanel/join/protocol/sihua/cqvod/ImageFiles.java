package com.ipanel.join.protocol.sihua.cqvod;

import java.util.List;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;

@Root(name="image-files",strict=false)
public class ImageFiles {
	@Attribute
	private String items;// 图片文件个数，如果无图片文件，该值为0，且不再显示image-file标签

	@ElementList(inline=true)
	List<ImageFile> imageFileList;

	public String getItems() {
		return items;
	}

	public void setItems(String items) {
		this.items = items;
	}

	public List<ImageFile> getImageFileList() {
		return imageFileList;
	}

	public void setImageFileList(List<ImageFile> imageFileList) {
		this.imageFileList = imageFileList;
	}

	@Override
	public String toString() {
		return "ImageFiles [items=" + items + ", imageFileList=" + imageFileList + "]";
	}

}
