package com.ipanel.hengyun.message;

public class GatewayMessage {
	
	public static String getMessage(Event event) {
		String message = null;
		switch (event.subType) {
		case 6007:
			message = "1001 对不起，信号暂时中断请检查室内线路或拨打客服热线：96868";
			break;
		case 2005:
			message = "1002 对不起，信号暂时无法接收";
			break;
//		case 1007:
//			message = "1003 请插入智能卡";
//			break;
		case 1008:
			message = "1004 无效的智能卡";
			break;
		case 1013:
			message = "1006 该节目已过期";
			break;
		case 1002:
			message = "1008 该节目无授权";
			break;
		case 1009:
			message = "1011 对不起，您的机顶盒和智能卡不对应，请更换为对应的智能卡。";
			break;
		case 2007:
			message = "1012 发现频道更新，正在搜索中…";
			break;
		case 2009:
			message = "1013 频道更新成功";
			break;
		case 2006:
			message = "1014 频道更新失败";
			break;
		case 4003:
			message = "1015 发现网关更新，即将进行升级.";
			break;
		case 4004:
			message = "1019 网关处于待机状态，服务暂不可用。";
			break;
		case 1014:
			message = "1021 对不起，该频道被加锁，请输入密码";
			break;
		case 4001:
			message = "1023 检测到USB设备插入网关";
			break;
		case 4002:
			message = "1024 检测到USB设备拔出网关";
			break;
		case 4005:
			message = "1025 网关正在重启，服务暂不可用";
			break;
		case 6003:
			message = "4001 无法连接网络，请检查室内信号或拨打96868";
			break;
		case 9001:
			message = "4003 无法连接认证服务器";
			break;
		case 3135:
			message = "4004 EDS调度失败";
			break;
		case 3076:
			message = "4005 IC卡信息错误，未开通双向业务";
			break;
		case 3068:
			message = "4006 未请求到点播数据";
			break;
		case 3168:
			message = "4007 IPQAM申请失败";
			break;
		case 3070:
			message = "4008 网络故障";
			break;
		case 3132:
			message = "4009 网络故障";
			break;
		case 3064:
			message = "4016 信号暂时无法接收";
			break;
		case 3137:
			message = "4011 IPQAM申请失败";
			break;
		case 3114:
		case 3116:
			message = "4012 点播节目暂不可用";
			break;
		case 3118:
			message = "4013 时移节目暂不可用";
			break;
		case 6004:
			message = "4014 点播过程网络出现错误，请检查网关设备";
			break;
		case 3065:
			message = "4017 信号中断";
			break;
		case 3025:
			message = "4018 节目播放完毕";
			break;
		case 3060:
			message = "4019 您已退出时移状态！";
			break;
		case 3007:
			message = "4020 您尚未订购该业务！";
			break;
		case 3043:
			message = "4025 您已退出回看状态！";
			break;
		case 3059:
			message = "4026 您已快退至起点";
			break;
		default:
			break;
		}
		return message;
	}

}
