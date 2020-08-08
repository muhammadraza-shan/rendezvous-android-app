package com.example.folio9470m.rendezvous_re;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;

import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.AlarmClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.folio9470m.rendezvous_re.models.Invitation;
import com.example.folio9470m.rendezvous_re.models.Meetup;
import com.example.folio9470m.rendezvous_re.models.User;
import com.example.folio9470m.rendezvous_re.services.LocationService;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.readystatesoftware.viewbadger.BadgeView;

import com.google.android.libraries.places.api.Places;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    public static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 9002;
    public static final int PERMISSIONS_REQUEST_ENABLE_GPS = 9003;
    public static final int ERROR_DIALOG_REQUEST = 9001;

    private FirebaseAuth mAuth;
    private boolean mLocationPermissionGranted = false;
    private Button signOutButton;
    private Button inviteButton;
    private BadgeView badge;
    private ArrayList<String> meetups ;
    private DatabaseReference onlineStatusRef;
    private boolean useLocation = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        getActionBar().show();
        FirebaseApp.initializeApp(this);
        Places.initialize(getApplicationContext(), "AIzaSyBrdbrXBF4MKt5vq8zAIkTFjx8VeHhWMns");
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        mAuth = FirebaseAuth.getInstance();


        BottomNavigationView bottomNavigationView = (BottomNavigationView) findViewById(R.id.bottomNavigationView);
        Menu menu = bottomNavigationView.getMenu();
        MenuItem menuItem = menu.getItem(0);
        menuItem.setChecked(true);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch(menuItem.getItemId()){
                    case R.id.nav_home:

                        break;
                    case R.id.nav_friends:
                        startActivity(new Intent(MainActivity.this, FriendsActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_meetups:
                        startActivity(new Intent(MainActivity.this, CurrentMeetupsRecycler.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                    case R.id.nav_search:
                        startActivity(new Intent(MainActivity.this, SearchActivity.class)
                                .addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT));
                        break;
                }
                return false;
            }
        });

        Button createButton = findViewById(R.id.button3);
        signOutButton = findViewById(R.id.sign_out_button);
        inviteButton = findViewById(R.id.invitation_button);


        badge = new BadgeView(this, inviteButton);
        badge.setText("0");
        badge.setTextSize(12);
        badge.setBadgePosition(BadgeView.POSITION_TOP_RIGHT);
        badge.setBadgeMargin(0, 0);
        ViewCompat.setTranslationZ(badge, 10);
        //badge.show();

        createButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CreateActivity.class)));
        inviteButton.setOnClickListener(view -> startActivity(new Intent(MainActivity.this, CurrMeetupsActivity.class)));
        signOutButton.setOnClickListener(v -> {
            if(signOutButton.getText().toString().equals("Sign Out")){
                if( LoginManager.getInstance()!=null){
                    LoginManager.getInstance().logOut();
                }
                onlineStatusRef.setValue(false);
                mAuth.signOut();
                Toast.makeText(MainActivity.this, "Signed Out",
                        Toast.LENGTH_SHORT).show();
                signOutButton.setText("Sign In");
                Intent intent = new Intent(getApplicationContext(), LoginChooserActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("EXIT", true);
                startActivity(intent);
            }
            else{
                signOutButton.setText("Sign Out");
                startActivity(new Intent(MainActivity.this, LoginChooserActivity.class));
                finish();
            }
        });

    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(MainActivity.this, LoginChooserActivity.class));
            finish();
        }
        else{
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            ((Rendezvous)(getApplicationContext())).setUserName(dataSnapshot.getValue(User.class).getName());
                            ((TextView)findViewById(R.id.signedinastext)).setText("Signed in as: "+((Rendezvous)getApplicationContext()).getUserName());
                            meetups = new ArrayList<>();
                            for(DataSnapshot ds : dataSnapshot.child("meetupArrayList").getChildren()){
                                meetups.add(ds.getKey());
                            }
                            ((Rendezvous)(getApplicationContext())).setCurrentMeetups(meetups);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });

        }

    }
    @Override
    protected void onResume() {
        super.onResume();

        if(checkMapServices() ){
            if(mLocationPermissionGranted){
                startLocationService();
            }
            else{
                getLocationPermission();
            }
        }
        if(mAuth.getCurrentUser()==null){
            signOutButton.setText("Sign In");
            startActivity(new Intent(MainActivity.this,LoginChooserActivity.class));
        }
        else{
            signOutButton.setText("Sign Out");
            FirebaseUser currentUser = mAuth.getCurrentUser();
            List<String> list = currentUser.getProviders();
            ((Rendezvous)(getApplicationContext())).setUserID(mAuth.getUid());
            FirebaseDatabase.getInstance().getReference().child("users").child(mAuth.getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            meetups = new ArrayList<>();
                            HashMap<String, Invitation> invites ;
                            invites = (HashMap)dataSnapshot.child("invitedMeetupList").getValue();
                            if(invites!=null){
                                badge.setText(String.valueOf(invites.size()));
                                badge.show();
                            }
                            else{
                                badge.setText("0");
                                badge.show();
                            }
                            for(DataSnapshot ds : dataSnapshot.child("meetupArrayList").getChildren()){
                                meetups.add(ds.getKey());
                            }
                            ((Rendezvous)(getApplicationContext())).setCurrentMeetups(meetups);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                        }
                    });
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("meetups");
            mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot meetupSnapshot : dataSnapshot.getChildren()){
                        Meetup check = meetupSnapshot.getValue(Meetup.class);
                        if(check.getLocationMethod() == 0 && check.getCoordinates() == null){
                            mDatabase.child(meetupSnapshot.getKey()).removeValue();
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                }
            });
        }
        if(mAuth!= null && mAuth.getCurrentUser()!= null){
            onlineStatusRef = FirebaseDatabase.getInstance().getReference().child("users")
                    .child(mAuth.getUid()).child("online");
            onlineStatusRef.setValue(true);
            onlineStatusRef.onDisconnect().setValue(false);
        }
    }


    private void startLocationService(){
        if(!isLocationServiceRunning()){
            Intent serviceIntent = new Intent(this, LocationService.class);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O){

                MainActivity.this.startForegroundService(serviceIntent);
            }else{
                startService(serviceIntent);
            }
        }
    }

    private boolean isLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)){
            if("com.example.folio9470m.rendezvous_re.services.LocationService".equals(service.service.getClassName())) {
                Log.d("Main", "isLocationServiceRunning: location service is already running.");
                return true;
            }
        }
        Log.d("Main", "isLocationServiceRunning: location service is not running.");
        return false;
    }

    private boolean checkMapServices(){
        if(isServicesOK()){
            if(isMapsEnabled()){
                return true;
            }
        }
        return false;
    }

    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("This application requires GPS to work properly, do you want to enable it?")
                .setCancelable(false)
                .setPositiveButton("Yes", (dialog, id) -> {
                    useLocation = true;
                    Intent enableGpsIntent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivityForResult(enableGpsIntent, PERMISSIONS_REQUEST_ENABLE_GPS);
                }).setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                useLocation = false;
            }
        });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public boolean isMapsEnabled(){
        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );

        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            if(useLocation){
                buildAlertMessageNoGps();
                return false;
            }
        }
        return true;
    }

    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
            startLocationService();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    public boolean isServicesOK(){
        Log.d(TAG, "isServicesOK: checking google services version");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);

        if(available == ConnectionResult.SUCCESS){
            //everything is fine and the user can make map requests
            Log.d(TAG, "isServicesOK: Google Play Services is working");
            return true;
        }
        else if(GoogleApiAvailability.getInstance().isUserResolvableError(available)){
            //an error occured but we can resolve it
            Log.d(TAG, "isServicesOK: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this, available, ERROR_DIALOG_REQUEST);
            dialog.show();
        }else{
            Toast.makeText(this, "You can't make map requests", Toast.LENGTH_SHORT).show();
        }
        return false;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: called.");
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ENABLE_GPS: {
                if(mLocationPermissionGranted){
                }
                else{
                    getLocationPermission();
                }
            }
        }

    }









}
