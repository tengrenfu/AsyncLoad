/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  
package com.displayjson.asyncload;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;

import android.os.Bundle;
import android.os.AsyncTask;
import android.os.Handler;
import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AbsListView;  
import android.widget.AbsListView.OnScrollListener;  
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;


public class AsyncLoadActivity extends Activity {
    private Button mButton;
    private ListView mListView;
    private TextView mTitle;
    private int mVisibleLastIndex;
    private SimpleAdapter mAdapter;
    private JsonBody mJsonBody;
    private Handler mHandler = new Handler();  
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_async_load);
		
		mVisibleLastIndex = 0;
    	mTitle = (TextView)findViewById(R.id.title);
		mButton = (Button)findViewById(R.id.reload);
		mButton.setOnClickListener(listener);
		mListView = (ListView)findViewById(R.id.list);
		mListView.setOnScrollListener(new scrollListener());
		/* 
		 * start a task to request json data 
		 * and display the contents on the list view
		 */ 
        new requestTask().execute();
	}

	/* 
	 * the click listener for 'Reload' button,   
	 * to start a task to request json data and display 
	 * the contents on the list view when it's clicked
	 */ 
    Button.OnClickListener listener = new Button.OnClickListener() {     
        public void onClick(View v) {
            new requestTask().execute();
        }
    }; 

    private class scrollListener implements OnScrollListener {
    	@Override  
    	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    		mVisibleLastIndex = firstVisibleItem + visibleItemCount;
    	}  
  
    	/** 
    	 * load the contents on the list view 
    	 * when on idle state, here to implement
    	 * to load on demand
    	 */ 
    	@Override  
    	public void onScrollStateChanged(AbsListView view, int scrollState) {  
    		int itemsLastIndex = mAdapter.getCount() - 1;
    		if (scrollState == OnScrollListener.SCROLL_STATE_IDLE 
    				&& mVisibleLastIndex >= itemsLastIndex) {
    			if (mAdapter.getCount() < mJsonBody.getRows().size()) {
    				loadMore();
    			}
    		}
    	}
    }

	/** 
	 * run a thread to add more items to
	 * adapter then notify state changed 
	 */ 
    private void loadMore() {
        mHandler.postDelayed(new Runnable() {
            @Override  
            public void run() {
                loadData();  
                mAdapter.notifyDataSetChanged();
            }  
        }, Constants.DELAY);  
    }  
      
	/** 
	 * add more items to adapter 
	 */ 
    private void loadData() {
        int end;
        int count = mAdapter.getCount();
        if (count >= mJsonBody.getRows().size()) {
        	return;
        }
        end = count + Constants.MAX_ONCE_LOAD;
        if (end > mJsonBody.getRows().size()) {
        	end = mJsonBody.getRows().size();
        }
        for (int i = count; i < end; i++) {
        	mAdapter.addRowItems(mJsonBody.getRows().get(i));
        }  
        
    }  
    
	/** 
	 * get a part of json row item from list
	 */ 
    private List<JsonRowItem> getRangeRowItem(int start, int end) {
    	if (mJsonBody == null) {
    		return null;
    	}
    	int size = mJsonBody.getRows().size();
    	if (start > size || start < 0 || end < start) {
    		return null;
    	}
    	if (end > size) {
    		end = size;
    	}
    	List<JsonRowItem> list = new ArrayList<JsonRowItem>();
    	for (int i = start; i < end; i++) {
    	    list.add(mJsonBody.getRows().get(i));
    	}
    	return list;
    }
    
	/** 
	 * clear all the cache and free the resource used by list view
	 */ 
    @Override
    public void onDestroy() {
    	if (mAdapter != null) {
    		mAdapter.clearCache();
    	}
    	mListView.setAdapter(null);
        super.onDestroy();
    }


	/** 
	 * get json data with http request from server
	 * then parse the response stream with gson
	 * and store the objects into JsonBody
	 */ 
    private JsonBody getJsonFromUrl(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
            conn.setRequestMethod(Constants.HTTP_REQUEST_METHOD);
            conn.setConnectTimeout(Constants.JSON_CONNECT_TIMEOUT);
            conn.setReadTimeout(Constants.JSON_READ_TIME);
            conn.setInstanceFollowRedirects(true);
 
            int response_code = conn.getResponseCode();
            if (response_code == Constants.HTTP_RESPONSE_OK) {
                InputStream is = conn.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(streamReader);
 
                String line = "", JsonStr = "";
                while ((line = bufferReader.readLine()) != null) {
                    JsonStr += line;
                }
                Gson gson = new Gson();
                return gson.fromJson(JsonStr, JsonBody.class);
            } else {
                conn.disconnect();
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

	/** 
	 * Main job task to request json data, parse
	 * and display them on list view
	 */ 
    private class requestTask extends AsyncTask<Object, Void, Void> {
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
        	/*
        	 * clear all the cache first
        	 */
        	if (mAdapter != null) {
        		mAdapter.clearCache();
        	}
        	/*
        	 *  the 'Reload' button is disabled before 
        	 *  the task is over and tell the user loading now
        	 *  with a progress dialog
        	 */
        	mButton.setEnabled(false);
        	progressDialog = ProgressDialog.show(AsyncLoadActivity.this, 
        			Constants.PROGRESS_DIALOG_TITLE, Constants.PROGRESS_DIALOG_DESCRIPT); 
        }
        
        @Override
        protected Void doInBackground(Object... arg0) {
        	mJsonBody = getJsonFromUrl(Constants.JSON_URL);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        	/*
        	 *  finish loading, dismiss the dialog
        	 *  enable the 'Reload' button
        	 */
            if (mJsonBody != null) {
            	/*
            	 *  parse successfully
            	 */
            	if (mJsonBody.getTitle() != null) {
            		mTitle.setText(mJsonBody.getTitle());
            	}
            	if (mJsonBody.getRows() != null) {
            		/*
            		 *  set json data to the adapter
            		 *  then set the adapter to the list view
            		 */
                	mAdapter = new SimpleAdapter(AsyncLoadActivity.this, getRangeRowItem(0, Constants.MAX_ONCE_LOAD));
                	mListView.setAdapter(mAdapter);
            	}
            } else {
            	Toast.makeText(AsyncLoadActivity.this, 
            			Constants.TOAST_TEXT, Toast.LENGTH_SHORT
            			).show();         		
        	}
        	progressDialog.dismiss();
        	mButton.setEnabled(true);
        }
    }

}
