/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  

package com.displayjson.asyncload;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import android.graphics.Bitmap;


public class ImageMemoryCache {
    private long currentSize = 0;
    private long maxSize = Constants.MAX_MEMORY_CACHE_SIZE;
    private Map<String, Bitmap> mCache;

    public ImageMemoryCache() {
    	long size = Runtime.getRuntime().maxMemory() / Constants.JVM_MEMORY_DIV;
    	if (size > maxSize) {
    		setMaxSize(size);
    	}
        mCache = Collections.synchronizedMap(
                new LinkedHashMap<String, Bitmap>(10, 1.5f, true));
    }
    
    private void setMaxSize(long max){
        maxSize = max;
    }

	/**
	 * get the image from mCache by URL
	 * @param  id  URL of the image to get
	 * @return     the image decoded in memory
	 */
    public Bitmap getImage(String id) {
    	if (id == null) {
    		return null;
    	}
        try {        	
            if (!mCache.containsKey(id)) {
                return null;
            }
            return mCache.get(id);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return null;
        }
    }

	/**
	 * get the image size in bytes
	 */
    private long getImageSize(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

	/**
	 * make sure not larger than maxSize
	 * if happened, delete the oldest image stored in the mCache
	 */
    private void checkLimit() {
        if (currentSize > maxSize) {
            Iterator<Entry<String, Bitmap>> iterator = mCache.entrySet().iterator();
            while (iterator.hasNext()) {
                Entry<String, Bitmap> entry = iterator.next();
                currentSize -= getImageSize(entry.getValue());
                iterator.remove();
                if (currentSize <= maxSize) {
                    break;
                }
            }
        }
    }

	/**
	 * put the image into mCache and check cache size limit
	 * @param  id       URL for the image to get
	 * @param  bitmap   the corresponding image decoded to save
	 */
    public void putImage(String id, Bitmap bitmap) {
    	if (id == null || bitmap == null) {
    		return;
    	}
        try {
            if (mCache.containsKey(id)) {
            	currentSize -= getImageSize(mCache.get(id));
            }
            mCache.put(id, bitmap);
            currentSize += getImageSize(bitmap);
            checkLimit();
        } catch (Throwable e){
            e.printStackTrace();
        }
    }
    
	/**
	 * clear all the images in memory
	 */
    public void clear() {
        try {
            mCache.clear();
            currentSize = 0;
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }
    
}
