package android.net.telecast;

/**
 * 电视广播网络接口
 */
public class NetworkInterface {
	/** 传输类型-未知(一般远程设备会无法预知类型) */
	public static final int DELIVERY_UNKNOWN = 0;
	/** 传输类型-有线 */
	public static final int DELIVERY_CABLE = 'C';
	/** 传输类型-卫星 */
	public static final int DELIVERY_SATELLITE = 'S';
	/** 传输类型-地面 */
	public static final int DELIVERY_TERRESTRIAL = 'T';

	/** 最大的本地接口ID值 */
	public static final int MAX_LOCAL_INTERFACE_ID = 999;
	/** 远程传输网络接口-ID1 */
	public static final int REMOTE_INTERFACE_ID1 = 1001;
	/** 远程传输网络接口-ID2 */
	public static final int REMOTE_INTERFACE_ID2 = 1002;	

	int id, type;
	boolean remote = false;
	boolean tdma = false;
	public NetworkInterface(int id, int type) {
		this.id = id;
		this.type = type;
	}

	NetworkInterface(int id, int type, boolean r, boolean t) {
		this.id = id;
		this.type = type;
		this.remote = r;
		this.tdma = t;
	}

	/** @hide */
	public NetworkInterface() {
	}

	/**
	 * 得到网络接口ID
	 * 
	 * @return id值
	 */
	public int getId() {
		return id;
	}

	/**
	 * 是否为远程设备
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isRemote() {
		return remote;
	}

	/**
	 * 设备传输为分时复用型
	 * 
	 * @return 是返回true,否则返回false
	 */
	public boolean isTdmaDelivery() {
		return tdma;
	}

	/**
	 * 传输类型
	 * 
	 * @return 类型值
	 */
	public int getDevliveryType() {
		return type;
	}

	@Override
	public int hashCode() {
		return id * 37 + type;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof NetworkInterface) {
			return ((NetworkInterface) o).id == id
					&& ((NetworkInterface) o).type == type;
		}
		return false;
	}

}
