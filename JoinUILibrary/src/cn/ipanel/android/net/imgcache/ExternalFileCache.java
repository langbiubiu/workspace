package cn.ipanel.android.net.imgcache;

import java.io.InputStream;

public interface ExternalFileCache {
	public InputStream getInputStream(String url);
}
