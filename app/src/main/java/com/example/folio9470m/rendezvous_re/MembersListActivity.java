package com.example.folio9470m.rendezvous_re;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import com.example.folio9470m.rendezvous_re.adapters.MembersListAdapter;
import com.example.folio9470m.rendezvous_re.models.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MembersListActivity extends AppCompatActivity {
    private static final String TAG = MembersListActivity.class.getSimpleName();

    private ListView membersListView;
    private Rendezvous mApp;
    private String meetupID;
    private ArrayList<String> membersList = new ArrayList();
    private ArrayList<String> membersNameList = new ArrayList();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members_list);
        Log.d(TAG, "onCreate: Activity Created");
        setTitle("Members");


        mApp = ((Rendezvous)getApplicationContext());
        meetupID = mApp.getMeetupID();
        Log.d(TAG, "onCreate: Current MeetupID :"+ meetupID);

        membersListView = findViewById(R.id.members_list_view);

        FirebaseDatabase.getInstance().getReference()
                .child("meetups")
                .child(meetupID)
                .child("members")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot member : dataSnapshot.getChildren()){
                            membersList.add(member.getKey());
                        }
                        Log.d(TAG, "onDataChange: Members IDs retrieved");
                        FirebaseDatabase.getInstance().getReference()
                                .child("users")
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                                            User user = userSnapshot.getValue(User.class);
                                            for(String member : membersList){
                                                if(member.equals(userSnapshot.getKey())){
                                                    membersNameList.add(user.getName());
                                                }
                                            }
                                        }
                                        Log.d(TAG, "onDataChange: Members Names retrieved");
                                        MembersListAdapter adapter = new MembersListAdapter(MembersListActivity.this,
                                                R.layout.memberslistitem,
                                                membersNameList);
                                        membersListView.setAdapter(adapter);
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {
                                    }
                                });
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }
}
