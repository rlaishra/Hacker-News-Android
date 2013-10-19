package com.rickylaishram.hackernews;

import android.content.Context;

class ArticlesList {
    public String points;
    public String title;
    public String submitter;
    public String comments;
	public int draw;
	public String upvote_url;
	public String comment_url;
	public String article_url;
	public String login_cookie;
	public Context parent;
	public String domain;
	
    public ArticlesList(){}
    
    public ArticlesList(String points, String title, String submitter, String comments, int draw, String upvote_url, String article_url, String comment_url, String login_cookie, Context parent, String domain) {
        this.points = points;
        this.title = title;
        this.submitter = submitter;
        this.comments = comments;
        this.draw = draw;
        this.upvote_url = upvote_url;
        this.article_url = article_url;
        this.comment_url = comment_url;
        this.login_cookie = login_cookie;
        this.parent = parent;
        this.domain = domain;
    }
}