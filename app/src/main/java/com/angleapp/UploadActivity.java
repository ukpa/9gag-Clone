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

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.util.ImageSelectorUtils;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class UploadActivity extends AppCompatActivity {
    static final int PICK_PICTURE_REQUEST = 1111;
    String data_path;
    ImageView imageView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);
        final EditText title = (EditText)findViewById(R.id.postTitle);
        final EditText category = (EditText)findViewById(R.id.postCategory);
        imageView= (ImageView)findViewById(R.id.postImage);
        Button imageOrGif = (Button)findViewById(R.id.uploadData);
        Button uploadButton  = (Button)findViewById(R.id.postUpload);
        imageOrGif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(ImageSelectorUtils.getImageSelectionIntent(),PICK_PICTURE_REQUEST);
            }
        });
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();
                final Post post = new Post();
                post.setAuthor("Unni");
                post.setUserId(Application.userId);
                post.setCategory(category.getText().toString());
                post.setContent(data_path);
                post.setCreationDate(1231213);
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
                        Intent intent=new Intent();
                        intent.putExtra("message","Uploaded Successfully");
                        setResult(RESULT_OK,intent);
                        finish();
                        return null;
                    }
                };

                asyncTask.execute();

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
