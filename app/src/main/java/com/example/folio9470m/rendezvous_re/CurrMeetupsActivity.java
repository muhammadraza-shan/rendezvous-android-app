package com.example.folio9470m.rendezvous_re;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.folio9470m.rendezvous_re.adapters.MeetupListAdapter;
import com.example.folio9470m.rendezvous_re.models.Invitation;
import com.example.folio9470m.rendezvous_re.models.InvitedMeetup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class CurrMeetupsActivity extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private DatabaseReference userDatabase;
    private FirebaseAuth mAuth;
    private String userID;
    private HashMap<String, Invitation> hashMap = new HashMap();
    private MeetupListAdapter adapter;
    private ProgressBar progressBar;
    private ArrayList<InvitedMeetup> meetupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_curr_meetups);
        setTitle("Meetup Invitations");
        progressBar = findViewById(R.id.indeterminateBar);



        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference().child("meetups");
        userDatabase = database.getReference().child("users");
        mAuth = FirebaseAuth.getInstance();
        userID = mAuth.getUid();

        ListView mList = findViewById(R.id.listView);
        adapter = new MeetupListAdapter(CurrMeetupsActivity.this, R.layout.adapter_view_layout,meetupList);
        mList.setAdapter(adapter);
        adapter.setOnYesClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = Integer.parseInt( v.getTag().toString());
                String meetupID = meetupList.get(position).getMeetupID();
                for (Map.Entry<String, Invitation> entry : hashMap.entrySet()) {
                    if(entry.getValue().getMeetupID().equals(meetupID)){
                        userDatabase.child(userID)
                                .child("invitedMeetupList").child(entry.getKey()).removeValue();
                        userDatabase.child(userID)
                                .child("meetupArrayList").child(meetupID).setValue(true);
                        mDatabase.child(meetupID)
                                .child("members").child(userID).setValue(true);
                        if(meetupList.get(position).getLocationMethod()==1){
                            FirebaseDatabase.getInstance().getReference()
                                    .child("voting").child(meetupID)
                                    .child("peopleNotVoted")
                                    .child(userID).setValue(true);
                        }
                        meetupList.remove(position);
                        break;
                    }
                }


                adapter.notifyDataSetChanged();
            }

        });
        adapter.setOnNoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = Integer.parseInt( v.getTag().toString());
                String meetupID = meetupList.get(position).getMeetupID();
                for (Map.Entry<String, Invitation> entry : hashMap.entrySet()) {
                    if(entry.getValue().getMeetupID().equals(meetupID)){
                        userDatabase.child(userID)
                                .child("invitedMeetupList").child(entry.getKey()).removeValue();
                        meetupList.remove(position);
                        break;
                    }
                }

                adapter.notifyDataSetChanged();
            }
        });
        adapter.setOnInfoClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = Integer.parseInt( v.getTag().toString());
                String meetupID = meetupList.get(position).getMeetupID();
                Intent intent = new Intent(CurrMeetupsActivity.this, MeetupOverviewNav.class);
                intent.putExtra("id",meetupID);
                intent.putExtra("ACTIVITY_NAME_BUNDLE_ID", "CurrMeetupsActivity");
                startActivity(intent);
            }
        });
        adapter.notifyDataSetChanged();

        userDatabase.child(mAuth.getUid()).child("invitedMeetupList").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<HashMap<String, Invitation>> genericTypeIndicator = new GenericTypeIndicator<HashMap<String, Invitation>>() {};
                hashMap = dataSnapshot.getValue(genericTypeIndicator);
                if(hashMap!= null &&hashMap.size() != 0){
                    getInvitedMeetups();
                }
                else{
                    progressBar.setVisibility(View.GONE);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });



    }

    private void getInvitedMeetups() {

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot meetupSnapshot : dataSnapshot.getChildren()){
                    for (Invitation value : hashMap.values()) {
                        if(((Invitation)value).getMeetupID().equals(meetupSnapshot.getKey())){
                            InvitedMeetup meetupToBeAdded = meetupSnapshot.getValue(InvitedMeetup.class);
                            meetupToBeAdded.setInvitedBy(((Invitation)value).getInviterName());
                            meetupList.add(meetupToBeAdded);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }
}
