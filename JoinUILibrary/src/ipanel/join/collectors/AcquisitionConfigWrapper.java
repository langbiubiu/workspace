package ipanel.join.collectors;

public abstract class AcquisitionConfigWrapper implements IAcquisitionConfig {

	@Override
	public long getMaxSaveFileSize() {
		// TODO Auto-generated method stub
		return 1024 * 1024 * 10;
	}

	@Override
	public long getUploadHearter() {
		// TODO Auto-generated method stub
		return 5 * 60 * 1000;
	}

	@Override
	public long getMaxUploadFileSize() {
		// TODO Auto-generated method stub
		return 1024 * 1024 * 10;
	}
	
	@Override
	public long getZipSaveDuration() {
		// TODO Auto-generated method stub
		return 3600*1000*2;
	}

	@Override
	public String getCollectorBroadcastAction() {
		// TODO Auto-generated method stub
		return "com.join.DATA_COLLECT";
	}
	@Override
	public String getUserToken() {
		// TODO Auto-generated method stub
		return "";
	}
	public String getZipPassword(){
		return "123";
	}

}
