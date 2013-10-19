package com.rickylaishram.hackernews;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import com.flurry.android.FlurryAgent;
import com.google.android.gcm.GCMRegistrar;
import com.rickylaishram.hackernews.db.LinksDataSource;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.ShareActionProvider;
import android.widget.SimpleCursorAdapter;
import android.widget.SlidingDrawer;
import android.widget.TextView;
import android.widget.Toast;

public class Comments extends Activity {
	
	static ArrayAdapter<CustomCommentListItem> adapter;
	static ListView list;
	static ProgressBar spinner;
	static ScrollView drawer;
	static LinearLayout drawer_handle;
	static LinearLayout title_view;
	static ImageView blue_bar;
	static ImageButton slider_button;
	static LinearLayout comment_header;
	static String upvote_top;
	static String points_top;
	static String article_url;
	static String comment_url;
	static String title;
	static String login_cookie;
	static String fnid_top, text_top;
	static Context context;
	static Vector<CustomCommentListItem> mlist 	= new Vector<CustomCommentListItem>();
	static ArrayList<String> listItems 			= new ArrayList<String>();
	static List<String> upvote_list 			= new LinkedList<String>();	// upvote urls
	static List<String> downvote_list 			= new LinkedList<String>(); // downvote urls
	static List<String> reply_list 				= new LinkedList<String>();	// reply urls
	static List<String> nested_list 			= new LinkedList<String>();	// for comment nesting
	static List<String[]> links_list 			= new LinkedList<String[]>(); // links in comment
	static Boolean drawer_open 					= false;
	static Boolean hidden 						= false;
	static String COMMENT_ID 					= null;
	static Integer COMMENT_POS 					= 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.comments);
        
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
        
        //clear all lists
        mlist.clear();
        links_list.clear();
        upvote_list.clear();
        downvote_list.clear();
        reply_list.clear();
        nested_list.clear();
        
        Bundle bundle 	= getIntent().getExtras();
        comment_url 	= "https://news.ycombinator.com/" + bundle.getString("comment_url");
        article_url 	= bundle.getString("article_url");
        title 			= bundle.getString("submission_title");
        login_cookie 	= bundle.getString("login_cookie");
        COMMENT_ID 		= bundle.getString("comment_id", "0");
        context 		= this;
        
        actionbar.setTitle(title);
        
        slider_button 	= (ImageButton) findViewById(R.id.slider_button);
        title_view 		= (LinearLayout) findViewById(R.id.title_text_view);
        drawer 			= (ScrollView) findViewById(R.id.header_more);
        drawer_handle 	= (LinearLayout) findViewById(R.id.header_more_handle);
        spinner 		= (ProgressBar) findViewById(R.id.progressBar1);
        list 			= (ListView) findViewById(R.id.comment_list);
        blue_bar 		= (ImageView) findViewById(R.id.blue_bar);
        comment_header 	= (LinearLayout) findViewById(R.id.comment_header);
        
        adapter = new CommentsAdapter(this, R.layout.comment_1, mlist);
        list.setAdapter(adapter);
        
        drawer_handle.setVisibility(View.GONE);
        blue_bar.setVisibility(View.INVISIBLE);
        title_view.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.VISIBLE);
        list.setVisibility(View.INVISIBLE);
        drawer.setVisibility(View.GONE);
        
        DownloadTask task = new DownloadTask();
        task.execute(new String[]{comment_url});
        
        slider_button.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (drawer_open) {
					//drawer.animateClose();
					drawer.setVisibility(View.GONE);
					//list.setVisibility(View.VISIBLE);
					slider_button.setImageResource(R.drawable.button_down);
					drawer_open = false;
				} else {
					//drawer.animateOpen();
					drawer.setVisibility(View.VISIBLE);		
					//list.setVisibility(View.INVISIBLE);
					slider_button.setImageResource(R.drawable.button_up);
					drawer_open = true;
				}
			}
		});
        
        title_view.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				final Dialog dialog = new Dialog(context);
				dialog.setContentView(R.layout.comment_popup);
				dialog.setTitle("Select action");
				dialog.setCancelable(true);
				dialog.show();
				
				ImageButton upvote_p 	= (ImageButton) dialog.findViewById(R.id.vote_up);
				ImageButton downvote_p 	= (ImageButton) dialog.findViewById(R.id.vote_down);
				ImageButton links_p 	= (ImageButton) dialog.findViewById(R.id.comment_links);
				ImageButton reply_p 	= (ImageButton) dialog.findViewById(R.id.comment_reply);
				ImageButton collapse_p	= (ImageButton) dialog.findViewById(R.id.comment_collapse);
				
				//Disable upvote and downvote if not available
				if(upvote_top.equals("") || login_cookie.equals("")) {
					upvote_p.setImageResource(R.drawable.button_up_disabled);
					upvote_p.setEnabled(false);
				}     
				
				if (reply_p.equals("")  || login_cookie.equals("")) {
					reply_p.setEnabled(false);
					reply_p.setImageResource(R.drawable.ic_write_disabled);
				}
				
				downvote_p.setImageResource(R.drawable.button_down_disabled);
				downvote_p.setEnabled(false);
				links_p.setEnabled(false);
				links_p.setVisibility(View.GONE);
				collapse_p.setVisibility(View.GONE);
				collapse_p.setEnabled(false);
				
				upvote_p.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						//ping server with request
						PingServer ping = new PingServer();
						ping.execute(new String[] { "https://news.ycombinator.com/" + upvote_top });
						
						// change count and color			           
			            String point_t_n = (Integer.parseInt(points_top) + 1) + "";
			            Integer draw_t_n = R.drawable.blue;
			            
			            TextView points_view_top 	= (TextView) findViewById(R.id.points_top);
			            ImageView image_top 		= (ImageView) findViewById(R.id.left_image_top);
			            
			            points_view_top.setText(point_t_n);
			            image_top.setImageResource(R.drawable.blue);
			            
			            
			            dialog.dismiss();
			            
			            //to disable voting again
			            upvote_top = "";
					}
				});
				
				reply_p.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						dialog.dismiss();
						final Dialog dialog_reply = new Dialog(context);
						dialog_reply.setContentView(R.layout.comment_reply);
						dialog_reply.setCancelable(false);
						dialog_reply.setTitle("Submit reply");
						dialog_reply.show();
						
						final EditText reply_text 	= (EditText) dialog_reply.findViewById(R.id.comment_reply);
						Button send 				= (Button) dialog_reply.findViewById(R.id.reply_confirm);
						Button cancel 				= (Button) dialog_reply.findViewById(R.id.reply_cancel);
						
						cancel.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								dialog_reply.dismiss();
							}
						});
						
						send.setOnClickListener(new OnClickListener() {
							
							@Override
							public void onClick(View v) {
								dialog_reply.dismiss();
								SendReply reply = new SendReply();
								reply.execute(new String[] { "", reply_text.getText().toString(), fnid_top});
								
								//add comment to list
								Integer image = 0;
								
								SharedPreferences settings 	= getSharedPreferences("Cookie", 0);
						        String submitter 			= settings.getString("username", "ME");
					            
					            Spanned text 	= Html.fromHtml(reply_text.getText().toString());
					            Integer draw 	= R.drawable.blue;
					            String time 	= "0 seconds ago";
					            
					            Integer new_pos = 0;
					            
					            mlist.add(new_pos,new CustomCommentListItem(image,submitter,text,draw,time, null, "no"));
					            adapter.notifyDataSetChanged();
					            
					            //change lists
					            upvote_list.add(new_pos,"");
					        	downvote_list.add(new_pos,"");
					        	reply_list.add(new_pos,"");
					        	nested_list.add(new_pos,"");
					        	links_list.add(new_pos, new String[] {});
					        	
					        	if(mlist.size() > 1) {
						        	image 		= mlist.get(1).image;
						            submitter 	= mlist.get(1).submitter;
						            text 		= mlist.get(1).comment;
						            time 		= mlist.get(1).time;
						            draw 		= R.drawable.blue;
						            Integer padding		= null;
						            
						            mlist.remove(1);
						            mlist.add(1,new CustomCommentListItem(image,submitter,text,draw, time, padding, "no"));
						            adapter.notifyDataSetChanged();
					        	}
					        	
							}
						});
					}
				});
			}
		});
        
        list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final int pos 		= arg2;
				final Dialog dialog = new Dialog(context);
				
				//if collapsed, expand. otherwise show normal dialog
				if(mlist.get(pos).collapse.equals("header")) {
					expandCommentThread(pos);
					Log.i("Expand Thread", pos+"");
				} else {
					dialog.setContentView(R.layout.comment_popup);
					dialog.setTitle("Select action");
					dialog.setCancelable(true);
					dialog.show();
					
					ImageButton upvote_p 	= (ImageButton) dialog.findViewById(R.id.vote_up);
					ImageButton downvote_p 	= (ImageButton) dialog.findViewById(R.id.vote_down);
					ImageButton links_p 	= (ImageButton) dialog.findViewById(R.id.comment_links);
					ImageButton reply_p 	= (ImageButton) dialog.findViewById(R.id.comment_reply);
					ImageButton collapse_p	= (ImageButton) dialog.findViewById(R.id.comment_collapse);
					
					//Disable upvote and downvote if not available
					if(upvote_list.get(pos).equals("") || login_cookie.equals("")) {
						upvote_p.setImageResource(R.drawable.button_up_disabled);
						upvote_p.setEnabled(false);
					}
					
					if(downvote_list.get(pos).equals("") || login_cookie.equals("")) {
						downvote_p.setImageResource(R.drawable.button_down_disabled);
						downvote_p.setEnabled(false);
					}
					
					//disable links if no links are available
					if(links_list.get(pos).length == 0) {
						links_p.setEnabled(false);
						links_p.setImageResource(R.drawable.ic_link_disabled);
					}
					
					//disable reply to self
					if (reply_list.get(pos).equals("") || login_cookie.equals("")) {
						reply_p.setEnabled(false);
						reply_p.setImageResource(R.drawable.ic_write_disabled);
					}
				
					collapse_p.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							dialog.dismiss();
							collapseCommentThread(pos);	
						}
					});
					
					upvote_p.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							//ping server with request
							PingServer ping = new PingServer();
							ping.execute(new String[] { "https://news.ycombinator.com/" + upvote_list.get(pos) });
							
							// change count and color			           
				            Integer image 		= mlist.get(pos).image;
				            String submitter 	= mlist.get(pos).submitter;
				            Spanned text 		= mlist.get(pos).comment;
				            String time 		= mlist.get(pos).time;
				            Integer draw 		= R.drawable.blue;
				            Integer padding		= mlist.get(pos).padding;
				            
				            mlist.remove(pos);
				            mlist.add(pos,new CustomCommentListItem(image,submitter,text,draw, time, padding, "no"));
				            adapter.notifyDataSetChanged();
				            dialog.dismiss();
				            
				            //to disable voting again
				            downvote_list.remove(pos);
				            downvote_list.add(pos, "");
				            upvote_list.remove(pos);
				            upvote_list.add(pos, "");
						}
					});
					
					downvote_p.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							//ping server with request
							PingServer ping = new PingServer();
							ping.execute(new String[] { "https://news.ycombinator.com/" + downvote_list.get(pos) });
							
							// change count and color
				            Integer image 		= mlist.get(pos).image;
				            String submitter 	= mlist.get(pos).submitter;
				            Spanned text 		= mlist.get(pos).comment;
				            String time 		= mlist.get(pos).time;
				            Integer draw 		= R.drawable.blue;
				            Integer padding		= mlist.get(pos).padding;
				            
				            mlist.remove(pos);
				            mlist.add(pos,new CustomCommentListItem(image,submitter,text,draw,time, padding, "no"));
				            adapter.notifyDataSetChanged();
				            dialog.dismiss();
				            
				            //to disable voting again
				            downvote_list.remove(pos);
				            downvote_list.add(pos, "");
				            upvote_list.remove(pos);
				            upvote_list.add(pos, "");
						}
					});
					
					links_p.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							dialog.dismiss();
							final String[] values 			= links_list.get(pos); // the links associated with the comment
							
							//if more than one urls show listview to select string, else open only url
							if(values.length > 1) {
								final Dialog dialog_link = new Dialog(context);
								dialog_link.setContentView(R.layout.comment_links);
								
								ListView links = (ListView) dialog_link.findViewById(R.id.comment_links_listview);
								
								//String[] links_array = (String[]) links_list.get(pos).toArray();
								//Adapter mAdapter = new SimpleCursorAdapter(context, android.R.layout.simple_list_item_1, null, links_array, android.R.id.text1, 0);
								
								ArrayAdapter<String> adapter 	= new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, values);
								links.setAdapter(adapter);
								
								dialog_link.setTitle("Select link");
								dialog_link.setCancelable(true);
								dialog_link.show();
								
								links.setOnItemClickListener( new OnItemClickListener() {
		
									@Override
									public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
										dialog_link.dismiss();
										Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(values[arg2]));
										startActivity(browserIntent);
										overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
									}
								
								});
							} else {
								Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(values[0]));
								startActivity(browserIntent);
								overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
							}
							
						}
					});
					
					reply_p.setOnClickListener(new OnClickListener() {
						
						@Override
						public void onClick(View v) {
							dialog.dismiss();
							final Dialog dialog_reply = new Dialog(context);
							dialog_reply.setContentView(R.layout.comment_reply);
							dialog_reply.setCancelable(false);
							dialog_reply.setTitle("Replying to "+ mlist.get(pos).submitter);
							dialog_reply.show();
							
							final EditText reply_text 	= (EditText) dialog_reply.findViewById(R.id.comment_reply);
							Button send 				= (Button) dialog_reply.findViewById(R.id.reply_confirm);
							Button cancel 				= (Button) dialog_reply.findViewById(R.id.reply_cancel);
							
							cancel.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									dialog_reply.dismiss();
									
								}
							});
							
							send.setOnClickListener(new OnClickListener() {
								
								@Override
								public void onClick(View v) {
									dialog_reply.dismiss();
									SendReply reply = new SendReply();
									reply.execute(new String[] { reply_list.get(pos), reply_text.getText().toString(), ""});
									
									//add comment to list
									Integer image = mlist.get(pos).image;
									
									if (image < 200) {
										image += 20;
									}
									
									SharedPreferences settings = getSharedPreferences("Cookie", 0);
							        String submitter 	= settings.getString("username", "ME");
						            
						            Spanned text 		= reply_text.getText();
						            Integer draw 		= R.drawable.blue;
						            String time 		= "0 seconds ago";
						            
						            Integer new_pos 	= pos + 1;
						            
						            mlist.add(new_pos,new CustomCommentListItem(image,submitter,text,draw,time, null, "no"));
						            adapter.notifyDataSetChanged();
						            
						            //change lists
						            upvote_list.add(new_pos,"");
						        	downvote_list.add(new_pos,"");
						        	reply_list.add(new_pos,"");
						        	nested_list.add(new_pos,"");
						        	links_list.add(new_pos, new String[] {});
									
								}
							});
					
						}
					});
				}
			}
        	
        });
        
        
        //causes more problem than its worth
        /*list.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				if((firstVisibleItem > 3) && !hidden ) {
					comment_header.animate().y(-1*comment_header.getHeight()).setListener(new AnimatorListener() {
						
						@Override
						public void onAnimationStart(Animator animation) {}
						
						@Override
						public void onAnimationRepeat(Animator animation) {}
						
						@Override
						public void onAnimationEnd(Animator animation) {
							comment_header.setVisibility(View.GONE);
						}
						
						@Override
						public void onAnimationCancel(Animator animation) {}
					});
					hidden = true;
					//comment_header.setVisibility(View.GONE);
				} else if ((firstVisibleItem < 3) && hidden) {
					//comment_header.setVisibility(View.VISIBLE);
					comment_header.animate().y(0).setListener(new AnimatorListener() {
						
						@Override
						public void onAnimationStart(Animator animation) {
							comment_header.setVisibility(View.VISIBLE);
							//list.animate().y(comment_header.getHeight());
						}
						
						@Override
						public void onAnimationRepeat(Animator animation) {}
						
						@Override
						public void onAnimationEnd(Animator animation) {}
						
						@Override
						public void onAnimationCancel(Animator animation) {}
					});
					hidden = false;
				}
			}
		});*/
	}
	
	@Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
	
	@Override
	public boolean onPrepareOptionsMenu (Menu menu) {
	    if (article_url.startsWith("http")) {
	    	menu.findItem(R.id.menu_article).setVisible(true);
	    } else {
	    	menu.findItem(R.id.menu_article).setVisible(false);
	    }
	    
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
		Bundle bundle = new Bundle();
		switch (menuItem.getItemId()) {
	    	case android.R.id.home:
	    		SharedPreferences prefs = getSharedPreferences("Cookie", 0);
	            String cookie_login = prefs.getString("login_cookie", "");
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
	    	case R.id.menu_article:
	    		
	    		SharedPreferences settings = getSharedPreferences("Settings", 0);
				Boolean e_browser = settings.getBoolean("external_browser", false);
				
				//add link to read
				LinksDataSource links = new LinksDataSource(context);
		        links.open();
		        links.addLink(article_url);
		        links.close();
				
				if (!e_browser) {
					mIntent = new Intent(this,Article.class);
					bundle = new Bundle();
					bundle.putString("article_url", article_url);
	  				bundle.putString("comment_url", comment_url);
	  				bundle.putString("submission_title", title);
	  				bundle.putString("login_cookie", login_cookie);
	  				mIntent.putExtras(bundle);
	  				this.startActivity(mIntent);
	  				this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				} else {
	  				String url = article_url;
	  				
	  				if (!url.startsWith("http")) {
	  					url = "https://news.ycombinator.com/" + url;
	  				}
	  				
	  				mIntent = new Intent(Intent.ACTION_VIEW);
	  				mIntent.setData(Uri.parse(url));
	  				this.startActivity(mIntent);
	  				this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				}
	    		
	    		return true;
	    	case R.id.menu_about:
	    		mIntent = new Intent(this, About.class);
	    		this.startActivity(mIntent);
	    		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	    		return true;
	    	case R.id.menu_login:
	    		mIntent = new Intent(this, Login.class);
	    		this.startActivity(mIntent);
	    		this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
	    		finish();
	    		return true;
	    	case R.id.menu_logout:
	    		// delete cookie to logout
	    		SharedPreferences cookie = getSharedPreferences("Cookie", 0);
				SharedPreferences.Editor editor = cookie.edit();
				editor.putString("login_cookie", "");
				editor.commit();
				
				//unregister from GCM
				GCMRegistrar.unregister(context);
				
				//go back to list
				mIntent = new Intent(context,ListActivity.class);
				bundle.putString("login_cookie", "");
				mIntent.putExtras(bundle);
				Comments.this.startActivity(mIntent);
				Comments.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
				finish();
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
	
	private class DownloadTask extends AsyncTask<String, Void, Document> {
    	
    	public DownloadTask(){}
		@Override
		protected Document doInBackground(String... urls) {
			Document doc = null;
			for(String url : urls) {
				try {
					//set timeouts
    		        int timeoutConnection 		= 5000;
    		        int timeoutSocket 			= 15000;
    		        HttpParams httpParameters 	= new BasicHttpParams();
    		        
    		        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    		        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
    		        
    				HttpClient httpclient 	= new DefaultHttpClient(httpParameters);
    			    HttpGet httpget 		= new HttpGet(url);
    			    httpget.setHeader( "Cookie", login_cookie );
    			    
    			    Integer i = 0;
    			    Boolean success = false;
    			    
    			    while((i < 3) && !success) {
    			    	HttpResponse response = httpclient.execute(httpget);
    			    	
    			    	if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
    			    		HttpEntity httpEntity = response.getEntity();
    			    	
    			    		String result 	= EntityUtils.toString(httpEntity);
    			    	
    			    		doc 			= Jsoup.parse(result);
    			    		
    			    		success 		= true;
    			    	} else {
    			    		success			= false;
    			    		Log.i("GetCommentsStatusCode", response.getStatusLine().getStatusCode()+"");
    			    		Thread.sleep((long) (Math.random()*5*1000));
    			    	}
    			    }
				} catch (IOException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			return doc;
		}
    	
		protected void onPostExecute(Document doc) {
			try {
				fnid_top = doc.select("input[name=fnid]").attr("value");
				//fnid_top = "";
				
				//Extracts header info
				Elements title_block = doc.select("body>center>table>tbody>tr>td>table>tbody:has(tr>td[class=title])").select("tr");
				
				upvote_top = "";
				
				if (!title_block.select("a[href^=vote").attr("href").isEmpty()) {
					upvote_top = title_block.select("a[href^=vote").attr("href");
				}
				
				points_top = title_block.select("td[class=subtext]>span").text().replaceAll(" points", "").replaceAll(" point", "").replaceAll("discuss", "");
				String submitter_top = title_block.select("td[class=subtext]>a[href^=user]").text();
				
				String text_top = "";
				try {
					text_top = title_block.select("tr").get(3).select("td").last().text();
				} catch (Exception e) {
					// TODO: handle exception
				}
				
				TextView title_view_top 	= (TextView) findViewById(R.id.title_top);
				TextView points_view_top 	= (TextView) findViewById(R.id.points_top);
				TextView submitter_view_top = (TextView) findViewById(R.id.submitter_top);
				TextView text_view_top 		= (TextView) findViewById(R.id.text_top);
				ImageView image_top 		= (ImageView) findViewById(R.id.left_image_top);
				
				text_top = Jsoup.clean(text_top, Whitelist.basic());
				
				//extract part between <pre> and </pre>
				if(text_top.contains("<pre>")) {
					String temp_text = text_top;
					
					while(temp_text.contains("<pre>")) {
						Integer pos_start = temp_text.indexOf("<pre>");
						Integer pos_end = temp_text.indexOf("</pre>") + 6; // 5 for length of </pre>
						
						String code = temp_text.substring(pos_start, pos_end);
						code 		= code.replaceAll("\\r\\n|\\r|\\n", "<br>");
						code		= code.replaceAll("  ", "&nbsp;&nbsp;");
						code 		= code.replace("<pre>", "");
						code 		= code.replace("</pre>", "");
						code		= code.replace("<code>", "<blockquote><tt>");
						code		= code.replace("</code>", "</tt></blockquote>");
						
						temp_text = temp_text.substring(0, pos_start) + code + temp_text.substring(pos_end);
					}
					text_top = temp_text;
				}
				
				title_view_top.setText(title);
				points_view_top.setText(points_top);
				submitter_view_top.setText("Submitted by " + submitter_top);
				//text_view_top.setText(Html.fromHtml(text_top));
				text_view_top.setText(Html.fromHtml(text_top));
				
				
				
				if (upvote_top.equals("")) {
					image_top.setImageResource(R.drawable.blue);
				} else {
					image_top.setImageResource(R.drawable.white);
				}
				
				Elements comments = doc.select("body>center>table>tbody>tr>td>table>tbody>tr>td>table>tbody>tr");
				
				int com_count = 0;
				for(Element comment : comments) {
					try {
						int image_l = R.drawable.white;
						
						String reply_l 		= comment.select("p>font>u>a[href^=reply]").get(0).attr("href");
						String up_l 		= comment.select("a[id^=up]").attr("href");
						String down_l 		= comment.select("a[id^=down]").attr("href");						
						String submitter 	= comment.select("span[class=comhead]>a").get(0).text();
						String text 		= comment.select("span[class=comment]").html();
						//String nested 		= comment.select("img[src=http://ycombinator.com/images/s.gif]").attr("width");
						String nested 		= comment.select("img[src=s.gif]").attr("width");
						Integer image 		= R.drawable.c_1;
						String time 		= comment.select("span[class=comhead]").text().replaceAll(submitter, "").replaceAll("\\|","").replaceAll("link", "");
						String commentid 	= comment.select("span[class=comhead]>a").get(1).attr("href");
						
						if(commentid.equals(COMMENT_ID)) {
							COMMENT_POS = com_count;
						}
						
						//extract part between <pre> and </pre>
						if(text.contains("<pre>")) {
							String temp_text = text;
							
							while(temp_text.contains("<pre>")) {
								Integer pos_start 	= temp_text.indexOf("<pre>");
								Integer pos_end 	= temp_text.indexOf("</pre>") + 5; // 5 for length of </pre>
								
								String code = temp_text.substring(pos_start, pos_end);
								code 		= code.replaceAll("\\r\\n|\\r|\\n", "<br>");
								code		= code.replaceAll("  ", "&nbsp;&nbsp;");
								code 		= code.replace("<pre>", "");
								code 		= code.replace("</pre>", "");
								code		= code.replace("<code>", "<blockquote><tt>");
								code		= code.replace("</code>", "</tt></blockquote>");
								
								temp_text 	= temp_text.substring(0, pos_start) + code + temp_text.substring(pos_end);
							}
							text = temp_text;
						}
						
						if (text.contains("<p>") && text.contains("reply?id=")) { // removed reply link from multi paragraph replies
							text = text.substring(0, text.lastIndexOf("<p>"));
						} else {
							text = text + "<p></p>"; // maintains uniformity with multiparagraph lines
						}
						
						text 				= Jsoup.clean(text, Whitelist.basic());
						Spanned text_sp 	= Html.fromHtml(text);
						
						Elements a_links 	= Jsoup.parse(text).select("a[href]");
						
						//extract all links in the reply
						String[] url_list 	= new String[a_links.size()];
						int i = 0;
						for (Element a : a_links) {
							url_list[i++] = a.attr("href");
						}
						
						//Log.i("Nested", nested + " nested");
						
						if (nested.equals("0") ) {
							image = 0;
						} else if (nested.equals("40")) {
							image	= 15;
						} else if (nested.equals("80")) {
							image	= 30;
						} else if (nested.equals("120")) {
							image	= 45;
						} else if (nested.equals("160")) {
							image	= 60;
						} else if (nested.equals("200")) {
							image	= 75;
						} else if (nested.equals("240")) {
							image	= 90;
						} else if (nested.equals("280")) {
							image	= 105;
						} else if (nested.equals("320")) {
							image	= 120;
						} else if (nested.equals("360")) {
							image	= 135;
						} else {
							image	= 0;
						}
						
						if (up_l.equals("")) {
							image_l = R.drawable.blue;
						}
						
						mlist.add(new CustomCommentListItem(image, submitter, text_sp, image_l, time, null, "no"));
						
						// add all data to global list
						links_list.add(url_list);
						upvote_list.add(up_l);
						downvote_list.add(down_l);
						reply_list.add(reply_l);
						nested_list.add(nested);
					} catch (Exception e) {
						e.printStackTrace();
					}
					com_count = com_count + 1;
				}
				
				adapter.notifyDataSetChanged();
	
				
				if (!text_top.equals("")) {
					drawer_handle.setVisibility(View.VISIBLE);
				}
				
		        blue_bar.setVisibility(View.VISIBLE);
		        title_view.setVisibility(View.VISIBLE);
				spinner.setVisibility(View.INVISIBLE);
		        list.setVisibility(View.VISIBLE);
		        
		        //required in case user arrived at activity through notification
		        list.setSelection(COMMENT_POS);
		        //drawer.setVisibility(View.VISIBLE);
				return;
			} catch (Exception e) {
				e.printStackTrace();
				
				spinner.setVisibility(View.INVISIBLE );
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
    			alert.setTitle("Error");
    			alert.setMessage("Sorry there is a network problem and your request cannot be completed");
    			alert.setCancelable(false);
    			alert.setPositiveButton("Ok", null);
    			alert.create().show();
			}
		}
    }
	
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.comment_menu, menu);
        
        if(!article_url.startsWith("http")) {
        	menu.findItem(R.id.menu_article).setEnabled(false);
        } else {
        	menu.findItem(R.id.menu_article).setEnabled(true);
        }
        
        //Share Intent
        ShareActionProvider mShareActionProvider = (ShareActionProvider) menu.findItem(R.id.menu_share).getActionProvider();
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, title + " " + comment_url);
        mShareActionProvider.setShareIntent(shareIntent);
        return true;
    }

	//Asynchronous server ping
	public class PingServer extends AsyncTask<String, Void, Document> {
		
		public PingServer(){}
		@Override
		protected Document doInBackground(String... urls) {
			for (String link : urls) {
				try {
					HttpClient client 	= new DefaultHttpClient();
		            HttpGet request 	= new HttpGet(link);
		            request.setHeader("Cookie", login_cookie);
		            client.execute(request);
				} catch (ClientProtocolException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			return null;
		}
			
		protected void onPostExecute(Document doc) {
			
		}
	}
	
	//Asynchronous server ping
	public class SendReply extends AsyncTask<String, Void, String> {
			
		public SendReply(){}
		@Override
		protected String doInBackground(String... params) {
			String url = params[0];
			String text = params[1];
			String fnid = params[2];
			
			//fetch fnid is don't exist
			if (fnid.equals("")) {
				Document doc = null;
				try {
					//set timeouts
    		        int timeoutConnection 		= 5000;
    		        int timeoutSocket 			= 15000;
    		        HttpParams httpParameters 	= new BasicHttpParams();
    		        
    		        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    		        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
    		        
    				HttpClient httpclient 	= new DefaultHttpClient(httpParameters);
    			    HttpGet httpget 		= new HttpGet("https://news.ycombinator.com/" + url);
    			    httpget.setHeader( "Cookie", login_cookie );
    			    
    			    HttpResponse response 	= httpclient.execute(httpget);
    				HttpEntity httpEntity 	= response.getEntity();
    			    
    				String result 			= EntityUtils.toString(httpEntity);
    				
    				doc 					= Jsoup.parse(result);
					//doc = Jsoup.connect("http://news.ycombinator.com/" + url).header("Cookie", login_cookie).get();
					fnid = doc.select("input[name=fnid]").first().attr("value");
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			//Submit reply
			HttpClient httpclient 				= new DefaultHttpClient();
		    HttpPost httppost 					= new HttpPost("https://news.ycombinator.com/r");
			List<NameValuePair> nameValuePairs 	= new ArrayList<NameValuePair>(2);
	        
			nameValuePairs.add(new BasicNameValuePair("fnid", fnid));
	        nameValuePairs.add(new BasicNameValuePair("text", text));
	        
	        try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			} catch (UnsupportedEncodingException e1) {
				e1.printStackTrace();
			}
	        
	        httppost.addHeader("Content-type", "application/x-www-form-urlencoded");
	        httppost.addHeader("Accept","text/plain");
	        httppost.addHeader("Cookie", login_cookie);
	        
	        try {
	        	int count 		= 0;
	        	Boolean success = false;
	        	
	        	while((count < 3) && !success) {
	        		HttpResponse response = httpclient.execute(httppost);
	        		
	        		if(response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
	        			success	= true;
	        		} else {
	        			Log.i("SendReplyStatusCode", response.getStatusLine().getStatusCode()+"");
	        		}
	        	}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			return null;
		}
				
		protected void onPostExecute(Document doc) {
			
		}
	}
	
	private void collapseCommentThread(Integer pos) {
		Integer nestState 	= mlist.get(pos).image;
		
		//set collapse header
		mlist.get(pos).collapse = "header";
		
		Integer i 	= pos+1;
		while(i < mlist.size()) {
			if(mlist.get(i).image > nestState) {
				mlist.get(i).collapse = "yes";
			} else {
				break;
			}
			i++;
		}
		
		adapter.notifyDataSetChanged();
	}
	
	private void expandCommentThread(Integer pos) {
		Integer nestState 	= mlist.get(pos).image;
		
		//set header
		mlist.get(pos).collapse = "no";
		
		Integer i 	= pos+1;
		while(i < mlist.size()) {
			if(mlist.get(i).image > nestState) {
				mlist.get(i).collapse = "no";
			} else {
				break;
			}
			i++;
		}
		list.setSelection(pos);
		adapter.notifyDataSetChanged();
	}
	
	@Override
	protected void onStart()
	{
		super.onStart();
		FlurryAgent.onStartSession(this, "YOUR_API_KEY");
	}
}