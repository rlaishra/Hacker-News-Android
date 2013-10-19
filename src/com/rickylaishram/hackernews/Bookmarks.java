package com.rickylaishram.hackernews;

import java.util.Vector;

import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.rickylaishram.hackernews.db.BookMarksDataSource;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

public class Bookmarks extends Activity {
	
	private Context context;
	private ListView list;
	private String login_cookie;
	static BookMarkAdapter adapter;
	static Vector<BookmarkItems> mlist 	= new Vector<BookmarkItems>();
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list);
        
        //get default settings
        SharedPreferences settings 	= getSharedPreferences("Settings", 0);
        SharedPreferences cookie 	= getSharedPreferences("Cookie", MODE_PRIVATE);
		Integer d_page 				= settings.getInt("default_page", 0);
		Integer color 				= settings.getInt("color_scheme", 0);
		login_cookie 				= cookie.getString("login_cookie", "");
		
		ActionBar actionbar = getActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle("Bookmarks");
        
        //home icon color change for logged out users
        if(cookie.getString("login_cookie", "").equals("")) {
        	actionbar.setLogo(R.drawable.icon_small_bw);
        } else if(color == 0){
        	actionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_orange));
        }
        
        context 		= this;
        list 			= (ListView) findViewById(R.id.listView1);
        
        mlist.clear();
        
        adapter = new BookMarkAdapter(this, R.layout.bookmark_element, mlist);
        list.setAdapter(adapter);
        
        (new PopulateBookMark()).execute();
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
   		getMenuInflater().inflate(R.menu.bookmark_menu, menu);
        return true;
    }
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
	    if (login_cookie.equals("")) {
	    	menu.findItem(R.id.menu_logout).setVisible(false);
	    	menu.findItem(R.id.menu_login).setVisible(true);
	    } else {
	    	menu.findItem(R.id.menu_logout).setVisible(true);
	    	menu.findItem(R.id.menu_login).setVisible(false);
	    }
	        
	    return true;
	}
	
	private class PopulateBookMark extends AsyncTask<Void, Void, Vector<BookmarkItems>> {

		@Override
		protected Vector<BookmarkItems> doInBackground(Void... params) {
			
			BookMarksDataSource bm		= new BookMarksDataSource(context);
			bm.open();
			Vector<BookmarkItems> data	= bm.fetchAll();
			bm.close();
			
			return data;
		}
		
		@Override
		protected void onPostExecute(Vector<BookmarkItems> data) {
			if(data != null) {
				mlist.addAll(data);
				adapter.notifyDataSetChanged();
			} else {
				Toast.makeText(context, "Your bookmarks list is empty", Toast.LENGTH_LONG).show();
			}
			
			((ProgressBar) findViewById(R.id.main_list_spinner)).setVisibility(View.GONE);
		}
	}
	
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		Intent mIntent;
		Bundle bundle = new Bundle();
		switch (menuItem.getItemId()) {
			case android.R.id.home:
	    		SharedPreferences prefs = getSharedPreferences("Cookie", 0);
	            String cookie_login 	= prefs.getString("login_cookie", "");
	    		bundle.putString("login_cookie", cookie_login);
	    		mIntent = new Intent(this, ListActivity.class);
	            mIntent.addFlags(
	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
	                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            mIntent.putExtras(bundle);
	            startActivity(mIntent);
	            this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	            finish();
	    		return true;
	    	case R.id.menu_login:
	    		mIntent = new Intent(this, Login.class);
	    		this.startActivity(mIntent);
	    		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	    		finish();
	    		return true;
	    		
	    	case R.id.menu_logout:
	    		// delete cookie to logout
	    		SharedPreferences settings = getSharedPreferences("Cookie", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("login_cookie", "");
				editor.commit();
				
				//unregister from GCM
				GCMRegistrar.unregister(context);
				
				//go back to list
				mIntent = new Intent(context,ListActivity.class);
				bundle 	= new Bundle();
				bundle.putString("login_cookie", "");
				mIntent.putExtras(bundle);
				startActivity(mIntent);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				finish();
	    		return true;
	    		
	    	case R.id.menu_about:
	    		mIntent = new Intent(this, About.class);
	    		this.startActivity(mIntent);
	    		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	    		return true;
	    		
	    	case R.id.menu_settings:
	    		mIntent = new Intent(this, Settings.class);
	    		this.startActivity(mIntent);
	    		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	    		return true;
	    		
	    	case R.id.menu_bookmarks_clear:
	    		Builder builder = new AlertDialog.Builder(this);
	    	    builder.setMessage("Are you sure you want to clear the bookmarks list?");
	    	    builder.setCancelable(true);
	    	    builder.setPositiveButton("Yes", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						BookMarksDataSource bm 	= new BookMarksDataSource(context);
						bm.open();
						bm.clearAll();
						bm.close();
						
						mlist.clear();
						adapter.notifyDataSetChanged();
					}
				});
	    	    builder.setNegativeButton("No", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
	    	    AlertDialog dialog = builder.create();
	    	    dialog.show();
	    		
	    		return true;
		}
		return (super.onOptionsItemSelected(menuItem));	
	}
}
