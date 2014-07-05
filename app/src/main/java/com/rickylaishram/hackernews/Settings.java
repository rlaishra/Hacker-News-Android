package com.rickylaishram.hackernews;

import java.util.ArrayList;
import java.util.List;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.Toast;

import static com.rickylaishram.util.CommonUitls.isNightMode;


public class Settings extends Activity {
	
	Integer COLOR_NUM = 0;
    Context ctx;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(isNightMode(this)) {
            setContentView(R.layout.settings_night);
        } else {
    		setContentView(R.layout.settings);
        }

        ctx = this;

		ActionBar actionbar = getActionBar();
		actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle("Settings");
		
		//get settings
		final SharedPreferences settings 	= getSharedPreferences("Settings", MODE_PRIVATE);
		Integer d_page 						= settings.getInt("default_page", 0);
		Integer t_size 						= settings.getInt("text_size", 1);
		Integer color_num 					= settings.getInt("color_scheme", 0);
		Boolean e_browser 					= settings.getBoolean("external_browser", false);
		Boolean night		 				= settings.getBoolean("night_mode", false);
		
		Spinner default_page 				= (Spinner) findViewById(R.id.default_page);
		Spinner text_size					= (Spinner) findViewById(R.id.text_size);
		Switch external_browser 			= (Switch) findViewById(R.id.external_browser);
		Switch night_mode 				    = (Switch) findViewById(R.id.night_mode);
		Spinner color						= (Spinner) findViewById(R.id.color_scheme);
		
		COLOR_NUM	= color_num;
		
		//home icon color change for logged out users
        SharedPreferences cookie 			= getSharedPreferences("Cookie", 0);
		
        if(cookie.getString("login_cookie", "").equals("")) {
        	actionbar.setLogo(R.drawable.icon_small_bw);
        } else if(color_num == 0) {
        	actionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_orange));
        }
		
		// default/user selected settings
		external_browser.setChecked(e_browser);
		night_mode.setChecked(night);
		
		//default page Setup spinner
		List<String> page_list = new ArrayList<String>();
		page_list.add(getString(R.string.title_section1));
        page_list.add(getString(R.string.title_section2));
        page_list.add(getString(R.string.title_section3));
        page_list.add(getString(R.string.title_section4));
        page_list.add(getString(R.string.title_section5));
        page_list.add(getString(R.string.title_section6));
		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, page_list);
		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		default_page.setAdapter(dataAdapter);
		default_page.setSelection(d_page);
		
		//text size spinner
		List<String> text_list = new ArrayList<String>();
		text_list.add("Small");
		text_list.add("Medium");
		text_list.add("Large");
		ArrayAdapter<String> textAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, text_list);
		textAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		text_size.setAdapter(textAdapter);
		text_size.setSelection(t_size);
		
		//color spinner
		List<String> color_list = new ArrayList<String>();
		color_list.add("Orange");
		color_list.add("Light");
		ArrayAdapter<String> colorAdapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, color_list);
		colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		color.setAdapter(colorAdapter);
		color.setSelection(color_num);
		
		external_browser.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("external_browser", isChecked);
				editor.commit();
			}
		});
		
		night_mode.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putBoolean("night_mode", isChecked);
				editor.commit();

                AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
                builder.setMessage("Restart app for change to switch mode?");

                builder.setPositiveButton("Restart", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent mStartActivity = new Intent(ctx, Splash.class);
                        int mPendingIntentId = 123456;
                        PendingIntent mPendingIntent = PendingIntent.getActivity(ctx, mPendingIntentId,    mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
                        AlarmManager mgr = (AlarmManager)ctx.getSystemService(Context.ALARM_SERVICE);
                        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
                        System.exit(0);
                    }
                });

                builder.setNegativeButton("Restart Later", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(ctx, "Change will take place when you restart the app", Toast.LENGTH_LONG).show();
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();

			}
		});
		
		default_page.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("default_page", arg2);
				editor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		text_size.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("text_size", arg2);
				editor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
		
		color.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				SharedPreferences.Editor editor = settings.edit();
				editor.putInt("color_scheme", arg2);
				editor.commit();
				
				if(COLOR_NUM != arg2) {
					Toast.makeText(getApplicationContext(), "The change will take effect the next time the app is restarted", Toast.LENGTH_LONG).show();
				}
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}
		});
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
	
	public boolean onOptionsItemSelected(MenuItem menuItem) {
		Intent mIntent;
		Bundle bundle = new Bundle();
		switch (menuItem.getItemId()) {
	    	case android.R.id.home:
	    		Intent parentActivityIntent = new Intent(this, ListActivity.class);
	            parentActivityIntent.addFlags(
	                    Intent.FLAG_ACTIVITY_CLEAR_TOP |
	                    Intent.FLAG_ACTIVITY_SINGLE_TOP);
	            startActivity(parentActivityIntent);
	            overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
	            finish();
	    		return true;
		}
		return (super.onOptionsItemSelected(menuItem));	
	}
}
