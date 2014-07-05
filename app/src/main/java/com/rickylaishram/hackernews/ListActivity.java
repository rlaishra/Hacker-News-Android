package com.rickylaishram.hackernews;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;

import static com.rickylaishram.util.CommonUitls.isNightMode;

public class ListActivity extends FragmentActivity implements ActionBar.OnNavigationListener {
	
	static ListView list_main;
	static ProgressBar spinner;
	static LinearLayout list_loading;
	static Context context;
	static ListAdapter adapter;
	static String login_cookie;
	static Integer curr_selection;
	static Elements titles, comments, points, submitter;
	static Vector<ArticlesList> mlist	= new Vector<ArticlesList>();
	static String url 					= "https://news.ycombinator.com";
	static String url_next 				= "https://news.ycombinator.com";
	static List<String> article_links 	= new ArrayList<String>();
	static List<String> comment_links 	= new ArrayList<String>();
	static List<String> titles_list 	= new ArrayList<String>();
	static List<String> upvotes_list 	= new ArrayList<String>();
	static List<String> points_list 	= new ArrayList<String>();
	static List<String> submitters_list = new ArrayList<String>();
	static List<String> comments_list 	= new ArrayList<String>();
	static List<String> domains_list 	= new ArrayList<String>();
	static boolean loading 				= true;

    private static final String STATE_SELECTED_NAVIGATION_ITEM = "selected_navigation_item";

    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
    	//StrictMode.enableDefaults();
        super.onCreate(savedInstanceState);

        if(isNightMode(this)) {
            setContentView(R.layout.list_night);
        } else {
            setContentView(R.layout.list);
        }
        
        context 		= this;
        list_main 		= (ListView) findViewById(R.id.listView1);
        list_loading 	= (LinearLayout) findViewById(R.id.list_loading_indicator);
        spinner 		= (ProgressBar) findViewById(R.id.main_list_spinner);
        
        //get default settings
        SharedPreferences settings 	= getSharedPreferences("Settings", 0);
		Integer d_page 				= settings.getInt("default_page", 0);
		Integer color 				= settings.getInt("color_scheme", 0);
        
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
        
