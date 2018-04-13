package com.example.raymon.datapuzzle;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.events.OpenFileCallback;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GoogleDriveFileDownloadActivity extends BaseActivity {


    /**
     * Progress bar to show the current download progress of the file.
     */
    private ProgressBar mProgressBar;

    /**
     * Text view for file contents
     */
    private TextView mFileContents;
    private String TAG ="GoogleDriveFileDownloadActivity";
    private ExecutorService mExecutorService;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive_file_download);
        mProgressBar = findViewById(R.id.progressBar);
        mProgressBar.setMax(100);
        mFileContents = findViewById(R.id.fileContents);
        mFileContents.setText("");
        mExecutorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onDriveClientReady() {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mExecutorService.shutdown();
    }

    private void retrieveContents(DriveFile file) {
        // [START read_with_progress_listener]
        OpenFileCallback openCallback = new OpenFileCallback() {
            @Override
            public void onProgress(long bytesDownloaded, long bytesExpected) {
                // Update progress dialog with the latest progress.
                int progress = (int) (bytesDownloaded * 100 / bytesExpected);
                Log.d(TAG, String.format("Loading progress: %d percent", progress));
                mProgressBar.setProgress(progress);
            }

            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onContents(@NonNull DriveContents driveContents) {
                // onProgress may not be called for files that are already
                // available on the device. Mark the progress as complete
                // when contents available to ensure status is updated.
                mProgressBar.setProgress(100);
                // Read contents
                // [START_EXCLUDE]
                try {
                    try (BufferedReader reader = new BufferedReader(
                            new InputStreamReader(driveContents.getInputStream()))) {
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = reader.readLine()) != null) {
                            builder.append(line);
                        }
                        showMessage(getString(R.string.content_loaded));
                        mFileContents.setText(builder.toString());
                        getDriveResourceClient().discardContents(driveContents);
                    }
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
