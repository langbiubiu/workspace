package ipaneltv.toolkit.parentslock;

public class ParentLockChannel {

	private int id;
	private int	frequency;
	private int channel_number;
	private int	program_number;
	private String channel_name;//eg 中央一台
	private int locked;
//	private String program_desc;//eg CCTV-1
	
	public ParentLockChannel() {

	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public void setChannel_number(int channel_number) {
		this.channel_number = channel_number;
	}

	public void setProgram_number(int program_number) {
		this.program_number = program_number;
	}

	public int getFrequency() {
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
}
