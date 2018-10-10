package android.net.telecast;

import android.net.Uri;
import android.net.telecast.INetworkServiceManagerCallback;

/**@hide*/
interface INetworkServiceManager{
	void setCallback(in INetworkServiceManagerCallback cb);
	List<String> getNetworkUUIDs();
	String getPreferredNetworkUUID();	
	boolean setPreferredNetworkByUUID(String uuid);
	boolean registerNetworkAppProcess(IBinder b);
	boolean registerNetworkCAProcess(IBinder b);
	Uri getNetworkDatabaseUri(String uuid);
	boolean bindNetworkTeeveeWidgetId(String uuid, int id, String name);
	String getNetworkProperty(String uuid, String name);	
	String getNetworkSectionbaseDir(String uuid);	
	String getNetworkDsmccServiceName(String uuid);
}
