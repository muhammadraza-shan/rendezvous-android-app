package com.example.folio9470m.rendezvous_re;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.amalbit.trail.Route;
import com.amalbit.trail.RouteOverlayView;
import com.example.folio9470m.rendezvous_re.util.ViewWeightAnimationWrapper;
import com.example.folio9470m.rendezvous_re.models.LatLong;
import com.example.folio9470m.rendezvous_re.models.PolygonCentroid;
import com.example.folio9470m.rendezvous_re.models.UserLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.GeoApiContext;
import com.google.maps.PlacesApi;
import com.google.maps.model.PlaceType;
import com.google.maps.model.PlacesSearchResponse;
import com.google.maps.model.PlacesSearchResult;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class IntelligentLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String MAPVIEW_BUNDLE_KEY = "MapViewBundleKey";
    private static final String TAG = "Intelligent Location Activity";

    private MapView mMapView;
    private RelativeLayout mMapContainer;
    private LinearLayout mLinearLayout;


    private String meetupID;
    private Rendezvous mApp;
    private ArrayList<String> listOfUserIDsInMeetup = new ArrayList();
    private ArrayList<UserLocation> userLocationsList = new ArrayList();
    private DatabaseReference currentMeetupReference;
    private DatabaseReference locationReference;
    private GoogleMap mMap;
    private RouteOverlayView mRouteOverlayView;
    private GeoApiContext mGeoApiContext;
    private Geocoder mGeocoder;

    private boolean placeFound;
    private LatLong centroid;
    private String placeName;
    private LatLong placeCoordinates;
    private String placeID;
    private String placeAddress;



    private TextView placeFoundText;
    private TextView nameOutputText;
    private TextView addressOutputText;
    private TextView cityOutputText;
    private TextView ratingOutputText;
    private Button confirmButton;
    private Button closeButton;

    private ProgressBar progressBar;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intelligent_location);
        setTitle("Location Decider");

        mApp = ((Rendezvous)getApplicationContext());
        meetupID = mApp.getMeetupID();
        placeFound = false;

        currentMeetupReference = FirebaseDatabase.getInstance().getReference().child("meetups").child(meetupID);
        locationReference = FirebaseDatabase.getInstance().getReference().child("userlocations");

        mRouteOverlayView = findViewById(R.id.mapOverlayView);
        mMapContainer = findViewById(R.id.map_container);
        mLinearLayout = findViewById(R.id.intel_linear_layout);
        
        placeFoundText =findViewById(R.id.place_found_text);
        nameOutputText = findViewById(R.id.locationText);
        addressOutputText = findViewById(R.id.addressText);
        cityOutputText = findViewById(R.id.cityText);
        ratingOutputText = findViewById(R.id.ratingText);
        confirmButton = findViewById(R.id.confirm_intel_button);
        closeButton = findViewById(R.id.close_button_intel);
        progressBar = findViewById(R.id.progressBar);

        mGeocoder = new Geocoder(IntelligentLocationActivity.this, Locale.ENGLISH);
        mGeoApiContext = new GeoApiContext.Builder()
                .apiKey("Key Here")
                .build();

        confirmButton.setOnClickListener(v -> {
            if(placeFound){
                //Save Place Into Current Meetup Database
                currentMeetupReference.child("coordinates").setValue(placeCoordinates);
                currentMeetupReference.child("location").setValue(placeName);
                currentMeetupReference.child("placeID").setValue(placeID);
                currentMeetupReference.child("address").setValue(placeAddress);
                currentMeetupReference.child("locationMethod").setValue(0);
                Intent intent = new Intent(IntelligentLocationActivity.this, MeetupOverviewNav.class);
                intent.putExtra("id", meetupID);
                startActivity(intent);
                finish();
            }
            else{
                DynamicToast.makeError(this,"Could not find an optimal place around the central location").show();
                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Intent intent = new Intent(IntelligentLocationActivity.this, MeetupOverviewNav.class);
                        startActivity(intent);
                        finish();
                    }
                }, 2000);

            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(IntelligentLocationActivity.this, MeetupOverviewNav.class);
                intent.putExtra("id", meetupID);
                startActivity(intent);
                finish();
            }
        });

        Toast.makeText(this, "Retrieving User Locations",Toast.LENGTH_SHORT).show();


       // expandMapAnimation();
       // expandMapAnimation();
        initGoogleMap(savedInstanceState);

    }



    private void retrieveDataFromDatabase() {
        currentMeetupReference.child("members").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    listOfUserIDsInMeetup.add(snapshot.getKey());
                }
                locationReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for(String usersID : listOfUserIDsInMeetup){
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                                if(snapshot.getKey().equals(usersID)){
                                    userLocationsList.add(snapshot.getValue(UserLocation.class));
                                }
                            }
                        }
                        progressBar.setVisibility(View.GONE);
                        showLocationsOnMap();
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                    }
                });
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

    }

    private void showLocationsOnMap() {

        List<LatLong> centroidPoints = new ArrayList();
        LatLngBounds.Builder latlngBounds = LatLngBounds.builder();
        for(UserLocation userLocation : userLocationsList){
            MarkerOptions marker = new MarkerOptions()
                    .position(new LatLng(userLocation.getLocation().getLatitude(),userLocation.getLocation().getLongitude()))
                    .title(userLocation.getName());
            mMap.addMarker(marker);
            centroidPoints.add(userLocation.getLocation());

            latlngBounds.include(new LatLng(userLocation.getLocation().getLatitude(),userLocation.getLocation().getLongitude()));

        }
        //Toast.makeText(this, "Calculating Center Point....", Toast.LENGTH_LONG).show();
        DynamicToast.make(this,
                "Calculating Center Point...",
                Color.parseColor("#FFFFFF"),
                Color.parseColor("#4CAF50"),
                Toast.LENGTH_LONG)
                .show();

        if(centroidPoints.size() == 2){
            centroid = midPoint(centroidPoints.get(0).getLatitude(),centroidPoints.get(0).getLongitude(),
                    centroidPoints.get(1).getLatitude(),centroidPoints.get(1).getLongitude());
        }
        else{
            PolygonCentroid polygonCentroid = new PolygonCentroid(centroidPoints);
            centroid = polygonCentroid.centroid();
        }

        LatLngBounds bounds = latlngBounds.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 0));



        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                for(UserLocation userLocation : userLocationsList){
                    List<LatLng> latLngs = new ArrayList<>();
                    latLngs.add(new LatLng(userLocation.getLocation().getLatitude(),userLocation.getLocation().getLongitude()));
                    latLngs.add(new LatLng(centroid.getLatitude(),centroid.getLongitude()));
                    latLngs.add(new LatLng(centroid.getLatitude(),centroid.getLongitude()));
                    Route normalRoute = new Route.Builder(mRouteOverlayView)
                            .setRouteType(RouteOverlayView.RouteType.PATH)
                            .setCameraPosition(mMap.getCameraPosition())
                            .setProjection(mMap.getProjection())
                            .setLatLngs(latLngs)
                            .setBottomLayerColor(Color.LTGRAY)
                            .setTopLayerColor(Color.BLACK)
                            .create();
                }
                nearbySearchAroundCenter();
            }
        }, 3000);
    }

    private void nearbySearchAroundCenter() {
        try{
            ArrayList<PlacesSearchResult> nearbyPlaces = new ArrayList();
            PlacesSearchResponse result = PlacesApi
                    .nearbySearchQuery(mGeoApiContext, new com.google.maps.model.LatLng(centroid.getLatitude(),centroid.getLongitude()))
                    .radius(5000)
                    .type(PlaceType.RESTAURANT)
                    .await();

            int length = result.results.length;

            if(result.results.length!=0){
                Log.d(TAG, "nearbySearchAroundCenter: Found Places results within 5km radius");
                for(PlacesSearchResult placesSearchResult : result.results){
                    nearbyPlaces.add(placesSearchResult);
                }
                showResultOnMap(nearbyPlaces);

            }
            else{ // Results Not Found the First Time
                PlacesSearchResponse result25k = PlacesApi
                        .nearbySearchQuery(mGeoApiContext, new com.google.maps.model.LatLng(centroid.getLatitude(),centroid.getLongitude()))
                        .radius(25000)
                        .type(PlaceType.RESTAURANT)
                        .await();
                if(result25k.results.length!=0){
                    Log.d(TAG, "nearbySearchAroundCenter: Found Places results within 25km radius");
                    for(PlacesSearchResult placesSearchResult : result25k.results){
                        nearbyPlaces.add(placesSearchResult);
                    }
                    showResultOnMap(nearbyPlaces);
                }
                else{//Results Not Found the Second Time
                    PlacesSearchResponse result50k = PlacesApi
                            .nearbySearchQuery(mGeoApiContext, new com.google.maps.model.LatLng(centroid.getLatitude(),centroid.getLongitude()))
                            .radius(50000)
                            .type(PlaceType.RESTAURANT)
                            .await();
                    if(result50k.results.length!=0){
                        Log.d(TAG, "nearbySearchAroundCenter: Found places results within 50km radius");
                        for(PlacesSearchResult placesSearchResult : result50k.results){
                            nearbyPlaces.add(placesSearchResult);
                        }
                        showResultOnMap(nearbyPlaces);
                    }
                    else{//NO RESULTS FOUND AT ALL
                        //ADD CODE FOR PLACE NOT FOUND ETC
                        Log.d(TAG, "nearbySearchAroundCenter: Could not find results. Closing down Activity...");
                        showPlaceNotFound();
                    }
                }
            }

         /*   if(result.nextPageToken != null && !result.nextPageToken.equals("")){
                Log.d(TAG, "Second Page Found. Retrieving Results");
                PlacesSearchResponse nextResult = PlacesApi
                        .nearbySearchNextPage(mGeoApiContext,result.nextPageToken)
                        .await();
                for(PlacesSearchResult placesSearchResult : nextResult.results){
                    nearbyPlaces.add(placesSearchResult);
                }

                if(nextResult.nextPageToken != null && !nextResult.nextPageToken.equals("")){

                    PlacesSearchResponse finalResult = PlacesApi
                            .nearbySearchNextPage(mGeoApiContext,result.nextPageToken)
                            .await();
                    for(PlacesSearchResult placesSearchResult : finalResult.results){
                        nearbyPlaces.add(placesSearchResult);
                    }
                    Log.d(TAG,"Third Page Found. Retrieving Results");

                }

            }*/



        }
        catch (Exception e){
            Log.e(TAG, "Exception thrown"+e.getMessage() );
        }
    }

    private void showPlaceNotFound() {
        currentMeetupReference.child("creator").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String creator = dataSnapshot.getValue(String.class);
                mApp.setMeetupID(meetupID);
                if(creator.equals(mApp.getUserID())){
                    DynamicToast.makeWarning(IntelligentLocationActivity.this,
                            "Unable to Calculate Place. Please Select Manually",
                            Toast.LENGTH_LONG).show();
                    mMap.clear();
                    mRouteOverlayView.removeRoutes();
                    final Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            startActivity(new Intent(IntelligentLocationActivity.this,PlacesActivity.class));
                        }
                    }, 2000);
                }
                else{
                    DynamicToast.makeWarning(IntelligentLocationActivity.this,
                            "Unable to Calculate Place. Returning to Meetup Screen",
                            Toast.LENGTH_LONG).show();
                    finish();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void showResultOnMap(ArrayList<PlacesSearchResult> nearbyPlaces) {
        placeFound = true;
        placeCoordinates = new LatLong(nearbyPlaces.get(0).geometry.location.lat,nearbyPlaces.get(0).geometry.location.lng);
        placeName = nearbyPlaces.get(0).name;
        placeID = nearbyPlaces.get(0).placeId;
        placeAddress = nearbyPlaces.get(0).vicinity;

        Log.d(TAG, "showResultOnMap: Showing Place Results on Map");
        Log.d(TAG, "showResultOnMap: Place coordinates: "+nearbyPlaces.get(0).geometry.location.toString());
        Collections.sort(nearbyPlaces, new Comparator<PlacesSearchResult>() {
            @Override
            public int compare(PlacesSearchResult o1, PlacesSearchResult o2) {
                return Double.compare(o1.rating,o2.rating);
            }
        });
        Collections.reverse(nearbyPlaces);
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                DynamicToast.make(IntelligentLocationActivity.this,
                        "Place Found",
                        Color.parseColor("#FFFFFF"),
                        Color.parseColor("#4CAF50"),
                        Toast.LENGTH_LONG)
                        .show();
                contractMapAnimation();
                contractMapAnimation();
                mMap.clear();
                mRouteOverlayView.removeRoutes();
                com.google.maps.model.LatLng position = nearbyPlaces.get(0).geometry.location;
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(position.lat,position.lng), 14f));
                MarkerOptions marker = new MarkerOptions()
                        .position(new LatLng(position.lat,position.lng))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                        .title(nearbyPlaces.get(0).name);
                mMap.addMarker(marker).showInfoWindow();

                nameOutputText.setText(nearbyPlaces.get(0).name);
                addressOutputText.setText(nearbyPlaces.get(0).vicinity);
                ratingOutputText.setText(String.valueOf(nearbyPlaces.get(0).rating)+" out of 5.0");
                try{
                    String cityName = getCityNameByCoordinates(position.lat,position.lng);
                    if(cityName != null && !cityName.equals("")){
                        cityOutputText.setVisibility(View.VISIBLE);
                        cityOutputText.setText("City: " +cityName);
                    }
                }
                catch(IOException e){
                    Log.e(TAG,"GeoCoder IOException");
                }

            }
        }, 3000);

    }


    private String getCityNameByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getLocality();
        }
        return null;
    }


    private void initGoogleMap(Bundle savedInstanceState) {
        Bundle mapViewBundle = null;
        if (savedInstanceState != null) {
            mapViewBundle = savedInstanceState.getBundle(MAPVIEW_BUNDLE_KEY);
        }
        mMapView = (MapView) findViewById(R.id.user_list_map);
        mMapView.onCreate(mapViewBundle);

        mMapView.getMapAsync(this);
    }


    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnCameraMoveListener(() -> {
                    mRouteOverlayView.onCameraMove(mMap.getProjection(), mMap.getCameraPosition());
                }
        );
        mMap.setOnMapLoadedCallback(() -> {
            retrieveDataFromDatabase();
        });
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Bundle mapViewBundle = outState.getBundle(MAPVIEW_BUNDLE_KEY);
        if (mapViewBundle == null) {
            mapViewBundle = new Bundle();
            outState.putBundle(MAPVIEW_BUNDLE_KEY, mapViewBundle);
        }

        mMapView.onSaveInstanceState(mapViewBundle);
    }

    private void expandMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                50,
                100);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mLinearLayout);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                50,
                0);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }
    private void contractMapAnimation(){
        ViewWeightAnimationWrapper mapAnimationWrapper = new ViewWeightAnimationWrapper(mMapContainer);
        ObjectAnimator mapAnimation = ObjectAnimator.ofFloat(mapAnimationWrapper,
                "weight",
                100,
                50);
        mapAnimation.setDuration(800);

        ViewWeightAnimationWrapper recyclerAnimationWrapper = new ViewWeightAnimationWrapper(mLinearLayout);
        ObjectAnimator recyclerAnimation = ObjectAnimator.ofFloat(recyclerAnimationWrapper,
                "weight",
                0,
                50);
        recyclerAnimation.setDuration(800);

        recyclerAnimation.start();
        mapAnimation.start();
    }




    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mMapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMapView.onStop();
    }


    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }
    public static LatLong midPoint(double lat1,double lon1,double lat2,double lon2){

        double dLon = Math.toRadians(lon2 - lon1);

        //convert to radians
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);
        lon1 = Math.toRadians(lon1);

        double Bx = Math.cos(lat2) * Math.cos(dLon);
        double By = Math.cos(lat2) * Math.sin(dLon);
        double lat3 = Math.atan2(Math.sin(lat1) + Math.sin(lat2), Math.sqrt((Math.cos(lat1) + Bx) * (Math.cos(lat1) + Bx) + By * By));
        double lon3 = lon1 + Math.atan2(By, Math.cos(lat1) + Bx);


        //System.out.println( + " " + );
        return new LatLong(Math.toDegrees(lat3),Math.toDegrees(lon3));
    }

}

