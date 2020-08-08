package com.example.folio9470m.rendezvous_re;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.example.folio9470m.rendezvous_re.util.TextInputAutoCompleteTextView;
import com.example.folio9470m.rendezvous_re.models.Invitation;
import com.example.folio9470m.rendezvous_re.models.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


public class InviteFragment extends Fragment {
    private static final String TAG = "InviteFragment";

    private String meetupID;
    private String nameOfUser;

    private TextInputAutoCompleteTextView inputText;
    private Button inviteButton;
    private Button closeButton;
    private ListView invitedListView;
    private ArrayAdapter<String> adapter;

    private ArrayList<User> listOFriends = new ArrayList();
    private ArrayList<String> invitedUsersList = new ArrayList();
    private DatabaseReference userDatabase;
    private FirebaseAuth mAuth;
    private ArrayList<String> friendsIDs = new ArrayList();
    private ArrayList<String> friendsNames = new ArrayList();
    private ArrayAdapter<String> arrayAdapter;;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite,container,false);

        mAuth = FirebaseAuth.getInstance();

        meetupID = ((Rendezvous)getActivity().getApplicationContext()).getMeetupID();
        nameOfUser = ((Rendezvous)getActivity().getApplicationContext()).getUserName();
        inputText = view.findViewById(R.id.invite_text);
        inviteButton = view.findViewById(R.id.invite_people_button);
        invitedListView = view.findViewById(R.id.listViewInvite);
        closeButton = view.findViewById(R.id.close_button);
        arrayAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, friendsNames);
        inputText.setAdapter(arrayAdapter);

        userDatabase = FirebaseDatabase.getInstance().getReference().child("users");
        getFriendsList();
        populateAllUsersArrayList();

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
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }


    private void inviteButtonListener() {
        inviteButton.setOnClickListener(v -> {
            if(inputText.getText() == null || inputText.getText().toString().equals("") ){
                inputText.setError("Field must be filled");
            }
            else{
                String nameOfInvitedUser = inputText.getText().toString();
                boolean userFound = false;
                for(int i = 0 ; i < listOFriends.size() ; i++){
                    User current = listOFriends.get(i);
                    if(current.getName().equals(nameOfInvitedUser) && !invitedUsersList.contains(nameOfInvitedUser)){
                        userFound = true;
                        userDatabase.child(listOFriends.get(i).getUserID()).child("invitedMeetupList")
                                .child(mAuth.getUid()+meetupID).setValue(new Invitation(meetupID,nameOfUser));
                        invitedUsersList.add(nameOfInvitedUser);
                        adapter = new ArrayAdapter<String>(getActivity(),
                                android.R.layout.simple_list_item_1,
                                invitedUsersList);
                        invitedListView.setAdapter(adapter);
                        break;
                    }
                }
                if(userFound){
                    DynamicToast.makeSuccess(getActivity(), "Friend Successfully Invited").show();
                }
                else{
                    DynamicToast.makeError(getActivity(), "Friend Not Found or Already Invited").show();
                }
                inputText.setText("");
            }
        });
    }

    private void populateAllUsersArrayList() {
        userDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot userSnapshot : dataSnapshot.getChildren()){
                    if(!userSnapshot.getKey().equals(mAuth.getUid()) && friendsIDs.contains(userSnapshot.getKey())){
                        listOFriends.add(userSnapshot.getValue(User.class));
                        friendsNames.add(userSnapshot.getValue(User.class).getName());
                    }
                }
                arrayAdapter.notifyDataSetChanged();
                inviteButtonListener();
                closeButton.setOnClickListener(v -> {
                    Intent intent = new Intent(getActivity(), MeetupOverviewNav.class);
                    intent.putExtra("id",meetupID);
                    startActivity(intent);
                    getActivity().finish();
                });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }
}
