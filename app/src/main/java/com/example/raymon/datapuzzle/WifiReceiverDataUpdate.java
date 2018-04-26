package com.example.raymon.datapuzzle;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WifiReceiverDataUpdate {
    public static void dataUpdate(final String Receiver, final String originalFileName, final String fragmentName){
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        final String username = UserModeActivity.username;
        mDatabase.child("users").child(username).child("files").child(originalFileName).child("fragments").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot child:dataSnapshot.getChildren())
                {
                    if(child.getValue(String.class).equals(fragmentName))
                    {
                        String fragIndex = child.getKey()+"_Receiver";
                        mDatabase.child("users").child(username).child("files").child(originalFileName).child("fragments").child(fragIndex).setValue(Receiver);
                    }

                }
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {

            }

        });
    }
}
