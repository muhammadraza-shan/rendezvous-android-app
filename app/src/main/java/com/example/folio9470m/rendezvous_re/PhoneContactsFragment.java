package com.example.folio9470m.rendezvous_re;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.folio9470m.rendezvous_re.adapters.PhoneContactAdapter;
import com.example.folio9470m.rendezvous_re.models.User;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import static android.Manifest.permission.READ_CONTACTS;
import static android.app.Activity.RESULT_OK;

public class PhoneContactsFragment extends Fragment {
    private static final String TAG = "PhoneContactsFragment";

    private static final int CONTACTS_REQUEST_CODE = 1234;
    private static final int REQUEST_INVITE = 5432;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private ArrayList<User> phoneUsersList = new ArrayList<>();
    private PhoneNumberUtil phoneNumberUtil;
    private PhoneContactAdapter adapter;
    private boolean permissionsGranted = false;
    private ArrayList<String> list = new ArrayList<>();
    private ProgressBar progressBar;
    private Button inviteButton;
    private static final String[] PROJECTION = new String[] {
            ContactsContract.CommonDataKinds.Phone.NUMBER
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        phoneNumberUtil = PhoneNumberUtil.getInstance();
        getContactsPermissions();
        if(permissionsGranted){
            getContacts(getActivity());
        }

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_phone_contacts,container,false);

        progressBar = view.findViewById(R.id.progressBar4);
        inviteButton = view.findViewById(R.id.invite_firebasebutton);
        inviteButton.bringToFront();
        if(list.size()>0){
            progressBar.setVisibility(View.GONE);
        }

        recyclerView = view.findViewById(R.id.phone_recyclerview);
        linearLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);
        adapter = new PhoneContactAdapter(getActivity(), phoneUsersList);
        recyclerView.setAdapter(adapter);
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(permissionsGranted){
                    Intent intent = new AppInviteInvitation.IntentBuilder("Invite Friends to Install Rendezvous")
                            .setMessage("You have been invited to install Rendezvous App on your Android phone by "+
                                    ((Rendezvous)(getActivity().getApplicationContext())).getUserName() )
                            .setDeepLink(Uri.parse("https://rendezvousapp.page.link/installapp"))
                            .build();
                    startActivityForResult(intent, REQUEST_INVITE);
                }
                else {
                    DynamicToast.makeWarning(getActivity(), "Permissions Not Granted").show();
                }
            }
        });

        return view;
    }

    private void getContactsPermissions() {
        String[] permissions = {READ_CONTACTS};
        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            permissionsGranted = true;

        }else{
            ActivityCompat.requestPermissions(getActivity(),
                    permissions,
                    CONTACTS_REQUEST_CODE);
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsGranted = false;
        switch(requestCode){
            case CONTACTS_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            permissionsGranted= false;
                            progressBar.setVisibility(View.GONE);
                            inviteButton.setVisibility(View.GONE);
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    permissionsGranted = true;
                    getContacts(getActivity());
                }
            }
        }
    }

    private void getPhoneUsersFromDatabase() {
        if(permissionsGranted){
            FirebaseDatabase.getInstance().getReference()
                    .child("users").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    phoneUsersList.clear();
                    for(DataSnapshot ds : dataSnapshot.getChildren()){
                        User currentNode = ds.getValue(User.class);
                        if(currentNode.getType().equals("phone")){
                            for(String contact : list){
                                PhoneNumberUtil.MatchType matchType =
                                        phoneNumberUtil.isNumberMatch(contact,currentNode.getPhone());
                                if(matchType.equals(PhoneNumberUtil.MatchType.NSN_MATCH)||
                                        matchType.equals(PhoneNumberUtil.MatchType.EXACT_MATCH)){
                                    phoneUsersList.add(currentNode);
                                }
                            }
                        }
                    }
                    adapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

    }

    @SuppressLint("StaticFieldLeak")
    public void getContacts(Context ctx) {

        new AsyncTask<Void, Void, Void>(){
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Void doInBackground(Void... voids) {
                ContentResolver cr = ctx.getContentResolver();
                Cursor cursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, PROJECTION, null, null, null);
                if (cursor != null) {
                    try {
                        final int numberIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                        String phone;
                        while (cursor.moveToNext()) {
                            phone = cursor.getString(numberIndex);
                            boolean match = false;
                            for(String s : list){
                                PhoneNumberUtil.MatchType matchType =
                                        phoneNumberUtil.isNumberMatch(phone,s);
                                if(matchType.equals(PhoneNumberUtil.MatchType.NSN_MATCH)||
                                        matchType.equals(PhoneNumberUtil.MatchType.EXACT_MATCH)){
                                    match = true;
                                    break;
                                }
                            }
                            if(!match)
                                list.add(phone);

                        }

                    } finally {
                        cursor.close();
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                getPhoneUsersFromDatabase();
            }
        }.execute();

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult: requestCode=" + requestCode + ", resultCode=" + resultCode);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // Get the invitation IDs of all sent messages
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                for (String id : ids) {
                    Log.d(TAG, "onActivityResult: sent invitation " + id);
                }
            } else {
            }
        }

    }
}
