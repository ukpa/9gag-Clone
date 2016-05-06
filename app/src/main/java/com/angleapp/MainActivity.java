package com.angleapp;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.Manifest;
import android.view.View;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

public class MainActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    static int SUCCESSFULL_UPLOAD=111;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    PaginatedQueryList<Post> result;
    SwipeRefreshLayout swipeRefreshLayout;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        swipeRefreshLayout = (SwipeRefreshLayout)findViewById(R.id.mainSwipeRefreshLayout);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        updatePermission();
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.mainCoordinator);
        Snackbar.make(coordinatorLayout,"Signed in Successfully",Snackbar.LENGTH_LONG).show();
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        //this.getSupportActionBar().setTitle("Browse");
        mRecyclerView = (RecyclerView)findViewById(R.id.mainRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemViewCacheSize(40);
        mAdapter = new PostAdapter(result,this);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && fab.isShown()||dy < 0 && fab.isShown())
                    fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState ==  RecyclerView.SCROLL_STATE_IDLE) {
                    fab.show();
                }
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        refreshFeed();

        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    refreshFeed();

                }
            });
        }

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, UploadActivity.class),SUCCESSFULL_UPLOAD);

            }
        });


    }

    private void refreshFeed() {
        Post postToFind = new Post();
        postToFind.setUserId(Application.userId);
        AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        final DynamoDBMapper dbMapper = awsMobileClient.getDynamoDBMapper();

        final DynamoDBQueryExpression<Post> queryExpression = new DynamoDBQueryExpression<Post>()
                .withHashKeyValues(postToFind)
                .withConsistentRead(false)
                .withLimit(20);
        AsyncTask<Void,Void,PaginatedQueryList<Post>> asyncTask = new AsyncTask<Void, Void, PaginatedQueryList<Post>>() {
            @Override
            protected void onPostExecute(PaginatedQueryList<Post> result) {
                super.onPostExecute(result);
                mAdapter = new PostAdapter(result,MainActivity.this);
                Log.d("size of your ass",String.valueOf(result.size()));
                swipeRefreshLayout.setRefreshing(false);
                mRecyclerView.swapAdapter(mAdapter,false);


            }

            @Override
            protected PaginatedQueryList<Post> doInBackground(Void... params) {
                result = dbMapper.query(Post.class, queryExpression);
                Log.d("gahdghagdhagdhgahgdhagd",String.valueOf(result.size()));
                return result;
            }


        };
        asyncTask.execute();
    }



    private void updatePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        10);
            }
        }
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SUCCESSFULL_UPLOAD) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(coordinatorLayout,"Uploaded Successfully",Snackbar.LENGTH_LONG).show();
            }
        }
    }

}
