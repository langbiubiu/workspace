package cn.ipanel.dlna;

import org.cybergarage.upnp.std.av.server.object.item.ItemNode;
import org.cybergarage.upnp.std.av.server.object.item.ResourceNode;

public class Util {
	public static final String NOT_IMPLEMENTED = "NOT_IMPLEMENTED";

	public static String formatMediaTime(int value){
		int ds = value % 60;
		value /= 60;
		int dm = value % 60;
		value /= 60;
		return String.format("%02d:%02d:%02d", value, dm, ds);
	}
	
	public static int parseMediaTime(String str){
		if(str == null || str.length()==0 || NOT_IMPLEMENTED.equals(str)){
			return -1;
		}
		
		int idx = str.indexOf('.');
		if(idx != -1)
			str = str.substring(0, idx);
		int seconds = 0;
		int minutes = 0;
		int hours = 0;
		
		idx = str.lastIndexOf(':');
		if(idx != -1){
			seconds = Integer.parseInt(str.substring(idx+1));
			str = str.substring(0, idx);
		}
		
		idx = str.lastIndexOf(':');
		if(idx != -1){
			minutes= Integer.parseInt(str.substring(idx+1));
			str = str.substring(0, idx);
		}
		
		idx = str.lastIndexOf(':');
		if(idx != -1){
			hours = Integer.parseInt(str.substring(idx+1));
			str = str.substring(0, idx);
		}

		return hours * 3600 + minutes*60 + seconds;
	}

	public static ResourceNode findBestResource(ItemNode item){
		ResourceNode best = null;
		int priority = -1;
		int size = item.getNNodes();
		for(int i=0;i<size;i++){
			ResourceNode r = item.getResourceNode(i);
			int p = getPriority(r);
			if(p > priority){
				best = r;
				priority = p;
			}
		}
		return best;
	}

	private static int getPriority(ResourceNode r) {
		if(r.isVideo()){
			return getVideoPriority(r.getContentFormat());
		}
		if(r.isAudio()){
			return getAudioPriority(r.getContentFormat());
		}
		if(r.isImage()){
			return getImagePriority(r.getContentFormat());
		}
		return 0;
	}

	private static int getImagePriority(String contentFormat) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int getAudioPriority(String contentFormat) {
		// TODO Auto-generated method stub
		return 0;
	}

	private static int getVideoPriority(String contentFormat) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	
}
