��Ҫ��˵��:

1. android.support.v4.View.ViewPager2

	��չViewPager, һ����ҳʱ��ÿ��ֻ����һҳ
	
2. cn.ipanel.android.net.http
	�첽http ���������. ��ʾ��:
	AsyncHttpClient client = new AsyncHttpClient();
	client.get("http://xxxx.xxxx/xxxx", new JsonHttpResponseHandler(){
		public void onSuccess(JSONObject jsonObject){
		
		}
	});
	
3. cn.ipanel.android.net.imgcache
	�첽ͼƬ�Զ�����, ����ͼƬ���ڴ�ʹ�����������, ���÷�:
	ImageView imageView = findViewById(R.id.image);
	SharedImageFetcher.getSharedFetcher(getContext()).loadImage("http://xxx.xx/xx.jpg",imageView);
	
4. cn.ipanel.android.widget
	��Ҫ��һЩListAdapter, ������ʹ�û�����ܱȽ���
	
	ViewFrameIndicator �÷�:
	a.��ʼ��, ��Activity onCreate�����
	ViewFrameIndicator mTestIndicator = new ViewFrameIndicator(this);
	mTestIndicator.setFrameResouce(R.drawable.focus_001);//�����.9.png,���Զ�����padding, ������Ҫ�Լ��趨
	b.�ƶ�frame
	mTestIndicator.moveFrameTo(viewToCover, true, false);
		