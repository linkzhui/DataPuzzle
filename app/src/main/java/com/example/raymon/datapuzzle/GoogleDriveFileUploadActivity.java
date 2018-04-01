package com.example.raymon.datapuzzle;




import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import java.io.BufferedOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;

/**
 * An activity that creates a text file in the App Folder.
 */
public class GoogleDriveFileUploadActivity extends BaseActivity {
    private static final String TAG = "CreateFileInAppFolder";

    @Override
    protected void onDriveClientReady() {

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        Bundle args  = data.getBundleExtra("fragment_list");

        //The file's fragments are stored into list of outputStream
        ArrayList<BufferedOutputStream> fragment_list = (ArrayList<BufferedOutputStream>) args.getSerializable("ARRAYLIST");

        //The fragment title is stored into list of String
        ArrayList<String> fragment_title = data.getStringArrayListExtra("fileList");
        switch (requestCode)
        {
            case 1:
                UploadFilesTask task = new UploadFilesTask();
                for(int i = 0;i<fragment_list.size();i++)
                {
                    FileInfo fileinfo = new FileInfo(fragment_list.get(i),fragment_title.get(i));
                    task.execute(fileinfo);
                }
        }
    }


    private class UploadFilesTask extends AsyncTask<FileInfo, Void, Void> {
        protected Void doInBackground(FileInfo... file_infos) {
            for(int i = 0;i<file_infos.length;i++)
            {
                createFileInAppFolder(file_infos[i]);
            }
            return null;
        }


        protected void onPostExecute(Void Void) {
            Toast.makeText(getBaseContext(), "File Upload Successful", Toast.LENGTH_SHORT).show();
        }
    }


    // [START create_file_in_appfolder]
    private void createFileInAppFolder(final FileInfo file_info) {
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getRootFolder();
        final Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
        Tasks.whenAll(appFolderTask, createContentsTask)
                .continueWithTask(new Continuation<Void, Task<DriveFile>>() {
                    @Override
                    public Task<DriveFile> then(@NonNull Task<Void> task) throws Exception {
                        DriveFolder parent = appFolderTask.getResult();
                        DriveContents contents = createContentsTask.getResult();
                        OutputStream outputStream = contents.getOutputStream();
                        try (Writer writer = new OutputStreamWriter(outputStream)) {
                            writer.write(file_info.bufferedOutputStream.toString());
                        }

                        MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                                .setTitle(file_info.file_title)
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
