package com.example.raymon.datapuzzle;




import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

/**
 * An activity that creates a text file in the App Folder.
 */
public class Main2Activity extends BaseActivity {
    private static final String TAG = "CreateFileInAppFolder";

    @Override
    protected void onDriveClientReady() {
        createFileInAppFolder();
    }

    // [START create_file_in_appfolder]
    private void createFileInAppFolder() {
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write("Hello World!");
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("New file_test")
                                .setMimeType("text/plain")
                                .setStarred(true)
                                .build();

                        return getDriveResourceClient().createFile(parent, changeSet, contents);
                    }
                })
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFile>() {
                            @Override
                            public void onSuccess(DriveFile driveFile) {
                                showMessage(getString(R.string.file_created,
                                        driveFile.getDriveId().encodeToString()));
                                finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                        showMessage(getString(R.string.file_create_error));
                        finish();
                    }
                });
    }
    // [END create_file_in_appfolder]
}
