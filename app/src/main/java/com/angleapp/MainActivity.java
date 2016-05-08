package com.angleapp;


import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
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
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.Manifest;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.content.ContentManager;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    static int SUCCESSFULL_UPLOAD=111;
    AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
    ContentManager contentManager;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    static FloatingActionButton fab;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private ActionBarDrawerToggle mDrawerToggle;
    private int[] tabIcons = {
            R.mipmap.ic_trending_up,
            R.mipmap.ic_whatshot,
            R.mipmap.ic_public
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);




        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.mainCoordinator);
        Snackbar.make(coordinatorLayout,"Signed in Successfully",Snackbar.LENGTH_LONG).show();

        //this.getSupportActionBar().setTitle("Browse");
          fab = (FloatingActionButton) findViewById(R.id.fab);
        TextView toolbarText = (TextView) findViewById(R.id.toolbarText);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/Roboto-Thin.ttf");
        toolbarText.setTypeface(tf, Typeface.BOLD);



        if (fab != null) {
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(new Intent(MainActivity.this, UploadActivity.class),SUCCESSFULL_UPLOAD);

                }
            });
        }
        AWSMobileClient
                .defaultMobileClient()
                .getIdentityManager()
                .getUserID(new IdentityManager.IdentityHandler() {
                    @Override
                    public void handleIdentityID(final String identityId) {

                        Application.userId = identityId;
                        Log.d("fuck yes","done");
                    }

                    @Override
                    public void handleError(final Exception exception) {
                        // This should never happen since the Identity ID is retrieved
                        // when the Application starts.

                    }
                });


        viewPager = (ViewPager) findViewById(R.id.viewpager);
        setupViewPager(viewPager);

        tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        setupTabIcons();

        awsMobileClient.createDefaultContentManager(new ContentManager.BuilderResultHandler() {
            @Override
            public void onComplete(ContentManager contentManager) {
                MainActivity.this.contentManager = contentManager;
            }
        });



    }
    private void setupTabIcons() {
        tabLayout.getTabAt(0).setIcon(tabIcons[0]);
        tabLayout.getTabAt(1).setIcon(tabIcons[1]);
        tabLayout.getTabAt(2).setIcon(tabIcons[2]);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        return true;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        contentManager.clearCache();



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
