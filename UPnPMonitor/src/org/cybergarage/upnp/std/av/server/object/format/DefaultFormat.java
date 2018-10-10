/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File : DefaultPlugIn.java
*
*	Revision:
*
*	02/12/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.format;

import java.io.*;

import org.cybergarage.xml.*;
import org.cybergarage.upnp.std.av.server.object.*;
import org.cybergarage.util.MimeTypeMap;

public class DefaultFormat implements Format, FormatObject
{
	private File file;
	private String mime = "*/*";
	private String mediaClass = "object.item";
	
	////////////////////////////////////////////////
	// Constroctor
	////////////////////////////////////////////////
	
	public DefaultFormat()
	{
	}
	
	////////////////////////////////////////////////
	// Abstract Methods
	////////////////////////////////////////////////
	
	public DefaultFormat(File file) {
		this.file = file;
		this.mime = MimeTypeMap.getSingleton().getMimeTypeFromUrl(file.getAbsolutePath());
		if(mime.startsWith("video/")){
			mediaClass = "object.item.videoItem.movie";
		}
		if(mime.startsWith("audio/")){
			mediaClass = "object.item.audioItem.musicTrack";
		}
		if(mime.startsWith("image/")){
			mediaClass = "object.item.imageItem.photo";
		}
	}

	public boolean equals(File file)
	{
		String mime = MimeTypeMap.getSingleton().getMimeTypeFromUrl(file.getAbsolutePath());
		if (mime == null)
			return false;
		if (mime.startsWith("video/") || mime.startsWith("audio/") ||mime.startsWith("image/"))
			return true;
		return false;
	}
	
	public FormatObject createObject(File file)
	{
		return new DefaultFormat(file);
	}
	
	public String getMimeType()
	{
		return mime;
	}

	public String getMediaClass()
	{
		return mediaClass;
	}
	
	public AttributeList getAttributeList()
	{
		return new AttributeList();
	}
	
	public String getTitle()
	{
		return file.getName();
	}
	
	public String getCreator()
	{
		return "";
	}
}

