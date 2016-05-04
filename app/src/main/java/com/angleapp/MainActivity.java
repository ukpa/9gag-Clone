package com.angleapp;


import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.Manifest;

public class MainActivity extends AppCompatActivity {
    CoordinatorLayout coordinatorLayout;
    static int SUCCESSFULL_UPLOAD=111;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        updatePermission();
        coordinatorLayout = (CoordinatorLayout)findViewById(R.id.mainCoordinator);
        Snackbar.make(coordinatorLayout,"Signed in Successfully",Snackbar.LENGTH_LONG).show();



    }

    private void updatePermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE)) {
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        10);
            }
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.upload:
                startActivityForResult(new Intent(this, UploadActivity.class),SUCCESSFULL_UPLOAD);

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == SUCCESSFULL_UPLOAD) {
            if (resultCode == RESULT_OK) {
                Snackbar.make(coordinatorLayout,"Uploaded Successfully",Snackbar.LENGTH_LONG).show();
            }
        }
    }
}
