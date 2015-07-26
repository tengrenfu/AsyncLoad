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
    	 * create the directory to storage
    	 * the image in SD card
    	 */
    	File sdExternalDir = android.os.Environment.getExternalStorageDirectory();
        if (sdExternalDir.equals(android.os.Environment.MEDIA_MOUNTED)) {
        	mCacheDir = new File(sdExternalDir, Constants.CACHE_DIR);
        } else {
        	mCacheDir = context.getCacheDir();
        }
        if (!mCacheDir.exists()) {
        	mCacheDir.mkdirs();
        }
    }
    
	/**
	 * get the image file with encode name
	 */
    public File getFile(String url){
    	String file_name;
    	try {
    		file_name = URLEncoder.encode(url, Constants.ENCODE_CHARSET);
    	} catch (UnsupportedEncodingException e) {
    		file_name = String.valueOf(url.hashCode());    		
    	}
        File file = new File(mCacheDir, file_name);  //???
        return file;
        
    }
    
	/**
	 * clear all the files stored
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
