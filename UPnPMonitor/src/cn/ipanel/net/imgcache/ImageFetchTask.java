package cn.ipanel.net.imgcache;

public interface ImageFetchTask {
	public String getImageUrl();
	public String getStoreKey();
	
	public ImageFetchListener getListener();
}
