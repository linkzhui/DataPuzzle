package com.example.raymon.datapuzzle;

import android.content.Intent;
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

import java.util.ArrayList;
import java.util.List;

public class IndividualFileDownloadActivity extends AppCompatActivity {

    private ListView listView;
    private DatabaseReference mDatabase;
    private String username;
    private ArrayAdapter<String> adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_individual_file_download);
        //Get the username
        Intent intent = getIntent();
        username = intent.getStringExtra("username");
        Log.e("username",username);
        listView = (ListView) findViewById(R.id.listViewResults);
        //Store all the available download file into fileList
        ArrayList<String> fileList = new ArrayList<>();
        getBookList(fileList);
        adapter = new ArrayAdapter<>(this,android.R.layout.simple_list_item_1,fileList);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final String item = (String) parent.getItemAtPosition(position);

                //implement file download and file upload
                Toast.makeText(getBaseContext(),item,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getBookList(final ArrayList<String> list){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(username).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long bookCount = dataSnapshot.getChildrenCount();
                Log.e("file count",bookCount+"");
                for(DataSnapshot child:dataSnapshot.getChildren())
                {
                    String filename = child.getKey();
                    Log.e("book name",filename);
                    list.add(filename);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return;
    }
}
