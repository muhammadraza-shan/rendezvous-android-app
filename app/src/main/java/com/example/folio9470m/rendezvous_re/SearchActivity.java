package com.example.folio9470m.rendezvous_re;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.Toast;

import com.example.folio9470m.rendezvous_re.adapters.PublicMeetupsAdapter;
import com.example.folio9470m.rendezvous_re.models.Meetup;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SearchActivity extends AppCompatActivity {
    private ListView searchMeetups;
    private PublicMeetupsAdapter adapter;
    private DatabaseReference meetupRef;
    private ArrayList<String> meetupIDs = new ArrayList();
    private ArrayList<Meetup> meetupArrayList = new ArrayList();
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setTitle("Search Meetups");
        progressBar = findViewById(R.id.indeterminateBar);


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(3);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_home:
                        startActivity(new Intent(SearchActivity.this, MainActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_friends:
                        startActivity(new Intent(SearchActivity.this, FriendsActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_meetups:
                        startActivity(new Intent(SearchActivity.this, CurrentMeetupsRecycler.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_search:

                        break;
                }
                return false;
            }
        });




        meetupRef = FirebaseDatabase.getInstance().getReference().child("meetups");
        searchMeetups = findViewById(R.id.searchPublicMeetups);
        adapter = new PublicMeetupsAdapter(SearchActivity.this,
                R.layout.search_item,
                meetupArrayList,
                new PublicMeetupsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Meetup item) {
                        String meetupid = item.getMeetupID();
                        Intent intent = new Intent(SearchActivity.this, MeetupOverviewNav.class);
                        intent.putExtra("id", meetupid);
                        intent.putExtra("ACTIVITY_NAME_BUNDLE_ID", "CurrMeetupsActivity");
                        startActivity(intent);
                    }
                },
                new PublicMeetupsAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(Meetup item) {
                        meetupRef.child(item.getMeetupID()).child("members")
                                .child(FirebaseAuth.getInstance().getUid())
                                .setValue(true);
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(FirebaseAuth.getInstance().getUid())
                                .child("meetupArrayList")
                                .child(item.getMeetupID())
                                .setValue(true);
                        DynamicToast.makeSuccess(SearchActivity.this, "Meetup succesfully joined", Toast.LENGTH_LONG).show();
                        adapter.notifyDataSetChanged();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                String meetupid = item.getMeetupID();
                                Intent intent = new Intent(SearchActivity.this, MeetupOverviewNav.class);
                                intent.putExtra("id", meetupid);
                                startActivity(intent);
                            }
                        }, 2000);
                        finish();
                    }
                }
        );
        searchMeetups.setAdapter(adapter);

        meetupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ArrayList<String> publicMeetupsList = new ArrayList();
                for(DataSnapshot var : dataSnapshot.getChildren()){
                    Meetup current = var.getValue(Meetup.class);
                    if (current.isPublic()){
                        meetupArrayList.add(current);
                        meetupIDs.add(current.getMeetupID());
                    }
                }
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_menu,menu);
        MenuItem item = menu.findItem(R.id.search_meetups);
        SearchView searchView = (SearchView) item.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }
}
