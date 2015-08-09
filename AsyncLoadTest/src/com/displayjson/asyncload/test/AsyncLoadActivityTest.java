/**
 * 
 */
package com.displayjson.asyncload.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import android.test.ActivityInstrumentationTestCase2;
import android.widget.ListView;
import android.widget.TextView;

import com.displayjson.asyncload.*;


public class AsyncLoadActivityTest extends
		ActivityInstrumentationTestCase2<AsyncLoadActivity> {
	private AsyncLoadActivity mActivity;
    private ListView mList;
    private TextView mTitle;

	public AsyncLoadActivityTest(Class<AsyncLoadActivity> activityClass) {
		super(activityClass);
	}

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	protected void setUp() throws Exception {
		super.setUp();
		mActivity = getActivity();
		mList = (ListView)mActivity.findViewById(com.displayjson.asyncload.R.id.list);
		mTitle = (TextView)mActivity.findViewById(com.displayjson.asyncload.R.id.title);
	}

	@After
	protected void tearDown() throws Exception {
		super.tearDown();
		
		mActivity.finish();  
        try {  
            super.tearDown();  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
    }

	@Test
	public void testOnDestroy() {
		assertEquals(null, mList.getAdapter());
		assertTrue(mTitle.getTextSize() > 0);
	}

	@Test
	public void testOnCreateBundle() {
		assertEquals("", mTitle.getText()); 
	}

}
