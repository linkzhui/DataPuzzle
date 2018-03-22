package com.example.raymon.datapuzzle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

public class ExternalStorage extends AppCompatActivity {

    private static final String TAG = ExternalStorage.class.getSimpleName();
    protected static Bitmap uploadFile = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("The life cycle","External permission test");
        /* Checks if external storage is available for read and write */
        if(isExternalStorageReadable() && isExternalStorageWritable())
        {
            performFileSearch();
        }
    }


    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //Toast.makeText(getBaseContext(),"external permission is readable",Toast.LENGTH_SHORT).show();
            return true;
        }
        //Toast.makeText(getBaseContext(),"external permission is not readable!!!!!",Toast.LENGTH_SHORT).show();
        return false;
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {

            //Toast.makeText(getBaseContext(),"external permission is writable",Toast.LENGTH_SHORT).show();
            return true;
        }

        //Toast.makeText(getBaseContext(),"external permission is not writable!!!!",Toast.LENGTH_SHORT).show();
        return false;
    }


    //file search
    private static final int READ_REQUEST_CODE = 42;
    /**
     * Fires an intent to spin up the "file chooser" UI and select an image.
     */
    public void performFileSearch() {

        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file
        // browser.
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a
        // file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".

        intent.setType("*/*");
        startActivityForResult(intent, READ_REQUEST_CODE);

    }


    //get the bit map information from the Uri
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        uploadFile = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        this.finish();
        return uploadFile;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.

        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().
            Uri uri;
            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());
                try {
                    getBitmapFromUri(uri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
