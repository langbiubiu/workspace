package cn.ipanel.dlna.device;

import java.io.File;

import org.cybergarage.http.HTTPRequest;
import org.cybergarage.net.HostInterface;
import org.cybergarage.upnp.Device;
import org.cybergarage.upnp.Service;
import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.util.Debug;

public class KeypadMouseDevice extends Device {
	// //////////////////////////////////////////////
	// Constants
	// //////////////////////////////////////////////

	public final static String DEVICE_TYPE = "urn:schemas-upnp-org:device:KeypadMouseServer:1";

	public final static int DEFAULT_HTTP_PORT = 38920;

	public final static String DESCRIPTION = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
			+ "<root xmlns=\"urn:schemas-upnp-org:device-1-0\">\n"
			+ "   <specVersion>\n"
			+ "      <major>1</major>\n"
			+ "      <minor>0</minor>\n"
			+ "   </specVersion>\n"
			+ "   <device>\n"
			+ "      <deviceType>urn:schemas-upnp-org:device:KeypadMouseServer:1</deviceType>\n"
			+ "      <friendlyName>iPanel Keypad Mouse Server</friendlyName>\n"
			+ "      <manufacturer>iPanel</manufacturer>\n"
			+ "      <manufacturerURL>http://www.ipanel.tv</manufacturerURL>\n"
			+ "      <modelDescription>Provides remote keypad mouse through UPnP KeyPadMouseControl service</modelDescription>\n"
			+ "      <modelName>iPanel Keypad Mouse Server</modelName>\n"
			+ "      <modelNumber>1.0</modelNumber>\n"
			+ "      <modelURL>http://www.ipanel.tv</modelURL>\n"
			+ "      <UDN>uuid:362d9414-21a8-48b6-b684-2b4bd38391d0</UDN>\n"
			+ "      <serviceList>\n"
			+ "         <service>\n"
			+ "            <serviceType>urn:schemas-upnp-org:service:KeypadMouseControl:1</serviceType>\n"
			+ "            <serviceId>urn:upnp-org:serviceId:urn:schemas-upnp-org:service:KeypadMouseControl</serviceId>\n"
			+ "            <SCPDURL>/service/KeypadMouseControl1.xml</SCPDURL>\n"
			+ "            <controlURL>/service/KeypadMouseControl_control</controlURL>\n"
			+ "            <eventSubURL>/service/KeypadMouseControl_event</eventSubURL>\n"
			+ "         </service>\n"
			+ "      </serviceList>\n" + "   </device>\n" + "</root>";

	// //////////////////////////////////////////////
	// Constructor
	// //////////////////////////////////////////////

	private final static String DESCRIPTION_FILE_NAME = "description/description.xml";
	
	KeypadMouseControl mKeypadMouseControl;

	public KeypadMouseDevice(String descriptionFileName) throws InvalidDescriptionException {
		super(new File(descriptionFileName));
		initialize();
	}

	public KeypadMouseDevice() {
		super();
		try {
			initialize(DESCRIPTION, KeypadMouseControl.SCPD);
		} catch (InvalidDescriptionException ide) {
		}
	}

	public KeypadMouseDevice(String description, String keypadMouseSCPD)
			throws InvalidDescriptionException {
		super();
		initialize(description, keypadMouseSCPD);
	}

	private void initialize(String description, String keypadMouseSCPD)
			throws InvalidDescriptionException {
		loadDescription(description);

		Service servConDir = getService(KeypadMouseControl.SERVICE_TYPE);
		servConDir.loadSCPD(keypadMouseSCPD);
		initialize();
	}

	private void initialize() {
		// Netwroking initialization
		// UPnP.setEnable(UPnP.USE_ONLY_IPV4_ADDR);
		// String firstIf = HostInterface.getHostAddress(0);
		// setInterfaceAddress(firstIf);
		setHTTPPort(DEFAULT_HTTP_PORT);

		mKeypadMouseControl = new KeypadMouseControl(this);
		// conMan = new ConnectionManager(this);
		// mediaReg = new MediaReceiverRegistrar(this);
		//
		Service service = getService(KeypadMouseControl.SERVICE_TYPE);
		service.setActionListener(mKeypadMouseControl);
		service.setQueryListener(mKeypadMouseControl);
		//
		// Service servConMan = getService(ConnectionManager.SERVICE_TYPE);
		// servConMan.setActionListener(getConnectionManager());
		// servConMan.setQueryListener(getConnectionManager());
		//
		// Service servMediaReg =
		// getService(MediaReceiverRegistrar.SERVICE_TYPE);
		// servMediaReg.setActionListener(getMediaRegistrar());
		// servMediaReg.setQueryListener(getMediaRegistrar());
	}

	protected void finalize() {
		stop();
	}

	public void setInterfaceAddress(String ifaddr) {
		HostInterface.setInterface(ifaddr);
	}

	public String getInterfaceAddress() {
		return HostInterface.getInterface();
	}

	// //////////////////////////////////////////////
	// HttpRequestListner (Overridded)
	// //////////////////////////////////////////////

	public void httpRequestRecieved(HTTPRequest httpReq) {
		String uri = httpReq.getURI();
		Debug.message("uri = " + uri);

		super.httpRequestRecieved(httpReq);
	}

	// //////////////////////////////////////////////
	// start/stop (Overided)
	// //////////////////////////////////////////////

	public boolean start() {
		super.start();
		return true;
	}

	public boolean stop() {
		super.stop();
		return true;
	}

	// //////////////////////////////////////////////
	// update
	// //////////////////////////////////////////////

	public void update() {
	}

}
