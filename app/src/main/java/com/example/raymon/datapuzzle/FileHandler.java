package com.example.raymon.datapuzzle;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jerry on 3/21/18.
 */

public class FileHandler {

    public static List<BufferedOutputStream> split(String filename, List<String> fileList) throws IOException
    {
        // open the file
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
        List<BufferedOutputStream> listOfOutFileStream = new ArrayList<>();

        // get the file length
        File f = new File(filename);
        long fileSize = f.length();

        // loop for each full chunk
        int subfile;
        long chunkSize = fileSize/2;
        for (subfile = 0; subfile < 2; subfile++)
        {
            // open the output file
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(filename + "." + subfile));
            listOfOutFileStream.add(out);
            fileList.add(filename + "." + subfile);
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
//            out.close();
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
        return listOfOutFileStream;
    }

    public static void merge(String filename, List<String> fileList) throws IOException{

        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
    }

}
