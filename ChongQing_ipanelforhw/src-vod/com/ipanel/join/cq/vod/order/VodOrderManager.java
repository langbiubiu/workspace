package com.ipanel.join.cq.vod.order;

import java.util.ArrayList;
import java.util.List;

/**
 * ��Ӱ�����Ӿ�Ķ���
 */
public class VodOrderManager {
	public static List<OrderInfo> getOrderList(){
		List<OrderInfo> list = new ArrayList<OrderInfo>();
		OrderInfo order1 = new OrderInfo();
		OrderInfo order2 = new OrderInfo();
		OrderInfo order3 = new OrderInfo();
		OrderInfo order4 = new OrderInfo();
		OrderInfo order5 = new OrderInfo();
		OrderInfo order6 = new OrderInfo();
		/**
		 * ģ������
		 */
		order1.orderName = "TV+ HD��";
		order1.price = "20Ԫ/��/�ն�";
		order1.desc = "�����Ƭ���缯���գ�����㲥";
		order1.posterUrl="http://p3.qhimg.com/dr/250_500_/t0170c4c5acbc04cf4f.png";
		
		order2.orderName = "TV+ �����";
		order2.price = "25Ԫ/��/�ն�";
		order2.desc = "������Ƭ������ѡ";
		order2.posterUrl="http://img5q.duitang.com/uploads/item/201402/02/20140202131333_5QWcu.thumb.700_0.jpeg";
		
		order3.orderName = "HBO ����";
		order3.price = "23Ԫ/��/�ն�";
		order3.desc = "Ʒ��ר��-HBO����";
		order3.posterUrl="http://img01.store.sogou.com/net/a/04/link?appid=100520031&w=710&url=http://mmbiz.qpic.cn/mmbiz/iciccsvt6TiawpChfic4KdicKSB3lw3e1GFBoFGWxBdIJFpCgEXDWdMQsicdFRAF7624YSXjCzTemkl86rBiaoZLH7MuA/0?wx_fmt=";
		
		order4.orderName = "Ӣ����ר��";
		order4.price = "23Ԫ/��/�ն�";
		order4.desc = "Ʒ��ר��-Ӣ��";
		order4.posterUrl="http://npic7.edushi.com/cn/zixun/zh-chs/2015-10/26/64eccad03f3e4a17ad487e75b7260e80.jpg";
		
		order5.orderName = "���ר��";
		order5.price = "23Ԫ/��/�ն�";
		order5.desc = "Ʒ��ר��-���ר��";
		order5.posterUrl="http://img1.imgtn.bdimg.com/it/u=3214266199,3152827822&fm=21&gp=0.jpg";
		
		order6.orderName = "����ר��";
		order6.price = "23Ԫ/��/�ն�";
		order6.desc = "Ʒ��ר��";
		order6.posterUrl="http://cdnweb.b5m.com/web/discuz/portal/201502/25/150807e7ppzwsdpxjsyypg.jpg";
		
		list.add(order1);
		list.add(order2);
		list.add(order3);
		list.add(order4);
		list.add(order5);
		list.add(order6);
		
		return list;
	}
}

class OrderInfo{
	String orderName;
	String price;
	String desc;
	String posterUrl;
}
