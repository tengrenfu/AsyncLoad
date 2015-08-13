package com.displayjson.asyncload.test;


import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import android.test.AndroidTestCase;
import com.displayjson.asyncload.*;

public class ImageLoaderTest extends AndroidTestCase {

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
	public void testImageLoader() throws Throwable {
		ImageLoader loader = new ImageLoader(getContext(), null);
		assertTrue(loader != null);
	}

	@Test
	public void testDisplayImageOnView() throws Throwable {
		ImageLoader loader = new ImageLoader(getContext(), null);
		loader.displayImageOnView(null, null);
		loader.displayImageOnView("", null);
	}

	@Test
	public void testClearCache() throws Throwable {
		ImageLoader loader = new ImageLoader(getContext(), null);
		loader.clearCache();
	}

}
