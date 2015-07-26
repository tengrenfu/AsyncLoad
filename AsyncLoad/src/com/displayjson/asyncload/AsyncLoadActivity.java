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

import android.os.Bundle;
import android.os.AsyncTask;
import android.app.Activity;
import android.app.ProgressDialog;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;


public class AsyncLoadActivity extends Activity {
    private Button mButton;
    private ListView mList;
    private TextView mTitle;
    private SimpleAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_async_load);
		
		mButton = (Button)findViewById(R.id.refresh);
		mButton.setOnClickListener(listener);
		/* 
		 * start a task to request json data 
		 * and load the content into listview
		 */ 
        new requestTask().execute();
	}

	/* 
	 * the click listen of the refresh button  
	 * start a task to request json data 
	 * and load the content into listview
	 * when it's clicked
	 */ 
    Button.OnClickListener listener = new Button.OnClickListener() {     
        public void onClick(View v) {
            new requestTask().execute();
        }    
    }; 

	/** 
	 * here we'd clear the cache
	 * and free the resource for listview
	 */ 
    @Override
    public void onDestroy() {
    	if (mAdapter != null) {
    		mAdapter.clearCache();
    	}
        mList.setAdapter(null);
        super.onDestroy();
    }


	/** 
	 * get json data with http request
	 * then parse the data with gson
	 * called by the task
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
	 * the task for get json data
	 * parsing the data with gson
	 * then display them on listview
	 */ 
    private class requestTask extends AsyncTask<Object, Void, Void> {
        JsonBody jsonData;
        ProgressDialog progressDialog;

        @Override
        protected void onPreExecute() {
        	/*
        	 *  clear the cache first
        	 */
        	if (mAdapter != null) {
        		mAdapter.clearCache();
        	}
        	/*
        	 *  the refresh button is disabled when loading
        	 *  and tell users loading now
        	 */
        	mButton.setEnabled(false);
        	progressDialog = ProgressDialog.show(AsyncLoadActivity.this, 
        			Constants.PROGRESS_DIALOG_TITLE, Constants.PROGRESS_DIALOG_DESCRIPT); 
        }
        
        @Override
        protected Void doInBackground(Object... arg0) {
            jsonData = getJsonFromUrl(Constants.JSON_URL);
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
        	/*
        	 *  finish loading, dismiss the dialog
        	 */
        	progressDialog.dismiss();
            if (jsonData != null) {
            	/*
            	 *  parse successfully
            	 */
            	mTitle = (TextView)findViewById(R.id.title);
            	if (jsonData.getTitle() != null) {
            		mTitle.setText(jsonData.getTitle());
            	}
            	if (jsonData.getRows() != null) {
            		/*
            		 *  set json data to adapter then set to listview
            		 */
                	mList = (ListView)findViewById(R.id.list);
                	mAdapter = new SimpleAdapter(AsyncLoadActivity.this, jsonData.getRows());
                	mList.setAdapter(mAdapter);
            	}
            } else {
            	Toast.makeText(AsyncLoadActivity.this, 
            			Constants.TOAST_TEXT, Toast.LENGTH_SHORT
            			).show(); 
            }
        	mButton.setEnabled(true);
        }
    }

}
