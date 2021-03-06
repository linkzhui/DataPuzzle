package com.example.raymon.datapuzzle;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class CooperFileDecMergeActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private String username = UserModeActivity.username;
    private ArrayAdapter<String> adapter;
    private String[] fragNameArray = new String[3];
    private String[] fragReceiverArray = new String[3];
    private long[] fragSizeArray = new long[3];
    private boolean[] fragExist = new boolean[3];
    private String TAG = "Cooper Mode File Merge/Decry Activity";
    private String secretkey;
    private FileHandler fileHandler = new FileHandler();
    private File mergedFile;
    private String fileName;
    private String originFileName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate Mode");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooper_file_dec_merge);
        //Get the username
        Intent intent = getIntent();
        Log.i(TAG,"username: "+username);
        secretkey = intent.getStringExtra("secret key");
        listView = findViewById(R.id.listViewResults);
        //Store all the available download file into fileList
        ArrayList<String> fileList = new ArrayList<>();
        getBookList(fileList);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,fileList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                fileName = (String) parent.getItemAtPosition(position);

                //implement file download and file upload
                Toast.makeText(getBaseContext(),fileName+" is selected",Toast.LENGTH_SHORT).show();
                mDatabase.child("users").child(username).child("files").child(fileName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        int i = 0;
                        for(DataSnapshot child:dataSnapshot.child("fragments").getChildren())
                        {
                            String fragmentName = child.child("fragName").getValue(String.class);
                            String receiver = child.child("receiver").getValue(String.class);
                            Long fragSize = child.child("fragSize").getValue(Long.class);
                            Log.i(TAG,"fragment name: "+fragmentName);
                            fragNameArray[i]=fragmentName;
                            fragReceiverArray[i]=receiver;
                            fragSizeArray[i] = fragSize;
                            i++;
                        }

                        originFileName = dataSnapshot.child("file_name").getValue(String.class);
                        //TODO: 1.search internal file dir and external file dir

                        ArrayList<FragmentInfo> fragmentInfos = new ArrayList<>();
                        int fragmentFoundCount = 0;
                        //the index in the fragment Name array, which is obtained from the firebase database
                        int index = 0;
                        while(index<3 && fragmentFoundCount<2)
                        {
                            File fragment = internalFileSearch(fragNameArray[index]);
                            if(fragment!=null)
                            {
                                fragmentFoundCount++;
                                fragmentInfos.add(new FragmentInfo(fragment,index));
                                fragExist[index] = true;
                                Log.i(TAG,fragNameArray[index]+" is founded in internal");
                            }
                            else{
                                fragment = externalFileSearch(fragNameArray[index]);
                                if(fragment!=null)
                                {
                                    fragmentFoundCount++;
                                    fragmentInfos.add(new FragmentInfo(fragment,index));
                                    fragExist[index] = true;
                                    Log.i(TAG,fragNameArray[index]+" is founded in external");
                                }
                            }
                            index++;
                        }
                        if(fragmentFoundCount==2)
                        {
                            //fragment A, B, C (XOR)
                            //XOR mode 1: A, B
                            //XOR mode 2: A, C
                            //XOR mode 3: B, C

                            if(fragmentInfos.get(0).fragIndex==0)
                            {
                                if(fragmentInfos.get(1).fragIndex==1)
                                {
                                    // fragment A, B founded
                                    //XOR mode 1
                                    try {
                                        mergedFile = fileHandler.mergeCooper(1,fragmentInfos.get(0).fragment,fragmentInfos.get(1).fragment,fragSizeArray,originFileName);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                                else{
                                    //fragment A, C founded
                                    //XOR mode 2
                                    try {
                                        mergedFile = fileHandler.mergeCooper(2,fragmentInfos.get(0).fragment,fragmentInfos.get(1).fragment,fragSizeArray,originFileName);
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            else{
                                //fragmentInfos.get(0).fragIndex==1 && fragmentInfos.get(1).fragIndex==2
                                //fragment b, c founded
                                //XOR mode 3
                                try {
                                    mergedFile = fileHandler.mergeCooper(3,fragmentInfos.get(0).fragment,fragmentInfos.get(1).fragment,fragSizeArray,originFileName);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }

                            Log.i(TAG,"Begin file merge");
                            Crypt crypt = new Crypt();
                            try {
                                crypt.AESFileDecryption(new Crypt.DecryptNode(originFileName,mergedFile,secretkey,1));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                        else{
                            String notifyMessage = "";
                            for(int j = 0;j<3;j++)
                            {
                                if(!fragExist[j])
                                {
                                    notifyMessage +="Fragment "+fragNameArray[j]+" is missing,"+" you can get this fragment from "+fragReceiverArray[j]+".";
                                }
                            }
                            Log.i(TAG,notifyMessage);
                            Toast.makeText(getBaseContext(),notifyMessage,Toast.LENGTH_LONG).show();
                            Intent myIntent = new Intent(getBaseContext(), UserModeActivity.class);
                            myIntent.putExtra("username", username);
                            //return to cooperate mode
                            myIntent.putExtra("pageIndex",1);
                            startActivity(myIntent);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
    }

    private void getBookList(final ArrayList<String> list){

        username = UserModeActivity.username;
        Log.i(TAG,"username:"+username);
        mDatabase.child("users").child(username).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child:dataSnapshot.getChildren())
                {
                    String filename = child.getKey();
                    String mode = child.child("mode").getValue(String.class);
                    if(mode.equals("Cooperate") || mode.equals("individual"))
                    {
                        Log.e("file name",filename);
                        list.add(filename);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return;
    }

    private File internalFileSearch(String fname)
    {
        //search the file fragment is existed in the internal storage or not?
        File file = getBaseContext().getFileStreamPath(fname);
        if(file.exists())
        {
            String uri = file.toURI().toString()+"/"+fname;
            Log.i(TAG,fname + "existed in the internal storage. Uri is : " +uri);
            return file;
        }
        Log.i(TAG,fname + "existed is not in the internal storage");
        return null;
    }

    private File externalFileSearch(String fname)
    {
        //search the file fragment is existed in the external storage or not?
        File decryptFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "DataPuzzle");
        if(decryptFolder.exists() && decryptFolder.isDirectory())
        {
            File myFile = new File(decryptFolder.getAbsoluteFile()+"/"+fname);
            if(myFile.exists())
            {
                String uri = decryptFolder.toURI()+"/"+fname;
                Log.i(TAG,fname + " is founded, and uri is : "+ uri);
                return myFile;
            }
        }
        Log.i(TAG,fname + "existed is not in the external storage");
        return null;
    }

    private class FragmentInfo{
        File fragment;
        int fragIndex;
        public FragmentInfo(File fragment,int fragIndex)
        {
            this.fragment = fragment;
            this.fragIndex = fragIndex;
        }
    }
}