        //home icon color change for logged out users
        SharedPreferences cookie = getSharedPreferences("Cookie", 0);
        if(cookie.getString("login_cookie", "").equals("")) {
        	actionBar.setLogo(R.drawable.icon_small_bw);
        } else if(color == 0){
        	actionBar.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_orange));
        }
        
        // Set up the dropdown list navigation in the action bar.
        actionBar.setListNavigationCallbacks(
                // Specify a SpinnerAdapter to populate the dropdown list.
                new ArrayAdapter(
                        actionBar.getThemedContext(),
                        android.R.layout.simple_list_item_1,
                        android.R.id.text1,
                        new String[]{
                                getString(R.string.title_section1),
                                getString(R.string.title_section2),
                                getString(R.string.title_section3),
                                getString(R.string.title_section4),
                                getString(R.string.title_section5),
                                getString(R.string.title_section6),
                        }),
                this);
        actionBar.setSelectedNavigationItem(d_page);
        
        // load cookie
        Bundle bundle = getIntent().getExtras();
        login_cookie = bundle.getString("login_cookie");
        
        list_main.setOnScrollListener(new EndlessScrollListener());
    }
    
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        //overridePendingTransition(R.anim.push_right_in, R.anim.push_right_out);
    }
    
    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState.containsKey(STATE_SELECTED_NAVIGATION_ITEM)) {
            getActionBar().setSelectedNavigationItem(
                    savedInstanceState.getInt(STATE_SELECTED_NAVIGATION_ITEM));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_SELECTED_NAVIGATION_ITEM,
                getActionBar().getSelectedNavigationIndex());
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
    	
   		getMenuInflater().inflate(R.menu.list, menu);
 
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

    @Override
    public boolean onNavigationItemSelected(int position, long id) {
        // When the given tab is selected, show the tab contents in the container
        Fragment fragment = new Content();
        Bundle args = new Bundle();
        args.putInt(Content.ARG_SECTION_NUMBER, position + 1);
        fragment.setArguments(args);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
        return true;
    }
    
    public static class Content extends Fragment {
		public Content(){}
    	
    	public static final String ARG_SECTION_NUMBER = "section_number";
    	
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        	spinner.setVisibility(View.VISIBLE);
            list_main.setVisibility(View.INVISIBLE);
            list_loading.setVisibility(View.INVISIBLE);
        	Bundle args = getArguments();
            curr_selection = args.getInt(ARG_SECTION_NUMBER);
            
            if (curr_selection == 1) {
            	url = "https://news.ycombinator.com";
            } else if (curr_selection == 2) {
            	url = "https://news.ycombinator.com/newest";
            } else if (curr_selection == 3) {
            	url = "https://news.ycombinator.com/ask";
            } else if (curr_selection == 4) {
            	url = "https://news.ycombinator.com/best";
            } else if (curr_selection == 5) {
                url = "https://news.ycombinator.com/show";
            } else if (curr_selection == 6) {
                url = "https://news.ycombinator.com/shownew";
            }
            
            //clear everything
            titles_list.clear();
        	article_links.clear();
        	comment_links.clear();
        	upvotes_list.clear();
        	points_list.clear();
        	submitters_list.clear();
        	comments_list.clear();
        	domains_list.clear();
            
            // Download task loads asynchronously
            DownloadTask task = new DownloadTask();
            task.execute(new String[]{url});
            
            return null;
        }
        
        // Asynchronous article list download
        private class DownloadTask extends AsyncTask<String, Void, Document> {
        	
        	public DownloadTask(){}
    		@Override
    		protected Document doInBackground(String... urls) {
    			Document doc = null;
    			
    			try {
    				//set timeouts
    		        int timeoutConnection 		= 5000;
    		        int timeoutSocket 			= 15000;
    		        HttpParams httpParameters 	= new BasicHttpParams();
    		        
    		        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
    		        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

    				HttpClient httpclient = new DefaultHttpClient(httpParameters);
    			    HttpGet httpget = new HttpGet(url);
    			    httpget.setHeader( "Cookie", login_cookie );
    			    
    			    HttpResponse response = httpclient.execute(httpget);
    				HttpEntity httpEntity = response.getEntity();
    			    
    				String result 	= EntityUtils.toString(httpEntity);
    				
    				doc 			= Jsoup.parse(result);
				} catch (Exception e) {
					e.printStackTrace();
				}
    			return doc;
    		}
        	
    		protected void onPostExecute(Document doc) {
    			try {   				
    				mlist.clear();
        			// Adds the item to the adapter

                    if(isNightMode(context)) {
                        adapter = new ListAdapter(context, R.layout.list_layout_night, mlist);
                    } else {
        			    adapter = new ListAdapter(context, R.layout.list_layout, mlist);
                    }
        	        list_main.setAdapter(adapter);
        	        
    				Elements titles = doc.select("body>center>table>tbody>tr>td>table>tbody>tr:has(td[class=title])");
    				Elements subtext = doc.select("body>center>table>tbody>tr>td>table>tbody>tr:has(td[class=subtext])");
        			 
                    Integer i = 0;
                    for (Element st: subtext) {  
                    	try {
	                    	String point_i 			= subtext.get(i).select("td[class=subtext]>span").text().replaceAll(" points", "").replaceAll(" point", "");
	        				String upvote_i 		= titles.get(i).select("td>center>a[href^=vote]").attr("href");
	        				String title_i 			= titles.get(i).select("td[class=title]>a").text();
	        				String domain 			= titles.get(i).select("td[class=title]>span").text();
	        				String article_url_i 	= titles.get(i).select("td[class=title]>a").attr("href");
	        				String submitter_i 		= subtext.get(i).select("a[href*=user]").text();
	        				String comments_i 		= subtext.get(i).select("a[href*=item]").text().replaceAll("discuss", "0 comments");
	        				String comment_url_i 	= subtext.get(i).select("a[href*=item]").attr("href");
	        				
	        				//domain = domain.replace("(", "").replace(")", "");
	        				
	                    	int draw_left = R.drawable.white;
	                    	
	                    	article_links.add(article_url_i);
	                    	comment_links.add(comment_url_i);
	                    	titles_list.add(title_i);                   	
	                    	points_list.add(point_i);
	                        comments_list.add(comments_i);
	                        submitters_list.add(submitter_i);
	                        domains_list.add(domain);
	                        
	                        if (upvote_i.isEmpty()) {  // item has been upvoted already
	                    		upvotes_list.add("");
	                    		draw_left = R.drawable.blue;
	                    	} else {
	                    		upvotes_list.add(upvote_i);
	                    	}
	                        
	                        mlist.add(new ArticlesList(point_i, title_i, "by " + submitter_i, comments_i, draw_left, upvote_i, article_url_i, comment_url_i, login_cookie, context, domain));	
                    	} catch (Exception e) {
							e.printStackTrace();
						}
                    	
                    	i++;
                    }
                    
                    // Next page
                    url_next = doc.select(".title:not(span[class=comhead])>a[href]:contains(more)").last().attr("href");
                    
                    //Toast.makeText(context, url_next, Toast.LENGTH_LONG).show();
                    
                    spinner.setVisibility(View.INVISIBLE);
                    list_main.setVisibility(View.VISIBLE);
                    loading = false;
        			return;
    			} catch (Exception e) {
    				//StackTraceElement[] err = e.getStackTrace();
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
    }
    
    
    public boolean onOptionsItemSelected(MenuItem menuItem) {
		Intent mIntent;
		switch (menuItem.getItemId()) {
			case R.id.reload:
				spinner.setVisibility(View.VISIBLE);
		        list_main.setVisibility(View.INVISIBLE);
		        list_loading.setVisibility(View.INVISIBLE);
		        
		        if (curr_selection == 1) {
		        	url = "https://news.ycombinator.com";
		        } else if (curr_selection == 2) {
		        	url = "https://news.ycombinator.com/newest";
		        } else if (curr_selection == 3) {
		        	url = "https://news.ycombinator.com/ask";
		        }
		        
		        //clear everything
		        titles_list.clear();
		    	article_links.clear();
		    	comment_links.clear();
		    	upvotes_list.clear();
		    	points_list.clear();
		    	submitters_list.clear();
		    	comments_list.clear();
		    	
		    	Reload task = new Reload();
		        task.execute(new String[]{url});
		        
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
				
				//go back to list
				mIntent = new Intent(context,ListActivity.class);
				Bundle bundle = new Bundle();
				bundle.putString("login_cookie", "");
				mIntent.putExtras(bundle);
				ListActivity.this.startActivity(mIntent);
				ListActivity.this.overridePendingTransition(R.anim.push_left_in, R.anim.push_left_out);
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
    
    // Asynchronous article list download
    private class Reload extends AsyncTask<String, Void, Document> {
    	
    	public Reload(){}
		@Override
		protected Document doInBackground(String... urls) {
			Document doc = null;
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
			    
			    HttpResponse response = httpclient.execute(httpget);
				HttpEntity httpEntity = response.getEntity();
			    
				String result 	= EntityUtils.toString(httpEntity);
				
				doc 			= Jsoup.parse(result);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return doc;
		}
    	
		protected void onPostExecute(Document doc) {
			try {
				mlist.clear();
    			// Adds the item to the adapter
                if(isNightMode(context)) {
                    adapter = new ListAdapter(context, R.layout.list_layout_night, mlist);
                } else {
                    adapter = new ListAdapter(context, R.layout.list_layout, mlist);
                }
    	        list_main.setAdapter(adapter);

				Elements titles 	= doc.select("body>center>table>tbody>tr>td>table>tbody>tr:has(td[class=title])");
				Elements subtext 	= doc.select("body>center>table>tbody>tr>td>table>tbody>tr:has(td[class=subtext])");

                Integer i = 0;
                for (Element st: subtext) {
                	try {
	                	String point_i 			= subtext.get(i).select("td[class=subtext]>span").text().replaceAll(" points", "").replaceAll(" point", "");
	    				String upvote_i 		= titles.get(i).select("td>center>a[href^=vote]").attr("href");
	    				String title_i 			= titles.get(i).select("td[class=title]>a").text();
	    				String domain 			= titles.get(i).select("td[class=title]>span").text();
	    				String article_url_i 	= titles.get(i).select("td[class=title]>a").attr("href");
	    				String submitter_i 		= subtext.get(i).select("a[href*=user]").text();
	    				String comments_i 		= subtext.get(i).select("a[href*=item]").text().replaceAll("discuss", "0 comments");
	    				String comment_url_i 	= subtext.get(i).select("a[href*=item]").attr("href");
	    				
	    				//domain = domain.replace("(", "").replace(")", "");
	    				
	                	int draw_left = R.drawable.white;
	                	
	                	article_links.add(article_url_i);
	                	comment_links.add(comment_url_i);
	                	titles_list.add(title_i);                   	
	                	points_list.add(point_i);
	                    comments_list.add(comments_i);
	                    submitters_list.add(submitter_i);
	                    
	                    if (upvote_i.isEmpty()) {  // item has been upvoted already
	                		upvotes_list.add("");
	                		draw_left = R.drawable.blue;
	                	} else {
	                		upvotes_list.add(upvote_i);
	                	}
	                    
	                    mlist.add(new ArticlesList(point_i, title_i, "by " + submitter_i, comments_i, draw_left, upvote_i, article_url_i, comment_url_i, login_cookie, context, domain));
	                } catch (Exception e) {
						e.printStackTrace();
					}
                	
                	i++;
                }
                
                // Next page
                url_next = doc.select(".title:not(span[class=comhead])>a[href]:contains(more)").last().attr("href");
                
                spinner.setVisibility(View.INVISIBLE);
                list_main.setVisibility(View.VISIBLE);
                loading = false;
    			return;
			} catch (Exception e) {
				spinner.setVisibility(View.INVISIBLE );
				AlertDialog.Builder alert = new AlertDialog.Builder(context);
    			alert.setTitle("Error");
    			alert.setMessage("Sorry there is a network problem and your request cannot be completed");
    			alert.setCancelable(false);
    			alert.setPositiveButton("Ok", null);
    			alert.create().show();
    			
    			e.printStackTrace();
			}
		}
    }
    
    
    //implements endless scrolling
    public class EndlessScrollListener implements OnScrollListener {
    	
        private int visibleThreshold = 3;
        private int currentPage = 0;
        private int previousTotal = 0;

        public EndlessScrollListener() {
        }
        
        public EndlessScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
            	loading = true;
            	list_loading.setVisibility(View.VISIBLE);
            	DownloadTask task = new DownloadTask();
                task.execute(new String[]{url_next});
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
        
        // Asynchronous article list download
        private class DownloadTask extends AsyncTask<String, Void, Document> {
        	
        	public DownloadTask(){}
    		@Override
    		protected Document doInBackground(String... urls) {
    			Document doc = null;
    			try {
    				if (url_next.startsWith("/")) {
    					//set timeouts
        		        int timeoutConnection 		= 5000;
        		        int timeoutSocket 			= 15000;
        		        HttpParams httpParameters 	= new BasicHttpParams();
        		        
        		        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        		        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        		        
        				HttpClient httpclient = new DefaultHttpClient(httpParameters);
        			    HttpGet httpget = new HttpGet("https://news.ycombinator.com"+url_next);
        			    httpget.setHeader( "Cookie", login_cookie );
        			    
        			    HttpResponse response = httpclient.execute(httpget);
        				HttpEntity httpEntity = response.getEntity();
        			    
        				String result 	= EntityUtils.toString(httpEntity);
        				
        				doc 			= Jsoup.parse(result);
    					//doc = Jsoup.connect("https://news.ycombinator.com"+url_next).header("Cookie", login_cookie).get();
    				} else {
    					//set timeouts
        		        int timeoutConnection 		= 5000;
        		        int timeoutSocket 			= 15000;
        		        HttpParams httpParameters 	= new BasicHttpParams();
        		        
        		        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        		        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        		        
        				HttpClient httpclient = new DefaultHttpClient(httpParameters);
        			    HttpGet httpget = new HttpGet(url + "/" + url_next);
        			    httpget.setHeader( "Cookie", login_cookie );
        			    
        			    HttpResponse response = httpclient.execute(httpget);
        				HttpEntity httpEntity = response.getEntity();
        			    
        				String result 	= EntityUtils.toString(httpEntity);
        				
        				doc 			= Jsoup.parse(result);
    					//doc = Jsoup.connect(url + "/" + url_next).header("Cookie", login_cookie).get();
    				}
				} catch (IOException e1) {
					e1.printStackTrace();
				}	
    			return doc;
    		}
        	
    		protected void onPostExecute(Document doc) {
    			try {
    				Elements titles 	= doc.select("body>center>table>tbody>tr>td>table>tbody>tr:has(td[class=title])");
    				Elements subtext 	= doc.select("body>center>table>tbody>tr>td>table>tbody>tr:has(td[class=subtext])");
    				
                    Integer i = 0;
                    for (Element st: subtext) {  
                    	try {
	                    	String point_i 			= subtext.get(i).select("td[class=subtext]>span").text().replaceAll(" points", "").replaceAll(" point", "");
	        				String upvote_i 		= titles.get(i).select("td>center>a[href^=vote]").attr("href");
	        				String title_i 			= titles.get(i).select("td[class=title]>a").text();
	        				String domain 			= titles.get(i).select("td[class=title]>span").text();
	        				String article_url_i 	= titles.get(i).select("td[class=title]>a").attr("href");
	        				String submitter_i 		= subtext.get(i).select("a[href*=user]").text();
	        				String comments_i 		= subtext.get(i).select("a[href*=item]").text().replaceAll("discuss", "0 comments");
	        				String comment_url_i 	= subtext.get(i).select("a[href*=item]").attr("href");
	        				
	        				//domain = domain.replace("(", "").replace(")", "");
	        				
	                    	int draw_left = R.drawable.white;
	                    	
	                    	article_links.add(article_url_i);
	                    	comment_links.add(comment_url_i);
	                    	titles_list.add(title_i);                   	
	                    	points_list.add(point_i);
	                        comments_list.add(comments_i);
	                        submitters_list.add(submitter_i);
	                        
	                        if (upvote_i.isEmpty()) {  // item has been upvoted already
	                    		upvotes_list.add("");
	                    		draw_left = R.drawable.blue;
	                    	} else {
	                    		upvotes_list.add(upvote_i);
	                    	}
	                        
	                        mlist.add(new ArticlesList(point_i, title_i, "by " + submitter_i, comments_i, draw_left, upvote_i, article_url_i, comment_url_i, login_cookie, context, domain));
	                    } catch (Exception e) {
							e.printStackTrace();
						}
                    	i++;
                    }
                    adapter.notifyDataSetChanged();
                    
                    url_next = doc.select(".title:not(span[class=comhead])>a[href]:contains(more)").last().attr("href");
                    
                    //Toast.makeText(context, url_next, Toast.LENGTH_LONG).show();
                  
                    list_loading.setVisibility(View.INVISIBLE);
        			return;
    			} catch (Exception e) {
    				list_loading.setVisibility(View.INVISIBLE );
    				AlertDialog.Builder alert = new AlertDialog.Builder(context);
        			alert.setTitle("Error");
        			alert.setMessage("Network problem!");
        			alert.setCancelable(false);
        			alert.setPositiveButton("Ok", null);
        			alert.create().show();
        			
        			e.printStackTrace();
    			}    			
    		}
        }
    }
}

	