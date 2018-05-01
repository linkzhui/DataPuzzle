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
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import org.apache.commons.io.IOUtils;
import com.google.android.gms.drive.MetadataBuffer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.concurrent.ExecutionException;

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
        DeleteFilesTask deleteFilesTask = new DeleteFilesTask();
        try {
            deleteFilesTask.execute(fileUploadInfo).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        task.execute(fileUploadInfo);

    }


    private class DeleteFilesTask extends AsyncTask<FileUploadInfo,Void,Void>
    {

        @Override
        protected Void doInBackground(FileUploadInfo... fileUploadInfos) {
            for(int j = 0;j<fileUploadInfos[0].fragment.length;j++)
            {
                queryFile(fileUploadInfos[0].fragName[j]);
            }
            return null;
        }
    }
    private void queryFile(String filename)
    {
        Query query = new Query.Builder()
                .addFilter(Filters.eq(SearchableField.TITLE, filename))
                .build();
        Task<MetadataBuffer> queryTask = getDriveResourceClient().query(query)
                .addOnSuccessListener(this,
                        new OnSuccessListener<MetadataBuffer>() {
                            @Override
                            public void onSuccess(MetadataBuffer metadataBuffer) {
                                Log.i(TAG,"duplicate file detected");
                                for(Metadata data:metadataBuffer)
                                {
                                    deleteFile(data.getDriveId().asDriveFile());
                                }
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i(TAG, "No duplicate file detected");
                        finish();
                    }
                });
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

    //Delete the fole in google drive
    private void deleteFile(DriveFile file) {
        // [START delete_file]
        getDriveResourceClient()
                .delete(file)
                .addOnSuccessListener(this,
                        new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showMessage(getString(R.string.file_deleted));
                                finish();
                            }
                        })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Unable to delete file", e);
                        showMessage(getString(R.string.delete_failed));
                        finish();
                    }
                });
        // [END delete_file]
    }

    // [START create_file_in_appfolder]
    private void createFileInAppFolder(final File fragment, final String filename) {
        final Task<DriveFolder> appFolderTask = getDriveResourceClient().getAppFolder();
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

                                //delete the file fragment from internal storage
//                                fragment.delete();

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
