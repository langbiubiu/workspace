package cn.ipanel.dlna.device;

import org.cybergarage.upnp.Action;
import org.cybergarage.upnp.StateVariable;
import org.cybergarage.upnp.control.ActionListener;
import org.cybergarage.upnp.control.QueryListener;

public class KeypadMouseControl implements ActionListener, QueryListener {
	////////////////////////////////////////////////
	// Constants
	////////////////////////////////////////////////

	public final static String SERVICE_TYPE = "urn:schemas-upnp-org:service:KeypadMouseControl:1";
	
	public final static String SCPD = 
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<scpd xmlns=\"urn:schemas-upnp-org:service-1-0\">\n" +
			"   <specVersion>\n" +
			"      <major>1</major>\n" +
			"      <minor>0</minor>\n" +
			"	</specVersion>\n" +
			"	<actionList>\n" +
			"		<action>\n" +
			"         <name>SendKey</name>\n" +
			"         <argumentList>\n" +
			"            <argument>\n" +
			"               <name>KeyCode</name>\n" +
			"               <direction>in</direction>\n" +
			"            </argument>\n" +
			"            <argument>\n" +
			"               <name>ActionType</name>\n" +
			"               <direction>in</direction>\n" +
			"            </argument>\n" +
			"         </argumentList>\n" +
			"      </action>\n" +
			"      <action>\n" +
			"         <name>SendPointer</name>\n" +
			"         <argumentList>\n" +
			"            <argument>\n" +
			"               <name>deltaX</name>\n" +
			"               <direction>in</direction>\n" +
			"            </argument>\n" +
			"            <argument>\n" +
			"               <name>deltaY</name>\n" +
			"               <direction>in</direction>\n" +
			"            </argument>\n" +
			"         </argumentList>\n" +
			"      </action>\n" +
			"   </actionList>\n" +
			"   <serviceStateTable>\n" +
			"   </serviceStateTable>\n" +
			"</scpd>";	


	KeypadMouseDevice mKeypadMouseDevice;
	public KeypadMouseControl(KeypadMouseDevice keypadMouseDevice) {
		this.mKeypadMouseDevice = keypadMouseDevice;
	}

	@Override
	public boolean queryControlReceived(StateVariable stateVar) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean actionControlReceived(Action action) {
		// TODO Auto-generated method stub
		return false;
	}		
}
