package com.ipanel.join.chongqing.wechattv;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ipanel.chongqing_ipanelforhw.R;
import com.ipanel.join.cq.vod.utils.Tools;

public class PicScanActivity extends Activity{
	ImageView wechat_pic;
	private int[] images;       //ͼƬ��Դid����  
    private int currentImage;   //��ǰ��ʾͼƬ 
    int num;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.picscan_activity);		
		init();
	}
	private void init() {
		num = getIntent().getExtras().getInt("num");
		/*���ݵ�����û���ʼ��Ҫ��ʾ��ͼƬ*/
		images = new int[]{R.drawable.img_01, R.drawable.img_02,R.drawable.img_03};  
        currentImage = Integer.MAX_VALUE / 2;   
        wechat_pic = (ImageView) findViewById(R.id.wechat_pic); 
        wechat_pic.setImageResource(images[(currentImage + num) % images.length]);		
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event){
		switch (keyCode){
		/*��ʾ��һ��*/
		case KeyEvent.KEYCODE_DPAD_LEFT:
			wechat_pic.setImageResource(images[ (--currentImage + num) % images.length ]);  
            return true;
         /*��ʾ��һ��*/
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			wechat_pic.setImageResource(images[ (++currentImage + num) % images.length ]);  
            return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
