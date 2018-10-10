package ipaneltv.toolkit.db;

public class WardshipProgram {

	protected int id;
	protected long frequency;
	protected String local_number;
	protected int type;
	protected int channel_number;
	protected int program_number;
	protected String channel_name;// eg 中央一台
	protected int locked = 0;
	protected int hide = 0;

	// private String program_desc;//eg CCTV-1

	public WardshipProgram() {

	}

	public int getHide() {
		return hide;
	}
	
	public int getType() {
		return type;
	}

	public void setHide(int hide) {
		this.hide = hide;
	}

//	public void setFrequency(int frequency) {
//		this.frequency = frequency;
//	}

	public String getLocal_number() {
		return local_number;
	}

	public void setLocal_number(String local_number) {
		this.local_number = local_number;
	}

	public void setChannel_number(int channel_number) {
		this.channel_number = channel_number;
	}

//	public void setProgram_number(int program_number) {
//		this.program_number = program_number;
//	}

	public long getFrequency() {
		return frequency;
	}

	public int getChannel_number() {
		return channel_number;
	}

	public int getProgram_number() {
		return program_number;
	}

	public String getChannel_name() {
		return channel_name;
	}

	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLocked() {
		return locked;
	}

	public void setLocked(int locked) {
		this.locked = locked;
	}

	@Override
	public String toString() {
		return "channel_name = " + channel_name + ";channel_number = " + channel_number
				+ ";program_number = " + program_number + ";locked = " + locked + ";hide = " + hide;
	}

}
