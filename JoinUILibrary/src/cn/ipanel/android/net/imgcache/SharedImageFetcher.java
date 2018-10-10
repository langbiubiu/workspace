package cn.ipanel.android.net.imgcache;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cn.ipanel.android.Logger;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;

public class SharedImageFetcher
{
    private static final String IMAGE_CACHE_DIR = "imgcache";
    private static ImageCache   mImageCache;
    private static ImageFetcher mFetcher;
    private static final int    CACHE_VERSION   = 3;
    
    public static float sCacheSize = 0.18f;
    public static boolean sDiskCacheEnabled = true;
    /**
     * disk cache size in MB
     */
    public static int sDiskCacheSize = 100;
    public static int sPoolSize = -1;

    public static synchronized ImageFetcher getSharedFetcher(Context context)
    {
        initCache(context);
        if (mFetcher == null)
            mFetcher = createFetcher(context);
        return mFetcher;
    }

    private static synchronized void initCache(Context context)
    {
        if (mImageCache == null)
            mImageCache = createCache(context);
    }

    public static synchronized ImageFetcher getNewFetcher(Context context, int poolSize)
    {
        context = context.getApplicationContext();
        initCache(context);
		ImageFetcher f = createFetcher(context);
		f.setExecutor(new ThreadPoolExecutor(0, poolSize, 60L, TimeUnit.SECONDS,
				new LinkedBlockingQueue<Runnable>()));
        return f;
    }
    
    public static synchronized ImageFetcher createFetcher(Context context, ExecutorService pool){
        context = context.getApplicationContext();
        initCache(context);
        ImageFetcher f = createFetcher(context);
        f.setExecutor(pool);
        return f;   	
    }

    public static void clearMemoryCache(){
        if(mImageCache != null)
            mImageCache.clearMemoryCache();
    }
    
    private static ImageCache createCache(Context context)
    {
        SharedPreferences sp = context.getSharedPreferences("CacheConfig", 0);
        int ver = sp.getInt("CacheVersion", 0);
        if(ver < CACHE_VERSION){
            clearDiskCache(context);
            sp.edit().putInt("CacheVersion", CACHE_VERSION).commit();
        }
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(context, IMAGE_CACHE_DIR);
        cacheParams.diskCacheEnabled = sDiskCacheEnabled;
        cacheParams.setMemCacheSizePercent(context, sCacheSize);
        cacheParams.compressFormat = Bitmap.CompressFormat.JPEG;
        cacheParams.compressQuality = 100;
        cacheParams.diskCacheSize = 1024 * 1024 * sDiskCacheSize;

        final ImageCache cache = new ImageCache(cacheParams);
        
        return cache;
    }

	/**
	 * USE with caution! Once called, all ImageFetcher should be recreated, otherwise disk cache
	 * will failed and may cause memory issue
	 * 
	 * @param context
	 */
	public static void clearDiskCache(Context context) {
		Logger.d("Upgrade cache data");
		File cdir = ImageCache.getDiskCacheDir(context, IMAGE_CACHE_DIR);
		deleteDirectory(cdir);
		clearMemoryCache();
		//once directory is delete, need to recreate cache
		mImageCache = null;
		mFetcher = null;
		Logger.d("clear old cache done");
	}
    
    public static boolean deleteDirectory(File path) {
        if( path.exists() ) {
          File[] files = path.listFiles();
          if (files == null) {
              return true;
          }
          for(int i=0; i<files.length; i++) {
             if(files[i].isDirectory()) {
               deleteDirectory(files[i]);
             }
             else {
               files[i].delete();
             }
          }
        }
        return( path.delete() );
      }

    private static ImageFetcher createFetcher(Context context)
    {
        // The ImageFetcher takes care of loading images into our ImageView
        // children asynchronously
		ImageFetcher mFetcher = new ImageFetcher(context,
				(context.getResources().getDisplayMetrics().widthPixels + context.getResources()
						.getDisplayMetrics().heightPixels) / 2);
        mFetcher.setImageCache(mImageCache);
        mFetcher.initCache();
		if (sPoolSize > 0)
			mFetcher.setExecutor(Executors.newFixedThreadPool(sPoolSize));
		//        mFetcher.setImageFadeIn(false);
        return mFetcher;
    }

}
