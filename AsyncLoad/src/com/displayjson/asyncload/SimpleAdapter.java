/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  
package com.displayjson.asyncload;

import java.util.List;

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

    public SimpleAdapter(Activity activity, List<JsonRowItem> list) {
    	mList = list;
    	mActivity = activity;
    	mInflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    	mImageLoader = new ImageLoader(mActivity.getApplicationContext());
    }
    
	@Override
	public int getCount() {
    	return mList.size();
	}

	@Override
	public Object getItem(int position) {
        return mList.get(position);
	}

	@Override
	public long getItemId(int position) {
        return position;
	}

	/**
	 * once a row of the list view is visible, 
	 * this method will be called
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
    	ViewHolder holder = null;
    	JsonRowItem item = mList.get(position);
    	
    	if (convertView == null) {
        	convertView = mInflater.inflate(R.layout.row_item, null);
            holder = new ViewHolder();
            holder.rowTitle = (TextView)convertView.findViewById(R.id.row_title);
            holder.descritpion = (TextView)convertView.findViewById(R.id.description);
            holder.image = (ImageView)convertView.findViewById(R.id.image);
            convertView.setTag(holder);
            
        } else {
        	holder = (ViewHolder)convertView.getTag();
        }

        if (item.getTitle() != null) {
        	holder.rowTitle.setText(item.getTitle());
        } else {
        	holder.rowTitle.setText("");
        }
        if (item.getDescription() != null) {
        	holder.descritpion.setText(item.getDescription());
        } else {
        	holder.descritpion.setText("");
        }
        if (item.getImageHref() != null) {
        	/*
        	 * set URL as tag to the image view, later image loader finish
        	 * downloading the image and display it on the list view according
        	 * to the tag, thus avoid to display the image on another row 
        	 * and here the imageloader download and display the image in another
        	 * thread to keep the UI not blocking
        	 */
        	holder.image.setTag(item.getImageHref());
            mImageLoader.displayImageOnView(item.getImageHref(), holder.image);
        } else {
        	/*
        	 * set the image as transparent color if this item has no image
        	 */
        	holder.image.setImageResource(R.color.transparent);
        }
        
        return convertView;
	}

	/**
	 * add more items to the list 
	 */
    public void addRowItems(JsonRowItem item) {
        mList.add(item);
    }

	/**
	 * clear all the images' saved
	 * whatever in memory or in file system 
	 */
    public void clearCache() {
    	if (mImageLoader != null) {
    		mImageLoader.clearCache();
    	}
    }
    
    /**
     * item view holder for convert view
     */
    private class ViewHolder {
        public TextView rowTitle;
        public TextView descritpion;
        public ImageView image;
    }

}
