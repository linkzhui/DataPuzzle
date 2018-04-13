package com.example.raymon.datapuzzle;




import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import org.apache.commons.io.IOUtils;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.Writer;
import java.util.ArrayList;

/**
 * An activity that creates a text file in the App Folder.
 */
public class GoogleDriveFileUploadActivity extends BaseActivity {

    private static final String TAG = "CreateFileInAppFolder";
    @Override
    protected void onDriveClientReady() {
        Log.i(TAG,"on Drive Client Ready");
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        FileUploadInfo fileUploadInfo = (FileUploadInfo) bundle.getSerializable("fragment_info");
        Log.i(TAG,"begin upload");
        UploadFilesTask task = new UploadFilesTask();
        task.execute(fileUploadInfo);


    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        Log.i(TAG,"receive the intent");
        Intent intent = this.getIntent();
        Bundle bundle = intent.getExtras();
        FileUploadInfo[] fileUploadInfo = (FileUploadInfo[]) bundle.getSerializable("fragment_info");

        switch (requestCode)
        {
            case 1:
                Log.i(TAG,"begin upload");
                UploadFilesTask task = new UploadFilesTask();
                for(int i = 0;i<fileUploadInfo.length;i++)
                {
                    task.execute(fileUploadInfo[i]);
                }
                break;
        }
    }


    private class UploadFilesTask extends AsyncTask<FileUploadInfo, Void, Void> {
        protected Void doInBackground(FileUploadInfo... file_infos) {
            for(int i = 0;i<file_infos.length;i++)
            {
                for(int j = 0;j<file_infos[i].fragment.length;j++)
                {
                    createFileInAppFolder(file_infos[i].fragment[j],file_infos[i].fragName[j]);
                }
            }
            return null;
        }


        protected void onPostExecute(Void Void) {
            Toast.makeText(getBaseContext(), "File Upload Successful", Toast.LENGTH_SHORT).show();
        }
    }


    // [START create_file_in_appfolder]
    private void createFileInAppFolder(final File fragment, final String filename) {
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        InputStream inputStream = new FileInputStream(fragment);
                        IOUtils.copy(inputStream,outputStream);

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(filename)
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

    public static class FileUploadInfo implements Serializable {
        File[] fragment;
        String[] fragName;
        public FileUploadInfo(File[] fragment, String[] fragName)
        {
            this.fragment = fragment;
            this.fragName = fragName;
        }
    }
}
