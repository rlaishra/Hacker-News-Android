package com.rickylaishram.hackernews;

public class BookmarkItems {
	public String article_url;
	public String comment_id;
	public String day;
	public String month;
	public String year;
	public String title;
	
	public BookmarkItems() {}
	
	public void setItems(String url, String id, String day, String month, String year, String title) {
		this.article_url	= url;
		this.comment_id		= id;
		this.day			= day;
		this.month			= month;
		this.year			= year;
		this.title			= title;
	}
}
