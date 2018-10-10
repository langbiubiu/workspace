package com.ipanel.join.cq.vod.order;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import cn.ipanel.android.net.http.RequestParams;
import cn.ipanel.android.reflect.SysUtils;

import com.ipanel.chongqing_ipanelforhw.CQApplication;
import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.detail.DetailActivity;
import com.ipanel.join.cq.vod.utils.Logger;
import com.ipanel.join.protocol.a7.ServiceHelper;
import com.ipanel.join.protocol.a7.ServiceHelper.ResponseHandlerT;
import com.ipanel.join.protocol.a7.ServiceHelper.SerializerType;
import com.ipanel.join.protocol.huawei.cqvod.RTSPResponse;

public class OrderDialog2 extends Dialog implements OnClickListener {
	private Context context;
	private FrameLayout fl; // 充值父类布局
	private EditText et; // 输入金额
	private TextView hundred; // 充值100
	private TextView fifty; // 充值50
	private TextView back; // 返回
	private TextView confirm; // 确认
	private TextView ok; // 充值
	private TextView sure; // 购买
	private TextView cancel; // 取消
	private TextView content; // 显示提示
	private String tipContent; // 提示内容
	private String url = "http://192.168.9.120/chongqing_TV/index.htm";
	private RTSPResponse rtspResponse;
	private String rechargeUrl = "http://192.168.9.106/commonPay/index.htm?payMoney=num&returnUrl=";
	/***
	 * payChannel：商户编码：ccn+16位字符串，此处固定为ccnc00c309bf4a4b4ce
	 * userName：用户名，此处key和valu均不传
	 * orderId:商户订单号，任意唯一小于64位的字符串，此处用当前系统时间表示
	 * payAmount：充值金额，单位为分
	 * targetId：机顶盒序列号
	 * cardNo：CA卡号
	 * extend：扩展字段，可用于商品描述，此处使用4K充值缴费
	 * notifyUrl：商户服务器异步通知地址，暂无，用其他不为空字符串表示
	 * homeUrl：充值成功或取消支付返回应用首页地址(格式：scheme://host:port/path)
	 * returnUrl：充值成功返回应用界面地址(格式：scheme://host:port/path)
	 * 注：homeUrl和returnUrl的scheme与AndroidManifest.xml中配置一样即可，其他可随意填写
	 * rechargeType：缴费类型，此处1表示账户充值
	 * rechargeAccount：缴费账本，此处0表示通用账本
	 */
	private String payUrl = "http://tvpay.cqccn.com:9000/ccn-pay/payPlatform"
			+ "?payChannel=ccnc00c309bf4a4b4ce&orderId=time"
			+ "&payAmount=amount&targetId=stbId&cardNo=cardId&extend=4K充值缴费"
			+ "&notifyUrl=undefined"
			+ "&homeUrl=homeValue"
			+ "&returnUrl=returnValue"
			+ "&rechargeType=1&rechargeAccount=0";

	public OrderDialog2(Context context, String tipContent) {
		super(context, R.style.Dialog_Collect_Dark);
		this.context = context;
		this.tipContent = tipContent;
	}
	
	public OrderDialog2(Context context, RTSPResponse rtspResponse) {
		super(context, R.style.Dialog_Collect_Dark);
		this.context = context;
		this.rtspResponse = rtspResponse;
		this.tipContent = rtspResponse.getMessage();
	}

	public OrderDialog2(Context context, String tipContent, int theme) {
		super(context, theme);
		this.context = context;
		this.tipContent = tipContent;
	}

