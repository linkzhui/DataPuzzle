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
import java.util.ArrayList;
import java.util.List;

import com.example.raymon.datapuzzle.DBHelper;
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


    public GoogleDriveFileUploadActivity.FileUploadInfo split(FileHandlerInfo fileHandlerInfo, final String mode) throws IOException
    {

        // open the file

        Log.i(TAG,"The encrypted file in file split handler is: "+fileHandlerInfo.encryptFile.length());
        Log.i(TAG,"encrypt file name: "+fileHandlerInfo.encryptFile.getName());
        long fileSize = fileHandlerInfo.encryptFile.length();
        final String filename = fileHandlerInfo.fileName;

        //Get the Google firebase database instance
        mDatabase = FirebaseDatabase.getInstance().getReference();

        //Get the account username
        final String username = fileHandlerInfo.username;

        final String filenameWithoutExt;
        final String fileExtension;

        //parse the file name (without extension) and file type
        if(filename.indexOf('.')!=-1)
        {
            filenameWithoutExt = filename.substring(0,filename.indexOf('.'));
            fileExtension = filename.substring(filename.indexOf('.'));
        }
        else{
            filenameWithoutExt = filename;
            fileExtension = "";
        }
        final GoogleDriveFileUploadActivity.FileUploadInfo fileUploadInfo;
        //get InputStream from encryptFile
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileHandlerInfo.encryptFile));

        long startIndex = 0;
        long endIndex = fileSize/2;

        //if the mode is individual, the number of file fragment is 2
        //if the mode is cooperate, the number of file fragment is 3
        int fragNum = mode.equals("Individual") ? 2:3;
        File[] fragment = new File[fragNum];
        final String[] fragName = new String[fragNum];
        for (int subfileIndex = 0; subfileIndex < 2; subfileIndex++)
        {
            // If the mode is Individual, create the temp fragment file to store the result of split file
            // If the mode is Cooperate, create the file store in internal storage
            fragment[subfileIndex] = mode.equals("Individual")? File.createTempFile(filename,"."+subfileIndex,context.getCacheDir()):new File(context.getFilesDir(),filename+"."+subfileIndex);
            fragName[subfileIndex] = filename+"."+subfileIndex;
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(fragment[subfileIndex]));
            Log.i(TAG,"fragment name: "+fragment[subfileIndex]);
            Log.i(TAG,"start index: " + startIndex);
            Log.i(TAG,"end index " + endIndex);
            for (;startIndex < endIndex; startIndex++)
            {
                // load one byte from the input file and write it to the output file
                out.write(in.read());
            }
            endIndex = fileSize;

            Log.i(TAG,"fragment size: "+fragment[subfileIndex].length());
            // close the file
            out.close();
        }

        fileUploadInfo = new GoogleDriveFileUploadActivity.FileUploadInfo(fragment,fragName);
        int size = 0;



        Log.i(TAG,size+"");
        //check if inputStream read reach the end of original file?
        //in.read() = -1, inputstream read reach the end of the original file
        if(in.read() != -1){
            Log.e("File Split", "File split not completed.");
        }
        else{

            Log.i(TAG,"file split completed successful");
            switch (mode)
            {
                case "Cooperate":

                    //TODO: implement XOR right there:*************

                    fragment[2] = new File(context.getFilesDir(),filename+".XOR");
                    fragName[2] = filename+".XOR";
                    BufferedOutputStream xorOut = new BufferedOutputStream(new FileOutputStream(fragment[2]));
                    BufferedInputStream inFile1 = new BufferedInputStream(new FileInputStream(fragment[0]));
                    BufferedInputStream inFile2 = new BufferedInputStream(new FileInputStream(fragment[1]));
                    byte[] input1 = new byte[64];
                    byte[] input2 = new byte[64];
                    int bytesRead1, byteRead2;
                    while ((bytesRead1 = inFile1.read(input1)) != -1 || (byteRead2 = inFile2.read(input2)) != -1 ) {
                        int i = 0;
                        for(byte b: input1) {
                            input2[i] = (byte)(b ^ input2[i++]);
                        }
                        if (xorOut != null) {
                            xorOut.write(input2);
                        }
                    }
                    if(bytesRead1 == -1){
                        xorOut.write(inFile2.read());
                    } else if(byteRead2 == -1){
                        xorOut.write(inFile1.read());
                    }

                    inFile1.close();
                    inFile2.close();
                    xorOut.close();


                    // Insert fileFragment into the SQLite
                    String[] filePaths = new String[3];
                    for(int i = 0; i < fragNum; i++){
                        filePaths[i]  = fragment[i].toURI().toString();
                    }
                    createFileFragment(filenameWithoutExt,fragName[0], filePaths[0], fragName[1],filePaths[1], fragName[2],filePaths[2]);

                    //add the file fragment name into firebase database
                    mDatabase.child("users").child(username).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(filenameWithoutExt))
                            {
                                //delete exist file fragment from the file list
                                mDatabase.child("users").child(username).child("files").child(filenameWithoutExt).setValue(null);
                            }
                            mDatabase.child("users").child(username).child("files").child(filenameWithoutExt).setValue("file_extension",fileExtension);
                            mDatabase.child("users").child(username).child("files").child(filenameWithoutExt).child("fragments").setValue(0,fileUploadInfo.fragName[0]);
                            mDatabase.child("users").child(username).child("files").child(filenameWithoutExt).child("fragments").setValue(1,fileUploadInfo.fragName[1]);
                            mDatabase.child("users").child(username).child("files").child(filenameWithoutExt).child("fragments").setValue(2,fileUploadInfo.fragName[2]);
                            mDatabase.child("users").child(username).child("files").child(filenameWithoutExt).setValue("mode",mode);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    break;
                case "Individual":
                    Log.i(TAG,"upload the file metadata to firebase database");
                    //add the file fragment name into firebase database
                    mDatabase.child("users").child(username).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.hasChild(filenameWithoutExt))
                            {
                                //delete exist file fragment from the file list
                                mDatabase.child("users").child(username).child("files").child(filenameWithoutExt).setValue(null);
                            }
                            DatabaseReference fileRef = mDatabase.child("users").child(username).child("files").child(filenameWithoutExt);
                            FileDatabase fileDatabase = new FileDatabase();
                            fileDatabase.setMode(mode);
                            fileDatabase.setFile_name(filename);
                            FragDatabase fragDatabase = new FragDatabase();
                            fragDatabase.setFragName1(fragName[0]);
                            fragDatabase.setFragName2(fragName[1]);
                            fragDatabase.setFragName3("null");
                            fileRef.setValue(fileDatabase);
                            fileRef.child("fragments").setValue(fragDatabase);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e(TAG,databaseError.getMessage());
                        }
                    });
                    Log.i(TAG,"Complete firebase database update");
                    break;
            }
        }

        // close the file
        in.close();
        return fileUploadInfo;
    }

    public Crypt.DecryptNode merge(final String origFileName, final String mode, ArrayList<File> fragments, String secretKey) throws IOException{


        String TAG = "File Merge Progress";
        Log.i(TAG,"begin file merge");
        Log.i(TAG,"fragment 0 size: "+fragments.get(0).length());
        Log.i(TAG,"fragment 1 size: "+fragments.get(1).length());
        BufferedInputStream inputs[] = new BufferedInputStream[2];
        inputs[0] = new BufferedInputStream(new FileInputStream(fragments.get(0)));
        inputs[1] = new BufferedInputStream(new FileInputStream(fragments.get(1)));


        File mergedFile = File.createTempFile(origFileName,".enc",context.getCacheDir());
        BufferedOutputStream outputFile = new BufferedOutputStream(new FileOutputStream(mergedFile));

        switch(mode){

            case "Individual":
                for(int i =0; i<fragments.size(); i++) {
                    int b;
                    while((b=inputs[i].read())!=-1)
                    {
                        outputFile.write(b);
                    }
                    inputs[i].close();
                }
                outputFile.close();
                break;

            case "Cooperation":
                //TODO:cooperative mode merge


                break;
        }
        Log.i(TAG,"merge file size: "+mergedFile.length()+"");
        if(mergedFile.length()>0)
        {
            Log.i(TAG,"file merge successful");
            Log.i(TAG,"merged file name: "+mergedFile.getName());
            Log.i(TAG,"merged file size: "+mergedFile.length()+"");
        }
        Crypt.DecryptNode decryptNode = new Crypt.DecryptNode(origFileName,mergedFile,secretKey);
        return decryptNode;

    }


    public static class FileHandlerInfo implements Serializable{
        String fileName;
        File encryptFile;
        String username;
        public FileHandlerInfo(String fileName, File encryptFile, String username){
            this.fileName = fileName;
            this.encryptFile = encryptFile;
            this.username = username;
        }

    }

    //Class used to store file's name and user's mode, then store original file's metadata into firebase database
    public static class FileDatabase implements Serializable{
        String mode;

        public void setMode(String mode) {
            this.mode = mode;
        }

        public void setFile_name(String file_name) {
            this.file_name = file_name;
        }

        String file_name;

        public String getMode() {
            return mode;
        }

        public String getFile_name() {
            return file_name;
        }

        public FileDatabase()
        {

        }
    }

    //Class used to store fragments' , then put this object into firebase database
    public static class FragDatabase implements Serializable{
        String fragName1;
        String fragName2;
        String fragName3;
        public String getFragName1() {
            return fragName1;
        }

        public String getFragName2() {
            return fragName2;
        }

        public String getFragName3() {
            return fragName3;
        }


        public FragDatabase()
        {

        }

        public void setFragName1(String fragName1) {
            this.fragName1 = fragName1;
        }

        public void setFragName2(String fragName2) {
            this.fragName2 = fragName2;
        }

        public void setFragName3(String fragName3) {
            this.fragName3 = fragName3;
        }
    }

    public void createFileFragment(String fileFragmentsOrigin, String fileFragmentsFirst, String fileFragmentsFirstUri, String fileFragmentsSecond,String fileFragmentsSecondUri ,String fileFragmentsThird, String fileFragmentsThirdUri){
        DBHelper db = new DBHelper(context);
        long id = db.insertFileFragments(fileFragmentsOrigin,fileFragmentsFirst,fileFragmentsFirstUri,fileFragmentsSecond,fileFragmentsSecondUri, fileFragmentsThird, fileFragmentsThirdUri );
        //FileFragment n = SQLiteDatabase.getFile(id);

    }


}

