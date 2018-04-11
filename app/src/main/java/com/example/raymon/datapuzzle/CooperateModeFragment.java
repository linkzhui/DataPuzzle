package com.example.raymon.datapuzzle;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;


/**
 * A simple {@link Fragment} subclass.
 */
public class CooperateModeFragment extends Fragment {

    private Button mbuttonSplitEnc;
    private Button mbuttonDecMerge;
    private Button mbuttonWifiDirect;
    private EditText passwordText;
    private String username;
    public CooperateModeFragment() {
        // Required empty public constructor

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View fragment_view = inflater.inflate(R.layout.fragment_cooperate_mode, container, false);
        username = getArguments().getString("username");

        return fragment_view;
    }


}
