package com.example.raymon.datapuzzle;

import android.content.Intent;
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
    private String TAG = "Individual File Download Activity";
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
                            String fragmentName = child.getValue(String.class);
                            Log.i(TAG,"fragment name: "+fragmentName);
                            fragNameArray.add(fragmentName);
                        }
                        String originalFileName = dataSnapshot.child("file_name").getValue(String.class);
                        //TODO: 1.search internal file dir and external file dir


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

    private boolean internalFileSearch(String fname)
    {
        //search the file fragment is existed in the internal storage or not?
        File file = getBaseContext().getFileStreamPath(fname);
        boolean searchResult =  file.exists();
        Log.i(TAG,fname + "existed in the internal storage? " +searchResult);
        return searchResult;
    }

    private boolean externalFileSearch(String fname)
    {
        //search the file fragment is existed in the external storage or not?
        File decryptFolder = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOWNLOADS), "DataPuzzle");
        if(decryptFolder.isDirectory()&&decryptFolder.exists())
        {
            return true;
        }
        else{
            return false;
        }
    }
}
