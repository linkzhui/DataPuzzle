package com.example.raymon.datapuzzle;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.widget.Toast;
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
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.spec.KeySpec;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

/**
 * Created by Jerry on 3/21/18.
 */

public class Crypt {
    public File AESFileEncrypt(CryptNode cryptNode) throws Exception{

        final String TAG = "File Encrypt Method";

        //Get Uri from the selected file
        Uri uri = cryptNode.uri;
        String filename = cryptNode.fileName;
        Context context=UserModeActivity.getContextOfApplication();
        //Use contentResolver to get the content of selected file
        Context applicationContext = UserModeActivity.getContextOfApplication();
        InputStream inFile = applicationContext.getContentResolver().openInputStream(uri);

        //The Files.createTempFile method provides an alternative method to create an empty file in the temporary-file directory.
        //Files created by that method may have more restrictive access permissions to files created by this method and so may be more suited to security-sensitive applications.

        Log.i(TAG,"generate  the encrypt file");
        File encryptFile = File.createTempFile(filename,".encryptedfile.des",context.getCacheDir());

        // encrypted file
        BufferedOutputStream outFile = new BufferedOutputStream(new FileOutputStream(encryptFile));

        //**Use user's secret key input to generate secret key for cipher to encrypt/decrypt the message
        // password to encrypt the file
        String userKey = cryptNode.secretKey;
        byte[] salt = new byte[8];
        //For the AES key size, it can be 128, 192, 256 bits. we choose 128 bits -> 16 bytes;
        SecretKeyFactory factory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(userKey.toCharArray(), salt, 65536,
                128);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        //
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();


        //file encryption
        byte[] input = new byte[64];
        int bytesRead;

        while ((bytesRead = inFile.read(input)) != -1) {
            byte[] output = cipher.update(input, 0, bytesRead);
            if (output != null) {
                outFile.write(output);
            }
        }

        byte[] output = cipher.doFinal();
        if (output != null)
        {
            outFile.write(output);
        }

        inFile.close();
        outFile.flush();
        outFile.close();

        Log.i(TAG,"File Encrypted.");
        Log.i(TAG,"Encrypt file size: "+encryptFile.length()+" byte");

        File originalFile = new File(uri.getPath());
        boolean deleteOrignalFile = originalFile.delete();
        if(deleteOrignalFile)
        {
            Log.i(TAG,"Original file delete successful!");
        }
        else{
            Log.e(TAG,"Delete original file failed");
        }

        return encryptFile;
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void AESFileDecryption (final DecryptNode decryptNode) throws Exception {

        final String filename = decryptNode.fileName;
        final String TAG = "Decrypt progress";
        final Context context=UserModeActivity.getContextOfApplication();

        Log.i(TAG,"begin file decrypt");
        //**Use user's secret key input to generate secret key for cipher to encrypt/decrypt the message
        // password to encrypt the file
        String userKey = decryptNode.secretKey;
        Log.i(TAG,"secret key: "+userKey);
        byte[] salt = new byte[8];
        //For the AES key size, it can be 128, 192, 256 bits. we choose 128 bits -> 16 bytes;
        SecretKeyFactory factory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(userKey.toCharArray(), salt, 65536,
                128);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");


        // file decryption
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret);

        //Read in file to descrypt
        FileInputStream encryptFileInputStream = new FileInputStream(decryptNode.encryFile);
        BufferedInputStream inputFile = new BufferedInputStream(encryptFileInputStream);

        //check if external storage is available for read and write
        String state = Environment.getExternalStorageState();
        if(Environment.MEDIA_MOUNTED.equals(state)) {
            Log.i(TAG, "external storage file write permission gained");
            //create output file and store the decrypt file into external storage with its original file name
            File decryptFolder = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_DOWNLOADS), "DataPuzzle");
            if (!decryptFolder.mkdirs()) {
                Log.e(TAG, "Directory not created");
            } else {
                Log.i(TAG, "Directory created successful");
            }

            File decryptFile = new File(decryptFolder, filename);
            BufferedOutputStream outFile = new BufferedOutputStream(new FileOutputStream(decryptFile));


            byte[] in = new byte[64];
            int read;
            while ((read = inputFile.read(in)) != -1) {
                byte[] output = cipher.update(in, 0, read);
                if (output != null)
                    outFile.write(output);
            }

            byte[] output = cipher.doFinal();
            if (output != null)
                outFile.write(output);
            inputFile.close();
            outFile.flush();
            outFile.close();
            System.out.println("File Decrypted.");
            Toast.makeText(context, "Check the file under Download/DataPuzzle folder", Toast.LENGTH_SHORT).show();
            if (decryptNode.encryFile.exists())
            {
                decryptNode.encryFile.delete();
            }
            final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
            mDatabase.child("users").child(UserModeActivity.username).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    String filenameWithoutExt = decryptNode.fileName.substring(0,decryptNode.fileName.indexOf('.'));
                    if(dataSnapshot.hasChild(filenameWithoutExt))
                    {
                        mDatabase.child("users").child(UserModeActivity.username).child("files").child(filenameWithoutExt).setValue(null);
                        if(decryptNode.mode==1)
                        {
                            //in cooperate mode, merge file successful, delete the file information in sqlite
                            DBHelper dbHelper = new DBHelper(context);
                            dbHelper.deleteFilebyName(filenameWithoutExt);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Log.e(TAG,databaseError.getMessage());
                }
            });

            Intent myIntent = new Intent(context, UserModeActivity.class);
            myIntent.putExtra("username",UserModeActivity.username);
            myIntent.putExtra("pageIndex",decryptNode.mode);
            myIntent.setFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(myIntent);

        }
        else{
            Log.e(TAG,"external storage write permission failed");
        }



    }

    public static class CryptNode{
        Uri uri;

        //string[] to store file info:
        String fileName;
        String secretKey;
        public CryptNode(Uri uri, String fileName, String secretKey)
        {
            this.uri = uri;
            this.fileName = fileName;
            this.secretKey = secretKey;
        }
    }

    public static class DecryptNode{
        String fileName;
        File encryFile;
        String secretKey;
        int mode;
        public DecryptNode(String fileName, File encryFile, String secretKey,int mode)
        {
            this.fileName = fileName;
            this.encryFile = encryFile;
            this.secretKey = secretKey;
            this.mode = mode;
        }
    }
}
