package com.rickylaishram.hackernews;

import android.text.Spanned;

class CustomCommentListItem {
    public Integer image;
    public String submitter;
    public Spanned comment;
	public int draw;
	public String time;
	public Integer padding;
	public String collapse; //no, yes, header
    
    public CustomCommentListItem(){
    }
    
    public CustomCommentListItem(Integer image, String submitter, Spanned comments, 
    		int draw, String time, Integer padding, String collapse) {
        this.image = image;
        this.submitter = submitter;
        this.comment = comments;
        this.draw = draw;
        this.time = time;
        this.padding = padding;
        this.collapse = collapse;
    }
}
