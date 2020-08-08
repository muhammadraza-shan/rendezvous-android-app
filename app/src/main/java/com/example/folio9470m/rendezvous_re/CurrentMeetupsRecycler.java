package com.example.folio9470m.rendezvous_re;

import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.folio9470m.rendezvous_re.adapters.MeetupRecyclerAdapter;
import com.example.folio9470m.rendezvous_re.models.Meetup;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class CurrentMeetupsRecycler extends AppCompatActivity {
    private FirebaseDatabase database;
    private DatabaseReference mDatabase;
    private DatabaseReference userDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private ArrayList<String> currentMeetupIds = new ArrayList();
    private PlacesClient placesClient;
    private ArrayList<Meetup> meetupList =new ArrayList<>();
    private RecyclerView recyclerView;
    private MeetupRecyclerAdapter adapter;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_current_meetups_recycler);
        setTitle("Current Meetups");
        database = FirebaseDatabase.getInstance();
        mDatabase = database.getReference().child("meetups");
        userDatabase = database.getReference().child("users");

        progressBar = findViewById(R.id.indeterminateBar);

        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(1);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_home:
                        startActivity(new Intent(CurrentMeetupsRecycler.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_friends:
                        startActivity(new Intent(CurrentMeetupsRecycler.this, FriendsActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_meetups:

                        break;
                    case R.id.nav_search:
                        startActivity(new Intent(CurrentMeetupsRecycler.this, SearchActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                }
                return false;
            }
        });

        Places.initialize(getApplicationContext(), "Google Key Here");
        placesClient = Places.createClient(this);
        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MeetupRecyclerAdapter(CurrentMeetupsRecycler.this, meetupList, item -> {
            Intent intent = new Intent(CurrentMeetupsRecycler.this, MeetupOverviewNav.class);
            intent.putExtra("id",item.getMeetupID());
            startActivity(intent);

        });
        recyclerView.setAdapter(adapter);
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        currentMeetupIds.clear();
                        for(DataSnapshot ds : dataSnapshot.child("meetupArrayList").getChildren()){
                            currentMeetupIds.add(ds.getKey());
                        }
                        populateRecyclerView();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });




    }

    public void populateRecyclerView(){
        mDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                meetupList.clear();
                for(DataSnapshot meetupSnapshot : dataSnapshot.getChildren()){
                    for(int i=0; i < currentMeetupIds.size(); i++ ){
                        if(currentMeetupIds.get(i).equals(meetupSnapshot.getKey())){
                            meetupList.add(meetupSnapshot.getValue(Meetup.class));
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
