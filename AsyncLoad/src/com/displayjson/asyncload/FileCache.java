/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  

package com.displayjson.asyncload;

import java.io.File;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;

import android.content.Context;


public class FileCache {
    private File mCacheDir;
    
    public FileCache(Context context) {
    	/*
    	 * create the path to save the images 
    	 * in SD card or cache directory
    	 */
        if (android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState())) {
        	mCacheDir = new File(android.os.Environment.getExternalStorageDirectory(), Constants.CACHE_DIR);
        } else {
        	mCacheDir = context.getCacheDir();
        }
        if (!mCacheDir.exists()) {
        	mCacheDir.mkdirs();
        }
    }
    
	/**
	 * get the image file with URL in file system
	 * @param url  URL for the image
	 * @return     the path for the image saved in file system
	 */
    public File getFile(String url) {
    	String file_name;
    	try {
    		file_name = URLEncoder.encode(url, Constants.ENCODE_CHARSET);
    	} catch (UnsupportedEncodingException e) {
    		file_name = String.valueOf(url.hashCode());    		
    	}
        File file = new File(mCacheDir, file_name);
        return file;
        
    }
    
	/**
	 * clear all the images saved in files system
	 */
    public void clear() {
    	try {
    		File[] files = mCacheDir.listFiles();
    		if (files == null) {
    			return;
    		}
    		for (File file:files) {
    			file.delete();
    		}
        } catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

}
