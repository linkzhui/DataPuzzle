// Copyright 2011 Google Inc. All Rights Reserved.

package com.example.raymon.datapuzzle;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * A service that process each file transfer request i.e Intent by opening a
 * socket connection with the WiFi Direct Group Owner and writing the file
 */
public class FileTransferService extends IntentService {

    private DBHelper db;

    private static final int SOCKET_TIMEOUT = 5000;
    public static final String ACTION_SEND_FILE = "com.example.android.wifidirect.SEND_FILE";
    public static final String EXTRAS_FILE_PATH = "file_url";
    public static final String EXTRAS_GROUP_OWNER_ADDRESS = "go_host";
    public static final String EXTRAS_GROUP_OWNER_PORT = "go_port";
    public static final String EXTRA_FILE_NAME = "file_name";
    public static final String EXTRA_File_Origin_Name = "file_origin_name";


    public FileTransferService(String name) {
        super(name);
    }

    public FileTransferService() {
        super("FileTransferService");
    }

    /*
     * (non-Javadoc)
     * @see android.app.IntentService#onHandleIntent(android.content.Intent)
     */
    @Override
    protected void onHandleIntent(Intent intent) {

        Context context = getApplicationContext();
        if (intent.getAction().equals(ACTION_SEND_FILE)) {
            String fileUri = intent.getExtras().getString(EXTRAS_FILE_PATH);
            String host = intent.getExtras().getString(EXTRAS_GROUP_OWNER_ADDRESS);
            String fileName = intent.getExtras().getString(EXTRA_FILE_NAME);
            String fileOriginName = intent.getStringExtra(EXTRA_File_Origin_Name);
            Socket socket = new Socket();
            int port = intent.getExtras().getInt(EXTRAS_GROUP_OWNER_PORT);

            try {
                Log.d(WiFiDirectCopActivity.TAG, "Opening client socket - ");
                socket.bind(null);
                socket.connect((new InetSocketAddress(host, port)), SOCKET_TIMEOUT);

                Log.d(WiFiDirectCopActivity.TAG, "Client socket - " + socket.isConnected());
                BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
                DataOutputStream dos = new DataOutputStream(out);
                dos.writeUTF(fileName);

                //OutputStream stream = socket.getOutputStream();
                ContentResolver cr = context.getContentResolver();
                InputStream is = null;
                try {
                    is = cr.openInputStream(Uri.parse(fileUri));
                } catch (FileNotFoundException e) {
                    Log.d(WiFiDirectCopActivity.TAG, e.toString());
                }
                DeviceDetailFragment.copyFile(is, dos);
                Log.d(WiFiDirectCopActivity.TAG, "Client: Data written");
                if(!fileOriginName.equals("file_origin_name")){
                    //updateFileFragmentDataBase(fileOriginName, fileName);
                }
                 // delte file from internal storage
                //deltefile(fileUri);
            } catch (IOException e) {
                Log.e(WiFiDirectCopActivity.TAG, e.getMessage());
            } finally {
                if (socket != null) {
                    if (socket.isConnected()) {
                        try {
                            socket.close();
                        } catch (IOException e) {
                            // Give up
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
    }

    // delete file after succefully transit
    public void deltefile(String fileUri){
        Uri myUri = Uri.parse(fileUri);
        File toDelteFile = new File(myUri.getPath());
        toDelteFile.delete();

    }

    public void updateFileFragmentDataBase(String fileOriginName, String fileFragmentName){
        db = new DBHelper(this);
        FileFragment toUpdateFragment;
        List<FileFragment> fileFragmentsList = new ArrayList<>();
        fileFragmentsList.addAll(db.getAllFiles());
        for(int i = 0; i < fileFragmentsList.size(); i++){
            String fileOriginNameRead = fileFragmentsList.get(i).getFileOriginName();
            if(fileOriginNameRead.equals(fileOriginName)){
                toUpdateFragment = fileFragmentsList.get(i);
                updateFileFragment(toUpdateFragment, fileFragmentName);
                break;
            }
        }
    }

    public void updateFileFragment(FileFragment toUpdateFragment, String fileFragmentName){

        String fileFragmentNameOne = toUpdateFragment.getFileFragmentNameOne();
        String fileFragmentNameTwo = toUpdateFragment.getFileFragmentNameTwo();
        String fileFragmentNameThree = toUpdateFragment.getFileFragmentNameThree();

        if (fileFragmentNameOne.equals(fileFragmentName)){
            toUpdateFragment.setFileFragmentNameOne(null);
            toUpdateFragment.setFileFragmentNameOneUri(null);
        }else if (fileFragmentNameTwo.equals(fileFragmentName)){
            toUpdateFragment.setFileFragmentNameTwo(null);
            toUpdateFragment.setFileFragmentNameTwoUri(null);
        }else if (fileFragmentNameThree.equals(fileFragmentName)) {
            toUpdateFragment.setFileFragmentNameThree(null);
            toUpdateFragment.setFileFragmentNameThreeUri(null);
        }else{
            Log.d(TAG, "updateFileFragment: Fail");
        }

        if (toUpdateFragment.getFileFragmentNameOne() == null
                &&toUpdateFragment.getFileFragmentNameTwo() == null
                &&toUpdateFragment.getFileFragmentNameThree() == null ){
            // delete toUpdateFragment from Database
        }else{
            // update toUpdateFragment to Database
        }

    }

}
