package com.example.oldhamd.myapplication;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VideoActivity extends AppCompatActivity {

    VideoView vid;
    private String Source;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);
        vid = (VideoView) findViewById(R.id.videoview);
        vid.setMediaController(new MediaController(this));

        Intent myIntent = getIntent();
        String vidUrl = myIntent.getStringExtra("url");
        Source = myIntent.getStringExtra("source");
        new getRecord().execute(vidUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_video, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int h = displaymetrics.heightPixels;
        int w = displaymetrics.widthPixels;
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RecyclerView.LayoutParams.WRAP_CONTENT, RecyclerView.LayoutParams.MATCH_PARENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            System.out.println(vid.getHeight());
            System.out.println(h);
            System.out.println(vid.getWidth());
            System.out.println(w);
            lp.height = h;
            lp.width = vid.getWidth() * h / vid.getHeight();
            vid.setLayoutParams(lp);
        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
            RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            lp.addRule(RelativeLayout.CENTER_IN_PARENT);
            vid.setLayoutParams(lp);
        }
    }

    private class getRecord extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String...  params){
            String results = "";
            String url = params[0];
            System.out.println(url);
            try {
                results = getPage(url);
            }catch(Exception e){
                e.printStackTrace();
            }
            return results;
        }
        protected void onPostExecute(String result){
            try {
                System.out.println(result);
                VideoSource VidSrc = findSource(result, Source);
                String vidUrl = "";
                if(VidSrc.s240p != "none"){
                    vidUrl = VidSrc.s240p;
                    System.out.println(VidSrc.s240p);
                }
                if(VidSrc.s480p != "none"){
                    vidUrl = VidSrc.s480p;
                    System.out.println(VidSrc.s480p);
                }
                if(VidSrc.s720p != "none"){
                    vidUrl = VidSrc.s720p;
                    System.out.println(VidSrc.s720p);
                }
                Uri vidUri = Uri.parse(vidUrl);
                vid.setVideoURI(vidUri);
                vid.start();
            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private String getPage(String api) throws IOException {
            StringBuilder result = new StringBuilder();
            URL url = new URL(api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setReadTimeout(1000);
                conn.setConnectTimeout(15000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.17 (KHTML, like Gecko) Chrome/24.0.1312.52 Safari/537.17");
                conn.setRequestMethod("GET");
                conn.setDoInput(true);
                conn.connect();
                InputStream in = new BufferedInputStream(conn.getInputStream());
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));

                String line;

                while ((line = reader.readLine()) != null) {
                    result.append(line);
                }
            }catch(Exception e){
                e.printStackTrace();
            }
            finally {
                conn.disconnect();
            }
            return result.toString();
        }
    }

    private VideoSource findSource(String result, String source){
        VideoSource src = new VideoSource();
        switch (source){
            case "pornhub":
                int startPt = result.indexOf("var player_quality_240p");
                if(startPt > 0) {
                    System.out.println(startPt);
                    int stSC = result.indexOf(";", startPt);
                    System.out.println(stSC);
                    String tempURL = result.substring(startPt, stSC - 1);
                    System.out.println(tempURL);
                    src.s240p = tempURL.substring(tempURL.indexOf("'"));
                }
                else{
                    src.s240p = "none";
                }
                int startPt480 = result.indexOf("var player_quality_480p");
                if(startPt480 > 0) {
                    int stSC480 = result.indexOf(";", startPt480);
                    String tempURL480 = result.substring(startPt480, stSC480 - 1);
                    System.out.println(tempURL480);
                    src.s480p = tempURL480.replace("var player_quality_480p = '", "");
                }
                else{
                    src.s480p = "none";
                }
                int startPt720 = result.indexOf("var player_quality_720p");
                if(startPt720 > 0) {
                    int stSC720 = result.indexOf(";", startPt720);
                    String tempURL720 = result.substring(startPt720, stSC720 - 1);
                    System.out.println(tempURL720);
                    src.s720p = tempURL720.replace("var player_quality_720p = '", "");
                }
                else{
                    src.s720p = "none";
                }
                break;
            case "youporn":
                startPt = result.indexOf("sources: {");
                int end = result.indexOf("}", startPt);
                String tempURL = result.substring(startPt, end);
                String sources = tempURL.replace("sources: ", "");
                int pos = sources.indexOf("'");
                src.s240p = sources.substring(pos + 1, sources.indexOf(",") - 1);
                System.out.println(src.s240p);
                break;
            case "redtube":
                startPt = result.indexOf("sources: {");
                end = result.indexOf("}", startPt);
                tempURL = result.substring(startPt, end + 1);
                sources = tempURL.replace("sources: ", "");
                try {
                    JSONObject jsonSource = new JSONObject(sources);
                    src.s240p = jsonSource.getString("240");
                    src.s480p = jsonSource.getString("480");
                    src.s720p = jsonSource.getString("720");
                    System.out.println("240="+jsonSource.getString("240"));
                    System.out.println("480="+jsonSource.getString("480"));
                    System.out.println("720="+jsonSource.getString("720"));
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
            case "tube8":
                startPt = result.indexOf("var flashvars = {");
                end = result.indexOf(";", startPt);
                tempURL = result.substring(startPt, end);
                sources = tempURL.replace("var flashvars = ", "");
                try {
                    JSONObject jsonSource = new JSONObject(sources);
                    src.s240p = jsonSource.getString("quality_240p");
                    src.s480p = jsonSource.getString("quality_480p");
                    src.s720p = jsonSource.getString("quality_720p");
                    System.out.println("240="+jsonSource.getString("quality_240p"));
                    System.out.println("480="+jsonSource.getString("quality_480p"));
                    System.out.println("720="+jsonSource.getString("quality_720p"));
                }catch (Exception e){
                    e.printStackTrace();
                }
                break;
        }
        return src;
    }
}
