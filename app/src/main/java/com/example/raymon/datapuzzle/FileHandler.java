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
        final File[] fragment = new File[fragNum];
        final String[] fragName = new String[fragNum];
        for (int subfileIndex = 0; subfileIndex < 2; subfileIndex++)
        {
            // If the mode is Individual, create the temp fragment file to store the result of split file
            // If the mode is Cooperate, create the file store in internal storage
            fragment[subfileIndex] = mode.equals("Individual")?  File.createTempFile(filename, "."+subfileIndex,context.getCacheDir()):new File(context.getFilesDir(),filename+"."+subfileIndex);
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
            in.close();
            Log.i(TAG,"file split completed successful");
            switch (mode)
            {
                case "Cooperate":

                    //TODO: implement XOR right there:*************

                    fragment[2] = new File(context.getFilesDir(),filename+".XOR");
                    fragName[2] = filename+".XOR";
                    BufferedOutputStream xorOut = new BufferedOutputStream(new FileOutputStream(fragment[2]));
                    BufferedInputStream inFile0 = new BufferedInputStream(new FileInputStream(fragment[0]));
                    BufferedInputStream inFile1 = new BufferedInputStream(new FileInputStream(fragment[1]));
                    byte[] input1 = new byte[64];
                    byte[] input2 = new byte[64];
                    int bytesRead1, bytesRead2;

                    while ((bytesRead1 = inFile0.read(input1)) != -1 && (bytesRead2 =inFile1.read(input2)) != -1 ) {
                        for(int i = 0;i<Math.min(bytesRead1,bytesRead2);i++) {
                            xorOut.write(input1[i]^input2[i]);
                        }
                    }
                    if(fragment[0].length()<fragment[1].length())
                    {
                        xorOut.write(inFile1.read());
                    }
                    else if(fragment[0].length()>fragment[1].length())
                    {
                        xorOut.write(inFile0.read());
                    }
//                    else if(bytesRead2 == -1){
//                        xorOut.write(inFile1.read());
//                    }

                    inFile0.close();
                    inFile1.close();
                    xorOut.close();

                    Log.i(TAG,"XOR file size is: "+fragment[2].length());

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
                            DatabaseReference fileRef = mDatabase.child("users").child(username).child("files").child(filenameWithoutExt);

                            FileDatabase fileDatabase = new FileDatabase();
                            fileDatabase.setMode(mode);
                            fileDatabase.setFile_name(filename);
                            fileRef.setValue(fileDatabase);
                            for(int i = 1;i<=3;i++)
                            {
                                fileRef.child("fragments").child("fragName"+i).child("fragName").setValue(fragName[i-1]);
                                fileRef.child("fragments").child("fragName"+i).child("receiver").setValue("null");
                                fileRef.child("fragments").child("fragName"+i).child("fragSize").setValue(fragment[i-1].length());
                            }


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


        File mergedFile = new File(context.getFilesDir(),origFileName+".enc");
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
        }
        Log.i(TAG,"merge file size: "+mergedFile.length()+"");
        if(mergedFile.length()>0)
        {
            Log.i(TAG,"file merge successful");
            Log.i(TAG,"merged file name: "+mergedFile.getName());
            Log.i(TAG,"merged file size: "+mergedFile.length()+"");
        }
        Crypt.DecryptNode decryptNode = new Crypt.DecryptNode(origFileName,mergedFile,secretKey,0);
        return decryptNode;

    }

    public File mergeCooper(int mode, File fragment1, File fragment2,long[] fragSize,String fileName) throws IOException {
        String TAG = "Cooperate File Merge";
        Log.i(TAG,"Begin file merge");
        Log.i(TAG,"file fragement 1 is: "+fragment1.getName()+" , framgnment 2 is: "+fragment2.getName());
        //Fragment 1 size <= Fragment 2 size
        //fragment A, B, C (XOR)
        //XOR mode 1: A, B
        //XOR mode 2: A, C
        //XOR mode 3: B, C
        File mergedFile = new File(context.getFilesDir(),fileName+".enc");
        File fileB = null;
        File fileA = null;
        BufferedOutputStream outputFile = new BufferedOutputStream(new FileOutputStream(mergedFile));
        BufferedInputStream inputs[] = new BufferedInputStream[2];
        inputs[0] = new BufferedInputStream(new FileInputStream(fragment1));
        inputs[1] = new BufferedInputStream(new FileInputStream(fragment2));
        BufferedInputStream frag[] = new BufferedInputStream[2];
        switch (mode)
        {
            case 1:
            {
                frag[0] = inputs[0];
                frag[1] = inputs[1];
                break;
            }
            case 2:
            {
                //XOR mode 2: A, C
                byte[] input0 = new byte[64];
                byte[] input1 = new byte[64];
                int bytesRead0;
                int bytesRead1 = 0;
                fileA = fragment1;
                fileB = File.createTempFile(fileName,".B",context.getCacheDir());
                BufferedOutputStream outputStreamB = new BufferedOutputStream(new FileOutputStream(fileB));

                //fragment B's size should bigger or equal to fragment A's size
                while ((bytesRead0 = inputs[0].read(input0)) != -1 && (bytesRead1 =inputs[1].read(input1)) != -1 ) {
                    for(int i = 0;i<Math.min(bytesRead0,bytesRead1);i++) {
                        outputStreamB.write(input1[i]^input0[i]);
                    }
                }
                if(fragSize[0]<fragSize[1])
                {
                    outputStreamB.write(0^input1[bytesRead1-1]);
                }
                inputs[0].close();
                inputs[1].close();
                outputStreamB.close();
                frag[0] = new BufferedInputStream(new FileInputStream(fileA));
                frag[1] = new BufferedInputStream(new FileInputStream(fileB));
                break;
            }
            case 3:
            {
                //XOR mode 3: B, C
                byte[] input0 = new byte[64];
                byte[] input1 = new byte[64];
                int bytesRead0;
                int bytesRead1;
                fileA = File.createTempFile(fileName,".A",context.getCacheDir());
                fileB = fragment1;
                BufferedOutputStream outputStreamA = new BufferedOutputStream(new FileOutputStream(fileA));
                while ((bytesRead0 = inputs[0].read(input0)) != -1 && (bytesRead1 =inputs[1].read(input1)) != -1 ) {
                    for(int i = 0;i<Math.min(bytesRead0,bytesRead1);i++) {
                        outputStreamA.write(input1[i]^input0[i]);
                    }
                }
                //if fragment C size is bigger than fragment B size,
                //then we fragment A need to read one more byte
//                if(fragment2.length()>fragment1.length())
//                {
//                    outputStreamA.write(inputs[1].read());
//                }
                inputs[0].close();
                inputs[1].close();
                outputStreamA.close();
                frag[0] = new BufferedInputStream(new FileInputStream(fileA));
                frag[1] = new BufferedInputStream(new FileInputStream(fileB));
                break;
            }
        }


        for(int i =0; i<frag.length; i++) {
            int b;
            while((b=frag[i].read())!=-1)
            {
                outputFile.write(b);
            }
        }

        frag[0].close();
        frag[1].close();
        outputFile.close();
        //TODO: Delete the fragment after file merge successful
//        fileA.delete();
//        fileB.delete();
//        fragment1.delete();
//        fragment2.delete();
        Log.i(TAG,"file merge completed");
        Log.i(TAG,"merged file length is: "+mergedFile.length());
        return mergedFile;

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

    public static class FragInfo implements Serializable{
        String fragName;
        String receiver;
        public String getFragName() {
            return fragName;
        }

        public void setFragName(String fragName) {
            this.fragName = fragName;
        }

        public void setReceiver(String receiver) {
            this.receiver = receiver;
        }

        public String getReceiver() {
            return receiver;

        }


        public FragInfo (){

        }
    }
    public void createFileFragment(String fileFragmentsOrigin, String fileFragmentsFirst, String fileFragmentsFirstUri, String fileFragmentsSecond,String fileFragmentsSecondUri ,String fileFragmentsThird, String fileFragmentsThirdUri){
        DBHelper db = new DBHelper(context);
        List<FileFragment> fileFragmentsList = new ArrayList<>();

        fileFragmentsList.addAll(db.getAllFiles());

        for(int i = 0; i < fileFragmentsList.size(); i++) {
            String fileOriginName = fileFragmentsList.get(i).getFileOriginName();
            if(fileFragmentsOrigin.equals(fileOriginName)){
                db.deleteFileFragment(fileFragmentsList.get(i));
            }
        }

        long id = db.insertFileFragments(fileFragmentsOrigin,fileFragmentsFirst,fileFragmentsFirstUri,fileFragmentsSecond,fileFragmentsSecondUri, fileFragmentsThird, fileFragmentsThirdUri );

    }


}

