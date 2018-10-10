/******************************************************************
*
*	MediaPlayer for CyberLink
*
*	Copyright (C) Satoshi Konno 2005
*
*	File : MediaPlayer.java
*
*	09/26/05
*		- first revision.
*	02/05/08
*		- Added getContentDirectory(dev, objectId).
*		- Added browse().
*
******************************************************************/

package org.cybergarage.upnp.std.av.player;

import org.cybergarage.upnp.std.av.renderer.*;
import org.cybergarage.upnp.std.av.controller.*;

public class MediaPlayer
{
	////////////////////////////////////////////////
	// Member
	////////////////////////////////////////////////
	
	private MediaRenderer renderer = null;
	private MediaController controller = null;
	
	////////////////////////////////////////////////
	// Constructor
	////////////////////////////////////////////////
	
	public MediaPlayer(MediaRenderer renderer, MediaController controller)
	{
		this.renderer = renderer;
		this.controller = controller;
	}
	
	////////////////////////////////////////////////
	// Controller
	////////////////////////////////////////////////
	
	public MediaController getController()
	{
		return controller;
	}

	public boolean isControllerEnable()
	{
		return (controller != null) ? true : false;
	}
	
	////////////////////////////////////////////////
	// Renderer
	////////////////////////////////////////////////
	
	public MediaRenderer getRenderer()
	{
		return renderer;
	}

	public boolean isRendererEnable()
	{
		return (renderer != null) ? true : false;
	}
	
	////////////////////////////////////////////////
	// Member
	///////////////////////////
	/////////////////////
	
	public void start()
	{
		if (renderer != null)
			renderer.start();
		if (controller != null)
			controller.start();
	}
	
	public void stop()
	{
		if (renderer != null)
			renderer.stop();
		if (controller != null)
			controller.stop();
	}
}
