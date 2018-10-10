package ipaneltv.toolkit.dvb;

public class DataCarouselFile {

	int moduleId, moduleVersion, moduleSize;
	int blockSize;
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

	public int getBlockSize() {
		return blockSize;
	}

	public void setBlockSize(int blockSize) {
		this.blockSize = blockSize;
	}

	public String getModuleName() {
		return moduleName;
	}

	public void setModuleName(String moduleName) {
		this.moduleName = moduleName;
	}
}