package com.rickylaishram.hackernews;

import java.math.BigInteger;
import java.security.SecureRandom;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import com.flurry.android.FlurryAgent;
import com.rickylaishram.util.IabHelper;
import com.rickylaishram.util.IabResult;
import com.rickylaishram.util.Key;
import com.rickylaishram.util.Purchase;

public class Donate extends Activity {
    boolean donated 					= false;
    static final String SKU_DONATE_1 	= "donate_1";
    //static final String SKU_DONATE_1 	= "android.test.purchased";
    static final String SKU_DONATE_2 	= "donate_2";
    static final int RC_REQUEST 		= 10001;
    private String PAYLOAD;
	
	IabHelper mHelper;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.donate);
		
		ActionBar actionbar = getActionBar();
		actionbar.setHomeButtonEnabled(true);
        actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle("Donate");
		
		SharedPreferences cookie 			= getSharedPreferences("Cookie", 0);
		SharedPreferences settings 			= getSharedPreferences("Settings", MODE_PRIVATE);
		Integer color	 					= settings.getInt("color_scheme", 0);
        if(cookie.getString("login_cookie", "").equals("")) {
        	actionbar.setLogo(R.drawable.icon_small_bw);
        } else if(color == 0) {
        	actionbar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_orange));
        }
		
		Key keyClass	= new Key();
		String key		= keyClass.getKey();
		
		mHelper = new IabHelper(this, key);
		mHelper.enableDebugLogging(true);
		
		mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d("Donate", "Setup finished.");

                if (!result.isSuccess()) {
                    Log.e("Donate","Problem setting up in-app billing: " + result);
                    return;
                }
                Log.d("Donate", "Setup successful. Querying inventory.");
                //mHelper.queryInventoryAsync(mGotInventoryListener);
            }
        });
		
		((Button) findViewById(R.id.donate1)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onDonateButtonClicked(v, 1);
			}
		});
		
		((Button) findViewById(R.id.donate2)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				onDonateButtonClicked(v, 2);
			}
		});
	}
    
    public void onDonateButtonClicked(View arg0, Integer id) {
        Log.d("Donate", "Upgrade button clicked; launching purchase flow for upgrade.");
        
        SecureRandom random 	= new SecureRandom();
        PAYLOAD					= new BigInteger(32, random).toString();
        String payload 			= PAYLOAD ; 
        
        if(id == 1) {
        mHelper.launchPurchaseFlow(this, SKU_DONATE_1, RC_REQUEST, mPurchaseFinishedListener, payload);
        } else if(id == 2) {
        	mHelper.launchPurchaseFlow(this, SKU_DONATE_2, RC_REQUEST, mPurchaseFinishedListener, payload);
        }
    }
    
    boolean verifyDeveloperPayload(Purchase p) {
        String payload 	= p.getDeveloperPayload();
        
        Boolean value 	= false;
        
        if(payload.equals(PAYLOAD)) {
        	value = true;
        }
        
        return value;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("Donate", "onActivityResult(" + requestCode + "," + resultCode + "," + data);

        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.d("Donate", "onActivityResult handled by IABUtil.");
        }
    }
    
    // Callback for when a purchase is finished
    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            Log.d("Donate", "Purchase finished: " + result + ", purchase: " + purchase);
            if (result.isFailure()) {
                Log.e("Donate","Error purchasing: " + result);
                return;
            }
            if (!verifyDeveloperPayload(purchase)) {
            	Log.e("Donate","Error purchasing. Authenticity verification failed.");
                return;
            }

            Log.d("Donate", "Purchase successful.");
            
            if (purchase.getSku().equals(SKU_DONATE_1)) {
                Log.d("Donate", "Purchase is premium upgrade. Congratulating user.");
                Toast.makeText(getApplicationContext(),"Thank you for donating!", Toast.LENGTH_LONG).show();
            } else if(purchase.getSku().equals(SKU_DONATE_2)) {
            	Log.d("Donate", "Purchase is premium upgrade. Congratulating user.");
                Toast.makeText(getApplicationContext(),"Thank you for donating!", Toast.LENGTH_LONG).show();
            }
        }
    };
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        
        // very important:
        Log.d("Donate", "Destroying helper.");
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
    
    public void updateUi() {
        if(donated) {
        	//((Button) findViewById(R.id.donate)).setEnabled(false);
        	//((TextView) findViewById(R.id.text)).setText("Thank you for donating");
        }
    }
	
    void setWaitScreen(boolean set) {
        //findViewById(R.id.wait).setVisibility(set ? View.GONE : View.VISIBLE);
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
	    		Intent parentActivityIntent = new Intent(this, Settings.class);
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
