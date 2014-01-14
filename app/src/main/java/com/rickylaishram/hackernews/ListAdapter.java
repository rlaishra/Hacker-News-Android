package com.rickylaishram.hackernews;

import java.io.IOException;
import java.util.Vector;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.jsoup.nodes.Document;

import com.rickylaishram.hackernews.R;
import com.rickylaishram.hackernews.R.anim;
import com.rickylaishram.hackernews.R.drawable;
import com.rickylaishram.hackernews.R.id;
import com.rickylaishram.hackernews.db.BookMarksDataSource;
import com.rickylaishram.hackernews.db.LinksDataSource;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ListAdapter extends ArrayAdapter<ArticlesList>{

    Context context; 
    int layoutResourceId;    
    Vector<ArticlesList> data = new Vector<ArticlesList>();
    
    public ListAdapter(Context context, int layoutResourceId, Vector<ArticlesList> data) 
    {
        super(context, layoutResourceId, data);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) 
    {
        View row = convertView;
        //Holder holder = null;
        final Holder holder = new Holder();
        
        //if(row == null){
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row 					= inflater.inflate(layoutResourceId, parent, false);
            
            //holder = new Holder();
            holder.points 			= (TextView) row.findViewById(R.id.points);
            holder.title 			= (TextView) row.findViewById(R.id.title);
            holder.submiter 		= (TextView) row.findViewById(R.id.submitter);
            holder.comments 		= (TextView) row.findViewById(R.id.comments);
            holder.draw 			= (ImageView) row.findViewById(R.id.left_image);
            holder.upvote 			= (ImageButton) row.findViewById(R.id.front_upvote);
            holder.domain 			= (TextView) row.findViewById(R.id.domain);
            holder.bookmark			= (ImageView) row.findViewById(R.id.bookmark);
            
            row.setTag(holder);
        //} else {
            //holder = (Holder) row.getTag();
        //}

        final ArticlesList item = data.elementAt(position);
        holder.points.setText(item.points);
        holder.title.setText(item.title);
        holder.submiter.setText(item.submitter);
        holder.comments.setText(item.comments);
        holder.draw.setImageResource(item.draw);
        holder.domain.setText(item.domain);
        
        if (item.upvote_url.equals("")) {
        	holder.upvote.setImageResource(R.drawable.button_up_disabled);
        	holder.draw.setImageResource(R.drawable.blue);
        	holder.upvote.setEnabled(false);
        } else if (!item.upvote_url.contains("&auth=")) {
        	holder.upvote.setImageResource(R.drawable.button_up_disabled);
        	holder.upvote.setEnabled(false);
        	holder.draw.setImageResource(R.drawable.white);
        }
        
        if(!item.article_url.startsWith("http")) {
        	if(!item.comment_url.isEmpty()) {
        		holder.title.setEnabled(false);
        		holder.title.setTextColor(Color.BLACK);
        	} else {
        		holder.title.setEnabled(true);
        		holder.title.setTextColor(Color.BLACK);
        	}
        }
        
        //check if article is seen
        LinksDataSource links = new LinksDataSource(context);
        links.open();
        Boolean link_seen = links.isLinkSeen(item.article_url);
        links.close();
        if(link_seen) {
        	holder.title.setTextColor(Color.GRAY);
        	holder.points.setTextColor(Color.GRAY);
            holder.submiter.setTextColor(Color.GRAY);
            holder.comments.setTextColor(Color.GRAY);
            holder.domain.setTextColor(Color.GRAY);
        } else {
        	holder.title.setTextColor(Color.BLACK);
        	holder.points.setTextColor(Color.BLACK);
            holder.submiter.setTextColor(Color.BLACK);
            holder.comments.setTextColor(Color.GRAY);
            holder.domain.setTextColor(Color.GRAY);
        }
        
        //check if bookmarked
        BookMarksDataSource bm 	= new BookMarksDataSource(context);
        bm.open();
        Boolean isBookmarked	= bm.isBookmarked(item.comment_url);
        bm.close();
        if(isBookmarked) {
        	holder.bookmark.setImageResource(R.drawable.ic_star_filled);
        } else {
        	holder.bookmark.setImageResource(R.drawable.ic_star_empty);
        }
        
        //if (!item.article_url.startsWith("http") && item.comment_url.isEmpty()) {
        //	holder.title.setEnabled(true);
        //}
        
        holder.title.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				SharedPreferences settings 	= item.parent.getSharedPreferences("Settings", Context.MODE_PRIVATE);
				Boolean e_browser 			= settings.getBoolean("external_browser", false);
				
				//add link to read
				LinksDataSource links = new LinksDataSource(context);
		        links.open();
		        links.addLink(item.article_url);
		        links.close();
				
				if (!e_browser) {
					Intent mIntent=new Intent(item.parent,Article.class);
					Bundle bundle=new Bundle();
					bundle.putString("article_url", item.article_url);
	  				bundle.putString("comment_url", item.comment_url);
	  				bundle.putString("submission_title", item.title);
	  				bundle.putString("login_cookie", item.login_cookie);
	  				mIntent.putExtras(bundle);
	  				item.parent.startActivity(mIntent);
	  				((Activity) item.parent).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				} else {
	  				String url = item.article_url;
	  				
	  				if (!url.startsWith("http")) {
	  					url = "http://news.ycombinator.com/" + url;
	  				}
	  				
	  				Intent i = new Intent(Intent.ACTION_VIEW);
	  				i.setData(Uri.parse(url));
	  				item.parent.startActivity(i);
	  				((Activity) item.parent).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				}
			}
		});
        
        holder.title.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
					holder.title.setBackgroundResource(android.R.color.holo_blue_light);
				} else {
					holder.title.setBackgroundResource(android.R.color.transparent);
				}
				
				return false;
			}
		});
        
        holder.comments.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mIntent=new Intent(item.parent,Comments.class);
				Bundle bundle=new Bundle();
				bundle.putString("article_url", item.article_url);
  				bundle.putString("comment_url", item.comment_url);
  				bundle.putString("submission_title", item.title);
  				bundle.putString("login_cookie", item.login_cookie);
  				mIntent.putExtras(bundle);
  				item.parent.startActivity(mIntent);
  				//((Activity) item.parent).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
  				((Activity) item.parent).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			}
		});
        
        holder.comments.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
					holder.comments.setBackgroundResource(android.R.color.holo_blue_light);
				} else  {
					holder.comments.setBackgroundResource(android.R.color.transparent);
				}
				
				return false;
			}
		});
        
        holder.upvote.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				PingServer ping = new PingServer();
				ping.execute(new String[]{ "http://news.ycombinator.com/" + item.upvote_url, item.login_cookie });
				
				holder.upvote.setEnabled(false);
				holder.upvote.setImageResource(R.drawable.button_up_disabled);
				holder.draw.setImageResource(R.drawable.blue);
				holder.points.setText((Integer.parseInt(item.points) + 1) + "");
				
				item.upvote_url = "";
				item.points = (Integer.parseInt(item.points) + 1) + "";
			}
		});
        
        holder.upvote.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == android.view.MotionEvent.ACTION_DOWN) {
					holder.upvote.setBackgroundResource(android.R.color.holo_blue_light);
				} else {
					holder.upvote.setBackgroundResource(android.R.color.transparent);
				}
				
				return false;
			}
		});
        
        holder.bookmark.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				BookMarksDataSource bm 	= new BookMarksDataSource(context);
		        bm.open();
		        
		        if(bm.isBookmarked(item.comment_url)) {
		        	bm.remove(item.comment_url);
		        	holder.bookmark.setImageResource(R.drawable.ic_star_empty);
		        } else {
		        	bm.add(item.article_url, item.comment_url, item.title);
		        	holder.bookmark.setImageResource(R.drawable.ic_star_filled);
		        }
				bm.close();
			}
		});
        
        return row;
    }
    
    static class Holder
    {
        public TextView submiter;
		public TextView comments;
		public TextView title;
		public TextView domain;
		public TextView points;
		public ImageView draw;
		public ImageView upvote;
		public ImageView bookmark;
    }
    
    //Asynchronous server ping
  	public class PingServer extends AsyncTask<String, Void, Document> {
  	
  		public PingServer(){}
  		@Override
  		protected Document doInBackground(String... params) {
  			String link = params[0];
  			String cookie = params[1];
  			
  			try {
  				HttpClient client = new DefaultHttpClient();
  		           HttpGet request = new HttpGet(link);
  		           request.setHeader("Cookie", cookie);
  		           client.execute(request);
  			} catch (ClientProtocolException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			} catch (IOException e) {
  				// TODO Auto-generated catch block
  				e.printStackTrace();
  			}
  			
  			return null;
  		}
  		
  		protected void onPostExecute(Document doc) {
  			
  		}
  	}
}