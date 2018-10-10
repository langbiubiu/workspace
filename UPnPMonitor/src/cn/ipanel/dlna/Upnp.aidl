package cn.ipanel.dlna;

interface Upnp{
	String getWifiApInfo();
	String setWifiApEnable(String enable);
	int reset(int flag);
	String getGateWayInfo();
	String setWifiAp(String data);
}