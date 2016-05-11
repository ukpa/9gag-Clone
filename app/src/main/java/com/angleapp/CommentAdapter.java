package com.angleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.mobile.AWSConfiguration;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.content.ContentItem;
import com.amazonaws.mobile.content.ContentProgressListener;
import com.amazonaws.mobile.content.UserFileManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by unnikrishnanpatel on 11/05/16.
 */

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private PaginatedQueryList<Comment> mDataset;
    AWSMobileClient awsMobileClient  = AWSMobileClient.defaultMobileClient();
    DynamoDBMapper dbMapper = awsMobileClient.getDynamoDBMapper();
    Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView username;
        public TextView createdAt;
        public TextView votePost;
        public ImageView cardUpVote;
        public CardView cardView;
        public ImageView deleteComment;
        public TextView commentArea;
        public ViewHolder(View v) {
            super(v);
            commentArea = (TextView)v.findViewById(R.id.commentArea);
            username = (TextView)v.findViewById(R.id.commentUsername);
            createdAt = (TextView)v.findViewById(R.id.commentTime);
            votePost = (TextView)v.findViewById(R.id.commentVoteCount);
            cardUpVote = (ImageView)v.findViewById(R.id.commentLike);
            cardView = (CardView)v.findViewById(R.id.card_view);
            deleteComment = (ImageView)v.findViewById(R.id.deleteComment);


        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CommentAdapter(PaginatedQueryList<Comment> myDataset,Context c) {
        mDataset = myDataset;
        context = c;


    }

    // Create new views (invoked by the layout manager)
    @Override
    public CommentAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        if(mDataset.get(position).getUserid().equals(Application.userId)){
            holder.deleteComment.setVisibility(View.VISIBLE);
            holder.deleteComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setIcon(R.mipmap.ic_add_alert_black_24dp).setTitle("Sure about deleting the post?");
                    builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            AsyncTask<Void,Void,Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                                @Override
                                protected Void doInBackground(Void... params) {
                                    dbMapper.delete(mDataset.get(position));
                                    return null;
                                }

                                @Override
                                protected void onPostExecute(Void aVoid) {
                                    holder.cardView.setVisibility(View.GONE);
                                }
                            };
                            asyncTask.execute();
                        }
                    });
                    builder.setNegativeButton("No, Leave it", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                }
            });
        }



        if(mDataset.get(position).getVotes().contains(Application.userId)){
            holder.cardUpVote.setImageDrawable(CommentAdapter.this.context.getResources().getDrawable(R.mipmap.heart));
        }
        holder.commentArea.setText(mDataset.get(position).getComment());
        holder.username.setText(mDataset.get(position).getUsername());

        String friendlyTime = (String) DateUtils.getRelativeDateTimeString(context,
                (long) mDataset.get(position).getCreationDate(),
                DateUtils.MINUTE_IN_MILLIS,DateUtils.WEEK_IN_MILLIS,0);
        holder.createdAt.setText(String.valueOf(friendlyTime));
        holder.votePost.setText(String.valueOf(mDataset.get(position).getVotes()==null?0:mDataset.get(position).getVotes().size()-1));
        holder.username.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,ProfileActivity.class);
                i.putExtra("userid",mDataset.get(position).getUserid());
                context.startActivity(i);
            }
        });

        holder.cardUpVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set votes = mDataset.get(position).getVotes()==null?new HashSet():mDataset.get(position).getVotes();
                if(!votes.contains(Application.userId)){
                    votes.add(Application.userId);
                    holder.cardUpVote.setImageDrawable(CommentAdapter.this.context.getResources().getDrawable(R.mipmap.heart));
                    mDataset.get(position).setVotes(votes);
                    holder.votePost.setText(String.valueOf(votes.size()-1));
                    mDataset.get(position).setVoteCount(votes.size()-1);
                    AsyncTask<Void,Void,Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            dbMapper.save(mDataset.get(position));
                            return null;
                        }
                    };
                    asyncTask.execute();

                }else if(votes.contains(Application.userId)){
                    votes.remove(Application.userId);
                    Log.d("ahsjahsjhajhsjahsj",String.valueOf(votes));
                    holder.cardUpVote.setImageDrawable(CommentAdapter.this.context.getResources().getDrawable(R.mipmap.heart_outline));
                    holder.votePost.setText(String.valueOf(votes.size()-1));
                    mDataset.get(position).setVoteCount(votes.size()-1);
                    mDataset.get(position).setVotes(votes);
                    AsyncTask<Void,Void,Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            dbMapper.save(mDataset.get(position));
                            return null;
                        }
                    };
                    asyncTask.execute();

                }
            }
        });




    }

    @Override
    public int getItemCount()
    {   if(mDataset==null){
        return 0;
    }
        return mDataset.size();
    }


}