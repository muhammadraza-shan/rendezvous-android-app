package com.example.folio9470m.rendezvous_re;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.folio9470m.rendezvous_re.adapters.FacebookFriendAdapter;
import com.example.folio9470m.rendezvous_re.models.FacebookFriend;
import com.example.folio9470m.rendezvous_re.models.User;
import com.example.folio9470m.rendezvous_re.util.GlideApp;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class FacebookFriendsFragment extends Fragment {
    private static final String TAG = "FacebookFriendsFragment";
    private LoginButton facebookLoginButton;
    private CallbackManager mCallbackManager;
    private AccessToken accessToken;
    private ArrayList<FacebookFriend> friendsList = new ArrayList();
    private RecyclerView recyclerView;
    private FacebookFriendAdapter adapter;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<User> allUsersList = new ArrayList();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_facebook_friends,container,false);


        mCallbackManager = CallbackManager.Factory.create();
        facebookLoginButton = view.findViewById(R.id.facebookLoginFriends);
        recyclerView = view.findViewById(R.id.facebookRecyclerView);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken!=null){
            facebookLoginButton.setVisibility(View.GONE);
			getFriendsFromFacebook();
        }
		
        



        facebookLoginButton.setReadPermissions("public_profile", "user_friends");
        facebookLoginButton.setFragment(this);
        facebookLoginButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                accessToken = loginResult.getAccessToken();
                facebookLoginButton.setVisibility(View.GONE);
				getFriendsFromFacebook();
            }
            @Override
            public void onCancel() {
                Log.d(TAG, "onCancel: ");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "onError: Facebook Login ",error);
            }
        });

        FirebaseDatabase.getInstance().getReference().child("users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(DataSnapshot ds : dataSnapshot.getChildren()){
                            if(ds.getValue(User.class).getFacebookid() != null){
                                allUsersList.add(ds.getValue(User.class));
                            }
                        }
                        adapter = new FacebookFriendAdapter(getActivity(), friendsList, allUsersList);
                        recyclerView.setAdapter(adapter);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });



        return view;
    }

    private void getFriendsFromFacebook() {
        GraphRequest request = GraphRequest.newMeRequest(
                accessToken,
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        friendsList.clear();
                        Log.d(TAG, "onCompleted: "+response.toString());
                        try {
                            JSONArray jsonArray = object.getJSONObject("friends").getJSONArray("data");
                            for(int i =0 ; i< jsonArray.length() ; i++){
                                String name = jsonArray.getJSONObject(0).getString("name");
                                String id = jsonArray.getJSONObject(0).getString("id");
                                friendsList.add(new FacebookFriend(name, id));
                            }
                            adapter.notifyDataSetChanged();

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        Log.d(TAG, "onCompleted: "+object.toString());
                    }
                });
        Bundle parameters = new Bundle();
        parameters.putString("fields", "friends");
        request.setParameters(parameters);
        request.executeAsync();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
