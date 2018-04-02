package com.example.raymon.datapuzzle;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

//remaining task in this fragment:
//1. use intent to start the file split, merge and upload activity  (upload mode)
//2. connect the database to display the list of files, that are available for users to download  (download mode)

public class IndividualModeFragment extends Fragment {

    private Button mbuttonUpload;
    private Button mbuttonDownload;
    private EditText mpasswordText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {

        //let the inflater to inflate the fragment's layout
        View fragmentView = inflater.inflate(R.layout.fragment_individual_mode, container, false);


        mpasswordText = fragmentView.findViewById(R.id.passwordText);
        mbuttonDownload = fragmentView.findViewById(R.id.buttonDecMerge);
        mbuttonUpload = fragmentView.findViewById(R.id.buttonUpload);

        //set onclick listener on upload button
        mbuttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mpasswordText.getText().length()==0)
                {
                    Toast.makeText(getContext(), "Please input the password", Toast.LENGTH_SHORT).show();
                }
                else{
                    //start activity
                }
            }
        });

        //set onclick listener on download button
        mbuttonDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mpasswordText.getText().length()==0)
                {
                    Toast.makeText(getContext(), "Please input the password", Toast.LENGTH_SHORT).show();
                }
                else{
                    //start activity


                }
            }
        });

        return fragmentView;
    }
}
