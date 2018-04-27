package com.example.raymon.datapuzzle;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
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
import java.util.ArrayList;

public class CooperFileDecMergeActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseReference mDatabase;
    private String username;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> fragNameArray = new ArrayList<>();
    private ArrayList<String> fragReceiverArray = new ArrayList<>();
    private String TAG = "Cooper Mode File Merge/Decry Activity";
    private String secretkey;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cooper_file_dec_merge);
        //Get the username
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        Log.e("username",username);
        secretkey = intent.getStringExtra("secret key");
        listView = (ListView) findViewById(R.id.listViewResults);
        //Store all the available download file into fileList
        ArrayList<String> fileList = new ArrayList<>();
        getBookList(fileList);

        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,fileList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String fileName = (String) parent.getItemAtPosition(position);

                //implement file download and file upload
                Toast.makeText(getBaseContext(),fileName+" is selected",Toast.LENGTH_SHORT).show();

                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child("users").child(username).child("files").child(fileName).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        for(DataSnapshot child:dataSnapshot.child("fragments").getChildren())
                        {
                            String fragmentName = child.child("fragName").getValue(String.class);
                            String receiver = child.child("receiver").getValue(String.class);
                            Log.i(TAG,"fragment name: "+fragmentName);
                            fragNameArray.add(fragmentName);
                            fragReceiverArray.add(receiver);
                        }
                        String originalFileName = dataSnapshot.child("file_name").getValue(String.class);
                        //TODO: 1.search internal file dir and external file dir

                        ArrayList<FragmentInfo> fragmentInfos = new ArrayList<>();
                        int fragmentFoundCount = 0;
                        //the index in the fragment Name array, which is obtained from the firebase database
                        int index = 0;
                        while(index<3||fragmentFoundCount<2)
                        {
                            File fragment = internalFileSearch(fragNameArray.get(index));
                            if(fragment!=null)
                            {
                                fragmentFoundCount++;
                                fragmentInfos.add(new FragmentInfo(fragment,index));
                                Log.i(TAG,fragNameArray.get(index)+" is founded");
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

                                }
                                else{
                                    //fragment A, C founded
                                    //XOR mode 2
                                }
                            }
                            else if(fragmentInfos.get(0).fragIndex==1 && fragmentInfos.get(1).fragIndex==2){
                                //fragment b, c founded
                                //XOR mode 3

                            }
                            else{
                                Log.e(TAG,"unkonwn error");
                            }
                        }
                        else{
                            //TODO: reminder the user which fragment is missing and who have this fragment
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
        mDatabase = FirebaseDatabase.getInstance().getReference();
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

    private File searchFile(String fname)
    {
        File result = internalFileSearch(fname);
        if(result==null)
        {
            //the fragment is not exist in the internal storage, search if the fragment is exist in the external storage
            result = externalFileSearch(fname);
        }
        return result;

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
