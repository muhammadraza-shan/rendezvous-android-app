package com.example.folio9470m.rendezvous_re;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.folio9470m.rendezvous_re.adapters.CloseByFriendsAdapter;
import com.example.folio9470m.rendezvous_re.models.Invitation;
import com.example.folio9470m.rendezvous_re.models.LatLong;
import com.example.folio9470m.rendezvous_re.models.User;
import com.example.folio9470m.rendezvous_re.models.UserLocation;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class CloseByFriendsFragment extends Fragment {
    private ListView listView;
    private ArrayList<UserLocation> userLocationsList = new ArrayList();
    private FirebaseAuth mAuth;
    private DatabaseReference userDatabase;
    private DatabaseReference userLocationsRef;
    private ArrayList<String> friendsIDs = new ArrayList();
    private CloseByFriendsAdapter adapter;
    private LatLong myLocation;
    private ArrayList<UserLocation> closeByFriendsList = new ArrayList();
    private ArrayList<Double> friendsDistanceList = new ArrayList();
    private String meetupID;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_closeby_friends,container,false);

        meetupID = ((Rendezvous)getActivity().getApplicationContext()).getMeetupID();

        mAuth = FirebaseAuth.getInstance();
        userLocationsRef = FirebaseDatabase.getInstance().getReference().child("userlocations");
        userDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        listView = view.findViewById(R.id.listview_closebyfriends);
        getFriendsList();
        return view;
    }

    private void getFriendsList() {
        userDatabase.child(mAuth.getUid()).child("friends")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            friendsIDs.add(ds.getKey());
                        }
                        getFriendsLocationsFromDatabase();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                }

                );
    }

    private void getFriendsLocationsFromDatabase() {
        userLocationsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    if(friendsIDs.contains(ds.getKey())){
                        userLocationsList.add(ds.getValue(UserLocation.class));
                    }
                    if(ds.getKey().equals(mAuth.getUid())){
                        myLocation = ds.getValue(UserLocation.class).getLocation();
                    }
                }
                calculateCloseByFriends();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void calculateCloseByFriends() {
        for(UserLocation location : userLocationsList){
            double distance = distance(myLocation.getLatitude(),location.getLocation().getLatitude(),
                    myLocation.getLongitude(),location.getLocation().getLongitude(),
                    0,0);
            if(distance<0){
                distance = distance * (-1);
            }
            if(distance< 30000){
                closeByFriendsList.add(location);
                friendsDistanceList.add(distance);
            }
        }
        setUpListView();
    }

    private void setUpListView() {

        adapter = new CloseByFriendsAdapter(getActivity(),R.layout.closebyfriends_item,closeByFriendsList, friendsDistanceList);
        adapter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = Integer.parseInt( v.getTag().toString());
                userDatabase.child(closeByFriendsList.get(position).getUserID()).child("invitedMeetupList")
                        .child(mAuth.getUid()+meetupID)
                        .setValue(new Invitation(meetupID,
                                ((Rendezvous)getActivity().getApplicationContext()).getUserName()));
                closeByFriendsList.remove(position);
                friendsDistanceList.remove(position);
                adapter.notifyDataSetChanged();
            }
        });
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public static double distance(double lat1, double lat2, double lon1,
                                  double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}
