package com.example.folio9470m.rendezvous_re;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.folio9470m.rendezvous_re.util.MyClusterManagerRenderer;
import com.example.folio9470m.rendezvous_re.models.ClusterMarker;
import com.example.folio9470m.rendezvous_re.models.LatLong;
import com.example.folio9470m.rendezvous_re.models.PolylineData;
import com.example.folio9470m.rendezvous_re.models.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.DirectionsApiRequest;
import com.google.maps.GeoApiContext;
import com.google.maps.PendingResult;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.internal.PolylineEncoding;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.DirectionsRoute;

import java.util.ArrayList;
import java.util.List;

public class LiveLocation extends FragmentActivity implements OnMapReadyCallback {

    private static final String TAG = "Live Location";
    private static final int LOCATION_UPDATE_INTERVAL = 3000;

    private GoogleMap mGoogleMap;
    private ClusterManager<ClusterMarker> mClusterManager;
    private MyClusterManagerRenderer mClusterManagerRenderer;
    private ArrayList<ClusterMarker> mClusterMarkers = new ArrayList<>();
    private LatLngBounds mMapBoundary;
    private String meetupID;
    private ArrayList<UserLocation> mUserLocations =  new ArrayList();;
    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private LatLong mUserPosition;
    private ArrayList<String> selectedMeetupUserList= new ArrayList();
    private LatLng mMeetupPosition;
    private GeoApiContext mGeoApiContext;
    private ArrayList<PolylineData> mPolyLinesData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_live_location);
        setTitle("Location Tracker");

        Intent intent = getIntent();
        meetupID = intent.getStringExtra("id");

        mGeoApiContext = new GeoApiContext.Builder()
                .apiKey("Key Here")
                .build();

        mUserPosition = new LatLong(33.6843983,73.0478983);
  

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        startUserLocationsRunnable();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;

        getCurrentMeetupPosition();
        mGoogleMap.setOnCameraIdleListener(mClusterManager);
    }

    private void getCurrentMeetupPosition() {
        DatabaseReference meetupLocationRef = FirebaseDatabase.getInstance().getReference()
                .child("meetups")
                .child(meetupID)
                .child("coordinates");
        meetupLocationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                LatLong coord = dataSnapshot.getValue(LatLong.class);
                mMeetupPosition = new LatLng(coord.getLatitude(),coord.getLongitude());
                MarkerOptions meetupMarker = new MarkerOptions()
                        .position(mMeetupPosition)
                        .title("Meetup")
                        .snippet("Location of Meetup")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                mGoogleMap.addMarker(meetupMarker);
                getCurrentMeetupMembers();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getCurrentMeetupMembers() {
        DatabaseReference meetupRef = FirebaseDatabase.getInstance().getReference()
                .child("meetups")
                .child(meetupID)
                .child("members");

        meetupRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot var : dataSnapshot.getChildren()){
                    selectedMeetupUserList.add(var.getKey());
                }
                fillLocationList();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void fillLocationList(){
        DatabaseReference locationRef = FirebaseDatabase.getInstance().getReference()
                .child("userlocations");
        locationRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                for(DataSnapshot var : dataSnapshot.getChildren()){
                    UserLocation current = var.getValue(UserLocation.class);
                    for(String user : selectedMeetupUserList){
                        if(user.equals(var.getKey())){
                            mUserLocations.add(current);
                        }
                    }
                }
                addMapMarkers();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void addMapMarkers(){

        if(mGoogleMap != null){
            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {

                    LinearLayout info = new LinearLayout(LiveLocation.this);
                    info.setOrientation(LinearLayout.VERTICAL);

                    TextView title = new TextView(LiveLocation.this);
                    title.setTextColor(Color.BLACK);
                    title.setGravity(Gravity.CENTER);
                    title.setTypeface(null, Typeface.BOLD);
                    title.setText(marker.getTitle());

                    TextView snippet = new TextView(LiveLocation.this);
                    snippet.setTextColor(Color.GRAY);
                    snippet.setText(marker.getSnippet());

                    info.addView(title);
                    info.addView(snippet);

                    return info;
                }
            });

            if(mClusterManager == null){
                mClusterManager = new ClusterManager<ClusterMarker>(getApplicationContext(), mGoogleMap);
            }
            String name =((Rendezvous)(getApplicationContext())).getUserName();
            if(mClusterManagerRenderer == null){
                mClusterManagerRenderer = new MyClusterManagerRenderer(
                        LiveLocation.this,
                        mGoogleMap,
                        mClusterManager,
                        name
                );
                mClusterManager.setRenderer(mClusterManagerRenderer);
            }

            for(UserLocation userLocation: mUserLocations){

                try{
                    String snippet = "";
                    if(userLocation.getName().equals(name)){
                        snippet = "This is you\n";
                        mUserPosition = userLocation.getLocation();
                    }
                    snippet = snippet+"\nLast Updated: "+DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                            userLocation.getLocationTime()).toString();



                    ClusterMarker newClusterMarker = new ClusterMarker(
                            new LatLng(userLocation.getLocation().getLatitude(),userLocation.getLocation().getLongitude()),
                            userLocation.getName(),
                            snippet,
                            userLocation.getName(),
                            userLocation.getUserID()
                    );
                    mClusterManager.addItem(newClusterMarker);
                    mClusterMarkers.add(newClusterMarker);

                }catch (NullPointerException e){
                    Log.e(TAG, "addMapMarkers: NullPointerException: " + e.getMessage() );
                }

            }
            mClusterManager.cluster();

            setCameraView();
        }
    }



    private void startUserLocationsRunnable(){
        Log.d(TAG, "startUserLocationsRunnable: starting runnable for retrieving updated locations.");
        mHandler.postDelayed(mRunnable = () -> {
            //removeOldPolylines();
            retrieveUserLocations();
            mHandler.postDelayed(mRunnable, LOCATION_UPDATE_INTERVAL);
        }, LOCATION_UPDATE_INTERVAL);
    }

    private void stopLocationUpdates(){
        mHandler.removeCallbacks(mRunnable);
    }

    private void removeOldPolylines(){
        if(mPolyLinesData.size() > 0){
            for(PolylineData polylineData: mPolyLinesData){
                polylineData.getPolyline().remove();
            }
            mPolyLinesData.clear();
            mPolyLinesData = new ArrayList<>();
        }
    }

    private void retrieveUserLocations(){
        Log.d(TAG, "retrieveUserLocations: retrieving location of all users.");

        try{
            for(ClusterMarker clusterMarker: mClusterMarkers){

                DatabaseReference userlocations= FirebaseDatabase.getInstance().getReference()
                        .child("userlocations")
                        .child(clusterMarker.getUserID());
                userlocations.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        final UserLocation updatedUserLocation = dataSnapshot.getValue(UserLocation.class);
                        for (int i = 0; i < mClusterMarkers.size(); i++) {
                            try {
                                if (mClusterMarkers.get(i).getUserID().equals(updatedUserLocation.getUserID())) {

                                    LatLng updatedLatLng = new LatLng(
                                            updatedUserLocation.getLocation().getLatitude(),
                                            updatedUserLocation.getLocation().getLongitude()
                                    );
                                    //mClusterMarkers.get(i).setPosition(updatedLatLng);
                                    //mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(i));


                                    String time = DateFormat.format("dd-MM-yyyy (HH:mm:ss)",
                                            updatedUserLocation.getLocationTime()).toString();
                                    calculateDirections(updatedLatLng, mClusterMarkers.get(i).getUser(), i, time);


                                }


                            } catch (NullPointerException e) {
                                Log.e(TAG, "retrieveUserLocations: NullPointerException: " + e.getMessage());
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        }catch (IllegalStateException e){
            Log.e(TAG, "retrieveUserLocations: Fragment was destroyed during Firestore query. Ending query." + e.getMessage() );
        }

    }

    private void calculateDirections(LatLng markerPosition, String userName, int position, String time){
        Log.d(TAG, "calculateDirections: calculating directions.");

        //Marker Update Code (Position)
        mClusterMarkers.get(position).setPosition(markerPosition);
        //Update Code ends

        com.google.maps.model.LatLng destination = new com.google.maps.model.LatLng(
                markerPosition.latitude,
                markerPosition.longitude
        );
        DirectionsApiRequest directions = new DirectionsApiRequest(mGeoApiContext);




        directions.alternatives(false);
        directions.origin(
                new com.google.maps.model.LatLng(
                        mMeetupPosition.latitude,
                        mMeetupPosition.longitude
                )
        );
        Log.d(TAG, "calculateDirections: destination: " + destination.toString());
        directions.destination(destination).setCallback(new PendingResult.Callback<DirectionsResult>() {
            @Override
            public void onResult(DirectionsResult result) {
//                Log.d(TAG, "calculateDirections: routes: " + result.routes[0].toString());
//                Log.d(TAG, "calculateDirections: duration: " + result.routes[0].legs[0].duration);
//                Log.d(TAG, "calculateDirections: distance: " + result.routes[0].legs[0].distance);
//                Log.d(TAG, "calculateDirections: geocodedWayPoints: " + result.geocodedWaypoints[0].toString());

                Log.d(TAG, "onResult: successfully retrieved directions.");
                addPolylinesToMap(result, userName, position, time);
            }

            @Override
            public void onFailure(Throwable e) {
                Log.e(TAG, "calculateDirections: Failed to get directions: " + e.getMessage() );

            }
        });
    }

    private void addPolylinesToMap(final DirectionsResult result, String userName, int position, String time){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: result routes: " + result.routes.length);
                for(int i = 0 ; i<mPolyLinesData.size();i++){
                    if(mPolyLinesData.get(i).getUserName().equals(userName)){
                        mPolyLinesData.get(i).getPolyline().remove();
                        mPolyLinesData.remove(i);
                    }
                }

                //Marker Update Code (Duration)
                mClusterMarkers.get(position).setSnippet(result.routes[0].legs[0].duration.toString()+" away \n"+
                        "Last Updated: "+time);
                mClusterManagerRenderer.setUpdateMarker(mClusterMarkers.get(position));
                //Update Code ends


                for(DirectionsRoute route: result.routes){
                    Log.d(TAG, "run: leg: " + route.legs[0].toString());
                    List<com.google.maps.model.LatLng> decodedPath = PolylineEncoding.decode(route.overviewPolyline.getEncodedPath());

                    List<LatLng> newDecodedPath = new ArrayList<>();

                    // This loops through all the LatLng coordinates of ONE polyline.
                    for(com.google.maps.model.LatLng latLng: decodedPath){

//                        Log.d(TAG, "run: latlng: " + latLng.toString());

                        newDecodedPath.add(new LatLng(
                                latLng.lat,
                                latLng.lng
                        ));
                    }
                    Polyline polyline = mGoogleMap.addPolyline(new PolylineOptions().addAll(newDecodedPath));
                    if(userName.equals(((Rendezvous)(getApplicationContext())).getUserName())){
                        polyline.setColor(ContextCompat.getColor(LiveLocation.this, R.color.quantum_indigo300));
                    }
                    else{
                        polyline.setColor(ContextCompat.getColor(LiveLocation.this, R.color.quantum_grey400));
                    }
                    mPolyLinesData.add(new PolylineData(polyline, route.legs[0],userName));

                }


            }
        });
    }


    private void setCameraView() {

        // Set a boundary to start
        double bottomBoundary = mUserPosition.getLatitude() - .1;
        double leftBoundary = mUserPosition.getLongitude() - .1;
        double topBoundary = mUserPosition.getLatitude() + .1;
        double rightBoundary = mUserPosition.getLongitude() + .1;

        mMapBoundary = new LatLngBounds(
                new LatLng(bottomBoundary, leftBoundary),
                new LatLng(topBoundary, rightBoundary)
        );

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(mMapBoundary, 500,500,1));
    }
}


