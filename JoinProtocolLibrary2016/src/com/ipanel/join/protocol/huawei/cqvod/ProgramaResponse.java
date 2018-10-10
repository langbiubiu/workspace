package com.ipanel.join.protocol.huawei.cqvod;

import java.io.Serializable;

import com.google.gson.annotations.Expose;

/**
 * vod华为接口 栏目列表
 * 
 * @author dzwillpower
 * 
 */
public class ProgramaResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6194496541893794258L;
	
	
		@Expose
		private String typeId;
		@Expose
		private String typeName;
		@Expose
		private String isSubType;

		public String getTypeId() {
			return typeId;
		}

		public void setTypeId(String typeId) {
			this.typeId = typeId;
		}

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public String getIsSubType() {
			return isSubType;
		}

		public void setIsSubType(String isSubType) {
			this.isSubType = isSubType;
		}

		@Override
		public String toString() {
			return "ProgramaResponse [typeId=" + typeId + ", typeName=" + typeName + ", isSubType=" + isSubType + "]";
		}

		
		
}
