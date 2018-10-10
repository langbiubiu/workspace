package com.ipanel.hengyun.message;

public class GatewayMessage {
	
	public static String getMessage(Event event) {
		String message = null;
		switch (event.subType) {
		case 6007:
			message = "1001 �Բ����ź���ʱ�ж�����������·�򲦴�ͷ����ߣ�96868";
			break;
		case 2005:
			message = "1002 �Բ����ź���ʱ�޷�����";
			break;
//		case 1007:
//			message = "1003 ��������ܿ�";
//			break;
		case 1008:
			message = "1004 ��Ч�����ܿ�";
			break;
		case 1013:
			message = "1006 �ý�Ŀ�ѹ���";
			break;
		case 1002:
			message = "1008 �ý�Ŀ����Ȩ";
			break;
		case 1009:
			message = "1011 �Բ������Ļ����к����ܿ�����Ӧ�������Ϊ��Ӧ�����ܿ���";
			break;
		case 2007:
			message = "1012 ����Ƶ�����£����������С�";
			break;
		case 2009:
			message = "1013 Ƶ�����³ɹ�";
			break;
		case 2006:
			message = "1014 Ƶ������ʧ��";
			break;
		case 4003:
			message = "1015 �������ظ��£�������������.";
			break;
		case 4004:
			message = "1019 ���ش��ڴ���״̬�������ݲ����á�";
			break;
		case 1014:
			message = "1021 �Բ��𣬸�Ƶ��������������������";
			break;
		case 4001:
			message = "1023 ��⵽USB�豸��������";
			break;
		case 4002:
			message = "1024 ��⵽USB�豸�γ�����";
			break;
		case 4005:
			message = "1025 �������������������ݲ�����";
			break;
		case 6003:
			message = "4001 �޷��������磬���������źŻ򲦴�96868";
			break;
		case 9001:
			message = "4003 �޷�������֤������";
			break;
		case 3135:
			message = "4004 EDS����ʧ��";
			break;
		case 3076:
			message = "4005 IC����Ϣ����δ��ͨ˫��ҵ��";
			break;
		case 3068:
			message = "4006 δ���󵽵㲥����";
			break;
		case 3168:
			message = "4007 IPQAM����ʧ��";
			break;
		case 3070:
			message = "4008 �������";
			break;
		case 3132:
			message = "4009 �������";
			break;
		case 3064:
			message = "4016 �ź���ʱ�޷�����";
			break;
		case 3137:
			message = "4011 IPQAM����ʧ��";
			break;
		case 3114:
		case 3116:
			message = "4012 �㲥��Ŀ�ݲ�����";
			break;
		case 3118:
			message = "4013 ʱ�ƽ�Ŀ�ݲ�����";
			break;
		case 6004:
			message = "4014 �㲥����������ִ������������豸";
			break;
		case 3065:
			message = "4017 �ź��ж�";
			break;
		case 3025:
			message = "4018 ��Ŀ�������";
			break;
		case 3060:
			message = "4019 �����˳�ʱ��״̬��";
			break;
		case 3007:
			message = "4020 ����δ������ҵ��";
			break;
		case 3043:
			message = "4025 �����˳��ؿ�״̬��";
			break;
		case 3059:
			message = "4026 ���ѿ��������";
			break;
		default:
			break;
		}
		return message;
	}

}
