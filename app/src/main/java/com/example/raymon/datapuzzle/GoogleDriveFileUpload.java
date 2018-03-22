package com.example.raymon.datapuzzle;

import java.io.ByteArrayOutputStream;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.OutputStream;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;

public class GoogleDriveFileUpload extends Activity implements ConnectionCallbacks,
        OnConnectionFailedListener {

    private static final String TAG = "drive-quickstart";
    private static final int REQUEST_CODE_File_UPLOAD = 1;
    private static final int REQUEST_CODE_CREATOR = 2;
    private static final int REQUEST_CODE_RESOLUTION = 3;

    protected static GoogleApiClient mGoogleApiClient;
    private Bitmap mBitmapToSave = ExternalStorage.uploadFile;


    /**
     * Create a new file and save it to Drive.
     */

    @Override
    protected void onCreate(Bundle savedInstanceState){
        Log.e("life cycle test","we are on google drive connection cycle");
        super.onCreate(savedInstanceState);
        if (mGoogleApiClient == null) {
            //authorization for the Google Drive Android API is handled by the GoogleApiClient

            // Create the API client and bind it to an instance variable.
            // We use this instance as the callback for connection and connection
            // failures.
            // Since no account name is passed, the user is prompted to choose.

            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Drive.API)
                    .addScope(Drive.SCOPE_FILE)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }
    }
    private void saveFileToDrive() {
        // Start by creating a new contents, and setting a callback.
        Log.e(TAG, "Save file to Drive.");
        final Bitmap image = mBitmapToSave;
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {

                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        // If the operation was not successful, we cannot do anything
                        // and must
                        // fail.
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new contents.");
                            return;
                        }
                        // Otherwise, we can write our data to the new contents.
                        Log.i(TAG, "New contents created.");
                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();
                        // Write the bitmap data from it.
                        ByteArrayOutputStream bitmapStream = new ByteArrayOutputStream();
                        image.compress(Bitmap.CompressFormat.PNG, 100, bitmapStream);
                        try {
                            outputStream.write(bitmapStream.toByteArray());
                        } catch (IOException e1) {
                            Log.i(TAG, "Unable to write file contents.");
                        }
                        // Create the initial metadata - MIME type and title.
                        // Note that the user will be able to change the title later.
                        MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                .setMimeType("image/jpeg").setTitle("Android Photo.png").build();

                        // Create an intent for the file chooser, and start it.
                        IntentSender intentSender = Drive.DriveApi
                                .newCreateFileActivityBuilder()
                                .setInitialMetadata(metadataChangeSet)
                                .setInitialDriveContents(result.getDriveContents())
                                .build(mGoogleApiClient);
                        try {
                            startIntentSenderForResult(
                                    intentSender, REQUEST_CODE_CREATOR, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.i(TAG, "Failed to launch file chooser.");
                        }
                    }
                });
    }

    @Override
    protected void onStart(){
        super.onStart();
        Log.e(TAG,"google drive connection begin");
        mGoogleApiClient.connect();
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Connect the client. Once connected, the camera is launched.

    }

    @Override
    protected void onPause() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onPause();
    }


    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        Log.e(TAG,"onActivityResult");
        switch (requestCode) {
            case REQUEST_CODE_File_UPLOAD:
                // Called after a file has been selected.

                mBitmapToSave = ExternalStorage.uploadFile;

                if (resultCode == Activity.RESULT_OK) {
                    Intent myIntent = new Intent(GoogleDriveFileUpload.this, UserActivity.class);
                    startActivity(myIntent);
                }
                break;
            case REQUEST_CODE_CREATOR:
                // Called after a file is saved to Drive.
                if (resultCode == RESULT_OK) {
                    Log.i(TAG, "Image successfully saved.");
                    mBitmapToSave = null;
                    // Just start the camera again for another photo.
                    startActivityForResult(new Intent(this, ExternalStorage.class),REQUEST_CODE_File_UPLOAD
                            );
                }
                break;
        }
    }



    @Override
    public void onConnectionFailed(ConnectionResult result) {
        // Called whenever the API client fails to connect.
        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
        if (!result.hasResolution()) {
            // show the localized error dialog.
            GoogleApiAvailability.getInstance().getErrorDialog(this, result.getErrorCode(), 0).show();
            return;
        }
        // The failure has a resolution. Resolve it.
        // Called typically when the app is not yet authorized, and an
        // authorization
        // dialog is displayed to the user.
        try {
            result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
        }
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.i(TAG, "API client connected.");
        if(mBitmapToSave == null)
        {
            startActivityForResult(new Intent(this, ExternalStorage.class),REQUEST_CODE_File_UPLOAD);
            return;
        }
        saveFileToDrive();

    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i(TAG, "GoogleApiClient connection suspended");
    }


}




