/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  

package com.displayjson.asyncload;


public class JsonRowItem {
	/* 
	 * just the same with 'title' in every 'rows' array of the json response
	 */
	private String title;
	/* 
	 * just the same with 'description' in every 'rows' array of the json response
	 */
	private String description;
	/*
	 *  just the same with 'imageHref' in every 'rows' array of the json response
	 */
	private String imageHref;
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getImageHref() {
		return imageHref;
	}
	
	public void setImageHref(String imageHref) {
		this.imageHref = imageHref;
	}
	
}
