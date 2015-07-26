package com.displayjson.asyncload.test;


import java.io.File;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import android.test.AndroidTestCase;
import com.displayjson.asyncload.*;

public class FileCacheTest extends AndroidTestCase {
	private final static String url = "http://sucai.qqjay.com/fengmian/201104/tupian1/6.jpg";
			
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	protected void setUp() throws Exception {
		super.setUp();
	}

	@After
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testFileCache() throws Throwable {
		FileCache cache = new FileCache(getContext());		
		File dir, sdExternalDir = android.os.Environment.getExternalStorageDirectory();
        if (sdExternalDir.equals(android.os.Environment.MEDIA_MOUNTED)) {
        	dir = new File(sdExternalDir, Constants.CACHE_DIR);
        } else {
        	dir = getContext().getCacheDir();
        }
		assertTrue(dir.exists());
	}

	@Test
	public void testGetFile() throws Throwable {
		FileCache cache = new FileCache(getContext());
		cache.getFile(url);
	}

	@Test
	public void testClear() throws Throwable {
		FileCache cache = new FileCache(getContext());
		cache.clear();
	}

}
