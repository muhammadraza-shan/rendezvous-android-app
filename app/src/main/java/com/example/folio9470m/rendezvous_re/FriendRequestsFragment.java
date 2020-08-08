package com.example.folio9470m.rendezvous_re;

import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.folio9470m.rendezvous_re.adapters.FriendRequestsAdapter;
import com.example.folio9470m.rendezvous_re.adapters.FriendsListAdapter;
import com.example.folio9470m.rendezvous_re.models.Friend;
import com.example.folio9470m.rendezvous_re.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class FriendRequestsFragment extends Fragment {
    private static final String TAG = "FriendRequestsFragment";

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private String userID;
    private FriendRequestsAdapter adapter;
    private ArrayList<String> requestIDs = new ArrayList();
    private ArrayList<Friend> requestsList = new ArrayList();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friend_requests,container,false);

        userID = FirebaseAuth.getInstance().getUid();
        recyclerView = view.findViewById(R.id.requests_recycler_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new FriendRequestsAdapter(getActivity(), requestsList);
        FirebaseDatabase.getInstance().getReference()
                .child("users").child(userID).child("requests")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        requestIDs.clear();
                        Log.d(TAG, "onDataChange: Getting requests ID");
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            requestIDs.add(ds.getKey());
                        }
                        getrequestsDetails();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });

        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT | ItemTouchHelper.LEFT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                DatabaseReference usersNode = FirebaseDatabase.getInstance().getReference().child("users");
                if( direction == ItemTouchHelper.LEFT){ // Add Friend Swipe Left
                    //Add friend's ID to friend list and remove ID from friend requests
                    usersNode.child(userID).child("friends").child(requestsList.get(position).getId()).setValue(true);
                    usersNode.child(userID).child("requests").child(requestsList.get(position).getId()).removeValue();

                    //Add your own ID to requester's friend list
                    usersNode.child(requestsList.get(position).getId()).child("friends").child(userID).setValue(true);
                    requestsList.remove(position);
                }
                else if(direction == ItemTouchHelper.RIGHT){
                    usersNode.child(userID).child("requests").child(requestsList.get(position).getId()).removeValue();
                    requestsList.remove(position);
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(getActivity(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightActionIcon(R.drawable.ic_delete_white_24dp)
                        .addSwipeRightBackgroundColor(Color.RED)
                        .addSwipeRightLabel("Delete")
                        .addSwipeLeftActionIcon(R.drawable.ic_add_white_24dp)
                        .addSwipeLeftBackgroundColor(Color.GREEN)
                        .addSwipeLeftLabel("Add")
                        .create()
                        .decorate();
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(callback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private void getrequestsDetails() {
        FirebaseDatabase.getInstance().getReference()
                .child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestsList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){

                    Log.d(TAG, "onDataChange: Getting friends Details");
                    User currentNode = ds.getValue(User.class);
                    for(String requestID : requestIDs){
                        if(requestID.equals(currentNode.getUserID())){
                            Friend friend = new Friend(currentNode.getName(), currentNode.getUserID(), false);
                            requestsList.add(friend);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
