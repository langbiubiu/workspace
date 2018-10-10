package ngbj.ipanel.player;

import android.os.Parcel;
import android.os.Parcelable;

public class NgbjRect implements Parcelable {
	
	private int x;
	private int y;
	private int width;
	private int height;
	
	public NgbjRect(){}
	
	public NgbjRect(int x,	int y,	int width, int height){
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(x);
		dest.writeInt(y);
		dest.writeInt(width);
		dest.writeInt(height);
	}

	
	public static final Parcelable.Creator<NgbjRect> CREATOR = new Parcelable.Creator<NgbjRect>() {
		public NgbjRect createFromParcel(Parcel in) {
			NgbjRect  rect = new NgbjRect();
			rect.x = in.readInt();
			rect.y = in.readInt();
			rect.width = in.readInt();
			rect.height = in.readInt();
			return rect;
		}

		@Override
		public NgbjRect[] newArray(int size) {
			return new NgbjRect[size];
		}
	};
}
