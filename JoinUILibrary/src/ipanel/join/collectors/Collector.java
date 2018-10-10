package ipanel.join.collectors;

import android.text.TextUtils;

public class Collector {

	/**�ռ���Ϣ*/
	private String msg;
	/**��Ϣ��Դ*/
	private String owner;
	/**�����ռ�*/
	private String time;

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getOwner() {
		if(TextUtils.isEmpty(owner)){
			return "default";
		}
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public boolean isValid() {
		return !TextUtils.isEmpty(msg) && !"".equals(msg);
	}
	
	@Override
	public String toString() {
		return "[ collector: msg->"+msg+"  owner->"+owner+" ]";
	}

}
