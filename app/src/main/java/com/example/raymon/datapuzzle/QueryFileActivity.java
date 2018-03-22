package com.example.raymon.datapuzzle;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ListView;

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

    @Override
    protected void onDriveClientReady() {
        Query query = new Query.Builder()
                .build();
        // [START query_children]
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query);
        // END query_children]
        queryTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                Log.e("File","found successful");
                                mResultsAdapter.append(metadataBuffer);
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

    /**
     * Retrieves results for the next page. For the first run,
     * it retrieves results for the first page.
     */
    private void listFilesInFolder() {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.MIME_TYPE, "text/plain"))
                .build();
        // [START query_children]
        Task<MetadataBuffer> queryTask = getDriveResourceClient().queryChildren(getDriveResourceClient().getRootFolder().getResult(), query);
        // END query_children]
        queryTask
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                mResultsAdapter.append(metadataBuffer);
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
}