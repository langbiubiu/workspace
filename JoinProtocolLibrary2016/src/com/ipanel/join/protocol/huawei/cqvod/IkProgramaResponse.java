package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class IkProgramaResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3493341129783506685L;
	@Expose
	private String total;
	@Expose
	@SerializedName("col")
	private List<Columes> cols;

	public String getTotal() {
		return total;
	}

	public void setTotal(String total) {
		this.total = total;
	}

	public List<Columes> getCols() {
		return cols;
	}

	public void setCols(List<Columes> cols) {
		this.cols = cols;
	}

	public class Columes implements Serializable {

		/**
		 * 
		 */
		private static final long serialVersionUID = 6194496541893794258L;

		@Expose
		private String typeID;
		@Expose
		private String name;
		@Expose
		private String HD;
		@Expose
		private String img;
		public String getTypeID() {
			return typeID;
		}
		public void setTypeID(String typeID) {
			this.typeID = typeID;
		}
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getHD() {
			return HD;
		}
		public void setHD(String hD) {
			HD = hD;
		}
		public String getImg() {
			return img;
		}
		public void setImg(String img) {
			this.img = img;
		}

	

	}

}
