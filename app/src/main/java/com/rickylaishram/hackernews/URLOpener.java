package com.rickylaishram.hackernews;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

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
import org.jsoup.select.Elements;

/**
 * Created by david on 5/3/14.
 */
public class URLOpener extends Activity {

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent filterIntent = getIntent();
        String comments_url = filterIntent.getDataString();

        new ArticleFetch().execute(comments_url);

    }

    private class ArticleFetch extends AsyncTask<String, Void, Document> {
        String comment_url;
        String login_cookie;

        @Override
        protected Document doInBackground(String... strings) {
            comment_url = strings[0];
            Document doc = null;
            SharedPreferences cookie = getSharedPreferences("Cookie", Context.MODE_PRIVATE);
            login_cookie = cookie.getString("login_cookie", "");
            try {
                //set timeouts
                int timeoutConnection = 5000;
                int timeoutSocket = 15000;
                HttpParams httpParameters = new BasicHttpParams();

                HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
                HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

                HttpClient httpclient = new DefaultHttpClient(httpParameters);
                HttpGet httpget = new HttpGet(comment_url);
                httpget.setHeader("Cookie", login_cookie);

                HttpResponse response = httpclient.execute(httpget);
                HttpEntity httpEntity = response.getEntity();

                String result = EntityUtils.toString(httpEntity);

                doc = Jsoup.parse(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return doc;
        }

        @Override
        protected void onPostExecute(Document doc) {
            try {
                Elements titles = doc.select("body>center>table>tbody>tr>td>table>tbody>tr:has(td[class=title])");
                String article_title = titles.get(0).select("td[class=title]>a").text();
                String article_url = titles.get(0).select("td[class=title]>a").attr("href");
                String comment_url_suffix = comment_url.substring(comment_url.lastIndexOf(".com/") + 4, comment_url.length());
                Bundle bundle = new Bundle();
                bundle.putString("article_url", article_url);
                bundle.putString("comment_url", comment_url_suffix);
                bundle.putString("submission_title", article_title);
                bundle.putString("login_cookie", login_cookie);
                Intent mIntent = new Intent(getApplicationContext(), Comments.class);
                mIntent.putExtras(bundle);
                startActivity(mIntent);
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Sorry there is a network problem and your request cannot be completed",
                        Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

}
