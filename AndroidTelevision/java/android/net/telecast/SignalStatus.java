package android.net.telecast;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 信号状态
 */
public final class SignalStatus implements Parcelable {
	int quality = 0;
	int strength = 0;
	int level = 0;
	int snr = 0;
	float ber = 0;

	SignalStatus() {
	}

	/**
	 * 信号质量
	 * 
	 * @return 值，单位分贝
	 */
	public int getQuality() {
		return quality;
	}

	/**
	 * 信号强度
	 * 
	 * @return 值，单位分贝
	 */
	public int getStrength() {
		return strength;
	}

	/**
	 * 信号水平
	 * 
	 * @return 值，单位分贝
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * 信噪比
	 * 
	 * @return 值
	 */
	public int getSNR() {
		return snr;
	}

	/**
	 * 误差率
	 * 
	 * @return 值
	 */
	public float getBER() {
		return ber;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int arg1) {
		dest.writeInt(quality);
		dest.writeInt(strength);
		dest.writeInt(level);
		dest.writeInt(snr);
		dest.writeFloat(ber);
	}

	public static final Parcelable.Creator<SignalStatus> CREATOR = new Parcelable.Creator<SignalStatus>() {
		public SignalStatus createFromParcel(Parcel in) {
			SignalStatus tp = new SignalStatus();
			tp.quality = in.readInt();
			tp.strength = in.readInt();
			tp.level = in.readInt();
			tp.snr = in.readInt();
			tp.ber = in.readFloat();
			return tp;
		}

		public SignalStatus[] newArray(int size) {
			return new SignalStatus[size];
		}
	};

	public String toString() {
		return quality + "," + strength + "," + level + "," + snr + "," + ber;
	}

	public static final SignalStatus fromString(String s) {
		String[] p = s.split(",");
		SignalStatus ss = new SignalStatus();
		ss.quality = Integer.parseInt(p[0]);
		ss.strength = Integer.parseInt(p[1]);
		ss.level = Integer.parseInt(p[2]);
		ss.snr = Integer.parseInt(p[3]);
		ss.ber = Float.parseFloat(p[4]);
		return ss;
	}
}
