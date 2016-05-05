package com.angleapp;

import android.content.Context;
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
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedQueryList;
import com.bumptech.glide.Glide;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by unnikrishnanpatel on 05/05/16.
 */
public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {
    private PaginatedQueryList<Post> mDataset;
    AWSMobileClient awsMobileClient  = AWSMobileClient.defaultMobileClient();
    UserFileManager mUserFileManager;
    Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView title;
        public TextView username;
        public TextView createdAt;
        public ImageView userImage;
        public ImageView userPost;
        public TextView votePost;
        public ViewHolder(View v) {
            super(v);
            title = (TextView)v.findViewById(R.id.cardpostTitle);
            userImage = (ImageView)v.findViewById(R.id.cardPostUserImage);
            username = (TextView)v.findViewById(R.id.cardPostUserName);
            createdAt = (TextView)v.findViewById(R.id.cardPostCreationTime);
            userPost = (ImageView)v.findViewById(R.id.cardPostImage);
            votePost = (TextView)v.findViewById(R.id.cardPostVotes);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
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
        holder.title.setText(mDataset.get(position).getTitle());
        holder.username.setText(mDataset.get(position).getAuthor());
        String friendlyTime = (String) DateUtils.getRelativeDateTimeString(context,
                (long) mDataset.get(position).getCreationDate(),
                DateUtils.MINUTE_IN_MILLIS,DateUtils.WEEK_IN_MILLIS,0);
        holder.createdAt.setText(String.valueOf(friendlyTime));
        Log.d("ashahsjahsjha",mDataset.get(position).getContent());
        holder.votePost.setText(String.valueOf(mDataset.get(position).getVotes()));
        holder.userImage.setImageBitmap(AWSMobileClient.defaultMobileClient().getIdentityManager().getUserImage());
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