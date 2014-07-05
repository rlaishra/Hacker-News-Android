package com.rickylaishram.hackernews;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import static com.rickylaishram.util.CommonUitls.isNightMode;

public class About extends Activity{

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(isNightMode(this)) {
            setContentView(R.layout.about_night);
        } else {
            setContentView(R.layout.about);
        }

        ActionBar actionbar = getActionBar();
        actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setTitle("About");
        
        //home icon color change for logged out users
        SharedPreferences cookie 			= getSharedPreferences("Cookie", 0);
        SharedPreferences settings 			= getSharedPreferences("Settings", Context.MODE_PRIVATE);
		Integer color 						= settings.getInt("color_scheme", 0);
		
        if(cookie.getString("login_cookie", "").equals("")) {
        	actionbar.setLogo(R.drawable.icon_small_bw);
        } else if(color == 0){
        	actionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_orange));
        }
        
        Button contact = (Button) findViewById(R.id.contact);
        
        contact.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
				String aEmailList[] = {"rickylaishram@gmail.com"};
				emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, aEmailList);
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "[Hacker News Android]");
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "App version - " + getResources().getString(R.string.app_version)+"\nEnter Message.");
				emailIntent.setType("plain/text");
				About.this.startActivity(emailIntent);
				About.this.overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
			}
		});
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
	
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		switch (menuItem.getItemId()) {
	    	case android.R.id.home:
	    		finish();
	    		return true;
		}
		return (super.onOptionsItemSelected(menuItem));	
	}
}