	public OrderDialog2(Context context, String tipContent, int theme,
			OrderDialog2Listener orderDialog2Listener) {
		super(context, theme);
		this.context = context;
		this.tipContent = tipContent;
		this.orderDialog2Listener = orderDialog2Listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.order_dialog2);
		fl = (FrameLayout) findViewById(R.id.order_dialog2_fl);
		hundred = (TextView) findViewById(R.id.order_dialog2_hundred);
		fifty = (TextView) findViewById(R.id.order_dialog2_fifty);
		et = (EditText) findViewById(R.id.order_dialog2_et);
		back = (TextView) findViewById(R.id.order_dialog2_back);
		confirm = (TextView) findViewById(R.id.order_dialog2_confirm);
		ok = (TextView) findViewById(R.id.order_dialog2_ok);
		sure = (TextView) findViewById(R.id.order_dialog2_sure);
		cancel = (TextView) findViewById(R.id.order_dialog2_cancel);
		content = (TextView) findViewById(R.id.order_dialog2_tip);
		hundred.setOnClickListener(this);
		fifty.setOnClickListener(this);
		back.setOnClickListener(this);
		confirm.setOnClickListener(this);
		ok.setOnClickListener(this);
		sure.setOnClickListener(this);
		cancel.setOnClickListener(this);
		if (null != tipContent) {
			if ("1".equals(rtspResponse.getAnCiFlag())) { // 按次购买点播
				content.setText(tipContent+"\n"+"购买成功后可在48小时内使用当前机顶盒反复观看");
			}else { // 其他方式
				content.setText(tipContent);
				sure.setText("进入营业厅");
			}
		}
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.order_dialog2_sure:
			Log.i("OrderDialog2", " =购买= ");
			if ("1".equals(rtspResponse.getAnCiFlag())) {
				content.setText(context.getResources().getString(R.string.purchaseing_film));
				buyInSequence();
			}else {
				Log.i("OrderDialog2", " =进入营业厅= ");
				startTVBusinessHall();
			}
//			content.setText(context.getResources().getString(R.string.purchaseing_film));
//			buyInSequence();
			break;
		case R.id.order_dialog2_cancel:
			Log.i("OrderDialog2", " =取消= ");
			this.dismiss();
			if (orderDialog2Listener != null) {
				orderDialog2Listener.cancel();
			} else {

			}
			break;
		case R.id.order_dialog2_ok:
			Log.i("OrderDialog2", " =购买= ");
			this.dismiss();
			recharge("0.01");
			break;
		case R.id.order_dialog2_hundred:
			Log.i("OrderDialog2", " =充值100=");
			this.dismiss();
			recharge("10000");
			break;
		case R.id.order_dialog2_fifty:
			Log.i("OrderDialog2", " =充值50=");
			this.dismiss();
			recharge("5000");
			break;
		case R.id.order_dialog2_back:
			Log.i("OrderDialog2", " =返回=");
			this.dismiss();
			break;
		case R.id.order_dialog2_confirm:
			this.dismiss();
			String value = et.getText().toString().trim() + "00";
			Log.i("OrderDialog2", " =其他金额="+value);
			recharge(value);
			break;
		}

	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			this.dismiss();
			break;
		}
		return super.onKeyDown(keyCode, event);
	}

	OrderDialog2Listener orderDialog2Listener;

	public interface OrderDialog2Listener {
		public void sure();

		public void cancel();
	}

	private void gotoIPanel30(String url) {
		Log.i("OrderDialog", " =gotoIPanel30= ");
		try {
			Intent intent = new Intent();
			intent.putExtra("url", url);
			intent.setClassName("com.ipanel.dtv.chongqing",
					"com.ipanel.dtv.chongqing.IPanel30PortalActivity");
			context.startActivity(intent);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Log.d("OrderDialog", "gotoIPanel30 e = " + e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 刷新界面
	 * @param result
	 */
	private void updatUI(RTSPResponse result){
		if (result == null) {
			content.setText(context.getResources().getString(R.string.purchase_failed));
		}else {
			if (result.getMessage().contains("2")) { // 余额不足
				sure.setVisibility(View.GONE);
				cancel.setVisibility(View.GONE);
				fl.setVisibility(View.VISIBLE);
				hundred.requestFocus();
				content.setText(context.getResources().getString(R.string.insufficient_funds));
			}else {
				content.setText(result.getMessage());
			}
		}
	}
	
	/**
	 * 按次购买
	 */
	private void buyInSequence(){
		ServiceHelper serviceHelper = ServiceHelper.getHelper();
		Logger.d("OrderDialog2", "buyInSequence"+",rtspResponse="+(rtspResponse == null ? "rtspResponse is null" : rtspResponse.getConfirmUrl()));
		serviceHelper.setRootUrl(rtspResponse.getConfirmUrl());
		serviceHelper.setSerializerType(SerializerType.JSON);
		RequestParams requestParamsfilm = new RequestParams();
		serviceHelper.callServiceAsync(context,
				requestParamsfilm, RTSPResponse.class,
				new ResponseHandlerT<RTSPResponse>() {

					@Override
					public void onResponse(boolean success,
							RTSPResponse result) {
						Logger.d("OrderDialog2", "buyInSequence success = "+success);
						if (success && result != null && "1".equals(result.getPlayFlag())) {
							OrderDialog2.this.dismiss();
							((DetailActivity)context).startToMoviePlay(result);
						} else {
							updatUI(result);
						}
					}
				});
	}
	
	/**
	 * 充值
	 * @param value 充值金额,单位为分
	 */
	private void recharge(String value){
		Logger.d("OrderDialog2", "recharge value = "+value);
		payUrl = payUrl.replace("time", ""+System.currentTimeMillis());
		payUrl = payUrl.replace("amount", value); // 充值金额，单位为分
		String propStbID = SysUtils.getSystemProperty(SysUtils.PROP_STB_ID);
		payUrl = payUrl.replace("stbId", propStbID); // 机顶盒序列号
		String card = CQApplication.getInstance().getCardID();
		payUrl = payUrl.replace("cardId", card); // CA卡号
		String home = "ipanelfilmlist://FilmListActivity/home?name=4K体验&params=10000100000000090000000000105822&sub=1";
		String returns = new StringBuffer("ipaneldetail://DetailActivity/return?").append(((DetailActivity)context).params).toString();
		Logger.d("OrderDialog2", "recharge returns = "+returns);
		try {
//			payUrl = payUrl.replace("homeValue",  URLEncoder.encode(home, "UTF-8")); // 参数
			payUrl = payUrl.replace("homeValue",  URLEncoder.encode(returns, "UTF-8")); // 参数
			payUrl = payUrl.replace("returnValue",  URLEncoder.encode(returns, "UTF-8")); // 参数
			Logger.d("OrderDialog2", "recharge payUrl = "+payUrl);
			ComponentName component = new ComponentName("com.crunii.ccn.tvhall",
					"com.crunii.ccn.tvhall.activity.PayActivity");
			Intent intent = new Intent();
			intent.setComponent(component);
			intent.putExtra("payurl",payUrl);
			context.startActivity(intent);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			Logger.d("OrderDialog2", "recharge e = "+e);
			e.printStackTrace();
		}
	}
	
	/**
	 * 电视营业厅
	 */
	private void startTVBusinessHall(){
		ComponentName com = new ComponentName("com.crunii.ccn.tvhall",
				"com.crunii.ccn.tvhall.activity.MainActivity");
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setComponent(com);
		if (intent.resolveActivity(context.getPackageManager()) != null) {
			context.startActivity(intent);
		}
	}
}
