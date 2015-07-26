/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  
package com.displayjson.asyncload;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class SimpleAdapter extends BaseAdapter {	
    private Activity mActivity;
    private List<JsonRowItem> mList;
    private ImageLoader mImageLoader; 
    private static LayoutInflater mInflater = null;
    private Map<Integer, View> mViewMap = new HashMap<Integer, View>();

    public SimpleAdapter(Activity activity, List<JsonRowItem> list) {
    	mActivity = activity;
    	mList = list;
    	mInflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	mImageLoader = new ImageLoader(mActivity.getApplicationContext());
    }
    
	@Override
	public int getCount() {
    	return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
        return arg0;
	}

	@Override
	public long getItemId(int arg0) {
        return arg0;
	}

	/**
	 * once a row of the list view is visible, 
	 * this method will be called
	 */
	@Override
	public View getView(int arg0, View arg1, ViewGroup arg2) {
    	ViewHolder holder = null;
    	View rowView = mViewMap.get(arg0);
    	
    	if (rowView == null) {
        	JsonRowItem item = mList.get(arg0);
        	rowView = mInflater.inflate(R.layout.row_item, null);
            holder = new ViewHolder();
            holder.rowTitle = (TextView)rowView.findViewById(R.id.row_title);
            holder.descritpion = (TextView)rowView.findViewById(R.id.description);
            holder.image = (ImageView)rowView.findViewById(R.id.image);
            rowView.setTag(holder);
            
            if (item.getTitle() != null) {
            	holder.rowTitle.setText(item.getTitle());
            }
            if (item.getDescription() != null) {
            	holder.descritpion.setText(item.getDescription());
            }
            if (item.getImageHref() != null) {
            	/*
            	 * set URL as tag to the image view, later image loader finish
            	 * downloading the image and display the image on the view according
            	 * to the tag, it avoid to display the image to another row 
            	 * and here image loader download and display the image in other
            	 * threads to keep the UI not blocking
            	 */
            	holder.image.setTag(item.getImageHref());
                mImageLoader.displayImageOnView(item.getImageHref(), holder.image);
            }
            
            mViewMap.put(arg0, rowView);
        } else {
        	holder = (ViewHolder)rowView.getTag();
        }

        return rowView;
	}

	/**
	 * clear all the image cache
	 * whatever in memory or in SD card 
	 */
    public void clearCache() {
    	if (mImageLoader != null) {
    		mImageLoader.clearCache();
    	}
    }
    
    public class ViewHolder {
        public TextView rowTitle;
        public TextView descritpion;
        public ImageView image;
    }

}
