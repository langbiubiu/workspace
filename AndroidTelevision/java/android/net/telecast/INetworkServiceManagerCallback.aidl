package android.net.telecast;

/*@hide*/
interface INetworkServiceManagerCallback{
	void onNetworkAdd( String netid);
	void onNetworkRemove(String netid);
	void onNetworkChange(String netid);	
}
