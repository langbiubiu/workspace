package com.ipanel.join.cq.vod.jsondata;

import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
@Root(name="folder")
public class VodMenu {
	@Element(required=false)
	private String typeId;
	@Element(required=false)
	private String typeName;
	@Element(required=false)
	private String isSubType;//是否有子菜单，0无，1有
	
	//思华接口
	@Element
	private String code;//栏目代码
	@Element
	private String name ;//栏目名称
	@Element(name="site-code")
	private String site_code;//站点代码
	@Element
	private String type;//栏目类型
	@Element(name="parent-folder-code")
	private String parent_folder_code;//该栏目的父级栏目代码，如果无父级 则为""
	@Element
	private String url;//栏目访问地址
	@Element(name="with-content")
	private String with_content;//栏目下是否编排内容标志，true为有，false为无
	@Element(name="icon-url",required=false)
	private String icon_url;//栏目图标的访问地址
	@Element(name="sort-index")
	private String sort_index;//栏目序号
	@Element(required=false)
	private String description;//栏目的描述信息
	
	
	
	public String getTypeId() {
		return typeId;
	}
	public void setTypeId(String typeId) {
		this.typeId = typeId;
	}
	public String getTypeName() {
		return typeName;
	}
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}
	public String getIsSubType() {
		return isSubType;
	}
	public void setIsSubType(String isSubType) {
		this.isSubType = isSubType;
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
	public String getSite_code() {
		return site_code;
	}
	public void setSite_code(String site_code) {
		this.site_code = site_code;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getParent_folder_code() {
		return parent_folder_code;
	}
	public void setParent_folder_code(String parent_folder_code) {
		this.parent_folder_code = parent_folder_code;
	}
	public String getWith_content() {
		return with_content;
	}
	public void setWith_content(String with_content) {
		this.with_content = with_content;
	}
	public String getIcon_url() {
		return icon_url;
	}
	public void setIcon_url(String icon_url) {
		this.icon_url = icon_url;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getSort_index() {
		return sort_index;
	}
	public void setSort_index(String sort_index) {
		this.sort_index = sort_index;
	}
	@Override
	public String toString() {
		return "VodMenu [typeId=" + typeId + ", typeName=" + typeName + ", isSubType=" + isSubType + ", code=" + code
				+ ", name=" + name + ", site_code=" + site_code + ", type=" + type + ", parent_folder_code="
				+ parent_folder_code + ", url=" + url + ", with_content=" + with_content + ", icon_url=" + icon_url
				+ ", sort_index=" + sort_index + ", description=" + description + "]";
	}
	

}
