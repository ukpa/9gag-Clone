package com.angleapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.amazonaws.mobile.AWSConfiguration;
import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.content.ContentItem;
import com.amazonaws.mobile.content.ContentManager;
import com.amazonaws.mobile.content.ContentProgressListener;
import com.amazonaws.mobile.content.UserFileManager;
import com.amazonaws.mobile.util.ImageSelectorUtils;
import com.amazonaws.mobileconnectors.amazonmobileanalytics.internal.core.system.System;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapperConfig;

import java.io.File;
import java.io.IOException;
import java.security.Timestamp;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    static final int PICK_PICTURE_REQUEST = 1111;
    String data_path;
    ImageView imageView;
    UserFileManager userFileManager;
    AWSMobileClient awsMobileClient= AWSMobileClient.defaultMobileClient();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        final EditText title = (EditText)findViewById(R.id.postTitle);
        final EditText category = (EditText)findViewById(R.id.postCategory);
        imageView= (ImageView)findViewById(R.id.postImage);
        Button imageOrGif = (Button)findViewById(R.id.uploadData);
        Button uploadButton  = (Button)findViewById(R.id.postUpload);
        createUserFileManager();
        if (imageOrGif != null) {
            imageOrGif.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivityForResult(ImageSelectorUtils.getImageSelectionIntent(),PICK_PICTURE_REQUEST);
                }
            });
        }

        if (uploadButton != null) {
            uploadButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    final Post post = new Post();
                    post.setAuthor("Unni");
                    post.setUserId(Application.userId);
                    post.setCategory(category.getText().toString());
                    final String extension = data_path.substring(data_path.lastIndexOf("."));
                    final String content  = "fun_stuff/"+String.valueOf(UUID.randomUUID())+extension;
                    post.setContent(content);
                    post.setCreationDate(new Date().getTime());
                    post.setTitle(title.getText().toString());
                    Set<String> set = new HashSet<>();
                    set.add("a");
                    set.add("b");
                    post.setKeywords(set);
                    final DynamoDBMapper dbMapper = awsMobileClient.getDynamoDBMapper();
                    AsyncTask<Void,Void,Void> asyncTask = new AsyncTask<Void, Void, Void>() {
                        @Override
                        protected Void doInBackground(Void... params) {
                            dbMapper.save(post);
                            File file = new File(data_path);
                            Log.d("FILE",String.valueOf(file));
                            userFileManager.uploadContent(file, content, new ContentProgressListener() {
                                @Override
                                public void onSuccess(ContentItem contentItem) {
                                    Log.d("uploaded fucker","done");
                                }

                                @Override
                                public void onProgressUpdate(String filePath, boolean isWaiting, long bytesCurrent, long bytesTotal) {

                                }

                                @Override
                                public void onError(String filePath, Exception ex) {

                                }
                            });
                            Intent intent=new Intent();
                            setResult(RESULT_OK,intent);
                            finish();

                            return null;
                        }
                    };

                    asyncTask.execute();

                }
            });
        }

    }

    private void createUserFileManager() {

        awsMobileClient.createUserFileManager(AWSConfiguration.AMAZON_S3_USER_FILES_BUCKET, "public/", new UserFileManager.BuilderResultHandler() {
            @Override
            public void onComplete(UserFileManager userFileManager) {
                UploadActivity.this.userFileManager = userFileManager;
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PICK_PICTURE_REQUEST) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                Uri pictureuri = data.getData();
                try{
                    Bitmap bitmap  = MediaStore.Images.Media.getBitmap(this.getContentResolver(),pictureuri);
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageBitmap(bitmap);
                }catch (IOException e){}

                data_path = ImageSelectorUtils.getFilePathFromUri(this,pictureuri);
            }
        }

    }


}
