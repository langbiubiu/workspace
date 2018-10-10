package org.cybergarage.upnp.std.av.server;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.QueryListener;
import org.cybergarage.util.Mutex;

public class MediaReceiverRegistrar  implements ActionListener, QueryListener{

	public final static String SERVICE_TYPE = "urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1";		
	
	// Browse Action	
	
	public final static String HTTP_GET = "http-get";
	
	// Action
	public static final String ACT_IsValidated = "IsValidated";
	public static final String ACT_RegisterDevice = "RegisterDevice";
	public static final String ACT_GetValidationSucceededUpdateID = "GetValidationSucceededUpdateID";
	public static final String ACT_IsAuthorized = "IsAuthorized";
	public static final String ACT_GetAuthorizationDeniedUpdateID = "GetAuthorizationDeniedUpdateID";
	public static final String ACT_GetValidationRevokedUpdateID = "GetValidationRevokedUpdateID";
	public static final String ACT_GetAuthorizationGrantedUpdateID = "GetAuthorizationGrantedUpdateID";
	
	//Arguments
	
	public static final String ARG_DeviceID = "DeviceID";
	public static final String ARG_Result = "Result";
	public static final String ARG_RegistrationReqMsg = "RegistrationReqMsg";
	public static final String ARG_ValidationSucceededUpdateID = "ValidationSucceededUpdateID";
	public static final String ARG_AuthorizationDeniedUpdateID = "AuthorizationDeniedUpdateID";
	public static final String ARG_ValidationRevokedUpdateID = "ValidationRevokedUpdateID";
	public static final String ARG_AuthorizationGrantedUpdateID = "AuthorizationGrantedUpdateID";

	//State Variable
	public static final String VAL_AuthorizationGrantedUpdateID = "AuthorizationGrantedUpdateID";
	public static final String VAL_AuthorizationDeniedUpdateID = "AuthorizationDeniedUpdateID";
	public static final String VAL_ValidationSucceededUpdateID = "ValidationSucceededUpdateID";
	public static final String VAL_ValidationRevokedUpdateID = "ValidationRevokedUpdateID";
	
	public static final String SCPD ="<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">\n<script />\n<specVersion>\n<major>1</major>\n<minor>0</minor>\n</specVersion>\n<actionList>\n<action>\n<name>IsValidated</name>\n<argumentList>\n<argument>\n<name>DeviceID</name>\n<direction>in</direction>\n<relatedStateVariable>A_ARG_TYPE_DeviceID</relatedStateVariable>\n</argument>\n<argument>\n<name>Result</name>\n<direction>out</direction>\n<relatedStateVariable>A_ARG_TYPE_Result</relatedStateVariable>\n</argument>\n</argumentList>\n</action>\n<action>\n<name>RegisterDevice</name>\n<argumentList>\n<argument>\n<name>RegistrationReqMsg</name>\n<direction>in</direction>\n<relatedStateVariable>A_ARG_TYPE_RegistrationReqMsg</relatedStateVariable>\n</argument>\n<argument>\n<name>RegistrationRespMsg</name>\n<direction>out</direction>\n<relatedStateVariable>A_ARG_TYPE_RegistrationRespMsg</relatedStateVariable>\n</argument>\n</argumentList>\n</action>\n<action>\n<name>GetValidationSucceededUpdateID</name>\n<argumentList>\n<argument>\n<name>ValidationSucceededUpdateID</name>\n<direction>out</direction>\n<relatedStateVariable>ValidationSucceededUpdateID</relatedStateVariable>\n</argument>\n</argumentList>\n</action>\n<action>\n<name>IsAuthorized</name>\n<argumentList>\n<argument>\n<name>DeviceID</name>\n<direction>in</direction>\n<relatedStateVariable>A_ARG_TYPE_DeviceID</relatedStateVariable>\n</argument>\n<argument>\n<name>Result</name>\n<direction>out</direction>\n<relatedStateVariable>A_ARG_TYPE_Result</relatedStateVariable>\n</argument>\n</argumentList>\n</action>\n<action>\n<name>GetAuthorizationDeniedUpdateID</name>\n<argumentList>\n<argument>\n<name>AuthorizationDeniedUpdateID</name>\n<direction>out</direction>\n<relatedStateVariable>AuthorizationDeniedUpdateID</relatedStateVariable>\n</argument>\n</argumentList>\n</action>\n<action>\n<name>GetValidationRevokedUpdateID</name>\n<argumentList>\n<argument>\n<name>ValidationRevokedUpdateID</name>\n<direction>out</direction>\n<relatedStateVariable>ValidationRevokedUpdateID</relatedStateVariable>\n</argument>\n</argumentList>\n</action>\n<action>\n<name>GetAuthorizationGrantedUpdateID</name>\n<argumentList>\n<argument>\n<name>AuthorizationGrantedUpdateID</name>\n<direction>out</direction>\n<relatedStateVariable>AuthorizationGrantedUpdateID</relatedStateVariable>\n</argument>\n</argumentList>\n</action>\n</actionList>\n<serviceStateTable>\n<stateVariable sendEvents=\"yes\">\n<name>AuthorizationGrantedUpdateID</name>\n<dataType>ui4</dataType>\n</stateVariable>\n<stateVariable sendEvents=\"no\">\n<name>A_ARG_TYPE_DeviceID</name>\n<dataType>string</dataType>\n</stateVariable>\n<stateVariable sendEvents=\"yes\">\n<name>AuthorizationDeniedUpdateID</name>\n<dataType>ui4</dataType>\n</stateVariable>\n<stateVariable sendEvents=\"yes\">\n<name>ValidationSucceededUpdateID</name>\n<dataType>ui4</dataType>\n</stateVariable>\n<stateVariable sendEvents=\"no\">\n<name>A_ARG_TYPE_RegistrationRespMsg</name>\n<dataType>bin.base64</dataType>\n</stateVariable>\n<stateVariable sendEvents=\"no\">\n<name>A_ARG_TYPE_RegistrationReqMsg</name>\n<dataType>bin.base64</dataType>\n</stateVariable>\n<stateVariable sendEvents=\"yes\">\n<name>ValidationRevokedUpdateID</name>\n<dataType>ui4</dataType>\n</stateVariable>\n<stateVariable sendEvents=\"no\">\n<name>A_ARG_TYPE_Result</name>\n<dataType>int</dataType>\n</stateVariable>\n</serviceStateTable>\n</scpd>";
	
	private MediaServer mediaServer;
	public MediaReceiverRegistrar(MediaServer mediaServer){
		this.mediaServer = mediaServer;
	}
	
	private Mutex mutex = new Mutex();
	
	public void lock()
	{
		mutex.lock();
	}
	
	public void unlock()
	{
		mutex.unlock();
	}
	

			
	@Override
	public boolean queryControlReceived(StateVariable stateVar) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean actionControlReceived(Action action) {
		String name = action.getName();
		if(ACT_IsAuthorized.equals(name)){
			action.setArgumentValue(ARG_Result, 1);
			return true;
		} else if(ACT_IsValidated.equals(name)){
			action.setArgumentValue(ARG_Result, 1);
			return true;
		}
		return false;
	}

}
