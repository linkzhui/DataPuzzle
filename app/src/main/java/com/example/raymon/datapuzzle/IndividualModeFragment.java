package com.example.raymon.datapuzzle;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.provider.OpenableColumns;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;


import java.io.File;


//remaining task in this fragment:
//1. use intent to start the file split, merge and upload activity  (upload mode)
//2. connect the database to display the list of files, that are available for users to download  (download mode)

public class IndividualModeFragment extends Fragment {

    private static final String TAG = "Individual Mode";
    private Button mbuttonUpload;
    private Button mbuttonDownload;
    private EditText secretKeyText;
    private Crypt crypt = new Crypt();
    private FileHandler fileHandle = new FileHandler();
    private File encryptFile;
    final int requestCode = 1;
    private String username;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {

        //let the inflater to inflate the fragment's layout
        View fragmentView = inflater.inflate(R.layout.fragment_individual_mode, container, false);


        secretKeyText = fragmentView.findViewById(R.id.secretKeyText);
        mbuttonDownload = fragmentView.findViewById(R.id.buttonDecMerge);
        mbuttonUpload = fragmentView.findViewById(R.id.buttonUpload);
        username = getArguments().getString("username");
        //set onclick listener on upload button
        mbuttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(secretKeyText.getText().length()==0)
                {
                    Toast.makeText(getContext(), "Please input the password", Toast.LENGTH_SHORT).show();
                }
                else{
                    //start activity
                    Log.e(TAG,"External permission test");
                    /* Checks if external storage is available for read and write */
                    if(isExternalStorageReadable() && isExternalStorageWritable())
                    {
                        performFileSearch();
                    }
                }
            }
        });

        //set onclick listener on download button
        mbuttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(secretKeyText.getText().length()==0)
                {
                    Toast.makeText(getContext(), "Please input the password", Toast.LENGTH_SHORT).show();
                }
                else{
                    //start the activity to return a list of files which are available to be downloaded by user
                    Intent intent = new Intent(getActivity(),IndividualFileDownloadActivity.class);
                    intent.putExtra("username",username);
                    intent.putExtra("secret key",secretKeyText.getText().toString());
                    startActivity(intent);

                }
            }
        });

        return fragmentView;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        // The ACTION_OPEN_DOCUMENT intent was sent with the request code
        // READ_REQUEST_CODE. If the request code seen here doesn't match, it's the
        // response to some other intent, and the code below shouldn't run at all.
        Uri uri;
        if (requestCode == READ_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            // Pull that URI using resultData.getData().

            if (resultData != null) {
                uri = resultData.getData();
                Log.i(TAG, "Uri: " + uri.toString());

                //result[0]: retrieve the fileName from the selected file
                //result[1]: retrieve the fileSize from the selected file
                String[] result = getFileMetaData(uri);
                try {
//                        fragment_list=fileHandle.split(bufferedInputStream,result, fileList);
                    Crypt.CryptNode cryptNode = new Crypt.CryptNode(uri,result[0],secretKeyText.getText().toString());

                    //call AsyncTask.get() for Main thread UI to wait until the async task is finished.
                    new EncryptInBG().execute(cryptNode).get();

                    Log.i(TAG,"begin file split");
                    FileHandler.FileHandlerInfo fileHandlerInfo = new FileHandler.FileHandlerInfo(result[0],encryptFile,username);
                    GoogleDriveFileUploadActivity.FileUploadInfo fileUploadInfo = fileHandle.split(fileHandlerInfo,"Individual");

                    Intent intent = new Intent(getActivity(),GoogleDriveFileUploadActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("fragment_info",fileUploadInfo);
                    intent.putExtras(bundle);
                    startActivityForResult(intent,1);

                } catch (Exception e){
                    Log.e("File", e.getMessage()+"");
                    Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_SHORT).show();
                }
            }
        }
    }




    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public String[] getFileMetaData(Uri uri) {

        // The query, since it only applies to a single document, will only return
        // one row. There's no need to filter, sort, or select fields, since we want
        // all fields for one document.
        Cursor cursor = getActivity().getContentResolver()
                .query(uri, null, null, null, null, null);
        String displayName = "";

        String[] result = new String[2];
        //result[0] will be file name
        //result[1] will be file size

        try {
            // moveToFirst() returns false if the cursor has 0 rows.  Very handy for
            // "if there's anything to look at, look at it" conditionals.
            if (cursor != null && cursor.moveToFirst()) {

                // Note it's called "Display Name".  This is
                // provider-specific, and might not necessarily be the file name.
                displayName = cursor.getString(
                        cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                Log.i(TAG, "Display Name: " + displayName);
                result[0] = displayName;
                int sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE);
                // If the size is unknown, the value stored is null.  But since an
                // int can't be null in Java, the behavior is implementation-specific,
                // which is just a fancy term for "unpredictable".  So as
                // a rule, check if it's null before assigning to an int.  This will
                // happen often:  The storage API allows for remote files, whose
                // size might not be locally known.
                String size = null;
                if (!cursor.isNull(sizeIndex)) {
                    // Technically the column stores an int, but cursor.getString()
                    // will do the conversion automatically.
                    size = cursor.getString(sizeIndex);
                    result[1] = size;
                } else {
                    size = "Unknown";
                }
                Log.i(TAG, "Size: " + size);
            }
        } finally {
            cursor.close();
        }
        return result;
    }

    private class EncryptInBG extends AsyncTask<Crypt.CryptNode, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Crypt.CryptNode... cryptNodes) {
            boolean result = false;
            try {
                for(int i = 0;i<cryptNodes.length;i++)
                {
                    Log.i(TAG,"Begin File encrypt");
                    //get the encrypt file from AESFileEncrypt function
                    encryptFile = crypt.AESFileEncrypt(cryptNodes[i]);
                    result = true;
                }
            } catch (Exception e){
                Log.e("File Encrypt", e.getMessage());
                // Caused by: java.lang.RuntimeException: Can't create handler inside thread that has not called Looper.prepare()
                //Toast.makeText(getBaseContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
            finally {
                return result;
            }

        }

        @Override
        protected void onPostExecute(Boolean result) {
            if(result)
            {

                Toast.makeText(getContext(),"Encryption completed.",Toast.LENGTH_LONG).show();
            }
           else{
                Toast.makeText(getContext(),"Encryption failed.",Toast.LENGTH_LONG).show();
            }
        }
    }


    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            //Toast.makeText(getBaseContext(),"external permission is readable",Toast.LENGTH_SHORT).show();
            return true;
        }
        //Toast.makeText(getBaseContext(),"external permission is not readable!!!!!",Toast.LENGTH_SHORT).show();
        return false;
    }

    public static boolean isExternalStorageWritable() {
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



}
