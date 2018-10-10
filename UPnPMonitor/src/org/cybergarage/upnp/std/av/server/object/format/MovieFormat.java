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

public class MovieFormat implements Format, FormatObject {
	private File file;

	// //////////////////////////////////////////////
	// Constroctor
	// //////////////////////////////////////////////

	public MovieFormat() {
	}

	// //////////////////////////////////////////////
	// Abstract Methods
	// //////////////////////////////////////////////

	public MovieFormat(File file) {
		this.file = file;
	}

	public boolean equals(File file) {
		String mime = MimeTypeMap.getSingleton().getMimeTypeFromUrl(file.getAbsolutePath());
		if (mime == null)
			return false;
		if (mime.startsWith("video/"))
			return true;
		return false;
	}

	public FormatObject createObject(File file) {
		return new MovieFormat(file);
	}

	public String getMimeType() {
		return "video/mpeg";
	}

	public String getMediaClass() {
		return "object.item.videoItem.movie";
	}

	public AttributeList getAttributeList() {
		return new AttributeList();
	}

	public String getTitle() {
		return file.getName();
	}

	public String getCreator() {
		return "";
	}
}
