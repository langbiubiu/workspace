package org.cybergarage.http.netty;

import org.cybergarage.http.HTTP;
import org.jboss.netty.handler.codec.http.HttpMethod;

public class UPnPHttpMethod extends HttpMethod{
	public UPnPHttpMethod(String name) {
		super(name);
	}

	public static HttpMethod SUBSCRIBE = new HttpMethod(HTTP.SUBSCRIBE);
	public static HttpMethod UNSUBSCRIBE = new HttpMethod(HTTP.UNSUBSCRIBE);
	public static HttpMethod NOTIFY = new HttpMethod(HTTP.NOTIFY);
	
}
