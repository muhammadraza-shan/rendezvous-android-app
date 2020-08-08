package com.example.folio9470m.rendezvous_re;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.folio9470m.rendezvous_re.adapters.FriendsListAdapter;
import com.example.folio9470m.rendezvous_re.models.Friend;
import com.example.folio9470m.rendezvous_re.models.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class FriendsFragment extends Fragment {
    private static final String TAG = "FriendsFragment";

    private LinearLayoutManager linearLayoutManager;
    private RecyclerView recyclerView;
    private String userID;
    private ArrayList<String> friendsIDs = new ArrayList();
    private ArrayList<Friend> friendsList = new ArrayList();
    private FriendsListAdapter adapter;
    private FloatingActionButton addFriendButton;
    private ArrayList<User> allUsersList = new ArrayList();
    private ProgressBar progressBar;
    private ArrayAdapter<String> arrayAdapter;
    private ArrayList<String> namesOfUsers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_friends,container,false);
        addFriendButton = view.findViewById(R.id.addFriendButton);

        progressBar = view.findViewById(R.id.indeterminateBar);
        userID = FirebaseAuth.getInstance().getUid();
        recyclerView = view.findViewById(R.id.friend_recycler_view);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new FriendsListAdapter(getActivity(), friendsList);

        FirebaseDatabase.getInstance().getReference()
                .child("users").child(userID).child("friends")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        friendsIDs.clear();
                        Log.d(TAG, "onDataChange: Getting Friends ID");
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            friendsIDs.add(ds.getKey());
                        }
                        getFriendsDetails();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });


        // Add Friend Dialog Code
        LayoutInflater linf = LayoutInflater.from(getActivity());
        final View inflator = linf.inflate(R.layout.dialog_add_friend, null);
        AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());
        alert.setView(inflator);
        final AutoCompleteTextView et1 =  inflator.findViewById(R.id.add_friend_id);

        arrayAdapter = new ArrayAdapter<String>(getActivity()
                , android.R.layout.simple_list_item_1, namesOfUsers);
        et1.setAdapter(arrayAdapter );

        alert.setPositiveButton("Add Friend", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(User user : allUsersList){
                    if(et1.getText().toString().equals(user.getName()) && !friendsIDs.contains(user.getUserID())){
                        FirebaseDatabase.getInstance().getReference()
                                .child("users").child(user.getUserID()).child("requests")
                                .child(userID).setValue(true);
                        Toast.makeText(getActivity(), "Request sent", Toast.LENGTH_SHORT).show();
                        break;
                    }
                }

            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        AlertDialog alertDialog =  alert.create();
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                Button posButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                Button negButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                View parent =(View)posButton.getParent();
                parent.setPadding(0,0,0,0);

                LinearLayout.LayoutParams posParams = (LinearLayout.LayoutParams) posButton.getLayoutParams();
                posParams.width = parent.getWidth()/2;
                posParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;

                LinearLayout.LayoutParams negParams = (LinearLayout.LayoutParams) negButton.getLayoutParams();
                negParams.width = parent.getWidth()/2;
                negParams.height = LinearLayout.LayoutParams.WRAP_CONTENT;

            }
        });
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.show();
            }
        });
        ItemTouchHelper.SimpleCallback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }



            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                if(direction == ItemTouchHelper.RIGHT){
                    int position = viewHolder.getAdapterPosition();
                    DatabaseReference myNode = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

                    myNode.child("friends").child(friendsList.get(position).getId()).removeValue();
                    FirebaseDatabase.getInstance().getReference().child("users").child(friendsList.get(position).getId())
                            .child("friends").child(userID).removeValue();
                    friendsList.remove(position);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                new RecyclerViewSwipeDecorator.Builder(getActivity(), c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                        .addSwipeRightActionIcon(R.drawable.ic_delete_white_24dp)
                        .addSwipeRightBackgroundColor(Color.RED)
                        .addSwipeRightLabel("Delete")
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
    private void getFriendsDetails() {
        FirebaseDatabase.getInstance().getReference()
                .child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                allUsersList.clear();
                friendsList.clear();
                namesOfUsers.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()){
                    Log.d(TAG, "onDataChange: Getting friends Details");
                    User currentNode = ds.getValue(User.class);
                    if(!currentNode.getUserID().equals(FirebaseAuth.getInstance().getUid())){
                        allUsersList.add(currentNode);
                        namesOfUsers.add(currentNode.getName());
                    }
                    for(String friendID : friendsIDs){
                        if(friendID.equals(currentNode.getUserID())){
                            Boolean status = ds.child("online").getValue(Boolean.class);
                            if(status == null){
                                status = false;
                            }
                            Friend friend = new Friend(currentNode.getName(),
                                    currentNode.getUserID(),
                                    status);
                            friendsList.add(friend);
                        }
                    }
                }
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
                arrayAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
