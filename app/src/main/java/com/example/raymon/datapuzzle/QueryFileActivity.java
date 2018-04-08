package com.example.raymon.datapuzzle;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;

import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.drive.widget.DataBufferAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

/**
 * An activity that illustrates how to query files in a folder.
 */
public class QueryFileActivity extends BaseActivity {
    private static final String TAG = "QueryFilesInFolder";

    private DataBufferAdapter<Metadata> mResultsAdapter;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_listfiles);
        ListView mListView = findViewById(R.id.listViewResults);
        mResultsAdapter = new ResultsAdapter(this);
        mListView.setAdapter(mResultsAdapter);
    }


    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */

    @Override
    protected void onDriveClientReady() {
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
        appFolderTask.addOnSuccessListener(this, new OnSuccessListener<DriveFolder>() {
            @Override
            public void onSuccess(DriveFolder driveFolder) {
                DriveFolder parent = appFolderTask.getResult();
                queryFile(parent);
            }
        })
                .addOnFailureListener(this, new OnFailureListener(){

                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error retrieving files", e);
                        showMessage("found root folder fail");
                        finish();
                    }
                });
    }

    private void queryFile(DriveFolder folder){
        Query query = new Query.Builder()
                .build();
        // [START query_children]

        Task<MetadataBuffer> queryTask = getDriveResourceClient().queryChildren(folder,query);
        // END query_children]
        queryTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                Log.e("File","found successful");
                                mResultsAdapter.append(metadataBuffer);
                                mResultsAdapter.notifyDataSetChanged();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Error retrieving files", e);
                        showMessage(getString(R.string.query_failed));
                        finish();
                    }
                });
    }

    /**
     * Clears the result buffer to avoid memory leaks as soon
     * as the activity is no longer visible by the user.
     */
    @Override
    protected void onStop() {
        super.onStop();
        mResultsAdapter.clear();
    }



}