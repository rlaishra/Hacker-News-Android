package com.rickylaishram.hackernews;

import static com.rickylaishram.util.CommonUitls.SENDER_ID;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;

/**
 * IntentService responsible for handling GCM messages.
 */
public class GCMIntentService extends GCMBaseIntentService {

    @SuppressWarnings("hiding")
    private static final String TAG = "GCMIntentService";

    public GCMIntentService() {
        super(SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
        Log.i(TAG, "Device registered: regId = " + registrationId);
        ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {
        Log.i(TAG, "Device unregistered");
        if (GCMRegistrar.isRegisteredOnServer(context)) {
            ServerUtilities.unregister(context, registrationId);
        } else {
            // This callback results from the call to unregister made on
            // ServerUtilities when the registration to the server failed.
            Log.i(TAG, "Ignoring unregister callback");
        }
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        Bundle payload 		= intent.getExtras();
        String commentor 	= payload.getString("commentor", "");
        String element 		= payload.getString("element", "");
        String onid			= payload.getString("onid", "");
        String onname		= payload.getString("onname", "");
        

        // check if user has enabled
        SharedPreferences settings 	= getSharedPreferences("Settings", MODE_PRIVATE);
		Boolean noti		 		= settings.getBoolean("notification", true);
		
		if(noti) {
			generateNotification(context, commentor, element, onid, onname);
		}
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        //String message = getString(R.string.gcm_deleted, total);
        //generateNotification(context, "message");
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String commentor, String element, String onid, String onname) {
        int icon = R.drawable.icon_small_noti;
        String message = commentor + " replied to your comment on \"" + onname +"\"";
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        
        Notification notification 	= new Notification(icon, message, when);
        String title 				= context.getString(R.string.app_name);
        Intent notificationIntent 	= new Intent(context, Comments.class);
        
        //fetch article url
        Document doc = null;
    	String article_url = null;
    	int i = 0;
    	
    	while(i < 10) {
	    	try {
				doc 		= Jsoup.connect("http://news.ycombinator.com/item?id="+onid).get();
				article_url	= doc.select("td[class=title]>a").attr("href");
			} catch (Exception e) {
				e.printStackTrace();
				i = i + 1;
				try {
					Thread.sleep((long)(1000*Math.random()*60));
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
	    	
	    	if(article_url != null) {
	    		i = 11;
	    	}
    	}
		
    	if (article_url != null) {
			SharedPreferences settings = context.getSharedPreferences("Cookie", 0);
	        String login_cookie = settings.getString("login_cookie", "");
	        
	        Bundle bundle = new Bundle();
	        
	        bundle.putString("comment_url", "item?id="+onid);
	        bundle.putString("article_url", article_url);
	        bundle.putString("submission_title", onname);
	        bundle.putString("login_cookie", login_cookie );
	        bundle.putString("comment_id", "item?id="+element);
	        
	        notificationIntent.putExtras(bundle);
	        
	        
	        // set intent so it does not start a new activity
	        //notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
	        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
	        
	        PendingIntent intent =
	                PendingIntent.getActivity(context, 0, notificationIntent, 0);
	        notification.setLatestEventInfo(context, title, message, intent);
	        notification.flags |= Notification.FLAG_AUTO_CANCEL;
	        notificationManager.notify(0, notification);
    	}
    }
}