package android.net.telecast;

/**
 * ���ӹ㲥����ӿ�
 */
public class NetworkInterface {
	/** ��������-δ֪(һ��Զ���豸���޷�Ԥ֪����) */
	public static final int DELIVERY_UNKNOWN = 0;
	/** ��������-���� */
	public static final int DELIVERY_CABLE = 'C';
	/** ��������-���� */
	public static final int DELIVERY_SATELLITE = 'S';
	/** ��������-���� */
	public static final int DELIVERY_TERRESTRIAL = 'T';

	/** ���ı��ؽӿ�IDֵ */
	public static final int MAX_LOCAL_INTERFACE_ID = 999;
	/** Զ�̴�������ӿ�-ID1 */
	public static final int REMOTE_INTERFACE_ID1 = 1001;
	/** Զ�̴�������ӿ�-ID2 */
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
	 * �õ�����ӿ�ID
	 * 
	 * @return idֵ
	 */
	public int getId() {
		return id;
	}

	/**
	 * �Ƿ�ΪԶ���豸
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean isRemote() {
		return remote;
	}

	/**
	 * �豸����Ϊ��ʱ������
	 * 
	 * @return �Ƿ���true,���򷵻�false
	 */
	public boolean isTdmaDelivery() {
		return tdma;
	}

	/**
	 * ��������
	 * 
	 * @return ����ֵ
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
