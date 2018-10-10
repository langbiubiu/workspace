package ipanel.join.collectors;

import android.text.TextUtils;

public class Collector {

	/**收集消息*/
	private String msg;
	/**消息来源*/
	private String owner;
	/**接受收集*/
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
