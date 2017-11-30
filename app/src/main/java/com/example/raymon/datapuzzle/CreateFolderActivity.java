package com.example.raymon.datapuzzle;

import android.support.annotation.NonNull;
import android.util.Log;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class CreateFolderActivity extends BaseActivity {
    private static final String TAG = "CreateFolderActivity";

    @Override
    protected void onDriveClientReady() {
        createFolder();
    }

    // [START create_folder]
    private void createFolder() {
        getDriveResourceClient()
                .getRootFolder()
                .continueWithTask(new Continuation<DriveFolder, Task<DriveFolder>>() {
                    @Override
                    public Task<DriveFolder> then(@NonNull Task<DriveFolder> task)
                            throws Exception {
                        DriveFolder parentFolder = task.getResult();
                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle("Hi!!!This is Folder")
                                .setMimeType(DriveFolder.MIME_TYPE)
                                .setStarred(true)
                                .build();
                        
                        //this createFolder is not same with our method, this createFolder method is belong to Google Drive APIz`12]\
                        return getDriveResourceClient().createFolder(parentFolder, changeSet);
                    }
                })
                .addOnSuccessListener(this,
                        new OnSuccessListener<DriveFolder>() {
                            @Override
                            public void onSuccess(DriveFolder driveFolder) {
                                showMessage("File created "+
                                        driveFolder.getDriveId().encodeToString());
                                finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to create file", e);
                        showMessage("File Error");
                        finish();
                    }
                });
    }
    // [END create_folder]
}