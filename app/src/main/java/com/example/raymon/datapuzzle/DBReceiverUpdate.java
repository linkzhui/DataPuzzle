package com.example.raymon.datapuzzle;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DBReceiverUpdate {
    private static DatabaseReference mDatabase;
    private static String TAG = "DBReceiverUpdate";
    public static void update(final String receiver, final String originalFileName, final String fragmentName){
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("users").child(UserModeActivity.username).child("files").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(originalFileName))
                {
                    for(DataSnapshot child:dataSnapshot.child(originalFileName).child("fragments").getChildren())
                    {
                        if(child.child("fragName").getValue(String.class).equals(fragmentName))
                        {
                            Log.i(TAG,"update the "+fragmentName +"'s"+" receiver: "+receiver);
                            String key = child.getKey();
                            mDatabase.child("users").child(UserModeActivity.username).child("files").child(originalFileName).child("fragments").child(key).child("receiver").setValue(receiver);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e(TAG,databaseError.getMessage());
            }
        });
    }
}
