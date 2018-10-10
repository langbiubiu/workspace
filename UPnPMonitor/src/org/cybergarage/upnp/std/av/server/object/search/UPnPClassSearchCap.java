/******************************************************************
*
*	MediaServer for CyberLink
*
*	Copyright (C) Satoshi Konno 2003-2004
*
*	File: TitleSearchCap.java
*
*	Revision;
*
*	08/21/04
*		- first revision.
*
******************************************************************/

package org.cybergarage.upnp.std.av.server.object.search;

import org.cybergarage.upnp.std.av.server.object.*;

public class UPnPClassSearchCap implements SearchCap 
{
	public UPnPClassSearchCap() 
	{
	}
	
	public String getPropertyName() 
	{
		return SearchCriteria.CLASS;
	}

	public boolean compare(SearchCriteria searchCri, ContentNode conNode)
	{
		String searchCriTitle = searchCri.getValue();
		String conTitle = conNode.getUPnPClass();
		if (searchCriTitle == null|| conTitle == null)
				return false;
		if(searchCri.isEQ())
			return conTitle.equals(searchCriTitle);
		if(searchCri.isDerivedFrom())
			return conTitle.startsWith(searchCriTitle);
		return false;
	}
}

