/* 
 * Created by Cory Teng  email:tengrenfu@163.com
 * July 25, 2015 
 */  

package com.displayjson.asyncload;

import java.util.List;


public class JsonBody {	
	/*
	 * just the same with 'title' in the json response
	 */
	private String title;
	/*
	 * just the same with 'rows' in the json response, it's an array
	 */
	private List<JsonRowItem> rows;
	
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public List<JsonRowItem> getRows() {
		return rows;
	}

	public void setTitle(List<JsonRowItem> rows) {
		this.rows = rows;
	}
	
}
