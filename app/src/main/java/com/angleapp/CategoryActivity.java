package com.angleapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

public class CategoryActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    PaginatedQueryList<Post> result1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);
        Intent i = getIntent();
        mRecyclerView = (RecyclerView)findViewById(R.id.newRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemViewCacheSize(20);
        mAdapter = new PostAdapter(result1,this);
        refreshFeed(i.getStringExtra("keyword"));

    }
    private void refreshFeed(String query) {
        Post postToFind = new Post();
        postToFind.setKeyword(query);
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
                if(result.size()==0){
                    mRecyclerView.setVisibility(View.GONE);
                    TextView textView = (TextView)findViewById(R.id.emptyView);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("No such post found. Sorry!");

                }
                else{
                    mAdapter = new PostAdapter(result,CategoryActivity.this);
                    mRecyclerView.swapAdapter(mAdapter,false);

                }





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

    @Override
    protected void onStop() {
        super.onStop();
        finish();
    }
}
