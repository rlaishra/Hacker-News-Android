package com.rickylaishram.hackernews;

import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ShareActionProvider;
import android.widget.Toast;

public class Article extends Activity {
	
	static String article_url;
	static String comment_url;
	static String title;
	static String login_cookie;
	static Context ctx;
	
	@SuppressLint("SetJavaScriptEnabled")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.article);
        
        ActionBar actionbar = getActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
        
        //home icon color change for logged out users
        SharedPreferences cookie 			= getSharedPreferences("Cookie", 0);
        SharedPreferences settings 			= getSharedPreferences("Settings", Context.MODE_PRIVATE);
		Integer color 						= settings.getInt("color_scheme", 0);
        
        if(cookie.getString("login_cookie", "").equals("")) {
        	actionbar.setLogo(R.drawable.icon_small_bw);
        } else if(color == 0){
        	actionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_orange));
        }
        
        ctx = this;
        
        final Activity activity = this;
        
        WebView web 					= (WebView) findViewById(R.id.webView1);
        final ProgressBar progressBar 	= (ProgressBar) findViewById(R.id.progressBar1);
        final RelativeLayout container	= (RelativeLayout) findViewById(R.id.progressBarContainer);
        progressBar.setProgress(0);
        progressBar.setMax(100);
        progressBar.bringToFront();
        
        Bundle bundle 	= getIntent().getExtras();
        article_url 	= bundle.getString("article_url");
        comment_url 	= bundle.getString("comment_url");
        title 			= bundle.getString("submission_title");
        login_cookie 	= bundle.getString("login_cookie");
        
        
        actionbar.setTitle(title);
        
        //web.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        web.getSettings().setJavaScriptEnabled(true);
        web.getSettings().setBuiltInZoomControls(true);
        web.getSettings().setDisplayZoomControls(true);
        web.getSettings().setSupportZoom(true);
        web.getSettings().setLoadWithOverviewMode(true);
        web.getSettings().setUseWideViewPort(true);
        web.setWebChromeClient(new WebChromeClient() {
        	public void onProgressChanged(WebView view, int progress) {
        	     // Activities and WebViews measure progress with different scales.
        	     // The progress meter will automatically disappear when we reach 100%
        	     progressBar.setProgress(progress);
        	     
        	     if(progressBar.getProgress() >= 100){
        	    	 progressBar.setVisibility(View.GONE);
        	    	 container.setVisibility(View.GONE);
        	     }
        	   }
        });
        
        web.setWebViewClient(new WebViewClient() {
        	   public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        	     Toast.makeText(activity, description, Toast.LENGTH_LONG).show();
        	   }
        });
        
        web.loadUrl(article_url);
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.article_menu, menu);
        
        //Share Intent
        ShareActionProvider mShareActionProvider 	= (ShareActionProvider) menu.findItem(R.id.menu_share).getActionProvider();
        Intent shareIntent 							= new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " + article_url);
        mShareActionProvider.setShareIntent(shareIntent);
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
	
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		Intent mIntent;
		Bundle bundle=new Bundle();
		switch (menuItem.getItemId()) {
	    	case android.R.id.home:
	    		SharedPreferences prefs = getSharedPreferences("Cookie", 0);
	            String cookie 			= prefs.getString("login_cookie", "");
	    		bundle.putString("login_cookie", cookie);
	    		mIntent = new Intent(this, ListActivity.class);
	            mIntent.addFlags(
	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
	                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            mIntent.putExtras(bundle);
	            startActivity(mIntent);
	            this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	            finish();
	    		return true;
	    	
	    	case R.id.external_browser:
	    		String url = article_url;
  				
  				if (!url.startsWith("http")) {
  					url = "https://news.ycombinator.com/" + url;
  				}	
  				Intent i = new Intent(Intent.ACTION_VIEW);
  				i.setData(Uri.parse(url));
  				Article.this.startActivity(i);
  				Article.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
  				return true;
	    		
	    	case R.id.menu_comments:
	    		mIntent = new Intent(Article.this, Comments.class);
	    		//mIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);    		
  				bundle.putString("article_url", article_url);
  				bundle.putString("comment_url", comment_url);
  				bundle.putString("submission_title", title);
  				bundle.putString("login_cookie", login_cookie);
  				mIntent.putExtras(bundle);
	    		Article.this.startActivity(mIntent);
	    		Article.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	    		return true;
	    		
	    	case R.id.menu_login:
	    		mIntent = new Intent(this, Login.class);
	    		this.startActivity(mIntent);
	    		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	    		finish();
	    		return true;
	    		
	    	case R.id.menu_logout:
	    		// delete cookie to logout
	    		SharedPreferences settings 		= getSharedPreferences("Cookie", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("login_cookie", "");
				editor.commit();
				
				//unregister from GCM
				GCMRegistrar.unregister(ctx);
				
				//go back to list
				mIntent = new Intent(this,ListActivity.class);
				bundle.putString("login_cookie", "");
				mIntent.putExtras(bundle);
				Article.this.startActivity(mIntent);
				Article.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
	    		
	    	case R.id.menu_bookmarks:
	    		mIntent = new Intent(this, Bookmarks.class);
	    		this.startActivity(mIntent);
	    		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	    		return true;
		}
		return (super.onOptionsItemSelected(menuItem));	
	}
}
