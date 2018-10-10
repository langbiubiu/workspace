package com.ipanel.join.cq.sihua.data;

import java.io.Serializable;
import java.util.List;

public class GetChannelResponse implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Body body;

	public class Body implements Serializable{
		
		public Result result;
		public Channel[] channels;
		
		public class Result implements Serializable{
			
			public int code;
			public String description;
		}
		
		public class Channel implements Serializable{
			
			public String channelCode;//CHANNEL_LIVE_QUERY
			public String channelName;
			public String imgPath;
			public String status;
			public Program[] assetPrograms;
			public Program[] AssetPrograms;//CHANNEL_SCHEDULE_QUERY
			
			public String titleFull;//CHANNEL_SCHEDULE_QUERY
			public String type;
			public PhysicalChannel[] physicalChannels;
			
			public class Program implements Serializable{
				
				public String assetID;
				public String startDateTime;
				public String endDateTime;
				public String programName;
				public String description;
				public String duration;
				public String category;
				public String type;
				
				public String channelID;//CHANNEL_SCHEDULE_QUERY
				public String status;
			}
			
			public class PhysicalChannel implements Serializable{
				
				public String assetID;
				public String encodingProfile;
				public String videoCodec;
				public String audioCodec;
				public String hDFlag;
				public String channelID;
				public String bitRate;
				public String mimeType;
			}
		}
	}
}
