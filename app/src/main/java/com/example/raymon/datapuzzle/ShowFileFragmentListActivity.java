package com.example.raymon.datapuzzle;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShowFileFragmentListActivity extends Activity {

    FileFragmentListAdapter listAdapter;
    ExpandableListView expListView;
    List<String> listDataHeader;
    HashMap<String, List<String>> listDataChild;
    HashMap<String, List<String>> listURIChild;
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

                String file_fragment_uri =  listURIChild.get(
                        listDataHeader.get(groupPosition)).get(
                        childPosition);


                Intent resultIntent = new Intent();
                resultIntent.setData(Uri.parse(file_fragment_uri));
                resultIntent.putExtra("fileOriginName",origin_file_name);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();

                return false;

            }
        });
    }

    private void prepareListData(){

        listDataHeader = new ArrayList<String>();
        listDataChild = new HashMap<String, List<String>>();
        listURIChild = new HashMap<String, List<String>>();

        // Adding child data
        db = new DBHelper(this);
        List<FileFragment> fileFragmentsList = new ArrayList<>();

        fileFragmentsList.addAll(db.getAllFiles());

        for(int i = 0; i < fileFragmentsList.size(); i++){
            String fileOriginName = fileFragmentsList.get(i).getFileOriginName();
            listDataHeader.add(fileOriginName);

            List<String> childList = new ArrayList<>();
            List<String> URIList = new ArrayList<>();

            if(!fileFragmentsList.get(i).getFileFragmentNameOne().equals("null")){
                childList.add(fileFragmentsList.get(i).getFileFragmentNameOne());
                URIList.add(fileFragmentsList.get(i).getFileFragmentNameOneUri());
            }

            if(!fileFragmentsList.get(i).getFileFragmentNameTwo().equals("null")){
                childList.add(fileFragmentsList.get(i).getFileFragmentNameTwo());
                URIList.add(fileFragmentsList.get(i).getFileFragmentNameTwoUri());
            }

            if(!fileFragmentsList.get(i).getFileFragmentNameThree().equals("null")){
                childList.add(fileFragmentsList.get(i).getFileFragmentNameThree());
                URIList.add(fileFragmentsList.get(i).getFileFragmentNameThreeUri());
            }

            listDataChild.put(fileOriginName, childList);
            listURIChild.put(fileOriginName,URIList);
        }

    }
}
