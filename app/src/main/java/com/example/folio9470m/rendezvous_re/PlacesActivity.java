package com.example.folio9470m.rendezvous_re;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;

import com.example.folio9470m.rendezvous_re.adapters.CustomInfoWindowAdapter;
import com.example.folio9470m.rendezvous_re.models.LatLong;
import com.example.folio9470m.rendezvous_re.models.LocationVote;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PointOfInterest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.model.PhotoMetadata;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPhotoRequest;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranavpandey.android.dynamic.toasts.DynamicToast;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class PlacesActivity extends AppCompatActivity
        implements OnMapReadyCallback,
        GoogleMap.OnPoiClickListener,
        GoogleMap.OnCameraMoveStartedListener,
        GoogleMap.OnCameraIdleListener
{


    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final String TAG = "PlacesActivity";
    private static final float DEFAULT_ZOOM = 15f;
    private static final LatLngBounds LAT_LNG_BOUNDS = new LatLngBounds(
            new LatLng(-40, -168), new LatLng(71, 136));

    private EditText mSearchText;
    private ImageView mGps;
    private Button confirmButton;

    private Boolean mLocationPermissionsGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private PlacesClient placesClient;
    private Geocoder mGeocoder;
    private AutocompleteSupportFragment autocompleteFragment;
    private String locationName;
    private LatLng currentCoord;
    private String placeID;
    private String address;
    private String mID;
    private Rendezvous mApp;
    private boolean sentFromVotingActivity;
    private boolean sentFromMeetupSettings;
    private boolean addMarkerAtCameraCenter = false;
    private String setImage;
    private Switch markerSwitch;
    private boolean moveMarker = false;
    private ProgressBar progressBar;


    @Override
    public void onBackPressed() {
        if(sentFromVotingActivity){
            super.onBackPressed();
        }
        else{
            DynamicToast.makeWarning(this, "No location selected. Meetup creation failed!").show();
            FirebaseDatabase.getInstance().getReference().child("meetups").child(mID).removeValue();
            FirebaseDatabase.getInstance().getReference().child("users").child(mApp.getUserID())
                    .child("meetupArrayList").child(mID).removeValue();
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_places);

        setTitle("Select Location");

        mApp =((Rendezvous)getApplicationContext());
        mID = mApp.getMeetupID();

        Places.initialize(getApplicationContext(), "Google KEY Here");
        placesClient = Places.createClient(this);
        mGeocoder = new Geocoder(PlacesActivity.this, Locale.ENGLISH);
        autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.autocomplete_fragment);

        String senderActivityName = getIntent().getStringExtra("ACTIVITY_NAME_BUNDLE_ID");
        setImage = getIntent().getStringExtra("image");
        if(senderActivityName!=null && senderActivityName.equals("VotingActivity")){
            sentFromVotingActivity = true;
        }
        else{
            sentFromVotingActivity = false;
        }

        if(senderActivityName!=null && senderActivityName.equals("MeetupSettingActivity")){
            sentFromMeetupSettings = true;
        }
        else{
            sentFromMeetupSettings = false;
        }


        progressBar = findViewById(R.id.progressBar3);
        mGps = (ImageView) findViewById(R.id.ic_gps);
        markerSwitch = findViewById(R.id.switch1);
        markerSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    moveMarker = true;
                else
                    moveMarker = false;
            }
        });
        confirmButton = findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(v -> {
            if(currentCoord!=null && locationName!=null){
                if(sentFromVotingActivity){

                    String key = FirebaseDatabase.getInstance().getReference()
                            .child("voting")
                            .child(mID)
                            .child("votes")
                            .push()
                            .getKey();
                    LocationVote addVote = new LocationVote(locationName,
                            placeID,
                            new LatLong(currentCoord.latitude,currentCoord.longitude),key, address);
                    FirebaseDatabase.getInstance().getReference()
                            .child("voting")
                            .child(mID)
                            .child("votes")
                            .child(key)
                            .setValue(addVote);
                    finish();
                }
                else{
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("meetups").child(mID);
                    myRef.child("coordinates").setValue(currentCoord);
                    myRef.child("location").setValue(locationName);
                    myRef.child("placeID").setValue(placeID);
                    myRef.child("address").setValue(address);
                    if(sentFromMeetupSettings){
                        Intent intent1 = new Intent(PlacesActivity.this, MeetupSettingActivity.class);
                        intent1.putExtra("id",mID);
                        startActivity(intent1);
                        finish();
                    }
                    DynamicToast.makeSuccess(this, "Meetup successfully created! Redirecting.....").show();
                    if(setImage!= null && setImage.equals("no") && !placeID.equals("customplace")){
                        savePictureToStorageDatabase();
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                startActivity(new Intent(PlacesActivity.this,InvitePeopleActivity.class));
                            }
                        }, 2000);
                    }
                    else{
                        new Timer().schedule(new TimerTask() {
                            @Override
                            public void run() {
                                startActivity(new Intent(PlacesActivity.this,InvitePeopleActivity.class));
                                finish();
                            }
                        }, 2000);
                    }


                }

            }
            else{
                Toast.makeText(PlacesActivity.this, "No Location Selected", Toast.LENGTH_SHORT).show();

            }

        });
        getLocationPermission();
        initMap();
    }

    private void savePictureToStorageDatabase() {
        List<Place.Field> fields = Arrays.asList(Place.Field.PHOTO_METADATAS);
        FetchPlaceRequest placeRequest = FetchPlaceRequest.builder(placeID, fields).build();
        placesClient.fetchPlace(placeRequest).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            // Get the photo metadata.
            if(place.getPhotoMetadatas() != null){
                PhotoMetadata photoMetadata = place.getPhotoMetadatas().get(0);
                // Get the attribution text.
                String attributions = photoMetadata.getAttributions();
                // Create a FetchPhotoRequest.
                FetchPhotoRequest photoRequest = FetchPhotoRequest.builder(photoMetadata)
                        .build();
                placesClient.fetchPhoto(photoRequest).addOnSuccessListener((fetchPhotoResponse) -> {
                    Bitmap bitmap = fetchPhotoResponse.getBitmap();
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    byte[] data = baos.toByteArray();
                    StorageReference riversRef = FirebaseStorage.getInstance().getReference().child("images/"+mID+"/placeimage");
                    String path = "images/"+mID+"/placeimage";

                    UploadTask uploadTask = riversRef.putBytes(data);
                    uploadTask.addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("meetups").child(mID);
                            myRef.child("imagepath").setValue(path);
                            finish();
                        }
                    });
                }).addOnFailureListener((exception) -> {
                    if (exception instanceof ApiException) {
                        ApiException apiException = (ApiException) exception;
                        int statusCode = apiException.getStatusCode();
                        // Handle error with given status code.
                        Log.e(TAG, "Place not found: " + exception.getMessage());
                    }
                });
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");
        mLocationPermissionsGranted = false;

        switch(requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length > 0){
                    for(int i = 0; i < grantResults.length; i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                }
            }
        }
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;
        mMap.setOnPoiClickListener(this);
        mMap.setOnCameraIdleListener(this);
        mMap.setOnCameraMoveStartedListener(this);
        if (mLocationPermissionsGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
        init();
    }
    private void initMap(){
        Log.d(TAG, "initMap: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(PlacesActivity.this);
    }
    private void init(){
        Log.d(TAG, "init: initializing");

        // Specify the types of place data to return.
        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.RATING));
        autocompleteFragment.setCountry("PK"); // Results will be shown only from Pakistan

        // Set up a PlaceSelectionListener to handle the response.
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId()+ place.getAddress());
                moveCamera(place.getLatLng(), DEFAULT_ZOOM,
                        place);
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });
        mGps.setOnClickListener(view -> {
            Log.d(TAG, "onClick: clicked gps icon");
            getDeviceLocation();
        });
    }

    private void getDeviceLocation(){
        Log.d(TAG, "getDeviceLocation: getting the devices current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try{
            if(mLocationPermissionsGranted){
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(task -> {
                    if(task.isSuccessful()){
                        Log.d(TAG, "onComplete: found location!");
                        Location currentLocation = (Location) task.getResult();
                        if(currentLocation == null){
                            Toast.makeText(PlacesActivity.this, "unable to get current location. Random location set to Lahore", Toast.LENGTH_LONG).show();

                            moveCamera(new LatLng(31.5204,74.3587),
                                    DEFAULT_ZOOM, "Lahore");
                        }
                        else{
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
                                    DEFAULT_ZOOM, "My Location");
                        }


                    }else{
                        Toast.makeText(PlacesActivity.this, "unable to get current location. Random location set to Lahore", Toast.LENGTH_SHORT).show();

                        moveCamera(new LatLng(31.5204,74.3587),
                                DEFAULT_ZOOM, "Lahore");

                        Log.d(TAG, "onComplete: current location is null");
                    }
                });
            }
        }catch (SecurityException e){
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
        }
    }
    private void moveCamera(LatLng latLng, float zoom ,String title){
        mMap.clear();
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(PlacesActivity.this));
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(title);
        Marker myMarker=  mMap.addMarker(options);
        try{
            address = getAddressByCoordinates(myMarker.getPosition().latitude,
                    myMarker.getPosition().longitude);
            myMarker.setSnippet("Address: "+ address );
        }
        catch (IOException o){
            address = "Could not retrieve";
            Log.e(TAG, "moveCamera: GeoCoder IOException", o );
        }
        myMarker.showInfoWindow();
        locationName = title;
        currentCoord = latLng;
        placeID = "customplace";
    }

    private void moveCamera(LatLng latLng, float zoom ,Place place){
        mMap.clear();
        Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(PlacesActivity.this));

        String snippet = "Address : " + place.getAddress()+ "\n" +
                "Place Rating : " + place.getRating();

        Marker myMarker;
        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title(place.getName())
                .snippet(snippet);
        mMap.addMarker(options).showInfoWindow();

        locationName = place.getName();
        currentCoord = latLng;
        placeID = place.getId();
        address = place.getAddress();
    }


    private void getLocationPermission() {
        Log.d(TAG, "getLocationPermission: getting location permissions");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionsGranted = true;
            }else{
                ActivityCompat.requestPermissions(this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else{
            ActivityCompat.requestPermissions(this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }
    @Override
    public void onPoiClick(PointOfInterest poi) {
        progressBar.setVisibility(View.VISIBLE);
        String placeId = poi.placeId;
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID,
                Place.Field.NAME,
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.RATING);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();
        placesClient.fetchPlace(request).addOnSuccessListener((response) -> {
            Place place = response.getPlace();
            Log.d(TAG, "Place found: " + place.getName());
            progressBar.setVisibility(View.GONE);
            moveCamera(place.getLatLng(),mMap.getCameraPosition().zoom,place);
        }).addOnFailureListener((exception) -> {
            if (exception instanceof ApiException) {
                ApiException apiException = (ApiException) exception;
                int statusCode = apiException.getStatusCode();
                // Handle error with given status code.
                Log.e(TAG, "Place not found: " +statusCode, exception);
            }
        });
    }
    @Override
    public void onCameraMoveStarted(int reason) {

        if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
            // Only add the Marker when user themselves have moved the map
            addMarkerAtCameraCenter = true;
            Log.i(TAG, "onCameraMoveStarted: User moved the map");
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_API_ANIMATION) {
            addMarkerAtCameraCenter = false;
        } else if (reason == GoogleMap.OnCameraMoveStartedListener
                .REASON_DEVELOPER_ANIMATION) {
            addMarkerAtCameraCenter = false;
        }
    }

    @Override
    public void onCameraIdle() {
        if(addMarkerAtCameraCenter && moveMarker){
            moveCamera(mMap.getCameraPosition().target,mMap.getCameraPosition().zoom,"Unidentified Place");
        }
    }

    private String getAddressByCoordinates(double lat, double lon) throws IOException {

        List<Address> addresses = mGeocoder.getFromLocation(lat, lon, 1);
        if (addresses != null && addresses.size() > 0) {
            return addresses.get(0).getAddressLine(0);
        }
        return null;
    }




}
