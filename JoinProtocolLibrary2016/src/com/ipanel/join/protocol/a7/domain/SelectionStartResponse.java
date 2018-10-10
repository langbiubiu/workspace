package com.ipanel.join.protocol.a7.domain;

import java.io.Serializable;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

@Root(name = "StartResponse", strict = false)
public class SelectionStartResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 264151850556176409L;
	@Attribute(name="purchaseToken",required=false)
	private String purchaseToken;
	@Attribute(name="previewAssetId",required=false)
	private String previewAssetId;
	@Attribute(required=false)
	private String rtsp;
	
	public String getPurchaseToken() {
		return purchaseToken;
	}
	public void setPurchaseToken(String purchaseToken) {
		this.purchaseToken = purchaseToken;
	}
	public String getPreviewAssetId() {
		return previewAssetId;
	}
	public void setPreviewAssetId(String previewAssetId) {
		this.previewAssetId = previewAssetId;
	}
	
	public String getRtsp() {
		return rtsp;
	}
	public void setRtsp(String rtsp) {
		this.rtsp = rtsp;
	}
	@Override
	public String toString() {
		return "SelectionStartResponse [purchaseToken=" + purchaseToken + ", previewAssetId=" + previewAssetId
				+ ", rtsp=" + rtsp + "]";
	}
	
	

}
