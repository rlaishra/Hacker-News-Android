package com.rickylaishram.hackernews;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Login extends Activity {
	
	static Context context;
	static String username;
	static String password;
	static String login_cookie;
	static ProgressDialog progressdialog;
	
	static String LOGIN_PAGE_URL 	= "https://news.ycombinator.com/login";
	static String LOGIN_URL 		= "https://news.ycombinator.com/x";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.userpasslayout);
        
        context 			= this;
        
        ActionBar actionbar = getActionBar();
        actionbar.setTitle("Login");
        
        //home icon color change for logged out users
        SharedPreferences cookie = getSharedPreferences("Cookie", 0);
        if(cookie.getString("login_cookie", "").equals("")) {
        	actionbar.setLogo(R.drawable.icon_small_bw);
        }
        
        final EditText username_i 	= (EditText) findViewById(R.id.username);
        final EditText password_i 	= (EditText) findViewById(R.id.password);
        Button login_b 				= (Button) findViewById(R.id.login);
        Button cancel_b 			= (Button) findViewById(R.id.cancel);
        
        // Login click event
        login_b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				username = username_i.getText().toString();
        		password = password_i.getText().toString();
        		
        		if (username.isEmpty() || password.isEmpty()) {
        			AlertDialog.Builder alert = new AlertDialog.Builder(context);
        			alert.setTitle("Error");
        			alert.setMessage("Username and Password cannot be blank");
        			alert.setCancelable(false);
        			alert.setPositiveButton("Ok", null);
        			alert.create().show();
        		} else {
        			progressdialog = ProgressDialog.show(context, null, "Loging you in", true);
        			progressdialog.show();
        			Authenticate auth	= new Authenticate();
        			auth.execute(new String[]{username, password});
        		}
			}
		});
        
        // Cancel click event
        cancel_b.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent mIntent 	= new Intent(context,ListActivity.class);
				Bundle bundle 	= new Bundle();
				
				bundle.putString("login_cookie", "");
				mIntent.putExtras(bundle);
				startActivity(mIntent);
				overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				finish();
			}
		});
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
	
	// return types
	// 1 - success
	// 2 - wrong username/password
	// 3 - network problem
    private Integer log_in_user(String username, String password) {
    	
    	
    	//get fnid from login page
    	try {
			Document loginpage 	= Jsoup.connect(LOGIN_PAGE_URL).get();
			String fnid 		= loginpage.select("input[name=fnid]").attr("value");
			
			//Login
			HttpClient httpclient 	= new DefaultHttpClient();
		    HttpPost httppost 		= new HttpPost(LOGIN_URL);
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
	        nameValuePairs.add(new BasicNameValuePair("fnid", fnid));
	        nameValuePairs.add(new BasicNameValuePair("u", username));
	        nameValuePairs.add(new BasicNameValuePair("p", password));
	        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
	        httppost.addHeader("Content-type", "application/x-www-form-urlencoded");
	        httppost.addHeader("Accept","text/plain");
	        httppost.addHeader("User-agent","Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
	        
	        HttpResponse response = httpclient.execute(httppost);
	        if (response.containsHeader("Set-Cookie")) {
	        	login_cookie = response.getFirstHeader("Set-Cookie").getValue();
	        	return 1;
	        } else {
	        	return 2;
	        }
		} catch (IOException e) {
			return 3;
		}	
	}
    
    private class Authenticate extends AsyncTask<String, Void, Integer> {
    	
    	public Authenticate(){}
		@Override
		protected Integer doInBackground(String... params) {
			String username = params[0];
			String password	= params[1];

			try {
				//extract fnid
				Document loginpage 	= Jsoup.connect(LOGIN_PAGE_URL).get();
				String fnid 		= loginpage.select("input[name=fnid]").attr("value");
				
				//Login
				HttpClient httpclient 	= new DefaultHttpClient();
			    HttpPost httppost 		= new HttpPost(LOGIN_URL);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
		        nameValuePairs.add(new BasicNameValuePair("fnid", fnid));
		        nameValuePairs.add(new BasicNameValuePair("u", username));
		        nameValuePairs.add(new BasicNameValuePair("p", password));
		        httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
		        httppost.addHeader("Content-type", "application/x-www-form-urlencoded");
		        httppost.addHeader("Accept","text/plain");
		        httppost.addHeader("User-agent","Mozilla/5.0 (Linux; U; Android 2.2; en-us; Nexus One Build/FRF91) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
		        
		        HttpResponse response = httpclient.execute(httppost);
		        if (response.containsHeader("Set-Cookie")) {
		        	login_cookie = response.getFirstHeader("Set-Cookie").getValue();
		        	return 1;
		        } else {
		        	return 2;
		        }
			} catch (IOException e) {
				return 3;
			}
		}
		
		@Override
		protected void onPostExecute(Integer login_status) {
			if (login_status == 1) {
				//save cookie
				SharedPreferences settings 		= getSharedPreferences("Cookie", 0);
				SharedPreferences.Editor editor = settings.edit();
				editor.putString("login_cookie", login_cookie);
				editor.putString("username", username);
				editor.commit();
				
				//go back to list
				Intent mIntent 	= new Intent(context,ListActivity.class);
				Bundle bundle 	= new Bundle();
				
				bundle.putString("login_cookie", login_cookie);
				mIntent.putExtras(bundle);
				context.startActivity(mIntent);
				((Activity) context).overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				finish();
			} else if(login_status == 2){
				progressdialog.dismiss();
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
    			alert.setTitle("Error");
    			alert.setMessage("Wrong username or password");
    			alert.setCancelable(false);
    			alert.setPositiveButton("Ok", null);
    			alert.create().show();
			} else {
				progressdialog.dismiss();
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
    			alert.setTitle("Error");
    			alert.setMessage("Network problem");
    			alert.setCancelable(false);
    			alert.setPositiveButton("Ok", null);
    			alert.create().show();
			}
		}
    }
}