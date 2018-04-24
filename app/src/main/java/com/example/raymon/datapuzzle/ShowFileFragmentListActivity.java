package com.example.raymon.datapuzzle;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShowFileFragmentListActivity extends AppCompatActivity {

    FileFragmentListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;

    private List<FileFragment> fileFragmentsList = new ArrayList<>();
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_file_fragment_list);


        // preparing list data
        prepareListData();

        // get the listview
        expListView = (ExpandableListView) findViewById(R.id.filefragment_name_list);


        listAdapter = new FileFragmentListAdapter(this, listDataHeader, listDataChild);

        // setting list adapter
        expListView.setAdapter(listAdapter);

        // Listview Group click listener
        expListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {

            @Override
            public boolean onGroupClick(ExpandableListView parent, View v,
                                        int groupPosition, long id) {
                // Toast.makeText(getApplicationContext(),
                // "Group Clicked " + listDataHeader.get(groupPosition),
                // Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Listview Group expanded listener
        expListView.setOnGroupExpandListener(new ExpandableListView.OnGroupExpandListener() {

            @Override
            public void onGroupExpand(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Expanded",
                        Toast.LENGTH_SHORT).show();
            }
        });

        // Listview Group collasped listener
        expListView.setOnGroupCollapseListener(new ExpandableListView.OnGroupCollapseListener() {

            @Override
            public void onGroupCollapse(int groupPosition) {
                Toast.makeText(getApplicationContext(),
                        listDataHeader.get(groupPosition) + " Collapsed",
                        Toast.LENGTH_SHORT).show();

            }
        });

        // Listview on child click listener
        expListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {

            @Override
            public boolean onChildClick(ExpandableListView parent, View v,
                                        int groupPosition, int childPosition, long id) {
                // TODO Auto-generated method stub
                String origin_file_name=  listDataHeader.get(groupPosition);
                String file_fragment_name =  listDataChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition);

                Intent intent = new Intent(ShowFileFragmentListActivity.this
                        , WiFiDirectCopActivity.class);
                intent.putExtra("FILE_FRAGMENT_NAME", file_fragment_name);
                startActivity(intent);

                return false;

            }
        });
    }

    private void prepareListData(){

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();

        // Adding child data
        db = new DBHelper(this);
        List<FileFragment> fileFragmentsList = new ArrayList<>();
        fileFragmentsList.addAll(db.getAllFiles());

        for(int i = 0; i < fileFragmentsList.size(); i++){
            String fileOriginName = fileFragmentsList.get(i).getFileOriginName();
            listDataHeader.add(fileOriginName);
            List<String> childList = new ArrayList<>();
            childList.add(fileFragmentsList.get(i).getFileFragmentNameOne());
            childList.add(fileFragmentsList.get(i).getFileFragmentNameTwo());
            childList.add(fileFragmentsList.get(i).getFileFragmentNameThree());
            listDataChild.put(fileOriginName, childList);
        }

    }
}
