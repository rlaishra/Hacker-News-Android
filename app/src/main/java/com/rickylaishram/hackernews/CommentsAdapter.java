package com.rickylaishram.hackernews;

import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class CommentsAdapter extends ArrayAdapter<CustomCommentListItem>{

    Context context; 
    int layoutResourceId;    
    Vector<CustomCommentListItem> data 	= new Vector<CustomCommentListItem>();
    List<Float> text_sizes			= Arrays.asList(12f, 15f, 18f);
    
    public CommentsAdapter(Context context, int layoutResourceId, Vector<CustomCommentListItem> data) {
        super(context, layoutResourceId, data);
        this.layoutResourceId 	= layoutResourceId;
        this.context 			= context;
        this.data 				= data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row 		= convertView;
        //Holder holder 	= null;
        final Holder holder = new Holder();
        
        //if(row == null) {
            LayoutInflater inflater = ((Activity)context).getLayoutInflater();
            row 					= inflater.inflate(layoutResourceId, parent, false);
            
            //holder 					= new Holder();
            holder.submiter 		= (TextView) row.findViewById(R.id.commenter);
            holder.comments 		= (TextView) row.findViewById(R.id.comment);
            holder.space 			= (ImageView) row.findViewById(R.id.imageView1);
            holder.draw 			= (ImageView) row.findViewById(R.id.imageView3);
            holder.time 			= (TextView) row.findViewById(R.id.time);
            holder.comment_all		= (LinearLayout) row.findViewById(R.id.comment_all);
            holder.plus				= (ImageView) row.findViewById(R.id.ic_plus);
            
            row.setTag(holder);
        //} else {
            //holder = (Holder)row.getTag();
        //}

        CustomCommentListItem item = data.elementAt(position);
        holder.submiter.setText(item.submitter);
        holder.comments.setText(item.comment);
        
        //make links clickable
        //holder.comments.setMovementMethod(LinkMovementMethod.getInstance());
        
        //set text size
        SharedPreferences settings 			= context.getSharedPreferences("Settings", Context.MODE_PRIVATE);
		Integer t_size 						= settings.getInt("text_size", 1);
		holder.comments.setTextSize(text_sizes.get(t_size));
        
		//set margin for child comments
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
		lp.setMargins(item.image, 0, 0, 0);
		holder.space.setLayoutParams(lp);
        
        holder.draw.setImageResource(item.draw);
        holder.time.setText(item.time);
        //holder.web.loadDataWithBaseURL(null, "<html><body>" + item.comment + "</body></html>", "text/html", "utf-8", null);
        
        if(item.padding != null) {
        	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        	params.setMargins(0, item.padding, 0, 0);
        	
        	holder.ll = (LinearLayout) row.findViewById(R.id.comment_1);
        	holder.ll.setLayoutParams(params);
        } else {
        	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        	params.setMargins(0, 0, 0, 0);
        	
        	holder.ll = (LinearLayout) row.findViewById(R.id.comment_1);
        	holder.ll.setLayoutParams(params);
        }
        
        //collapse state
        if(item.collapse.equals("no")) {
        	holder.comment_all.setVisibility(View.VISIBLE);
        	holder.comments.setVisibility(View.VISIBLE);
        	holder.plus.setVisibility(View.GONE);
        } else if(item.collapse.equals("yes")) {
        	holder.comment_all.setVisibility(View.GONE);
        } else if(item.collapse.equals("header")) {
        	holder.comments.setVisibility(View.GONE);
        	holder.plus.setVisibility(View.VISIBLE);
        }
        
        return row;
    }
    
    static class Holder {
		public ImageView space;
        public TextView submiter;
		public TextView comments;
		public ImageView draw;
		public TextView time;
		public LinearLayout ll;
		public ImageView plus;
		public LinearLayout comment_text_only;
		public LinearLayout comment_all;
    }
}