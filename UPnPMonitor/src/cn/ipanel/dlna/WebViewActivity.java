package cn.ipanel.dlna;


import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.Bundle;
import android.view.KeyEvent;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebSettings.ZoomDensity;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends Activity implements UPnPService.PlayerControlListener
{
	List<PlayEntry> entryList;
	int position;
	
    private WebView webview;
    
    //private ImageFetcher mFetcher;
    
    private ProgressDialog loading;
    
    static final String template = "<html><meta name='viewport' content='width=device-width,height=device-height, user-scalable=no' /><body><img src = \"{0}\" /></body></html>";
    
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);


		if (getIntent().hasExtra(PlayEntry.EXTRA_ENTRY_LIST)) {
			entryList = (ArrayList<PlayEntry>) getIntent().getSerializableExtra(
					PlayEntry.EXTRA_ENTRY_LIST);
			position = getIntent().getIntExtra(PlayEntry.EXTRA_POSITION, 0);
		} else {
			entryList = new ArrayList<PlayEntry>();
			entryList.add((PlayEntry) getIntent().getSerializableExtra(PlayEntry.EXTRA_ENTRY));
			position = 0;
		}

        String url = entryList.get(position).url;
        webview = new WebView(getApplicationContext()){

        	@Override
        	public boolean onKeyDown(int keyCode, KeyEvent event) {
        		switch(keyCode){
        		case KeyEvent.KEYCODE_DPAD_LEFT:
        			if(position>0){
        				position--;
        				playCurrentEntry();
        				return true;
        			}
        			break;
        		case KeyEvent.KEYCODE_DPAD_RIGHT:
        			if(position < entryList.size()-1){
        				position++;
        				playCurrentEntry();
        				return true;
        			}
        			break;
        		}
        		return super.onKeyDown(keyCode, event);
        	}
        };
        webview.getSettings().setAppCachePath(getCacheDir().getAbsolutePath());
        webview.getSettings().setAppCacheMaxSize(16 * 1024 * 1024);
        webview.getSettings().setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        webview.getSettings().setAppCacheEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.getSettings().setUseWideViewPort(true);
        webview.getSettings().setLoadWithOverviewMode(true);
        webview.getSettings().setBuiltInZoomControls(true);
        webview.getSettings().setSupportZoom(true);
        webview.getSettings().setAllowFileAccess(true);
        webview.getSettings().setDefaultZoom(ZoomDensity.FAR);
        webview.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        webview.setWebViewClient(new MyWebViewClient());
        webview.setWebChromeClient(new WebChromeClient(){

			@Override
			public void onProgressChanged(WebView view, int newProgress) {
				if(newProgress < 100)
					loading.show();
				else
					loading.dismiss();
				super.onProgressChanged(view, newProgress);
			}
        	
        });
        
//        ImageView iView = new ImageView(this);
//        iView.setScaleType(ScaleType.MATRIX);
//        iView.setOnTouchListener(new MultiTouchListener());
//
//        setContentView(iView);
//        
//        mFetcher = SharedImageFetcher.getNewFetcher(getApplicationContext(), 1);
//        int size = (getResources().getDisplayMetrics().widthPixels+getResources().getDisplayMetrics().heightPixels)/3;
//        mFetcher.setImageSize(size);
//        
        loading = ProgressDialog.show(this, "", "");
        loading.setCancelable(true);
        loading.setOnCancelListener(new OnCancelListener(){

			@Override
			public void onCancel(DialogInterface arg0) {
				finish();
			}
        	
        });
//        mFetcher.loadImage(new BaseImageFetchTask(url, url,size+"x"+size).setListener(new ImageFetchListener(){
//
//			@Override
//			public void OnComplete(int status) {
//				if(loading != null)
//					loading.dismiss();
//				
//			}
//        	
//        }), iView);
        //SharedImageFetcher.getSharedFetcher(getBaseContext()).loadImage(url, iView);
        
        System.out.println(url);
        setContentView(webview);
        //webview.loadUrl(url);
        String temp = IOUtils.loadAssetText(this, "template.html");
        webview.loadDataWithBaseURL("", temp.replace("$IMG_URL", url), "text/html", "UTF-8", "");
        UPnPService.setPlayerListener(this);   
    }
    
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }
    }

    
	@Override
	protected void onDestroy() {
		UPnPService.clearPlayerListener(this);
		//mFetcher.stopFetcher();
		if(loading != null)
			loading.dismiss();
		super.onDestroy();
	}




	private void playCurrentEntry() {
		webview.loadUrl(null);
        String url = entryList.get(position).url;
        String temp = IOUtils.loadAssetText(this, "template.html");
        webview.loadDataWithBaseURL("", temp.replace("$IMG_URL", url), "text/html", "UTF-8", "");

	}


	@Override
	public void stop() {
		finish();
	}


	@Override
	public int getDuration() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getPosition() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void seek(int position) {
		// TODO Auto-generated method stub
		
	}




	@Override
	public void setPause(boolean pause) {
		// TODO Auto-generated method stub
		
	}    
    
    
}
