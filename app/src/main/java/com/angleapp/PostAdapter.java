package com.angleapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.mobile.AWSConfiguration;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.content.ContentItem;
import com.amazonaws.mobile.content.ContentManager;
import com.amazonaws.mobile.content.ContentProgressListener;
import com.amazonaws.mobile.content.UserFileManager;
import com.amazonaws.mobile.user.IdentityManager;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.amazonaws.transform.VoidJsonUnmarshaller;
import com.bumptech.glide.Glide;
import com.facebook.drawee.view.SimpleDraweeView;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by unnikrishnanpatel on 05/05/16.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private PaginatedQueryList<Post> mDataset;
    AWSMobileClient awsMobileClient  = AWSMobileClient.defaultMobileClient();
    DynamoDBMapper dbMapper = awsMobileClient.getDynamoDBMapper();
    Context context;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView username;
        public TextView createdAt;
        public ImageView userImage;
        public SimpleDraweeView userPost;
        public TextView votePost;
        public ImageView cardUpVote;
        public TextView cardKeyword;
        public  ImageView cardShare;
        public ImageView deletePost;
        public CardView cardView;
        public ImageView cardComment;
        public ViewHolder(View v) {
            super(v);
            title = (TextView)v.findViewById(R.id.cardpostTitle);
            userImage = (ImageView)v.findViewById(R.id.cardPostUserImage);
            username = (TextView)v.findViewById(R.id.cardPostUserName);
            createdAt = (TextView)v.findViewById(R.id.cardPostCreationTime);
            userPost = (SimpleDraweeView) v.findViewById(R.id.cardPostImage);
            votePost = (TextView)v.findViewById(R.id.cardPostVotes);
            cardUpVote = (ImageView)v.findViewById(R.id.cardUpVote);
            cardKeyword = (TextView)v.findViewById(R.id.cardKeyword);
            cardShare = (ImageView)v.findViewById(R.id.shareCardPost);
            deletePost = (ImageView)v.findViewById(R.id.deletePost);
            cardView = (CardView)v.findViewById(R.id.card_view);
            cardComment = (ImageView)v.findViewById(R.id.cardComment);


        }
    }

    public PostAdapter(PaginatedQueryList<Post> myDataset,Context c) {
        mDataset = myDataset;
        context = c;


    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.post_layout, parent, false);
        // set the view's size, margins, paddings and layout parameters
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {

        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        final String preUrl = "https://s3.amazonaws.com/angleapp-userfiles-mobilehub-1491286053/public/";
        if(mDataset.get(position).getUserId().equals(Application.userId)){
            holder.deletePost.setVisibility(View.VISIBLE);
            holder.deletePost.setOnClickListener(new View.OnClickListener() {
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
        holder.cardShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT,mDataset.get(position).getTitle()+" - "+preUrl+mDataset.get(position).getContent());
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);
            }
        });


        holder.cardComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,CommentActivity.class);
                i.putExtra("DATA",mDataset.get(position));
                context.startActivity(i);
            }
        });
        if(mDataset.get(position).getVotes().contains(Application.userId)){
            holder.cardUpVote.setImageDrawable(PostAdapter.this.context.getResources().getDrawable(R.mipmap.heart));
        }else{
            holder.cardUpVote.setImageDrawable(PostAdapter.this.context.getResources().getDrawable(R.mipmap.heart_outline));
        }
        holder.title.setText(mDataset.get(position).getTitle());
        holder.username.setText(mDataset.get(position).getAuthor());
        if(mDataset.get(position).getKeyword()!=null){
            holder.cardKeyword.setText(mDataset.get(position).getKeyword());
        }

        String friendlyTime = (String) DateUtils.getRelativeDateTimeString(context,
                (long) mDataset.get(position).getCreationDate(),
                DateUtils.MINUTE_IN_MILLIS,DateUtils.WEEK_IN_MILLIS,0);
        holder.createdAt.setText(String.valueOf(friendlyTime));
        holder.votePost.setText(String.valueOf(mDataset.get(position).getVotes()==null?0:mDataset.get(position).getVotes().size()-1));
        if(mDataset.get(position).getUserImage()==null){
            Glide.with(context).load(R.drawable.placeholder).into(holder.userImage);
        }else
        {
            Glide.with(context).load(mDataset.get(position).getUserImage()).into(holder.userImage);
        }
        holder.userImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context,ProfileActivity.class);
                i.putExtra("userid",mDataset.get(position).getUserId());
                context.startActivity(i);
            }
        });
        awsMobileClient.createUserFileManager(AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET, "public/", new UserFileManager.BuilderResultHandler() {
            @Override
            public void onComplete(UserFileManager userFileManager) {
                userFileManager.getContent(mDataset.get(position).getContent(), new ContentProgressListener() {
                    @Override
                    public void onSuccess(ContentItem contentItem) {
                        holder.userPost.setScaleType(ImageView.ScaleType.FIT_XY);
                        Glide.with(context).load(contentItem.getFile()).into(holder.userPost);
                    }

                    @Override
                    public void onProgressUpdate(String filePath, boolean isWaiting, long bytesCurrent, long bytesTotal) {

                    }

                    @Override
                    public void onError(String filePath, Exception ex) {

                    }
                });

            }
        });

        holder.cardUpVote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Set votes = mDataset.get(position).getVotes()==null?new HashSet():mDataset.get(position).getVotes();
                if(!votes.contains(Application.userId)){
                    votes.add(Application.userId);
                    holder.cardUpVote.setImageDrawable(PostAdapter.this.context.getResources().getDrawable(R.mipmap.heart));
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
                    holder.cardUpVote.setImageDrawable(PostAdapter.this.context.getResources().getDrawable(R.mipmap.heart_outline));
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

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount()
    {   if(mDataset==null){
        return 0;
         }
        return mDataset.size();
    }


}