package com.rickylaishram.hackernews;

import static com.rickylaishram.util.CommonUitls.SENDER_ID;
import static com.rickylaishram.util.CommonUitls.FLURY_KEY;


import java.util.Timer;
import java.util.TimerTask;

import com.google.android.gcm.GCMRegistrar;

import com.flurry.android.FlurryAgent;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;

public class Splash extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_orange));
        
        //get cookie if exist
        SharedPreferences settings 	= getSharedPreferences("Cookie", MODE_PRIVATE);
        String login_cookie 		= settings.getString("login_cookie", "");
        
        if(login_cookie != "") {
        	if(!GCMRegistrar.isRegistered(this) || !GCMRegistrar.isRegisteredOnServer(this)) {
            	GCMRegistrar.register(this, SENDER_ID);
            }
        }
        
        startApp();
	}
	
	class StartApp extends TimerTask {

		@Override
		public void run() {
			SharedPreferences settings 	= getSharedPreferences("Cookie", MODE_PRIVATE);
	        String login_cookie 		= settings.getString("login_cookie", "");
	        
	        Intent mIntent 	= new Intent(Splash.this, ListActivity.class);
	        Bundle bundle 	= new Bundle();
	        
			bundle.putString("login_cookie", login_cookie);
			mIntent.putExtras(bundle);
			Splash.this.startActivity(mIntent);
			Splash.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
			finish();
		}
	}
	
	private void startApp() {
		SharedPreferences settings 	= getSharedPreferences("Cookie", MODE_PRIVATE);
        String login_cookie 		= settings.getString("login_cookie", "");
        
        Intent mIntent 	= new Intent(Splash.this, ListActivity.class);
        Bundle bundle 	= new Bundle();
        
		bundle.putString("login_cookie", login_cookie);
		mIntent.putExtras(bundle);
		Splash.this.startActivity(mIntent);
		Splash.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
		finish();
	}
}