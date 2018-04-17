package com.example.raymon.datapuzzle;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.events.OpenFileCallback;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import org.apache.commons.io.IOUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class GoogleDriveFileDownloadActivity extends BaseActivity {


    /**
     * Text view for file contents
     */
    private String TAG ="GoogleDriveFileDownloadActivity";
    private ArrayList<String> fragNameList;
    private ArrayList<File> fragList = new ArrayList<>();
    private FileHandler fileHandler = new FileHandler();
    private String fileName;
    private String secretKey;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onDriveClientReady() {
        Intent intent = getIntent();
        fragNameList = intent.getStringArrayListExtra("fragment name");
        fileName = intent.getStringExtra("file name");
        secretKey = intent.getStringExtra("secret key");
        DownloadFilesTask task = new DownloadFilesTask();

        //Use Async Task to implement the File Download from Google Drive
        try {
            task.execute(fragNameList.get(0),fragNameList.get(1)).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        Handler handler = new Handler();

        handler.postDelayed(new Runnable() {
            public void run() {
                // Actions to do after 10 seconds
                Log.i(TAG, "current list size: "+fragList.size());
                try {
                    Crypt.DecryptNode decryptNode = fileHandler.merge(fileName,"Individual", fragList,secretKey);
                    Crypt crypt = new Crypt();
                    crypt.AESFileDecryption(decryptNode);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }, 2000);


//        queryFile("file download.txt");
    }

    private class DownloadFilesTask extends AsyncTask<String, Void, Void> {
        protected Void doInBackground(String... fileNames) {
            for(int i = 0;i<fileNames.length;i++)
            {
                try {
                    queryFile(fileNames[i]);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }


        protected void onPostExecute(Void Void) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void queryFile(final String filename) throws IOException {
//        mFragmentName.setText(filename);

        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, filename))
                .build();
        getDriveResourceClient().query(query)
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                Log.i(TAG,"Query the fragment in Google drive successful");
                                for(Metadata data:metadataBuffer)
                                {
                                    retrieveContents(data.getDriveId().asDriveFile(),data.getOriginalFilename());
                                }
                                finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Query the fragment in Google Drive is failed");
                        finish();
                    }
                });
    }



    private void retrieveContents(final DriveFile file, final String filename) {
        // [START read_with_progress_listener]
        OpenFileCallback openCallback = new OpenFileCallback() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                // Update progress dialog with the latest progress.
//                int progress = (int) (bytesDownloaded * 100 / bytesExpected);
//                Log.d(TAG, String.format("Loading progress: %d percent", progress));
//                mProgressBar.setProgress(progress);
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onContents(@NonNull DriveContents driveContents) {
                // onProgress may not be called for files that are already
                // available on the device. Mark the progress as complete
                // when contents available to ensure status is updated.
//                mProgressBar.setProgress(100);
                // Read contents
                // [START_EXCLUDE]
                File fragment;
                try {
                    fragment = File.createTempFile(filename,null,getCacheDir());
                    OutputStream fileOutputStream = new FileOutputStream(fragment);
                    InputStream inputStream = driveContents.getInputStream();
                    IOUtils.copy(inputStream,fileOutputStream);
                    fragList.add(fragment);
                    Log.i(TAG,"fragment name: "+ fragment.getName());
                    Log.i(TAG,"fragment size: " + fragment.length());
                    showMessage(getString(R.string.content_loaded));
                    getDriveResourceClient().discardContents(driveContents);
                    inputStream.close();
                    fileOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                // [END_EXCLUDE]
            }

            @Override
            public void onError(@NonNull Exception e) {
                // Handle error
                // [START_EXCLUDE]
                Log.e(TAG, "Unable to read contents", e);
                showMessage(getString(R.string.read_failed));
                finish();
                // [END_EXCLUDE]
            }
        };

        getDriveResourceClient().openFile(file, DriveFile.MODE_READ_ONLY, openCallback);
        // [END read_with_progress_listener]
    }
}
