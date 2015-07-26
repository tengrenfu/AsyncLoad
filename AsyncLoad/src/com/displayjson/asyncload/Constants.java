/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  

package com.displayjson.asyncload;


/** 
 * Constant variables are put here
 */ 
public final class Constants {
	
	private Constants () {}  // this class can't be instanced
	
	public static final String JSON_URL = "https://dl.dropboxusercontent.com/u/746330/facts.json";
	public static final String  PROGRESS_DIALOG_TITLE = "Loading";
	public static final String  PROGRESS_DIALOG_DESCRIPT = "Please wait...";	
	public static final String  TOAST_TEXT = "Failed, please try again.";
	public static final String  HTTP_REQUEST_METHOD = "GET";    //do NOT change this value
	public static final String  CACHE_DIR = "ImageCache";
	public static final String  ENCODE_CHARSET = "UTF-8";
	
	public static final int  MAX_ONCE_LOAD = 10;
	public static final int  DELAY = 1000;
	public static final int  JSON_CONNECT_TIMEOUT = 3000;
	public static final int  JSON_READ_TIME = 3000;
	public static final int  HTTP_RESPONSE_OK = 200;    //do NOT change this value
	public static final int  THREAD_POOL_SIZE = 5;
	public static final int  IMAGE_CONNECT_TIMEOUT = 30000;
	public static final int  IMAGE_READ_TIME = 30000;
	public static final int  BUFFER_SIZE = 1024;    // 2^n is better
	public static final int  REQUIRED_IMAGE_SIZE = 80;
	public static final int  MAX_MEMORY_CACHE_SIZE = 1048576;
	public static final int  JVM_MEMORY_DIV = 4;
	
}
