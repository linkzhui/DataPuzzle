package com.example.raymon.datapuzzle;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class UserActivity extends AppCompatActivity {

    private Button mFileUploadButton;
    private Button mFileDownloadButton;
    private Button mFileTransferButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        mFileUploadButton = findViewById(R.id.button_file_upload);
        mFileDownloadButton = findViewById(R.id.button_file_download);
        mFileTransferButton = findViewById(R.id.button_file_transfer);

        mFileUploadButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent myIntent = new Intent(UserActivity.this, GoogleDriveFileUploadActivity.class);
                startActivity(myIntent);
            }
        });

        mFileDownloadButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {

                Toast.makeText(getBaseContext(), "you click file download button", Toast.LENGTH_SHORT).show();
//                Intent myIntent = new Intent(UserActivity.this, GoogleDriveFileUpload.class);
//                startActivity(myIntent);
            }
        });


        mFileTransferButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Toast.makeText(getBaseContext(), "you click file transfer button", Toast.LENGTH_SHORT).show();
//                Intent myIntent = new Intent(UserActivity.this, GoogleDriveFileUpload.class);
//                startActivity(myIntent);
            }
        });

    }



    //create options menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.setting, menu);
        return true;
    }

    //response to the menu item select
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case android.R.id.home:
                finish();
                break;
            default:
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                startActivity(intent);
                Toast.makeText(this, "Logout successful", Toast.LENGTH_SHORT).show();
        }
        return true;
    }
}
