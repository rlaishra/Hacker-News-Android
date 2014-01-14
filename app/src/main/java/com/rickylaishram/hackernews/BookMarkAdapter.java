package com.rickylaishram.hackernews;

import java.util.Vector;

import com.rickylaishram.hackernews.db.BookMarksDataSource;
import com.rickylaishram.hackernews.db.LinksDataSource;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BookMarkAdapter extends ArrayAdapter<BookmarkItems>{
	
    Context context; 
    int layoutResourceId;    
    Vector<BookmarkItems> data 	= new Vector<BookmarkItems>();
	
	public BookMarkAdapter(Context context, int layoutResourceId, Vector<BookmarkItems> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId 	= layoutResourceId;
        this.context 			= context;
        this.data 				= data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row 				= convertView;
        final Holder holder 	= new Holder();
        
        LayoutInflater inflater = ((Activity)context).getLayoutInflater();
        row 					= inflater.inflate(layoutResourceId, parent, false);
            
        holder.day 				= (TextView) row.findViewById(R.id.day);
        holder.month 			= (TextView) row.findViewById(R.id.month);
        holder.year 			= (TextView) row.findViewById(R.id.year);
        holder.star 			= (ImageView) row.findViewById(R.id.star);
        holder.chat 			= (ImageView) row.findViewById(R.id.comments);
        holder.title			= (TextView) row.findViewById(R.id.title);
        holder.content			= (LinearLayout) row.findViewById(R.id.content);
            
        row.setTag(holder);
        
        final BookmarkItems item = data.elementAt(position);
        
        holder.day.setText(item.day);
        holder.month.setText(item.month);
        holder.year.setText(item.year);
        holder.title.setText(item.title);
        
        holder.title.setOnClickListener(new OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			SharedPreferences settings 	= context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
    			Boolean e_browser 			= settings.getBoolean("external_browser", false);
    			SharedPreferences cookie 	= context.getSharedPreferences("Cookie", Context.MODE_PRIVATE);
    			String login_cookie 		= cookie.getString("login_cookie", "");
    			
    			//add link to read
    			LinksDataSource links = new LinksDataSource(context);
    	        links.open();
    	        links.addLink(item.article_url);
    	        links.close();
    			
    			if (!e_browser) {
    				Intent mIntent=new Intent(context, Article.class);
    				Bundle bundle=new Bundle();
    				bundle.putString("article_url", item.article_url);
      				bundle.putString("comment_url", item.comment_id);
      				bundle.putString("submission_title", item.title);
      				bundle.putString("login_cookie", login_cookie);
      				mIntent.putExtras(bundle);
      				context.startActivity(mIntent);
      				((Activity) context).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    			} else {
      				String url = item.article_url;
      				
      				if (!url.startsWith("http")) {
      					url = "http://news.ycombinator.com/" + url;
      				}
      				
      				Intent i = new Intent(Intent.ACTION_VIEW);
      				i.setData(Uri.parse(url));
      				context.startActivity(i);
      				((Activity) context).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    			}
    		}
    	});
        holder.chat.setOnClickListener(new OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			SharedPreferences cookie 	= context.getSharedPreferences("Cookie", Context.MODE_PRIVATE);
    			String login_cookie 		= cookie.getString("login_cookie", "");
    			
    			Intent mIntent=new Intent(context, Comments.class);
    			Bundle bundle=new Bundle();
    			bundle.putString("article_url", item.article_url);
    			bundle.putString("comment_url", item.comment_id);
    			bundle.putString("submission_title", item.title);
    			bundle.putString("login_cookie", login_cookie);
    			mIntent.putExtras(bundle);
    			context.startActivity(mIntent);
    			//((Activity) item.parent).overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    			((Activity) context).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
    		}
    	});
        holder.star.setOnClickListener(new OnClickListener() {
    		
    		@Override
    		public void onClick(View v) {
    			BookMarksDataSource bm 	= new BookMarksDataSource(context);
    	        bm.open();
    	        bm.remove(item.comment_id);
    			bm.close();
    			
    			//hide item
    			holder.content.setVisibility(View.GONE);
    		}
    	});
        
        BookMarksDataSource bm 	= new BookMarksDataSource(context);
        bm.open();
        if(!bm.isBookmarked(item.comment_id)) {
        	holder.content.setVisibility(View.GONE);
        } else {
        	holder.content.setVisibility(View.VISIBLE);
        }
		bm.close();
        
        return row;
    }
    
    static class Holder {
		public ImageView star;
        public TextView day;
		public TextView month;
		public TextView year;
		public ImageView chat;
		public TextView title;
		public LinearLayout content;
    }
}
