package com.angleapp;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

import java.util.ConcurrentModificationException;


/**
 * A simple {@link Fragment} subclass.
 */
public class TopFragment extends Fragment {
    CoordinatorLayout coordinatorLayout;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    PaginatedQueryList<Post> result1;
    SwipeRefreshLayout swipeRefreshLayout;


    public TopFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View  v= inflater.inflate(R.layout.fragment_new, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout)v.findViewById(R.id.newSwipeRefreshLayout);
        mRecyclerView = (RecyclerView)v.findViewById(R.id.newRecyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemViewCacheSize(20);

        mAdapter = new PostAdapter(result1,getActivity());

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 && MainActivity.fab.isShown()||dy < 0 && MainActivity.fab.isShown())
                    MainActivity.fab.hide();
            }

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {

                if (newState ==  RecyclerView.SCROLL_STATE_IDLE) {
                    MainActivity.fab.show();
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

        return v;
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
                mAdapter = new PostAdapter(result,getActivity());
                swipeRefreshLayout.setRefreshing(false);
                mRecyclerView.swapAdapter(mAdapter,false);



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

}


