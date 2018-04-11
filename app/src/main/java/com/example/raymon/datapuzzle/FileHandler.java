package com.example.raymon.datapuzzle;

import android.content.Context;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Created by Jerry on 3/21/18.
 */

public class FileHandler {


    //BufferedInputStream in will be data for the selected file from external storage
    //string[] result, result[0] is file's name, result[1] is file's size
    //List<String> fileList, will be the list to store the fragment' name


    private Context context = UserModeActivity.getContextOfApplication();
    private DatabaseReference mDatabase;
    private String TAG = "File Handler";
    public GoogleDriveFileUploadActivity.FileUploadInfo[] split(FileHandlerInfo fileHandlerInfo, String mode) throws IOException
    {

        // open the file

        long fileSize = Long.parseLong(fileHandlerInfo.fileSize);
        final String filename = fileHandlerInfo.fileName;

        //Get the Google firebase database instance
        mDatabase = FirebaseDatabase.getInstance().getReference();
        //Get the account username
        final String username = fileHandlerInfo.username;
        long chunkSize = fileSize/2;
        GoogleDriveFileUploadActivity.FileUploadInfo[] fileUploadInfo = new GoogleDriveFileUploadActivity.FileUploadInfo[2];
        //get InputStream from encryptFile
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileHandlerInfo.encryptFile));

        for (int subfile = 0; subfile < 2; subfile++)
        {
            // Create the temp fragment file to store the result of split file
            File temp_file = File.createTempFile(filename,"."+subfile,context.getCacheDir());

            //get
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temp_file));
            fileUploadInfo[subfile].fragment = temp_file;
            fileUploadInfo[subfile].fragName = filename+"."+subfile;

            // write the right amount of bytes
            if(fileSize %2 == 0){
                for (long currentByte = 0; currentByte < chunkSize; currentByte++)
                {
                    // load one byte from the input file and write it to the output file
                    out.write(in.read());
                }
            } else {
                for (long currentByte = 0; currentByte < chunkSize + 1; currentByte++)
                {
                    // load one byte from the input file and write it to the output file
                    out.write(in.read());
                }
                chunkSize -= 1;
            }

            // close the file
            out.close();
        }
        if(in.read() != -1){
            Log.e("File Split", "File split not completed.");
        }
        else{

            Log.i(TAG,"file split completed successful");
            final String framgentName_0 = fileUploadInfo[0].fragName;
            final String fragmentName_1 = fileUploadInfo[1].fragName;
            switch (mode)
            {
                case "cooperate":
                    final String fragmentName_2 = fileUploadInfo[2].fragName;
                    //*************unfinished block, implement XOR right there:*************




                    //add the file fragment name into firebase database
                    mDatabase.child(username).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(filename))
                            {
                                //delete exist file fragment from the file list
                                mDatabase.child(username).child("files").child(filename).setValue(null);
                            }

                            mDatabase.child(username).child("files").child(filename).child("fragments").setValue(0,framgentName_0);
                            mDatabase.child(username).child("files").child(filename).child("fragments").setValue(1,fragmentName_1);
                            mDatabase.child(username).child("files").child(filename).child("fragments").setValue(2,fragmentName_2);
                            mDatabase.child(username).child("files").child(filename).setValue("mode","cooperate");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    break;
                case "individual":
                    //add the file fragment name into firebase database
                    mDatabase.child(username).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(filename))
                            {
                                //delete exist file fragment from the file list
                                mDatabase.child(username).child("files").child(filename).setValue(null);
                            }

                            mDatabase.child(username).child("files").child(filename).child("fragments").setValue(0,framgentName_0);
                            mDatabase.child(username).child("files").child(filename).child("fragments").setValue(1,fragmentName_1);
                            mDatabase.child(username).child("files").child(filename).setValue("mode","individual");
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    break;
            }
        }




//        // loop for the last chunk (which may be smaller than the chunk size)
//        if (fileSize != chunkSize * (subfile - 1))
//        {
//            // open the output file
//            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));
//            Log.i("File Split", "Entering");
//            // write the rest of the file
//            int b;
//            while ((b = in.read()) != -1)
//                out.write(b);
//
//            // close the file
//            out.close();
//        }

        // close the file
        in.close();
        return fileUploadInfo;
    }

    public static void merge(String filename, List<String> fileList) throws IOException{

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
    }


    public static class FileHandlerInfo implements Serializable{
        String fileName;
        String fileSize;
        File encryptFile;
        String username;
        public FileHandlerInfo(String fileName, String fileSize, File encryptFile, String username){
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.encryptFile = encryptFile;
            this.username = username;
        }

    }
}
