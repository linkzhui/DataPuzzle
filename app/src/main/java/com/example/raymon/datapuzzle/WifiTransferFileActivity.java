package com.example.raymon.datapuzzle;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class WifiTransferFileActivity extends AppCompatActivity {

    WifiP2pManager mManager;
    Channel mChannel;
    BroadcastReceiver mReceiver;
    private final IntentFilter mIntentFilter = new IntentFilter();
    Button mWifiPeerDisc;
    Button mWifiPeerCon;
    Button mWifiFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_transfer_file);

        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        //mChannel is used to connect our application to the Wi-Fi P2P framework
        mChannel = mManager.initialize(this,getMainLooper(),null);
        mReceiver = new WiFiDirectBroadcastReceiver(mManager,mChannel,this);

        mWifiPeerDisc = findViewById(R.id.DiscoverPeer);
        mWifiPeerCon = findViewById(R.id.ConnectPeer);
        mWifiFile = findViewById(R.id.TransferFile);

        mWifiPeerDisc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        Toast.makeText(WifiTransferFileActivity.this, "Discover peer successful", Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFailure(int i) {
                        Toast.makeText(WifiTransferFileActivity.this, "Discover peer failed", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

    }

    /* register the broadcast receiver with the intent values to be matched */
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver, mIntentFilter);
    }

    /* unregister the broadcast receiver */
    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);
    }


}
