package com.example.oldhamd.myapplication;


import android.os.AsyncTask;
import android.os.Handler;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toolbar;


import com.facebook.drawee.backends.pipeline.Fresco;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    public static String apiURL = "http://dreamfactory-reason4design.rhcloud.com/api/v2/";
    public static String apiKey = "729d180869a927654f3a95a9799310292fec4a79f00cc225770758d42414788b";
    private RecyclerView mRecycleView;
    private RecyclerView.LayoutManager mLayoutManager;
    private PostAdapter mAdapter;
    private List<PostInfo> postList;

    protected Handler handler;

    private DrawerLayout mDrawerLayout;
    private ListView mMenuList;
    private ActionBarDrawerToggle mDrawerToggle;
    private Toolbar toolbar;
    private SwipeRefreshLayout mSwipeRefreshLayout;

    private CharSequence mDrawerTitle;
    private CharSequence mTitle;
    private TextView tvEmptyView;
    String [] MenuTitles = new String[]{"First Item","Second Item","Third Item","Fourth Item"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);
        setContentView(R.layout.activity_main);
        mTitle = mDrawerTitle = getTitle();
        mDrawerLayout =  (DrawerLayout) findViewById(R.id.drawer_layout);
        mMenuList = (ListView) findViewById(R.id.left_drawer);
        mMenuList.setAdapter(new ArrayAdapter<>(this, R.layout.drawer_list_item, MenuTitles));

        mMenuList.setOnItemClickListener(new DrawerItemClickListener());
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.content_frame);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.green);
        mDrawerToggle = new ActionBarDrawerToggle(
                this,
                mDrawerLayout,
                R.string.drawer_open,
                R.string.drawer_close
        ) {
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mTitle);
                }
                mDrawerToggle.syncState();
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerClosed(drawerView);
                if(getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(mDrawerTitle);
                }
                mDrawerToggle.syncState();
                //invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
            }
        };
        mDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        handler = new Handler();
        //mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar, R.string.drawer_open,  R.string.drawer_close);
        //mDrawerLayout.setDrawerListener(mDrawerToggle);
        mRecycleView = (RecyclerView) findViewById(R.id.rv);
        mLayoutManager = new LinearLayoutManager(this);
        mRecycleView.setLayoutManager(mLayoutManager);
        postList = new ArrayList<>();
        mAdapter = new PostAdapter(postList, mRecycleView);
        mRecycleView.setAdapter(mAdapter);
        mRecycleView.setHasFixedSize(true);



        mSwipeRefreshLayout.post(new Runnable() {
                                     @Override
                                     public void run() {
                                         mSwipeRefreshLayout.setRefreshing(true);
                                         new getDFRecord().execute("pornhub");
                                     }
                                 }
        );

        mAdapter.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        new getDFRecord().execute("pornhub");
                    }
                });
            }
        });
    }

    @Override
    public void onRefresh() {
        // Refresh items
        postList.clear();
        new getDFRecord().execute("pornhub");
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            selectItem(position);
        }
    }

    void onItemsLoadComplete() {
        // Update the adapter and notify data set changed
        // ...

        // Stop refresh animation
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /* Called whenever we call invalidateOptionsMenu() */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // If the nav drawer is open, hide action items related to the content view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(mMenuList);
        menu.findItem(R.id.action_websearch).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        // ActionBarDrawerToggle will take care of this.
        /*
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        */
        return mDrawerToggle.onOptionsItemSelected(item)|| super.onOptionsItemSelected(item);
    }

    private void selectItem(int position) {
        mDrawerLayout.closeDrawer(mMenuList);
    }

    private class getDFRecord extends AsyncTask<String, String, String> {

        @Override
        protected String doInBackground(String...  params){
            String results = "";
            String source = params[0];
            try {
                //results = getRecord( "Post?filter=source%3D" + source + "&limit=50&order=Modified%20DESC" );
                results = getRecord( "Post?limit=10&order=Modified%20DESC" );
            }catch(Exception e){
                e.printStackTrace();
            }
            return results;
        }
        protected void onPostExecute(String result){
            System.out.println(result);
            try {
                JSONObject jsonObj = new JSONObject(result);
                JSONArray resources = jsonObj.getJSONArray("resource");
                for(int i = 0; i < resources.length(); i++){
                    JSONObject ttt = resources.getJSONObject(i);
                    PostInfo item = new PostInfo();
                    item.thumb = ttt.getString("Thumbnail");
                    item.url = ttt.getString("Url");
                    item.title = ttt.getString("Title");
                    item.source = ttt.getString("Source");
                    postList.add(item);

                    System.out.println(item.url);
                }
                mAdapter.notifyItemInserted(postList.size());
                mAdapter.setLoaded();
                mSwipeRefreshLayout.setRefreshing(false);

            }catch (Exception e){
                e.printStackTrace();
            }
        }

        private String getRecord(String api) throws IOException {
            StringBuilder result = new StringBuilder();
            String db = "db/_table/";
            URL url = new URL(apiURL + db + api);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            try {
                conn.setReadTimeout(1000);
                conn.setConnectTimeout(15000);
                conn.setRequestProperty("X-DreamFactory-Api-Key", apiKey);
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

}


