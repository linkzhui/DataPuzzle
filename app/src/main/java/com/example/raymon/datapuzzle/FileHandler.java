package com.example.raymon.datapuzzle;

import android.content.Context;
import android.util.Log;

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


    Context context = UserModeActivity.getContextOfApplication();
    public GoogleDriveFileUploadActivity.FileUploadInfo[] split(FileHandlerInfo fileHandlerInfo) throws IOException
    {

        // open the file

        long fileSize = Long.parseLong(fileHandlerInfo.fileSize);
        String filename = fileHandlerInfo.fileName;
        // loop for each full chunk
        int subfile;
        long chunkSize = fileSize/2;
        GoogleDriveFileUploadActivity.FileUploadInfo[] fileUploadInfo = new GoogleDriveFileUploadActivity.FileUploadInfo[2];
        //get InputStream from encryptFile
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileHandlerInfo.encryptFile));
        for (subfile = 0; subfile < 2; subfile++)
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
        Log.i("File Split", "Returning File List");
        return fileUploadInfo;
    }

    public static void merge(String filename, List<String> fileList) throws IOException{

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
    }


    public static class FileHandlerInfo implements Serializable{
        String fileName;
        String fileSize;
        File encryptFile;
        public FileHandlerInfo(String fileName, String fileSize, File encryptFile){
            this.fileName = fileName;
            this.fileSize = fileSize;
            this.encryptFile = encryptFile;
        }

    }
}
