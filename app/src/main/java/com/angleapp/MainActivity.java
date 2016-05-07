package com.angleapp;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.Manifest;
import android.view.View;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    static int SUCCESSFULL_UPLOAD=111;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    PaginatedQueryList<Post> result1;
    SwipeRefreshLayout swipeRefreshLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;



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

        mAdapter = new PostAdapter(result1,this);

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
        try{
            if (swipeRefreshLayout != null) {
                swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        refreshFeed();

                    }
                });
            }
        }catch (ConcurrentModificationException e){
            e.printStackTrace();
        }



        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, UploadActivity.class),SUCCESSFULL_UPLOAD);

            }
        });
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);


    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TopFragment(), "TOP");
        adapter.addFragment(new HotFragment(), "HOT");
        adapter.addFragment(new NewFragment(), "NEW");


        viewPager.setAdapter(adapter);
    }

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
    }

    private void refreshFeed() {
        Post postToFind = new Post();
        postToFind.setUserId(Application.userId);
        AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
        final DynamoDBMapper dbMapper = awsMobileClient.getDynamoDBMapper();

        final DynamoDBQueryExpression<Post> queryExpression = new DynamoDBQueryExpression<Post>()
                .withHashKeyValues(postToFind)
                .withConsistentRead(true)
                .withLimit(20);
        AsyncTask<Void,Void,PaginatedQueryList<Post>> asyncTask = new AsyncTask<Void, Void, PaginatedQueryList<Post>>() {
            @Override
            protected void onPostExecute(PaginatedQueryList<Post> result) {
                super.onPostExecute(result);
                mAdapter = new PostAdapter(result,MainActivity.this);
                swipeRefreshLayout.setRefreshing(false);
                mRecyclerView.swapAdapter(mAdapter,false);
                mRecyclerView.setItemViewCacheSize(10);


            }

            @Override
            protected PaginatedQueryList<Post> doInBackground(Void... params) {
                result1 = dbMapper.query(Post.class, queryExpression);
                Log.d("gahdghagdhagdhgahgdhagd",String.valueOf(result1.size()));
                return result1;
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
