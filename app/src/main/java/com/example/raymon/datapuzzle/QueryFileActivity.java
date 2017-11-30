package com.example.raymon.datapuzzle;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class QueryFileActivity extends BaseActivity {

    private static final String TAG = "query file";
    StringBuffer sb = new StringBuffer();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_file);
    }

    @Override
    protected void onDriveClientReady() {
        listFiles();
    }

    private void listFiles() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, "Android Photo.png"))
                .build();
        // [START query_files]
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        // [END query_files]
        // [START query_results]
        queryTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                // Handle results...
                                // [START_EXCLUDE]

                                Log.e("Query ","Successful");
                                for(Metadata element:metadataBuffer)
                                {
                                    Log.e("The file is","Founded");
                                    Log.e(element.getTitle(),"  "+element.getFileSize());
                                    metadataBuffer.release();
                                }

                                // [END_EXCLUDE]
                            }

                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Handle failure...
                        // [START_EXCLUDE]
                        Log.e(TAG, "Error retrieving files", e);
                        showMessage(getString(R.string.query_failed));
                        finish();
                        // [END_EXCLUDE]
                    }
                });
        // [END query_results]
    }
}
