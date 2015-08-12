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
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;


public class AsyncLoadActivity extends Activity implements OnUpdateListener {
    private EnhancedListView mListView;
    private TextView mTitle;
    private SimpleAdapter mAdapter;
    private JsonBody mJsonBody;
    private long mLastModified;
    private Handler mHandler = new Handler();
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_async_load);
		mLastModified = 0;
		
		initView();
		/* 
		 * start a task to request json data 
		 * and display the contents on the list view
		 */ 
        new requestTask().execute();
	}

	private void initView() {
		mTitle = (TextView) findViewById(R.id.title);
		mListView = (EnhancedListView) findViewById(R.id.list);
		mListView.setOnRefreshListener(this);
	}

	/**
	 * after json data is parsed, set row items to list view
	 */
	private void setToListView(JsonBody jsonBody) {
		if (jsonBody != null) {
			/*
			 * parse successfully
			 */
			if (jsonBody.getTitle() != null) {
				mTitle.setText(jsonBody.getTitle());
			} else {
				mTitle.setText("");
			}
			if (jsonBody.getRows() != null && mListView != null) {
				/*
				 * set json data to the adapter and set the adapter to the list
				 * view with a range
				 */
				if (mAdapter == null) {
					mAdapter = new SimpleAdapter(AsyncLoadActivity.this,
							getRangeRowItem(0, Constants.MAX_ONCE_LOAD));
					mListView.setAdapter(mAdapter);
				} else {
					mAdapter.setAdapterData(getRangeRowItem(0, Constants.MAX_ONCE_LOAD));
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
                mListView.hideFooterView();
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
        	mListView.setAllLoaded(true);
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
          	conn.setIfModifiedSince(mLastModified);
 
            int response_code = conn.getResponseCode();
            if (response_code == Constants.HTTP_RESPONSE_OK) {
            	mLastModified = conn.getLastModified();
                InputStream is = conn.getInputStream();
                InputStreamReader streamReader = new InputStreamReader(is);
                BufferedReader bufferReader = new BufferedReader(streamReader);
 
                String line = "", JsonStr = "";
                while ((line = bufferReader.readLine()) != null) {
                    JsonStr += line;
                }
                Gson gson = new Gson();
                return gson.fromJson(JsonStr, JsonBody.class);
            } else if (response_code == Constants.HTTP_RESPONSE_NOCHANG) {
                conn.disconnect();
                return null;
            } else {
                Toast.makeText(AsyncLoadActivity.this, Constants.TOAST_TEXT, Toast.LENGTH_SHORT).show();
                conn.disconnect();
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
//          e.printStackTrace();
            /* it's  workaround for disconnecting this json server in china, begin */
        	String jsonResponse = "{\"title\":\"About Canada\",\"rows\":[{\"title\":\"Beavers\",\"description\":\"Beavers are second only to humans in their ability to manipulate and change their environment. They can measure up to 1.3 metres long. A group of beavers is called a colony\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/11.jpg\"},{\"title\":\"Flag\",\"description\":null,\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/12.jpg\"},{\"title\":\"Transportation\",\"description\":\"It is a well known fact that polar bears are the main mode of transportation in Canada. They consume far less gas and have the added benefit of being difficult to steal.\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/10.jpg\"},{\"title\":\"Hockey Night in Canada\",\"description\":\"These Saturday night CBC broadcasts originally aired on radio in 1931. In 1952 they debuted on television and continue to unite (and divide) the nation each week.\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/9.jpg\"},{\"title\":\"Eh\",\"description\":\"A chiefly Canadian interrogative utterance, usually expressing surprise or doubt or seeking confirmation.\",\"imageHref\":null},{\"title\":\"Housing\",\"description\":\"Warmer than you might think.\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/8.jpg\"},{\"title\":\"Public Shame\",\"description\":\" Sadly it's true.\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/7.jpg\"},{\"title\":null,\"description\":null,\"imageHref\":null},{\"title\":\"Space Program\",\"description\":\"Canada hopes to soon launch a man to the moon.\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/6.jpg\"},{\"title\":\"Meese\",\"description\":\"A moose is a common sight in Canada. Tall and majestic, they represent many of the values which Canadians imagine that they possess. They grow up to 2.7 metres long and can weigh over 700 kg. They swim at 10 km/h. Moose antlers weigh roughly 20 kg. The plural of moose is actually 'meese', despite what most dictionaries, encyclopedias, and experts will tell you.\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/5.jpg\"},{\"title\":\"Geography\",\"description\":\"It's really big.\",\"imageHref\":null},{\"title\":\"Kittens...\",\"description\":\"?are illegal. Cats are fine.\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/4.jpg\"},{\"title\":\"Mounties\",\"description\":\"They are the law. They are also Canada's foreign espionage service. Subtle.\",\"imageHref\":\"http://sucai.qqjay.com/fengmian/201104/tupian1/3.jpg\"},{\"title\":\"Language\",\"description\":\"Nous parlons tous les langues importants.\",\"imageHref\":null}]}";
            Gson gson = new Gson();
            return gson.fromJson(jsonResponse, JsonBody.class);
            /* it's  workaround for disconnecting this json server in china, end */
        }
        return null;
    }

	/** 
	 * the task to request json data, parse into
	 * jsonBody, then set to adapter for list view to
	 * display the row item
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
			 * before the task is over, tell
			 * the user loading now with a progress dialog
			 */
			progressDialog = ProgressDialog.show(AsyncLoadActivity.this,
								Constants.PROGRESS_DIALOG_TITLE, 
								Constants.PROGRESS_DIALOG_DESCRIPT);
        }
        
        @Override
        protected Void doInBackground(Object... arg0) {
        	mJsonBody = getJsonFromUrl(Constants.JSON_URL);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
			/*
			 * finish loading, dismiss the dialog
			 */
			if (!AsyncLoadActivity.this.isFinishing() && progressDialog.isShowing()) {
				progressDialog.dismiss();
			}
    		/*
    		 * set json object to adapter then to list view
    		 */
            if (mJsonBody != null) {
                setToListView(mJsonBody);
				mAdapter.notifyDataSetChanged();
				mListView.hideHeaderView();
				mListView.setAllLoaded(false);
            }
        }
    }

    /**
     * pull down the list view to request the URL
     */
    @Override  
    public void onPullToUpdate() {
    	new requestTask().execute();
    }
    
    /**
     * load more when pulling up the list view at the bottom
     */
    @Override  
    public void onLoadingMore() {
    	loadMore();
    }
    
}
