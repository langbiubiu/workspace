package ipaneltv.toolkit.dvb;

public class ObjectCarouselModule {

	int moduleId, moduleVersion, moduleSize;
	int moduleLink, moduleAssocTag;
	String moduleName;

	public int getModuleId() {
		return moduleId;
	}

	public void setModuleId(int moduleId) {
		this.moduleId = moduleId;
	}

	public int getModuleVersion() {
		return moduleVersion;
	}

	public void setModuleVersion(int moduleVersion) {
		this.moduleVersion = moduleVersion;
	}

	public int getModuleSize() {
		return moduleSize;
	}

	public void setModuleSize(int moduleSize) {
		this.moduleSize = moduleSize;
	}

	public int getModuleLink() {
		return moduleLink;
	}

	public void setModuleLink(int moduleLink) {
		this.moduleLink = moduleLink;
	}

	public int getModuleAssocTag() {
		return moduleAssocTag;
	}

	public void setModuleAssocTag(int moduleAssocTag) {
		this.moduleAssocTag = moduleAssocTag;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}

}