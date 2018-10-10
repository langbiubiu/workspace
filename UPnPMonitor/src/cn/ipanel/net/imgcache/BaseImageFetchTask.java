package cn.ipanel.net.imgcache;

public class BaseImageFetchTask implements ImageFetchTask {
	private String url;
	
	private String key;
	
	private String keyPrefix;

	private ImageFetchListener listener;

	public BaseImageFetchTask(String url) {
		this.url = url;
		this.key = url;
	}
	
	public BaseImageFetchTask(String url, String key, String keyPrefix){
		this(url);
		this.keyPrefix = keyPrefix;
	}
	

	public BaseImageFetchTask setListener(ImageFetchListener l) {
		this.listener = l;
		return this;
	}

	@Override
	public String getImageUrl() {
		return url;
	}

	@Override
	public String getStoreKey() {
		if(keyPrefix != null)
			return keyPrefix + key;
		return key;
	}

	@Override
	public ImageFetchListener getListener() {
		return listener;
	}
}
