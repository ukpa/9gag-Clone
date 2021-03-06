package com.angleapp;

import android.animation.LayoutTransition;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.content.ContentManager;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.InitializationException;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.MobileAnalyticsManager;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    CoordinatorLayout coordinatorLayout;
    static int SUCCESSFULL_UPLOAD=111;
    AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
    ContentManager contentManager;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    static FloatingActionButton fab;
    private int[] tabIcons = {
            R.mipmap.ic_trending_up,
            R.mipmap.ic_public
    };
    private static MobileAnalyticsManager analytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_activity_temp);
        try {
            analytics = MobileAnalyticsManager.getOrCreateInstance(
                    this.getApplicationContext(),
                    "appId",
                    "identityPoolId"
            );
        } catch(InitializationException ex) {
            Log.e(this.getClass().getName(), "Failed to initialize Amazon Mobile Analytics", ex);
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.mainCoordinator);
        Snackbar.make(coordinatorLayout,"Signed in successfully",Snackbar.LENGTH_LONG).show();
        fab = (FloatingActionButton) findViewById(R.id.fab);
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View headerLayout = navigationView.inflateHeaderView(R.layout.nav_header_main_activity_temp);
        ImageView profile = (ImageView)headerLayout.findViewById(R.id.imageProfile);
        TextView userName = (TextView)headerLayout.findViewById(R.id.profileUserName);
        userName.setText(AWSMobileClient.defaultMobileClient().getIdentityManager().getUserName());
        Glide.with(this).load(SignInActivity.userImageUrl==null?SplashActivity.userImageUrl:SignInActivity.userImageUrl).into(profile);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setItemIconTintList(null);

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
                    }

                    @Override
                    public void handleError(final Exception exception) {
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
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new TopFragment(), "TOP");
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
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.search));
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setLayoutTransition(new LayoutTransition());

        return true;
    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(analytics != null) {
            analytics.getSessionClient().pauseSession();
            //Attempt to send any events that have been recorded to the Mobile Analytics service.
            analytics.getEventClient().submitEvents();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(analytics != null)  {
            analytics.getSessionClient().resumeSession();
        }
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_funny) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_wtf) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_geeky) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_meme) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_cute) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_comic) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_cosplay) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_food) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_girl) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_timely) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_design) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_nsfw) {
            openKeyword(item.getTitle().toString());
        }
        else if (id == R.id.nav_signout) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setIcon(R.mipmap.ic_power_settings_new_black_48dp).setTitle("Please don't leave! :(");
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                }
            });
            builder.setNegativeButton("No, Screw You!", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    // User cancelled the dialog
                    AWSMobileClient.defaultMobileClient().getIdentityManager().signOut();
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();

        }
        else if (id == R.id.nav_share) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_app));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void openKeyword(String keyword){
        Intent i = new Intent(this,CategoryActivity.class);
        i.putExtra("keyword",keyword);
        startActivity(i);
    }
}
