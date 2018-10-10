package com.ipanel.join.cq.vod.order;

import java.util.ArrayList;
import java.util.List;

/**
 * 电影、电视剧的订购
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
		 * 模拟数据
		 */
		order1.orderName = "TV+ HD版";
		order1.price = "20元/月/终端";
		order1.desc = "高清大片，剧集综艺，任意点播";
		order1.posterUrl="http://p3.qhimg.com/dr/250_500_/t0170c4c5acbc04cf4f.png";
		
		order2.orderName = "TV+ 乐享版";
		order2.price = "25元/月/终端";
		order2.desc = "海量大片随意挑选";
		order2.posterUrl="http://img5q.duitang.com/uploads/item/201402/02/20140202131333_5QWcu.thumb.700_0.jpeg";
		
		order3.orderName = "HBO 美剧";
		order3.price = "23元/月/终端";
		order3.desc = "品牌专区-HBO美剧";
		order3.posterUrl="http://img01.store.sogou.com/net/a/04/link?appid=100520031&w=710&url=http://mmbiz.qpic.cn/mmbiz/iciccsvt6TiawpChfic4KdicKSB3lw3e1GFBoFGWxBdIJFpCgEXDWdMQsicdFRAF7624YSXjCzTemkl86rBiaoZLH7MuA/0?wx_fmt=";
		
		order4.orderName = "英美剧专区";
		order4.price = "23元/月/终端";
		order4.desc = "品牌专区-英美";
		order4.posterUrl="http://npic7.edushi.com/cn/zixun/zh-chs/2015-10/26/64eccad03f3e4a17ad487e75b7260e80.jpg";
		
		order5.orderName = "凤凰专区";
		order5.price = "23元/月/终端";
		order5.desc = "品牌专区-凤凰专区";
		order5.posterUrl="http://img1.imgtn.bdimg.com/it/u=3214266199,3152827822&fm=21&gp=0.jpg";
		
		order6.orderName = "韩剧专区";
		order6.price = "23元/月/终端";
		order6.desc = "品牌专区";
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
