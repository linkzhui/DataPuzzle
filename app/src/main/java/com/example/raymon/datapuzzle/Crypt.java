package com.example.raymon.datapuzzle;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.security.AlgorithmParameters;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by Jerry on 3/21/18.
 */

public class Crypt {
    public File AESFileEncrypt(CryptNode cryptNode) throws Exception{

        final String TAG = "File Encrypt Method";
        //get filedescriptor and file name from cryptNode
        Uri uri = cryptNode.uri;
        String filename = cryptNode.fileName;
        Context context=UserModeActivity.getContextOfApplication();
        // file to be encrypted

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
        String secretKey = cryptNode.secretKey;
        //Use SHA-1 to generate a hash from user's input key and trim the result to 256 bits (32 bytes)
        byte[] key = secretKey.getBytes("UTF-8");
        MessageDigest sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        //For the AES key size, it can be 128, 192, 256 bits. we choose 128 bits -> 16 bytes;
        key = Arrays.copyOf(key,16);
        SecretKey secret = new SecretKeySpec(key, "AES");


        //"AES/CBC/PKCS5Padding" method require 128 bits key size
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();

        // iv adds randomness to the text and just makes the mechanism more
        // secure
        // used while initializing the cipher
        // file to store the iv
        File encryptIvFile =  File.createTempFile(filename,".iv.enc");
        FileOutputStream ivOutFile = new FileOutputStream(encryptIvFile);
        byte[] iv = params.getParameterSpec(IvParameterSpec.class).getIV();
        ivOutFile.write(iv);
        ivOutFile.close();

        //file encryption
        byte[] input = new byte[64];
        int bytesRead;

        while ((bytesRead = inFile.read(input)) != -1) {
            byte[] output = cipher.update(input, 0, bytesRead);
            if (output != null)
                outFile.write(output);
        }

        byte[] output = cipher.doFinal();
        if (output != null)
            outFile.write(output);

        inFile.close();
        outFile.flush();
        outFile.close();

        Log.i(TAG,"File Encrypted.");
        Log.i(TAG,"encrypt file size: "+encryptFile.length()+" byte");
        return encryptFile;
    }

    public void AESFileDecryption (String filename) throws Exception {

        String password = "javapapers";

        // reading the salt
        // user should have secure mechanism to transfer the
        // salt, iv and password to the recipient
        FileInputStream saltFis = new FileInputStream(filename + ".salt.enc");
        byte[] salt = new byte[8];
        saltFis.read(salt);
        saltFis.close();

        // reading the iv
        FileInputStream ivFis = new FileInputStream(filename + "." + "iv.enc");
        byte[] iv = new byte[16];
        ivFis.read(iv);
        ivFis.close();

        SecretKeyFactory factory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 65536,
                256);
        SecretKey tmp = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(tmp.getEncoded(), "AES");

        // file decryption
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secret, new IvParameterSpec(iv));
        FileInputStream fis = new FileInputStream(filename + ".encryptedfile.des");
        FileOutputStream fos = new FileOutputStream(filename + ".decrypt");
        byte[] in = new byte[64];
        int read;
        while ((read = fis.read(in)) != -1) {
            byte[] output = cipher.update(in, 0, read);
            if (output != null)
                fos.write(output);
        }

        byte[] output = cipher.doFinal();
        if (output != null)
            fos.write(output);
        fis.close();
        fos.flush();
        fos.close();
        System.out.println("File Decrypted.");

    }

    public static class CryptNode{
        Uri uri;

        //string[] to store file info:
        String fileName;
        String secretKey;
        final String TAG = "File Decrypt Method";
        public CryptNode(Uri uri, String fileName, String secretKey)
        {
            this.uri = uri;
            this.fileName = fileName;
            this.secretKey = secretKey;
        }
    }
}
