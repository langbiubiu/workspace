package ipaneltv.toolkit.db;

import java.util.List;

public class DvbDatabaseObjectification extends DatabaseObjectification {

	public static class Service extends Channel {
		protected int serviceType, eitPfFlag, eitScheduleFlag, isCaFree;
		protected String shortServiceName, shortProvider_name;

		public int getServiceType() {
			return serviceType;
		}

		public int getEitPfFlag() {
			return eitPfFlag;
		}

		public int getEitScheduleFlag() {
			return eitScheduleFlag;
		}

		public int getIsCaFree() {
			return isCaFree;
		}

		public String getShortServiceName() {
			return shortServiceName;
		}

		public String getShortProvider_name() {
			return shortProvider_name;
		}
	}

	public static class Bouquet extends Objectification {
		protected String name, shortName;
		protected int id, transportStreamId, serviceId;
		protected List<ChannelKey> keys;

		public String getName() {
			return name;
		}

		public int getServiceId() {
			return serviceId;
		}

		public int getTransportStreamId() {
			return transportStreamId;
		}

		public String getShortName() {
			return shortName;
		}

		public int getId() {
			return id;
		}

		public List<ChannelKey> getChannelKeys() {
			return keys;
		}
	}

	public static class Network extends Objectification {
		protected String name, shortName;
		protected int id;
		protected List<ChannelKey> keys;

		public String getShortName() {
			return shortName;
		}

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public List<ChannelKey> getChannelKeys() {
			return keys;
		}
	}

	public static class TransportStream extends Frequency {
		protected int transportStreamId, originalNetworkId, networkId;

		public int getNetworkId() {
			return networkId;
		}

		public int getOriginalNetworkId() {
			return originalNetworkId;
		}

		public int getTransportStreamId() {
			return transportStreamId;
		}
	}

	public static class ProgramEvent extends Program {
		protected int eventId, isCaFree, runningStatus;
		protected String shortEventName;

		public int getRunningStatus() {
			return runningStatus;
		}

		public int getEventId() {
			return eventId;
		}

		public String getShortEventName() {
			return shortEventName;
		}
	}

	public static class ElementaryStream extends Stream {
		protected int componentTag;

		public int getComponentTag() {
			return componentTag;
		}
	}

}
