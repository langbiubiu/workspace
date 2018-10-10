package cn.ipanel.dlna;

import java.util.UUID;

import org.cybergarage.upnp.device.InvalidDescriptionException;
import org.cybergarage.upnp.std.av.renderer.AVTransport;
import org.cybergarage.upnp.std.av.renderer.ConnectionManager;
import org.cybergarage.upnp.std.av.renderer.MediaRenderer;
import org.cybergarage.upnp.std.av.renderer.RenderingControl;
import org.cybergarage.upnp.std.av.server.ContentDirectory;
import org.cybergarage.upnp.std.av.server.MediaReceiverRegistrar;
import org.cybergarage.upnp.std.av.server.MediaServer;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class DeviceBuilder {
	public final static String RENDERER_DESCRIPTION = 
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<root xmlns=\"urn:schemas-upnp-org:device-1-0\"  xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\">\n" +
			"   <specVersion>\n" +
			"      <major>1</major>\n" +
			"      <minor>0</minor>\n" +
			"   </specVersion>\n" +
			"   <device>\n" +
			"      <deviceType>urn:schemas-upnp-org:device:MediaRenderer:1</deviceType>\n" +
			"      <dlna:X_DLNACAP>playcontainer-0-1</dlna:X_DLNACAP>\n"+
			"      <dlna:X_DLNADOC>DMR-1.50</dlna:X_DLNADOC>\n"+			
			"      <friendlyName>%1$s</friendlyName>\n" +
			"      <manufacturer>%2$s</manufacturer>\n" +
			"      <manufacturerURL>http://www.ipanel.cn</manufacturerURL>\n" +
			"      <modelDescription>Simple UPnP Renderer</modelDescription>\n" +
			"      <modelName>%3$s</modelName>\n" +
			"      <modelNumber>%4$s</modelNumber>\n" +
			"      <modelURL>http://www.ipanel.cn</modelURL>\n" +
			"      <UDN>uuid:%5$s</UDN>\n" +
//			"	   <dlna:X_DLNADOC xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\">DMR-1.50</dlna:X_DLNADOC>" +
			"	   <iconList><icon><mimetype>image/png</mimetype><width>96</width><height>96</height><depth>32</depth><url>/icon/device.png</url></icon></iconList>" +
			"      <serviceList>\n" +
			"         <service>\n" +
			"            <serviceType>urn:schemas-upnp-org:service:RenderingControl:1</serviceType>\n" +
			"            <serviceId>urn:upnp-org:serviceId:RenderingControl</serviceId>\n" +
			"            <SCPDURL>/service/RenderingControl1.xml</SCPDURL>\n" +
			"            <controlURL>/service/RenderingControl_control</controlURL>\n" +
			"            <eventSubURL>/service/RenderingControl_event</eventSubURL>\n" +
			"         </service>\n" +
			"         <service>\n" +
			"            <serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType>\n" +
			"            <serviceId>urn:upnp-org:serviceId:ConnectionManager</serviceId>\n" +
			"            <SCPDURL>/service/ConnectionManager1.xml</SCPDURL>\n" +
			"            <controlURL>/service/ConnectionManager_control</controlURL>\n" +
			"            <eventSubURL>/service/ConnectionManager_event</eventSubURL>\n" +
			"         </service>\n" +
			"         <service>\n" +
			"            <serviceType>urn:schemas-upnp-org:service:AVTransport:1</serviceType>\n" +
			"            <serviceId>urn:upnp-org:serviceId:AVTransport</serviceId>\n" +
			"            <SCPDURL>/service/AVTransport1.xml</SCPDURL>\n" +
			"            <controlURL>/service/AVTransport_control</controlURL>\n" +
			"            <eventSubURL>/service/AVTransport_event</eventSubURL>\n" +
			"         </service>\n" +
			"      </serviceList>\n" +
			"   </device>\n" +
			"</root>";
	
	public static MediaRenderer createRenderer(Context context, String name) {
		SharedPreferences sp = context.getSharedPreferences(DeviceBuilder.class.getName(), 0);
		String key = MediaRenderer.class.getSimpleName();
		String uuid = sp.getString(key, UUID.randomUUID().toString());
		if(!sp.contains(key)){
			sp.edit().putString(key, uuid).commit();
		}
		String desc = String.format(RENDERER_DESCRIPTION, name, Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, uuid);
		
		try {
			return new MediaRenderer(desc, RenderingControl.SCPD, ConnectionManager.SCPD, AVTransport.SCPD);
		} catch (InvalidDescriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	
	public final static String SERVER_DESCRIPTION = 
			"<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
			"<root xmlns=\"urn:schemas-upnp-org:device-1-0\"  xmlns:dlna=\"urn:schemas-dlna-org:device-1-0\">\n" +
			"   <specVersion>\n" +
			"      <major>1</major>\n" +
			"      <minor>0</minor>\n" +
			"   </specVersion>\n" +
			"   <device>\n" +
			"      <deviceType>urn:schemas-upnp-org:device:MediaServer:1</deviceType>\n" +
			"      <friendlyName>%1$s</friendlyName>\n" +
			"      <manufacturer>%2$s</manufacturer>\n" +
			"      <manufacturerURL>http://www.ipanel.cn</manufacturerURL>\n" +
			"      <modelDescription>Provides content through UPnP ContentDirectory service</modelDescription>\n" +
			"      <modelName>%3$s</modelName>\n" +
			"      <modelNumber>%4$s</modelNumber>\n" +
			"      <modelURL>http://www.ipanel.cn</modelURL>\n" +
			"      <UDN>uuid:%5$s</UDN>\n" +
			"	   <dlna:X_DLNADOC>DMS-1.50</dlna:X_DLNADOC>" +
			"	   <iconList><icon><mimetype>image/png</mimetype><width>96</width><height>96</height><depth>32</depth><url>/icon/device.png</url></icon></iconList>" +
			"      <serviceList>\n" +
			"         <service>\n" +
			"            <serviceType>urn:schemas-upnp-org:service:ContentDirectory:1</serviceType>\n" +
			"            <serviceId>urn:upnp-org:serviceId:urn:schemas-upnp-org:service:ContentDirectory</serviceId>\n" +
			"            <SCPDURL>/service/ContentDirectory1.xml</SCPDURL>\n" +
			"            <controlURL>/service/ContentDirectory_control</controlURL>\n" +
			"            <eventSubURL>/service/ContentDirectory_event</eventSubURL>\n" +
			"         </service>\n" +
			"         <service>\n" +
			"            <serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType>\n" +
			"            <serviceId>urn:upnp-org:serviceId:urn:schemas-upnp-org:service:ConnectionManager</serviceId>\n" +
			"            <SCPDURL>/service/ConnectionManager1.xml</SCPDURL>\n" +
			"            <controlURL>/service/ConnectionManager_control</controlURL>\n" +
			"            <eventSubURL>/service/ConnectionManager_event</eventSubURL>\n" +
			"         </service>\n" +
			"         <service>\n" +
			"            <serviceType>urn:microsoft.com:service:X_MS_MediaReceiverRegistrar:1</serviceType>\n" +
			"            <serviceId>urn:microsoft.com:serviceId:X_MS_MediaReceiverRegistrar</serviceId>\n" +
			"            <SCPDURL>/service/MediaReceiverRegistrar1.xml</SCPDURL>\n" +
			"            <controlURL>/service/MediaReceiverRegistrar_control</controlURL>\n" +
			"            <eventSubURL>/service/MediaReceiverRegistrar_event</eventSubURL>\n" +
			"         </service>\n" +
			"      </serviceList>\n" +
			"   </device>\n" +
			"</root>";

	public static MediaServer createServer(Context context, String name) {
		SharedPreferences sp = context.getSharedPreferences(DeviceBuilder.class.getName(), 0);
		String key = MediaServer.class.getSimpleName();
		String uuid = sp.getString(key, UUID.randomUUID().toString());
		if(!sp.contains(key)){
			sp.edit().putString(key, uuid).commit();
		}
		String desc = String.format(SERVER_DESCRIPTION, name, Build.MANUFACTURER, Build.MODEL, Build.VERSION.RELEASE, uuid);
		
		try {
			return new MediaServer(desc, ContentDirectory.SCPD, ConnectionManager.SCPD, MediaReceiverRegistrar.SCPD);
		} catch (InvalidDescriptionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
