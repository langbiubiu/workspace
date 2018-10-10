package cn.ipanel.net.imgcache;

import java.io.File;
import java.util.concurrent.Executors;

import cn.ipanel.dlna.Logger;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Build;

public class SharedImageFetcher
{
    private static final String IMAGE_CACHE_DIR = "imgcache";
    private static ImageCache   mImageCache;
    private static ImageFetcher mFetcher;
    private static final int    CACHE_VERSION   = 2;

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
        f.setExecutor(Executors.newFixedThreadPool(poolSize));
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
            Logger.d("Upgrade cache data");
            File cdir = ImageCache.getDiskCacheDir(context, IMAGE_CACHE_DIR);
            deleteDirectory(cdir);
            Logger.d("clear old cache done");
            sp.edit().putInt("CacheVersion", CACHE_VERSION).commit();
        }
        ImageCache.ImageCacheParams cacheParams = new ImageCache.ImageCacheParams(context, IMAGE_CACHE_DIR);
        cacheParams.setMemCacheSizePercent(context, 0.18f);
        cacheParams.compressFormat = Bitmap.CompressFormat.JPEG;
        cacheParams.compressQuality = 85;
        cacheParams.diskCacheSize = 1024 * 1024 * 100;

        final ImageCache cache = new ImageCache(cacheParams);
        
        return cache;
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
        ImageFetcher mFetcher = new ImageFetcher(context, (context.getResources().getDisplayMetrics().widthPixels + context.getResources().getDisplayMetrics().heightPixels) / 7);
        mFetcher.setImageCache(mImageCache);
        mFetcher.initCache();
        mFetcher.setImageFadeIn(false);
        return mFetcher;
    }

}
