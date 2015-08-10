/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  
package com.displayjson.asyncload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.BufferedHttpEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.widget.ImageView;


public class ImageLoader {
    private ImageMemoryCache mMemoryCache;
    private FileCache mFileCache;
    private Map<ImageView, String> mImageViews;
    private Map<String, BitmapLoader> mThreads;
    private ExecutorService executorService; 
    
    public ImageLoader(Context context) {
    	mImageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    	mThreads = new HashMap<String, BitmapLoader>();
    	mMemoryCache = new ImageMemoryCache();
        mFileCache = new FileCache(context);
        /*
         * a thread pool is ready for downloading, decoding and displaying
         * the images on the list view 
         */
        executorService = Executors.newFixedThreadPool(Constants.THREAD_POOL_SIZE);
    }
    
    /**
     * if URL has not be used to download and decode the image 
     * the queue a task to get the image from file system
     * or from web site, then decode and display it on the list view
     */
    private void queueImage(String url, ImageView imageView) {
        BitmapToLoad bitmapToLoad = new BitmapToLoad(url, imageView);
        BitmapLoader loader = new BitmapLoader(bitmapToLoad);
        if (url != null && imageView != null && needRunNewThread(imageView)) {
        	executorService.submit(loader);
        	mThreads.put(url, loader);
        }
    }

    /**
     * if the working threads pools contain the URL has been 
     * set to the image view, return false, otherwise return true
     */
    private boolean needRunNewThread(ImageView imageView) { 
        boolean ret = true; 
        if (imageView != null) {
            String url = (String)imageView.getTag();
            if (isThreadsContains(url)) {
                ret = false; 
            } 
        } 
        return ret; 
    } 

    /**
     *  the working threads pools contain the URL
     *  return true, or return false
     */
    private boolean isThreadsContains(String url) {
        boolean ret = false; 
        if (mThreads != null && mThreads.get(url) != null) {
            ret = true; 
        } 
        return ret; 
    } 

    /**
     *  remove the URL from the working threads pools
     *  because the corresponding work has been done.
     */
    private void removeThreadForCache(String url) { 
        if (url != null && mThreads != null && mThreads.get(url) != null) { 
            mThreads.remove(url); 
        } 
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
        		imageView.setTag("");
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
        } catch(Exception e) {
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
        Bitmap iamge = decodeImage(file, Constants.REQUIRED_IMAGE_WIDTH, 
        									Constants.REQUIRED_IMAGE_HEIGHT);
        if (iamge != null) {
            return iamge;
        }        
        /*
         * get image from web site and save it 
         * in file system then decode the image
         */
        try {
            Bitmap bitmap = null;
            HttpGet request = new HttpGet(url);
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = (HttpResponse) client.execute(request);
            HttpEntity entity = response.getEntity();
            BufferedHttpEntity bufferedEntity = new BufferedHttpEntity(entity);
            
            InputStream src = bufferedEntity.getContent();            
            OutputStream dst = new FileOutputStream(file);
            copyStream(src, dst);
            dst.close();
            
            bitmap = decodeImage(bufferedEntity, 160, 120);
            return bitmap;
        } catch (Throwable e) {
           if (e instanceof OutOfMemoryError) {
               mMemoryCache.clear();
           }
        }
        return null;
    }

    /**
     * decode the image with input size
     * @param bitmap  the image to be resized
     * @param width   new width for the image
     * @param height  new height for the image
     * @return  new size image
     */
    private Bitmap resizeImage(Bitmap bitmap, int width, int height) {
    	if (bitmap == null || width <=0 || height <= 0) {
    		return null;
    	}
        int orgWidth = bitmap.getWidth();
        int orgHeight = bitmap.getHeight();
        int newWidth = width;
        int newHeight = height;
 
        float scaleWidth = ((float) newWidth) / orgWidth;
        float scaleHeight = ((float) newHeight) / orgHeight;
 
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, orgWidth, orgHeight, matrix, true);    	
    }
    
    private Bitmap decodeImage(File file, int width, int height) {
    	Bitmap bitmapOrg = null;
    	try {
    		bitmapOrg = BitmapFactory.decodeStream(new FileInputStream(file), 
        						null, new BitmapFactory.Options());
        } catch (FileNotFoundException e) {
        }
    	return resizeImage(bitmapOrg, width, height);
    }

    private Bitmap decodeImage(BufferedHttpEntity entity, int width, int height) {
    	Bitmap bitmapOrg = null;
    	try {
    		bitmapOrg = BitmapFactory.decodeStream(entity.getContent(), 
        						null, new BitmapFactory.Options());
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
        }
    	return resizeImage(bitmapOrg, width, height);
    }
        
    /**
     * It's a helper class for BitmapLoader
     */
    private class BitmapToLoad {
        public String mUrl;
        public ImageView mImageView;
        
        public BitmapToLoad(String url, ImageView view) {
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
            /*
             * remove the URL from the working thread pools
             */
            removeThreadForCache(mBitmapToLoad.mUrl);
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
    private boolean isImageViewReused(BitmapToLoad bitmapToLoad) {
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
            	mPhoto.mImageView.setTag("");
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
        mImageViews.clear();
        mThreads.clear();
    }

}
