package com.displayjson.asyncload.test;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.test.AndroidTestCase;
import com.displayjson.asyncload.*;

public class ImageMemoryCacheTest extends AndroidTestCase {
	private final static String url = "http://sucai.qqjay.com/fengmian/201104/tupian1/7.jpg";

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
	public void testImageMemoryCache() throws Throwable {
		assertTrue((new ImageMemoryCache()) != null);
	}

	@Test
	public void testGetImage() throws Throwable {
		ImageMemoryCache cache = new ImageMemoryCache();
		cache.getImage(null);
		cache.getImage("");
		cache.getImage(url);
	}

	@Test
	public void testPutImage() throws Throwable {
		ImageMemoryCache cache = new ImageMemoryCache();
		cache.putImage(null, null);
		cache.putImage("", null);
		cache.putImage(url, null);
		Bitmap b = BitmapFactory.decodeFile(url);
		cache.putImage(null, b);
		cache.putImage("", b);
		cache.putImage(url, b);
	}

	@Test
	public void testClear() throws Throwable {
		ImageMemoryCache cache = new ImageMemoryCache();
		cache.clear();
	}

}
