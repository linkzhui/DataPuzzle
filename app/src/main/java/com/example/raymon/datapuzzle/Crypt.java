package com.example.raymon.datapuzzle;

import android.content.Context;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.AlgorithmParameters;
import java.security.SecureRandom;
import java.security.spec.KeySpec;

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

        //get filedescriptor and file name from cryptNode
        FileDescriptor fileDescriptor = cryptNode.fileDescriptor;
        String filename = cryptNode.fileName;
        Context context=UserModeActivity.getContextOfApplication();
        // file to be encrypted
        BufferedInputStream inFile = new BufferedInputStream(new FileInputStream(fileDescriptor));

        File encryptFile;
        //The Files.createTempFile method provides an alternative method to create an empty file in the temporary-file directory.
        //Files created by that method may have more restrictive access permissions to files created by this method and so may be more suited to security-sensitive applications.
        encryptFile = File.createTempFile(filename,".encryptedfile.des",context.getCacheDir());
        // encrypted file
        BufferedOutputStream outFile = new BufferedOutputStream(new FileOutputStream(encryptFile));


        // password to encrypt the file
        String password = cryptNode.password;

        // password, iv and salt should be transferred to the other end
        // in a secure manner

        // salt is used for encoding
        // writing it to a file
        // salt should be transferred to the recipient securely
        // for decryption
//        byte[] salt = new byte[8];
//        SecureRandom secureRandom = new SecureRandom();
//        secureRandom.nextBytes(salt);
//        FileOutputStream saltOutFile = new FileOutputStream(filename + ".salt.enc");
//        saltOutFile.write(salt);
//        saltOutFile.close();

        SecretKeyFactory factory = SecretKeyFactory
                .getInstance("PBKDF2WithHmacSHA1");
        KeySpec keySpec = new PBEKeySpec(password.toCharArray(), null, 65536,
                256);
        SecretKey secretKey = factory.generateSecret(keySpec);
        SecretKey secret = new SecretKeySpec(secretKey.getEncoded(), "AES");

        //
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, secret);
        AlgorithmParameters params = cipher.getParameters();

        // iv adds randomness to the text and just makes the mechanism more
        // secure
        // used while initializing the cipher
        // file to store the iv
        FileOutputStream ivOutFile = new FileOutputStream(filename + "." + "iv.enc");
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

        System.out.println("File Encrypted.");
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
        FileDescriptor fileDescriptor;

        //string[] to store file info:
        String fileName;
        String password;
        public CryptNode(FileDescriptor fileDescriptor, String fileName, String password)
        {
            this.fileDescriptor = fileDescriptor;
            this.fileName = fileName;
            this.password = password;
        }
    }
}
