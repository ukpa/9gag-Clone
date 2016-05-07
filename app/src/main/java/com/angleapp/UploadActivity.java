package com.angleapp;

import android.*;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import com.anton46.collectionitempicker.CollectionPicker;
import com.anton46.collectionitempicker.Item;
import com.anton46.collectionitempicker.OnItemClickListener;
import com.facebook.drawee.view.SimpleDraweeView;

import java.io.File;
import java.io.IOException;
import java.security.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class UploadActivity extends AppCompatActivity {
    static final int PICK_PICTURE_REQUEST = 1111;
    String data_path;
    SimpleDraweeView imageView;
    UserFileManager userFileManager;
    AWSMobileClient awsMobileClient= AWSMobileClient.defaultMobileClient();
    ProgressBar progressBar;
    EditText title;
    RadioGroup uploadRadioGroup;
    RadioButton uploadradioButton;
    Set<String> keywords = new HashSet<>();
    String content ="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upload);
        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        uploadRadioGroup = (RadioGroup)findViewById(R.id.uploadRadio);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setTitle("Post");
         title = (EditText)findViewById(R.id.postTitle);
        //final EditText category = (EditText)findViewById(R.id.postCategory);
        imageView= (SimpleDraweeView) findViewById(R.id.postImage);
        progressBar = (ProgressBar)findViewById(R.id.uploadProgress);
        createUserFileManager();


        List<Item> items = new ArrayList<>();
        items.add(new Item("item1", "Funny"));
        items.add(new Item("item2", "WTF"));
        items.add(new Item("item3", "Geeky"));
        items.add(new Item("item4", "Meme"));
        items.add(new Item("item5", "Cute"));
        items.add(new Item("item6", "Comic"));
        items.add(new Item("item7", "Cosplay"));
        items.add(new Item("item8", "Food"));
        items.add(new Item("item9", "Girl"));
        items.add(new Item("item10", "Timely"));
        items.add(new Item("item11", "Design"));
        items.add(new Item("item12", "NSFW"));
        CollectionPicker picker = (CollectionPicker) findViewById(R.id.collection_item_picker);
        picker.setItems(items);
        picker.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(Item item, int position) {
                keywords.add(item.text);


            }
        });




    }

    private void postData() {


        final Post post = new Post();
        post.setAuthor(AWSMobileClient.defaultMobileClient().getIdentityManager().getUserName());
        post.setUserId(Application.userId);

        int selectedId = uploadRadioGroup.getCheckedRadioButtonId();
        uploadradioButton = (RadioButton) findViewById(selectedId);
        post.setCategory(uploadradioButton.getText().toString());
        if(data_path==null){
            content="";
        }
        else{
            final String extension = data_path.substring(data_path.lastIndexOf("."));
            content  = "fun_stuff/"+String.valueOf(UUID.randomUUID())+extension;
        }
        post.setContent(content);
        post.setCreationDate(new Date().getTime());
        post.setTitle(title.getText().toString());
        Set<String> votes = new HashSet<String>();
        votes.add("dummy_vote");
        keywords.add("dummy_keyword");
        post.setVotes(votes);
        post.setKeywords(keywords);
        final DynamoDBMapper dbMapper = awsMobileClient.getDynamoDBMapper();
        AsyncTask<Void,Void,Void> asyncTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                dbMapper.save(post);
                if(data_path!=null){
                    File file = new File(data_path);
                    Log.d("FILE",String.valueOf(file));
                    userFileManager.uploadContent(file, content, new ContentProgressListener() {
                        @Override
                        public void onSuccess(ContentItem contentItem) {
                            Log.d("uploaded fucker","done");
                            Intent intent=new Intent();
                            setResult(RESULT_OK,intent);
                            finish();
                        }

                        @Override
                        public void onProgressUpdate(String filePath, boolean isWaiting, long bytesCurrent, long bytesTotal) {
                            progressBar.setMax((int) bytesTotal);
                            progressBar.setProgress((int) bytesCurrent);

                        }

                        @Override
                        public void onError(String filePath, Exception ex) {

                        }
                    });

                }





                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

            }
        };

        asyncTask.execute();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.upload_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.uploadPost:
                if(data_path==null||title.getText().toString().matches("")){
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setIcon(R.mipmap.ic_add_alert_black_24dp).setTitle("Add Title and a GIF/Image.Let's make it viral");
                    builder.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User clicked OK button
                        }
                    });
                    builder.setNegativeButton("No, thanks", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else {
                    progressBar.setVisibility(View.VISIBLE);
                    postData();
                }


                return true;
            case R.id.attach_file:
                updatePermission();
                startActivityForResult(ImageSelectorUtils.getImageSelectionIntent(),PICK_PICTURE_REQUEST);

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    private void updatePermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                        10);
            }
        }
    }


}
