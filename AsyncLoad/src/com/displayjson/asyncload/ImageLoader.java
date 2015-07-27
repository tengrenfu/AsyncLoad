/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  
package com.displayjson.asyncload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.widget.ImageView;


public class ImageLoader {
    private ImageMemoryCache mMemoryCache;
    private FileCache mFileCache;
    private Map<ImageView, String> mImageViews;
    private ExecutorService executorService; 
    
    public ImageLoader(Context context) {
    	mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    	mMemoryCache = new ImageMemoryCache();
        mFileCache = new FileCache(context);
        /*
         * a thread pool is ready for downloading, decoding and displaying
         * the images on the list view 
         */
        executorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
    }
    
    /**
     * here queue a task to get the image from file system
     * or from web site, then decode and display it on the list view
     */
    private void queueImage(String url, ImageView imageView) {
        BitmapToLoad bitmapToLoad = new BitmapToLoad(url, imageView);
        executorService.submit(new BitmapLoader(bitmapToLoad));
    }
    
    /**
     * display the image on the list view if it's in memory cache
     * or call queueImage to run a thread to download, decode and display it 
     */
    public void displayImageOnView(String url, ImageView imageView) {
    	if (url == null || imageView == null) {
    		return;
    	}
        mImageViews.put(imageView, url);
        Bitmap bitmap = mMemoryCache.getImage(url);
        if (bitmap != null) {
        	/*
        	 * in memory cache, display it directly if the image view
        	 * is't reused by another row item
        	 */
        	if (imageView.getTag() != null && imageView.getTag().equals(url)) {
        		imageView.setImageBitmap(bitmap);
        	}
        } else {
        	/*
        	 * not in memory cache, queue a task thread to go on
        	 */
        	queueImage(url, imageView);
        	imageView.setImageResource(R.color.transparent);
        }
    }

    private void copyStream(InputStream src, OutputStream dst) {
        int bufferSize = Constants.BUFFER_SIZE;
        
        try {
            byte[] bytes = new byte[bufferSize];
            for(;;) {
            	int size = src.read(bytes, 0, bufferSize);
            	if (size == -1) {
            		break;
            	}
            	dst.write(bytes, 0, size);
            }
        } catch(Exception e){
        }
    }
    
    /**
     * get image from file system or from web site
     * then decode the image
     */
    private Bitmap getBitmap(String url) {
    	/*
    	 * get image from file cache first
    	 */
        File file = mFileCache.getFile(url);
        Bitmap iamge = decodeImage(file);
        if (iamge != null) {
            return iamge;
        }        
        /*
         * get image from web site and save it 
         * in file system then decode the image
         */
        try {
            Bitmap bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection)imageUrl.openConnection();
            conn.setConnectTimeout(Constants.IMAGE_CONNECT_TIMEOUT);
            conn.setReadTimeout(Constants.IMAGE_READ_TIME);
            conn.setInstanceFollowRedirects(true);
            
            InputStream src = conn.getInputStream();
            OutputStream dst = new FileOutputStream(file);
            copyStream(src, dst);
            dst.close();
            
            bitmap = decodeImage(file);
            return bitmap;
        } catch (Throwable e){
           if (e instanceof OutOfMemoryError) {
               mMemoryCache.clear();
           }
        }
        return null;
    }

    /**
     * decoding the image according to 
     * the proper scale to save memory used
     */
    private Bitmap decodeImage(File file) {
        try {
            /*
             * get the size of the image first
             */
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(file), null, options);            
            /*
             * here calculate the scale for next step to decode
             * according to the width or height of the image 
             * is smaller than Constants.REQUIRED_IMAGE_SIZE
             */
            int scale = 1;
            int width = options.outWidth, height = options.outHeight;
            for (;;) {
                if (width / 2 < Constants.REQUIRED_IMAGE_SIZE ||
                		height / 2 < Constants.REQUIRED_IMAGE_SIZE) {
                    break;
                }
                width = width / 2;
                height = height / 2;
                scale = scale * 2;
            }
            /*
             * then decode the image according to the scale
             */
            BitmapFactory.Options opt = new BitmapFactory.Options();
            opt.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(file), null, opt);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
    
    /**
     * It's a helper class for BitmapLoader
     */
    private class BitmapToLoad {
        public String mUrl;
        public ImageView mImageView;
        
        public BitmapToLoad(String url, ImageView view){
            mUrl = url; 
            mImageView = view;
        }
    }
    
    /**
     * call ImageDisplayer to display the image 
     * on list view and ask the UI thread to 
     * update the list view 
     */
    private class BitmapLoader implements Runnable {
    	BitmapToLoad mBitmapToLoad;
    	
    	BitmapLoader(BitmapToLoad bitmapToLoad) {
    		mBitmapToLoad = bitmapToLoad;
        }
        
        @Override
        public void run() {
            if (isImageViewReused(mBitmapToLoad)) {
                return;
            }
            /*
             * now the image view isn't used by another row item
             * get the image then put in memory cache
             */
            Bitmap bitmap = getBitmap(mBitmapToLoad.mUrl);
            mMemoryCache.putImage(mBitmapToLoad.mUrl, bitmap);
            if (isImageViewReused(mBitmapToLoad)) {
                return;
            }
            /*
             * now the image view isn't used by another row item
             * ask the UI thread to display the image
             */
            ImageDisplayer displayer = new ImageDisplayer(bitmap, mBitmapToLoad);
            Activity activity = (Activity)mBitmapToLoad.mImageView.getContext();
            activity.runOnUiThread(displayer);
        }
    }
    
    /**
     * to determine whether this image has been used by another
     * row item, it caused by reuse the convert view in 'getView'
     */
    private boolean isImageViewReused(BitmapToLoad bitmapToLoad){
        String tag = mImageViews.get(bitmapToLoad.mImageView);
        
        if (tag == null || !tag.equals(bitmapToLoad.mUrl)) {
            return true;
        }
        return false;
    }
    
    /**
     * ask the UI thread to set bitmap on list view
     */
    private class ImageDisplayer implements Runnable {
        Bitmap mBitmap;
        BitmapToLoad mPhoto;
        
        public ImageDisplayer(Bitmap bitmap, BitmapToLoad photo) {
        	mBitmap = bitmap;
        	mPhoto = photo;
        }
        
        public void run() {
            if (isImageViewReused(mPhoto)) {
                return;
            }
            if (mBitmap != null && mPhoto != null) {
            	mPhoto.mImageView.setImageBitmap(mBitmap);
            } else {
            	mPhoto.mImageView.setImageResource(R.color.transparent);            	
            }
        }
    }

    /**
     * clear all the image saved in memory and file system 
     */
    public void clearCache() {
        mMemoryCache.clear();
        mFileCache.clear();
    }

}
