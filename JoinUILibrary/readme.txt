主要类说明:

1. android.support.v4.View.ViewPager2

	扩展ViewPager, 一屏多页时可每次只滚动一页
	
2. cn.ipanel.android.net.http
	异步http 请求帮助类. 简单示例:
	AsyncHttpClient client = new AsyncHttpClient();
	client.get("http://xxxx.xxxx/xxxx", new JsonHttpResponseHandler(){
		public void onSuccess(JSONObject jsonObject){
		
		}
	});
	
3. cn.ipanel.android.net.imgcache
	异步图片自动加载, 包含图片的内存和磁盘两级缓存, 简单用法:
	ImageView imageView = findViewById(R.id.image);
	SharedImageFetcher.getSharedFetcher(getContext()).loadImage("http://xxx.xx/xx.jpg",imageView);
	
4. cn.ipanel.android.widget
	主要是一些ListAdapter, 电视上使用机会可能比较少
	
	ViewFrameIndicator 用法:
	a.初始化, 在Activity onCreate里调用
	ViewFrameIndicator mTestIndicator = new ViewFrameIndicator(this);
	mTestIndicator.setFrameResouce(R.drawable.focus_001);//如果是.9.png,会自动设置padding, 否则需要自己设定
	b.移动frame
	mTestIndicator.moveFrameTo(viewToCover, true, false);
		