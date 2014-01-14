package com.rickylaishram.hackernews;

import java.util.TimerTask;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class Splash extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_orange));

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