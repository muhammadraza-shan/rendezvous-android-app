package com.example.folio9470m.rendezvous_re;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.example.folio9470m.rendezvous_re.adapters.ViewHolder;
import com.example.folio9470m.rendezvous_re.models.LocationVote;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Query;
import com.google.firebase.database.Transaction;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class VotingActivity extends AppCompatActivity {

    private static final String TAG = VotingActivity.class.getSimpleName();
    private Rendezvous mApp;
    private String meetupID;
    private PlacesClient placesClient;
    private FirebaseRecyclerAdapter<LocationVote, ViewHolder> adapter;
    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;

    private FloatingActionButton addVoteButton;
    private Boolean hasVoted = true;
    private String userVoteKey;
    private ArrayList<String> recordOfPlaceIDs = new ArrayList<>();

    private int expandedPosition = -1;
    private int mapExpandedPosition = -1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voting);
        setTitle("Votes");

        Log.d(TAG, "onCreate: Activity Started");
        mApp = ((Rendezvous) getApplicationContext());
        meetupID = mApp.getMeetupID();
        placesClient = Places.createClient(this);


       // mListView = findViewById(R.id.voting_list_view);
        addVoteButton = findViewById(R.id.addVoteButton);

        recyclerView = findViewById(R.id.vote_recycler_view);

        linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        /*FirebaseDatabase.getInstance().getReference()
                .child("voting").child(meetupID).child("votes")
        .addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    String placeID = ds.getValue(LocationVote.class).getPlaceID();
                    if(placeID!=null && !placeID.equals("customplace")){
                        recordOfPlaceIDs.add(placeID);
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });*/

        FirebaseDatabase.getInstance().getReference()
                .child("voting").child(meetupID).child("votes")
        .addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                LocationVote newVote = dataSnapshot.getValue(LocationVote.class);
                boolean found = false;
                for(String placeID : recordOfPlaceIDs){
                    if(placeID.equals(newVote.getPlaceID()) && !newVote.equals("customplace")){
                        FirebaseDatabase.getInstance().getReference()
                                .child("voting").child(meetupID).child("votes").child(newVote.getKey()).removeValue();
                        found = true;
                        break;
                    }
                }
                if(!found&& !newVote.getPlaceID().equals("customplace")){
                    recordOfPlaceIDs.add(newVote.getPlaceID());
                }


            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        checkIfVoted();

        Query query = FirebaseDatabase.getInstance()
                .getReference()
                .child("voting")
                .child(meetupID)
                .child("votes");

        FirebaseRecyclerOptions<LocationVote> options =new FirebaseRecyclerOptions.Builder<LocationVote>()
                .setQuery(query, LocationVote.class)
                .build();


        adapter = new FirebaseRecyclerAdapter<LocationVote, ViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int i, @NonNull LocationVote locationVote) {
                Log.d(TAG, "onBindViewHolder: Populating recycler view. Location Name:  "+locationVote.getPlaceName());
                String address= locationVote.getAddress();
                viewHolder.setPlaceName(locationVote.getPlaceName()+" "+address);
                viewHolder.setVoteCount(String.valueOf(locationVote.getVoteCount()));
                viewHolder.setImageView(locationVote.getPlaceID(),placesClient);
                viewHolder.setCoordinates(locationVote.getCoordinates());
                viewHolder.setMapLocation();
                if (i == expandedPosition) {
                    viewHolder.llExpandArea.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.llExpandArea.setVisibility(View.GONE);
                }
                if (i == mapExpandedPosition) {
                    viewHolder.mapView.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.mapView.setVisibility(View.GONE);
                }
                viewHolder.setOnClickListener(new ViewHolder.ClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        if(expandedPosition == position){
                            int prev = expandedPosition;
                            expandedPosition = -1;
                            mapExpandedPosition = -1;
                            notifyItemChanged(prev);
                        }
                        else{
                            if (expandedPosition >= 0) {
                                int prev = expandedPosition;
                                mapExpandedPosition = -1;
                                notifyItemChanged(prev);
                            }
                            // Set the current position to "expanded"
                            expandedPosition = position;
                            notifyItemChanged(expandedPosition);
                        }
                    }

                    @Override
                    public void onItemLongClick(View view, int position) {

                    }

                    @Override
                    public void onMapClick(View view, int position) {
                        if (mapExpandedPosition >= 0) {
                            int prev = mapExpandedPosition;
                            mapExpandedPosition = -1;
                            notifyItemChanged(prev);
                        }
                        // Set the current position to "expanded"
                        mapExpandedPosition = position;
                        notifyItemChanged(mapExpandedPosition);

                    }

                    @Override
                    public void onVoteClick(View view, int position) {
                        if(!hasVoted){
                            FirebaseDatabase.getInstance().getReference()
                                    .child("voting")
                                    .child(meetupID)
                                    .child("votes")
                                    .child(locationVote.getKey())
                                    .child("voteCount")
                                    .runTransaction(new Transaction.Handler() {
                                        @NonNull
                                        @Override
                                        public Transaction.Result doTransaction(@NonNull MutableData mutableData) {
                                            Long voteCount = mutableData.getValue(Long.class);
                                            if (voteCount == null) {
                                                return Transaction.success(mutableData);
                                            }
                                            mutableData.setValue(voteCount+1);
                                            return Transaction.success(mutableData);
                                        }

                                        @Override
                                        public void onComplete(@Nullable DatabaseError databaseError, boolean b, @Nullable DataSnapshot dataSnapshot) {
                                            Log.d(TAG, "onComplete: Transaction: "+databaseError);
                                        }
                                    });
                            FirebaseDatabase.getInstance().getReference()
                                    .child("voting")
                                    .child(meetupID)
                                    .child("peopleNotVoted")
                                    .child(mApp.getUserID())
                                    .removeValue();
                            hasVoted = true;
                        }
                        else{
                            DynamicToast.makeWarning(VotingActivity.this, "You cannot vote more than once").show();
                        }

                    }
                });

            }

            @NonNull
            @Override
            public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                Log.d(TAG, "onCreateViewHolder: called");
                View view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.voting_list_item, parent, false);
                return new ViewHolder(view);
            }


        };
        recyclerView.setAdapter(adapter);
        recyclerView.setClickable(true);
        recyclerView.setRecyclerListener(mRecycleListener);


        addVoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(VotingActivity.this,PlacesActivity.class);
                intent.putExtra("ACTIVITY_NAME_BUNDLE_ID", "VotingActivity");
                startActivity(intent);
            }
        });

        DatabaseReference voteRef = FirebaseDatabase.getInstance().getReference().child("voting")
                .child(meetupID);
        voteRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    if(dataSnapshot.child("peopleNotVoted").getChildrenCount() == 0){
                        Log.d(TAG, "onDataChange: peopleVotedList Empty. Deleting Meetup Vote");
                        ArrayList<LocationVote> votes = new ArrayList();
                        for(DataSnapshot votesSnapshot : dataSnapshot.child("votes").getChildren()) {
                            votes.add(votesSnapshot.getValue(LocationVote.class));
                        }
                        Collections.sort(votes, new Comparator<LocationVote>() {
                            @Override
                            public int compare(LocationVote o1, LocationVote o2) {
                                return Long.compare(o1.getVoteCount(),o2.getVoteCount());
                            }
                        });
                        Collections.reverse(votes);
                        DatabaseReference meetupRef = FirebaseDatabase.getInstance().getReference()
                                .child("meetups").child(meetupID);
                        meetupRef.child("coordinates").setValue(votes.get(0).getCoordinates());
                        meetupRef.child("placeID").setValue(votes.get(0).getPlaceID());
                        meetupRef.child("location").setValue(votes.get(0).getPlaceName());
                        meetupRef.child("locationMethod").setValue(0);
                        meetupRef.child("address").setValue(votes.get(0).getAddress());
                        voteRef.setValue(null);

                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void checkIfVoted() {
        FirebaseDatabase.getInstance().getReference()
                .child("voting")
                .child(meetupID)
                .child("peopleNotVoted")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                            if(snapshot.getKey().equals(mApp.getUserID())){
                                hasVoted = false;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        adapter.startListening();
    }
    @Override
    protected void onStop() {
        super.onStop();
        adapter.stopListening();
    }

    private RecyclerView.RecyclerListener mRecycleListener = new RecyclerView.RecyclerListener() {

        @Override
        public void onViewRecycled(RecyclerView.ViewHolder holder) {
            ViewHolder mapHolder = (ViewHolder) holder;

            if (mapHolder != null && mapHolder.map != null) {
                // Clear the map and free up resources by changing the map type to none.
                // Also reset the map when it gets reattached to layout, so the previous map would
                // not be displayed.
                if(mapHolder.getAdapterPosition()!=expandedPosition){
                    mapHolder.map.clear();
                    mapHolder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
                }

            }
        }
    };



}
