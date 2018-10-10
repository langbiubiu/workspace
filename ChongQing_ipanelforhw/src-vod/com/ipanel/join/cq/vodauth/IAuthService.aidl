package com.ipanel.join.cq.vodauth;

/**
*This service is for retrieve ServiceGroupId and maintain login session
*
*/
interface IAuthService{
	/**
	* This is a blocking call, do not call it from UI thread
	*/
	boolean syncStartAuth();
	/**
	* Return login status, 1 means login success
	*/
	int getAuthStatus();
	long getServiceGroupId();
	/**
	* EPG server prefix, example:  http://192.168.12.157:8082/EPG/jsp
	*/
	String getEPGServerUrl();
	
	String getIP();
	String getMAC();
	String getCACardNumber();
	String getCookieString();
}