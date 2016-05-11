package com.angleapp;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class CommentActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CommentAdapter mAdapter;
    PaginatedQueryList<Comment> result1;
    private RecyclerView.LayoutManager mLayoutManager;
    AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
    final DynamoDBMapper dbMapper = awsMobileClient.getDynamoDBMapper();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        Intent i = getIntent();
        final Post post = (Post) i.getSerializableExtra("DATA");
        Log.d("ahsahsjahsja",String.valueOf(post.getContent()));
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        recyclerView = (RecyclerView)findViewById(R.id.commentRecyclerView);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemViewCacheSize(20);
        final EditText addComment = (EditText)findViewById(R.id.addComment);

        mAdapter = new CommentAdapter(result1,this);
        initRefreshFeed(post.getPostId());

        ImageView sendButton = (ImageView)findViewById(R.id.sendComment);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Comment comment = new Comment();
                comment.setPostId(post.getPostId());
                comment.setUserid(Application.userId);
                comment.setUsername(AWSMobileClient.defaultMobileClient().getIdentityManager().getUserName());
                comment.setComment(addComment.getText().toString());
                comment.setCreationDate(new Date().getTime());
                Set<String> set = new HashSet<String>();
                set.add("dummy_vote");
                comment.setVotes(set);
                AsyncTask<Void,Void,Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                    @Override
                    protected Void doInBackground(Void... params) {
                        dbMapper.save(comment);

                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        TextView textView = (TextView)findViewById(R.id.emptyView);
                        textView.setVisibility(View.GONE);
                        recyclerView.setVisibility(View.VISIBLE);

                        initRefreshFeed(post.getPostId());
                        addComment.setText("");
                        super.onPostExecute(aVoid);
                    }
                };
                asyncTask.execute();




            }
        });

    }

    private void initRefreshFeed(final String query) {
        AWSMobileClient
                .defaultMobileClient()
                .getIdentityManager()
                .getUserID(new IdentityManager.IdentityHandler() {
                    @Override
                    public void handleIdentityID(final String identityId) {
                        Comment commentToFind = new Comment();
                        commentToFind.setPostId(query);

                        final DynamoDBQueryExpression<Comment> queryExpression = new DynamoDBQueryExpression<Comment>()
                                .withHashKeyValues(commentToFind).withConsistentRead(false);
                        queryExpression.setScanIndexForward(false);
                        AsyncTask<Void,Void,PaginatedQueryList<Comment>> asyncTask = new AsyncTask<Void, Void, PaginatedQueryList<Comment>>() {
                            @Override
                            protected void onPostExecute(PaginatedQueryList<Comment> result) {
                                super.onPostExecute(result);
                                mAdapter = new CommentAdapter(result,CommentActivity.this);
                                recyclerView.swapAdapter(mAdapter,false);
                                if(result.size()==0){
                                    recyclerView.setVisibility(View.GONE);
                                    TextView textView = (TextView)findViewById(R.id.emptyView);
                                    textView.setVisibility(View.VISIBLE);
                                    textView.setText("No such post found. Sorry!");

                                }
                                else{
                                    mAdapter = new CommentAdapter(result,CommentActivity.this);
                                    recyclerView.swapAdapter(mAdapter,false);

                                }



                            }

                            @Override
                            protected PaginatedQueryList<Comment> doInBackground(Void... params) {
                                result1 = dbMapper.query(Comment.class, queryExpression);
                                Log.d("gahdghagdhagdhgahgdhagd",String.valueOf(result1.size()));
                                return result1;
                            }


                        };
                        asyncTask.execute();

                    }

                    @Override
                    public void handleError(final Exception exception) {
                        // This should never happen since the Identity ID is retrieved
                        // when the Application starts.

                    }
                });

    }
}
